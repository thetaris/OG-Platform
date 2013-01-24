/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.ContractDetails;
import com.ib.client.TickType;

/**
 * The wrapper listens to IB server responses.
 */
public class OpenGammaIBWrapper extends AbstractBaseWrapper {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaIBWrapper.class);

  public OpenGammaIBWrapper() {
  }

  ///////////////////////////////////////////////////////////////////////
  // Callbacks
  ///////////////////////////////////////////////////////////////////////
  
  @Override
  public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
    s_logger.debug("IB callback: tickPrice");
    String msg = "id=" + tickerId + "  " + TickType.getField( field) + "=" + price + " " + 
    ((canAutoExecute != 0) ? " canAutoExecute" : " noAutoExecute");
    s_logger.debug(msg);
  }

  @Override
  public void tickSize(int tickerId, int field, int size) {
    s_logger.debug("IB callback: tickSize");
    super.tickSize(tickerId, field, size);
  }

  @Override
  public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
    s_logger.debug("IB callback: tickOptionComputation");
    super.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice);
  }

  @Override
  public void tickGeneric(int tickerId, int tickType, double value) {
    s_logger.debug("IB callback: tickGeneric");
    super.tickGeneric(tickerId, tickType, value);
  }

  @Override
  public void tickString(int tickerId, int tickType, String value) {
    s_logger.debug("IB callback: tickString");
    super.tickString(tickerId, tickType, value);
  }

  @Override
  public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact,
      double dividendsToExpiry) {
    s_logger.debug("IB callback: tickEFP");
    super.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry);
  }

  @Override
  public void contractDetails(int reqId, ContractDetails contractDetails) {
    s_logger.debug("IB callback: contractDetails");
    super.contractDetails(reqId, contractDetails);
  }

  @Override
  public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    s_logger.debug("IB callback: bondContractDetails");
    super.bondContractDetails(reqId, contractDetails);
  }

  @Override
  public void contractDetailsEnd(int reqId) {
    s_logger.debug("IB callback: contractDetailsEnd");
    super.contractDetailsEnd(reqId);
  }

  @Override
  public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
    s_logger.debug("IB callback: historicalData");
    super.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps);
  }

  @Override
  public void tickSnapshotEnd(int reqId) {
    s_logger.debug("IB callback: tickSnapshotEnd");
    super.tickSnapshotEnd(reqId);
  }

  ///////////////////////////////////////////////////////////////////////
  // Error Logging
  ///////////////////////////////////////////////////////////////////////

  public void error(Exception e) {
    s_logger.error("Caught exception:" + e.toString());
  }

  public void error(String str) {
    s_logger.error("Caught error:" + str);
  }

  public void error(int id, int errorCode, String errorMsg) {
    s_logger.error("Caught error with id:" + id + " code:" + errorCode +
        " msg:" + errorMsg);
  }

  public void connectionClosed() {
    s_logger.warn("Connection closed");
  }

}
