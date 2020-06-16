package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.util.Map;

class Comparator {

  private Logger logger;
  private int strictLevel;

  /** Constructor
   * @param logger - logger reference
   * @param strictLevel:
   *   0 - base compare, suits for compare all documents (compares only common fields)
   *   1 - strict compare, suits for compare documents loads from FT14 (common fields and priority, transaction kind)
   *   2 - more strict compare with all fields, suits for documents loads from UFEBS only
   */
  Comparator(int strictLevel, Logger logger) {
    this.strictLevel = strictLevel;
    this.logger = logger;
  }
  Comparator(Logger logger) {
    this.strictLevel = 0;
    this.logger = logger;
  }

  /**
   * Compares 2 arrays of document
   *
   * @param pattern - pattern
   * @param sample  - checked sample
   */
  boolean compare(FDocumentArray pattern, FDocumentArray sample) {

    int iMismatch = 0;
    int iMissingInSample = 0;
    int iMissinginPattern = 0;

    for (Map.Entry<Long, FDocument> item : pattern.docs.entrySet()) {
      Long patternKey = item.getKey();
      FDocument patternDoc = item.getValue();
      if (sample.docs.containsKey(patternKey)) {
        if (!patternDoc.equals(sample.docs.get(patternKey), strictLevel)) {
          logger.error("2001: Mismatch pattern and sample documents with ID: " + patternKey);
          logger.error("2001: " + patternDoc.mismatchDescribe(sample.docs.get(patternKey), strictLevel));
          iMismatch++;
        }
      } else {
        logger.error("2002: Pattern document with ID: " + patternKey + " is not found in sample.");
        iMissingInSample++;
      }
    }
    for (Map.Entry<Long, FDocument> item : sample.docs.entrySet()) {
      Long sampleKey = item.getKey();
      if (!pattern.docs.containsKey(sampleKey)) {
        logger.error("2003: Sample document with ID: " + sampleKey + " is not found in pattern.");
        iMissinginPattern++;
      }
    }

    logger.info("2000: Compare complete. " + "Compared: " + sample.docs.size() +
                   " Mismatches: " + iMismatch + ", not in sample: " + iMissingInSample + ", not in pattern: " + iMissinginPattern);
    return (iMismatch == 0 && iMissinginPattern == 0 && iMissingInSample == 0);
  }

}
