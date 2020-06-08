package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class BQParser {
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

}
