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
  UNKNOWN, UFEBS, MT103, FT14, BQ
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
      logger.error("0502: Directory is empty or contains unknown files: " + inqPath);
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
    try {
      RandomAccessFile raf = new RandomAccessFile(fileName, "r");
      String firstStr = raf.readLine();
      if (firstStr != null) {
        if (firstStr.matches("^<\\?xml?.+")) // XML
          return FileType.UFEBS;
        else if (firstStr.matches("^\\{1:.+")) // SWIFT
          return FileType.MT103;
        else if (firstStr.matches("^..MOS.+")) // FT14 - RTMOS, CLMOS
          return FileType.FT14;
      }
      raf.close();
    }
    catch (IOException e) {
      logger.error("0101: Error access file: " + fileName, e);
    }
    return FileType.UNKNOWN;
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
      logger.error("0501: Error while file system access: " + inqPath);
    }
    return FileType.UNKNOWN;
  }

}
