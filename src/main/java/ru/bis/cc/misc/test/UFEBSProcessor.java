package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/** Class for UFEBS proceessing
 *
 */
class UFEBSProcessor extends XMLProcessor {

  UFEBSProcessor() {
    logger = LogManager.getLogger(UFEBSProcessor.class);
  }

  /**
   * Process one UFEBS file, fills docs array
   *
   * @param fileName - file name to parse (full path)
   * @param docs = array with documents
   * @return boolean: success or fail for file processing (true/false)
   */
  boolean readFile(String fileName, FDocumentArray docs) {
    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = root.getNodeName();
      if (rootNodeName.equals("PacketEPD")) { // For packet EPD
        NodeList eds = root.getChildNodes();
        for (int i = 0; i < eds.getLength(); i++) {
          // Each node: ED, empty text etc
          Node ed = eds.item(i);
          if (ed.getNodeType() != Node.TEXT_NODE) {
            String nodeName = ed.getNodeName();
            if (nodeName.matches("ED10[134]")) {
              FDocument doc = UFEBSParser.fromXML(ed);
              docs.add(doc);
            }
            else {
              logger.error("0801: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              return false;
            }
          }
        }
        return true;
      }
      else if (rootNodeName.matches("ED10[134]")) { // For single EPD
        FDocument doc = UFEBSParser.fromXML(root);
        docs.add(doc);
        return true;
      }
      else {
        logger.error("0802: File " + fileName + " contains unknown root element: " + rootNodeName);
      }
      return false;
    }
    catch (ParserConfigurationException | SAXException e) {
      logger.error("0803: Error parsing file " + fileName, e);
    }
    catch (IOException e) {
      logger.error("0804. Error while file access: " + fileName);
    }
    return false;
  }

  /** Function returns string with packet root or null (if documents array has no non-urgent documents).
   * It determines outgoind or incoming packet by first non-urgent document
   *
   * @param docs - documents array
   * @param rootName - name of root element ("PacketED" or "PackedESID")
   * @param edNo - EDNo for packet
   * @return = string with root element of packet
   */
  private String getPacketRoot(FDocumentArray docs, String rootName, String edNo) {
    // Has documents array contains non-urgent payments? Calculate count and total sum of all documents in packet
    int packetCount = 0;
    Long packetSum = 0L;
    String packetDate = "";

    for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
      FDocument doc = item.getValue();
      if (!doc.isUrgent) {
        packetCount++;
        packetSum += doc.amount;
        packetDate = doc.docDate;
      }
    }
    if (packetCount > 0) {
      StringBuilder str = new StringBuilder(getProlog());
      str.append(System.lineSeparator());
      str.append("<"); str.append(rootName);
      String edAuthor = Constants.ourBankUIS;
      String edReceiver = Constants.otherBankUIS;
      if (docs.isReversePacket) { edAuthor = Constants.otherBankUIS; edReceiver = Constants.ourBankUIS; }

      str.append(" EDAuthor=\""); str.append(edAuthor); str.append("\"");
      str.append(" EDReceiver=\""); str.append(edReceiver); str.append("\"");
      str.append(" EDDate=\""); str.append(packetDate); str.append("\"");
      str.append(" EDNo=\""); str.append(edNo); str.append("\"");
      str.append(" EDQuantity=\""); str.append(packetCount); str.append("\"");
      if (rootName.equals("PacketEPD")) {
        str.append(" Sum=\""); str.append(packetSum); str.append("\"");
        str.append(" SystemCode=\"02\"");
      }
      str.append(" xmlns=\"urn:cbr-ru:ed:v2.0\">");

      return str.toString();
    }
    else
      return null;
  }

  /**
   * Creates UFEBS files for all specified documents array: urgent payments place into individual files,
   * non-urgent documents places in common packet file
   *
   * @param outPath = path for create UFEBS files
   * @param docs   - documents array reference
   */
  void createAll(String outPath, FDocumentArray docs) {

    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0805: Error access output directory " + outPath);
      return;
    }
    try {
      BufferedWriter packetWriter = null;
      String rootElement = getPacketRoot(docs, "PacketEPD", "1000000");
      if (rootElement != null) {
        String outPacketFile = outPath + (docs.isReversePacket ? "pki1000000.xml" : "pko1000000.xml");
        OutputStream osp = new FileOutputStream(outPacketFile);
        packetWriter = new BufferedWriter(new OutputStreamWriter(osp, getCodePage()));
        packetWriter.write(rootElement + System.lineSeparator());
      }
      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        if (doc.isUrgent) {
          String outFile = outPath + (doc.payerBankBIC.equals(Constants.ourBankBIC)? "out" : "inc") + String.format("%07d", doc.getId()) + ".xml";
          OutputStream oss = new FileOutputStream(outFile);
          BufferedWriter singleWriter = new BufferedWriter(new OutputStreamWriter(oss, getCodePage()));
          singleWriter.write(getProlog() + System.lineSeparator());
          String str = UFEBSParser.toString(doc);
          singleWriter.write(str);
          singleWriter.close();
        }
        else {
          String str = UFEBSParser.toString(doc);
          if (packetWriter != null) packetWriter.write(str);
        }
      }
      if (packetWriter != null) {
        packetWriter.write("</PacketEPD>" + System.lineSeparator());
        packetWriter.close();
      }
    }
    catch (IOException e) {
      logger.error("0806: Error write output file with ED.");
    }
    logger.info("0807: Output UFEBS files created.");
  }

  /** Function generates ED206 confirmations by documents array
   *
   * @param outPath - path to output XML files
   * @param docs - documents array
   */
  void createConfirmations(String outPath, FDocumentArray docs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0810: Error access output directory " + outPath);
      return;
    }
    try {
      BufferedWriter packetWriter = null;
      String rootElement = getPacketRoot(docs, "PacketESID", "2000000");
      if (rootElement != null) {
        String outPacketFile = outPath + (docs.isReversePacket ? "ppi2000000.xml" : "ppo2000000.xml");
        OutputStream osp = new FileOutputStream(outPacketFile);
        packetWriter = new BufferedWriter(new OutputStreamWriter(osp, getCodePage()));
        packetWriter.write(rootElement + System.lineSeparator());
      }
      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        if (doc.isUrgent) {
          String outFile = outPath + (doc.payerBankBIC.equals(Constants.ourBankBIC)? "pco" : "pci") + String.format("%07d", doc.getId()) + ".xml";
          OutputStream oss = new FileOutputStream(outFile);
          BufferedWriter singleWriter = new BufferedWriter(new OutputStreamWriter(oss, getCodePage()));
          singleWriter.write(getProlog() + System.lineSeparator());
          String str = UFEBSParser.toConfirmation(doc);
          singleWriter.write(str);
          singleWriter.close();
        }
        else {
          String str = UFEBSParser.toConfirmation(doc);
          if (packetWriter != null) packetWriter.write(str);
        }
      }
      if (packetWriter != null) {
        packetWriter.write("</PacketESID>" + System.lineSeparator());
        packetWriter.close();
      }
    }
    catch (IOException e) {
      logger.error("0811: Error write output file with confirmation.");
    }
    logger.info("0812: UFEBS confirmations created.");
  }

  /** Function generates ED211 statement by documents array
   *
   * @param outPath - path to output XML files
   * @param docs - documents array
   * @param revs - reversed documents array (or null)
   */
  void createStatement(String outPath, FDocumentArray docs, FDocumentArray revs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0813: Error access output directory " + outPath);
      return;
    }
    try {
      String outPacketFile = outPath + "stm3000000.xml";
      OutputStream osp = new FileOutputStream(outPacketFile);
      BufferedWriter packetWriter = new BufferedWriter(new OutputStreamWriter(osp, getCodePage()));

      String date = docs.getDate();
      String edAuthor = Constants.otherBankUIS;
      String edReceiver = Constants.ourBankUIS;

      StringBuilder str = new StringBuilder();
      str.append(getProlog()); str.append(System.lineSeparator());
      str.append("<PacketESID ");
      str.append(" EDNo=\"3000000\"");
      str.append(" EDDate=\""); str.append(date); str.append("\"");
      str.append(" EDAuthor=\""); str.append(edAuthor); str.append("\"");
      str.append(" EDReceiver=\""); str.append(edReceiver); str.append("\"");
      str.append(" EDQuantity=\"1\"");
      str.append(" xmlns=\"urn:cbr-ru:ed:v2.0\">");
      str.append("<ED211 ");
      str.append(" EDNo=\"4000000\"");
      str.append(" EDDate=\""); str.append(date); str.append("\"");
      str.append(" EDAuthor=\""); str.append(edAuthor); str.append("\"");
      str.append(" EDReceiver=\""); str.append(edReceiver); str.append("\"");
      str.append(" AbstractKind=\"0\"");
      str.append(" LastMovetDate=\""); str.append(date); str.append("\"");
      str.append(" AbstractDate=\""); str.append(date); str.append("\"");
      str.append(" BeginTime=\"01:00:00\"");
      str.append(" EndTime=\"23:59:59\"");
      str.append(" BIC=\""); str.append(Constants.ourBankBIC); str.append("\"");
      str.append(" Acc=\""); str.append(Constants.ourBankAccPass); str.append("\"");

      long debet = docs.getSum();
      long credit = revs != null? revs.getSum(): 0L;
      long inRest = debet + 10000L;
      long outRest = inRest - debet + credit;
      str.append(" EnterBal=\""); str.append(inRest); str.append("\"");
      str.append(" OutBal=\""); str.append(outRest); str.append("\"");
      if (debet > 0) { str.append(" DebetSum=\""); str.append(debet); str.append("\""); }
      if (credit > 0) { str.append(" CreditSum=\""); str.append(credit); str.append("\""); }
      str.append(" >");

      packetWriter.write(str + System.lineSeparator());

      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        String line = UFEBSParser.toStatement(doc) + System.lineSeparator();
        packetWriter.write(line);
      }
      if (revs != null) {
        for (Map.Entry<Long, FDocument> item : revs.docs.entrySet()) {
          FDocument doc = item.getValue();
          String line = UFEBSParser.toStatement(doc) + System.lineSeparator();
          packetWriter.write(line);
        }
      }

      packetWriter.write("</ED211>");
      packetWriter.write("</PacketESID>" + System.lineSeparator());
      packetWriter.close();
    }
    catch (IOException e) {
      logger.error("0814: Error write output file with confirmation.");
    }
    logger.info("0815: UFEBS statement created.");
  }

}
