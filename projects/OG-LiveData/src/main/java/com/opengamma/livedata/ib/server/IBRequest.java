/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;

import com.ib.client.EClientSocket;

/**
 * A Request lifecycle object which calls the Interactive Brokers API and 
 * {@link #processChunk(IBDataChunk) processes} all matching response chunks 
 * until the request is fulfilled. 
 * Once a complete response object has been assembled, it may  
 * be published to the live data server via {@link #publishResponse()}.
 */
public abstract class IBRequest {

  private int _currentTickerId;
  private IBLiveDataServer _server;
  private String _uniqueId;
  private int _contractId;
  private final AtomicBoolean _terminated;

  /** 
   * Response to this request.
   * May be progressively completed by incoming data chunks. 
   * May be reset and re-used for multi-stage requests or ongoing subscriptions.
   */
  private AtomicReference<FudgeMsg> _response = new AtomicReference<FudgeMsg>();

  /**
   * Creates a new request lifecycle object.
   * @param server  reference to the live data server requesting the data
   * @param uniqueId  uniqueId of the security data is requested for; 
   * represents the IB contractId and hence must be a positive integer
   * @throws IllegalArgumentException if uniqueId cannot be parsed into an integer
   */
  public IBRequest(IBLiveDataServer server, String uniqueId) {
    this._server = server;
    this._uniqueId = uniqueId;
    this._terminated = new AtomicBoolean(false);
    try {
      this._contractId = Integer.parseInt(uniqueId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("IB uniqueId should be an integer: " + uniqueId);
    }
  }

  protected IBLiveDataServer getServer() {
    return _server;
  }

  protected EClientSocket getConnector() {
    return _server.getConnector();
  }

  /**
   * @return the uniqueId this request was constructed for
   */
  public String getUniqueId() {
    return _uniqueId;
  }

  /**
   * Currently the contractId is identical to the uniqueId. 
   * Since the IB conId is encoded as integer while OpenGamma uses a moe general string format, 
   * this method returns the uniqueId parsed into an integer. 
   * @return the contractId this request represents
   */
  public int getContractId() {
    return _contractId;
  }

  protected int getCurrentTickerId() {
    return _currentTickerId;
  }

  protected void setCurrentTickerId(int tickerId) {
    this._currentTickerId = tickerId;
  }

  /**
   * Returns the result of the request, encoding the response in a suitable Fudge message.
   * @return the Fudge encoded response
   */
  public FudgeMsg getResponse() {
    return _response.get();
  }

  protected void setResponse(FudgeMsg msg) {
    _response.set(msg);
  }

  /**
   * Implements the actual call into the Interactive Brokers API. 
   * This should be asynchronous, i.e. the method should send the request 
   * and return immediately.
   */
  protected abstract void fireRequest();

  /**
   * Called when receiving a chunk that is part of the response to our request. 
   * Implementations should extract data from each chunk and use it to build the response.
   * @param chunk  piece of data comprising the IB response
   */
  protected abstract void processChunk(IBDataChunk chunk);

  /**
   * Implementations must be able to indicate whether a request has been completely fulfilled. 
   * Once this is the case, the result should be available using {@link #getResponse()} 
   * and this method should return true.
   * @return true if the request is finished and its response available; 
   * false if the response is still pending
   */
  protected abstract boolean isResponseFinished();

  /**
   * @return the logger to use; must not be null
   */
  protected abstract Logger getLogger();

  /**
   * Publishes the current response as retrieved via {@link #getResponse()}. 
   * This will be called once for simple atomic requests, but may be called continuously 
   * in the case of ongoing subscriptions (when the next publishable response is complete).
   */
  protected abstract void publishResponse();

  /**
   * A terminated request should no longer process any incoming data chunks or create repsonses.
   * @return true if this request has been terminated 
   */
  protected boolean isTerminated() {
    return _terminated.get();
  }

  /**
   * Terminate lifecycle of this request and clean up.
   */
  protected void terminate() {
    _terminated.set(true);
    setResponse(null);
  }

}
