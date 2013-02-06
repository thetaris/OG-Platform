/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.concurrent.BlockingQueue;

import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.TickType;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * This wrapper listens to IB API responses and creates {@link IBDataChunk}s 
 * that can be processed by the {@link IBRequest}s spawned by the {@link IBLiveDataServer}. 
 * Use as callback when constructing an IB client.
 * @see EClientSocket#EClientSocket(com.ib.client.AnyWrapper)
 */
public class OpenGammaIBWrapper extends AbstractBaseWrapper {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaIBWrapper.class);

  private final BlockingQueue<IBDataChunk> _chunks;

  public OpenGammaIBWrapper(BlockingQueue<IBDataChunk> chunks) {
    this._chunks = chunks;
  }

  ///////////////////////////////////////////////////////////////////////
  // Callbacks
  ///////////////////////////////////////////////////////////////////////
  
  @Override
  public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
    String s = "IB callback: tickPrice received tid=" + tickerId + "  " + TickType.getField(field) + "=" + price;
    s_logger.debug(s);
    marketData(tickerId, field, price);
  }

  @Override
  public void tickSize(int tickerId, int field, int size) {
    String s = "IB callback: tickSize received tid=" + tickerId + "  " + TickType.getField(field) + "=" + size;
    s_logger.debug(s);
    marketData(tickerId, field, size);
  }

  @Override
  public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
    String msg = "IB callback: tickOptionComputation is not implemented";
    s_logger.error(msg);
    throw new OpenGammaRuntimeException(msg);
  }

  @Override
  public void tickGeneric(int tickerId, int tickType, double value) {
    // Note: most of these ticks are pretty rare, but "halted" is returned for many exchange traded symbols
    String s = "IB callback: tickGeneric received tid=" + tickerId + "  " + TickType.getField(tickType) + "=" + value;
    s_logger.debug(s);
    marketData(tickerId, tickType, value);
  }

  @Override
  public void tickString(int tickerId, int tickType, String value) {
    // Note: "lastTimestamp" is returned for many exchange traded symbols
    String s = "IB callback: tickString received tid=" + tickerId + "  " + TickType.getField(tickType) + "=" + value;
    s_logger.debug(s);
    marketData(tickerId, tickType, value);
  }

  @Override
  public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact,
      double dividendsToExpiry) {
    // Note: EFP means "Exchange for Physical"
    String msg = "IB callback: tickEFP is not implemented";
    s_logger.error(msg);
    throw new OpenGammaRuntimeException(msg);
  }

  /**
   * Internal emthod for handling misc. market data ticker fields. 
   * Creates data chunks with the tickType derived from the field and the value set as data. 
   * The chunks are put on the server's queue for any active requests to consume and process them.
   * @param tickerId  used to associate the data with the request that wants it
   * @param field  encoded tick type as defined by Interactive Brokers
   * @param value  the actual data
   */
  protected void marketData(int tickerId, int field, Object value) {
    MutableFudgeMsg data = OpenGammaFudgeContext.getInstance().newMessage();
    String tickType = TickType.getField(field);
    data.add(tickType, value);
    IBDataChunk chunk = new IBDataChunk(tickerId, data);
    try {
      _chunks.put(chunk);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("IB data chunk queue put wait interrupted", e);
    }
  }

  @Override
  public void contractDetails(int reqId, ContractDetails contractDetails) {
    String s = "IB callback: contract details received tid=" + reqId + " data=" + contractDetails;
    s_logger.debug(s);
    MutableFudgeMsg data = OpenGammaFudgeContext.getInstance().newMessage();
    data.add(IBConstants.CONTRACT_DETAILS, contractDetails);
    IBDataChunk chunk = new IBDataChunk(reqId, data);
    try {
      _chunks.put(chunk);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("IB data chunk queue put wait interrupted", e);
    }
  }

  @Override
  public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    String msg = "IB callback: bondContractDetails is not implemented";
    s_logger.error(msg);
    throw new OpenGammaRuntimeException(msg);
  }

  @Override
  public void contractDetailsEnd(int reqId) {
    String s = "IB callback: contract details end received tid=" + reqId;
    s_logger.debug(s);
    poisonMarker(reqId, IBConstants.CONTRACT_DETAILS_END);
  }

  @Override
  public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
    // TODO: implement
    String msg = "IB callback: historicalData is not implemented";
    s_logger.error(msg);
    throw new OpenGammaRuntimeException(msg);
  }

  @Override
  public void tickSnapshotEnd(int reqId) {
    String s = "IB callback: tickSnapshotEnd received tid=" + reqId;
    s_logger.debug(s);
    poisonMarker(reqId, IBConstants.MARKET_DATA_SNAPSHOT_END);
  }

  /**
   * Internal method for handling misc. poison markers which indicate end-of-transmission 
   * for certain request types, e.g. contract details or market data snapshots.
   * @param reqId  tickerId associated with the request
   * @param markerName  internal field name defined for this poison marker
   */
  protected void poisonMarker(int reqId, String markerName) {
    MutableFudgeMsg data = OpenGammaFudgeContext.getInstance().newMessage();
    // this is just a poison marker, so the data value is null
    data.add(markerName, null);
    IBDataChunk chunk = new IBDataChunk(reqId, data);
    try {
      _chunks.put(chunk);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("IB data chunk queue put wait interrupted", e);
    }
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
