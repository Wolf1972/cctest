package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

class XMLProcessor extends AProcessor {

  private String codePage = "utf-8";

  XMLProcessor() {
    logger = LogManager.getLogger(XMLProcessor.class);
  }

  /**
   * Process input directory and loads all files with XML into specified documents array
   *
   * @param inPath   = input path
   * @param fDocs    - reference to documents array
   */
  @Override
  void readAll(String inPath, FDocumentArray fDocs) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          filesCount++;
          String fileName = path.getFileName().toString();
          if (!readFile(inPath + fileName, fDocs)) filesError++;
        }
      }
    }
    catch (IOException e) {
      logger.error("0901: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0902: XML files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0903: Documents added: " + fDocs.docs.size());
  }

  /**
   * Process input directory and loads all files with XML
   *
   * @param inPath   = input path
   */
  void readAll(String inPath) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          filesCount++;
          String fileName = path.getFileName().toString();
          if (!readFile(inPath + fileName)) filesError++;
        }
      }
    }
    catch (IOException e) {
      logger.error("0901: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0902: XML files processed: " + filesCount + ", errors: " + filesError);
  }

  /** Function reads one XML with one single document or several documents in packet
   *
   * @param fileName - XML file name
   * @param fDocs - documents array
   */
  @Override
  boolean readFile(String fileName, FDocumentArray fDocs) {
    return false;
  }

  /** Function reads one XML file
   *
   * @param fileName - XML file name
   */
  boolean readFile(String fileName) {
    return false;
  }

  /** Function sets specified code page for XML output
   *
   * @param codePage - code page name ("windows-1251" or "utf-8" allowed)
   */
  void setCodePage(String codePage) {
    this.codePage = codePage;
  }

  /** Function returns name of code page was set
   *
   * @return - string with "utf-8" or "windows-1251"
   */
  String getCodePage() {
    return codePage;
  }

  /** Function returns string with XML prolog
   *
   * @return - string with prolog with code page
   */
  String getProlog() {
    return "<?xml version=\"1.0\" encoding=\"" + codePage + "\" ?>";
  }

  @Override
  void createAll(String outPath, FDocumentArray fDocs) {
  }

  /** Checks XML file against XML scheme
   *
   * @param fileName - check file, full path
   * @param xsdPath - path to XSD files, file determines by XML root element
   * @return boolean: is XML file accords XSD - true/false
   */
  boolean isXMLValid(String fileName, String xsdPath) {

    if (!Files.isDirectory(Paths.get(xsdPath))) {
      logger.error("0904: Error XSD path " + xsdPath);
      return false;
    }
    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);
      Node root = document.getDocumentElement(); // Try to obtain root element
      String rootNodeName = Helper.getSimpleNodeName(root);
      String xsdFile;
      if (rootNodeName.equals("PacketEPD")) { // For packets EPD
        xsdFile = xsdPath + "\\ed\\cbr_packetepd_v2020.2.0.xsd";
      }
      else if (rootNodeName.equals("PacketESID")) { // For packets ESIS
        xsdFile = xsdPath + "\\ed\\cbr_packetesid_v2020.2.0.xsd";
      }
      else if (rootNodeName.startsWith("ED")) { // For single EPD
        xsdFile = xsdPath + "\\ed\\cbr_" + rootNodeName + "_v2020.2.0.xsd";
      }
      else {
        logger.error("0102: XSD scheme for " + rootNodeName + " not found.");
        return false;
      }

      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory.newSchema(new StreamSource(xsdFile));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(fileName));
      logger.trace("0905: XSD check completed for file " + fileName);
      return true;
    }
    catch (IOException e) {
      logger.error("0906: Error access file " + fileName + " while XML scheme check.", e);
      return false;
    }
    catch (ParserConfigurationException e) {
      logger.error("0907: Parser configuration exception for file " + fileName + " while XML scheme check.", e);
      return false;
    }
    catch (SAXException e) {
      logger.error("0908: XML file " + fileName + " doesn't corresponds to XML scheme.", e);
      return false;
    }
  }

  /** Function checks all XML files in specified directory against XML scheme
   *  XML scheme selects for UFEBS root elements only
   * @param inqPath - directory with XML
   * @param xsdPath - directory with XSD
   */
  void checkAll(String inqPath, String xsdPath) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inqPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          filesCount++;
          if (!isXMLValid(path.toString(), xsdPath)) filesError++;
        }
      }
      logger.info("0909: Check files for XSD: " + filesCount + ", errors: " + filesError);
    }
    catch (IOException e) {
      logger.error("0910: Error while file system access: " + inqPath);
    }
  }

}
