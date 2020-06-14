package ru.bis.cc.misc.test;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MT103Parser extends SWIFTParser {

  MT103Parser() {
    super();
    expectedFields = new String[]{"20", "32A", "50K", "52A", "53B", "57D", "59", "70", "72", "77B"};
  }

  /** Loads object from MT103
   *
   * @param str = string with MT103 message
   * @return financial document
   */
  FDocument fromString(String str) {

    String innKeyWord = "INN";
    String regExpINN = innKeyWord + "\\d{12}\\D|"+ innKeyWord + "\\d{10}\\D|" + innKeyWord + "\\d{5}\\D" +
            innKeyWord + "\\d{12}$|"+ innKeyWord + "\\d{10}$|" + innKeyWord + "\\d{5}$"; // to take into account that INN may takes place in the end of the string
    Pattern patternINN = Pattern.compile(regExpINN);

    FDocument doc = new FDocument();

    int posMessage = str.indexOf("{2:");
    if (posMessage >= 0) {
      int endPos = str.indexOf("}", posMessage);
      if (endPos >= 0) {
        char urgent = str.charAt(endPos - 1);
        if (urgent == 'U') doc.isUrgent = true;
      }
    }

    String blockHeader = "{4:";
    String blockTrailer = "-}";
    posMessage = str.indexOf(blockHeader);

    StringBuilder tagContent = new StringBuilder();

    if (posMessage >= 0) {

      posMessage += blockHeader.length();
      if (str.substring(posMessage, posMessage + System.lineSeparator().length()).equals(System.lineSeparator()))
        posMessage += System.lineSeparator().length(); // skip possible CRLF from "{4:"

      String tagName = "";
      String newTag = "";
      boolean startOfMessage = true;
      boolean endOfMessage = false;
      String purposePart1 = "";
      String purposePart2 = "";

      while (posMessage < str.length()) {

        int endString = str.indexOf(System.lineSeparator(), posMessage); // TODO split()? Parse message to strings
        String oneLine;
        if (endString >= 0) {
          oneLine = str.substring(posMessage, endString);
          posMessage = endString + System.lineSeparator().length();
        } else { // last string of message
          oneLine = str.substring(posMessage);
          posMessage = str.length();
        }
        endString = oneLine.indexOf(blockTrailer);
        if (endString >= 0) {
          oneLine = oneLine.substring(0, endString);
          endOfMessage = true;
        }

        if (oneLine.length() > 0 || endOfMessage) { // Process message string by string

          boolean isNewTag = false;
          // Try to find next tag (NB: if message contains unknown tag, then it will be added to last known tag)
          for (String tagNo : expectedFields) {
            if (oneLine.startsWith(":" + tagNo + ":")) {
              isNewTag = true;
              newTag = tagNo;
              break;
            }
          }

          if (isNewTag || endOfMessage) {

            // Process previous tag
            if (!startOfMessage) {
              if (tagContent.length() > 0) {

                if (tagName.equals("20")) {
                  doc.docNum = tagContent.toString();
                }

                else if (tagName.equals("32A")) {
                  doc.docDate = Helper.getXMLDate(tagContent.substring(0, 6));
                  doc.amount = Long.parseLong(tagContent.substring(9).replace(".", ""));
                }

                else if (tagName.equals("50K") || tagName.equals("59")) {
                  String rawClientName = "";
                  String accountNo = null;
                  String inn = null;
                  StringBuilder cpp = new StringBuilder();
                  StringBuilder clientName = new StringBuilder();
                  if (tagContent.toString().startsWith("/")) {
                    int crlf = tagContent.indexOf(System.lineSeparator());
                    if (crlf == 0) crlf = tagContent.length();
                    accountNo = tagContent.substring(1, crlf);
                    if (crlf < tagContent.length() + System.lineSeparator().length()) // Have we client name at all?
                      rawClientName = tagContent.substring(crlf + System.lineSeparator().length()).replace(System.lineSeparator(), "");
                  }
                  else { // all strings contain client name only, w/o account (with INN inside, possible)
                    rawClientName = tagContent.toString().replace(System.lineSeparator(), "");
                  }
                  int innPos = -1;
                  int innEnd = -1;
                  try { // Search for INN in any place of client name with regex
                    Matcher matcher = patternINN.matcher(rawClientName);
                    if (matcher.find()) {
                      innPos = matcher.start();
                      if (innPos >= 0) innEnd = matcher.end();
                      if (innEnd < rawClientName.length()) innEnd--; // When INN in the middle of string - it matches with yet another symbol after, need to account it
                      inn = rawClientName.substring(innPos + innKeyWord.length(), innEnd); // INN extracts without CPP
                      if (rawClientName.substring(innEnd, innEnd + 4).equals("/KPP")) {
                        // Try to extract CPP just after INN - it may be several next digits (not more than 9)
                        innEnd += 4;
                        for (; innEnd < rawClientName.length(); innEnd++) {
                          char ch = rawClientName.charAt(innEnd);
                          if (Character.isDigit(ch)) cpp.append(ch);
                          else {
                            if (ch != ' ') innEnd--; // Alphabet char is a part of client name
                            break;
                          }
                          if (cpp.length() >= 9) break;
                        }
                      }
                    }
                  }
                  catch (IllegalStateException | IllegalArgumentException | IndexOutOfBoundsException e) {
                    // Just do not parse INN from string, nothing else matters
                  }
                  if (innPos >= 0 && innEnd >= 0) {
                    if (innPos > 0) clientName.append(rawClientName, 0, innPos);
                    clientName.append(rawClientName.substring(innEnd + 1).trim());
                  }
                  else
                    clientName.append(rawClientName.trim());
                  if (tagName.equals("50K")) {
                    doc.payerAccount = accountNo;
                    doc.payerINN = inn;
                    if (cpp.length() > 0) doc.payerCPP = cpp.toString();
                    doc.payerName = clientName.toString();
                  }
                  else {
                    doc.payeeAccount = accountNo;
                    doc.payeeINN = inn;
                    if (cpp.length() > 0) doc.payeeCPP = cpp.toString();
                    doc.payeeName = clientName.toString();
                  }
                }

                else if (tagName.equals("52A")) {
                  int pos = tagContent.indexOf("BIK");
                  if (pos >= 0) {
                    int end = tagContent.indexOf(System.lineSeparator(), pos);
                    if (end < 0) end = tagContent.length();
                    doc.payerBankBIC = tagContent.substring(pos + 3, end);
                  }
                }

                else if (tagName.equals("53B")) {
                  if (tagContent.substring(0, 1).equals("/")) {
                    int end = tagContent.indexOf(System.lineSeparator());
                    if (end < 0) end = tagContent.length();
                    doc.payerBankAccount = tagContent.substring(1, end);
                  }
                }

                else if (tagName.equals("57D")) {
                  int pos = tagContent.indexOf("/");
                  if (pos >= 0) {
                    int end = tagContent.indexOf(System.lineSeparator(), pos);
                    if (end < 0) end = tagContent.length();
                    doc.payeeBankAccount = tagContent.substring(pos + 1, end);
                  }
                  pos = tagContent.indexOf("BIK");
                  if (pos >= 0) {
                    int end = tagContent.indexOf(System.lineSeparator(), pos);
                    if (end < 0) end = tagContent.length();
                    doc.payeeBankBIC = tagContent.substring(pos + 3, end);
                  }
                }

                else if (tagName.equals("70")) {
                  purposePart1 = tagContent.toString().replace(System.lineSeparator(), "");
                }

                else if (tagName.equals("72")) {
                  purposePart2 = tagContent.toString().replace(System.lineSeparator(), "");
                }

                else if (tagName.equals("77B")) {
                  doc.referenceMT103 = tagContent.toString();
                }
              }
            }

            // Start next tag
            if (oneLine.length() > 0) {
              tagName = newTag;
              tagContent.setLength(0);
              tagContent.append(oneLine.substring(newTag.length() + 2)); // ":" + tagName + ":"
            }
          }
          else {
            tagContent.append(System.lineSeparator());
            tagContent.append(oneLine);
          }
        }
        if (startOfMessage) startOfMessage = false;
      } // end of main cycle

      if (purposePart1.length() > 0) {
        // purposePart1 = "((VO12345))//123456789/01/1234567890123456789012345/1234567/ПЕ/КВ.01.20/123/12.05.2020/1/" + purposePart1 + "УИН0///"; // Inplace test string
        // purposePart1 = "((VO12345))//123456789/01/1234567890123456789012345/1234567/ПЕ/КВ.01.20/123/12.05.2020/УИН0///" + purposePart1; // Inplace test string
        // Try to extract tax attributes
        int posStart = 0;
        if (purposePart1.substring(0, 2).equals("((")) { // purpose contains VO code - it places before tax attributes
          posStart = purposePart1.indexOf("))");
          if (posStart >= 0) posStart += 2;
          else posStart = 0;
        }
        String taxAttrBegin = purposePart1.substring(posStart, posStart + 2);
        int taxAttrLen = 0;
        if (taxAttrBegin.equals("//") || taxAttrBegin.equals("\\\\")) {
          String taxAttrSeparator = taxAttrBegin.substring(0, 1);
          StringTokenizer tokenizer = new StringTokenizer(purposePart1.substring(posStart + 2), taxAttrSeparator);
          int i = 0;
          taxAttrLen = 2;
          doc.isTax = true;
          while (tokenizer.hasMoreTokens() && i < 9) {
            String token = tokenizer.nextToken();
            if (i == 0) doc.payerCPP = token;
            if (i == 1) doc.taxStatus = token;
            if (i == 2) doc.CBC = token;
            if (i == 3) doc.OCATO = token;
            if (i == 4) doc.taxPaytReason = token;
            if (i == 5) doc.taxPeriod = token;
            if (i == 6) doc.taxDocNum = token;
            if (i == 7) doc.taxDocDate = token;
            if (i == 8) if (token.length() <= 1) doc.taxPaytKind = token;
            else break; // Field 110 may not be specified and final separated may be missed
            i++;
            taxAttrLen += token.length() + 1;
          }
        }
        if (taxAttrLen == 0) doc.purpose = purposePart1 + purposePart2;
        else
          doc.purpose = purposePart1.substring(0, posStart) + purposePart1.substring(posStart + taxAttrLen) + purposePart2;
        // Try to extract UIN
        posStart = doc.purpose.indexOf("УИН");
        if (posStart < 0) posStart = doc.purpose.indexOf("UIN");
        if (posStart >= 0) {
          int posEnd = doc.purpose.indexOf("///", posStart);
          if (posEnd >= 0) {
            doc.UIN = doc.purpose.substring(posStart + 3, posEnd);
            doc.purpose = doc.purpose.substring(0, posStart) + doc.purpose.substring(posEnd + 3);
          }
        }
      }
    }
    else {
      return null;
    }
    return doc;
  }

  /** Creates MT103 string from object
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

    str.append(":20:");
    str.append(doc.docNum);
    str.append(System.lineSeparator());

    str.append(":32A:");
    str.append(Helper.getSWIFTDate(doc.docDate));
    str.append("RUB");
    str.append(String.format("%d.%02d", doc.amount / 100, doc.amount % 100));
    str.append(System.lineSeparator());

    str.append(":50K:");
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
      str.append(":52A:");
      str.append("BIK");
      str.append(doc.payerBankBIC);
      str.append(System.lineSeparator());
    }

    if (doc.payerBankAccount != null) {
      str.append(":53B:");
      str.append("/");
      str.append(doc.payerBankAccount);
      str.append(System.lineSeparator());
    }

    str.append(":57D:");
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

    str.append(":59:");
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
    str.append(":70:");
    iPos = 0;
    for (int j = 0; j <= 6; j++) {
      if (j == 4) {
        str.append(":72:");
      }
      str.append(locPurpose.substring(iPos, Math.min(iPos + 35, locPurpose.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > locPurpose.length()) break;
    }

    str.append(":77B:");
    str.append("REF"); str.append(doc.getId());
    str.append(System.lineSeparator());

    str.append("-}");

    return str.toString();
  }

}
