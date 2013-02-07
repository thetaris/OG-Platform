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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.StandardRules;
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

  // infrastructure
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(IBLiveDataServer.class);

  private static final ExecutorService s_executorService = Executors.newSingleThreadExecutor(new NamedThreadPoolFactory("IBLiveDataServer"));

  // configuration
  private final String _host;
  private final int _port;
  
  // state
  /** total number of IB connections made during this server's lifecycle */
  private volatile int _numConnections; // = 0;

  /** identifies a data line of ticks associated with a request; incremented for each request made */
  private static AtomicInteger _tickerId = new AtomicInteger();

  /** requests are made via IB API calls into this IB class */
  private EClientSocket _connector;
  
  /** responses are handled by implementing this interface */
  private EWrapper _callback;

  // mappings of IB tickerId to OpenGamma uniqueId and internal requests 
  private final Map<Integer, String> _tickerId2uniqueId = new HashMap<Integer, String>();
  private final Map<Integer, IBRequest> _tickerId2request = new HashMap<Integer, IBRequest>();
  
  /** indicates progress for synchronous market data snapshots */
  private final Set<Integer> _waitingFor = Sets.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
  
  /** blocking queue which holds data chunks produced by IB until they are consumed by requests */
  private final BlockingQueue<IBDataChunk> _chunks = new LinkedBlockingQueue<IBDataChunk>();

  /** dispatches IB data chunks to the IB requests they belong to */
  private TerminatableJob _dispatcher;

  /** timeout used when waiting for market data requests */
  private long _marketDataTimeout = 20000000000L;

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

  protected void setMarketDataTimeout(final long timeout, final TimeUnit unit) {
    _marketDataTimeout = unit.toNanos(timeout);
  }

  protected long getMarketDataTimeout() {
    return _marketDataTimeout;
  }

  /** package */
  int getNextTickerId() {
    return _tickerId.incrementAndGet();
  }

  /** package */
  String getUniqueIdForTickerId(int tickerId) {
    return _tickerId2uniqueId.get(tickerId);
  }

  /** package */
  IBRequest getRequestForTickerId(int tickerId) {
    return _tickerId2request.get(tickerId);
  }

  /** package */
  void activateRequest(int tickerId, IBRequest request) {
    if (request != null) {
      s_logger.debug("activating request tid={} req={}", tickerId, request);
      _tickerId2request.put(tickerId, request);
      request.fireRequest();
    }
  }

  /** package */
  void terminateRequest(int tickerId) {
    IBRequest request = _tickerId2request.remove(tickerId);
    if (request != null) {
      s_logger.debug("terminating request tid={} req={}", tickerId, request);
      request.terminate();
    } else {
      s_logger.debug("cannot terminate unknown request tid={}", tickerId);
    }
  }

  /** package */
  BlockingQueue<IBDataChunk> getDataChunks() {
    return _chunks;
  }

  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    // Note: connection state check is implemented in verifyConnectionOk()
    // which is called before entering here, so we assume the connection is alive at this point
    
    ArgumentChecker.notNull(uniqueIds, "IB Contract IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    s_logger.debug("Subscribing to {}", uniqueIds);
    
    final Map<String, Object> result = Maps.newHashMapWithExpectedSize(uniqueIds.size());
    for (String uniqueId : uniqueIds) {
      try {
        Object handle = doSubscribe(uniqueId);
        result.put(uniqueId, handle);
      } catch (NumberFormatException e) {
        s_logger.warn("uniqueId {} is not a valid IB contract Id => ignoring this subscription", uniqueId);
        // TODO: what exception to throw in this case?
        continue;
      }
    }
    return result;
  }

  protected Object doSubscribe(String uniqueId) {
    int tickerId = getNextTickerId();
    s_logger.debug("IB preparing subscription to market data for cid={} tid={}", uniqueId, tickerId);
    IBMarketDataRequest req = new IBMarketDataRequest(this, uniqueId, false);
    req.setCurrentTickerId(tickerId);
    activateRequest(tickerId, req);
    return req;
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
    // Note: connection state check is implemented in verifyConnectionOk()
    // which is called before entering here, so we assume the connection is alive at this point
    for (Object handle : subscriptionHandles) {
      if (handle instanceof IBRequest) {
        IBRequest req = (IBRequest) handle;
        // re-use tickerId from market data request so IB knows which data line to kill
        int tickerId = req.getCurrentTickerId();
        
        // terminate the original market data request, so it doesn't receive/process chunks anymore
        terminateRequest(tickerId);
        
        IBCancelMarketDataRequest creq = new IBCancelMarketDataRequest(this, req.getUniqueId(), tickerId);
        // fire cancel request directly; 
        // we do this to avoid putting the same tickerId in the tickerId2request map twice
        // this is ok because we know there will be no callback anyway 
        // and the request will be terminated immediately within this method 
        creq.fireRequest();
        
        // just to be clean: terminate our cancel request (which does nothing atm.)
        creq.terminate();
      }
    }
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    // Note: connection state check is implemented in verifyConnectionOk()
    // which is called before entering here, so we assume the connection is alive at this point
    final Map<String, FudgeMsg> snapshots = new HashMap<String, FudgeMsg>();
    final Set<Integer> tickerIds = Sets.newHashSetWithExpectedSize(uniqueIds.size());
    
    for (String uniqueId : uniqueIds) {
      try {
        int tickerId = getNextTickerId();
        s_logger.debug("preparing market data snapshot for cid={} tid={}", uniqueId, tickerId);
        IBMarketDataRequest req = new IBMarketDataRequest(this, uniqueId, true) {
          @Override
          protected void publishResponse() {
            FudgeMsg response = getResponse();
            // TODO: how to handle response==null?
            int tid = getCurrentTickerId();
            if (_waitingFor.contains(tid)) {
              String cid = getUniqueId();
              getLogger().debug("publishing completed market data snapshot for cid={} tid={} res={}", new Object[] {cid, tid, response});
              synchronized (IBLiveDataServer.this) {
                // store response in snapshot map, mark as completed and wake up server thread
                snapshots.put(cid, response);
                _waitingFor.remove(tid);
                getLogger().debug("removing tid={} from snapshot waitingFor set", tid);
                IBLiveDataServer.this.notifyAll();
              }
            }
          }
        };
        
        req.setCurrentTickerId(tickerId);
        tickerIds.add(tickerId);
        activateRequest(tickerId, req);
        
      } catch (NumberFormatException e) {
        s_logger.error("uniqueId {} is not a valid IB contract Id", uniqueId);
        // TODO: what exception to throw in this case?
        continue;
      }
    }
    
    // let server wait until snapshots are collected
    waitForSnapshotMarketData(tickerIds);
    
    return snapshots;
  }

  /**
   * Retrieves details for the contract specified by the uniqueId (i.e. the conId). 
   * Note: this method blocks while waiting for the response from the IB system 
   * and should therefore should be used mainly for testing.
   * 
   * @param uniqueId  the unique contract Id of the contract
   * @return Fudge message containing an instance of {@link ContractDetails}
   */
  protected FudgeMsg getContractDetails(String uniqueId) {
    IBRequest req = new IBContractDetailsRequest(this, uniqueId);
    int tickerId = getNextTickerId();
    req.setCurrentTickerId(tickerId);
    activateRequest(tickerId, req);
    
    while (!req.isResponseFinished()) {
      try {
        s_logger.debug("waiting for IB contract details...");
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("IB contract details wait interrupted", e);
      }
    }
    
    FudgeMsg res = req.getResponse();
    terminateRequest(tickerId);
    return res;
  }

  /**
   * Let server wait until snapshots for all given tickerIds are collected or timeout is exceeded.
   * @param tickerIds  the tickerIds to wait on
   */
  protected synchronized void waitForSnapshotMarketData(final Collection<Integer> tickerIds) {
    s_logger.debug("waiting for market data snapshot on {}", tickerIds);
    _waitingFor.addAll(tickerIds);
    try {
      // Note: IB usually sends mkt data snapshot end marker 10-20 sec after last tick
      // so the timeout should be at least 20 sec to account for multiple requests
      final long completionTime = System.nanoTime() + getMarketDataTimeout();
      while (!Collections.disjoint(tickerIds, _waitingFor)) {
        s_logger.debug("still waiting for market data snapshot on {}", _waitingFor);
        final long timeout = completionTime - System.nanoTime();
        if (timeout < 1000000L) {
          s_logger.warn("timeout exceeded waiting for market data");
          break;
        }
        try {
          long timeToWait = timeout / 1000000L;
          s_logger.debug("waiting for market data for {}", timeToWait);
          wait(timeToWait);
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("IB market data snapshot wait interrupted", e);
        }
      }
    } finally {
      _waitingFor.removeAll(tickerIds); // needed in case timeout was exceeded
    }
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return ExternalSchemes.IB_CONTRACT;
  }

  @Override
  public String getDefaultNormalizationRuleSetId() {
    // TODO: figure out how to normalize IB market data responses
    //return super.getDefaultNormalizationRuleSetId();
    return StandardRules.getNoNormalization().getId();
  }

  @Override
  protected void doConnect() {
    s_logger.debug("IB connection opening...");
    // set up callback; TODO: hook up with OpenGamma MarketDataSender
    _callback = new OpenGammaIBWrapper(getDataChunks());

    // connect to TWS
    _connector = new EClientSocket(_callback);
    int clientId = createClientId();
    _connector.eConnect(getHost(), getPort(), clientId);
    // TODO implement a mechanism to deal with TWS acknowledge popup on connect
    // e.g. see http://ibcontroller.sourceforge.net/
    // Note: isConnected() is not synchronized, so this can't be 100% safe, but it's not critical
    if (_connector.isConnected()) {
      _numConnections++;
      s_logger.debug("IB server v={} handshake at {}", _connector.serverVersion(), _connector.TwsConnectionTime());
      s_logger.debug("IB connection openened (now open: {})", _numConnections);
      _dispatcher = new IBResponseDispatcher(this);
      try {
        getExecutorService().submit(_dispatcher);
      } catch (RejectedExecutionException ex) {
        String msg = "unable to spawn IB dispatcher";
        s_logger.error(msg);
        throw new OpenGammaRuntimeException(msg, ex);
      }
    } else {
      String msg = "IB connection opening failed";
      s_logger.error(msg);
      throw new OpenGammaRuntimeException(msg);
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
      s_logger.debug("IB connection closed (now open: {})", _numConnections);
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
