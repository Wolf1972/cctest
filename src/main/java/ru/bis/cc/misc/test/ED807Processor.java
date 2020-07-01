package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static ru.bis.cc.misc.test.App.banks;

public class ED807Processor extends XMLProcessor {
  ED807Parser parser = new ED807Parser();

  ED807Processor() {
    logger = LogManager.getLogger(ED807Processor.class);
  }

  @Override
  void readAll(String inPath) {
    logger.info("1100: Banks information reading.");
    super.readAll(inPath);
  }

  /**
   * Process one ED807 file, fills banks array
   *
   * @param fileName - file name to parse (full path)
   * @return boolean: success or fail for file processing (true/false)
   */
  @Override
  boolean readFile(String fileName) {
    try {

      logger.trace("1101: ED807 banks directory file reading: " + fileName);
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = Helper.getSimpleNodeName(root);
      if (rootNodeName.equals("ED807")) {
        NodeList eds = root.getChildNodes();
        for (int i = 0; i < eds.getLength(); i++) {
          Node nodeBank = eds.item(i);
          if (nodeBank.getNodeType() != Node.TEXT_NODE) {
            String nodeName = Helper.getSimpleNodeName(nodeBank);
            if (nodeName.equals("BICDirectoryEntry")) {
              Bank bank = parser.bankFromXML(nodeBank);
              if (bank != null) banks.items.put(bank.bic, bank);
            }
            else {
              logger.error("1102: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              return false;
            }
          }
        }
        logger.info("1103: Banks loaded: " + banks.items.size());
        return true;
      }
      else {
        logger.error("1104: File " + fileName + " contains unknown root element: " + rootNodeName);
      }
      return false;
    }
    catch (ParserConfigurationException | SAXException e) {
      logger.error("1105: Error parsing file " + fileName, e);
    }
    catch (IOException e) {
      logger.error("1106. Error while file access: " + fileName);
    }
    return false;
  }
}
