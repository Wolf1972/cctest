package ru.bis.cc.misc.test;

import java.util.ArrayList;

enum ClientType {
  UNKNOWN, PERSON, SELF_EMPLOYED, COMPANY, BANK
}

class Client {
  Long id;
  ClientType type;

  String lastName;
  String firstNames;

  String officialName;

  String INN;

  String bankBIC;
  String bankCorrAccount;

  ArrayList<Account> accounts = new ArrayList<>();

  Client() {
    type = ClientType.UNKNOWN;
  }

}
