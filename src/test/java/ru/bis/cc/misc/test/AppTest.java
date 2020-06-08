package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest
{

  private static Logger logger = LogManager.getLogger(AppTest.class);
  private static HashMap<Long, FDocument> sampleDocs = new HashMap<>(); // Checked documents array
  private static HashMap<Long, FDocument> patternDocs = new HashMap<>(); // Pattern documents array (for compare)

  /** Test one XML node parsing with ED1xx
   *
   */
  @Test
  public void testEDNodeParse() {
    String xmlString =
            "<?xml version=\"1.0\" encoding=\"windows-1251\"?>" +
            "<ED101 EDAuthor=\"4525101000\" EDDate=\"2020-01-10\" EDNo=\"1000000\"" +
            "       ChargeOffDate=\"2020-01-11\" ReceiptDate=\"2020-01-12\" Priority=\"5\" TransKind=\"01\"" +
            "       PaymentPrecedence=\"60\" SystemCode=\"05\" Sum=\"100\" PaytKind=\"4\" PaymentID=\"111\"" +
            "       xmlns=\"urn:cbr-ru:ed:v2.0\">" +
            "  <AccDoc AccDocDate=\"2020-01-09\" AccDocNo=\"1\"/>" +
            "  <Payer INN=\"123456789012\" KPP=\"111111111\" PersonalAcc=\"40702810500000000014\">" +
            "    <Name>Payer name</Name><Bank CorrespAcc=\"30101810100000000101\" BIC=\"044525101\"/>" +
            "  </Payer>" +
            "  <Payee INN=\"1234567890\" KPP=\"222222222\" PersonalAcc=\"40702810100000000389\">" +
            "    <Name>Payee name</Name><Bank CorrespAcc=\"30101810200000000111\" BIC=\"044525111\"/>" +
            "  </Payee>" +
            "  <DepartmentalInfo DocDate=\"01.02.2020\" DocNo=\"20\" TaxPeriod=\"МС.02.2020\"" +
            "        PaytReason=\"TP\" OKATO=\"45286560\" CBC=\"18210101011011000110\"" +
            "        DrawerStatus=\"01\" TaxPaytKind=\"1\"/>" +
            "  <Purpose>Payment purpose</Purpose>" +
            "</ED101>";

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xmlString));
      Document xmlDoc = builder.parse(is);
      Node root = xmlDoc.getDocumentElement();
      FDocument doc = UFEBSParser.fromXML(root);
      if (doc != null) {
        assertEquals(doc.edNo, "1000000");
        assertEquals(doc.edDate, "2020-01-10");
        assertTrue(doc.isUrgent);

        assertEquals(doc.docNum, "1");
        assertEquals(doc.docDate, "2020-01-09");
        assertEquals((long) doc.amount, 100L);
        assertEquals(doc.purpose, "Payment purpose");
        assertEquals(doc.UIN, "111");
        assertEquals(doc.priority, "5");
        assertEquals(doc.chargeOffDate, "2020-01-11");
        assertEquals(doc.receiptDate, "2020-01-12");
        assertEquals(doc.transKind, "01");

        assertEquals(doc.payerName, "Payer name");
        assertEquals(doc.payerAccount, "40702810500000000014");
        assertEquals(doc.payerINN, "123456789012");
        assertEquals(doc.payerCPP, "111111111");
        assertEquals(doc.payerBankBIC, "044525101");
        assertEquals(doc.payerBankAccount, "30101810100000000101");

        assertEquals(doc.payeeName, "Payee name");
        assertEquals(doc.payeeAccount, "40702810100000000389");
        assertEquals(doc.payeeINN, "1234567890");
        assertEquals(doc.payeeCPP, "222222222");
        assertEquals(doc.payeeBankBIC, "044525111");
        assertEquals(doc.payeeBankAccount, "30101810200000000111");

        assertTrue(doc.isTax);
        assertEquals(doc.taxStatus, "01"); // 101
        assertEquals(doc.CBC, "18210101011011000110"); // 104
        assertEquals(doc.OCATO, "45286560"); // 105
        assertEquals(doc.taxPaytReason, "TP"); // 106
        assertEquals(doc.taxPeriod, "МС.02.2020"); // 107
        assertEquals(doc.taxDocNum, "20"); // 108
        assertEquals(doc.taxDocDate, "01.02.2020"); // 109
        assertEquals(doc.taxPaytKind, "1"); // 110
      }
      else
        fail("UFEBS ED parse failed.");
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      fail("Preliminary XML parsing failed.");
    }
  }


  /** Test one MT103 message parsing
   *
   */
  @Test
  public void testMT103Parse() {
    String str =
            "{1:F01DEUTRUMMXXXX0000000101}{2:O1031007200514DBEBRUMMAXXX00000001011107200514N}{4:" + System.lineSeparator() +
            "20:101" + System.lineSeparator() +
            "32A:200514RUB100.20" + System.lineSeparator() +
            "50K:/42301810900000000002" + System.lineSeparator() +
            "INN776521543603 Payer name" + System.lineSeparator() +
            "52A:BIK044525101" + System.lineSeparator() +
            "53B:/30101810100000000101" + System.lineSeparator() +
            "57D:/30101810000000000001" + System.lineSeparator() +
            "BIK044583001" + System.lineSeparator() +
            "59:/40101810800000010041" + System.lineSeparator() +
            "INN7703363868/KPP770102011 Payee" + System.lineSeparator() +
            " name" + System.lineSeparator() +
            "70://771201325/01/18210101011011000110" + System.lineSeparator() +
            "/45286560/ТП/МС.04.2020/20/07.05.202" + System.lineSeparator() +
            "0/1/Payment purpose" + System.lineSeparator() +
            "УИН111///" + System.lineSeparator() +
            "77B:REF101" + System.lineSeparator() +
            "-}";
    FDocument doc = MT103Parser.fromString(str);
    if (doc != null) {
      assertFalse(doc.isUrgent);
      assertEquals(doc.docNum, "101");
      assertEquals(doc.docDate, "2020-05-14");
      assertEquals((long) doc.amount, 10020L);
      assertEquals(doc.purpose, "Payment purpose");
      assertEquals(doc.UIN, "111");

      assertEquals(doc.payerName, "Payer name");
      assertEquals(doc.payerINN, "776521543603");
      assertEquals(doc.payerCPP, "771201325");
      assertEquals(doc.payerAccount, "42301810900000000002");
      assertEquals(doc.payerBankBIC, "044525101");
      assertEquals(doc.payerBankAccount, "30101810100000000101");

      assertEquals(doc.payeeName, "Payee name");
      assertEquals(doc.payeeINN, "7703363868");
      assertEquals(doc.payeeCPP, "770102011");
      assertEquals(doc.payeeAccount, "40101810800000010041");
      assertEquals(doc.payeeBankBIC, "044583001");
      assertEquals(doc.payeeBankAccount, "30101810000000000001");

      assertTrue(doc.isTax);
      assertEquals(doc.taxStatus, "01"); // 101
      assertEquals(doc.CBC, "18210101011011000110"); // 104
      assertEquals(doc.OCATO, "45286560"); // 105
      assertEquals(doc.taxPaytReason, "ТП"); // 106
      assertEquals(doc.taxPeriod, "МС.04.2020"); // 107
      assertEquals(doc.taxDocNum, "20"); // 108
      assertEquals(doc.taxDocDate, "07.05.2020"); // 109
      assertEquals(doc.taxPaytKind, "1"); // 110
    }
    else
      fail("MT103 parse failed.");
  }

  /** Integrity test - test functionality at all
   *
   */
  @Test
  public void integrityTest()
  {

    String xsdPath = ".\\src\\test\\resources\\XMLSchemas\\";
    String patternPath = ".\\src\\test\\resources\\pattern\\";
    String outPath = ".\\src\\test\\resources\\out\\";

    // UFEBS processor test
    UFEBSProcessor procUFEBS = new UFEBSProcessor(logger);
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
   * @param fileName - file name
   * @return boolean - file deleted or not exist (true), error (false)
   */
  private boolean deleteOutFile(String fileName) {
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
  private void compareTwoFiles(String patternFileName, String sampleFileName) {
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
        assertEquals("Mismatch files at line " + lineCount + " in pattern file.", pattern, sample);
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
