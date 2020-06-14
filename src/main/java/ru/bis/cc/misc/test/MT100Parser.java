package ru.bis.cc.misc.test;

import java.util.regex.Pattern;

class MT100Parser extends SWIFTParser {

  public MT100Parser() {
    super();
    expectedFields = new String[]{"20", "32A", "50", "52D", "59", "70", "72"};
  }

  /** Loads object from MT100
   *
   * @param str = string with MT100 message
   * @return financial document
   */
  FDocument fromString(String str) {

    String innKeyWord = "INN";
    String regExpINN = innKeyWord + "\\d{12}\\D|"+ innKeyWord + "\\d{10}\\D|" + innKeyWord + "\\d{5}\\D" +
            innKeyWord + "\\d{12}$|"+ innKeyWord + "\\d{10}$|" + innKeyWord + "\\d{5}$"; // to take into account that INN may takes place in the end of the string
    Pattern patternINN = Pattern.compile(regExpINN);

    FDocument doc = new FDocument();
    readHeader(str, doc);

    String[] messageLines = splitMessage(str);

    read20(messageLines, doc);
    read32A(messageLines, doc);
    readCounterparty(messageLines, doc, "50");
    readBank(messageLines, doc, "52D");
    readCounterparty(messageLines, doc, "59");
    readPurpose(messageLines, doc);

    return doc;
  }

  /** Creates MT100 string from object
   * @param doc - financial document
   * @return MT103 string (unicode), returns null if error or filter
   */
  String toString(FDocument doc) {
    int iPos; // current position when we split text to multistring text fields
    int iStr; // current string number when we split text to multistring text fields

    StringBuilder participant = new StringBuilder(); // string uses to assemle multistring text fields

    StringBuilder str = new StringBuilder();
    str.append("{4:");
    str.append(System.lineSeparator());

    str.append("20:");
    str.append(doc.docNum);
    str.append(System.lineSeparator());

    str.append("32A:");
    str.append(Helper.getSWIFTDate(doc.docDate));
    str.append("RUB");
    str.append(String.format("%d.%02d", doc.amount / 100, doc.amount % 100));
    str.append(System.lineSeparator());

    str.append("50K:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (doc.payerAccount != null) { str.append("/"); str.append(doc.payerAccount); str.append(System.lineSeparator()); iStr = 1; } // 1st string
    if (doc.payerINN != null) {
      participant.append("INN"); participant.append(doc.payerINN);
      if (doc.payerName != null) participant.append(" "); // Separate INN by space with next client name
    }
    if (doc.payerName != null) participant.append(doc.payerName);
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length() - 1) break;
    }

    if (doc.payerBankBIC != null) {
      str.append("52A:");
      str.append("BIK");
      str.append(doc.payerBankBIC);
      str.append(System.lineSeparator());
    }

    if (doc.payerBankAccount != null) {
      str.append("53B:");
      str.append("/");
      str.append(doc.payerBankAccount);
      str.append(System.lineSeparator());
    }

    str.append("57D:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (doc.payeeBankAccount != null) { str.append("/"); str.append(doc.payeeBankAccount); str.append(System.lineSeparator()); iStr = 1; } // 1st string
    if (doc.payeeBankBIC != null) {
      participant.append("BIK"); participant.append(doc.payeeBankBIC);
      if(doc.payeeBankName != null) participant.append(" "); // Separate BIC by space with next bank name
    }
    if (doc.payeeBankName != null) participant.append(doc.payeeBankName);
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length() - 1) break;
    }

    str.append("59:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (doc.payeeAccount != null) { str.append("/"); str.append(doc.payeeAccount); str.append(System.lineSeparator()); iStr = 1; } // 1st string
    if (doc.payeeINN != null) {
      participant.append("INN"); participant.append(doc.payeeINN);
      if (doc.payeeName != null && doc.payeeCPP == null) participant.append(" "); // Separate INN by space with next client name
    }
    if (doc.payeeCPP != null) {
      participant.append("/KPP"); participant.append(doc.payeeCPP);
      if (doc.payeeName != null) participant.append(" "); // Separate INN by space with next client name
    }
    if (doc.payeeName != null) participant.append(doc.payeeName);

    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length()) break;
    }

    // Build tax attributes string It has to be add to payment purpose
    StringBuilder locPurpose = new StringBuilder(); // Payment purpose that saves into MT103 - it difference with document purpose (contains tax attributes)
    locPurpose.append(doc.getTaxAttrs());
    locPurpose.append(doc.purpose.replace("{", "((").replace("}", "))")); // Replace "{" and "}" to "((" and "))"
    if (doc.UIN != null) {
      locPurpose.append("УИН"); locPurpose.append(doc.UIN); locPurpose.append("///");
    }
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
    str.append("REF"); str.append(doc.getId());
    str.append(System.lineSeparator());

    str.append("-}");

    return str.toString();
  }

}
