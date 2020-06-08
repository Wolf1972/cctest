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

  /** Checks XML file against XML scheme
   *
   * @param fileName - check file, full path
   * @param xsdPath - path to XSD files, file determines by XML root element
   * @return boolean: is XML file accords XSD - true/false
   */
  private boolean isXMLValid(String fileName, String xsdPath, Logger logger) {

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

  void checkAll(String inqPath, String xsdPath, Logger logger) {
    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inqPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          isXMLValid(path.toString(), xsdPath, logger);
        }
      }
    } catch (IOException e) {
      logger.error("0106: Error while file system access: " + inqPath);
    }
  }

  @Override
  void readAll(String inPath, HashMap<Long, FDocument> fDocs) {

  }

  @Override
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {

  }
}
