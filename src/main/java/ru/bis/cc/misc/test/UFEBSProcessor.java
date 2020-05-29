package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

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
          logger.info("THI0501: Processing file: " + inPath + fileName);
          if (Helper.isXMLFile(inPath + fileName, logger)) {
            if (!readOne(inPath + fileName, fDocs)) filesError++;
          } else {
            logger.error("THE0502: File " + fileName + " is not contains XML prolog.");
            filesError++;
          }
        }
      }
    } catch (IOException e) {
      logger.error("THE0501: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("THI0502: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("THI0503: Documents added: " + fDocs.size());
  }

  /**
   * Process one UFEBS file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param fDocs    - documents array reference
   * @return boolean: success or fail (true/false)
   */
  private boolean readOne(String fileName, HashMap<Long, FDocument> fDocs) {
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
                fDoc.fromED(ed);
                logger.trace("THI0510: Packet item: " + fDoc.toString());
                Long id = fDoc.getId();
                if (!fDocs.containsKey(id)) {
                  fDocs.put(id, fDoc);
                } else {
                  logger.error("THE0511: Document ID " + id + "has already added.");
                }
              } else {
                logger.error("THE0512: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              }
            }
          }
        }
      } else if (rootNodeName.matches("ED10[134]")) { // For single EPD
        if (Helper.isXMLValid(fileName, XSDPath + "ed\\" + "cbr_" + rootNodeName + "_v2020.2.0.xsd", logger)) {
          FDocument fDoc = new FDocument();
          fDoc.fromED(root);
          logger.trace("THI0513: Single ED: " + fDoc.toString());
          fDocs.put(fDoc.getId(), fDoc);
        }
      } else {
        logger.error("THE0514: File " + fileName + " contains unknown root element: " + rootNodeName);
      }

    } catch (ParserConfigurationException | SAXException e) {
      logger.error("THE0515: Error parsing file " + fileName, e);
      return false;
    } catch (IOException e) {
      logger.error("THE0516. Error while file access: " + fileName, e);
      return false;
    }
    return true;
  }

}
