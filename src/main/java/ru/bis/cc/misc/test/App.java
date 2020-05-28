package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/**
 * UFEBS files parsing, converting to FT14 packet, comparing results of UFEBS parsing from 2 directories
 *
 */
public class App {

  private static Logger logger = null; // We will define configuration later

  private static HashMap<Long, FDocument> sampleDocs = new HashMap<>(); // Checked documents array
  private static HashMap<Long, FDocument> patternDocs = new HashMap<>(); // Pattern documents array (for compare)

  public static void main(String[] args) {

    String configPath = ".\\target\\";

    String samplePath = ".\\target\\in\\";
    String patternPath = ".\\target\\pattern\\";
    String outFT14Path = ".\\target\\out-ft14\\";
    String outMT103Path = ".\\target\\out-mt103\\";
    String xsdPath = ".\\target\\XMLSchemas\\";

    System.out.println("UFEBS CC test helper (c) BIS 2020.");

    // Looking for log4j
    String log4JPropertyFile = configPath + "log4j2.xml"; // Is Log4j configuration file in custom place?
    if (Files.isRegularFile(Paths.get(log4JPropertyFile))) {
      System.setProperty("log4j.configurationFile", log4JPropertyFile);
    }
    logger = LogManager.getLogger(App.class);

    FileInputStream fis;
    Properties property = new Properties();
    String configFile = configPath + "config.properties";
    try {
      fis = new FileInputStream(configFile);
      property.load(fis);
      samplePath = property.getProperty("inPath");
      patternPath = property.getProperty("patternPath");
      outFT14Path = property.getProperty("outFT14Path");
      outMT103Path = property.getProperty("outMT103Path");
      xsdPath = property.getProperty("xsdPath");
    } catch (IOException e) {
      logger.error("THE0007: Error opening properties file: " + configFile);
    }

    processUFEBS(patternPath, patternDocs, xsdPath);
    processUFEBS(samplePath, sampleDocs, xsdPath);
    createFT14(outFT14Path, patternDocs);
    createMT103(outMT103Path, patternDocs);
    compare2UFEBS(patternDocs, sampleDocs);

    logger.info("THI0001: End of work.");

  }

  /**
   * Process one UFEBS input directory and loads all EDs into specified documents array
   * Checks XML file against XSD before loading
   *
   * @param inPath   = input path
   * @param fDocs    - reference to documents array
   * @param path2XSD - path to XSD (uses when checks one file against XSD)
   */
  private static void processUFEBS(String inPath, HashMap<Long, FDocument> fDocs, String path2XSD) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        filesCount++;
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          logger.info("THI0001: Processing file: " + inPath + fileName);
          if (Helper.isXMLFile(inPath + fileName, logger)) {
            if (!processOneFile(inPath + fileName, fDocs, path2XSD)) filesError++;
          } else {
            logger.error("THE0002: File " + fileName + " is not contains XML prolog.");
            filesError++;
          }
        }
      }
    } catch (IOException e) {
      logger.error("THE0001: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("THI0002: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("THI0003: Documents added: " + fDocs.size());
  }

  /**
   * Process one UFEBS file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param fDocs    - documents array reference
   * @param path2XSD - path to XSD (uses when checks one file against XSD)
   * @return boolean: success or fail (true/false)
   */
  private static boolean processOneFile(String fileName, HashMap<Long, FDocument> fDocs, String path2XSD) {
    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = root.getNodeName();
      if (rootNodeName.equals("PacketEPD")) { // For packets EPD
        if (Helper.isXMLValid(fileName, path2XSD + "ed\\cbr_packetepd_v2020.2.0.xsd", logger)) {
          NodeList eds = root.getChildNodes();
          for (int i = 0; i < eds.getLength(); i++) {
            // Each node: ED, empty text etc
            Node ed = eds.item(i);
            if (ed.getNodeType() != Node.TEXT_NODE) {
              String nodeName = ed.getNodeName();
              if (nodeName.matches("ED10[134]")) {
                FDocument fDoc = new FDocument();
                fDoc.fromED(ed);
                logger.trace("THI0101: Packet item: " + fDoc.toString());
                Long id = fDoc.getId();
                if (!fDocs.containsKey(id)) {
                  fDocs.put(id, fDoc);
                } else {
                  logger.error("THE1008: Document ID " + id + "has already added.");
                }
              } else {
                logger.error("THE1001: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              }
            }
          }
        }
      } else if (rootNodeName.matches("ED10[134]")) { // For single EPD
        if (Helper.isXMLValid(fileName, path2XSD + "ed\\" + "cbr_" + rootNodeName + "_v2020.2.0.xsd", logger)) {
          FDocument fDoc = new FDocument();
          fDoc.fromED(root);
          logger.trace("THEI0102: Single ED: " + fDoc.toString());
          fDocs.put(fDoc.getId(), fDoc);
        }
      } else {
        logger.error("THE1002: File " + fileName + " contains unknown root element: " + rootNodeName);
      }

    } catch (ParserConfigurationException | SAXException e) {
      logger.error("THE1003: Error parsing file " + fileName, e);
      return false;
    } catch (IOException e) {
      logger.error("THE1004. Error while file access: " + fileName, e);
      return false;
    }
    return true;
  }

  /**
   * Compares 2 documents arrays
   *
   * @param pattern - pattern
   * @param sample  - checked sample
   * @return boolean: is 2 arrays equal (true/false)
   */
  private static boolean compare2UFEBS(HashMap<Long, FDocument> pattern, HashMap<Long, FDocument> sample) {

    boolean result = true;
    int iMismatch = 0;
    int iMissingInSample = 0;
    int iMissinginPattern = 0;

    for (Map.Entry<Long, FDocument> item : pattern.entrySet()) {
      Long patternKey = item.getKey();
      FDocument patternDoc = item.getValue();
      if (sample.containsKey(patternKey)) {
        if (!patternDoc.equals(sample.get(patternKey))) {
          logger.error("THE0301: Mismatch pattern and sample documents with ID: " + patternKey);
          logger.error("THI0302: " + patternDoc.mismatchDescribe(sample.get(patternKey)));
          iMismatch++;
          result = false;
        }
      } else {
        logger.error("THE0303: Pattern document with ID: " + patternKey + " is not found in sample.");
        iMissingInSample++;
        result = false;
      }
    }
    for (Map.Entry<Long, FDocument> item : sample.entrySet()) {
      Long sampleKey = item.getKey();
      FDocument sampleDoc = item.getValue();
      if (!pattern.containsKey(sampleKey)) {
        logger.error("THE0304: Sample document with ID: " + sampleKey + " is not found in pattern.");
        iMissinginPattern++;
        result = false;
      }
    }

    logger.info("THI0301: Compare complete. Mismatches: " + iMismatch + ", not in sample: " + iMissingInSample + ", not in pattern: " + iMissinginPattern);

    return result;
  }

  /**
   * Creates FT14 file for specified documents array
   *
   * @param outPath = path for create FT14 file
   * @param fDocs   - documents array reference
   */
  private static void createFT14(String outPath, HashMap<Long, FDocument> fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("THE0401: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "ft14test.txt";
    try {
      Charset chs = Charset.forName("ISO-8859-5");
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, chs));
      if (fDocs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
          Long key = item.getKey();
          FDocument value = item.getValue();
          String str = value.toFT14String();
          if (str != null) {
            writer.write(str);
            writer.write(System.lineSeparator());
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("THE0402: Error write output file " + outFile);
    }
  }

  /**
   * Creates MT103 file for specified documents array
   *
   * @param outPath = path for create MT103 file
   * @param fDocs   - documents array reference
   */
  private static void createMT103(String outPath, HashMap<Long, FDocument> fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("THE0501: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "mt103test.txt";
    try {
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
      if (fDocs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
          Long key = item.getKey();
          FDocument value = item.getValue();
          String str = value.toMT103String();
          if (str != null) {
            writer.write("{1:");
            writer.write("F01");
            writer.write("DEUTRUMMXXXX");
            writer.write(String.format("%10s", value.getId()).replace(' ','0'));
            writer.write("}");
            writer.write("{2:O103");
            writer.write("1007");
            writer.write(Helper.getSWIFTDate(value.getDate()));
            writer.write("DBEBRUMMAXXX");
            writer.write(String.format("%10s", value.getId()).replace(' ','0'));
            writer.write("1107");
            writer.write(Helper.getSWIFTDate(value.getDate()));
            writer.write("N}");
            writer.write(str);
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("THE0502: Error write output file " + outFile);
    }
  }

}