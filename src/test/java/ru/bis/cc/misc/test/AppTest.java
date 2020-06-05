package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for simple App.
 */
public class AppTest
{

  private static Logger logger = LogManager.getLogger(AppTest.class);
  private static HashMap<Long, FDocument> sampleDocs = new HashMap<>(); // Checked documents array
  private static HashMap<Long, FDocument> patternDocs = new HashMap<>(); // Pattern documents array (for compare)

  /** Integrity test - functionality at all
   *
   */
  @Test
  public void integrityTest()
  {

    String xsdPath = ".\\src\\test\\resources\\XMLSchemas\\";
    String patternPath = ".\\src\\test\\resources\\pattern\\";
    String outPath = ".\\src\\test\\resources\\out\\";

    // UFEBS processor test
    UFEBSProcessor procUFEBS = new UFEBSProcessor(xsdPath, logger);
    assertTrue("UFEBS single file parse error.", procUFEBS.readOne(patternPath + "single.xml", patternDocs));
    assertTrue("UFEBS packet file parse error.", procUFEBS.readOne(patternPath + "packet.xml", patternDocs));

    // FT14 assembler test
    String outFT14File = outPath + "ft14test.txt"; // First of all - delete previous test results
    if (!deleteOutFile(outFT14File)) fail("FT14 file delete error: " + outFT14File);

    FT14Processor procFT14 = new FT14Processor(logger);
    procFT14.createAll(outPath, patternDocs);
    assertTrue("Output file FT14 not found" + outFT14File, Files.isRegularFile(Paths.get(outFT14File)));
    compareTwoFiles(patternPath + "ft14test.txt", outFT14File);

    // MT103 assembler test
    String outMT103File = outPath + "mt103test.txt";
    if (!deleteOutFile(outMT103File)) fail("MT103 file delete error: " + outFT14File);

    MT103Processor procMT103 = new MT103Processor(logger);
    procMT103.createAll(outPath, patternDocs);
    assertTrue("Output file MT103 not found" + outMT103File, Files.isRegularFile(Paths.get(outMT103File)));
    compareTwoFiles(patternPath + "mt103test.txt", outPath + "mt103test.txt");

    // MT103 parser test
    assertTrue("MT103 file parse error.", procMT103.readFile(outMT103File, sampleDocs));

    // Comparator test - compare pattern documents was loaded from UFEBS, sample documents was loaded from MT103
    Comparator comparator = new Comparator(logger);
    assertTrue("Comparator error found when testing UFEBS and MT103 documents.", comparator.compare(patternDocs, sampleDocs));

  }

  /** Function deletes one output file if it exists
   *
   * @param fileName
   * @return boolean - file deleted or not exist (true), error (false)
   */
  boolean deleteOutFile(String fileName) {
    if (Files.isRegularFile(Paths.get(fileName))) {
      try {
        Files.delete(Paths.get(fileName));
      }
      catch (IOException e) {
        return false;
      }
    }
    return true;
  }

  /** Function compares two text files and fails current test if files mismatch
   *
   * @param patternFileName = pattern file
   * @param sampleFileName - sample file
   */
  void compareTwoFiles(String patternFileName, String sampleFileName) {
    String fileName = "";
    try {
      fileName = patternFileName;
      BufferedReader patternFile = new BufferedReader (new FileReader(fileName));
      fileName = sampleFileName;
      BufferedReader sampleFile = new BufferedReader (new FileReader(fileName));
      String pattern;
      int lineCount = 0;
      while ((pattern = patternFile.readLine()) != null) {
        lineCount++;
        String sample = sampleFile.readLine();
        assertTrue("Mismatch files at line " + lineCount + " in pattern file.", pattern.equals(sample));
      }
      assertTrue("Sample file has more lines then pattern " + patternFileName, (sampleFile.readLine() == null));
      assertTrue("Pattern file has more lines then sample " + sampleFileName, (patternFile.readLine() == null));
    }
    catch (FileNotFoundException e) {
      fail("Error while opening file to compare: " + fileName);
    }
    catch (IOException e) {
      fail("Error while reading files: " + patternFileName + " or " + sampleFileName);
    }
  }
}
