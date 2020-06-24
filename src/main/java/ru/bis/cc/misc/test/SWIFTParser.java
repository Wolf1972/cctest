package ru.bis.cc.misc.test;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SWIFTParser extends Parser {

  String[] expectedFields; // List of expected SWIFT fields {"20", "32A" etc}, fills in inherit constructor

  /** Function reads block {2:} and extracts isUrgent
   *
   * @param str - string with message
   * @param doc - document
   */
  void readHeader(String str, FDocument doc) {
    int posMessage = str.indexOf("{2:");
    if (posMessage >= 0) {
      int endPos = str.indexOf("}", posMessage);
      if (endPos >= 0) {
        char urgent = str.charAt(endPos - 1);
        if (urgent == 'U') doc.isUrgent = true;
      }
    }
  }

  /** Function splits block {4:} of message for separated strings array or splits any message for separated strings
   *
   * @param str - string with all message
   * @return - strings array or empty array (if block {4:} has not found)
   */
  String[] splitMessage(String str) {

    String blockHeader = "{4:";
    String blockTrailer = "-}";
    String altBlockTrailer = "}$";
    int startOfMessage = str.indexOf(blockHeader);
    int endOfMessage = str.indexOf(blockTrailer, startOfMessage);
    if (endOfMessage < 0) endOfMessage = str.indexOf(altBlockTrailer, startOfMessage);
    String[] empty = new String[]{};

    StringBuilder tagContent = new StringBuilder();

    if (startOfMessage >= 0 && endOfMessage > startOfMessage) {
      return str.substring(startOfMessage + 3, endOfMessage).replace("\r", "")
              .split("\n"); // Do not use System.lineSeparator, may be file UNIX-style
    }
    else {
      return str.replace("\r", "")
              .split("\n"); // Do not use System.lineSeparator, may be file UNIX-style
    }
  }

  /** Function returns array of strings with one tag content (:20, :32A etc)
   *
   * @param message - array of message strings
   * @param tagNo - tag code (without ":", e.g. "32A")
   * @return = list of strings with tag content
   */
  ArrayList<String> getTag(String[] message, String tagNo) {
     ArrayList<String> content = new ArrayList<>();
     int tagIndex = -1;
     for (int i = 0; i < expectedFields.length; i++) {
       if (expectedFields[i].equals(tagNo)) {
         tagIndex = i;
         break;
       }
     }

     if (tagIndex >= 0) {
       boolean insideTag = false;
       for (String line : message) {
         if (line.startsWith(":" + tagNo + ":")) {
           content.add(line.substring(tagNo.length() + 2));
           insideTag = true;
         }
         else {
           if (insideTag) {
             for (int j = tagIndex; j < expectedFields.length; j++) {// Looking for start next tag
               if (line.startsWith(":" + expectedFields[j] + ":")) {
                 insideTag = false;
               }
             }
             if (insideTag) { // Inside tag yet
               int pos = line.indexOf("-}"); // End of message
               if (pos >= 0) line = line.substring(0, pos);
               if (line.length() > 0) {
                 content.add(line);
               }
             }
             else
               break;
           }
         }
       }
     }
     return content;
  }

  /** Function process tag 20
   *
   * @param message - string array with message
   * @param doc - document
   */
  void read20(String[] message, FDocument doc) {
    ArrayList<String> tag;
    tag = getTag(message, "20");
    if (tag.size() > 0) {
      doc.docNum = tag.get(0);
    }
  }

  /** Function process tag 32A
   *
   * @param message - string array with message
   * @param doc - document
   */
  void read32(String[] message, FDocument doc) {
    ArrayList<String> tag;
    tag = getTag(message, "32A");
    if (tag.size() > 0) {
      String line = tag.get(0);
      doc.docDate = Helper.getXMLDate(line.substring(0, 6));
      String sum = line.substring(9).replace(".", "").replace(",", "");
      doc.amount = Long.parseLong(sum);
    }
  }

  /** Function for process any counterparty tag (payer, payee) - 50, 50K, 59
   *
   * Structure: [/account_no]
   *            name line 1
   *            ...
   *            [name line n]
   * INN, CPP inside name line with keywords
   * @param message - string array with message
   * @param doc - document
   * @param tagName - tag name (50, 50K, 59)
   */
  void readCounterparty(String[] message, FDocument doc, String tagName) {

    ArrayList<String> tag;
    tag = getTag(message, tagName);

    if (tag.size() > 0) {

      String innKeyWord = "INN";
      String regExpINN = innKeyWord + "\\d{12}\\D|"+ innKeyWord + "\\d{10}\\D|" + innKeyWord + "\\d{5}\\D" +
              innKeyWord + "\\d{12}$|"+ innKeyWord + "\\d{10}$|" + innKeyWord + "\\d{5}$"; // to take into account that INN may takes place in the end of the string
      Pattern patternINN = Pattern.compile(regExpINN);

      StringBuilder rawClientName = new StringBuilder();
      String accountNo = null;
      String inn = null;
      StringBuilder cpp = new StringBuilder();
      StringBuilder clientName = new StringBuilder();

      String line = tag.get(0);
      int startNameString = 0;
      if (line.startsWith("/")) { // Has account number?
        accountNo = line.substring(1);
        startNameString = 1;
      }
      for (int i = startNameString; i < tag.size(); i++) {
        rawClientName.append(tag.get(i));
      }

      // Try to extract INN, CPP from client name
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
              if (cpp.length() >= 9) { innEnd++; break; }
            }
          }
        }
      }
      catch (IllegalStateException | IllegalArgumentException | IndexOutOfBoundsException e) {
        // Just do not parse INN from string, nothing else matters
      }
      if (innPos >= 0 && innEnd >= 0) {
        if (innPos > 0) clientName.append(rawClientName, 0, innPos);
        clientName.append(rawClientName.substring(innEnd).trim());
      }
      else
        clientName.append(rawClientName.toString().trim());

      if (tagName.startsWith("50")) {
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
  }

  /** Function for process any bank tag (payer bank, payee bank) - 52D, 57D
   *
   * Structure: [/account_no]
   *            [BIK]
   *            name line 1
   *            ...
   *            [name line n]
   * @param message - string array with message
   * @param doc - document
   * @param tagName - tag name (52D, 57D)
   */
  void readBank(String[] message, FDocument doc, String tagName) {

    ArrayList<String> tag;
    tag = getTag(message, tagName);

    if (tag.size() > 0) {
      String bicKeyWord = "BIK";
      String regExpINN = bicKeyWord + "\\d{9}\\D|"+ bicKeyWord + "\\d{9}$"; // to take into account that BIK may takes place in the end of the string
      Pattern patternBIC = Pattern.compile(regExpINN);

      StringBuilder rawBankName = new StringBuilder();
      StringBuilder bankName = new StringBuilder();
      String accountNo = null;
      String bic = null;

      String line = tag.get(0);
      int startNameString = 0;
      if (line.startsWith("/")) { // Has account number?
        accountNo = line.substring(1);
        startNameString = 1;
      }
      for (int i = startNameString; i < tag.size(); i++) {
        rawBankName.append(tag.get(i));
      }

      // Try to extract BIC from bank name
      int bicPos = -1;
      int bicEnd = -1;
      try { // Search for INN in any place of client name with regex
        Matcher matcher = patternBIC.matcher(rawBankName);
        if (matcher.find()) {
          bicPos = matcher.start();
          if (bicPos >= 0) bicEnd = matcher.end();
          if (bicEnd < rawBankName.length()) bicEnd--; // When BIK in the middle of string - it matches with yet another symbol after, need to account it
          bic = rawBankName.substring(bicPos + bicKeyWord.length(), bicEnd); // Extracts BIC
        }
      }
      catch (IllegalStateException | IllegalArgumentException | IndexOutOfBoundsException e) {
        // Just do not parse INN from string, nothing else matters
      }
      if (bicPos >= 0 && bicEnd >= 0) {
        if (bicPos > 0) bankName.append(rawBankName, 0, bicPos);
        bankName.append(rawBankName.substring(bicEnd).trim());
      }
      else
        bankName.append(rawBankName.toString().trim());

      if (tagName.startsWith("52")) {
        doc.payerBankAccount = accountNo;
        doc.payerBankBIC = bic;
        if (bankName.length() > 0) doc.payerBankName = bankName.toString();
      }
      else {
        doc.payeeBankAccount = accountNo;
        doc.payeeBankBIC = bic;
        if (bankName.length() > 0) doc.payeeBankName = bankName.toString();
      }
    }
  }

  /** Function process payment purpose tags (70, 72) and extracts tax attributes and attributes with keywords (UIN)
   *
   * @param message - message strings array
   * @param doc - document
   */
  void readPurpose(String[] message, FDocument doc) {

    StringBuilder purpose = new StringBuilder();
    ArrayList<String> tag = getTag(message, "70");
    for (String str : tag) purpose.append(str);
    tag = getTag(message, "72");
    boolean firstStr = true;
    for (String str : tag) {
      if (firstStr) {
        firstStr = false;
        // Try to extract charge off date (/REC/DD.MM.YYYY)
        int pos = str.indexOf("/REC/");
        if (pos >= 0) {
          String date = str.substring(pos + 5, Math.min(pos + 15, str.length()));
          str = str.substring(0, pos) + str.substring(Math.min(pos + 15, str.length()));
        }
      }
      purpose.append(str);
    }

    if (purpose.length() > 0) {
      // Try to extract tax attributes
      int posStart = 0;
      if (purpose.substring(0, 2).equals("((")) { // purpose contains VO code - it places before tax attributes
        posStart = purpose.indexOf("))");
        if (posStart >= 0) posStart += 2;
        else posStart = 0;
      }
      String taxAttrBegin = purpose.substring(posStart, posStart + 2);
      int taxAttrLen = 0;
      if (taxAttrBegin.equals("//") || taxAttrBegin.equals("\\\\")) {
        String taxAttrSeparator = taxAttrBegin.substring(0, 1);
        StringTokenizer tokenizer = new StringTokenizer(purpose.substring(posStart + 2), taxAttrSeparator);
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
      if (taxAttrLen == 0) doc.purpose = purpose.toString();
      else
        doc.purpose = purpose.substring(0, posStart) + purpose.substring(posStart + taxAttrLen);

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

  /** Function process tag 53B
   *
   * @param message - string array with message
   * @param doc - document
   */
  void read53(String[] message, FDocument doc) {
    ArrayList<String> tag;
    tag = getTag(message, "53B");
    if (tag.size() > 0) {
      String tagContent = tag.get(0);
      if (tagContent.substring(0, 1).equals("/")) {
        int end = tagContent.indexOf(System.lineSeparator());
        if (end < 0) end = tagContent.length();
        doc.payerBankAccount = tagContent.substring(1, end);
      }
    }
  }

  /** Function process tag 77B
   *
   * @param message - string array with message
   * @param doc - document
   */
  void read77(String[] message, FDocument doc) {
    ArrayList<String> tag;
    tag = getTag(message, "77B");
    if (tag.size() > 0) {
      doc.referenceMT103 = tag.get(0);
    }
  }

  /** Function process tag 61 in statement operation (MT940)
   *  :61:0102080208D75,80NTRF00110//044525225
   * @param message - string array with message
   * @param doc - document
   */
  void read61(String[] message, FDocument doc) {
    ArrayList<String> tag;
    tag = getTag(message, "61");
    if (tag.size() > 0) {
      String oper = tag.get(0);
      doc.edDate = Helper.getXMLDate(oper.substring(0, 7));
      doc.docDate = doc.edDate;
      char opType = oper.charAt(9);
      int end = oper.indexOf("NTRF", 10);
      if (end >= 0) {
        String amoStr = oper.substring(11, end);
        doc.amount = Helper.getLongFromDecimal(amoStr);
        int pos = oper.indexOf("//", end);
        if (pos >= 0) {
          doc.docNum = oper.substring(end + "NTRF".length(), pos);
          String bic = oper.substring(pos + 2);
          if (opType == 'C') doc.payerBankBIC = bic;
          else doc.payeeBankBIC = bic;
        }
      }
    }
  }

  /** Function process tag 86 in statement operation (MT940)
   *  :61:0102080208D75,80NTRF00110//044525225
   * @param message - string array with message
   * @param doc - document
   */
  void read86(String[] message, FDocument doc) {
    ArrayList<String> tag;
    tag = getTag(message, "86");
    StringBuilder str = new StringBuilder("");
    for (int i = 0; i < tag.size(); i++) {
      str.append(tag.get(i));
    }
    if (str.length() > 0) doc.referenceSBP = str.toString();
  }
}
