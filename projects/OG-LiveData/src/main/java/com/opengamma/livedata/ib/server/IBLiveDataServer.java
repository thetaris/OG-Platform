/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An Interactive Brokers live data server. 
 */
public class IBLiveDataServer extends StandardLiveDataServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBLiveDataServer.class);

  // configuration
  private final String _host;
  private final int _port;
  
  // state
  private volatile int _numConnections; // = 0;
  private EClientSocket _eClientSocket;
  private EWrapper _callback;

  public IBLiveDataServer(boolean isPerformanceCountingEnabled, String host, int port) {
    super(EHCacheUtils.createCacheManager(), isPerformanceCountingEnabled);
    this._host = host;
    this._port = port;
  }

  /**
   * Gets the host.
   * @return the host
   */
  public String getHost() {
    return _host;
  }

  /**
   * Gets the port.
   * @return the port
   */
  public int getPort() {
    return _port;
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    // Note: connection state check is implemented as a decorator via verifyConnectionOk()
    // So we assume the connection is alive at this point
    
    ArgumentChecker.notNull(uniqueIds, "IB Contract IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Map<String, Object> returnValue = new HashMap<String, Object>();
    
    // TODO subscribe to market data feed for given IB contracts
    
    return returnValue;
  }

  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    // TODO check connection
    //eClientSocket.isConnected();
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    Map<String, FudgeMsg> returnValue = new HashMap<String, FudgeMsg>();
    
    for (String uniqueId : uniqueIds) {
      try {
        MutableFudgeMsg snapshotSpec = OpenGammaFudgeContext.getInstance().newMessage();
        int tickerId = System.identityHashCode(snapshotSpec);
        s_logger.debug("IB preparing mkt data snapshot for uniqueId={} with tickerId={}", uniqueId, tickerId);
        snapshotSpec.add("UNIQUE_ID", uniqueId);
        snapshotSpec.add("TICKER_ID", String.valueOf(tickerId));
        Contract contract = createContractFromId(uniqueId);
        _eClientSocket.reqMktData(tickerId, contract, null, true);
        returnValue.put(uniqueId, snapshotSpec);
      } catch (NumberFormatException e) {
        s_logger.error("uniqueId {} is not a valid IB contract Id", uniqueId);
        continue;
      }
    }
    
    return returnValue;
  }

  private Contract createContractFromId(String uniqueId) {
    Contract contract = new Contract();
    contract.m_conId = Integer.parseInt(uniqueId);
    // TODO derive exchange from contract ID using reqContractDetails()
    contract.m_exchange = "IDEALPRO";
    return contract;
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return ExternalSchemes.IB_CONTRACT;
  }

  @Override
  protected void doConnect() {
    s_logger.debug("IB connection opening...");
    // set up callback; TODO: hook up with OpenGamma MarketDataSender
    _callback = new OpenGammaIBWrapper();

    // connect to TWS
    _eClientSocket = new EClientSocket(_callback);
    int clientId = createClientId();
    _eClientSocket.eConnect(getHost(), getPort(), clientId);
    // TODO implement a mechanism to deal with TWS acknowledge popup on connect
    // e.g. see http://ibcontroller.sourceforge.net/
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (_eClientSocket.isConnected()) {
      _numConnections++;
      s_logger.debug("IB connection openened (now open: " + _numConnections + ")");
    } else {
      s_logger.debug("IB connection opening failed");
    }
  }

  /**
   * A number used to identify this client connection.
   * Note: Each client MUST connect with a unique clientId.
   * @return a unique Id to identify a connection made by this class
   */
  private int createClientId() {
    int id = System.identityHashCode(_eClientSocket);
    return id;
  }

  @Override
  protected void doDisconnect() {
    s_logger.debug("IB connection closing...");
    _eClientSocket.eDisconnect();
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (!_eClientSocket.isConnected()) {
      _numConnections--;
      s_logger.debug("IB connection closed (now open: " + _numConnections + ")");
    } else {
      s_logger.debug("IB connection closing failed");
    }
  }

  @Override
  protected void verifyConnectionOk() {
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (_eClientSocket.isConnected()) {
      if (getConnectionStatus() != ConnectionStatus.CONNECTED) {
        setConnectionStatus(ConnectionStatus.CONNECTED);
      }
    } else {
      if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
        setConnectionStatus(ConnectionStatus.NOT_CONNECTED);
      }
    }
    
    super.verifyConnectionOk();
  }
  
  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    // TODO check whether this is needed for IB TWS
    return false;
  }

}
