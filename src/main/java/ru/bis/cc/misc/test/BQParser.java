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
  FDocument fromXML(Node node) {

    FDocument doc = new FDocument();

    if (node.getNodeType() != Node.TEXT_NODE) {

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("id");
      if (nestedNode != null) doc.referenceBq = nestedNode.getNodeValue();

      NodeList edOne = node.getChildNodes(); // List of child nodes for <doc>

      for (int i = 0; i < edOne.getLength(); i++) {

        Node edChildNode = edOne.item(i);
        if (edChildNode.getNodeType() != Node.TEXT_NODE) {

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
            NodeList edEntries = edChildNode.getChildNodes(); // List of child nodes for <entries>
            for (int j = 0; j < edEntries.getLength(); j++) {
              Node edEntry = edEntries.item(j);
              if (edEntry.getNodeType() != Node.TEXT_NODE) {
                if (edEntry.getNodeName().equals("entry")) {
                  attr = edEntry.getAttributes();
                  nestedNode = attr.getNamedItem("amt-rub");
                  if (nestedNode != null) {
                    String amtFloat = nestedNode.getNodeValue();
                    if (!amtFloat.contains(".")) amtFloat += "00";
                    else if (amtFloat.substring(amtFloat.indexOf(".") + 1).length() == 1)
                      amtFloat += "0"; // 123.5 = 123.50
                    doc.amount = Long.parseLong(amtFloat.replace(".", ""));
                  }
                  nestedNode = attr.getNamedItem("op-date");
                  if (nestedNode != null) doc.edDate = nestedNode.getNodeValue();
                  nestedNode = attr.getNamedItem("acct-db");
                  if (nestedNode != null) doc.accountDb = nestedNode.getNodeValue();
                  nestedNode = attr.getNamedItem("acct-cr");
                  if (nestedNode != null) doc.accountCr = nestedNode.getNodeValue();

                  break; // Takes only one entry
                }
              }
            }
          }

          else if (nodeName.equals("references")) {
            NodeList edEntries = edChildNode.getChildNodes(); // List of child nodes for <references>
            for (int j = 0; j < edEntries.getLength(); j++) {
              Node edRef = edEntries.item(j);
              if (edRef.getNodeType() != Node.TEXT_NODE) {
                String reference = null;
                String type = null;
                if (edRef.getNodeName().equals("reference")) {
                  attr = edRef.getAttributes();
                  nestedNode = attr.getNamedItem("reference");
                  if (nestedNode != null) reference = nestedNode.getNodeValue();
                  nestedNode = attr.getNamedItem("type");
                  if (nestedNode != null) type = nestedNode.getNodeValue();
                }
                if (type != null) {
                  if (type.equals("DBI")) doc.referenceFT14 = reference;
                  else { // UFEBS reference
                    if (reference != null) {
                      if (reference.contains(",")) {
                        doc.edNo = reference.substring(reference.indexOf(",") + 1);
                        doc.authorUIS = reference.substring(0, reference.indexOf(","));
                      }
                    }
                  }
                }
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
  String toString(FDocument doc) {

    StringBuilder str = new StringBuilder();

    str.append("<doc");
    str.append(" id=\""); str.append(doc.getId()); str.append("\"");
    str.append(" action=\"update\">");
    str.append(System.lineSeparator());

    str.append("<reg");
    str.append(" doc-type=\""); str.append(doc.transKind); str.append("\"");
    str.append(" doc-num=\""); str.append(doc.docNum); str.append("\"");
    str.append(" doc-date=\""); str.append(doc.docDate); str.append("\"");
    str.append(" order-pay=\""); str.append(doc.priority); str.append("\"");
    if (doc.chargeOffDate != null) { str.append(" chgoff-date=\""); str.append(doc.chargeOffDate); str.append("\""); }
    if (doc.receiptDate != null) { str.append(" payee-received=\""); str.append(doc.receiptDate); str.append("\""); }
    str.append(" delivery=\""); str.append(doc.isUrgent? "Э" : "E"); str.append("\"");
    str.append("/>");
    str.append(System.lineSeparator());

    str.append("<details>"); str.append(replace4Elem(doc.purpose)); str.append("</details>");
    str.append(System.lineSeparator());

    if (doc.isTax) {
      str.append("<tax-index");
      if (doc.taxStatus != null) { str.append(" status-index=\""); str.append(doc.taxStatus); str.append("\""); } // 101
      if (doc.CBC != null) { str.append(" cbc=\""); str.append(doc.CBC); str.append("\""); } // 104
      if (doc.OCATO != null) { str.append(" ocato=\""); str.append(doc.OCATO); str.append("\""); } // 105
      if (doc.taxPaytReason != null) { str.append(" purpose-index=\""); str.append(doc.taxPaytReason); str.append("\"");} // 106
      if (doc.taxPeriod != null) { str.append(" period-index=\""); str.append(doc.taxPeriod); str.append("\"");} // 107
      if (doc.taxDocNum != null) { str.append(" num-index=\""); str.append(doc.taxDocNum); str.append("\"");} // 108
      if (doc.taxDocDate != null) { str.append(" date-index=\""); str.append(doc.taxDocDate); str.append("\"");} // 109
      if (doc.taxPaytKind != null) { str.append(" tax-index=\""); str.append(doc.taxPaytKind); str.append("\"");} // 110
      str.append("/>");
      str.append(System.lineSeparator());
    }

    str.append("<payer");
    str.append(" name=\""); str.append(replace4Attr(doc.payerName)); str.append("\"");
    if (doc.payerAccount != null) { str.append(" acct=\""); str.append(doc.payerAccount); str.append("\""); }
    if (doc.payerINN != null) { str.append(" inn=\""); str.append(doc.payerINN); str.append("\""); }
    if (doc.payerCPP != null) { str.append(" kpp=\""); str.append(doc.payerCPP); str.append("\""); }
    str.append("/>");
    str.append(System.lineSeparator());

    str.append("<payer-bank");
    if (doc.payerBankName != null) { str.append(" name=\""); str.append(replace4Attr(doc.payerBankName)); str.append("\""); }
    if (doc.payerBankAccount != null) { str.append(" acct=\""); str.append(doc.payerBankAccount); str.append("\""); }
    str.append(" ident-code=\""); str.append(doc.payerBankBIC); str.append("\"");
    str.append(" ident-type=\"МФО-9\"");
    str.append("/>");
    str.append(System.lineSeparator());

    str.append("<payee");
    str.append(" name=\""); str.append(replace4Attr(doc.payeeName)); str.append("\"");
    if (doc.payeeAccount != null) { str.append(" acct=\""); str.append(doc.payeeAccount); str.append("\""); }
    if (doc.payeeINN != null) { str.append(" inn=\""); str.append(doc.payeeINN); str.append("\""); }
    if (doc.payeeCPP != null) { str.append(" kpp=\""); str.append(doc.payeeCPP); str.append("\""); }
    str.append("/>");
    str.append(System.lineSeparator());

    str.append("<payee-bank");
    if (doc.payeeBankName != null) { str.append(" name=\""); str.append(replace4Attr(doc.payeeBankName)); str.append("\""); }
    if (doc.payeeBankAccount != null) { str.append(" acct=\""); str.append(doc.payeeBankAccount); str.append("\""); }
    str.append(" ident-code=\""); str.append(doc.payeeBankBIC); str.append("\"");
    str.append(" ident-type=\"МФО-9\"");
    str.append("/>");
    str.append(System.lineSeparator());

    str.append("<entries>");
    str.append(System.lineSeparator());
    str.append("<entry");
    str.append(" op-entry=\"0\"");
    str.append(" acct-db=\"");
    if (doc.accountDb != null)
      str.append(doc.accountDb);
    else {
      if (doc.payerBankBIC.equals(Constants.ourBankBIC)) str.append(doc.payerAccount);
      else str.append(Constants.ourBankAcc);
    }
    str.append("\"");
    str.append(" acct-cr=\"");
    if (doc.accountCr != null)
      str.append(doc.accountCr);
    else {
      if (doc.payeeBankBIC.equals(Constants.ourBankBIC)) str.append(doc.payeeAccount);
      else str.append(Constants.ourBankAcc);
    }
    str.append("\"");
    str.append(" amt-cur=\"0\"");
    if (doc.amount != null) {
      str.append(" amt-rub=\"");
      str.append(doc.amount / 100);
      if (doc.amount % 100 > 0) str.append("." + doc.amount % 100);
      str.append("\"");
    }
    str.append(" currency=\"810\"");
    if (doc.edDate != null) { str.append(" op-date=\""); str.append(doc.edDate); str.append("\""); }
    str.append(" eard=\"N\" qty=\"0\"/>");
    str.append(System.lineSeparator());
    str.append("</entries>");
    str.append(System.lineSeparator());

    if (doc.referenceFT14 != null || doc.edNo != null) {
      str.append("<references>");
      str.append(System.lineSeparator());
      if (doc.edNo != null) {
        str.append("<reference");
        str.append(" direction=\"I\""); // There is mistake - all UFEBS references are "INCOMING"
        str.append(" trip=\"1\"");
        str.append(" reference=\"");
        if (doc.authorUIS != null)
          str.append(doc.authorUIS);
        else
          str.append(doc.payerBankBIC.equals(Constants.ourBankBIC)? Constants.ourBankUIS: Constants.otherBankUIS);
        str.append(",");
        str.append(doc.edNo);
        str.append("\"");
        str.append(" type=\"РКЦ\"/>");
        str.append(System.lineSeparator());
      }
      if (doc.referenceFT14 != null) {
        str.append("<reference");
        str.append(" direction=\"I\"");
        str.append(" trip=\"1\"");
        str.append(" reference=\""); str.append(doc.referenceFT14); str.append("\"");
        str.append(" type=\"DBI\"/>");
        str.append(System.lineSeparator());
      }
      str.append("</references>");
      str.append(System.lineSeparator());
    }

    str.append("</doc>");

    return str.toString();
  }

  /** Loads one client from XML node
   *
   * @param node - XML node with one client (person, cust-corp or bank)
   * @return  Client object
   */
  Client clientFromXML(Node node) {

    if (node.getNodeType() != Node.TEXT_NODE) {

      Client client = new Client();

      String clientNodeName = node.getNodeName();
      if (clientNodeName.equals("person")) client.type = ClientType.PERSON;
      else if (clientNodeName.equals("cust-priv")) client.type = ClientType.SELF_EMPLOYED;
      else if (clientNodeName.equals("cust-corp")) client.type = ClientType.COMPANY;
      else if (clientNodeName.equals("bank")) client.type = ClientType.BANK;

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("id");
      if (nestedNode != null) client.id = getClientIdForType(Long.parseLong(nestedNode.getNodeValue()), client.type);

      NodeList edOne = node.getChildNodes(); // List of child nodes for client node (<person>, <cust-corp>, etc)

      for (int i = 0; i < edOne.getLength(); i++) {

        Node edChildNode = edOne.item(i);

        if (edChildNode.getNodeType() != Node.TEXT_NODE) {

          String nodeName = edChildNode.getNodeName();

          if (nodeName.equals("reg")) {
            attr = edChildNode.getAttributes();
            nestedNode = attr.getNamedItem("name-last");
            if (nestedNode != null) client.lastName = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("name-first");
            if (nestedNode != null) client.firstNames = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("name-mid");
            if (nestedNode != null) client.firstNames = (client.firstNames != null? client.firstNames + " " + nestedNode.getNodeValue(): nestedNode.getNodeValue());
            nestedNode = attr.getNamedItem("name");
            if (nestedNode != null) client.officialName = nestedNode.getNodeValue();
          }

          else if (nodeName.equals("idents")) {
            NodeList edIdents = edChildNode.getChildNodes(); // List of child nodes for <idents>
            for (int j = 0; j < edIdents.getLength(); j++) {
              Node edIdent = edIdents.item(j);
              if (edIdent.getNodeType() != Node.TEXT_NODE) {
                String type = "";
                String number = "";
                if (edIdent.getNodeName().equals("ident")) {
                  attr = edIdent.getAttributes();
                  nestedNode = attr.getNamedItem("type");
                  if (nestedNode != null) type = nestedNode.getNodeValue();
                  nestedNode = attr.getNamedItem("number");
                  if (nestedNode != null) number = nestedNode.getNodeValue();
                  if (type.equals("ИНН")) {
                    client.INN = number;
                  }
                  else if (type.equals("МФО-9")) {
                    client.bankBIC = number;
                  }
                  else if (type.equals("РКЦ_СЧЕТ")) {
                    client.bankCorrAccount = number;
                  }
                }
              }
            }
          }
        }
      }
      return client;
    }
    return null;
  }

  /** Loads one account from XML node
   *
   * @param node - XML node with one account (acct)
   * @return  Account object
   */
  Account accountFromXML(Node node) {

    if (node.getNodeType() != Node.TEXT_NODE) {

      Account account = new Account();

      NamedNodeMap attr = node.getAttributes();
      Node nestedNode = attr.getNamedItem("id");

      NodeList edOne = node.getChildNodes(); // List of child nodes for <acct>

      for (int i = 0; i < edOne.getLength(); i++) {

        Node edChildNode = edOne.item(i);

        if (edChildNode.getNodeType() != Node.TEXT_NODE) {

          String nodeName = edChildNode.getNodeName();

          if (nodeName.equals("reg")) {
            attr = edChildNode.getAttributes();
            nestedNode = attr.getNamedItem("acct");
            if (nestedNode != null) account.account = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("open-date");
            if (nestedNode != null) account.openDate = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("close-date");
            if (nestedNode != null) account.closeDate = nestedNode.getNodeValue();
          }
          else if (nodeName.equals("business")) {
            attr = edChildNode.getAttributes();
            nestedNode = attr.getNamedItem("details");
            if (nestedNode != null) account.details = nestedNode.getNodeValue();
          }
          else if (nodeName.equals("client")) {
            attr = edChildNode.getAttributes();
            nestedNode = attr.getNamedItem("cust-cat");
            if (nestedNode != null) {
              String type = nestedNode.getNodeValue();
              if (type.equals("Ч")) account.clientType = ClientType.PERSON;
              else if (type.equals("П")) account.clientType = ClientType.SELF_EMPLOYED;
              else if (type.equals("Ю")) account.clientType = ClientType.COMPANY;
              else if (type.equals("Б")) account.clientType = ClientType.BANK;
            }
            nestedNode = attr.getNamedItem("cust-id");
            if (nestedNode != null) account.clientId = getClientIdForType(Long.parseLong(nestedNode.getNodeValue()), account.clientType);
            nestedNode = attr.getNamedItem("internal");
            if (nestedNode != null) account.isInternal = nestedNode.getNodeValue().equals("Y");
          }
        }
      }
      return account;
    }
    return null;
  }

  /** Function returns id corrected with client type:
   *  multiplies by 10 and adds 1 for PERSON, 2 for SELF_EMPLOYED, 3 for COMPANY and 4 for BANK
   *
   * @param sourceId - source id (reads from BQ static file import)
   * @param type - client type
   * @return - unique identifier
   */
  private Long getClientIdForType(Long sourceId, ClientType type) {
    Long id = sourceId * 10;
    if (type == ClientType.PERSON) id += 1;
    else if (type == ClientType.SELF_EMPLOYED) id += 2;
    else if (type == ClientType.COMPANY) id += 3;
    else if (type == ClientType.BANK) id += 4;
    return id;
  }

}
