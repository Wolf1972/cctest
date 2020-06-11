package ru.bis.cc.misc.test;

import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

class FDocumentArray {

  HashMap<Long, FDocument> docs = new HashMap<>(); // Financial documents array
  boolean isReversePacket = false;

  /** Puts one document into array with key control
   *
   * @param doc - document to add
   * @param logger - current logger
   */
  void add(FDocument doc, Logger logger) {
    if (doc != null) {
      if (docs.containsKey(doc.getId())) {
        logger.error("0516: Document with ID " + doc.getId() + " has already exist in array.");
      }
      else {
        docs.put(doc.getId(), doc);
        if (!isReversePacket && !doc.isUrgent) {
          isReversePacket = !doc.payerBankBIC.equals("044525101"); // Packet type defines by first document in packet
        }
        logger.trace("0513: Document added: " + doc.toString());
      }
    }
  }

  /** Function sets specified date into all elements of documents array
   *
   * @param date - date in XML format [YYYY-MM-DD]
   */
  void setDate(String date) {
    for (Map.Entry<Long, FDocument> item : docs.entrySet()) {
      FDocument doc = item.getValue();
      doc.edDate = date;
      doc.docDate = date;
      if (doc.chargeOffDate != null) doc.chargeOffDate = date;
      if (doc.receiptDate != null) doc.receiptDate = date;
    }
  }

  /** Creates new document array with incoming (reverse) documents, assembled by outgoing (internal documents are not included)
   *
   * @param logger - current logger
   * @return - incoming (reverse) document array
   */
  FDocumentArray createReverse(Logger logger) {
    FDocumentArray revs = new FDocumentArray();
    int count = 0;
    for (Map.Entry<Long, FDocument> item : docs.entrySet()) {
      FDocument doc = item.getValue();
      if (!doc.payeeBankBIC.equals("044525101")) { // Outgoing document
        FDocument rev = doc.reverse();
        revs.add(rev, logger);
        count ++;
      }
    }
    logger.info("0500: Reverse documents created: " + count);
    return revs;
  }

}
