package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

class SWIFTProcessor extends AProcessor {

  SWIFTProcessor() {
    logger = LogManager.getLogger(SWIFTProcessor.class);
  }

  @Override
  void readAll(String inPath, FDocumentArray fDocs) {
    int filesCount = 0;
    int filesError = 0;

    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
      for (Path path : directoryStream) {
        filesCount++;
        if (isRegularFile(path)) {
          String fileName = path.getFileName().toString();
          ProcessorFabric fab = new ProcessorFabric();
          if (fab.fileType(inPath + fileName) == FileType.MT103) {
            if (!readFile(inPath + fileName, fDocs)) filesError++;
          } else {
            logger.error("0401: File " + fileName + " is not a SWIFT file.");
            filesError++;
          }

          logger.info("0402: Processing file: " + inPath + fileName);
        }
      }
    } catch (IOException e) {
      logger.error("0403: Error while file system access: " + inPath);
      filesError++;
    }
    logger.info("0404: Files processed: " + filesCount + ", errors: " + filesError);
    logger.info("0404: Documents added: " + fDocs.docs.size());
  }

  @Override
  boolean readFile(String fileName, FDocumentArray fDocs) {
    return false;
  }

  @Override
  void createAll(String outPath, FDocumentArray fDocs) {

  }

  @Override
  void checkAll(String inqPath, String xsdPath) {

  }

}
