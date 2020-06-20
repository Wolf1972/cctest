package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MT940Processor extends SWIFTProcessor {

  MT940Processor(Logger logger) {
    super(logger);
  }

  /**
   * Process one MT100 file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param fDocs    - documents array reference
   * @return boolean: success or fail (true/false)
   */
  boolean readFile(String fileName, FDocumentArray fDocs) {
    int msgCount = 0;

    MT940Parser parser = new MT940Parser();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      StringBuilder oneOperation = new StringBuilder();
      boolean in86tag = false;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(":61:") || line.startsWith(":62F:")) {
          // parse previous operation
          FDocument doc = parser.fromString(oneOperation.toString());
          if (doc != null) fDocs.add(doc, logger);
          msgCount++;
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
      logger.error("1005: Error while file read: " + fileName);
      return false;
    }
    logger.info("1006: Opearions read: " + msgCount);
    return true;
  }

}
