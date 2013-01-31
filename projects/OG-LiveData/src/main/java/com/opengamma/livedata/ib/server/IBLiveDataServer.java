/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An Interactive Brokers live data server. 
 */
public class IBLiveDataServer extends StandardLiveDataServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBLiveDataServer.class);

  private static final ExecutorService s_executorService = Executors.newCachedThreadPool(new NamedThreadPoolFactory("IBLiveDataServer"));

  // configuration
  private final String _host;
  private final int _port;
  
  // state
  private volatile int _numConnections; // = 0;

  /** request handlers increment this counter for each request made */
  private static AtomicInteger _tickerId = new AtomicInteger();

  private EClientSocket _connector;
  private EWrapper _callback;
  private final Map<Integer, String> _tickerId2uniqueId = new HashMap<Integer, String>();
  private final Map<Integer, IBRequest> _tickerId2request = new HashMap<Integer, IBRequest>();
  private final BlockingQueue<IBDataChunk> _received = new LinkedBlockingQueue<IBDataChunk>();

  /** dispatches IB data chunks to the IB requests they belong to */
  private TerminatableJob _dispatcher;

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

  protected EClientSocket getConnector() {
    return _connector;
  }

  protected EWrapper getCallback() {
    return _callback;
  }

  protected static ExecutorService getExecutorService() {
    return s_executorService;
  }

  /** package */ int getNextTickerId() {
    return _tickerId.incrementAndGet();
  }

  /** package */ String getUniqueIdForTickerId(int tickerId) {
    return _tickerId2uniqueId.get(tickerId);
  }

  /** package */ IBRequest getRequestForTickerId(int tickerId) {
    return _tickerId2request.get(tickerId);
  }

  /** package */ void activateRequest(int tickerId, IBRequest request) {
    if (request != null) {
      _tickerId2request.put(tickerId, request);
      request.fireRequest();
    }
  }

  /** package */ void terminateRequest(int tickerId) {
    IBRequest req = _tickerId2request.remove(tickerId);
    if (req != null) {
      req.terminate();
    }
  }

  /** package */ BlockingQueue<IBDataChunk> getDataChunks() {
    return _received;
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    // Note: connection state check is implemented as a decorator via verifyConnectionOk()
    // So we assume the connection is alive at this point
    
    ArgumentChecker.notNull(uniqueIds, "IB Contract IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    s_logger.debug("Subscribing to {}", uniqueIds);
    
    final Map<String, Object> result = Maps.newHashMapWithExpectedSize(uniqueIds.size());
    for (String id : uniqueIds) {
      Object handle = doSubscribe(id, false);
      result.put(id, handle);
    }
    return result;
  }

  protected Object doSubscribe(String uniqueId, boolean snapshot) {
    // TODO subscribe to market data feed for given IB contract
    final Subscription subscription = getSubscription(uniqueId);
    Object handle = new AtomicReference<FudgeMsg>();
    return handle;
  }

  @Override
  protected void subscriptionDone(Set<String> uniqueIds) {
    for (String identifier : uniqueIds) {
      final FudgeMsg msg = getLatestValue(identifier);
      if (msg != null) {
        liveDataReceived(identifier, msg);
      }
    }
  }

  private FudgeMsg getLatestValue(String uniqueId) {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("UNIQUE_ID", uniqueId);
    // TODO get real values here (from cache?)
    msg.add("bidPrice", 1.345);
    msg.add("askPrice", 1.34505);
    return msg;
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
        _connector.reqMktData(tickerId, contract, null, true);
        returnValue.put(uniqueId, snapshotSpec);
      } catch (NumberFormatException e) {
        s_logger.error("uniqueId {} is not a valid IB contract Id", uniqueId);
        continue;
      }
    }
    
    return returnValue;
  }

  /**
   * Retrieves details for the contract specified by the uniqueId (i.e. the conId). 
   * Note: this method blocks while waiting for the response from the IB system 
   * and should therefore should be used mainly for testing.
   * 
   * @param uniqueId  the unique contract Id of the contract
   * @return Fudge message containing an instance of {@link ContractDetails}
   */
  /** package */ FudgeMsg getContractDetailsSync(String uniqueId) {
    IBRequest req = new IBContractDetailsRequest(this, uniqueId);
    int tickerId = getNextTickerId();
    req.setCurrentTickerId(tickerId);
    activateRequest(tickerId, req);
    
    while (!req.isResponseFinished()) {
      try {
        s_logger.debug("waiting for contract details...");
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    FudgeMsg res = req.getResponse();
    terminateRequest(tickerId);
    return res;
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
    _callback = new OpenGammaIBWrapper(this);

    // connect to TWS
    _connector = new EClientSocket(_callback);
    int clientId = createClientId();
    _connector.eConnect(getHost(), getPort(), clientId);
    // TODO implement a mechanism to deal with TWS acknowledge popup on connect
    // e.g. see http://ibcontroller.sourceforge.net/
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (_connector.isConnected()) {
      _numConnections++;
      s_logger.debug("IB server " + _connector.serverVersion() + " handshake at " + _connector.TwsConnectionTime());
      s_logger.debug("IB connection openened (now open: " + _numConnections + ")");
      _dispatcher = new IBResponseDispatcher(this);
      getExecutorService().submit(_dispatcher);
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
    int id = System.identityHashCode(_connector);
    return id;
  }

  @Override
  protected void doDisconnect() {
    s_logger.debug("IB connection closing...");
    _connector.eDisconnect();
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (!_connector.isConnected()) {
      _numConnections--;
      s_logger.debug("IB connection closed (now open: " + _numConnections + ")");
    } else {
      s_logger.debug("IB connection closing failed");
    }
  }

  @Override
  protected void verifyConnectionOk() {
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (_connector.isConnected()) {
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
