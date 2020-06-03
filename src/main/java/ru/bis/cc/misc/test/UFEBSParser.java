package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class UFEBSParser {
  /** Loads object from XML node
   *
   * @param node - XML node with one document (ED1xx) or packet (PacketED)
   * @param doc - financial document
   */
  static void fromXML(Node node, FDocument doc) {
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
  }

}
