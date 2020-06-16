package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

enum FileType {
  UNKNOWN, UFEBS, MT103, FT14, BQ, MT100
}

class ProcessorFabric {

  /** Function returns specified processor
   *
   * @param fileType - file type for processor
   * @param logger - application logger
   * @return processor for
   */
  static AProcessor getProcessor(FileType fileType, Logger logger) {
    if (fileType == FileType.UFEBS)
      return new UFEBSProcessor(logger);
    else if (fileType == FileType.MT103)
      return new MT103Processor(logger);
    else if (fileType == FileType.MT100)
      return new MT100Processor(logger);
    else if (fileType == FileType.FT14)
      return new FT14Processor(logger);
    else if (fileType == FileType.BQ)
      return new BQProcessor(logger);

    return null;
  }

  /** Function returns processor that suits for parsing input files in specified directory
   *
   * @param inqPath - path with input files
   * @param logger - application logger
   * @return sutable processor for input file parsing or null
   */
  static AProcessor getProcessor(String inqPath, Logger logger) {
    FileType fileType = fileTypeInDirectory(inqPath, logger);
    if (fileType == FileType.UNKNOWN) {
      logger.error("0701: Directory is empty or contains unknown files: " + inqPath);
      return null;
    }
    else
      return getProcessor(fileType, logger);
  }

  /** Defines file type by first string
   *
   * @param fileName - check file, full path
   * @return enum with file type from FileType
   */
  static FileType fileType(String fileName, Logger logger) {

    FileType retType = FileType.UNKNOWN;

    try {
      RandomAccessFile raf = new RandomAccessFile(fileName, "r");
      // Try to read first 3 bytes - for MT100 binary container it will be message length (2 bytes) and '{'
      byte[] threeBytes = new byte[3];
      if (raf.read(threeBytes) == 3) {
        if (threeBytes[2] == (byte) '{') {
          raf.seek(2); // Stay of message begin
        }
        else {
          raf.seek(0);
        }
        String firstStr = raf.readLine();
        if (firstStr != null) {
          if (firstStr.matches("^<\\?xml?.+")) { // XML
            // Try to read root element (without XML parsing)
            int pos = firstStr.indexOf("?>");
            String rootName = "";
            do {
              pos = firstStr.indexOf("<", pos);
              if (pos >= 0) { // Root element in this string
                rootName = firstStr.substring(pos + 1);
                pos = rootName.indexOf(" ");
                if (pos >= 0) {
                  rootName = rootName.substring(0, pos);
                }
                break;
              } else {
                firstStr = raf.readLine();
                pos = 0;
              }
            }
            while (firstStr != null);
            if (rootName.matches("docs")) {
              retType = FileType.BQ;
            } else if (rootName.matches("ED.+") || rootName.matches("Packet.+")) {
              retType = FileType.UFEBS;
            }
          }
          else if (firstStr.matches("^\\{1:.+")) { // SWIFT
            int pos = firstStr.indexOf("{2:"); // Message type from block 2: "{2:O100"
            if (pos >= 0) {
              String msgType = firstStr.substring(pos + 4, pos + 7);
              if (msgType.equals("100")) {
                retType = FileType.MT100;
              } else if (msgType.equals("103")) {
                retType = FileType.MT103;
              }
            }
          }
          else if (firstStr.matches("^..MOS.+")) // FT14 - RTMOS, CLMOS, etc
            retType = FileType.FT14;
        }
      }
      raf.close();
    }
    catch (IOException e) {
      logger.error("0702: Error access file: " + fileName);
    }
    return retType;
  }

  /** Checks files type in specified path.
   * File type obtains from first file in directory (if directory contains mix files- oops)
   * If directory has no files, return UNKNOWN
   *
   * @param inqPath - path for check files, full path
   * @return enum with file type from FileType
   */
  private static FileType fileTypeInDirectory(String inqPath, Logger logger) {
    try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inqPath))) {
      for (Path path : directoryStream) {
        if (isRegularFile(path)) {
          return fileType(path.toString(), logger);
        }
      }
    }
    catch (IOException e) {
      logger.error("0703: Error while file system access: " + inqPath);
    }
    return FileType.UNKNOWN;
  }

}
