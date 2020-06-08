package ru.bis.cc.misc.test;

public class FDocument {

  // Financial document attributes, access from another classes is free - I don't want to create too many getters/setters
  String edNo;
  String edDate; // YYYY-MM-DD
  String referenceBq;
  String referenceMT103;
  boolean isUrgent;

  String docNum;
  String docDate; // YYYY-MM-DD
  Long amount;
  String purpose;
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

  FDocument() {
    isUrgent = false;
    isTax = false;
    docNum = "0";
    docDate = "1901-01-01";
  }

  Long getId() {
    return Long.parseLong(docNum); // Document ID, takes from document number
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
    if (taxStatus != null) tax += " TaxStatus: " + taxStatus;
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

  /** Compares current object with other
   *
   * @param obj - object to compare
   * @param strictLevel - strict level
   *    0 - base compare, suits for compare all documents (compares only common fields)
   *    1 - strict compare, suits for compare documents loads from FT14 (common fields and priority, transaction kind)
   *    2 - more strict compare with all fields, suits for documents loads from UFEBS only
   * @return: boolean - objects are equal (true/false)
   */
  public boolean equals(Object obj, int strictLevel) {
    if (obj == null) return false;
    if (obj.getClass() != FDocument.class) return false;
    else {
      FDocument compared = (FDocument) obj;
      if (isUrgent != compared.isUrgent) return false;
      if (isTax != compared.isTax) return false;

      if (Helper.isStrNullMismatch(docNum, compared.docNum)) return false;
      if (Helper.isStrNullMismatch(docDate, compared.docDate)) return false;
      if ((amount != null && compared.amount == null) || (amount == null && compared.amount != null)) return false;
      if (!(amount != null && amount.equals(compared.amount))) return false;
      if (Helper.isStrNullMismatch(purpose, compared.purpose)) return false;
      if (Helper.isStrNullMismatch(UIN, compared.UIN)) return false;

      if (strictLevel > 0) {
        if (Helper.isStrNullMismatch(priority, compared.priority)) return false;
        if (Helper.isStrNullMismatch(transKind, compared.transKind)) return false;
        if (strictLevel > 1) {
          if (Helper.isStrNullMismatch(chargeOffDate, compared.chargeOffDate)) return false;
          if (Helper.isStrNullMismatch(receiptDate, compared.receiptDate)) return false;
        }
      }

      if (Helper.isStrNullMismatch(payerName, compared.payerName)) return false;
      if (Helper.isStrNullMismatch(payerAccount, compared.payerAccount)) return false;
      if (Helper.isStrNullMismatch(payerINN, compared.payerINN)) return false;
      if (Helper.isStrNullMismatch(payerCPP, compared.payerCPP)) return false;

      if (Helper.isStrNullMismatch(payerBankName, compared.payerBankName)) return false;
      if (Helper.isStrNullMismatch(payerBankBIC, compared.payerBankBIC)) return false;
      if (Helper.isStrNullMismatch(payerBankAccount, compared.payerBankAccount)) return false;

      if (Helper.isStrNullMismatch(payeeName, compared.payeeName)) return false;
      if (Helper.isStrNullMismatch(payeeAccount, compared.payeeAccount)) return false;
      if (Helper.isStrNullMismatch(payeeINN, compared.payeeINN)) return false;
      if (Helper.isStrNullMismatch(payeeCPP, compared.payeeCPP)) return false;

      if (Helper.isStrNullMismatch(payeeBankName, compared.payeeBankName)) return false;
      if (Helper.isStrNullMismatch(payeeBankBIC, compared.payeeBankBIC)) return false;
      if (Helper.isStrNullMismatch(payeeBankAccount, compared.payeeBankAccount)) return false;

      if (Helper.isStrNullMismatch(taxStatus, compared.taxStatus)) return false;
      if (Helper.isStrNullMismatch(CBC, compared.CBC)) return false;
      if (Helper.isStrNullMismatch(OCATO, compared.OCATO)) return false;
      if (Helper.isStrNullMismatch(taxPaytReason, compared.taxPaytReason)) return false;
      if (Helper.isStrNullMismatch(taxPeriod, compared.taxPeriod)) return false;
      if (Helper.isStrNullMismatch(taxDocNum, compared.taxDocNum)) return false;
      if (Helper.isStrNullMismatch(taxDocDate, compared.taxDocDate)) return false;
      if (Helper.isStrNullMismatch(taxPaytKind, compared.taxPaytKind)) return false;

    }
    return true;
  }

  /** Returns string with compare details
   *
   * @param obj object to compare
   * @param strictLevel - strict level
   *    0 - base compare, suits for compare all documents (compares only common fields)
   *    1 - strict compare, suits for compare documents loads from FT14 (common fields and priority, transaction kind)
   *    2 - more strict compare with all fields, suits for documents loads from UFEBS only   *
   * @return string with list of mismatches
   */
  String mismatchDescribe(Object obj, int strictLevel) {
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

      if (Helper.isStrNullMismatch(docNum, compared.docNum)) oneMismatch(str, "DocNum", docNum, compared.docNum);
      if (Helper.isStrNullMismatch(docDate, compared.docDate)) oneMismatch(str, "DocDate", docDate, compared.docDate);
      if ((amount != null && compared.amount == null) || (amount == null && compared.amount != null)) {
        str.append(" Amount not specified: "); str.append(amount); str.append(" against ");
        str.append(compared.amount);
        str.append(System.lineSeparator());
      }
      else if (!(amount != null && amount.equals(compared.amount))) {
        str.append(" Amount mismatch: "); str.append(amount); str.append(" against "); str.append(compared.amount);
        str.append(System.lineSeparator());
      };
      if (Helper.isStrNullMismatch(purpose, compared.purpose)) oneMismatch(str, "Purpose", purpose, compared.purpose);
      if (Helper.isStrNullMismatch(UIN, compared.UIN)) oneMismatch(str, "UIN", UIN, compared.UIN);

      if (strictLevel > 0) {
        if (Helper.isStrNullMismatch(priority, compared.priority)) oneMismatch(str, "Priority", priority, compared.priority);
        if (Helper.isStrNullMismatch(transKind, compared.transKind)) oneMismatch(str, "TransKind", transKind, compared.transKind);
        if (strictLevel > 1) {
          if (Helper.isStrNullMismatch(chargeOffDate, compared.chargeOffDate)) oneMismatch(str, "ChargeOff", chargeOffDate, compared.chargeOffDate);
          if (Helper.isStrNullMismatch(receiptDate, compared.receiptDate)) oneMismatch(str, "ReceiptDate", receiptDate, compared.receiptDate);
        }
      }

      if (Helper.isStrNullMismatch(payerName, compared.payerName)) oneMismatch(str, "PayerName", payerName, compared.payerName);
      if (Helper.isStrNullMismatch(payerAccount, compared.payerAccount)) oneMismatch(str, "PayerAccount", payerAccount, compared.payerAccount);
      if (Helper.isStrNullMismatch(payerINN, compared.payerINN)) oneMismatch(str, "PayerINN", payerINN, compared.payerINN);
      if (Helper.isStrNullMismatch(payerCPP, compared.payerCPP)) oneMismatch(str, "PayerCPP", payerCPP, compared.payerCPP);
      if (Helper.isStrNullMismatch(payerBankName, compared.payerBankName)) oneMismatch(str, "PayerBankName", payerBankName, compared.payerBankName);
      if (Helper.isStrNullMismatch(payerBankBIC, compared.payerBankBIC)) oneMismatch(str, "PayerBankBIC", payerBankBIC, compared.payerBankBIC);
      if (Helper.isStrNullMismatch(payerBankAccount, compared.payerBankAccount)) oneMismatch(str, "PayerBankAccount", payerBankAccount, compared.payerBankAccount);

      if (Helper.isStrNullMismatch(payeeName, compared.payeeName)) oneMismatch(str, "PayeeName", payeeName, compared.payeeName);
      if (Helper.isStrNullMismatch(payeeAccount, compared.payeeAccount)) oneMismatch(str, "PayeeAccount", payeeAccount, compared.payeeAccount);
      if (Helper.isStrNullMismatch(payeeINN, compared.payeeINN)) oneMismatch(str, "PayeeINN", payeeINN, compared.payeeINN);
      if (Helper.isStrNullMismatch(payeeCPP, compared.payeeCPP)) oneMismatch(str, "PayeeCPP", payeeCPP, compared.payeeCPP);
      if (Helper.isStrNullMismatch(payeeBankName, compared.payeeBankName)) oneMismatch(str, "PayeeBankName", payeeBankName, compared.payeeBankName);
      if (Helper.isStrNullMismatch(payeeBankBIC, compared.payeeBankBIC)) oneMismatch(str, "PayeeBankBIC", payeeBankBIC, compared.payeeBankBIC);
      if (Helper.isStrNullMismatch(payeeBankAccount, compared.payeeBankAccount)) oneMismatch(str, "PayeeBankAccount", payeeBankAccount, compared.payeeBankAccount);

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
  private StringBuilder oneMismatch(StringBuilder str, String attrName, String val1, String val2) {
    str.append(" "); str.append(attrName); str.append(" mismatch: ");
    str.append(val1); str.append(" against "); str.append(val2);
    str.append(System.lineSeparator());
    return str;
  }

}
