/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * String constants related to the Interactive Brokers API.
 */
public interface IBConstants {

  /** Marker used to designate the data response to a contract details request */
  String CONTRACT_DETAILS = "contractDetails";

  /** Poison Marker used to designate the end of a contract details request */
  String CONTRACT_DETAILS_END = "contractDetailsEnd";

  /** Set of field names returned when requesting contract details */
  Set<String> CONTRACT_DETAILS_FIELDS = ImmutableSet.of(
      "conid", "symbol", "secType", "expiry", "strike", "right", "multiplier", "exchange", 
      "primaryExch", "currency", "localSymbol", "marketName", "tradingClass", "minTick", 
      "price magnifier", "orderTypes", "validExchanges", "underConId", "longName", "contractMonth", 
      "industry", "category", "subcategory", "timeZoneId", "tradingHours", "liquidHours", 
      "evRule", "evMultiplier", "secIdList");

}
