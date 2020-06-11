package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;
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

  private String codePage = "utf-8";

  UFEBSProcessor(Logger logger) {
    super(logger);
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
              docs.add(doc, logger);
            }
            else {
              logger.error("0512: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              return false;
            }
          }
        }
        return true;
      }
      else if (rootNodeName.matches("ED10[134]")) { // For single EPD
        FDocument doc = UFEBSParser.fromXML(root);
        docs.add(doc, logger);
        return true;
      }
      else {
        logger.error("0514: File " + fileName + " contains unknown root element: " + rootNodeName);
      }
      return false;
    }
    catch (ParserConfigurationException | SAXException e) {
      logger.error("0515: Error parsing file " + fileName, e);
    }
    catch (IOException e) {
      logger.error("0516. Error while file access: " + fileName);
    }
    return false;
  }

  /** Function sets specified code page for XML output
   *
   * @param codePage - code page name ("windows-1251" or "utf-8" allowed)
   */
  void setCodePage(String codePage) {
    this.codePage = codePage;
  }

  /** Function returns string with XML prolog
   *
   * @return - string with prolog with code page
   */
  private String getProlog() {
    return "<?xml version=\"1.0\" encoding=\"" + codePage + "\" ?>";
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
      str.append("<"); str.append(rootName);
      String edAuthor = "4525101000";
      String edReceiver = "4525225000";
      if (docs.isReversePacket) { edAuthor = "4525225000"; edReceiver = "4525101000"; }

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
      logger.error("0520: Error access output directory " + outPath);
      return;
    }
    try {
      BufferedWriter packetWriter = null;
      String rootElement = getPacketRoot(docs, "PacketEPD", "1000000");
      if (rootElement != null) {
        String outPacketFile = outPath + (docs.isReversePacket ? "pki1000000.xml" : "pko1000000.xml");
        OutputStream osp = new FileOutputStream(outPacketFile);
        packetWriter = new BufferedWriter(new OutputStreamWriter(osp, codePage));
        packetWriter.write(rootElement + System.lineSeparator());
      }
      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        if (doc.isUrgent) {
          String outFile = outPath + (doc.payerBankBIC.equals("044525101")? "out" : "inc") + String.format("%07d", doc.getId()) + ".xml";
          OutputStream oss = new FileOutputStream(outFile);
          BufferedWriter singleWriter = new BufferedWriter(new OutputStreamWriter(oss, codePage));
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
      logger.error("0521: Error write output file with ED.");
    }
    logger.info("0522: Output UFEBS files created.");
  }

  /** Function generates ED206 confirmations by documents array
   *
   * @param outPath - path to output XML files
   * @param docs - documents array
   */
  void createConfirmations(String outPath, FDocumentArray docs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0520: Error access output directory " + outPath);
      return;
    }
    try {
      BufferedWriter packetWriter = null;
      String rootElement = getPacketRoot(docs, "PacketESID", "2000000");
      if (rootElement != null) {
        String outPacketFile = outPath + (docs.isReversePacket ? "ppi2000000.xml" : "ppo2000000.xml");
        OutputStream osp = new FileOutputStream(outPacketFile);
        packetWriter = new BufferedWriter(new OutputStreamWriter(osp, codePage));
        packetWriter.write(rootElement + System.lineSeparator());
      }
      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        if (doc.isUrgent) {
          String outFile = outPath + (doc.payerBankBIC.equals("044525101")? "pco" : "pci") + String.format("%07d", doc.getId()) + ".xml";
          OutputStream oss = new FileOutputStream(outFile);
          BufferedWriter singleWriter = new BufferedWriter(new OutputStreamWriter(oss, codePage));
          singleWriter.write(getProlog() + System.lineSeparator());
          String str = UFEBSParser.toString(doc);
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
      logger.error("0521: Error write output file with confirmation.");
    }
    logger.info("0530: UFEBS confirmations created.");
  }

  /** Function generates ED211 statement by documents array
   *
   * @param outPath - path to output XML files
   * @param docs - documents array
   * @param revs - reversed documents array (or null)
   */
  void createStatement(String outPath, FDocumentArray docs, FDocumentArray revs) {
    logger.info("0530: UFEBS statement created.");
  }

}
