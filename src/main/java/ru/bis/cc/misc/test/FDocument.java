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
  public String getDate() {
    return docDate;
  }

  /** Loads object from XML node
   *
   * @param node - ED node with document
   */
  public void fromED(Node node) {
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

  /** Creates FT14 string from object
   *
   * @return FT14 string (unicode), returns null if error or filter
   */
  public String toFT14String() {

    StringBuilder str = new StringBuilder();

//    if (Helper.isBeginsList(payerAccount, "301,302")) return null; // Filter output records by payer account

    // Build tax attributes string It has to be add to payment purpose
    StringBuilder locPurpose = new StringBuilder(); // Payment purpose that saves into FT14 - it difference with document purpose (contains tax attributes)
    locPurpose.append(getTaxAttrs());
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
    str.append(String.format("%5s", getId())); // index in documents array as reference variable part
    str.append(String.format("%" + String.format("%d", 111 - str.length() - 1) + "s", " ")); // 111 - absolute pos for amount
    str.append("2RURS");
    str.append(String.format("%17s", amount.toString()).replace(" ", "0")); // amount (with pad 0 left)
    str.append("0"); // amount specifies in 1/1000 of currency item, we have amount in 1/100
    str.append(String.format("%" + String.format("%d", 337 - str.length() - 1) + "s", " ")); // 337 - absolute pos for docDate
    str.append(docDate.replace("-", ""));
    str.append(String.format("%" + String.format("%d", 371 - str.length() - 1) + "s", " ")); // 371 - absolute pos for docNum
    str.append(String.format("%10s", docNum));
    if (payerAccount != null) str.append(String.format("%20s", payerAccount)); // 381, payerAccount just after docNum
    str.append(String.format("%" + String.format("%d", 593 - str.length() - 1) + "s", " ")); // 593 - absolute pos for payerName (it doesn't use for payment create)
    str.append(String.format("%-140s", payerName));
    str.append(String.format("%" + String.format("%d", 1365 - str.length() - 1) + "s", " ")); // 1365 - absolute pos for purpose (2 nd part for PA-payments)
    if (locPurpose.length() > 140) str.append(locPurpose.substring(140, Math.min(locPurpose.length(), 140 + 93)));
//    str.append(String.format("%" + String.format("%d", 1393 - str.length() - 1) + "s", " ")); // 1365 - absolute pos for purpose (2 nd part for RE-payments)
//    if (locPurpose.length() > 140) str.append(locPurpose.substring(140, Math.min(locPurpose.length(), 140 + 70)));
    str.append(String.format("%" + String.format("%d", 1565 - str.length() - 1) + "s", " ")); // 1565 - absolute pos for payeeBankAccount
    if (payeeBankAccount != null) str.append(String.format("%20s", payeeBankAccount));
    str.append(String.format("%" + String.format("%d", 1593 - str.length() - 1) + "s", " ")); // 1593 - absolute pos for payeeBankBIC
    str.append("BIK"); str.append(String.format("%9s", payeeBankBIC));
    // 1628 - absolute pos for payeeBankName
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
      if (2265 - str.length() - 1 > 0) { // this field begins just after previous, so "%s0" - results runtime error
        str.append(String.format("%" + String.format("%d", 2265 - str.length() - 1) + "s", " ")); // 2265 - absolute pos for UIN
      }
      str.append("/ROC/"); str.append(UIN);
    }

    return str.toString();
  }

  /** Creates MT103 string from object
   *
   * @return MT103 string (unicode), returns null if error or filter
   */
  public String toMT103String() {
    int iPos; // current position when we split text multistring text fields
    int iStr; // current string when we split text multistring text fields

    StringBuilder participant = new StringBuilder(); // string uses to assemle multistring text fields

    StringBuilder str = new StringBuilder();
    str.append("{4:");
    str.append(System.lineSeparator());

    str.append("20:");
    str.append(docNum);
    str.append(System.lineSeparator());

    str.append("32A:");
    str.append(Helper.getSWIFTDate(docDate));
    str.append("RUB");
    str.append(String.format("%.2f", (float) amount / 100).replace(',','.'));
    str.append(System.lineSeparator());

    str.append("50K:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (payerINN != null) { participant.append("INN"); participant.append(payerINN); participant.append(" "); }
    if (payerName != null) participant.append(payerName);
    if (payerAccount != null) { str.append("/"); str.append(payerAccount); str.append(System.lineSeparator()); iStr = 1; }
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length() - 1) break;
    }

    str.append("57D:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (payeeBankBIC != null) { participant.append("BIK"); participant.append(payeeBankBIC); participant.append(" "); }
    if (payeeBankName != null) participant.append(payeeBankName);
    if (payeeBankAccount != null) { str.append("/"); str.append(payeeBankAccount); str.append(System.lineSeparator()); iStr = 1; }
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length() - 1) break;
    }

    str.append("59:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (payeeINN != null) { participant.append("INN"); participant.append(payeeINN); participant.append(" "); }
    if (payeeName != null) participant.append(payeeName);
    if (payeeAccount != null) { str.append("/"); str.append(payeeAccount); str.append(System.lineSeparator()); iStr = 1; }
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length()) break;
    }

    // Build tax attributes string It has to be add to payment purpose
    StringBuilder locPurpose = new StringBuilder(); // Payment purpose that saves into MT103 - it difference with document purpose (contains tax attributes)
    locPurpose.append(getTaxAttrs());
    if (UIN != null) {
      locPurpose.append("УИН"); locPurpose.append(UIN); locPurpose.append("///");
    }
    locPurpose.append(purpose);
    // In MT103 payment purpose divides between 70 and 72 fields
    str.append("70:");
    iPos = 0;
    for (int j = 0; j <= 6; j++) {
      if (j == 4) {
        str.append("72:");
      }
      str.append(locPurpose.substring(iPos, Math.min(iPos + 35, locPurpose.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > locPurpose.length()) break;
    }

    str.append("77B:");
    str.append("REF"); str.append(getId());
    str.append(System.lineSeparator());

    str.append("-}");

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

  /** Function assembles tax attfibutes string
   *
   * @return string with tax attributes, separated by "/" or "\"
   */
  public String getTaxAttrs() {
    StringBuilder taxStr = new StringBuilder();
    if (isTax) {
      // First of all: we have to determine - which separator we are going to use - "/" or "\"
      char sepChar = '/';
      int posSepChar = purpose.indexOf(sepChar);
      if (posSepChar >= 0) {
        int posAltChar = purpose.indexOf('\\');
        if (posAltChar < 0 || posSepChar > posAltChar) sepChar = '\\';
      }
      taxStr.append(sepChar); taxStr.append(sepChar); taxStr.append(payerCPP); // 102
      taxStr.append(sepChar); taxStr.append(taxStatus); // 101
      taxStr.append(sepChar); taxStr.append(CBC); // 104
      taxStr.append(sepChar); taxStr.append(OCATO);
      taxStr.append(sepChar); taxStr.append(taxPaytReason);
      taxStr.append(sepChar); taxStr.append(taxPeriod);
      taxStr.append(sepChar); taxStr.append(taxDocNum);
      taxStr.append(sepChar); taxStr.append(taxDocDate); // 109
      if (taxPaytKind != null) { // 110
        taxStr.append(sepChar); taxStr.append(taxPaytKind);
      }
      taxStr.append(sepChar);
    }
    return taxStr.toString();
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

      if (!Helper.cmpNullString(payerName, compared.payerName)) return false;
      if (!Helper.cmpNullString(payerAccount, compared.payerAccount)) return false;
      if (!Helper.cmpNullString(payerINN, compared.payerINN)) return false;
      if (!Helper.cmpNullString(payerCPP, compared.payerCPP)) return false;

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



  /** Returns string with compare detals
   *
   * @param obj object to compare
   * @return string with list of mismatches
   */
  public String mismatchDescribe(Object obj) {
    StringBuilder str = new StringBuilder();
    str.append("Document ID: "); str.append(getId());
    if (obj == null) str.append(" Compared object is null");
    else if (obj.getClass() != FDocument.class) { str.append(" Compared object class mismatch: "); str.append(obj.getClass()); }
    else {
      FDocument compared = (FDocument) obj;
      str.append(" Attributes mismatch: "); str.append(System.lineSeparator());
      if (isUrgent != compared.isUrgent) {
        str.append(" Urgent mismatch: "); str.append(isUrgent); str.append(" against "); str.append(compared.isUrgent);
        str.append(System.lineSeparator());
      }
      if (isTax != compared.isTax) {
        str.append(" Tax mismatch: "); str.append(isTax); str.append(" against "); str.append(compared.isTax);
        str.append(System.lineSeparator());
      }

      if (!Helper.cmpNullString(docNum, compared.docNum)) oneMismatch(str, "DocNum", docNum, compared.docNum);
      if (!Helper.cmpNullString(docDate, compared.docDate)) oneMismatch(str, "DocDate", docDate, compared.docDate);
      if ((amount != null && compared.amount == null) || (amount == null && compared.amount != null)) {
        str.append(" Amount not specified: "); str.append(amount); str.append(" against ");
        str.append(compared.amount);
        str.append(System.lineSeparator());
      }
      else if (!(amount != null && amount.equals(compared.amount))) {
        str.append(" Amount mismatch: "); str.append(amount); str.append(" against "); str.append(compared.amount);
        str.append(System.lineSeparator());
      };
      if (!Helper.cmpNullString(purpose, compared.purpose)) oneMismatch(str, "Purpose", purpose, compared.purpose);
      if (!Helper.cmpNullString(UIN, compared.UIN)) oneMismatch(str, "UIN", UIN, compared.UIN);

      if (!Helper.cmpNullString(payerName, compared.payerName)) oneMismatch(str, "PayerName", payerName, compared.payerName);
      if (!Helper.cmpNullString(payerAccount, compared.payerAccount)) oneMismatch(str, "PayerAccount", payerAccount, compared.payerAccount);
      if (!Helper.cmpNullString(payerINN, compared.payerINN)) oneMismatch(str, "PayerINN", payerINN, compared.payerINN);
      if (!Helper.cmpNullString(payerCPP, compared.payerCPP)) oneMismatch(str, "PayerCPP", payerCPP, compared.payerCPP);
      if (!Helper.cmpNullString(payerBankName, compared.payerBankName)) oneMismatch(str, "PayerBankName", payerBankName, compared.payerBankName);
      if (!Helper.cmpNullString(payerBankBIC, compared.payerBankBIC)) oneMismatch(str, "PayerBankBIC", payerBankBIC, compared.payerBankBIC);
      if (!Helper.cmpNullString(payerBankAccount, compared.payerBankAccount)) oneMismatch(str, "PayerBankAccount", payerBankAccount, compared.payerBankAccount);

      if (!Helper.cmpNullString(payeeName, compared.payeeName)) oneMismatch(str, "PayeeName", payeeName, compared.payeeName);
      if (!Helper.cmpNullString(payeeAccount, compared.payeeAccount)) oneMismatch(str, "PayeeAccount", payeeAccount, compared.payeeAccount);
      if (!Helper.cmpNullString(payeeINN, compared.payeeINN)) oneMismatch(str, "PayeeINN", payeeINN, compared.payeeINN);
      if (!Helper.cmpNullString(payeeCPP, compared.payeeCPP)) oneMismatch(str, "PayeeCPP", payeeCPP, compared.payeeCPP);
      if (!Helper.cmpNullString(payeeBankName, compared.payeeBankName)) oneMismatch(str, "PayeeBankName", payeeBankName, compared.payeeBankName);
      if (!Helper.cmpNullString(payeeBankBIC, compared.payeeBankBIC)) oneMismatch(str, "PayeeBankBIC", payeeBankBIC, compared.payeeBankBIC);
      if (!Helper.cmpNullString(payeeBankAccount, compared.payeeBankAccount)) oneMismatch(str, "PayeeBankAccount", payeeBankAccount, compared.payeeBankAccount);

    }
    return str.toString();
  }

  /** Function describes one attribute mismatch
   *
   * @param str - common string with all mismatches
   * @param attrName - name of attribute
   * @param val1 - compare value 1
   * @param val2 - compare value 2
   * @return common string with all mismatches
   */
  public StringBuilder oneMismatch(StringBuilder str, String attrName, String val1, String val2) {
    str.append(" "); str.append(attrName); str.append(" mismatch: ");
    str.append(val1); str.append(" against "); str.append(val2);
    str.append(System.lineSeparator());
    return str;
  }
}
