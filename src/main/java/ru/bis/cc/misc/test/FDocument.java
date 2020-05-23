package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FDocument {

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
    if (node.getNodeType() != Node.TEXT_NODE) {
      NodeList edOne = node.getChildNodes(); // List of child nodes for ED1xx
      for (int i = 0; i < edOne.getLength(); i++) {
        Node edChildNode = edOne.item(i);
        String nodeName = edChildNode.getNodeName();
        if (nodeName.equals("AccDoc")) {
          NamedNodeMap attr = edChildNode.getAttributes();
          Node nestedNode = attr.getNamedItem("AccDocNo");
          if (nestedNode != null) docNum = nestedNode.getNodeValue();
        }
        else if (nodeName.equals("Payer")) {
          NodeList nestedNodes = edChildNode.getChildNodes();
          for (int j = 0; j < nestedNodes.getLength(); j++) {
            Node nestedNode = nestedNodes.item(j);
            String nestedNodeName = nestedNode.getNodeName();
            if (nestedNodeName.equals("Name")) {
              payerName = nestedNode.getTextContent();
            }
          }
        }
        else if (nodeName.equals("Payee")) {
          NodeList nestedNodes = edChildNode.getChildNodes();
          for (int j = 0; j < nestedNodes.getLength(); j++) {
            Node nestedNode = nestedNodes.item(j);
            String nestedNodeName = nestedNode.getNodeName();
            if (nestedNodeName.equals("Name")) {
              payeeName = nestedNode.getTextContent();
            }
          }
        }
      }
    }
  }
}
