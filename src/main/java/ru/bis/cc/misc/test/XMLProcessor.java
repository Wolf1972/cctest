package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;
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
import java.util.HashMap;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

class XMLProcessor extends AProcessor {

  Logger logger;
  XMLProcessor(Logger logger) {
    this.logger = logger;
  }

  /**
   * Process input directory and loads all files with XML into specified documents array
   *
   * @param inPath   = input path
   * @param fDocs    - reference to documents array
   */
  @Override
  void readAll(String inPath, HashMap<Long, FDocument> fDocs) {

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
      logger.error("0501: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0502: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0503: Documents added: " + fDocs.size());
  }

  /** Function reads one XML with one single message or several messages in packet
   *
   * @param fileName - XML file name
   * @param fDocs - documents array
   */
  @Override
  boolean readFile(String fileName, HashMap<Long, FDocument> fDocs) {
    return false;
  }

  @Override
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {
  }

  /** Checks XML file against XML scheme
   *
   * @param fileName - check file, full path
   * @param xsdPath - path to XSD files, file determines by XML root element
   * @return boolean: is XML file accords XSD - true/false
   */
  boolean isXMLValid(String fileName, String xsdPath) {

    if (!Files.isDirectory(Paths.get(xsdPath))) {
      logger.error("0101: Error XSD path " + xsdPath);
      return false;
    }
    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);
      Node root = document.getDocumentElement(); // Try to obtain root element
      String rootNodeName = root.getNodeName();
      String xsdFile;
      if (rootNodeName.equals("PacketEPD")) { // For packets EPD
        xsdFile = xsdPath + "\\ed\\cbr_packetepd_v2020.2.0.xsd";
      }
      else if (rootNodeName.startsWith("ED1")) { // For single EPD
        xsdFile = xsdPath + "\\ed\\cbr_" + rootNodeName + "_v2020.2.0.xsd";
      }
      else {
        logger.error("0102: Error access file " + fileName + " while XML scheme check.");
        return false;
      }

      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory.newSchema(new StreamSource(xsdFile));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(fileName));
      logger.trace("0110: XSD check completed for file " + fileName);
      return true;
    }
    catch (IOException e) {
      logger.error("0111: Error access file " + fileName + " while XML scheme check.", e);
      return false;
    }
    catch (ParserConfigurationException e) {
      logger.error("0112: Parser configuration exception for file " + fileName + " while XML scheme check.", e);
      return false;
    }
    catch (SAXException e) {
      logger.error("0113: XML file " + fileName + " doesn't corresponds to XML scheme.", e);
      return false;
    }
  }

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
      logger.info("0502: Files processed: " + filesCount + ", errors: " + filesError);
    } catch (IOException e) {
      logger.error("0106: Error while file system access: " + inqPath);
    }
  }

  /** Puts one document into array with key control
   *
   * @param doc - document to add
   * @param fDocs - documents array
   */
  void addOne(FDocument doc, HashMap<Long, FDocument> fDocs) {
    if (doc != null) {
      logger.trace("0513: Single ED: " + doc.toString());
      if (fDocs.containsKey(doc.getId())) {
        logger.error("0516: Document with ID " + doc.getId() + " has already added to array.");
      }
      else {
        fDocs.put(doc.getId(), doc);
      }
    }
  }

}