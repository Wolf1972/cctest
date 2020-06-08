package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;
import java.util.HashMap;

public abstract class AProcessor {

  abstract void readAll(String inPath, HashMap<Long, FDocument> fDocs);
  abstract void createAll(String outPath, HashMap<Long, FDocument> fDocs);
  abstract void checkAll(String inqPath, String xsdPath, Logger logger);
}
