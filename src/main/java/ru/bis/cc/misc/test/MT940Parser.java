package ru.bis.cc.misc.test;

public class MT940Parser extends SWIFTParser {

  MT940Parser() {
    super();
    expectedFields = new String[]{"61", "86"};
  }

  /** Loads one statemnent operation from MT940
   *
   * @param str = string with MT940 fields 61 and 86
   * @return financial document
   */
  FDocument fromString(String str) {

    FDocument doc = new FDocument();
    readHeader(str, doc);

    String[] messageLines = splitMessage(str);

    read61(messageLines, doc);
    read86(messageLines, doc);

    return doc;
  }

}
