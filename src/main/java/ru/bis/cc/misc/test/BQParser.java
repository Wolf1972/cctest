package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class BQParser extends XMLParser {
  /** Loads object from XML node
   *
   * @param node - XML node with one document doc
   * @return  FDocument - financial document
   */
  static FDocument fromXML(Node node) {

    FDocument doc = new FDocument();

    if (node.getNodeType() != Node.TEXT_NODE) {

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("id");
      if (nestedNode != null) doc.referenceBq = nestedNode.getNodeValue();

      NodeList edOne = node.getChildNodes(); // List of child nodes for <doc>

      for (int i = 0; i < edOne.getLength(); i++) {

        Node edChildNode = edOne.item(i);
        String nodeName = edChildNode.getNodeName();

        if (nodeName.equals("reg")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("doc-type");
          if (nestedNode != null) doc.transKind = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("doc-num");
          if (nestedNode != null) doc.docNum = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("doc-date");
          if (nestedNode != null) doc.docDate = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("order-pay");
          if (nestedNode != null) doc.priority = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("chgoff-date");
          if (nestedNode != null) doc.chargeOffDate = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("payee-received");
          if (nestedNode != null) doc.receiptDate = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("delivery");
          if (nestedNode != null) doc.isUrgent = nestedNode.getNodeValue().equals("Э");
        }

        else if (nodeName.equals("payer")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("name");
          if (nestedNode != null) doc.payerName = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("inn");
          if (nestedNode != null) doc.payerINN = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("kpp");
          if (nestedNode != null) doc.payerCPP = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("acct");
          if (nestedNode != null) doc.payerAccount = nestedNode.getNodeValue();
        }

        else if (nodeName.equals("payer-bank")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("name");
          if (nestedNode != null) doc.payerBankName = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("ident-code");
          if (nestedNode != null) doc.payerBankBIC = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("acct");
          if (nestedNode != null) doc.payerBankAccount = nestedNode.getNodeValue();
        }

        else if (nodeName.equals("payee")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("name");
          if (nestedNode != null) doc.payeeName = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("inn");
          if (nestedNode != null) doc.payeeINN = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("kpp");
          if (nestedNode != null) doc.payeeCPP = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("acct");
          if (nestedNode != null) doc.payeeAccount = nestedNode.getNodeValue();
        }

        else if (nodeName.equals("payee-bank")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("name");
          if (nestedNode != null) doc.payeeBankName = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("ident-code");
          if (nestedNode != null) doc.payeeBankBIC = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("acct");
          if (nestedNode != null) doc.payeeBankAccount = nestedNode.getNodeValue();
        }

        else if (nodeName.equals("details")) {
          doc.purpose = edChildNode.getTextContent();
        }

        else if (nodeName.equals("entries")) {
          NodeList edEntries = edChildNode.getChildNodes(); // List of child nodes for <doc>
          for (int j = 0; j < edEntries.getLength(); j++) {
            Node edEntry = edEntries.item(j);
            if (edEntry.getNodeName().equals("entry")) {
              attr = edEntry.getAttributes();
              nestedNode = attr.getNamedItem("amt-rub");
              if (nestedNode != null) {
                String amtFloat = nestedNode.getNodeValue();
                if (amtFloat.indexOf(".") == 0) amtFloat += "00";
                else if (amtFloat.substring(amtFloat.indexOf(".") + 1).length() == 1) amtFloat += "0"; // 123.5 = 123.50
                doc.amount = Long.parseLong(amtFloat.replace(".", ""));
              }
              nestedNode = attr.getNamedItem("op-date");
              if (nestedNode != null) doc.edDate = nestedNode.getNodeValue();
              break; // Takes only one entry
            }
          }
        }

        else if (nodeName.equals("tax-index")) {
          attr = edChildNode.getAttributes();
          if (attr.getLength() > 0) {
            doc.isTax = true;
            nestedNode = attr.getNamedItem("status-index");
            if (nestedNode != null) doc.taxStatus = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("cbc");
            if (nestedNode != null) doc.CBC = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("ocato");
            if (nestedNode != null) doc.OCATO = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("purpose-index");
            if (nestedNode != null) doc.taxPaytReason = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("period-index");
            if (nestedNode != null) doc.taxPeriod = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("num-index");
            if (nestedNode != null) doc.taxDocNum = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("date-index");
            if (nestedNode != null) doc.taxDocDate = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("type-index");
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

  /** Returns string with one <doc> element
   *
   * @param doc - document
   * @return string with XML node <doc>
   */
  static String toString(FDocument doc) {

    StringBuilder str = new StringBuilder();

    str.append("<doc");
    str.append(" id=\""); str.append(doc.edNo != null? doc.edNo : doc.getId()); str.append("\"");
    str.append(" action=\"update\" >");

    str.append("<reg");
    str.append(" doc-type=\""); str.append(doc.transKind); str.append("\"");
    str.append(" doc-num=\""); str.append(doc.docNum); str.append("\"");
    str.append(" doc-date=\""); str.append(doc.docDate); str.append("\"");
    str.append(" order-pay=\""); str.append(doc.priority); str.append("\"");
    if (doc.chargeOffDate != null) { str.append(" chgoff-date=\""); str.append(doc.chargeOffDate); str.append("\""); }
    if (doc.receiptDate != null) { str.append(" payee-received=\""); str.append(doc.receiptDate); str.append("\""); }
    str.append(" delivery=\""); str.append(doc.isUrgent? "Э" : "E"); str.append("\"");
    str.append("/>");

    str.append("<details>"); str.append(replace4Elem(doc.purpose)); str.append("</details>");

    if (doc.isTax) {
      str.append("<tax-index ");
      if (doc.taxStatus != null) { str.append(" status-index=\""); str.append(doc.taxStatus); str.append("\""); } // 101
      if (doc.CBC != null) { str.append(" cbc=\""); str.append(doc.CBC); str.append("\""); } // 104
      if (doc.OCATO != null) { str.append(" ocato=\""); str.append(doc.OCATO); str.append("\""); } // 105
      if (doc.taxPaytReason != null) { str.append(" purpose-index=\""); str.append(doc.taxPaytReason); str.append("\"");} // 106
      if (doc.taxPeriod != null) { str.append(" period-index=\""); str.append(doc.taxPeriod); str.append("\"");} // 107
      if (doc.taxDocNum != null) { str.append(" num-index=\""); str.append(doc.taxDocNum); str.append("\"");} // 108
      if (doc.taxDocDate != null) { str.append(" date-index=\""); str.append(doc.taxDocDate); str.append("\"");} // 109
      if (doc.taxPaytKind != null) { str.append(" tax-index=\""); str.append(doc.taxPaytKind); str.append("\"");} // 110
      str.append(" />");
    }

    str.append("<payer");
    str.append(" name=\""); str.append(replace4Attr(doc.payerName)); str.append("\"");
    if (doc.payerAccount != null) { str.append(" acct=\""); str.append(doc.payerAccount); str.append("\""); }
    if (doc.payerINN != null) { str.append(" inn=\""); str.append(doc.payerINN); str.append("\""); }
    if (doc.payerCPP != null) { str.append(" kpp=\""); str.append(doc.payerCPP); str.append("\""); }
    str.append(" />");

    str.append("<payer-bank");
    if (doc.payerBankName != null) { str.append(" name=\""); str.append(replace4Attr(doc.payerBankName)); str.append("\""); }
    if (doc.payerBankAccount != null) { str.append(" acct=\""); str.append(doc.payerBankAccount); str.append("\""); }
    str.append(" ident-code=\""); str.append(doc.payerBankBIC); str.append("\"");
    str.append(" ident-type=\"МФО-9\"");
    str.append(" />");

    str.append("<payee");
    str.append(" name=\""); str.append(replace4Attr(doc.payeeName)); str.append("\"");
    if (doc.payeeAccount != null) { str.append(" acct=\""); str.append(doc.payeeAccount); str.append("\""); }
    if (doc.payeeINN != null) { str.append(" inn=\""); str.append(doc.payeeINN); str.append("\""); }
    if (doc.payeeCPP != null) { str.append(" kpp=\""); str.append(doc.payeeCPP); str.append("\""); }
    str.append(" />");

    str.append("<payee-bank");
    if (doc.payeeBankName != null) { str.append(" name=\""); str.append(replace4Attr(doc.payeeBankName)); str.append("\""); }
    if (doc.payeeBankAccount != null) { str.append(" acct=\""); str.append(doc.payeeBankAccount); str.append("\""); }
    str.append(" ident-code=\""); str.append(doc.payeeBankBIC); str.append("\"");
    str.append(" ident-type=\"МФО-9\"");
    str.append(" />");

    str.append("</doc>");

    return str.toString();
  }

}
