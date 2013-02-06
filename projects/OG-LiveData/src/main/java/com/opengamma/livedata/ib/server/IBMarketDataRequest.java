/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Implementation of the reqMktData() IB API method. 
 * Can also be used for snapshots.
 */
public class IBMarketDataRequest extends IBRequest {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBMarketDataRequest.class);

  /** Default tick tags for special market data, like e.g. option volume an open interest on stocks */
  public static final String GENERIC_TICK_TAGS = "100,101,104,105,106,107,165,221,225,233,236,258,293,294,295,318";

  /** Data fields anticipated as response to this request. */
  private Set<String> _acceptedFieldNames = Sets.newHashSet(IBConstants.MARKET_DATA_FIELDS);

  /** Data fields actually received during the lifecycle of this request. */
  private Set<String> _fieldNames = Sets.newHashSetWithExpectedSize(8);

  private String _tickTags;
  private boolean _snapshot;
  private Contract _contract;

  public IBMarketDataRequest(IBLiveDataServer server, String uniqueId) {
    this(server, uniqueId, GENERIC_TICK_TAGS, false);
  }

  public IBMarketDataRequest(IBLiveDataServer server, String uniqueId, boolean snapshot) {
    this(server, uniqueId, snapshot ? "" : GENERIC_TICK_TAGS, snapshot);
  }

  public IBMarketDataRequest(IBLiveDataServer server, String uniqueId, String tickTags, boolean snapshot) {
    super(server, uniqueId);
    this._tickTags = tickTags;
    this._snapshot = snapshot;
    this._contract = new Contract();
    this._contract.m_conId = getContractId();
    if (snapshot) {
      _acceptedFieldNames.add(IBConstants.MARKET_DATA_SNAPSHOT_END);
    }
  }

  public Set<String> getAcceptedFieldNames() {
    return _acceptedFieldNames;
  }

  protected Set<String> getFieldNames() {
    return _fieldNames;
  }

  public String getTickTags() {
    return _tickTags;
  }

  public void setTickTags(String tickTags) {
    _tickTags = tickTags;
  }

  public boolean isSnapshot() {
    return _snapshot;
  }

  public void setSnapshot(boolean snapshot) {
    _snapshot = snapshot;
  }

  public Contract getContract() {
    return _contract;
  }

  public void setContract(Contract contract) {
    _contract = contract;
  }

  @Override
  protected void fireRequest() {
    // first nullify any old response state
    getFieldNames().clear();
    setResponse(null);
    
    // request contract details for the contractId we have, so we can extract the exchange to use
    fireContractDetailsRequest();
    
    // workflow will continue asynchronously when this request 
    // publishes the contract details and with it the exchange 
    // we need to fire the actual market data request; see fireMarketDataRequest(...) below
  }

  private void fireContractDetailsRequest() {
    IBRequest req = new IBContractDetailsRequest(getServer(), getUniqueId()) {
      @Override
      protected void publishResponse() {
        getLogger().debug("extracting exchange from contract details for cid=" + getContractId() + " req=" + IBMarketDataRequest.this);
        FudgeMsg response = getResponse();
        ContractDetails cd = response.getValue(ContractDetails.class, "contractDetails");
        Contract filledContract = cd.m_summary;
        String validExchanges = cd.m_validExchanges;
        getLogger().debug("valid exchanges for cid=" + getContractId() + " sym=" + cd.m_summary.m_symbol + " are: " + validExchanges);
        fireMarketDataRequest(filledContract, validExchanges);
        getServer().terminateRequest(getCurrentTickerId());
      }
    };
    int tickerId = getServer().getNextTickerId();
    req.setCurrentTickerId(tickerId);
    getServer().activateRequest(tickerId, req);
  }

  private void fireMarketDataRequest(Contract filledContract, String validExchanges) {
    // build request data
    Contract contract = getContract();
    String exchange = filledContract.m_exchange;
    if (exchange == null) {
      // if no explicit exchange set in contract already, try first of valid exchanges
      Set<String> exchanges = IBContractDetailsRequest.getValidExchanges(validExchanges);
      if (exchanges.isEmpty()) {
        // TODO is this the right place and type of exception to throw here?
        throw new OpenGammaRuntimeException("cannot fire market data request because no valid exchange has been specified");
      }
      exchange = exchanges.iterator().next();
    }
    getLogger().debug("using exchange=" + exchange + " for req=" + this);
    contract.m_exchange = exchange;
    
    // fire async call to IB API
    String msg = "firing IB market data request for cid=" + getContractId() + " with tid=" + getCurrentTickerId() + " snapshot=" + isSnapshot();
    getLogger().debug(msg);
    EClientSocket conn = getConnector();
    conn.reqMktData(getCurrentTickerId(), contract, getTickTags(), isSnapshot());
  }

  @Override
  protected void processChunk(IBDataChunk chunk) {
    // sanity check input and state
    if (chunk == null) { return; }
    if (chunk.getTickerId() != getCurrentTickerId()) { return; }
    
    FudgeMsg data = chunk.getData();
    String msg = "processing IB market data response chunk: id=" + getCurrentTickerId() + "  cid=" + getContractId() + "  chunk=" + data;
    getLogger().debug(msg);
    Set<String> chunkFieldNames = data.getAllFieldNames();
    for (String fieldName : chunkFieldNames) {
      if (getAcceptedFieldNames().contains(fieldName)) {
        getFieldNames().add(fieldName);
        // this chunk contains valid data for our request
        FudgeField field = data.getByName(fieldName);
        if (isSnapshot()) {
          if (IBConstants.MARKET_DATA_SNAPSHOT_END.equals(fieldName)) {
            // received the poison marker designating end of transmission
            getLogger().debug("completed IB market data snapshot request for cid=" + getContractId() + "  with tid=" + getCurrentTickerId());
            publishResponse();
            getServer().terminateRequest(getCurrentTickerId());
          } else {
            FudgeMsg oldResponse = getResponse();
            // clone the current response
            MutableFudgeMsg newResponse = OpenGammaFudgeContext.getInstance().newMessage(oldResponse);
            // append field to it
            newResponse.add(field);
            // set atomically as new response
            setResponse(newResponse);
          }
        } else {
          MutableFudgeMsg newResponse = OpenGammaFudgeContext.getInstance().newMessage();
          newResponse.add(field);
          // the current response will always be the latest tick
          setResponse(newResponse);
          // publish every tick immediately for non-snapshots
          publishResponse();
        }
      } else {
        throw new IllegalStateException("received chunk with unexpected field name for market data request! tid=" + getCurrentTickerId() + " field=" + fieldName);
      }
    }
  }

  @Override
  public boolean isResponseFinished() {
    if (isSnapshot()) {
      // must have received a complete set of ticks plus the poison marker
      return getFieldNames().contains(IBConstants.MARKET_DATA_SNAPSHOT_END);
    }
    // on-going subscriptions never finish
    // the subscription is cancelled instead and the request terminated
    return false;
  }

  @Override
  public Logger getLogger() {
    return s_logger;
  }

}
