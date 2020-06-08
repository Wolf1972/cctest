package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Class for FT14 proceessing
 *
 */
class FT14Processor extends AProcessor {
  private Logger logger;

  FT14Processor(Logger logger) {
    this.logger = logger;
  }

  @Override
  void readAll(String inPath, HashMap<Long, FDocument> fDocs) {
    logger.error("0310: There is no method for FT14 create.");
  }

  /**
   * Creates FT14 file for all documents from specified array
   *
   * @param outPath = path for create FT14 file
   * @param fDocs   - documents array reference
   */
  @Override
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0301: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "ft14test.txt";
    try {
      Charset chs = Charset.forName("ISO-8859-5");
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, chs));
      if (fDocs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
          FDocument value = item.getValue();
          String str = FT14Parser.toString(value);
          if (str != null) {
            writer.write(str);
            writer.write(System.lineSeparator());
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("0302: Error write output file " + outFile);
    }
  }

  @Override
  void checkAll(String inqPath, String xsdPath, Logger logger) {
    logger.error("0310: There is no method for FT14 check.");
  }

}
