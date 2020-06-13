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
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      StringBuilder message = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        int end = line.indexOf("-}");
        if (end < 0) {
          message.append(line);
          message.append(System.lineSeparator());
        }
        else {
          message.append(line, 0, end + 2);
          FDocument doc = MT103Parser.fromString(message.toString());
          if (doc != null) fDocs.add(doc, logger);
          msgCount++;
          message.setLength(0);
          message.append(line.substring(end + 2));
          message.append(System.lineSeparator());
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
    String outFile = outPath + "mt103test.txt";
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
          writer.write(MT100Parser.toString(value));
        }
      }
      writer.close();
    }
    catch (IOException e) {
      logger.error("0411: Error write output file " + outFile);
    }
  }

}
