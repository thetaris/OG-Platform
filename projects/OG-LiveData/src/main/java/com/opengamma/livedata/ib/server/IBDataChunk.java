/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import org.fudgemsg.FudgeMsg;

/**
 * A piece of {@link #getData() data} returned via one of the callback methods of {@link OpenGammaIBWrapper}. 
 * Will be dispatched by {@link IBResponseDispatcher} and processed by the {@link IBRequest} 
 * this chunk is associated with via the {@link #getTickerId() tickerId}.
 */
public class IBDataChunk {

  private int _tickerId;
  private FudgeMsg _data;

  public IBDataChunk(int tickerId, FudgeMsg data) {
    this._tickerId = tickerId;
    this._data = data;
  }

  /**
   * Gets the tickerId.
   * @return the tickerId
   */
  public int getTickerId() {
    return _tickerId;
  }

  /**
   * Gets the data.
   * @return the data
   */
  public FudgeMsg getData() {
    return _data;
  }

}
