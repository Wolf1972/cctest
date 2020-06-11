package ru.bis.cc.misc.test;

abstract class AProcessor {

  abstract void readAll(String inPath, FDocumentArray fDocs); // Reads all files from specified directory
  abstract boolean readFile(String fileName, FDocumentArray fDocs); // Reads one file with one or several messages

  abstract void createAll(String outPath, FDocumentArray fDocs);

  abstract void checkAll(String inqPath, String xsdPath);
}
