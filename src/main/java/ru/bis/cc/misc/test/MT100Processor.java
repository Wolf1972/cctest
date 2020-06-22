package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/** Class for SWIFT MT100 proceessing
 *
 */
class MT100Processor extends SWIFTProcessor {

  MT100Processor() {
    logger = LogManager.getLogger(MT100Processor.class);
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
    byte[] message = new byte[3200]; // Each message in MT100 binary container aligns for 3200 bytes length

    MT100Parser parser = new MT100Parser();

    try {
      RandomAccessFile raf = new RandomAccessFile(fileName, "r");
      int count;
      while ((count = raf.read(message)) == message.length) {
        String str = new String(message, "ISO-8859-5");
        str = str.substring(2); // Skip first 2 bytes
        int end = str.indexOf("-}");
        if (end < 0) {
          logger.error("0307: MT100 format error = can't find message end for message " + msgCount);
          break;
        }
        if (str.startsWith("{9:")) { // Is it the end block of binary container?
          break;
        }
        else {
          str = str.substring(0, end + 2);
          FDocument doc = parser.fromString(str);
          if (doc != null) fDocs.add(doc);
          msgCount++;
        }
      }
    }
    catch (IOException e) {
      logger.error("0308: Error while file read: " + fileName);
      return false;
    }
    logger.info("0309: Messages read: " + msgCount);
    return true;
  }

  /**
   * Creates MT100 file for all specified documents array
   *
   * @param outPath = path for create MT103 file
   * @param fDocs   - documents array reference
   */
  void createAll(String outPath, FDocumentArray fDocs) {
    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0310: Error access output directory " + outPath);
      return;
    }
    MT100Parser parser = new MT100Parser();

    String outFile = outPath + "mt100test.txt";
    try {
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
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
      logger.error("0311: Error write output file " + outFile);
    }
  }

}
