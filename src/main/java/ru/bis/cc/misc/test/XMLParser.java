package ru.bis.cc.misc.test;

class XMLParser extends Parser {

  /** String returns string with macro &gt; &lt; for use in element content
   *
   * @param input - input string
   * @return - XML string with masked ",', >, < symbols
   */
  String replace4Elem(String input) {
    return (input != null? input.replace(">", "&gt;").replace("<", "&lt;"): "");
  }
  /** String returns string with macro &amp; &gt; &lt; &quot; for use in attribute content
   *
   * @param input - input string
   * @return - XML string with masked ",', >, < symbols
   */
  String replace4Attr(String input) {
    return replace4Elem(input).replace("'", "&amp;").replace("\"", "&quot;");
  }

}
