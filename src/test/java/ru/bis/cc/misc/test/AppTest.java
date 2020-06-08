package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
  public void testEDParse() {
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

  /** Test one XML node parsing with ED1xx
   *
   */
  @Test
  public void testBQParse() {
    String xmlString =
            "<?xml version=\"1.0\" encoding=\"windows-1251\"?>" +
            "<docs xmlns=\"http://www.bis.ru/XCNG/BQ\" ver-format=\"1.0.0\" eod=\"2017-09-04\" filial-id=\"0001\">\n" +
            "  <doc action=\"update\" id=\"10006179707402\">\n" +
            "    <reg op-status=\"В\" doc-type=\"01\" doc-num=\"111\" doc-num-full=\"111-278584\" doc-date=\"2020-05-15\"" +
            "         order-pay=\"3\" chgoff-date=\"2020-05-14\" payee-received=\"2020-05-13\" branch-id=\"0001\" delivery=\"Е\"/>\n" +
            "    <details>Payment purpose</details>\n" +
            "    <tax-index ocato=\"03233556000\" date-index=\"01.02.2020\" num-index=\"20\" period-index=\"МС.12.2011\"" +
            "         purpose-index=\"ТП\" status-index=\"14\" cbc=\"39210202020062000160\" type-index=\"1\"/>\n" +
            "    <payer name=\"Payer name\" acct=\"40702810300000000300\" inn=\"2342018036\" kpp=\"234201001\"/>\n" +
            "    <payee name=\"Payee name\" acct=\"40101810300000010013\" inn=\"2308014320\" kpp=\"231001001\"/>\n" +
            "    <payer-bank name=\"Payer bank name\" acct=\"30101810100000000101\" ident-type=\"МФО-9\" ident-code=\"044525101\"/>\n" +
            "    <payee-bank name=\"Payee bank name\" acct=\"30101810200000000001\" ident-type=\"МФО-9\" ident-code=\"040349001\"/>\n" +
            "    <entries>\n" +
            "       <entry op-entry=\"0\" acct-cr=\"30102810300000000101\" acct-db=\"40702810300000000300\" amt-cur=\"0\" amt-rub=\"52.7\"" +
            "              currency=\"810\" op-date=\"2017-09-04\" eard=\"N\" qty=\"0\"/>\n" +
            "    </entries>\n" +
            "    <references>\n" +
            "      <reference type=\"DBI\" reference=\"RE0111205029324\" trip=\"1\" direction=\"I\"/>\n" +
            "    </references>\n" +
            "    <signs>\n" +
            "      <sign code-value=\"12345678901234567890123456789012345\" code=\"ПризУслПер\"/>\n" +
            "      <sign code-value=\"Электронно\" code=\"СпособПолуч\"/>\n" +
            "    </signs>\n" +
            "  </doc>\n" +
            "</docs>";
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xmlString));
      Document xmlDoc = builder.parse(is);
      Node root = xmlDoc.getDocumentElement();
      NodeList docOne = root.getChildNodes(); // List of child nodes for <doc>

      for (int i = 0; i < docOne.getLength(); i++) {

        Node edChildNode = docOne.item(i);
        if (edChildNode.getNodeName().equals("doc")) {
          FDocument doc = BQParser.fromXML(edChildNode);
          if (doc != null) {
            assertFalse(doc.isUrgent);

            assertEquals(doc.docNum, "111");
            assertEquals(doc.docDate, "2020-05-15");
            assertEquals((long) doc.amount, 5270L);
            assertEquals(doc.purpose, "Payment purpose");
            assertEquals(doc.priority, "3");
            assertEquals(doc.chargeOffDate, "2020-05-14");
            assertEquals(doc.receiptDate, "2020-05-13");
            assertEquals(doc.transKind, "01");

            assertEquals(doc.payerName, "Payer name");
            assertEquals(doc.payerAccount, "40702810300000000300");
            assertEquals(doc.payerINN, "2342018036");
            assertEquals(doc.payerCPP, "234201001");
            assertEquals(doc.payerBankBIC, "044525101");
            assertEquals(doc.payerBankAccount, "30101810100000000101");

            assertEquals(doc.payeeName, "Payee name");
            assertEquals(doc.payeeAccount, "40101810300000010013");
            assertEquals(doc.payeeINN, "2308014320");
            assertEquals(doc.payeeCPP, "231001001");
            assertEquals(doc.payeeBankBIC, "040349001");
            assertEquals(doc.payeeBankAccount, "30101810200000000001");

            assertTrue(doc.isTax);
            assertEquals(doc.taxStatus, "14"); // 101
            assertEquals(doc.CBC, "39210202020062000160"); // 104
            assertEquals(doc.OCATO, "03233556000"); // 105
            assertEquals(doc.taxPaytReason, "ТП"); // 106
            assertEquals(doc.taxPeriod, "МС.12.2011"); // 107
            assertEquals(doc.taxDocNum, "20"); // 108
            assertEquals(doc.taxDocDate, "01.02.2020"); // 109
            assertEquals(doc.taxPaytKind, "1"); // 110
          } else
            fail("BQ XML parse failed.");
        }
      }
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      fail("Preliminary XML parsing failed.");
    }
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

    HashMap <Long, FDocument> packet = new HashMap<>();

    // UFEBS processor test
    UFEBSProcessor procUFEBS = new UFEBSProcessor(logger);
    assertTrue("UFEBS single file parse error.", procUFEBS.readFile(patternPath + "single.xml", patternDocs));
    assertTrue("UFEBS packet file parse error.", procUFEBS.readFile(patternPath + "packet.xml", patternDocs));

    // UFEBS assembler test
    for (Map.Entry<Long, FDocument> item : patternDocs.entrySet()) { // Build array with non-urgent documents only (to create one packet)
      FDocument doc = item.getValue();
      if (!doc.isUrgent) packet.put(doc.getId(), doc);
    }
    procUFEBS.createAll(outPath, packet);
    assertTrue("UFEBS packet XSD check error.", procUFEBS.isXMLValid(outPath + "pck000000.xml", xsdPath));
    assertTrue("UFEBS created packet file parse error.", procUFEBS.readFile(outPath + "pck000000.xml", sampleDocs));
    sampleDocs.clear();

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
