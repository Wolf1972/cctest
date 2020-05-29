package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/** Class for SWIFT MT103 proceessing
 *
 */
class MT103Processor {

  private Logger logger;

  MT103Processor(Logger logger) {
    this.logger = logger;
  }

  /**
   * Process one MT103 input directory and loads all MT103s into specified documents array
   *
   * @param inPath   = input path
   * @param fDocs    - reference to documents array
   */
  void readAll(String inPath, HashMap<Long, FDocument> fDocs) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        filesCount++;
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          logger.info("THI0401: Processing file: " + inPath + fileName);
        }
      }
    } catch (IOException e) {
      logger.error("THE0401: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("THI0402: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("THI0403: Documents added: " + fDocs.size());
  }

  /**
   * Creates MT103 file for all specified documents array
   *
   * @param outPath = path for create MT103 file
   * @param fDocs   - documents array reference
   */
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("THE0410: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "mt103test.txt";
    try {
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
      if (fDocs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
          Long key = item.getKey();
          FDocument value = item.getValue();
          String str = value.toMT103String();
          if (str != null) {
            writer.write("{1:");
            writer.write("F01");
            writer.write("DEUTRUMMXXXX");
            writer.write(String.format("%10s", value.getId()).replace(' ','0'));
            writer.write("}");
            writer.write("{2:O103");
            writer.write("1007");
            writer.write(Helper.getSWIFTDate(value.getDate()));
            writer.write("DBEBRUMMAXXX");
            writer.write(String.format("%10s", value.getId()).replace(' ','0'));
            writer.write("1107");
            writer.write(Helper.getSWIFTDate(value.getDate()));
            writer.write("N}");
            writer.write(str);
          }
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("THE0411: Error write output file " + outFile);
    }
  }

}
