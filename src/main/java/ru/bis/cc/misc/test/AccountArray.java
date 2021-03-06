package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

class AccountArray {
  HashMap<String, Account> items = new HashMap<>(); // Accounts array
  Logger logger;

  AccountArray() {
    logger = LogManager.getLogger(ClientArray.class);
  }

  Account getAccount(String accNo) {
    for (String acc : items.keySet()) {
      if (acc.equals(accNo)) {
        return items.get(acc);
      }
    }
    return null;
  }

}
