package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/** Class for SWIFT MT100 proceessing
 *
 */
class MT100Processor extends SWIFTProcessor {

  private Logger logger;

  MT100Processor(Logger logger) {
    this.logger = logger;
  }

  /**
   * Process one MT100 input directory and loads all MT100s into specified documents array
   * MT100 - binary file with one or several messages (length = 3200 bytes), ISO-8859-5 encoding
   *
   * @param inPath   = input path
   * @param fDocs    - reference to documents array
   */
  void readAll(String inPath, FDocumentArray fDocs) {

    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        filesCount++;
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          if (ProcessorFabric.fileType(inPath + fileName, logger) == FileType.MT103) {
            if (!readFile(inPath + fileName, fDocs)) filesError++;
          } else {
            logger.error("0402: File " + fileName + " is not contains XML prolog.");
            filesError++;
          }

          logger.info("0401: Processing file: " + inPath + fileName);
        }
      }
    } catch (IOException e) {
      logger.error("0401: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0403: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0404: Documents added: " + fDocs.docs.size());
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
          logger.error("0411: MT100 format error = can't find message end for message " + msgCount);
          break;
        }
        if (str.startsWith("{9:")) { // Is it the end block of binary container?
          break;
        }
        else {
          str = str.substring(0, end + 2);
          FDocument doc = parser.fromString(str);
          if (doc != null) fDocs.add(doc, logger);
          msgCount++;
        }
      }
    }
    catch (IOException e) {
      logger.error("0410: Error while file read: " + fileName);
      return false;
    }
    logger.info("0410: Messages read: " + msgCount);
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
      logger.error("0410: Error access output directory " + outPath);
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
      logger.error("0411: Error write output file " + outFile);
    }
  }

}
