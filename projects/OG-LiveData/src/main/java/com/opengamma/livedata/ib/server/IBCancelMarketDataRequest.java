/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.EClientSocket;

/**
 * Implementation of the cancelMktData() IB API method. 
 */
public class IBCancelMarketDataRequest extends IBRequest {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBCancelMarketDataRequest.class);

  public IBCancelMarketDataRequest(IBLiveDataServer server, String uniqueId, int tickerId) {
    super(server, uniqueId);
    setCurrentTickerId(tickerId); // need to re-use same tickerId to cancel subscription
  }

  @Override
  protected void fireRequest() {
    // fire async call to IB API
    getLogger().debug("firing IB cancel market data request for cid={} tid={}", getContractId(), getCurrentTickerId());
    EClientSocket conn = getConnector();
    conn.cancelMktData(getCurrentTickerId());
  }

  @Override
  protected void processChunk(IBDataChunk chunk) {
    // not needed, as there is no IB response to a cancel market data request
    getLogger().error("IB cancel market data request received a response chunk which should be impossible! cid={} tid={} data={}", new Object[]{getUniqueId(), getCurrentTickerId(), chunk.getData()});
    throw new IllegalStateException("IB cancel market data request should never receive a response chunk!");
  }

  @Override
  protected boolean isResponseFinished() {
    return true;
  }

  @Override
  protected void publishResponse() {
    // nothing to do
  }

  @Override
  protected Logger getLogger() {
    return s_logger;
  }

}
