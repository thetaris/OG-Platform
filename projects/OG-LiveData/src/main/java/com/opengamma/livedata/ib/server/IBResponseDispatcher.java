/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.TerminatableJob;

/**
 * Dispatches IB data chunks to the IB requests they belong to. 
 * Handles cases of orphan or stale chunks that might occur when active requests 
 * are terminated prematurely or when subscriptions are cancelled.
 */
public class IBResponseDispatcher extends TerminatableJob {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBResponseDispatcher.class);

  private IBLiveDataServer _server;

  public IBResponseDispatcher(IBLiveDataServer server) {
    this._server = server;
  }

  @Override
  protected void runOneCycle() {
    try {
      IBDataChunk chunk = _server.getDataChunks().take();
      //s_logger.debug("received IB response chunk: tid={} data={}", chunk.getTickerId(), chunk.getData());
      dispatch(chunk);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("IB data chunk queue take wait interrupted", e);
    }
  }

  protected void dispatch(IBDataChunk chunk) {
    int tickerId = chunk.getTickerId();
    IBRequest req = _server.getRequestForTickerId(tickerId);
    if (req != null) {
      s_logger.debug("dispatching IB response chunk tid={} to request: {}", tickerId, req);
      req.processChunk(chunk);
    } else {
      // may happen in case of terminated subscriptions
      // this implementation simply drops those chunks and logs a warning
      s_logger.warn("dropping IB response chunk for unknown or terminated request! tid={}", tickerId);
      //throw new IllegalStateException("received IB response chunk for unknown or terminated request! tid=" + tickerId);
    }
  }

}
