package ru.bis.cc.misc.test;

class Account {

  String account;
  boolean isInternal;
  Long clientId;
  ClientType clientType;
  String details;
  String openDate;
  String closeDate;

  Account() {
    isInternal = false;
  }
}
