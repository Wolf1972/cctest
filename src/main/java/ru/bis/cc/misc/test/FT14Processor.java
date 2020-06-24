package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/** Class for FT14 proceessing
 *
 */
class FT14Processor extends AProcessor {

  FT14Parser parser = new FT14Parser();

  FT14Processor() {
    logger = LogManager.getLogger(FT14Processor.class);
  }

  @Override
  void readAll(String inPath, FDocumentArray fDocs) {
    logger.info("0200: FT14 files reading.");
    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        filesCount++;
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          ProcessorFabric fab = new ProcessorFabric();
          if (fab.fileType(inPath + fileName) == FileType.FT14) {
            if (!readFile(inPath + fileName, fDocs)) filesError++;
          }
          else {
            logger.error("0201: File " + fileName + " is not a FT14 file.");
            filesError++;
          }
        }
      }
    }
    catch (IOException e) {
      logger.error("0203: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0204: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0204: Documents added: " + fDocs.docs.size());
  }

  @Override
  boolean readFile(String fileName, FDocumentArray fDocs) {
    int msgCount = 0;
    try {
      FileInputStream input = new FileInputStream(fileName);
      InputStreamReader reader = new InputStreamReader(input, "ISO-8859-5");
      BufferedReader handler = new BufferedReader(reader);
      String line;
      while ((line = handler.readLine()) != null) {
        FDocument doc = parser.fromString(line);
        if (doc != null) fDocs.add(doc);
        msgCount++;
      }
      input.close();
    }
    catch (IOException e) {
      logger.error("0205: Error while file read: " + fileName);
      return false;
    }
    logger.info("0206: Strings read: " + msgCount);
    return true;
  }

  /**
   * Creates FT14 file for all documents from specified array
   *
   * @param outPath = path for create FT14 file
   * @param fDocs   - documents array reference
   */
  @Override
  void createAll(String outPath, FDocumentArray fDocs) {

    logger.info("0210: FT14 files creating.");

    if (!Files.isDirectory(Paths.get(outPath))) {
      logger.error("0211: Error access output directory " + outPath);
      return;
    }
    String outFile = outPath + "ft14test.txt";
    int count = 0;
    try {
      Charset chs = Charset.forName("ISO-8859-5");
      OutputStream os = new FileOutputStream(outFile);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, chs));
      if (fDocs.docs.size() > 0) {
        for (Map.Entry<Long, FDocument> item : fDocs.docs.entrySet()) {
          FDocument value = item.getValue();
          writer.write(parser.toString(value));
          writer.write(System.lineSeparator());
          count++;
        }
      }
      writer.close();
    } catch (IOException e) {
      logger.error("0213: Error write output file " + outFile);
    }
    logger.info("0214: FT14 strings wrote: " + count);
    logger.info("0215: FT14 file created: " + outFile);
  }

  @Override
  void checkAll(String inqPath, String xsdPath) {
    logger.info("0220: FT14 files checking.");
    logger.error("0221: There is no method for FT14 check.");
  }

}
