package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class MT940Processor extends SWIFTProcessor {

  MT940Parser parser = new MT940Parser();

  MT940Processor() {
    logger = LogManager.getLogger(MT940Processor.class);
  }

  @Override
  void readAll(String inPath, FDocumentArray fDocs) {
    logger.info("1001: MT940 files reading.");
    super.readAll(inPath, fDocs);
  }

  @Override
  void createAll(String outPath, FDocumentArray fDocs) {
    logger.info("1005: MT940 files creating.");
    logger.error("1006: There is no MT940 file creating procedure.");
  }

  @Override
  void checkAll(String inqPath, String xsdPath) {
    logger.info("1010: MT940 files checking.");
    logger.error("1011: There is no MT940 file checking procedure.");
  }

  /**
   * Process one MT940 file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param fDocs    - documents array reference
   * @return boolean: success or fail (true/false)
   */
  boolean readFile(String fileName, FDocumentArray fDocs) {
    int msgCount = 0;
    logger.trace("1015: MT940 file read: " + fileName);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      StringBuilder oneOperation = new StringBuilder();
      boolean in86tag = false;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(":61:") || line.startsWith(":62F:")) {
          // parse previous operation
          FDocument doc = parser.fromString(oneOperation.toString());
          if (doc != null) fDocs.add(doc);
          msgCount++;
          oneOperation.delete(0, oneOperation.length());
          if (line.startsWith(":62F:")) break;
        }
        if (line.startsWith(":61:")) {
          oneOperation.append(line);
          in86tag = false;
        }
        else if (line.startsWith(":86:")) {
          oneOperation.append(line);
          in86tag = true;
        }
        else if (in86tag) {
          oneOperation.append(line);
        }
      }
    }
    catch (IOException e) {
      logger.error("1016: Error while file read: " + fileName);
      return false;
    }
    logger.info("1017: Operations read: " + msgCount);
    return true;
  }

}
