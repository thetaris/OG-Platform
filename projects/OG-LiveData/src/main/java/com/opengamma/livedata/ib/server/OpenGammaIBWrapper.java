/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.TickType;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * The wrapper listens to IB API responses. 
 * Used as callback when constructing an IB client.
 * @see EClientSocket#EClientSocket(com.ib.client.AnyWrapper)
 */
public class OpenGammaIBWrapper extends AbstractBaseWrapper {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaIBWrapper.class);

  private IBLiveDataServer _ibLiveDataServer;

  public OpenGammaIBWrapper(IBLiveDataServer ibLiveDataServer) {
    this._ibLiveDataServer = ibLiveDataServer;
  }

  ///////////////////////////////////////////////////////////////////////
  // Callbacks
  ///////////////////////////////////////////////////////////////////////
  
  @Override
  public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
    s_logger.debug("IB callback: tickPrice");
    String s = "IB tick received tid=" + tickerId + "  " + TickType.getField(field) + "=" + price;
    s_logger.debug(s);
    MutableFudgeMsg data = OpenGammaFudgeContext.getInstance().newMessage();
    String tickType = TickType.getField(field);
    data.add(tickType, price);
    IBDataChunk chunk = new IBDataChunk(tickerId, data);
    try {
      _ibLiveDataServer.getDataChunks().put(chunk);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new OpenGammaRuntimeException("IB data chunk queue put wait interrupted", e);
    }
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
    String s = "IB callback: contract details received tid=" + reqId + " data=" + contractDetails;
    s_logger.debug(s);
    MutableFudgeMsg data = OpenGammaFudgeContext.getInstance().newMessage();
    data.add(IBConstants.CONTRACT_DETAILS, contractDetails);
    IBDataChunk chunk = new IBDataChunk(reqId, data);
    try {
      _ibLiveDataServer.getDataChunks().put(chunk);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("IB data chunk queue put wait interrupted", e);
    }
  }

  @Override
  public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    s_logger.debug("IB callback: bondContractDetails");
    super.bondContractDetails(reqId, contractDetails);
  }

  @Override
  public void contractDetailsEnd(int reqId) {
    String s = "IB callback: contract details end received tid=" + reqId;
    s_logger.debug(s);
    MutableFudgeMsg data = OpenGammaFudgeContext.getInstance().newMessage();
    data.add(IBConstants.CONTRACT_DETAILS_END, null);
    IBDataChunk chunk = new IBDataChunk(reqId, data);
    try {
      _ibLiveDataServer.getDataChunks().put(chunk);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("IB data chunk queue put wait interrupted", e);
    }
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
