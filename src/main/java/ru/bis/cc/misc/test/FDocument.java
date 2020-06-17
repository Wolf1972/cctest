package ru.bis.cc.misc.test;

public class FDocument {

  // Financial document attributes, access from another classes is free - I don't want to create too many getters/setters
  String edNo;
  String edDate; // YYYY-MM-DD
  String referenceBq;
  String referenceMT103;
  String referenceFT14;
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
                 ", Amount: " + (amount != null? amount / 100 + "." + amount % 100 : "null") + System.lineSeparator() +
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
  String getTaxAttrs() {
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
      taxStr.append(sepChar); if (taxStatus != null) taxStr.append(taxStatus); // 101
      taxStr.append(sepChar); if (CBC != null) taxStr.append(CBC); // 104
      taxStr.append(sepChar); if (OCATO != null) taxStr.append(OCATO);
      taxStr.append(sepChar); if (taxPaytReason != null) taxStr.append(taxPaytReason);
      taxStr.append(sepChar); if (taxPeriod != null) taxStr.append(taxPeriod);
      taxStr.append(sepChar); if (taxDocNum != null) taxStr.append(taxDocNum);
      taxStr.append(sepChar); if (taxDocDate != null) taxStr.append(taxDocDate); // 109
      if (taxPaytKind != null) { // 110 - may be miss
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
   * @return boolean - objects are equal (true/false)
   */
  boolean equals(Object obj, int strictLevel) {
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

      if (Helper.isStrNullMismatch(docNum, compared.docNum)) str.append(oneMismatch("DocNum", docNum, compared.docNum));
      if (Helper.isStrNullMismatch(docDate, compared.docDate)) str.append(oneMismatch( "DocDate", docDate, compared.docDate));
      if ((amount != null && compared.amount == null) || (amount == null && compared.amount != null)) {
        str.append(" Amount not specified: "); str.append(amount); str.append(" against ");
        str.append(compared.amount);
        str.append(System.lineSeparator());
      }
      else if (!(amount != null && amount.equals(compared.amount))) {
        str.append(" Amount mismatch: "); str.append(amount); str.append(" against "); str.append(compared.amount);
        str.append(System.lineSeparator());
      }
      if (Helper.isStrNullMismatch(purpose, compared.purpose)) str.append(oneMismatch( "Purpose", purpose, compared.purpose));
      if (Helper.isStrNullMismatch(UIN, compared.UIN)) str.append(oneMismatch( "UIN", UIN, compared.UIN));

      if (strictLevel > 0) {
        if (Helper.isStrNullMismatch(priority, compared.priority)) str.append(oneMismatch( "Priority", priority, compared.priority));
        if (Helper.isStrNullMismatch(transKind, compared.transKind)) str.append(oneMismatch("TransKind", transKind, compared.transKind));
        if (strictLevel > 1) {
          if (Helper.isStrNullMismatch(chargeOffDate, compared.chargeOffDate)) str.append(oneMismatch("ChargeOff", chargeOffDate, compared.chargeOffDate));
          if (Helper.isStrNullMismatch(receiptDate, compared.receiptDate)) str.append(oneMismatch("ReceiptDate", receiptDate, compared.receiptDate));
        }
      }

      if (Helper.isStrNullMismatch(payerName, compared.payerName)) str.append(oneMismatch( "PayerName", payerName, compared.payerName));
      if (Helper.isStrNullMismatch(payerAccount, compared.payerAccount)) str.append(oneMismatch( "PayerAccount", payerAccount, compared.payerAccount));
      if (Helper.isStrNullMismatch(payerINN, compared.payerINN)) str.append(oneMismatch("PayerINN", payerINN, compared.payerINN));
      if (Helper.isStrNullMismatch(payerCPP, compared.payerCPP)) str.append(oneMismatch( "PayerCPP", payerCPP, compared.payerCPP));
      if (Helper.isStrNullMismatch(payerBankName, compared.payerBankName)) str.append(oneMismatch( "PayerBankName", payerBankName, compared.payerBankName));
      if (Helper.isStrNullMismatch(payerBankBIC, compared.payerBankBIC)) str.append(oneMismatch( "PayerBankBIC", payerBankBIC, compared.payerBankBIC));
      if (Helper.isStrNullMismatch(payerBankAccount, compared.payerBankAccount)) str.append(oneMismatch( "PayerBankAccount", payerBankAccount, compared.payerBankAccount));

      if (Helper.isStrNullMismatch(payeeName, compared.payeeName)) str.append(oneMismatch("PayeeName", payeeName, compared.payeeName));
      if (Helper.isStrNullMismatch(payeeAccount, compared.payeeAccount)) str.append(oneMismatch( "PayeeAccount", payeeAccount, compared.payeeAccount));
      if (Helper.isStrNullMismatch(payeeINN, compared.payeeINN)) str.append(oneMismatch( "PayeeINN", payeeINN, compared.payeeINN));
      if (Helper.isStrNullMismatch(payeeCPP, compared.payeeCPP)) str.append(oneMismatch( "PayeeCPP", payeeCPP, compared.payeeCPP));
      if (Helper.isStrNullMismatch(payeeBankName, compared.payeeBankName)) str.append(oneMismatch( "PayeeBankName", payeeBankName, compared.payeeBankName));
      if (Helper.isStrNullMismatch(payeeBankBIC, compared.payeeBankBIC)) str.append(oneMismatch( "PayeeBankBIC", payeeBankBIC, compared.payeeBankBIC));
      if (Helper.isStrNullMismatch(payeeBankAccount, compared.payeeBankAccount)) str.append(oneMismatch( "PayeeBankAccount", payeeBankAccount, compared.payeeBankAccount));

    }
    return str.toString();
  }

  /** Function describes one attribute mismatch
   *
   * @param attrName - name of attribute
   * @param val1 - compare value 1
   * @param val2 - compare value 2
   * @return common string with all mismatches
   */
  private StringBuilder oneMismatch(String attrName, String val1, String val2) {
    StringBuilder str = new StringBuilder();
    str.append(" "); str.append(attrName); str.append(" mismatch: ");
    str.append(val1); str.append(" against "); str.append(val2);
    str.append(System.lineSeparator());
    return str;
  }

  /** Function returns new reverse document by instance document (swaps payer and payee, bank payer and payee, changes document number and purpose)
   *
   * @return new reversed document
   */
  FDocument reverse() {

    FDocument rev = new FDocument();

    rev.edNo = generateEDNo(edNo, "1");
    rev.edDate = edDate;
    rev.isUrgent = isUrgent;

    rev.docNum = docNum;
    rev.docDate = docDate;
    rev.amount = amount;
    rev.purpose = "+++" + purpose;
    rev.UIN = UIN;
    rev.priority = priority;
    rev.chargeOffDate = chargeOffDate;
    rev.receiptDate = receiptDate;
    rev.transKind = transKind;

    rev.payerName = payeeName; // Swap payer
    rev.payerAccount = payeeAccount;
    rev.payerINN = payeeINN;
    rev.payerCPP = payeeCPP;

    rev.payerBankName = payeeBankName;  // Swap payer bank
    rev.payerBankBIC = payeeBankBIC;
    rev.payerBankAccount = payeeBankAccount;

    rev.payeeName = payerName; // Swap payee
    rev.payeeAccount = payerAccount;
    rev.payeeINN = payerINN;
    rev.payeeCPP = payerCPP;

    rev.payeeBankName = payerBankName;
    rev.payeeBankBIC = payerBankBIC;
    rev.payeeBankAccount = payerBankAccount;

    // We don't copy tax attributes - we didn't expect to receive incoming tax documents

    return rev;
  }

  /** Function generates unique EDNo - tries to add specified digit at left (9 digits at all as maximum)
   *
   * @param source - source EDNo
   * @param digit - left digit to create unique number (adds at left), e.g "1" - for reverse documents. "2" - for confirmations. etc
   * @return = new EDNo
   */
  String generateEDNo(String source, String digit) {

    String newNum = source; // Try to evaluate unique EDNo
    if (newNum == null) newNum = getId().toString();
    else if (newNum.length() == 0) newNum = getId().toString();
    if (newNum.length() < 9) newNum = digit + String.format("%8s", newNum).replace(" ", "0");
    else newNum = digit + newNum.substring(newNum.length() - 8);
    return newNum;
  }

}
