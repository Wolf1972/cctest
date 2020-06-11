package ru.bis.cc.misc.test;

class Helper {

  /** Compares two strings, each may be null - if strings mismatch, returns TRUE
   *
   * @param s1 - 1st string
   * @param s2 - 2nd string
   * @return boolean: strings are mismatch - true/false
   */
  static boolean isStrNullMismatch(String s1, String s2) {
    if ((s1 != null && s2 == null) || (s1 == null && s2 != null)) return true;
    if (s1 != null) return !s1.equals(s2); // don't need to check s2 with null
    return false;
  }

  /** Checks if string begins with symbols that were determined in specified mask
   *
   * @param chk - checked string (may be null)
   * @param mask - mask with one or several patterns (separated by ",")
   * @return boolean: checked string accords mask (true/false)
   */
  static boolean matchMask(String chk, String mask) {
    if (chk == null) return false;
    String[] patterns = mask.split(",");
    for (String str : patterns) {
      if (chk.startsWith(str)) return true;
    }
    return false;
  }

  /** Function returns date in SWIFT format (YYMMDD) from date in XML format (YYYY-MM-DD)
   *
   * @param XMLDate = XML date string (YYYY-MM-DD)
   * @return string with date in SWIFT format (YYMMDD)
   */
  static String getSWIFTDate(String XMLDate) {
    if (XMLDate == null) return "";
    if (XMLDate.length() < 10) return "";
    return XMLDate.substring(2, 4) + XMLDate.substring(5, 7) + XMLDate.substring(8, 10);
  }

  /** Function returns date in UFEBS format (YYYY-MM-DD) from date in SWIFT format (YYMMDD)
   *
   * @param SWIFTDate = SWIFT date string (YYMMDD)
   * @return string with date in XML format (YYYY-MM-DD)
   */
  static String getXMLDate(String SWIFTDate) {
    if (SWIFTDate == null) return "";
    if (SWIFTDate.length() < 6) return "";
    return "20" + SWIFTDate.substring(0, 2) + "-" + SWIFTDate.substring(2, 4) + "-" + SWIFTDate.substring(4, 6);
  }

}
