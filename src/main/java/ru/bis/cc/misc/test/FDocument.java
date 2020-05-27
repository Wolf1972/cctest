package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FDocument {

  private String edNo;
  private String edDate;
  private boolean isUrgent;

  private String docNum;
  private String docDate;
  private Long amount;
  private String purpose;
  private String UIN;

  private String payerName;
  private String payerAccount;
  private String payerINN;
  private String payerCPP;

  private String payerBankName;
  private String payerBankBIC;
  private String payerBankAccount;

  private String payeeName;
  private String payeeAccount;
  private String payeeINN;
  private String payeeCPP;

  private String payeeBankName;
  private String payeeBankBIC;
  private String payeeBankAccount;

  private boolean isTax;
  private String taxStatus; // 101
  private String CBC; // 104
  private String OCATO; // 105
  private String taxPaytReason; // 106
  private String taxPeriod; // 107
  private String taxDocNum; // 108
  private String taxDocDate; // 109
  private String taxPaytKind; // 110

  FDocument() {
    isUrgent = false;
    isTax = false;
  }

  public Long getId() {
    return Long.parseLong(docNum);
  }

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
      nestedNode = attr.getNamedItem("PaymentID");
      if (nestedNode != null) UIN = nestedNode.getNodeValue();

      nestedNode = attr.getNamedItem("SystemCode");
      if (nestedNode != null) isUrgent = nestedNode.getNodeValue().equals("05");

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
            else if (nestedNodeName.equals("Bank")) {
              attr = nestedNode.getAttributes();
              nestedNode = attr.getNamedItem("BIC");
              if (nestedNode != null) payerBankBIC = nestedNode.getNodeValue();
              nestedNode = attr.getNamedItem("CorrespAcc");
              if (nestedNode != null) payerBankAccount = nestedNode.getNodeValue();
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
            else if (nestedNodeName.equals("Bank")) {
              attr = nestedNode.getAttributes();
              nestedNode = attr.getNamedItem("BIC");
              if (nestedNode != null) payeeBankBIC = nestedNode.getNodeValue();
              nestedNode = attr.getNamedItem("CorrespAcc");
              if (nestedNode != null) payeeBankAccount = nestedNode.getNodeValue();
            }
          }
        }

        else if (nodeName.equals("Purpose")) {
          purpose = edChildNode.getTextContent();
        }

        else if (nodeName.equals("DepartmentalInfo")) {
          attr = edChildNode.getAttributes();
          if (attr.getLength() > 0) {
            nestedNode = attr.getNamedItem("DrawerStatus");
            if (nestedNode != null) taxStatus = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("CBC");
            if (nestedNode != null) CBC = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("OKATO");
            if (nestedNode != null) OCATO = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("PaytReason");
            if (nestedNode != null) taxPaytReason = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("TaxPeriod");
            if (nestedNode != null) taxPeriod = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("DocNo");
            if (nestedNode != null) taxDocNum = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("DocDate");
            if (nestedNode != null) taxDocDate = nestedNode.getNodeValue();
            nestedNode = attr.getNamedItem("TaxPaytKind");
            if (nestedNode != null) taxPaytKind = nestedNode.getNodeValue();
            isTax = true;
          }
        }
      }
    }
  }

  public String toFT14String(Long id) {
    StringBuilder str = new StringBuilder();
    StringBuilder locPurpose = new StringBuilder(); // Payment purpose that saves into FT14 - it difference with document purpose (contains tax attributes)

    // Build tax attributes string It has to be add to payment purpose
    if (isTax) {
      // First of all: we have to determine - which separator we are going to use - "/" or "\"
      char sepChar = '/';
      int posSepChar = purpose.indexOf(sepChar);
      if (posSepChar >= 0) {
        int posAltChar = purpose.indexOf('\\');
        if (posAltChar < 0 || posSepChar > posAltChar) sepChar = '\\';
      }
      locPurpose.append(sepChar); locPurpose.append(sepChar); locPurpose.append(payerCPP); // 102
      locPurpose.append(sepChar); locPurpose.append(taxStatus); // 101
      locPurpose.append(sepChar); locPurpose.append(CBC); // 104
      locPurpose.append(sepChar); locPurpose.append(OCATO);
      locPurpose.append(sepChar); locPurpose.append(taxPaytReason);
      locPurpose.append(sepChar); locPurpose.append(taxPeriod);
      locPurpose.append(sepChar); locPurpose.append(taxDocNum);
      locPurpose.append(sepChar); locPurpose.append(taxDocDate); // 109
      if (taxPaytKind != null) { // 110
        locPurpose.append(sepChar); locPurpose.append(taxPaytKind);
      }
      locPurpose.append(sepChar);
    }
    locPurpose.append(purpose);
    
    if (isUrgent) // CLMOS or RTMOS - ordinary or urgent payment
      str.append("RTMOS");
    else
      str.append("CLMOS");

    str.append("   ");
    str.append("PA"); // Type of payment: PA or RE
    str.append("00910280"); // Sequence (constant part of reference)
    str.append("  ");
    str.append(edDate.replace("-", "")); // EDDate as reference date
    str.append(" ");
    str.append(String.format("%5s", id.toString())); // index in documents array as reference variable part
    str.append(String.format("%" + String.format("%d", 111 - str.length() - 1) + "s", " ")); // 111 - absolute pos for amount
    str.append("2RURS");
    str.append(String.format("%17s", amount.toString()).replace(" ", "0")); // amount (with pad 0 left)
    str.append("0"); // amount specifies in 1/1000 of currency item, we have amount in 1/100
    str.append(String.format("%" + String.format("%d", 337 - str.length() - 1) + "s", " ")); // 337 - absolute pos for docDate
    str.append(docDate.replace("-", ""));
    str.append(String.format("%" + String.format("%d", 371 - str.length() - 1) + "s", " ")); // 371 - absolute pos for docNum
    str.append(String.format("%10s", docNum));
    str.append(String.format("%" + String.format("%d", 593 - str.length() - 1) + "s", " ")); // 593 - absolute pos for payerName (it doesn't use for payment create)
    str.append(String.format("%-140s", payerName));
    str.append(String.format("%20s", payerAccount)); // 381, payerAccount just after docNum
    str.append(String.format("%" + String.format("%d", 1365 - str.length() - 1) + "s", " ")); // 1365 - absolute pos for purpose (2 nd part for PA-payments)
    if (locPurpose.length() > 140) str.append(locPurpose.substring(140, Math.min(locPurpose.length(), 140 + 93)));
//    str.append(String.format("%" + String.format("%d", 1393 - str.length() - 1) + "s", " ")); // 1365 - absolute pos for purpose (2 nd part for RE-payments)
//    if (locPurpose.length() > 140) str.append(locPurpose.substring(140, Math.min(locPurpose.length(), 140 + 70)));
    str.append(String.format("%" + String.format("%d", 1565 - str.length() - 1) + "s", " ")); // 1565 - absolute pos for payeeBankBIC
    if (payeeBankAccount != null) str.append(String.format("%20s", payeeBankAccount));
    str.append(String.format("%" + String.format("%d", 1593 - str.length() - 1) + "s", " ")); // 1593 - absolute pos for payeeBankBIC
    str.append("BIK"); str.append(String.format("%9s", payeeBankBIC));
    str.append(String.format("%" + String.format("%d", 1765 - str.length() - 1) + "s", " ")); // 1765 - absolute pos for payeeAccount
    str.append(String.format("%20s", payeeAccount));
    str.append(String.format("%" + String.format("%d", 1793 - str.length() - 1) + "s", " ")); // 1793 - absolute pos for payeeINN
    if (payeeINN != null) { str.append("INN"); str.append(payeeINN); }
    if (payeeCPP != null) { str.append("/KPP"); str.append(payeeCPP); } // since 1806 - absolute pos for CPP
    str.append(String.format("%" + String.format("%d", 1828 - str.length() - 1) + "s", " ")); // 1828 - absolute pos for payeeName
    str.append(String.format("%-140s", payeeName));
    str.append(String.format("%" + String.format("%d", 2125 - str.length() - 1) + "s", " ")); // 2125 - absolute pos for purpose (1st part)
    str.append(locPurpose.substring(0, Math.min(locPurpose.length(), 140)));
    if (UIN != null) {
      if (2265 - str.length() - 1 > 0) { // this field begins right away after previous, so "%s0" - results runtime error
        str.append(String.format("%" + String.format("%d", 2265 - str.length() - 1) + "s", " ")); // 2265 - absolute pos for UIN
      }
      str.append("/ROC/"); str.append(UIN);
    }

    return str.toString();
  }

  @Override
  public String toString() {
      String str = "EDNo: " + edNo + ", EDDate: " + edDate +
                   ", DocNo: " + docNum + ", Date: " + docDate +
                   ", Amount: " + amount / 100 + "." + amount % 100 + System.lineSeparator() +
                   " Payer Bank: " + payerBankName + ", BIC: " + payerBankBIC + ", Account: " + payerBankAccount + System.lineSeparator() +
                   " Payer Name: " + payerName + ", Account: " + payerAccount +
                   ", INN: " + payerINN + ", CPP: " + payerCPP + System.lineSeparator() +
                   " Payee Bank: " + payeeBankName + ", BIC: " + payeeBankBIC + ", Account: " + payeeBankAccount + System.lineSeparator() +
                   " Payee Name: " + payeeName + ", Account: " + payeeAccount +
                   ", INN: " + payeeINN + ", CPP: " + payeeCPP + System.lineSeparator() +
                   " Purpose: " + purpose;
      String tax = "";
      if (taxStatus != null) tax += " Status: " + taxStatus;
      if (CBC != null) tax += " CBC: " + CBC;
      if (OCATO != null) tax += " OCATO: " + OCATO;
      if (taxPaytReason != null) tax += " Reason: " + taxPaytReason;
      if (taxPeriod != null) tax += " Period: " + taxPeriod;
      if (taxDocNum != null) tax += " DocNo: " + taxDocNum;
      if (taxDocDate != null) tax += " DocDate: " + taxDocDate;
      if (taxPaytKind != null) tax += " Kind: " + taxPaytKind;
      if (tax.length() > 0) str = str + System.lineSeparator() + tax;
      return str;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != FDocument.class) return false;
    else {
      FDocument compared = (FDocument) obj;
      if (isUrgent != compared.isUrgent) return false;
      if (isTax != compared.isTax) return false;

      if (!Helper.cmpNullString(docNum, compared.docNum)) return false;
      if (!Helper.cmpNullString(docDate, compared.docDate)) return false;
      if ((amount != null && compared.amount == null) || (amount == null && compared.amount != null)) return false;
      if (!(amount != null && amount.equals(compared.amount))) return false;
      if (!Helper.cmpNullString(purpose, compared.purpose)) return false;
      if (!Helper.cmpNullString(UIN, compared.UIN)) return false;

//      if (!Helper.cmpNullString(payerName, compared.payerName)) return false;
//      if (!Helper.cmpNullString(payerAccount, compared.payerAccount)) return false;
//      if (!Helper.cmpNullString(payerINN, compared.payerINN)) return false;
//      if (!Helper.cmpNullString(payerCPP, compared.payerCPP)) return false;

      if (!Helper.cmpNullString(payerBankName, compared.payerBankName)) return false;
      if (!Helper.cmpNullString(payerBankBIC, compared.payerBankBIC)) return false;
      if (!Helper.cmpNullString(payerBankAccount, compared.payerBankAccount)) return false;

      if (!Helper.cmpNullString(payeeName, compared.payeeName)) return false;
      if (!Helper.cmpNullString(payeeAccount, compared.payeeAccount)) return false;
      if (!Helper.cmpNullString(payeeINN, compared.payeeINN)) return false;
      if (!Helper.cmpNullString(payeeCPP, compared.payeeCPP)) return false;

      if (!Helper.cmpNullString(payeeBankName, compared.payeeBankName)) return false;
      if (!Helper.cmpNullString(payeeBankBIC, compared.payeeBankBIC)) return false;
      if (!Helper.cmpNullString(payeeBankAccount, compared.payeeBankAccount)) return false;

      if (!Helper.cmpNullString(taxStatus, compared.taxStatus)) return false;
      if (!Helper.cmpNullString(CBC, compared.CBC)) return false;
      if (!Helper.cmpNullString(OCATO, compared.OCATO)) return false;
      if (!Helper.cmpNullString(taxPaytReason, compared.taxPaytReason)) return false;
      if (!Helper.cmpNullString(taxPeriod, compared.taxPeriod)) return false;
      if (!Helper.cmpNullString(taxDocNum, compared.taxDocNum)) return false;
      if (!Helper.cmpNullString(taxDocDate, compared.taxDocDate)) return false;
      if (!Helper.cmpNullString(taxPaytKind, compared.taxPaytKind)) return false;

    }
    return true;
  }
}
