package ru.bis.cc.misc.test;

import java.util.HashMap;

abstract class AProcessor {

  abstract void readAll(String inPath, HashMap<Long, FDocument> fDocs); // Reads all files from specified directory
  abstract boolean readFile(String fileName, HashMap<Long, FDocument> fDocs); // Reads one file with one or several messages

  abstract void createAll(String outPath, HashMap<Long, FDocument> fDocs);

  abstract void checkAll(String inqPath, String xsdPath);
}
