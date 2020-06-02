package ru.bis.cc.misc.test;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FDocument {

  private String edNo;
  private String edDate;
  private String referenceMT103;
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
    docNum = "0";
    docDate = "1901-01-01";
  }

  Long getId() {
    return Long.parseLong(docNum); // Document ID takes from document number
  }
  String getDate() {
    return docDate;
  }
  boolean getIsUrgent() { return isUrgent; }
  void setIsUrgent(boolean isUrgent) { this.isUrgent = isUrgent; }

  /** Loads object from XML node
   *
   * @param node - ED node with document
   */
  public void getED(Node node) { // TODO: get => read etc
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

  /** Loads object from MT103
   *
   * @param str = string with MT103 message
   */
  void getMT103(String str) {

    String[] fields = {"20", "32A", "50K", "52A", "53B", "57D", "59", "70", "72", "77B"};
    StringBuilder tag = new StringBuilder();

    String innKeyWord = "INN";
    String regExpINN = innKeyWord + "\\d{12}\\D|"+ innKeyWord + "\\d{10}\\D|" + innKeyWord + "\\d{5}\\D" +
                       innKeyWord + "\\d{12}$|"+ innKeyWord + "\\d{10}$|" + innKeyWord + "\\d{5}$"; // to take into account that INN may takes place in the end of the string
    Pattern patternINN = Pattern.compile(regExpINN);

    int posMessage = str.indexOf("{2:");
    if (posMessage >= 0) {
      int endPos = str.indexOf("}", posMessage);
      if (endPos >= 0) {
        char urgent = str.charAt(endPos - 1);
        if (urgent == 'U') isUrgent = true;
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
          oneLine = str.substring(posMessage, str.length());
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
          for (String tagNo : fields) {
            if (oneLine.startsWith(tagNo + ":")) {
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
                  docNum = tagContent.toString();
                }

                else if (tagName.equals("32A")) {
                  docDate = Helper.getXMLDate(tagContent.substring(0, 6));
                  amount = Long.parseLong(tagContent.substring(9).replace(".", ""));
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
                  }
                  if (innPos >= 0 && innEnd >= 0) {
                    if (innPos > 0) clientName.append(rawClientName.substring(0, innPos));
                    clientName.append(rawClientName.substring(innEnd + 1).toString().trim());
                  }
                  else
                    clientName.append(rawClientName.trim());
                  if (tagName.equals("50K")) {
                    payerAccount = accountNo;
                    payerINN = inn;
                    if (cpp.length() > 0) payerCPP = cpp.toString();
                    payerName = clientName.toString();
                  }
                  else {
                    payeeAccount = accountNo;
                    payeeINN = inn;
                    if (cpp.length() > 0) payeeCPP = cpp.toString();
                    payeeName = clientName.toString();
                  }
                }

                else if (tagName.equals("52A")) {
                  int pos = tagContent.indexOf("BIK");
                  if (pos >= 0) {
                    int end = tagContent.indexOf(System.lineSeparator(), pos);
                    if (end < 0) end = tagContent.length();
                    payerBankBIC = tagContent.substring(pos + 3, end);
                  }
                }

                else if (tagName.equals("53B")) {
                  if (tagContent.substring(0, 1).equals("/")) {;
                    int end = tagContent.indexOf(System.lineSeparator());
                    if (end < 0) end = tagContent.length();
                    payerBankAccount = tagContent.substring(1, end);
                  }
                }

                else if (tagName.equals("57D")) {
                  int pos = tagContent.indexOf("/");
                  int end = -1;
                  if (pos >= 0) {
                    end = tagContent.indexOf(System.lineSeparator(), pos);
                    if (end < 0) end = tagContent.length();
                    payeeBankAccount = tagContent.substring(pos + 1, end);
                  }
                  pos = tagContent.indexOf("BIK");
                  if (pos >= 0) {
                    end = tagContent.indexOf(System.lineSeparator(), pos);
                    if (end < 0) end = tagContent.length();
                    payeeBankBIC = tagContent.substring(pos + 3, end);
                  }
                }

                else if (tagName.equals("70")) {
                  purposePart1 = tagContent.toString().replace(System.lineSeparator(), "");
                }

                else if (tagName.equals("72")) {
                  purposePart2 = tagContent.toString().replace(System.lineSeparator(), "");
                }

                else if (tagName.equals("77B")) {
                  referenceMT103 = tagContent.toString();
                }
              }
            }

            // Start next tag
            if (oneLine.length() > 0) {
              tagName = newTag;
              tagContent.setLength(0);
              tagContent.append(oneLine.substring(newTag.length() + 1));
            }
          }
          else {
            tagContent.append(System.lineSeparator());
            tagContent.append(oneLine);
          }
        }
        if (startOfMessage) startOfMessage = false;
      } // end of main cycle

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
      String taxAttrSeparator = "";
      int taxAttrLen = 0;
      if (taxAttrBegin.equals("//") || taxAttrBegin.equals("\\\\")) {
        taxAttrSeparator = taxAttrBegin.substring(0, 1);
        StringTokenizer tokenizer = new StringTokenizer(purposePart1.substring(posStart + 2),taxAttrSeparator);
        int i = 0; taxAttrLen = 2;
        isTax = true;
        while (tokenizer.hasMoreTokens() && i < 9) {
          String token = tokenizer.nextToken();
          if (i == 0) payerCPP = token;
          if (i == 1) taxStatus = token;
          if (i == 2) CBC = token;
          if (i == 3) OCATO = token;
          if (i == 4) taxPaytReason = token;
          if (i == 5) taxPeriod = token;
          if (i == 6) taxDocNum = token;
          if (i == 7) taxDocDate = token;
          if (i == 8) if (token.length() <= 1) taxPaytKind = token; else break; // Field 110 may not be specified and final separated may be missed
          i++; taxAttrLen += token.length() + 1;
        }
      }
      if (taxAttrLen == 0) purpose = purposePart1 + purposePart2;
      else purpose = purposePart1.substring(0, posStart) + purposePart1.substring(posStart + taxAttrLen) + purposePart2;
      // Try to extract UIN
      posStart = purpose.indexOf("УИН"); if (posStart < 0) posStart = purpose.indexOf("UIN");
      if (posStart >= 0) {
        int posEnd = purpose.indexOf("///", posStart);
        if (posEnd >= 0) {
          UIN = purpose.substring(posStart + 3, posEnd);
          purpose = purpose.substring(0, posStart) + purpose.substring(posEnd + 3);
        }
      }
    }
    else {
      // There is no block 4: in parsing message
    }
  }

  /** Creates FT14 string from object
   *
   * @return FT14 string (unicode), returns null if error or filter
   */
  public String putFT14() {

    StringBuilder str = new StringBuilder();

//    if (Helper.matchMask(payerAccount, "301,302")) return null; // Filter output records by payer account

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
  public String putMT103() {
    int iPos; // current position when we split text to multistring text fields
    int iStr; // current string number when we split text to multistring text fields

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
    str.append(String.format("%d.%02d", amount / 100, amount % 100));
    str.append(System.lineSeparator());

    str.append("50K:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (payerAccount != null) { str.append("/"); str.append(payerAccount); str.append(System.lineSeparator()); iStr = 1; } // 1st string
    if (payerINN != null) {
      participant.append("INN"); participant.append(payerINN);
      if (payerName != null) participant.append(" "); // Separate INN by space with next client name
    }
    if (payerName != null) participant.append(payerName);
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length() - 1) break;
    }

    if (payerBankBIC != null) {
      str.append("52A:");
      str.append("BIK");
      str.append(payerBankBIC);
      str.append(System.lineSeparator());
    }

    if (payerBankAccount != null) {
      str.append("53B:");
      str.append("/");
      str.append(payerBankAccount);
      str.append(System.lineSeparator());
    }

    str.append("57D:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (payeeBankAccount != null) { str.append("/"); str.append(payeeBankAccount); str.append(System.lineSeparator()); iStr = 1; } // 1st string
    if (payeeBankBIC != null) {
      participant.append("BIK"); participant.append(payeeBankBIC);
      if(payeeBankName != null) participant.append(" "); // Separate BIC by space with next bank name
    }
    if (payeeBankName != null) participant.append(payeeBankName);
    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length() - 1) break;
    }

    str.append("59:");
    iPos = 0; iStr = 0; participant.setLength(0);
    if (payeeAccount != null) { str.append("/"); str.append(payeeAccount); str.append(System.lineSeparator()); iStr = 1; } // 1st string
    if (payeeINN != null) {
      participant.append("INN"); participant.append(payeeINN);
      if (payeeName != null && payeeCPP == null) participant.append(" "); // Separate INN by space with next client name
    }
    if (payeeCPP != null) {
      participant.append("/KPP"); participant.append(payeeCPP);
      if (payeeName != null) participant.append(" "); // Separate INN by space with next client name
    }
    if (payeeName != null) participant.append(payeeName);

    for (int j = iStr; j <= 4; j++) {
      str.append(participant.substring(iPos, Math.min(iPos + 35, participant.length())));
      str.append(System.lineSeparator());
      iPos += 35;
      if (iPos > participant.length()) break;
    }

    // Build tax attributes string It has to be add to payment purpose
    StringBuilder locPurpose = new StringBuilder(); // Payment purpose that saves into MT103 - it difference with document purpose (contains tax attributes)
    locPurpose.append(getTaxAttrs());
    locPurpose.append(purpose.replace("{", "((").replace("}", "))")); // Replace "{" and "}" to "((" and "))"
    if (UIN != null) {
      locPurpose.append("УИН"); locPurpose.append(UIN); locPurpose.append("///");
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

      if (Helper.isStrNullMismatch(docNum, compared.docNum)) return false;
      if (Helper.isStrNullMismatch(docDate, compared.docDate)) return false;
      if ((amount != null && compared.amount == null) || (amount == null && compared.amount != null)) return false;
      if (!(amount != null && amount.equals(compared.amount))) return false;
      if (Helper.isStrNullMismatch(purpose, compared.purpose)) return false;
      if (Helper.isStrNullMismatch(UIN, compared.UIN)) return false;

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
  public StringBuilder oneMismatch(StringBuilder str, String attrName, String val1, String val2) {
    str.append(" "); str.append(attrName); str.append(" mismatch: ");
    str.append(val1); str.append(" against "); str.append(val2);
    str.append(System.lineSeparator());
    return str;
  }
}
