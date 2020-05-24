package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FDocument {

  public String edNo;
  public String edDate;

  public String docNum;
  public String docDate;
  public Long amount;
  public String purpose;

  public String payerName;
  public String payerAccount;
  public String payerINN;
  public String payerCPP;

  public String payerBankName;
  public String payerBankBIC;
  public String payerBankAccount;

  public String payeeName;
  public String payeeAccount;
  public String payeeINN;
  public String payeeCPP;

  public String payeeBankName;
  public String payeeBankBIC;
  public String payeeBankAccount;

  public void getFromED(Node node) {
    /**
     *  Loads FDocument fields from ED1xx element
     */
    if (node.getNodeType() != Node.TEXT_NODE) {

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("EDNo");
      if (nestedNode != null) edNo = nestedNode.getNodeValue();
      nestedNode = attr.getNamedItem("EDDate");
      if (nestedNode != null) edDate = nestedNode.getNodeValue();
      nestedNode = attr.getNamedItem("Sum");
      if (nestedNode != null) amount = Long.parseLong(nestedNode.getNodeValue());

      NodeList edOne = node.getChildNodes(); // List of child nodes for ED1xx

      for (int i = 0; i < edOne.getLength(); i++) {

        Node edChildNode = edOne.item(i);
        String nodeName = edChildNode.getNodeName();

        if (nodeName.equals("AccDoc")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("AccDocNo");
          if (nestedNode != null) docNum = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("AccDocDate");
          if (nestedNode != null) docDate = nestedNode.getNodeValue();
        }

        else if (nodeName.equals("Payer")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("INN");
          if (nestedNode != null) payerINN = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("KPP");
          if (nestedNode != null) payerCPP = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("PersonalAcc");
          if (nestedNode != null) payerAccount = nestedNode.getNodeValue();

          NodeList nestedNodes = edChildNode.getChildNodes();
          for (int j = 0; j < nestedNodes.getLength(); j++) {
            nestedNode = nestedNodes.item(j);
            String nestedNodeName = nestedNode.getNodeName();
            if (nestedNodeName.equals("Name")) {
              payerName = nestedNode.getTextContent();
            }
          }
        }

        else if (nodeName.equals("Payee")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("INN");
          if (nestedNode != null) payeeINN = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("KPP");
          if (nestedNode != null) payeeCPP = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("PersonalAcc");
          if (nestedNode != null) payeeAccount = nestedNode.getNodeValue();

          NodeList nestedNodes = edChildNode.getChildNodes();
          for (int j = 0; j < nestedNodes.getLength(); j++) {
            nestedNode = nestedNodes.item(j);
            String nestedNodeName = nestedNode.getNodeName();
            if (nestedNodeName.equals("Name")) {
              payeeName = nestedNode.getTextContent();
            }
          }
        }

        else if (nodeName.equals("Purpose")) {
          purpose = edChildNode.getTextContent();
        }
      }
    }
  }

  @Override
  public String toString() {
      String str = "EDNo: " + edNo + ", EDDate: " + edDate +
                   ", DocNo: " + docNum + ", Date: " + docDate +
                   ", Amount: " + amount / 100 + "." + amount % 100 + System.lineSeparator() +
                   " Payer Name: " + payerName + ", Account: " + payerAccount +
                   ", INN: " + payerINN + ", CPP: " + payerCPP + System.lineSeparator() +
                   " Payee Name: " + payeeName + ", Account: " + payeeAccount +
                   ", INN: " + payeeINN + ", CPP: " + payeeCPP + System.lineSeparator() +
                   " Purpose: " + purpose;
      return str;
  }
}
