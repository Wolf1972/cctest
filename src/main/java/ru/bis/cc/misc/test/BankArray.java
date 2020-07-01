package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class BankArray {
  HashMap<String, Bank> items = new HashMap<>(); // Banks array
  Logger logger;

  BankArray() {
    logger = LogManager.getLogger(ClientArray.class);
  }

}
