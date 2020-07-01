package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ED807Parser extends XMLParser {

  /** Loads one bank from XML node BICDirectoryEntry
   *
   * @param node - XML node with one bank
   * @return  Bank object
   */
  Bank bankFromXML(Node node) {

    if (node.getNodeType() != Node.TEXT_NODE) {

      Bank bank = new Bank();

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("BIC");
      if (nestedNode != null) bank.bic = nestedNode.getNodeValue();

      NodeList oneBank = node.getChildNodes(); // List of child nodes for BankDirectoryEntry

      for (int i = 0; i < oneBank.getLength(); i++) {

        Node edChildNode = oneBank.item(i);
        String nodeName = Helper.getSimpleNodeName(edChildNode);

        if (nodeName.equals("ParticipantInfo")) {
          attr = edChildNode.getAttributes();
          nestedNode = attr.getNamedItem("NameP");
          if (nestedNode != null) bank.name = nestedNode.getNodeValue();
          nestedNode = attr.getNamedItem("Tnp");
          if (nestedNode != null) bank.town = nestedNode.getNodeValue() + " ";
          nestedNode = attr.getNamedItem("Nnp");
          if (nestedNode != null) bank.town += nestedNode.getNodeValue();
        }
      }

      return bank;
    }
    return null;
  }
}
