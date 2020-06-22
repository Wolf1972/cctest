package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/** Class for FT14 proceessing
 *
 */
class FT14Processor extends AProcessor {
  private Logger logger;

  FT14Processor() {
    logger = LogManager.getLogger(FT14Processor.class);
  }

  @Override
  void readAll(String inPath, FDocumentArray fDocs) {
    logger.error("0201: There is no method for FT14 create.");
  }

  @Override
  boolean readFile(String fileName, FDocumentArray fDocs) {
    return false;
  }

  /**
   * Creates FT14 file for all documents from specified array
   *
   * @param outPath = path for create FT14 file
   * @param fDocs   - documents array reference
   */
  @Override
  void createAll(String outPath, FDocumentArray fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0202: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "ft14test.txt";
    try {
      Charset chs = Charset.forName("ISO-8859-5");
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, chs));
      if (fDocs.docs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.docs.entrySet()) {
          FDocument value = item.getValue();
          writer.write(FT14Parser.toString(value));
          writer.write(System.lineSeparator());
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("0203: Error write output file " + outFile);
    }
  }

  @Override
  void checkAll(String inqPath, String xsdPath) {
    logger.error("0204: There is no method for FT14 check.");
  }

}
