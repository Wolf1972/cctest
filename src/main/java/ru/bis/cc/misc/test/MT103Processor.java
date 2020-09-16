package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/** Class for SWIFT MT103 proceessing
 *
 */
class MT103Processor extends SWIFTProcessor {

  MT103Parser parser = new MT103Parser();

  MT103Processor() {
    logger = LogManager.getLogger(MT103Processor.class);
  }

  @Override
  void readAll(String inPath, FDocumentArray fDocs) {
    logger.info("0400: MT103 files reading.");
    super.readAll(inPath, fDocs);
  }

  @Override
  void checkAll(String inqPath, String xsdPath) {
    logger.info("0403: MT103 files checking.");
    logger.error("0404: There is no MT103 checking procedure.");
  }

  /**
   * Process one MT103 file, fills fDocs array
   *
   * @param fileName - file name to parse (full path)
   * @param fDocs    - documents array reference
   * @return boolean: success or fail (true/false)
   */
  boolean readFile(String fileName, FDocumentArray fDocs) {
    int msgCount = 0;

    logger.trace("0405: MT103 file read: " + fileName);

    try {
      //BufferedReader reader = new BufferedReader(new FileReader(fileName));
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName("UTF-8")));
      String line;
      StringBuilder oneMT103 = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        int end = line.indexOf("-}");
        if (end < 0) {
          oneMT103.append(line);
          oneMT103.append(System.lineSeparator());
        }
        else {
          oneMT103.append(line, 0, end + 2);
          FDocument doc = parser.fromString(oneMT103.toString());
          if (doc != null) fDocs.add(doc);
          msgCount++;
          oneMT103.setLength(0);
          oneMT103.append(line.substring(end + 2));
          oneMT103.append(System.lineSeparator());
        }
      }
    }
    catch (IOException e) {
      logger.error("0406: Error while file read: " + fileName);
      return false;
    }
    logger.info("0407: Messages read: " + msgCount);
    return true;
  }

  /**
   * Creates MT103 file for all specified documents array
   *
   * @param outPath = path for create MT103 file
   * @param fDocs   - documents array reference
   */
  void createAll(String outPath, FDocumentArray fDocs) {

    logger.info("0410: MT103 files creating.");
    String outFile = outPath + "mt103test.txt";
    logger.trace("0411: MT103 file creating: " + outFile);
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0412: Error access output directory " + outPath);
      return;
    }

    try {
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
      if (fDocs.docs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.docs.entrySet()) {
          FDocument value = item.getValue();
          writer.write("{1:");
          writer.write("F01");
          writer.write("DEUTRUMMXXXX");
          writer.write(String.format("%10s", value.getId()).replace(' ','0'));
          writer.write("}");
          writer.write("{2:O103");
          writer.write("1007");
          writer.write(Helper.getSWIFTDate(value.docDate));
          writer.write("DBEBRUMMAXXX");
          writer.write(String.format("%10s", value.getId()).replace(' ','0'));
          writer.write("1107");
          writer.write(Helper.getSWIFTDate(value.docDate));
          if (value.isUrgent) writer.write("U");
          else writer.write("N");
          writer.write("}");
          writer.write(parser.toString(value));
        }
      }
      writer.close();
    }
    catch (IOException e) {
      logger.error("0413: Error write output file " + outFile);
    }
  }

}
