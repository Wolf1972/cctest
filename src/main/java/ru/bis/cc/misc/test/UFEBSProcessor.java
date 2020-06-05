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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/** Class for UFEBS proceessing
 *
 */
class UFEBSProcessor {

  private Logger logger;
  private String XSDPath;

  UFEBSProcessor(String XSDPath, Logger logger) {
    this.logger = logger;
    this.XSDPath = XSDPath;
  }

  /**
   * Process UFEBS input directory and loads all files with EDs into specified documents array
   * Checks XML file against XSD before loading
   *
   * @param inPath   = input path
   * @param fDocs    - reference to documents array
   */
  void readAll(String inPath, HashMap<Long, FDocument> fDocs) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        filesCount++;
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          logger.info("0501: Processing file: " + inPath + fileName);
          if (Helper.isXMLFile(inPath + fileName, logger)) {
            if (!readOne(inPath + fileName, fDocs)) filesError++;
          }
          else {
            logger.error("0502: File " + fileName + " is not contains XML prolog.");
            filesError++;
          }
        }
      }
    }
    catch (IOException e) {
      logger.error("0501: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0502: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0503: Documents added: " + fDocs.size());
  }

  /**
   * Process one UFEBS file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param fDocs    - documents array reference
   * @return boolean: success or fail (true/false)
   */
  boolean readOne(String fileName, HashMap<Long, FDocument> fDocs) {
    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = root.getNodeName();
      if (rootNodeName.equals("PacketEPD")) { // For packets EPD
        if (Helper.isXMLValid(fileName, XSDPath + "ed\\cbr_packetepd_v2020.2.0.xsd", logger)) {
          NodeList eds = root.getChildNodes();
          for (int i = 0; i < eds.getLength(); i++) {
            // Each node: ED, empty text etc
            Node ed = eds.item(i);
            if (ed.getNodeType() != Node.TEXT_NODE) {
              String nodeName = ed.getNodeName();
              if (nodeName.matches("ED10[134]")) {
                FDocument fDoc = new FDocument();
                UFEBSParser.fromXML(ed, fDoc);
                logger.trace("0510: Packet item: " + fDoc.toString());
                Long id = fDoc.getId();
                if (!fDocs.containsKey(id)) {
                  fDocs.put(id, fDoc);
                } else {
                  logger.error("0511: Document ID " + id + "has already added.");
                }
              } else {
                logger.error("0512: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              }
            }
          }
        }
      }
      else if (rootNodeName.matches("ED10[134]")) { // For single EPD
        if (Helper.isXMLValid(fileName, XSDPath + "ed\\" + "cbr_" + rootNodeName + "_v2020.2.0.xsd", logger)) {
          FDocument fDoc = new FDocument();
          UFEBSParser.fromXML(root, fDoc);
          logger.trace("0513: Single ED: " + fDoc.toString());
          fDocs.put(fDoc.getId(), fDoc);
        }
      }
      else {
        logger.error("0514: File " + fileName + " contains unknown root element: " + rootNodeName);
      }

    } catch (ParserConfigurationException | SAXException e) {
      logger.error("0515: Error parsing file " + fileName, e);
      return false;
    } catch (IOException e) {
      logger.error("0516. Error while file access: " + fileName);
      return false;
    }
    return true;
  }

  /**
   * Creates UFEBS files for all specified documents array: urgent payments place into individual files,
   * non-urgent documents places in common packet file
   *
   * @param outPath = path for create MT103 file
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
      String outFile = "";
      if (packetCount > 0) {
        outFile = outPath + "pck000000.xml";
        OutputStream osp = new FileOutputStream(outFile);
        packetWriter = new BufferedWriter(new OutputStreamWriter(osp));
        packetWriter.write(prolog + System.lineSeparator());
        packetWriter.write(UFEBSParser.packetRoot(packetDate, packetCount, packetSum) + System.lineSeparator());
      }
      for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
        FDocument doc = item.getValue();
        if (doc.isUrgent) {
          outFile = outPath + "one" + String.format("%06d", doc.getId()) + ".xml";
          OutputStream oss = new FileOutputStream(outFile);
          BufferedWriter singleWriter = new BufferedWriter(new OutputStreamWriter(oss));
          singleWriter.write(prolog + System.lineSeparator());
          String str = UFEBSParser.toString(doc);
          singleWriter.write(str);
          singleWriter.close();
          String rootNodeName = "ED101"; // TODO
          Helper.isXMLValid(outFile, XSDPath + "ed\\" + "cbr_" + rootNodeName + "_v2020.2.0.xsd", logger);
        }
        else {
          String str = UFEBSParser.toString(doc);
          packetWriter.write(str);
        }
      }
      if (packetCount > 0) {
        packetWriter.write("</PacketEPD>" + System.lineSeparator());
        packetWriter.close();
        Helper.isXMLValid(outFile, XSDPath + "ed\\cbr_packetepd_v2020.2.0.xsd", logger);
      }

    }
    catch (IOException e) {
      logger.error("0521: Error write output file with ED.");
    }

  }
}
