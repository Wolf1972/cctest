package ru.bis.cc.misc.test;

import java.util.HashMap;

class SWIFTProcessor extends AProcessor {
  @Override
  void readAll(String inPath, HashMap<Long, FDocument> fDocs) {

  }

  @Override
  boolean readFile(String fileName, HashMap<Long, FDocument> fDocs) {
    return false;
  }

  @Override
  void createAll(String outPath, HashMap<Long, FDocument> fDocs) {

  }

  @Override
  void checkAll(String inqPath, String xsdPath) {

  }

}
