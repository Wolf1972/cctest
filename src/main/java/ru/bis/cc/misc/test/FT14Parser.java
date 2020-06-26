package ru.bis.cc.misc.test;

class FT14Parser extends Parser {
  /** Creates FT14 string from object
   *
   * @param doc - financial document
   * @return FT14 string (unicode), returns null if error or filter
   */
  String toString(FDocument doc) {

    StringBuilder str = new StringBuilder();

//    if (Helper.matchMask(payerAccount, "301,302")) return null; // Filter output records by payer account

    // Build tax attributes string It has to be add to payment purpose
    StringBuilder locPurpose = new StringBuilder(); // Payment purpose that saves into FT14 - it differences with document purpose (contains tax attributes)
    locPurpose.append(doc.buildPurpose(false));

    if (doc.isUrgent) // CLMOS or RTMOS - ordinary or urgent payment
      str.append("RTMOS");
    else
      str.append("CLMOS");

    str.append("   ");
    str.append("PA"); // Type of payment: PA or RE
    str.append("00910280"); // Sequence (constant part of reference)
    str.append("  ");
    str.append(doc.edDate.replace("-", "")); // EDDate as reference date (from XML date)
    str.append(" ");
    str.append(String.format("%5s", doc.getId())); // index in documents array as reference variable part
    str.append(String.format("%" + String.format("%d", 111 - str.length() - 1) + "s", " ")); // 111 - absolute pos for amount
    str.append("2RURS");
    str.append(String.format("%17s", doc.amount.toString()).replace(" ", "0")); // amount (with pad 0 left)
    str.append("0"); // amount specifies in 1/1000 of currency item, we have amount in 1/100
    str.append(String.format("%" + String.format("%d", 337 - str.length() - 1) + "s", " ")); // 337 - absolute pos for docDate
    str.append(doc.docDate.replace("-", ""));
    str.append(String.format("%" + String.format("%d", 371 - str.length() - 1) + "s", " ")); // 371 - absolute pos for docNum
    str.append(String.format("%10s", doc.docNum));
    if (doc.payerAccount != null) str.append(String.format("%20s", doc.payerAccount)); // 381, payerAccount just after docNum
    str.append(String.format("%" + String.format("%d", 485 - str.length() - 1) + "s", " ")); // 485 - absolute pos for priority
    str.append(String.format("%2s", doc.priority).replace(" ", "0"));
    str.append(String.format("%" + String.format("%d", 593 - str.length() - 1) + "s", " ")); // 593 - absolute pos for payerName (it doesn't use for payment create)
    str.append(String.format("%-140s", doc.payerName));
    str.append(String.format("%" + String.format("%d", 1365 - str.length() - 1) + "s", " ")); // 1365 - absolute pos for purpose (2 nd part for PA-payments)
    if (locPurpose.length() > 140) str.append(locPurpose.substring(140, Math.min(locPurpose.length(), 140 + 93)));
//    str.append(String.format("%" + String.format("%d", 1393 - str.length() - 1) + "s", " ")); // 1393 - absolute pos for purpose (2 nd part for RE-payments)
//    if (locPurpose.length() > 140) str.append(locPurpose.substring(140, Math.min(locPurpose.length(), 140 + 70)));
    str.append(String.format("%" + String.format("%d", 1565 - str.length() - 1) + "s", " ")); // 1565 - absolute pos for payeeBankAccount
    if (doc.payeeBankAccount != null) str.append(String.format("%20s", doc.payeeBankAccount));
    str.append(String.format("%" + String.format("%d", 1593 - str.length() - 1) + "s", " ")); // 1593 - absolute pos for payeeBankBIC
    str.append("BIK"); str.append(String.format("%9s", doc.payeeBankBIC));
    // 1628 - absolute pos for payeeBankName
    str.append(String.format("%" + String.format("%d", 1663 - str.length() - 1) + "s", " ")); // 1663 - absolute pos for payeeAccount
    if (doc.transKind != null) { str.append("*"); str.append(String.format("%2s", doc.transKind).replace(" ", "0")); }
    str.append(String.format("%" + String.format("%d", 1765 - str.length() - 1) + "s", " ")); // 1765 - absolute pos for payeeAccount
    str.append(String.format("%20s", doc.payeeAccount));
    str.append(String.format("%" + String.format("%d", 1793 - str.length() - 1) + "s", " ")); // 1793 - absolute pos for payeeINN
    if (doc.payeeINN != null) { str.append("INN"); str.append(doc.payeeINN); }
    if (doc.payeeCPP != null) { str.append("/KPP"); str.append(doc.payeeCPP); } // since 1806 - absolute pos for CPP
    str.append(String.format("%" + String.format("%d", 1828 - str.length() - 1) + "s", " ")); // 1828 - absolute pos for payeeName
    str.append(String.format("%-140s", doc.payeeName));
    str.append(String.format("%" + String.format("%d", 2125 - str.length() - 1) + "s", " ")); // 2125 - absolute pos for purpose (1st part)
    str.append(String.format("%-140s", locPurpose.substring(0, Math.min(locPurpose.length(), 140))));
    if (doc.UIN != null) {
      str.append("/ROC/"); str.append(doc.UIN);
    }

    return str.toString();
  }

  /** Loads document from FT14 string
   *
   * @param str = FT14 string
   * @return financial document
   */
  FDocument fromString(String str) {

    FDocument doc = new FDocument();

    doc.isUrgent = (str.substring(0, 2).equals("RT")); // RT* - urgent, CL* and others - ordinary payment
    doc.docNum = str.substring(29, 34).replace(" ", "");
    doc.docDate = Helper.getXMLDate(str.substring(20, 20 + 8));
    doc.referenceFT14 = str.substring(8, 19) + String.format("%5s", doc.docNum).replace(" ", "0");
    doc.amount = Long.parseLong(str.substring(115, 115 + 18)) / 1000;

    // Payment purpose depends on "RE" or "PA" string begins
    StringBuilder locPurpose = new StringBuilder(str.substring(2124, Math.min(2124 + 140, str.length())));
    if (str.substring(8, 10).equals("RE"))
      locPurpose.append(str, 1392, 1392 + 70);
    else
      locPurpose.append(str, 1365, 1365 + 98);
    doc.parsePurpose(locPurpose.toString()); // Fills purpose, tax attributes and attributes with key words
    System.out.println(doc.purpose);
/*
    String UIN;
    String priority;
    String chargeOffDate; // YYYY-MM-DD
    String receiptDate;   // YYYY-MM-DD
    String transKind;

    String payerName;
    String payerAccount;
    String payerINN;
    String payerCPP;

    String payerBankName;
    String payerBankBIC;
    String payerBankAccount;

    String payeeName;
    String payeeAccount;
    String payeeINN;
    String payeeCPP;

    String payeeBankName;
    String payeeBankBIC;
    String payeeBankAccount;

    boolean isTax;
    String taxStatus; // 101
    String CBC; // 104
    String OCATO; // 105
    String taxPaytReason; // 106
    String taxPeriod; // 107
    String taxDocNum; // 108
    String taxDocDate; // 109
    String taxPaytKind; // 110
*/
    return doc;
  }
}
