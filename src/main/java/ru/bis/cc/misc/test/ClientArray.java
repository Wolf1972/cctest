package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class ClientArray {
  HashMap<Long, Client> items = new HashMap<>(); // Clients array
  Logger logger;

  ClientArray() {
    logger = LogManager.getLogger(ClientArray.class);
  }
}
