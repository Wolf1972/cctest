package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;
import static ru.bis.cc.misc.test.App.accounts;
import static ru.bis.cc.misc.test.App.clients;

class BQProcessor extends XMLProcessor {

  BQParser parser = new BQParser();

  BQProcessor() {
    logger = LogManager.getLogger(BQProcessor.class);
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

      logger.trace("0100: BQ documents file reading: " + fileName);
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
              FDocument doc = parser.fromXML(ed);
              docs.add(doc);
            }
            else {
              logger.error("0101: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
              return false;
            }
          }
        }
        return true;
      }
      else {
        logger.error("0102: File " + fileName + " contains unknown root element: " + rootNodeName);
      }
      return false;
    }
    catch (ParserConfigurationException | SAXException e) {
      logger.error("0103: Error parsing file " + fileName, e);
    }
    catch (IOException e) {
      logger.error("0104. Error while file access: " + fileName);
    }
    return false;
  }

  @Override
  void checkAll(String inqPath, String xsdPath) {
    logger.info("0105: BQ files checking.");
    logger.error("0106: There is no XSD scheme for BQ format.");
  }


  /**
   * Creates BQ files for all specified documents array
   *
   * @param outPath = path for create BQ files
   * @param docs   - documents array reference
   */
  @Override
  void createAll(String outPath, FDocumentArray docs) {

    logger.info("0110: BQ files creating.");
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0111: Error access output directory " + outPath);
      return;
    }
    try {
      setCodePage("windows-1251");
      String outFile = outPath + "bqtest.xml";
      OutputStream osp = new FileOutputStream(outFile);
      BufferedWriter bqWriter = new BufferedWriter(new OutputStreamWriter(osp, getCodePage()));

      String str = getProlog() + System.lineSeparator() +
              "<docs filial-id=\"0001\" eod=\"" + docs.getDate() + "\"" +
              " ver-format=\"1.0.0\"" +
              " xmlns=\"http://www.bis.ru/XCNG/BQ\">" + System.lineSeparator();
      bqWriter.write(str);
      for (Map.Entry<Long, FDocument> item : docs.docs.entrySet()) {
        FDocument doc = item.getValue();
        String one = parser.toString(doc) + System.lineSeparator();
        bqWriter.write(one);
      }
      bqWriter.write("</docs>" + System.lineSeparator());
      bqWriter.close();
    }
    catch (IOException e) {
      logger.error("0112: Error write output file with BQ.");
    }
    logger.info("0113: Output BQ files created.");
  }

  /** Function reads all files with static data from BQ
   *
   * @param inPath - directory with static data
   */
  void readAllStatic(String inPath) {

    int filesCount = 0;
    int filesError = 0;

    logger.info("0130: BQ static files reading.");
    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          filesCount++;
          String fileName = path.getFileName().toString();
          if (!readStaticFile(inPath + fileName)) filesError++;
        }
      }
    }
    catch (IOException e) {
      logger.error("0131: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0132: XML files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0133: Clients added: " + clients.items.size());
    logger.info("0134: Accounts added: " + accounts.items.size());
  }

  private boolean readStaticFile(String fileName) {
    try {

      logger.trace("0120: Static file reading: " + fileName);
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(fileName);

      // Try to obtain root element
      Node root = document.getDocumentElement();
      String rootNodeName = root.getNodeName();
      if ("persons,cust-privs,cust-corps,banks".contains(rootNodeName)) {
        NodeList clientsNode = root.getChildNodes();
        for (int i = 0; i < clientsNode.getLength(); i++) {
          Node clientNode = clientsNode.item(i);
          if (clientNode.getNodeType() != Node.TEXT_NODE) {
            String nodeName = clientNode.getNodeName();
            if ("person,cust-priv,cust-corp,bank".contains(nodeName)) {
              Client clt = parser.clientFromXML(clientNode);
              if (clt != null) clients.items.put(clt.id, clt);
            }
            else {
              logger.error("0125: Clients static file " + fileName + ", element no " + i + " contains unknown element: " + nodeName);
              return false;
            }
          }
        }
        return true;
      }
      else if (rootNodeName.equals("accounts")) {
        NodeList accountsNode = root.getChildNodes();
        for (int i = 0; i < accountsNode.getLength(); i++) {
          Node accountNode = accountsNode.item(i);
          if (accountNode.getNodeType() != Node.TEXT_NODE) {
            String nodeName = accountNode.getNodeName();
            if (nodeName.equals("acct")) {
              Account acc = parser.accountFromXML(accountNode);
              if (acc != null) accounts.items.put(acc.account, acc);
            }
            else {
              logger.error("0126: Accounts static file " + fileName + ", element no " + i + " contains unknown element: " + nodeName);
              return false;
            }
          }
        }
        return true;

      }
      else {
        logger.error("0127: Static file " + fileName + " contains unknown root element: " + rootNodeName);
      }
      return false;
    }
    catch (ParserConfigurationException | SAXException e) {
      logger.error("0128: Error parsing static file " + fileName, e);
    }
    catch (IOException e) {
      logger.error("0129. Error while static file access: " + fileName);
    }
    return false;
  }

}
