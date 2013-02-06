/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.ib.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.ib.client.ContractDetails;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.MapLastKnownValueStoreProvider;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test for Interactive Brokers live market data server.
 */
@Test(groups = "unit")
public class IBLiveDataServerTest {

  /** 12087792 is the EUR.USD Forex rate traded at IDEALPRO */
  private static String CONTRACT_ID_1 = "12087792";

  /** 825711 is the DAX index traded at at DTB (Eurex) */
  private static String CONTRACT_ID_2 = "825711";

  /** 1411277 is the IBM stock traded at FWB */
  private static String CONTRACT_ID_3 = "1411277";

  /** 14094 is the BMW stock traded at FWB or IBIS (Xetra) */
  private static String CONTRACT_ID_4 = "14094";

  private ExternalScheme _domain;
  private IBLiveDataServer _server;
  private FudgeContext _fudgeContext;

  @BeforeClass
  public void setUpBeforeClass() {
    // need to call this once to build Fudge object and type dictionary
    _fudgeContext = OpenGammaFudgeContext.getInstance();
  }

  @BeforeMethod
  public void setUp() {
    _server = new IBLiveDataServer(true, "", 7496);
    _domain = _server.getUniqueIdDomain();
    _server.connect();
  }

  @AfterMethod
  public void tearDown() {
    _server.disconnect();
  }

  //-------------------------------------------------------------------------
  public void contractDetails() {
    FudgeMsg msg = _server.getContractDetails(CONTRACT_ID_1);
    assertNotNull(msg);
    ContractDetails cd = msg.getValue(ContractDetails.class, "contractDetails");
    assertEquals(Integer.parseInt(CONTRACT_ID_1), cd.m_summary.m_conId);
    assertEquals("Symbol should be EUR", "EUR", cd.m_summary.m_symbol);
    assertEquals("Currency should be USD", "USD", cd.m_summary.m_currency);
    assertEquals("IDEALPRO should be the only valid exchange", "IDEALPRO", cd.m_validExchanges);
    
    msg = _server.getContractDetails(CONTRACT_ID_2);
    assertNotNull(msg);
    cd = msg.getValue(ContractDetails.class, "contractDetails");
    assertEquals(Integer.parseInt(CONTRACT_ID_2), cd.m_summary.m_conId);
    assertEquals("Symbol should be DAX", "DAX", cd.m_summary.m_symbol);
    assertEquals("Currency should be EUR", "EUR", cd.m_summary.m_currency);
    assertEquals("DTB should be the only valid exchange", "DTB", cd.m_validExchanges);
    
    msg = _server.getContractDetails(CONTRACT_ID_3);
    assertNotNull(msg);
    cd = msg.getValue(ContractDetails.class, "contractDetails");
    assertEquals(Integer.parseInt(CONTRACT_ID_3), cd.m_summary.m_conId);
    assertEquals("Symbol should be IBM", "IBM", cd.m_summary.m_symbol);
    assertEquals("Currency should be EUR", "EUR", cd.m_summary.m_currency);
    Set<String> exchanges = IBContractDetailsRequest.getValidExchanges(cd.m_validExchanges);
    // actually these exchanges are valid: SMART,BATEDE,CHIXDE,FWB,IBIS,MIBSX,SWB
    assertTrue("Several exchanges should be listed as a valid exchange", exchanges.size() > 1);
    assertTrue("FWB should be listed as a valid exchange", exchanges.contains("FWB"));
    snapshot();
  }

  public void subscription() {
    getMethods(CONTRACT_ID_1, true); 
  }

  private LiveDataSpecification getSpec(String uniqueId) {
    LiveDataSpecification spec = new LiveDataSpecification(
        _server.getDefaultNormalizationRuleSetId(),
        ExternalId.of(_server.getUniqueIdDomain(), uniqueId));
    return spec;
  }

  private void getMethods(String uniqueId, boolean persistent) {
    LiveDataSpecification spec = getSpec(uniqueId);    
    
    LiveDataSubscriptionResponse result = _server.subscribe(uniqueId, persistent);

    assertNotNull(result);
    assertTrue(result.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS);
    
    String distributionSpec = result.getTickDistributionSpecification();
    assertNotNull(distributionSpec);
    
    
    Subscription subscription = _server.getSubscription(uniqueId); 
    
    assertNotNull(subscription);
    assertEquals(uniqueId, subscription.getSecurityUniqueId());
    assertEquals(1, subscription.getDistributors().size());
    assertSame(subscription, _server.getSubscription(spec));
    
    String invalidContractId = "bogus";
    assertTrue(_server.isSubscribedTo(subscription));
    assertFalse(_server.isSubscribedTo(new Subscription(invalidContractId, _server.getMarketDataSenderFactory(), new MapLastKnownValueStoreProvider())));
    assertTrue(_server.isSubscribedTo(uniqueId));
    assertFalse(_server.isSubscribedTo(invalidContractId));
    assertTrue(_server.isSubscribedTo(spec));
    assertFalse(_server.isSubscribedTo(getSpec(invalidContractId)));
    
    assertEquals(1, _server.getSubscriptions().size());
    assertEquals(1, _server.getNumActiveSubscriptions());
    assertSame(subscription, _server.getSubscriptions().iterator().next());
    assertEquals(1, _server.getActiveSubscriptionIds().size());
    assertEquals(uniqueId, _server.getActiveSubscriptionIds().iterator().next());
    
    assertEquals(0, _server.getNumLiveDataUpdatesSentPerSecondOverLastMinute(), 0.0001);
    assertEquals(0, _server.getNumMarketDataUpdatesReceived());
    
    MarketDataDistributor distributor = subscription.getDistributors().iterator().next();
        
    assertSame(distributor, subscription.getMarketDataDistributor(spec));
    
    assertSame(distributor, _server.getMarketDataDistributor(spec));
    
    assertTrue(distributor.isPersistent() == persistent);
    assertNull(distributor.getExpiry());
  }

  public void subscribeUnsubscribeA() {
    _server.subscribe(CONTRACT_ID_1, false);
    //_server.subscribe("persistent", true);
    
    assertTrue(_server.unsubscribe(CONTRACT_ID_1));
    //assertTrue(_server.unsubscribe("persistent"));
    
    assertNull(_server.getSubscription(CONTRACT_ID_1));
    //assertNull(_server.getSubscription("persistent"));
    
    assertFalse(_server.isSubscribedTo(CONTRACT_ID_1));
    //assertFalse(_server.isSubscribedTo("persistent"));
  }

  public void subscribeUnsubscribeB() {
    _server.subscribe(CONTRACT_ID_1, false);
    //_server.subscribe("persistent", true);
    
    Subscription nonpersistent = _server.getSubscription(CONTRACT_ID_1); 
    //Subscription persistent = _server.getSubscription("persistent");
    
    assertTrue(_server.unsubscribe(nonpersistent));
    //assertTrue(_server.unsubscribe(persistent));  
  }

  public void subscribeUnsubscribeC() {
    UserPrincipal user = new UserPrincipal("mark", "1.1.1.1");
    
    LiveDataSpecification requestedSpec = new LiveDataSpecification(
        StandardRules.getNoNormalization().getId(), 
        ExternalId.of(_domain, CONTRACT_ID_1));
    
    LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.NON_PERSISTENT, 
        Collections.singleton(requestedSpec));
    
    LiveDataSubscriptionResponseMsg response = _server.subscriptionRequestMade(request);
    
    checkResponse(user, requestedSpec, response);
    
    assertTrue(_server.unsubscribe(CONTRACT_ID_1));
    
    response = _server.subscriptionRequestMade(request);
    checkResponse(user, requestedSpec, response);
    
    assertTrue(_server.unsubscribe(CONTRACT_ID_1));
  }

  public void subscribeThenStopDistributor() {
    _server.subscribe(CONTRACT_ID_1, false);
    _server.subscribe(CONTRACT_ID_1, false);
    //_server.subscribe(CONTRACT_ID_1, true);
    
    assertEquals(1, _server.getNumActiveSubscriptions());
    
    Subscription sub = _server.getSubscription(CONTRACT_ID_1);
    assertEquals(1, sub.getDistributors().size());

    LiveDataSpecification spec = getSpec(CONTRACT_ID_1);
    MarketDataDistributor distributor = _server.getMarketDataDistributor(spec);
    assertNotNull(distributor);

    assertFalse(_server.stopDistributor(distributor));
    distributor.setPersistent(false);
    assertTrue(_server.stopDistributor(distributor));
    assertTrue(sub.getDistributors().isEmpty());
    assertFalse(_server.isSubscribedTo(CONTRACT_ID_1));
    assertNull(_server.getSubscription(CONTRACT_ID_1));
    assertNull(_server.getSubscription(spec));
    assertNull(_server.getMarketDataDistributor(spec));
    assertEquals(0, _server.getNumActiveSubscriptions());
    
    assertFalse(_server.stopDistributor(distributor));
  }

  public void snapshot() {
    //Subscription subscription = new Subscription(CONTRACT_ID_1, _server.getMarketDataSenderFactory(), _server.getLkvStoreProvider());
    //FudgeMsg snapshot = _server.doSnapshot(CONTRACT_ID_1);
    
    UserPrincipal user = new UserPrincipal("ibTest", "127.0.0.1");
    
    LiveDataSpecification requestedSpec = getSpec(CONTRACT_ID_1);
    
    LiveDataSpecification requestedSpec2 = getSpec(CONTRACT_ID_2);
    
    LiveDataSpecification requestedSpec3 = getSpec(CONTRACT_ID_3);
    
    LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.SNAPSHOT, 
        Collections.singleton(requestedSpec));
    
    LiveDataSubscriptionRequest request2 = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.SNAPSHOT, 
        Collections.singleton(requestedSpec2));
    
    LiveDataSubscriptionRequest request3 = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.SNAPSHOT, 
        Collections.singleton(requestedSpec3));
    
    LiveDataSubscriptionResponseMsg response = _server.subscriptionRequestMade(request);
    checkSnapshotResponse(user, requestedSpec, response);
    
    //assertFalse(_server.unsubscribe(CONTRACT_ID_1));
    //assertTrue(_server.unsubscribe(CONTRACT_ID_1));
    
    LiveDataSubscriptionResponseMsg response2 = _server.subscriptionRequestMade(request2);
    checkSnapshotResponse(user, requestedSpec2, response2);
    
    // temporarily disabled because FWB exchange subsription seems to be unstable/deactivated
    //LiveDataSubscriptionResponseMsg response3 = _server.subscriptionRequestMade(request3);
    //checkSnapshotResponse(user, requestedSpec3, response3);
    
    LiveDataSubscriptionRequest compoundRequest = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.SNAPSHOT, 
        Sets.newHashSet(requestedSpec, requestedSpec2));
        //Sets.newHashSet(requestedSpec, requestedSpec2, requestedSpec3));
    
    LiveDataSubscriptionResponseMsg compoundResponse = _server.subscriptionRequestMade(compoundRequest);
    LiveDataSubscriptionResponse res = compoundResponse.getResponses().get(0);
    FudgeMsg msg = res.getSnapshot().getFields();
    assertNotNull(msg);
    LiveDataSubscriptionResponse res2 = compoundResponse.getResponses().get(1);
    FudgeMsg msg2 = res2.getSnapshot().getFields();
    assertNotNull(msg2);
    //LiveDataSubscriptionResponse res3 = compoundResponse.getResponses().get(2);
    //FudgeMsg msg3 = res3.getSnapshot().getFields();
    //assertNotNull(msg3);
    
  }

  private void checkResponse(UserPrincipal user, LiveDataSpecification requestedSpec,
      LiveDataSubscriptionResponseMsg response) {
    assertEquals(user, response.getRequestingUser());
    assertEquals(1, response.getResponses().size());
    LiveDataSubscriptionResponse res = response.getResponses().get(0);
    assertEquals(requestedSpec, res.getRequestedSpecification());
    assertEquals(requestedSpec, res.getFullyQualifiedSpecification());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, res.getSubscriptionResult());
    assertEquals(null, res.getSnapshot()); 
    assertEquals(requestedSpec.getIdentifiers().toString(), res.getTickDistributionSpecification());
    assertEquals(null, res.getUserMessage());
  }

  private void checkSnapshotResponse(UserPrincipal user, LiveDataSpecification requestedSpec,
      LiveDataSubscriptionResponseMsg response) {
    assertNotNull(response);
    assertEquals(user, response.getRequestingUser());
    assertEquals(1, response.getResponses().size());
    LiveDataSubscriptionResponse res = response.getResponses().get(0);
    assertEquals(requestedSpec, res.getRequestedSpecification());
    LiveDataSpecification fqs = res.getFullyQualifiedSpecification();
    assertNotNull(fqs);
    assertEquals(requestedSpec, fqs);
    assertNull(res.getTickDistributionSpecification());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, res.getSubscriptionResult());
    assertEquals(null, res.getUserMessage());
    LiveDataValueUpdateBean data = res.getSnapshot();
    assertNotNull(data);
    assertEquals(requestedSpec, data.getSpecification());
    FudgeMsg msg = data.getFields();
    assertNotNull(msg);
  }

}
