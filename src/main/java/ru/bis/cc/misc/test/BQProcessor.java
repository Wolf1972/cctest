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

class BQProcessor extends XMLProcessor {

  BQProcessor(Logger logger) {
    super(logger);
  }

  /**
   * Process one BQ file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param docs - documents array
   * @return boolean: success or fail for file processing (true/false)
   */
  @Override
  boolean readFile(String fileName, FDocumentArray docs) {
    try {

      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = root.getNodeName();
      if (rootNodeName.equals("docs")) {
        NodeList eds = root.getChildNodes();
        for (int i = 0; i < eds.getLength(); i++) {
          Node ed = eds.item(i);
          if (ed.getNodeType() != Node.TEXT_NODE) {
            String nodeName = ed.getNodeName();
            if (nodeName.equals("doc")) {
              FDocument doc = BQParser.fromXML(ed);
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

  @Override
  void checkAll(String inqPath, String xsdPath) {
    logger.error("0701: There is no checker for BQ format.");
  }

  @Override
  void createAll(String outPath, FDocumentArray fDocs) {
    super.createAll(outPath, fDocs);
  }
}
