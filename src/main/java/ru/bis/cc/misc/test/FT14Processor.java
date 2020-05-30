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
class FT14Processor {
  private Logger logger;

  FT14Processor(Logger logger) {
    this.logger = logger;
  }

  /**
   * Creates FT14 file for all documents from specified array
   *
   * @param outPath = path for create FT14 file
   * @param fDocs   - documents array reference
   */
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("THE0301: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "ft14test.txt";
    try {
      Charset chs = Charset.forName("ISO-8859-5");
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, chs));
      if (fDocs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
          Long key = item.getKey();
          FDocument value = item.getValue();
          String str = value.putFT14();
          if (str != null) {
            writer.write(str);
            writer.write(System.lineSeparator());
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("THE0302: Error write output file " + outFile);
    }
  }

}
