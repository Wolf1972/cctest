package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;

class FT14Parser extends Parser {

  FT14Parser() {
    logger = LogManager.getLogger(FT14Processor.class);
  }

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
    if (doc.edDate != null)
      str.append(doc.edDate.replace("-", "")); // EDDate as reference date (from XML date)
    else
      str.append(doc.docDate.replace("-", "")); // Document date as reference date (if XML date is null)
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
    doc.docNum = str.substring(30 - 1, 30 - 1 + 5).replace(" ", "");
    doc.docDate = Helper.getXMLDate(str.substring(337 - 1, 337 - 1 + 8));
    doc.referenceFT14 = str.substring(9 - 1, 9 - 1 + 10) + String.format("%5s", doc.docNum).replace(" ", "0");
    doc.amount = Long.parseLong(str.substring(116 - 1, 116 - 1 + 18)) / 10; // Sum 1.00 RUR outputs in FT14 as 1000

    // Payment purpose depends on "RE" or "PA" string begins
    StringBuilder locPurpose = new StringBuilder(str.substring(2125 - 1, Math.min(2125 - 1 + 140, str.length())));
    if (doc.referenceFT14.substring(0, 2).equals("RE"))
      locPurpose.append(str, 1393 - 1, 1393 - 1 + 70);
    else
      locPurpose.append(str, 1365 - 1, 1365 - 1 + 98);
    doc.parsePurpose(locPurpose.toString().trim()); // Fills purpose, tax attributes and attributes with key words

    // UIN may be follow by special key word "/ROC/"
    String keyWordForUIN = "/ROC/";
    if (str.length() > 2265 - 1 + keyWordForUIN.length()) {
      if (str.substring(2265 - 1, 2265 - 1 + keyWordForUIN.length()).equals(keyWordForUIN))
        doc.UIN = str.substring(2265 - 1 + keyWordForUIN.length());
    }

    doc.priority = String.format("%d", Long.parseLong(str.substring(485 - 1, 485 - 1 + 2)));
    doc.chargeOffDate = doc.edDate;

    if (str.length() >= 1663 - 1 + 3) {
      if (str.substring(1663 - 1, 1663 - 1 + 1).equals("*")) {
        doc.transKind = String.format("%d", Long.parseLong(str.substring(1663 - 1 + 1, 1663 - 1 + 3)));
      }
      else
        doc.transKind = "1";
    }

    // Try to substitute payer information
    boolean isBankPayer = false;
    doc.payerAccount = str.substring(381 - 1, 381 - 1 + 20);
    Account acc = App.accounts.getAccount(doc.payerAccount);
    if (acc != null) {
      if (acc.isInternal) {
        isBankPayer = true;
      }
      else {
        Client clt = App.clients.items.get(acc.clientId);
        if (clt != null) {
          if (clt.type == ClientType.PERSON || clt.type == ClientType.SELF_EMPLOYED) {
            doc.payerName = clt.lastName + (clt.firstNames != null? " " + clt.firstNames: "");
          }
          else {
            doc.payerName = clt.officialName;
          }
          doc.payerINN = clt.INN;
        }
        else
          isBankPayer = true;
      }
    }
    else {
      // Unknown account
      logger.warn("1201: Unknown account: " + doc.payerAccount);
      // doc.payerAccount = Constants.unclearedSettlementsDb;
      isBankPayer = true;
    }
    if (isBankPayer) {
      doc.payerName = Constants.ourBankName;
      doc.payerINN = Constants.ourBankINN;
    }

    doc.payerBankName = Constants.ourBankName;
    doc.payerBankBIC = Constants.ourBankBIC;
    doc.payerBankAccount = Constants.ourBankAccPass;

    doc.payeeName = str.substring(1828 - 1, 1828 - 1 + 140).trim();
    doc.payeeAccount = str.substring(1765 - 1, 1765 - 1 + 20);
    if (str.substring(1793 - 1, 1793 - 1 + 3).equals("INN")) {
      int pos1 = str.indexOf("/", 1793 - 1);
      int pos2 = str.indexOf(" ", 1793 - 1);
      int pos = Math.min(pos1, pos2);
      if (pos >= 0) {
        doc.payeeINN = str.substring(1793 - 1 + 3, pos);
        if (str.substring(pos, pos + 4).equals("/KPP")) {
          int endPos = str.indexOf(" ", pos);
          if (endPos >= 0) {
            doc.payeeCPP = str.substring(pos + 4, endPos);
          }
        }
      }
    }

    doc.payeeBankBIC = str.substring(1596 - 1, 1596 - 1 + 9);
    doc.payeeBankAccount = str.substring(1565 - 1, 1565 - 1 + 20);
    Bank bank = App.banks.items.get(doc.payeeBankBIC);
    if (bank != null) {
      doc.payeeBankName = bank.name;
    }

    return doc;
  }
}
