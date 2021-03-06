package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

abstract class AProcessor {

  Logger logger;

  abstract void readAll(String inPath, FDocumentArray fDocs); // Reads all files with documents from specified directory

  abstract boolean readFile(String fileName, FDocumentArray fDocs); // Reads one file with one or several documents

  abstract void createAll(String outPath, FDocumentArray fDocs);

  abstract void checkAll(String inqPath, String xsdPath);
}
