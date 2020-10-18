package ru.bis.cc.misc.test;

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
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

// TODO: divide big integration test to several small tests

/**
 * Unit test for simple App.
 */
public class AppTest
{

  String xsdPath = ".\\src\\test\\resources\\XMLSchemas\\";
  String patternPath = ".\\src\\test\\resources\\pattern\\";
  String outPath = ".\\src\\test\\resources\\out\\";
  String staticPath = ".\\src\\test\\resources\\static\\";
  String banksPath = ".\\src\\test\\resources\\banks\\";

  FDocumentArray patternDocs = new FDocumentArray();
  FDocumentArray sampleDocs = new FDocumentArray();
  FDocumentArray reverseDocs;
  FDocumentArray packet = new FDocumentArray();

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
      UFEBSParser parser = new UFEBSParser();
      FDocument doc = parser.fromXML(root);
      if (doc != null) {
        assertEquals("1000000", doc.edNo);
        assertEquals("2020-01-10", doc.edDate);
        assertTrue(doc.isUrgent);

        assertEquals("1", doc.docNum);
        assertEquals("2020-01-09", doc.docDate);
        assertEquals(100L, (long) doc.amount);
        assertEquals("Payment purpose", doc.purpose);
        assertEquals("111", doc.UIN);
        assertEquals("5", doc.priority);
        assertEquals("2020-01-11", doc.chargeOffDate);
        assertEquals("2020-01-12", doc.receiptDate);
        assertEquals("01", doc.transKind);

        assertEquals("Payer name", doc.payerName);
        assertEquals("40702810500000000014", doc.payerAccount);
        assertEquals("123456789012", doc.payerINN);
        assertEquals("111111111", doc.payerCPP);
        assertEquals("044525101", doc.payerBankBIC);
        assertEquals("30101810100000000101", doc.payerBankAccount);

        assertEquals("Payee name", doc.payeeName);
        assertEquals("40702810100000000389", doc.payeeAccount);
        assertEquals("1234567890", doc.payeeINN);
        assertEquals("222222222", doc.payeeCPP);
        assertEquals("044525111", doc.payeeBankBIC);
        assertEquals("30101810200000000111", doc.payeeBankAccount);

        assertTrue(doc.isTax);
        assertEquals("01", doc.taxStatus); // 101
        assertEquals("18210101011011000110", doc.CBC); // 104
        assertEquals("45286560", doc.OCATO); // 105
        assertEquals("TP", doc.taxPaytReason); // 106
        assertEquals("МС.02.2020", doc.taxPeriod); // 107
        assertEquals("20", doc.taxDocNum); // 108
        assertEquals("01.02.2020", doc.taxDocDate); // 109
        assertEquals("1", doc.taxPaytKind); // 110
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
            ":20:101" + System.lineSeparator() +
            ":32A:200514RUB100.20" + System.lineSeparator() +
            ":50K:/42301810900000000002" + System.lineSeparator() +
            "INN776521543603 Payer name" + System.lineSeparator() +
            ":52A:BIK044525101" + System.lineSeparator() +
            ":53B:/30101810100000000101" + System.lineSeparator() +
            ":57D:/30101810000000000001" + System.lineSeparator() +
            "BIK044583001" + System.lineSeparator() +
            ":59:/40101810800000010041" + System.lineSeparator() +
            "INN7703363868/KPP770102011 Payee" + System.lineSeparator() +
            " name" + System.lineSeparator() +
            ":70://771201325/01/18210101011011000110" + System.lineSeparator() +
            "/45286560/ТП/МС.04.2020/20/07.05.202" + System.lineSeparator() +
            "0/1/Payment purpose" + System.lineSeparator() +
            "УИН111///" + System.lineSeparator() +
            ":77B:REF101" + System.lineSeparator() +
            "-}";
    MT103Parser mt103parser = new MT103Parser();
    FDocument doc = mt103parser.fromString(str);
    if (doc != null) {
      assertFalse(doc.isUrgent);
      assertEquals("101", doc.docNum);
      assertEquals("2020-05-14", doc.docDate);
      assertEquals(10020L, (long) doc.amount);
      assertEquals("Payment purpose", doc.purpose);
      assertEquals("111", doc.UIN);

      assertEquals("Payer name", doc.payerName);
      assertEquals("776521543603", doc.payerINN);
      assertEquals("771201325", doc.payerCPP);
      assertEquals("42301810900000000002", doc.payerAccount);
      assertEquals("044525101", doc.payerBankBIC);
      assertEquals("30101810100000000101", doc.payerBankAccount);

      assertEquals("Payee name", doc.payeeName);
      assertEquals("7703363868", doc.payeeINN);
      assertEquals("770102011", doc.payeeCPP);
      assertEquals("40101810800000010041", doc.payeeAccount);
      assertEquals("044583001", doc.payeeBankBIC);
      assertEquals("30101810000000000001", doc.payeeBankAccount);

      assertTrue(doc.isTax);
      assertEquals("01", doc.taxStatus); // 101
      assertEquals("18210101011011000110", doc.CBC); // 104
      assertEquals("45286560", doc.OCATO); // 105
      assertEquals("ТП", doc.taxPaytReason); // 106
      assertEquals("МС.04.2020", doc.taxPeriod); // 107
      assertEquals("20", doc.taxDocNum); // 108
      assertEquals("07.05.2020", doc.taxDocDate); // 109
      assertEquals("1", doc.taxPaytKind); // 110
    }
    else
      fail("MT103 parse failed.");
  }

  /** Test for MT100 parsing
   *
   */
  @Test
  public void testMT100Parse() {
    String str =
            "{1:F01DEUTRUMMXXXX0229000004}{2:O1001559160128HRANRUMMXXXX00000000011601281559N}{4:" + System.lineSeparator() +
            ":20:101" + System.lineSeparator() +
            ":32A:200514RUB100,20" + System.lineSeparator() +
            ":50:/30110810800000000004" + System.lineSeparator() +
            "INN7750003904" + System.lineSeparator() +
            "Payer name" + System.lineSeparator() +
            ":52D:BIK044525225" + System.lineSeparator() +
            "Payer bank" + System.lineSeparator() +
            ":59:/40702810200000000772" + System.lineSeparator() +
            "INN7707099460" + System.lineSeparator() +
            "Payee name" + System.lineSeparator() +
            ":70:Payment purpose" + System.lineSeparator() +
            ":72:/REC/20.01.2016" + System.lineSeparator() +
            "-}";
    MT100Parser mt100parser = new MT100Parser();
    FDocument doc = mt100parser.fromString(str);
    if (doc != null) {
      assertFalse(doc.isUrgent);
      assertEquals("101", doc.docNum);
      assertEquals("2020-05-14", doc.docDate);
      assertEquals(10020L, (long) doc.amount);
      assertEquals("Payment purpose", doc.purpose);

      assertEquals("Payer name", doc.payerName);
      assertEquals("7750003904", doc.payerINN);
      assertEquals("30110810800000000004", doc.payerAccount);
      assertEquals("044525225", doc.payerBankBIC);

      assertEquals("Payee name", doc.payeeName);
      assertEquals("7707099460", doc.payeeINN);
      assertEquals("40702810200000000772", doc.payeeAccount);

    }
    else
      fail("MT100 parse failed.");
  }


  /** Test for MT940 parsing
   *
   */
  @Test
  public void testMT940Parse() {
    String str =
            ":61:1606030603C12,45NTRF000443//044525225" + System.lineSeparator() +
            ":86:REF0001";
    MT940Parser parser = new MT940Parser();
    FDocument doc = parser.fromString(str);
    if (doc != null) {
      assertEquals("Date", "2016-06-03", doc.edDate);
      assertEquals("Amount", 1245L, (long) doc.amount);
      assertEquals("DocNum", "000443", doc.docNum);
      assertEquals("BIC", "044525225", doc.payeeBankBIC);
      assertEquals("SBP ref", "REF0001", doc.referenceSBP);
    }
    else
      fail("MT940 parse failed.");
  }

  /** Test for SWIFT tag extracting
   *
   */
  @Test
  public void testGetTag() {
    String[] message = new String[] { "{1:F01DEUTRUMMXXXX0000000101}{2:O1031007200514DBEBRUMMAXXX00000001011107200514N}{4:",
            ":20:101",
            ":32A:200514RUB100.20",
            ":50K:/42301810900000000002",
            "INN776521543603 Payer name",
            ":59:/40101810800000010041",
            "INN7703363868/KPP770102011 Payee",
            " name",
            ":70://771201325/01/18210101011011000110",
            "/45286560/ТП/МС.04.2020/20/07.05.202",
            "0/1/Payment purpose",
            "УИН111///",
            "-}"};
    MT103Parser mt103parser = new MT103Parser();

    ArrayList<String> tag20 = new ArrayList<>(); tag20.add("101");
    assertEquals(tag20, mt103parser.getTag(message, "20"));

    ArrayList<String> tag32A = new ArrayList<>(); tag32A.add("200514RUB100.20");
    assertEquals(tag32A, mt103parser.getTag(message, "32A"));

    ArrayList<String> tag50K = new ArrayList<>();
    tag50K.add("/42301810900000000002"); tag50K.add("INN776521543603 Payer name");
    assertEquals(tag50K, mt103parser.getTag(message, "50K"));

    ArrayList<String> tag59 = new ArrayList<>();
    tag59.add("/40101810800000010041"); tag59.add("INN7703363868/KPP770102011 Payee"); tag59.add(" name");
    assertEquals(tag59, mt103parser.getTag(message, "59"));

    ArrayList<String> tag70 = new ArrayList<>();
    tag70.add("//771201325/01/18210101011011000110"); tag70.add("/45286560/ТП/МС.04.2020/20/07.05.202");
    tag70.add("0/1/Payment purpose"); tag70.add("УИН111///");
    assertEquals(tag70, mt103parser.getTag(message, "70"));

    ArrayList<String> tag72 = new ArrayList<>();
    assertEquals(tag72, mt103parser.getTag(message, "72")); // Missing expected tag
    assertEquals(tag72, mt103parser.getTag(message, "??")); // Unexpected tag

  }

  /** Test for counterparty parsing win INN/KPP
   *
   */
  @Test
  public void testReadCounterparty() {
    FDocument doc = new FDocument();
    MT100Parser parser = new MT100Parser();

    String[] aTag0 = new String[]{":50:INN1234567890"};
    parser.readCounterparty(aTag0, doc, "50");
    assertEquals("50/0","1234567890", doc.payerINN);
    assertEquals("50/0", "", doc.payerName);

    String[] aTag1 = new String[]{":50:/12345678901234567890"};
    parser.readCounterparty(aTag1, doc, "50");
    assertEquals("50/1","12345678901234567890", doc.payerAccount);

    String[] aTag2 = new String[]{":50:Payer name"};
    parser.readCounterparty(aTag2, doc, "50");
    assertEquals("50/2","Payer name", doc.payerName);

    String[] aTag3 = new String[]{":50:/12345678901234567890","Payer name"};
    parser.readCounterparty(aTag3, doc, "50");
    assertEquals("50/3","12345678901234567890", doc.payerAccount);
    assertEquals("50/3","Payer name", doc.payerName);

    String[] aTag4 = new String[]{":50:/12345678901234567890","Payer nameINN1234567890"};
    parser.readCounterparty(aTag4, doc, "50");
    assertEquals("50/4","12345678901234567890", doc.payerAccount);
    assertEquals("50/4","Payer name", doc.payerName);
    assertEquals("50/4","1234567890", doc.payerINN);

    String[] aTag5 = new String[]{":50:Payer nameINN1234567890continue"};
    parser.readCounterparty(aTag5, doc, "50");
    assertEquals("50/5","Payer namecontinue", doc.payerName);
    assertEquals("50/5","1234567890", doc.payerINN);

    String[] aTag6 = new String[]{":50:Payer nameINN1234567890 continue"};
    parser.readCounterparty(aTag6, doc, "50");
    assertEquals("50/6","Payer namecontinue", doc.payerName); // eats inner space
    assertEquals("50/6","1234567890", doc.payerINN);

    String[] aTag7 = new String[]{":50:INN1234567890 Payer name"};
    parser.readCounterparty(aTag7, doc, "50");
    assertEquals("50/7","Payer name", doc.payerName);
    assertEquals("50/7","1234567890", doc.payerINN);

    String[] aTag8 = new String[]{":50:INN1234567890Payer name"};
    parser.readCounterparty(aTag8, doc, "50");
    assertEquals("50/8","Payer name", doc.payerName);
    assertEquals("50/8","1234567890", doc.payerINN);

    String[] aTag9 = new String[]{":50:Payer nameINN1234567890/KPP123456789continue"};
    parser.readCounterparty(aTag9, doc, "50");
    assertEquals("50/9","Payer namecontinue", doc.payerName);
    assertEquals("50/9","1234567890", doc.payerINN);
    assertEquals("50/9","123456789", doc.payerCPP);

    String[] aTag10 = new String[]{":50:Payer nameINN1234567890/KPP123456789 continue"}; // eats inner space
    parser.readCounterparty(aTag10, doc, "50");
    assertEquals("50/10","Payer namecontinue", doc.payerName);
    assertEquals("50/10","1234567890", doc.payerINN);
    assertEquals("50/10","123456789", doc.payerCPP);

    String[] aTag11 = new String[]{":50:INN1234567890/KPP123456789 Payer name"};
    parser.readCounterparty(aTag11, doc, "50");
    assertEquals("50/11","Payer name", doc.payerName);
    assertEquals("50/11","1234567890", doc.payerINN);
    assertEquals("50/11","123456789", doc.payerCPP);

    String[] aTag12 = new String[]{":50:INN1234567890/KPP123456789Payer name"};
    parser.readCounterparty(aTag12, doc, "50");
    assertEquals("50/12","Payer name", doc.payerName);
    assertEquals("50/12","1234567890", doc.payerINN);
    assertEquals("50/11","123456789", doc.payerCPP);
  }

  /** Test for counterparty parsing win INN/KPP
   *
   */
  @Test
  public void testReadBank() {
    FDocument doc = new FDocument();
    MT103Parser parser = new MT103Parser();

    String[] aTag0 = new String[]{":57D:/30101810200000000700","BIK044525700", "Bank_name", "+Bank_city"};
    parser.readBank(aTag0, doc, "57D");
    assertEquals("57/0", "044525700", doc.payeeBankBIC);
    assertEquals("57/0", "30101810200000000700", doc.payeeBankAccount);
    assertEquals("57/0", "Bank_name+Bank_city", doc.payeeBankName);

    String[] aTag1 = new String[]{":57D:BIK044525700", "/30101810200000000700", "Bank_name", "+Bank_city"};
    parser.readBank(aTag1, doc, "57D");
    assertEquals("57/1", "044525700", doc.payeeBankBIC);
    assertEquals("57/1", "30101810200000000700", doc.payeeBankAccount);
    assertEquals("57/0", "Bank_name+Bank_city", doc.payeeBankName);
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
          BQParser parser = new BQParser();
          FDocument doc = parser.fromXML(edChildNode);
          if (doc != null) {
            assertFalse(doc.isUrgent);

            assertEquals("111", doc.docNum);
            assertEquals("2020-05-15", doc.docDate);
            assertEquals(5270L, (long) doc.amount);
            assertEquals("Payment purpose", doc.purpose);
            assertEquals("3", doc.priority);
            assertEquals("2020-05-14", doc.chargeOffDate);
            assertEquals("2020-05-13", doc.receiptDate);
            assertEquals("01", doc.transKind);

            assertEquals("Payer name", doc.payerName);
            assertEquals("40702810300000000300", doc.payerAccount);
            assertEquals("2342018036", doc.payerINN);
            assertEquals("234201001", doc.payerCPP);
            assertEquals("044525101", doc.payerBankBIC);
            assertEquals("30101810100000000101", doc.payerBankAccount);

            assertEquals("Payee name", doc.payeeName);
            assertEquals("40101810300000010013", doc.payeeAccount);
            assertEquals("2308014320", doc.payeeINN);
            assertEquals("231001001", doc.payeeCPP);
            assertEquals("040349001", doc.payeeBankBIC);
            assertEquals("30101810200000000001", doc.payeeBankAccount);

            assertTrue(doc.isTax);
            assertEquals("14", doc.taxStatus); // 101
            assertEquals("39210202020062000160", doc.CBC); // 104
            assertEquals("03233556000", doc.OCATO); // 105
            assertEquals("ТП", doc.taxPaytReason); // 106
            assertEquals("МС.12.2011", doc.taxPeriod); // 107
            assertEquals("20", doc.taxDocNum); // 108
            assertEquals("01.02.2020", doc.taxDocDate); // 109
            assertEquals("1", doc.taxPaytKind); // 110
          } else
            fail("BQ XML parse failed.");
        }
      }
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      fail("Preliminary XML parsing failed.");
    }
  }

  /** Function parses any client static information in BQ format
   *
   */
  @Test
  public void testBQClientParse() {
    String xmlPerson = "<?xml version=\"1.0\" encoding=\"windows-1251\" ?>" + System.lineSeparator() +
            "<person action=\"update\" id=\"4260\" last-date=\"2020-01-29\">" + System.lineSeparator() +
            "<reg client=\"Y\" name-first=\"Ivan\" name-last=\"Dorohoff\" name-mid=\"Petrovitch\" resident=\"Y\"/>" + System.lineSeparator() +
            "<misc birthday=\"1983-06-18\" gender=\"M\" relation=\"0\"/>" + System.lineSeparator() +
            "<idents>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"776521543603\" type=\"ИНН\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"32000837\" type=\"УНК\"/>" + System.lineSeparator() +
            "</idents>" + System.lineSeparator() +
            "</person>";
    String xmlSelfEmp = "<?xml version=\"1.0\" encoding=\"windows-1251\" ?>" + System.lineSeparator() +
            "<cust-priv action=\"update\" id=\"4268\" last-date=\"2020-01-29\">" + System.lineSeparator() +
            "<reg client=\"Y\" name=\"\" name-first=\"Jane\" name-last=\"Curve\" name-mid=\"Arcady\" resident=\"Y\"/>" + System.lineSeparator() +
            "<misc birthday=\"1985-12-25\" gender=\"F\" relation=\"0\"/>" + System.lineSeparator() +
            "<idents>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"770565351400\" type=\"ИНН\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"32000847\" type=\"УНК\"/>" + System.lineSeparator() +
            "</idents>" + System.lineSeparator() +
            "</cust-priv>";
    String xmlCompany = "<?xml version=\"1.0\" encoding=\"windows-1251\" ?>" + System.lineSeparator() +
            "<cust-corp action=\"update\" id=\"9464\" last-date=\"2020-01-30\">" + System.lineSeparator() +
            "<reg client=\"Y\" name=\"Lirus ltd\" name-short=\"Lirus\" resident=\"Y\"/>" + System.lineSeparator() +
            "<misc financial=\"N\" issuer=\"N\" mat-company=\"N\" relation=\"0\" state-ctl=\"N\"/>" + System.lineSeparator() +
            "<idents>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"7705625484\" type=\"ИНН\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"1157705235467\" type=\"ОГРН\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"770522235\" type=\"КПП\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"32000852\" type=\"УНК\"/>" + System.lineSeparator() +
            "</idents>" + System.lineSeparator() +
            "</cust-corp>";
    String xmlBank = "<?xml version=\"1.0\" encoding=\"windows-1251\" ?>" + System.lineSeparator() +
            "<bank action=\"update\" id=\"43786\" last-date=\"2020-04-29\">" + System.lineSeparator() +
            "<reg client=\"Y\" engl-name=\"CREDIT SUISSE ZURICH\" name=\"CREDIT SUISSE ZURICH\" name-short=\"CREDIT SUISSE\" resident=\"N\"/>" + System.lineSeparator() +
            "<misc is-rkc=\"N\" issuer=\"N\" mat-company=\"N\" relation=\"0\" state-ctl=\"N\" town=\"ZURICH\" town-type=\"\"/>" + System.lineSeparator() +
            "<idents>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"12345\" type=\"ИНН\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"\" type=\"ОГРН\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"\" type=\"КПП\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"BSUIFRPPXXX\" type=\"BIC\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"044525225\" type=\"МФО-9\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"30101810100000000225\" type=\"РКЦ_СЧЕТ\"/>" + System.lineSeparator() +
            "<ident action=\"update\" class=\"Код\" number=\"0005002415\" type=\"УНК\"/>" + System.lineSeparator() +
            "</idents>" + System.lineSeparator() +
            "</bank>";

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xmlPerson));
      Document xmlDoc = builder.parse(is);
      Node root = xmlDoc.getDocumentElement();
      BQParser parser = new BQParser();
      Client clt = parser.clientFromXML(root);
      if (clt != null) {
        assertEquals(ClientType.PERSON, clt.type);
        assertEquals((long) 42601, (long) clt.id);
        assertEquals("Dorohoff", clt.lastName);
        assertEquals("Ivan Petrovitch", clt.firstNames);
        assertEquals("776521543603", clt.INN);
      }

      is = new InputSource(new StringReader(xmlSelfEmp));
      xmlDoc = builder.parse(is);
      root = xmlDoc.getDocumentElement();
      clt = parser.clientFromXML(root);
      if (clt != null) {
        assertEquals(ClientType.SELF_EMPLOYED, clt.type);
        assertEquals((long) 42682, (long) clt.id);
        assertEquals("Curve", clt.lastName);
        assertEquals("Jane Arcady", clt.firstNames);
        assertEquals("770565351400", clt.INN);
      }

      is = new InputSource(new StringReader(xmlCompany));
      xmlDoc = builder.parse(is);
      root = xmlDoc.getDocumentElement();
      clt = parser.clientFromXML(root);
      if (clt != null) {
        assertEquals(ClientType.COMPANY, clt.type);
        assertEquals((long) 94643, (long) clt.id);
        assertEquals("Lirus ltd", clt.officialName);
        assertEquals("7705625484", clt.INN);
      }

      is = new InputSource(new StringReader(xmlBank));
      xmlDoc = builder.parse(is);
      root = xmlDoc.getDocumentElement();
      clt = parser.clientFromXML(root);
      if (clt != null) {
        assertEquals(ClientType.BANK, clt.type);
        assertEquals((long) 437864, (long) clt.id);
        assertEquals("CREDIT SUISSE ZURICH", clt.officialName);
        assertEquals("12345", clt.INN);
        assertEquals("044525225", clt.bankBIC);
        assertEquals("30101810100000000225", clt.bankCorrAccount);
      }

    }
    catch(ParserConfigurationException | SAXException | IOException e) {
      fail("Preliminary XML parsing failed.");
    }
  }

  /** Function parses account static information in BQ format
   *
   */
  @Test
  public void testBQAccountParse() {

    String xmlAccount= "<?xml version=\"1.0\" encoding=\"windows-1251\" ?>" + System.lineSeparator() +
            "<acct action=\"update\" id=\"40817810800000000009,\" last-date=\"2020-01-29\">" + System.lineSeparator() +
            "<reg acct=\"40817810800000000009\" bal-acct=\"40817\" branch-id=\"0001\" category=\"А\" currency=\"810\" open-date=\"2020-01-01\" close-date=\"2120-01-01\" side=\"П\"/>" + System.lineSeparator() +
            "<business details=\"Dorohoff Ivan Petrovitch\" is_except=\"N\" nonreduct-saldo=\"0.00\"/>" + System.lineSeparator() +
            "<client cust-cat=\"Ч\" cust-id=\"4260\" internal=\"N\"/>" + System.lineSeparator() +
            "</acct>";

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xmlAccount));
      Document xmlDoc = builder.parse(is);
      Node root = xmlDoc.getDocumentElement();
      BQParser parser = new BQParser();
      Account acc = parser.accountFromXML(root);
      if (acc != null) {
        assertEquals("40817810800000000009", acc.account);
        assertEquals("Dorohoff Ivan Petrovitch", acc.details);
        assertEquals(42601L, (long) acc.clientId);
        assertEquals(ClientType.PERSON, acc.clientType);
        assertEquals("2020-01-01", acc.openDate);
        assertEquals("2120-01-01", acc.closeDate);
        assertFalse(acc.isInternal);
      }
    }
    catch(ParserConfigurationException | SAXException | IOException e) {
      fail("Preliminary XML parsing failed.");
    }
  }

  /** Test one XML node parsing with ED807
   *
   */
  @Test
  public void testED807Parse() {
    String xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<ed:ED807 xmlns:ed=\"urn:cbr-ru:ed:v2.0\" EDNo=\"700000053\" EDDate=\"2018-07-05\" EDAuthor=\"4583001999\" " +
                    "    CreationReason=\"SOBD\" CreationDateTime=\"2018-07-04T20:08:51Z\" InfoTypeCode=\"FIRR\">\n" +
                    "  <ed:BICDirectoryEntry BIC=\"044536002\">" +
                    "    <ed:ParticipantInfo NameP=\"ФИНАНСОВЫЙ Д-Т БАНКА РОССИИ\" Rgn=\"45\" Ind=\"107016\" Tnp=\"г.\" Nnp=\"Москва 701\" " +
                    "         Adr=\"ул Неглинная, 12\" PrntBIC=\"044537002\" DateIn=\"1994-01-20\" PtType=\"15\" Srvcs=\"3\" XchType=\"1\" " +
                    "         UID=\"4536002000\" NPSParticipant=\"1\" ParticipantStatus=\"PSAC\">" +
                    "    </ed:ParticipantInfo>" +
                    "  </ed:BICDirectoryEntry>" +
                    "</ed:ED807>";

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xmlString));
      Document xmlDoc = builder.parse(is);
      Node root = xmlDoc.getDocumentElement();
      ED807Parser parser = new ED807Parser();
      Bank bank = null;
      String rootNodeName = Helper.getSimpleNodeName(root);
      if (rootNodeName.equals("ED807")) {
        NodeList eds = root.getChildNodes();
        for (int i = 0; i < eds.getLength(); i++) {
          Node nodeBank = eds.item(i);
          if (nodeBank.getNodeType() != Node.TEXT_NODE) {
            String nodeName = Helper.getSimpleNodeName(nodeBank);
            if (nodeName.equals("BICDirectoryEntry")) {
              bank = parser.bankFromXML(nodeBank);
            }
          }
        }
      }
      if (bank != null) {
        assertEquals("ФИНАНСОВЫЙ Д-Т БАНКА РОССИИ", bank.name);
        assertEquals("г. Москва 701", bank.town);
      }
      else
        fail("Could not parse ED807.");
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      fail("Preliminary XML parsing failed.");
    }
  }

  /** Test for decimal string to long conversion
   *
   */
  @Test
  public void getLongFromDecimalTest() {
    assertEquals(0L, (long) Helper.getLongFromDecimal("0.00"));
    assertEquals(0L, (long) Helper.getLongFromDecimal("0,00"));
    assertEquals(0L, (long) Helper.getLongFromDecimal("0,0"));
    assertEquals(50L, (long) Helper.getLongFromDecimal("0.5"));
    assertEquals(50L, (long) Helper.getLongFromDecimal("0.50"));
  }

  /** Test for file type automatic determine function
   *
   */
  @Test
  public void getFileTypeTest() {
    String patternPath = ".\\src\\test\\resources\\pattern\\";
    ProcessorFabric fab = new ProcessorFabric();
    assertEquals("FT14", FileType.FT14, fab.fileType(patternPath + "ft14test.txt"));
    assertEquals("MT103", FileType.MT103, fab.fileType(patternPath + "mt103test.txt"));
    assertEquals("MT100", FileType.MT100, fab.fileType(patternPath + "mt100test.dat"));
    assertEquals("UFEBS single", FileType.UFEBS, fab.fileType(patternPath + "single.xml"));
    assertEquals("UFEBS packet", FileType.UFEBS, fab.fileType(patternPath + "packet.xml"));
    assertEquals("BQ", FileType.BQ, fab.fileType(patternPath + "bqtest.xml"));
    assertEquals("?", FileType.UNKNOWN, fab.fileType(patternPath + "nofile.xml"));
    assertEquals("?txt", FileType.UNKNOWN, fab.fileType(patternPath + "unknown.txt"));
    assertEquals("?xml", FileType.UNKNOWN, fab.fileType(patternPath + "unknown.xml"));
  }

  @Test
  public void buildPurposeTest() {
    FDocument doc = new FDocument();
    doc.purpose = "{VO12345}Purpose";
    doc.UIN = "1234567890";
    String target = doc.buildPurpose(true);
    assertEquals("((VO12345))PurposeУИН1234567890///", target);

    doc.purpose = "Purpose";
    doc.UIN = "";
    target = doc.buildPurpose(true);
    assertEquals("Purpose", target);
  }

  @Test
  public void parsePurposeTest() {
    FDocument doc = new FDocument();

    String source = "((VO12345))PurposeУИН1234567890///";
    doc.parsePurpose(source);
    assertEquals("((VO12345))Purpose", doc.purpose);
    assertEquals("1234567890", doc.UIN);
  }

  /** Test for UFEBS processor - parse, create ED1xx, create reverse documents, create confirmations and statement
   *
   */
  @Test
  public void UFEBSTest() {

    patternDocs.docs.clear(); sampleDocs.docs.clear();

    UFEBSProcessor procUFEBS = new UFEBSProcessor();
    assertTrue("UFEBS single file parse error.", procUFEBS.readFile(patternPath + "single.xml", patternDocs));
    assertTrue("UFEBS packet file parse error.", procUFEBS.readFile(patternPath + "packet.xml", patternDocs));

    // UFEBS assembler test
    for (Map.Entry<Long, FDocument> item : patternDocs.docs.entrySet()) { // Build array with non-urgent documents only (to create one packet)
      FDocument doc = item.getValue();
      if (!doc.isUrgent) packet.add(doc);
    }
    procUFEBS.createAll(outPath, packet);
    assertTrue("UFEBS packet XSD check error.", procUFEBS.isXMLValid(outPath + "pko1000000.xml", xsdPath));
    assertTrue("UFEBS created packet file parse error.", procUFEBS.readFile(outPath + "pko1000000.xml", sampleDocs));
    sampleDocs.docs.clear();

    reverseDocs = patternDocs.createReverse();
    procUFEBS.createAll(outPath, reverseDocs);
    compareTwoFiles(patternPath + "packet-income.xml", outPath + "pki1000000.xml");
    compareTwoFiles(patternPath + "single-income.xml", outPath + "inc0000143.xml");

    procUFEBS.createConfirmations(outPath, patternDocs);
    compareTwoFiles(patternPath + "packet-conf.xml", outPath + "ppo2000000.xml");
    compareTwoFiles(patternPath + "single-conf.xml", outPath + "pco0000143.xml");
    procUFEBS.createStatement(outPath, patternDocs, reverseDocs);
    compareTwoFiles(patternPath + "statement.xml", outPath + "stm3000000.xml");
  }

  /** MT103 processor test
   *
   */
  @Test
  public void MT103Test() {

    patternDocs.docs.clear(); sampleDocs.docs.clear();

    MT103Processor procMT103 = new MT103Processor();

    String inMT103File = patternPath + "mt103test.txt";
    assertTrue("MT103 file parse error.", procMT103.readFile(inMT103File, patternDocs));

    String outMT103File = outPath + "mt103test.txt";
    if (!deleteOutFile(outMT103File)) fail("MT103 file delete error: " + outMT103File);
    procMT103.createAll(outPath, patternDocs);
    assertTrue("Output file MT103 not found" + outMT103File, Files.isRegularFile(Paths.get(outMT103File)));

    procMT103.readFile(outMT103File, sampleDocs);

    Comparator comparator = new Comparator();
    assertTrue("Comparator error found when testing MT103 documents.", comparator.compare(patternDocs, sampleDocs));

    compareTwoFiles(inMT103File, outMT103File);
  }

  /** BQ documents parser test
   *
   */
  @Test
  public void BQTest() {

    patternDocs.docs.clear(); sampleDocs.docs.clear();

    Comparator comparator = new Comparator();

    BQProcessor procBQ = new BQProcessor();

    String inBQFile = patternPath + "bqtest.xml";
    procBQ.readFile(inBQFile, patternDocs);

    procBQ.createAll(outPath, patternDocs);
    String outBQFile = outPath + "bqtest.xml";
    procBQ.readFile(outBQFile, sampleDocs);
    assertTrue("Comparator error found when testing BQ documents.", comparator.compare(patternDocs, sampleDocs));

    compareTwoFiles(inBQFile, outBQFile);
  }

  /** Integrity test - test functionality at all
   *
   */
  @Test
  public void futureTest() {
    // TODO: MT100 loop test
    // TODO: MT940 loop test

    // MT100 parser test
    patternDocs.docs.clear();
    MT100Processor procMT100 = new MT100Processor();
    procMT100.readFile(patternPath + "mt100test.dat", patternDocs);

    // MT940 parser test
    patternDocs.docs.clear();
    MT940Processor procMT940 = new MT940Processor();
    procMT940.readFile(patternPath + "mt940test.txt", patternDocs);

  }

  /** Parse FT14, assemble FT14 and compare results
   *
   */
  @Test
  public void FT14Test() {

    patternDocs.docs.clear(); sampleDocs.docs.clear();

    ProcessorFabric fab = new ProcessorFabric();
    if (staticPath != null) {
      BQProcessor bqProc = (BQProcessor) fab.getProcessor(FileType.BQ);
      if (bqProc != null) {
        bqProc.readAllStatic(staticPath);
      }
    }

    if (banksPath != null) {
      ED807Processor ed807Proc = (ED807Processor) fab.getProcessor(FileType.ED807);
      if (ed807Proc != null) {
        ed807Proc.readAll(banksPath);
      }
    }

    FT14Processor procFT14 = new FT14Processor();
    String inFT14File = patternPath + "ft14test.txt";
    procFT14.readFile(inFT14File, patternDocs);

    String outFT14File = outPath + "ft14test.txt"; // First of all - delete previous test results
    if (!deleteOutFile(outFT14File)) fail("FT14 file delete error: " + outFT14File);
    procFT14.createAll(outPath, patternDocs);
    assertTrue("Output file FT14 not found" + outFT14File, Files.isRegularFile(Paths.get(outFT14File)));

    procFT14.readFile(outFT14File, sampleDocs);

    Comparator comparator = new Comparator();
    assertTrue(comparator.compare(patternDocs, sampleDocs));

    compareTwoFiles(patternPath + "ft14test.txt", outPath + "ft14test.txt");

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
