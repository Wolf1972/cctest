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
import java.util.HashMap;
import java.util.Map;

/** Class for UFEBS proceessing
 *
 */
class UFEBSProcessor extends XMLProcessor {

  UFEBSProcessor(Logger logger) {
    super(logger);
  }

  /**
   * Process one UFEBS file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @return boolean: success or fail for file processing (true/false)
   */
  boolean readFile(String fileName, HashMap<Long, FDocument> fDocs) {
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
              addOne(doc, fDocs);
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
        addOne(doc, fDocs);
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

  /**
   * Creates UFEBS files for all specified documents array: urgent payments place into individual files,
   * non-urgent documents places in common packet file
   *
   * @param outPath = path for create UFEBS files
   * @param fDocs   - documents array reference
   */
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0520: Error access output directory " + outPath);
      return;
    }
    String prolog = "<?xml version=\"1.0\" encoding=\"utf8\" ?>";
    // Has documents array contains non-urgent payments? Calculate count and total sum of all documents in packet
    int packetCount = 0;
    Long packetSum = 0L;
    String packetDate = "";
    for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
      FDocument doc = item.getValue();
      if (!doc.isUrgent) {
        packetCount++;
        packetSum += doc.amount;
        packetDate = doc.docDate;
      }
    }
    try {
      BufferedWriter packetWriter = null;
      if (packetCount > 0) {
        String outFile = outPath + "pck000000.xml";
        OutputStream osp = new FileOutputStream(outFile);
        packetWriter = new BufferedWriter(new OutputStreamWriter(osp));
        packetWriter.write(prolog + System.lineSeparator());
        packetWriter.write(UFEBSParser.packetRoot(packetDate, packetCount, packetSum) + System.lineSeparator());
      }
      for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
        FDocument doc = item.getValue();
        if (doc.isUrgent) {
          String outFile = outPath + "one" + String.format("%06d", doc.getId()) + ".xml";
          OutputStream oss = new FileOutputStream(outFile);
          BufferedWriter singleWriter = new BufferedWriter(new OutputStreamWriter(oss));
          singleWriter.write(prolog + System.lineSeparator());
          String str = UFEBSParser.toString(doc);
          singleWriter.write(str);
          singleWriter.close();
        }
        else {
          String str = UFEBSParser.toString(doc);
          if (packetWriter != null) packetWriter.write(str);
        }
      }
      if (packetCount > 0) {
        packetWriter.write("</PacketEPD>" + System.lineSeparator());
        packetWriter.close();
      }
    }
    catch (IOException e) {
      logger.error("0521: Error write output file with ED.");
    }
  }
}
