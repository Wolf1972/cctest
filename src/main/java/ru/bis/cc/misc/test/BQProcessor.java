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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

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
    logger.error("0701: There is no XSD scheme for BQ format.");
  }

  @Override
  /**
   * Creates BQ files for all specified documents array
   *
   * @param outPath = path for create BQ files
   * @param docs   - documents array reference
   */
  void createAll(String outPath, FDocumentArray docs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0520: Error access output directory " + outPath);
      return;
    }
    try {
      setCodePage("windows-1251");
      String outFile = outPath + "bqtest.xml";
      OutputStream osp = new FileOutputStream(outFile);
      BufferedWriter bqWriter = new BufferedWriter(new OutputStreamWriter(osp, getCodePage()));

      StringBuilder str = new StringBuilder();
      str.append(getProlog()); str.append(System.lineSeparator());
      str.append("<docs ");
      str.append(" filial-id=\"0001\"");
      str.append(" eod=\""); str.append(docs.getDate()); str.append("\"");
      str.append(" ver-format=\"1.0.0\"");
      str.append(" xmlns=\"http://www.bis.ru/XCNG/BQ\">");
      bqWriter.write(str.toString() + System.lineSeparator());
      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        String one = BQParser.toString(doc) + System.lineSeparator();
        bqWriter.write(one);
      }
      bqWriter.write("</docs>" + System.lineSeparator());
      bqWriter.close();
    }
    catch (IOException e) {
      logger.error("0521: Error write output file with BQ.");
    }
    logger.info("0522: Output BQ files created.");
  }
}
