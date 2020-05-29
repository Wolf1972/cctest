package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Comparator {

  private Logger logger;

  Comparator(Logger logger) {
    this.logger = logger;
  }

  /**
   * Compares 2 documents arrays
   *
   * @param pattern - pattern
   * @param sample  - checked sample
   */
  void compare(HashMap<Long, FDocument> pattern, HashMap<Long, FDocument> sample) {

    int iMismatch = 0;
    int iMissingInSample = 0;
    int iMissinginPattern = 0;

    for (Map.Entry<Long, FDocument> item : pattern.entrySet()) {
      Long patternKey = item.getKey();
      FDocument patternDoc = item.getValue();
      if (sample.containsKey(patternKey)) {
        if (!patternDoc.equals(sample.get(patternKey))) {
          logger.error("THE0201: Mismatch pattern and sample documents with ID: " + patternKey);
          logger.error("THI0201: " + patternDoc.mismatchDescribe(sample.get(patternKey)));
          iMismatch++;
        }
      } else {
        logger.error("THE0202: Pattern document with ID: " + patternKey + " is not found in sample.");
        iMissingInSample++;
      }
    }
    for (Map.Entry<Long, FDocument> item : sample.entrySet()) {
      Long sampleKey = item.getKey();
      FDocument sampleDoc = item.getValue();
      if (!pattern.containsKey(sampleKey)) {
        logger.error("THE0203: Sample document with ID: " + sampleKey + " is not found in pattern.");
        iMissinginPattern++;
      }
    }

    logger.info("THI0202: Compare complete. Mismatches: " + iMismatch + ", not in sample: " + iMissingInSample + ", not in pattern: " + iMissinginPattern);

  }

}
