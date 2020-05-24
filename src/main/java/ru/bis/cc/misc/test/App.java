package ru.bis.cc.misc.test;

import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/**
 * UFEBS files parsing, converting to FT14 packet
 *
 */
public class App {

  private static final Logger logger = LogManager.getLogger(App.class);

  public static HashMap<Long, FDocument> fDocs = new HashMap<>(); // Documents array

  public static void main( String[] args ) {

    HashSet<String> files = new HashSet<>(); // Input files list (only names)

    String inPath = ".\\";

    System.out.println("UFEBS CC test helper (c) BIS 2020.");

    Properties p = new Properties();
    String log4JPropertyFile = inPath + "log4j2.xml";
    try {
      p.load(new FileInputStream(log4JPropertyFile));
      PropertyConfigurator.configure(p);
      logger.info("THI0001: Logger configuration " + log4JPropertyFile + " used.");
    } catch (IOException e) {
      logger.error("THE0003: error access logger configuration file " + log4JPropertyFile + ", default configuration will use.");
    }


    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          files.add(fileName);
          logger.info("THI0001: Processing file: " + inPath + fileName);
          if (isXMLFile(fileName)) {
            processOneFile(fileName);
          }
          else {
            logger.error("THE0002: File " + fileName + " is not contains XML prolog.");
          }
        }
      }
    }
    catch (IOException e) {
      logger.error("THE0001: Error while file system access: " + inPath);
    }

    logger.info("THI0001: End of work.");

  }

  public static void processOneFile(String fileName) {

    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = root.getNodeName();
      if (rootNodeName.equals("PacketEPD")) {
        // root: Packet ED
        NodeList eds = root.getChildNodes();
        for (int i = 0; i < eds.getLength(); i++) {
          // Each node: ED, empty text etc
          Node ed = eds.item(i);
          if (ed.getNodeType() != Node.TEXT_NODE) {
            String nodeName = ed.getNodeName();
            if (nodeName.matches("ED10[134]")) {
              FDocument fDoc = new FDocument();
              fDoc.getFromED(ed);
              logger.info("THI0101: Packet item: " + fDoc.toString());
              fDocs.put(Long.parseLong(fDoc.docNum), fDoc);
            }
            else {
              logger.error("THE1001: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
            }
          }
        }
      }
      else if (rootNodeName.matches("ED10[134]")) {
        FDocument fDoc = new FDocument();
        fDoc.getFromED(root);

        logger.info("THI0102: Single ED: " + fDoc.toString());

        fDocs.put(Long.parseLong(fDoc.docNum), fDoc);
      }
      else {
        logger.error("TH1002: File " + fileName + " contains unknown root element: " + rootNodeName);
      }

    }
    catch (ParserConfigurationException | SAXException e) {
      logger.error("TH1003: Error parsing file " + fileName, e);
    }
    catch (IOException e) {
      logger.error("TH1004. Error while file access: " + fileName, e);
    }
  }

  public static boolean isXMLFile(String fileName) {
    try {
      RandomAccessFile raf = new RandomAccessFile(fileName, "r");
      String firstStr = raf.readLine();
      if (firstStr != null) {
        if (firstStr.matches("^<\\?xml?.+"))
          return true;
      }
      raf.close();
    }
    catch (IOException e) {
      logger.error("TH0201: Error access file: " + fileName, e);
    }
    return false;
  }
}
