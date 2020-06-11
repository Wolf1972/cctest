package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class UFEBSParser {
  /** Loads object from XML node
   *
   * @param node - XML node with one document (ED1xx) or packet (PacketED)
   * @return  FDocument - financial document
   */
  static FDocument fromXML(Node node) {

    FDocument doc = new FDocument();

    if (node.getNodeType() != Node.TEXT_NODE) {

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("EDNo");
      if (nestedNode != null) doc.edNo = nestedNode.getNodeValue();
      nestedNode = attr.getNamedItem("EDDate");
      if (nestedNode != null) doc.edDate = nestedNode.getNodeValue();

      nestedNode = attr.getNamedItem("Sum");
      if (nestedNode != null) doc.amount = Long.parseLong(nestedNode.getNodeValue());
      nestedNode = attr.getNamedItem("PaymentID");
      if (nestedNode != null) doc.UIN = nestedNode.getNodeValue();

      nestedNode = attr.getNamedItem("SystemCode");
      if (nestedNode != null) doc.isUrgent = nestedNode.getNodeValue().equals("05");

      nestedNode = attr.getNamedItem("Priority");
      if (nestedNode != null) doc.priority = nestedNode.getNodeValue();
      nestedNode = attr.getNamedItem("ChargeOffDate");
      if (nestedNode != null) doc.chargeOffDate = nestedNode.getNodeValue();
      nestedNode = attr.getNamedItem("ReceiptDate");
      if (nestedNode != null) doc.receiptDate = nestedNode.getNodeValue();
      nestedNode = attr.getNamedItem("TransKind");
      if (nestedNode != null) doc.transKind = nestedNode.getNodeValue();

      NodeList edOne = node.getChildNodes(); // List of child nodes for ED1xx

      for (int i = 0; i < edOne.getLength(); i++) {

        Node edChildNode = edOne.item(i);
        String nodeName = edChildNode.getNodeName();

        if (nodeName.equals("AccDoc")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("AccDocNo");
          if (nestedNode != null) doc.docNum = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("AccDocDate");
          if (nestedNode != null) doc.docDate = nestedNode.getNodeValue();
          if (doc.chargeOffDate == null) doc.chargeOffDate = doc.docDate;
          if (doc.receiptDate == null) doc.receiptDate = doc.docDate;
        }

        else if (nodeName.equals("Payer")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("INN");
          if (nestedNode != null) doc.payerINN = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("KPP");
          if (nestedNode != null) doc.payerCPP = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("PersonalAcc");
          if (nestedNode != null) doc.payerAccount = nestedNode.getNodeValue();

          NodeList nestedNodes = edChildNode.getChildNodes();
          for (int j = 0; j < nestedNodes.getLength(); j++) {
            nestedNode = nestedNodes.item(j);
            String nestedNodeName = nestedNode.getNodeName();
            if (nestedNodeName.equals("Name")) {
              doc.payerName = nestedNode.getTextContent();
            }
            else if (nestedNodeName.equals("Bank")) {
              attr = nestedNode.getAttributes();
              nestedNode = attr.getNamedItem("BIC");
              if (nestedNode != null) doc.payerBankBIC = nestedNode.getNodeValue();
              nestedNode = attr.getNamedItem("CorrespAcc");
              if (nestedNode != null) doc.payerBankAccount = nestedNode.getNodeValue();
            }
          }
        }

        else if (nodeName.equals("Payee")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("INN");
          if (nestedNode != null) doc.payeeINN = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("KPP");
          if (nestedNode != null) doc.payeeCPP = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("PersonalAcc");
          if (nestedNode != null) doc.payeeAccount = nestedNode.getNodeValue();

          NodeList nestedNodes = edChildNode.getChildNodes();
          for (int j = 0; j < nestedNodes.getLength(); j++) {
            nestedNode = nestedNodes.item(j);
            String nestedNodeName = nestedNode.getNodeName();
            if (nestedNodeName.equals("Name")) {
              doc.payeeName = nestedNode.getTextContent();
            }
            else if (nestedNodeName.equals("Bank")) {
              attr = nestedNode.getAttributes();
              nestedNode = attr.getNamedItem("BIC");
              if (nestedNode != null) doc.payeeBankBIC = nestedNode.getNodeValue();
              nestedNode = attr.getNamedItem("CorrespAcc");
              if (nestedNode != null) doc.payeeBankAccount = nestedNode.getNodeValue();
            }
          }
        }

        else if (nodeName.equals("Purpose")) {
          doc.purpose = edChildNode.getTextContent();
        }

        else if (nodeName.equals("DepartmentalInfo")) {
          attr = edChildNode.getAttributes();
          if (attr.getLength() > 0) {
            doc.isTax = true;
            nestedNode = attr.getNamedItem("DrawerStatus");
            if (nestedNode != null) doc.taxStatus = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("CBC");
            if (nestedNode != null) doc.CBC = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("OKATO");
            if (nestedNode != null) doc.OCATO = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("PaytReason");
            if (nestedNode != null) doc.taxPaytReason = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("TaxPeriod");
            if (nestedNode != null) doc.taxPeriod = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("DocNo");
            if (nestedNode != null) doc.taxDocNum = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("DocDate");
            if (nestedNode != null) doc.taxDocDate = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("TaxPaytKind");
            if (nestedNode != null) doc.taxPaytKind = nestedNode.getNodeValue();
          }
        }
      }
    }
    else {
      return null;
    }
    return doc;
  }

  /** Returns string with one ED1xx
   *
   * @param doc - document
   * @return string with XML node ED1xx
   */
  static String toString(FDocument doc) {
    StringBuilder str = new StringBuilder();

    String rootNodeName; // UFEBS message type
    if (doc.transKind.equals("02")) rootNodeName = "ED103";
    else if (doc.transKind.equals("06")) rootNodeName = "ED104";
    else if (doc.transKind.equals("16")) rootNodeName = "ED105";
    else rootNodeName = "ED101";
    str.append("<");
    str.append(rootNodeName);

    if (doc.payerBankBIC.equals("044525101")) str.append(" EDAuthor=\"4525101000\" EDReceiver=\"4525225000\"");
    else str.append(" EDAuthor=\"4525225000\" EDReceiver=\"4525101000\"");

    str.append(" EDDate=\""); str.append(doc.edDate); str.append("\"");
    str.append(" EDNo=\""); str.append(doc.edNo); str.append("\"");
    if (doc.UIN != null) { str.append(" PaymentID=\""); str.append(doc.UIN); str.append("\""); }
    str.append(" PaymentPrecedence=\""); if (doc.isUrgent) str.append("60"); else str.append("70"); str.append("\"");
    if (doc.priority != null) { str.append(" Priority=\""); str.append(doc.priority); str.append("\""); }
    if (doc.chargeOffDate != null) { str.append(" ChargeOffDate=\""); str.append(doc.chargeOffDate); str.append("\""); }
    if (doc.receiptDate != null) { str.append(" ReceiptDate=\""); str.append(doc.receiptDate); str.append("\""); }
    str.append(" Sum=\""); str.append(doc.amount); str.append("\"");
    str.append(" SystemCode=\""); if (doc.isUrgent) str.append("05"); else str.append("02"); str.append("\"");
    str.append(" TransKind=\""); str.append(doc.transKind); str.append("\"");
    if (doc.isUrgent) { str.append(" PaytKind=\"4\""); }
    str.append(" xmlns=\"urn:cbr-ru:ed:v2.0\">");

    str.append("<AccDoc");
    str.append(" AccDocDate=\""); str.append(doc.docDate); str.append("\"");
    str.append(" AccDocNo=\""); str.append(doc.docNum); str.append("\"");
    str.append(" />");

    str.append("<Payer");
    if (doc.payerAccount != null) { str.append(" PersonalAcc=\""); str.append(doc.payerAccount); str.append("\""); }
    if (doc.payerCPP != null) { str.append(" KPP=\""); str.append(doc.payerCPP); str.append("\""); }
    if (doc.payerINN != null) { str.append(" INN=\""); str.append(doc.payerINN); str.append("\""); }
    str.append(">");
    str.append("<Name>"); str.append(doc.payerName); str.append("</Name>");
    str.append("<Bank");
    if (doc.payerBankAccount != null) { str.append(" CorrespAcc=\""); str.append(doc.payerBankAccount); str.append("\""); }
    str.append(" BIC=\""); str.append(doc.payerBankBIC); str.append("\"");
    str.append("/>");
    str.append("</Payer>");

    str.append("<Payee");
    if (doc.payeeAccount != null) { str.append(" PersonalAcc=\""); str.append(doc.payeeAccount); str.append("\""); }
    if (doc.payeeCPP != null) { str.append(" KPP=\""); str.append(doc.payeeCPP); str.append("\""); }
    if (doc.payeeINN != null) { str.append(" INN=\""); str.append(doc.payeeINN); str.append("\""); }
    str.append(">");
    str.append("<Name>"); str.append(doc.payeeName); str.append("</Name>");
    str.append("<Bank");
    if (doc.payeeBankAccount != null) { str.append(" CorrespAcc=\""); str.append(doc.payeeBankAccount); str.append("\""); }
    str.append(" BIC=\""); str.append(doc.payeeBankBIC); str.append("\"");
    str.append("/>");
    str.append("</Payee>");

    str.append("<Purpose>"); str.append(doc.purpose); str.append("</Purpose>");

    if (doc.isTax) {
      str.append("<DepartmentalInfo ");
      if (doc.taxStatus != null) { str.append(" DrawerStatus=\""); str.append(doc.taxStatus); str.append("\""); } // 101
      if (doc.CBC != null) { str.append(" CBC=\""); str.append(doc.CBC); str.append("\""); } // 104
      if (doc.OCATO != null) { str.append(" OKATO=\""); str.append(doc.OCATO); str.append("\""); } // 105
      if (doc.taxPaytReason != null) { str.append(" PaytReason=\""); str.append(doc.taxPaytReason); str.append("\"");} // 106
      if (doc.taxPeriod != null) { str.append(" TaxPeriod=\""); str.append(doc.taxPeriod); str.append("\"");} // 107
      if (doc.taxDocNum != null) { str.append(" DocNo=\""); str.append(doc.taxDocNum); str.append("\"");} // 108
      if (doc.taxDocDate != null) { str.append(" DocDate=\""); str.append(doc.taxDocDate); str.append("\"");} // 109
      if (doc.taxPaytKind != null) { str.append(" PaytKind=\""); str.append(doc.taxPaytKind); str.append("\"");} // 110
      str.append(" />");
    }

    str.append("</"); str.append(rootNodeName); str.append(">"); str.append(System.lineSeparator());

    return str.toString();
  }

  /** Returns string with one confirmation (ED206)
   *
   * @param doc - document
   * @return string with XML node ED206
   */
  static String toConfirmation(FDocument doc) {
    StringBuilder str = new StringBuilder();
    /*
    <ED206 EDNo="280001" EDDate="2015-09-15" EDAuthor="1112223334" EDReceiver="4525101000" Acc="30101810100000000101" BICCorr="044583001" Sum="10000" TransDate="2015-09-15" TransTime="12:10:01" CorrAcc="" DC="1">
       <AccDoc AccDocNo="100" AccDocDate="2015-09-15" />
       <EDRefID EDNo="480001" EDDate="2015-09-15" EDAuthor="4525101000" />
    </ED206>
     */
    boolean isReverse = !doc.payerBankBIC.equals("044525101");

    str.append("<ED206");
    str.append(" EDNo=\""); str.append(doc.generateEDNo(doc.edNo, "2")); str.append("\"");
    str.append(" EDDate=\""); str.append(doc.edDate); str.append("\"");
    if (!isReverse) {
      str.append(" EDAuthor=\""); str.append("4525101000"); str.append("\"");
      str.append(" EDReceiver=\""); str.append("4525225000"); str.append("\"");
      str.append(" BICCorr=\""); str.append(doc.payeeBankBIC); str.append("\"");
      str.append(" DC=\"2\"");
    }
    else {
      str.append(" EDAuthor=\""); str.append("4525225000"); str.append("\"");
      str.append(" EDReceiver=\""); str.append("4525101000"); str.append("\"");
      str.append(" BICCorr=\""); str.append(doc.payerBankBIC); str.append("\"");
      str.append(" DC=\"1\"");
    }
    str.append(" Acc=\"30101810100000000101\"");
    str.append(" Sum=\""); str.append(doc.amount.toString()); str.append("\"");
    str.append(" TransDate=\""); str.append(doc.docDate); str.append("\"");
    str.append(" TransTime=\"10:00:01\"");
    str.append(">");

    str.append("<AccDoc");
    str.append(" AccDocNo=\""); str.append(doc.docNum); str.append("\"");
    str.append(" AccDocDate=\""); str.append(doc.docDate); str.append("\"");
    str.append(" />");

    str.append("<EDRefID");
    str.append(" EDNo=\""); str.append(doc.edNo); str.append("\"");
    str.append(" EDDate=\""); str.append(doc.edDate); str.append("\"");
    if (!isReverse) {
      str.append(" EDAuthor=\"4525101000\"");
    }
    else {
      str.append(" EDAuthor=\"4525225000\"");
    }
    str.append(" />");

    str.append("</ED206>");
    return str.toString();
  }

}
