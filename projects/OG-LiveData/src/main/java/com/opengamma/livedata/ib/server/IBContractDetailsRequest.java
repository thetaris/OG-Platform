/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;

/**
 * Implementation of the reqContractDetails() IB API method.
 */
public class IBContractDetailsRequest extends IBRequest {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBContractDetailsRequest.class);

  /** Data fields required to fulfill this request. */
  public static final Set<String> REQUIRED_FIELD_NAMES = ImmutableSet.of(
      IBConstants.CONTRACT_DETAILS, 
      IBConstants.CONTRACT_DETAILS_END);

  /** Data fields actually received during the lifecycle of this request. */
  private Set<String> _fieldNames = Sets.newHashSetWithExpectedSize(REQUIRED_FIELD_NAMES.size());

  public IBContractDetailsRequest(IBLiveDataServer server, String uniqueId) {
    super(server, uniqueId);
  }

  protected Set<String> getFieldNames() {
    return _fieldNames;
  }

  @Override
  protected void fireRequest() {
    // first nullify any old response state
    getFieldNames().clear();
    setResponse(null);
    
    // build request data
    Contract contract = new Contract();
    contract.m_conId = getContractId();
    
    // fire async call to IB API
    String msg = "firing IB contract details request for cid=" + getContractId() + " with tid=" + getCurrentTickerId();
    getLogger().debug(msg);
    EClientSocket conn = getConnector();
    conn.reqContractDetails(getCurrentTickerId(), contract);
  }

  @Override
  protected void processChunk(IBDataChunk chunk) {
    // sanity check input and state
    if (chunk == null) { return; }
    if (chunk.getTickerId() != getCurrentTickerId()) { return; }
    if (isResponseFinished()) {
      // this should not happen, as a finished response is published immediately 
      // and a new request would reset the response state
      throw new IllegalStateException("received chunk for already finished response! tid=" + getCurrentTickerId());
    }
    
    FudgeMsg data = chunk.getData();
    String msg = "processing IB contract details response chunk: id=" + getCurrentTickerId() + "  cid=" + getContractId() + "  chunk=" + data;
    getLogger().debug(msg);
    Set<String> chunkFieldNames = data.getAllFieldNames();
    for (String fieldName : chunkFieldNames) {
      if (IBConstants.CONTRACT_DETAILS.equals(fieldName)) {
        // this chunk actually contains a complete ContractDetails instance
        setResponse(data);
        getFieldNames().add(fieldName);
      } else if (IBConstants.CONTRACT_DETAILS_END.equals(fieldName)) {
        // received the poison marker designating end of transmission
        getLogger().debug("completed IB contract details request for cid=" + getContractId() + "  with tid=" + getCurrentTickerId());
        getFieldNames().add(fieldName);
        publishResponse();
      }
    }
  }

  @Override
  public boolean isResponseFinished() {
    boolean complete = getFieldNames().containsAll(REQUIRED_FIELD_NAMES);
    return complete;
  }

  @Override
  public Logger getLogger() {
    return s_logger;
  }

  @Override
  protected void publishResponse() {
    // override to do nothing, as this is an internal request, 
    // the response of which is only needed to fire other requests 
    // (namely the exchange must be determined)
  }

  /**
   * IB returns all valid exchanges where a contract is traded as part of a contract details request. 
   * The exchanges are encoded in a comma-separated string. 
   * This method splits the IB string into a normalized set.
   * @param validExchanges string of valid exchanges as returned by IB
   * @return normalized set of exchange names
   * @see ContractDetails#m_validExchanges
   */
  public static final Set<String> getValidExchanges(String validExchanges) {
    if (validExchanges == null || validExchanges.isEmpty()) {
      return Collections.emptySet();
    }
    String[] exchanges = validExchanges.split(",");
    return Sets.newHashSet(exchanges);
  }

}
