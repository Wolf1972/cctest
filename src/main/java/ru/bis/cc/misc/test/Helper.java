package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Helper {
  /** Checks if file is XML
   *
   * @param fileName - check file, full path
   * @return boolean: is file XML - true/false
   */
  static boolean isXMLFile(String fileName, Logger logger) {
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
      logger.error("THE0201: Error access file: " + fileName, e);
    }
    return false;
  }

  /** Checks XML file against XML scheme
   *
   * @param fileName - check file, full path
   * @param xsdFile - XSD file for root element
   * @return boolean: is XML file accords XSD - true/false
   */
  static boolean isXMLValid(String fileName, String xsdFile, Logger logger) {
    if (!Files.isRegularFile(Paths.get(xsdFile))) {
      logger.error("THE0202: Error access XSD file " + xsdFile);
      return false;
    }
    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = factory.newSchema(new StreamSource(xsdFile));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(fileName));
      logger.trace("THI0201: XSD check completed for file " + fileName);
      return true;
    }
    catch (IOException e) {
      logger.error("THE0203: Error access file " + fileName + " while XML scheme check.", e);
      return false;
    }
    catch (SAXException e) {
      logger.error("THE0204: XML file " + fileName + " doesn't accord with XML scheme.", e);
      return false;
    }
  }

  /** Compares two strings, each may be null
   *
   * @param s1 - 1st string
   * @param s2 - 2nd string
   * @return boolean: strings are equal - true/false
   */
  static boolean cmpNullString(String s1, String s2) {
    if ((s1 != null && s2 == null) || (s1 == null && s2 != null)) return false;
    if (s1 != null) return s1.equals(s2); // don't need to check s2 with null
    return true;
  }
}
