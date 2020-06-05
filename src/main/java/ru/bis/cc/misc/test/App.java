package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

/**
 * UFEBS files parsing, converting to FT14 packet, comparing results of UFEBS parsing from 2 directories
 *
 */
public class App {

  private static HashMap<Long, FDocument> sampleDocs = new HashMap<>(); // Checked documents array
  private static HashMap<Long, FDocument> patternDocs = new HashMap<>(); // Pattern documents array (for compare)

  public static void main(String[] args) {

    String configPath = ".\\target\\";

    String samplePath = ".\\target\\in\\";
    String patternPath = ".\\target\\pattern\\";
    String outFT14Path = ".\\target\\out-ft14\\";
    String outMT103Path = ".\\target\\out-mt103\\";
    String outEDPath = ".\\target\\out-ed\\";
    String inMT103Path = ".\\target\\in-mt103\\";
    String xsdPath = ".\\target\\XMLSchemas\\";

    System.out.println("UFEBS CC test helper (c) BIS 2020.");

    // Looking for log4j
    String log4JPropertyFile = configPath + "log4j2.xml"; // Is Log4j configuration file in custom place?
    if (Files.isRegularFile(Paths.get(log4JPropertyFile))) {
      System.setProperty("log4j.configurationFile", log4JPropertyFile);
    }
    // We will define configuration later
    Logger logger = LogManager.getLogger(App.class);

    FileInputStream fis;
    Properties property = new Properties();
    String configFile = configPath + "config.properties";
    try {
      fis = new FileInputStream(configFile);
      property.load(fis);
      samplePath = property.getProperty("inPath");
      patternPath = property.getProperty("patternPath");
      outFT14Path = property.getProperty("outFT14Path");
      outMT103Path = property.getProperty("outMT103Path");
      outEDPath = property.getProperty("outEDPath");
      inMT103Path = property.getProperty("inMT103Path");
      xsdPath = property.getProperty("xsdPath");
    }
    catch (IOException e) {
      logger.error("0001: Error opening properties file: " + configFile);
    }

    UFEBSProcessor procUFEBS = new UFEBSProcessor(xsdPath, logger);
    procUFEBS.readAll(patternPath, patternDocs);
    procUFEBS.readAll(samplePath, sampleDocs);

    procUFEBS.createAll(outEDPath, sampleDocs);

    Comparator comparator = new Comparator(logger);
    comparator.compare(patternDocs, sampleDocs);

    FT14Processor procFT14 = new FT14Processor(logger);
    procFT14.createAll(outFT14Path, patternDocs);

    MT103Processor procMT103 = new MT103Processor(logger);

    procMT103.createAll(outMT103Path, patternDocs);
    sampleDocs.clear();
    procMT103.readAll(inMT103Path, sampleDocs);
    comparator.compare(patternDocs, sampleDocs);

    logger.info("0000: End of work.");

  }

}