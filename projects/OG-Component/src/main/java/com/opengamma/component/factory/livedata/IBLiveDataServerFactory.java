/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.livedata;

import java.util.Collection;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.livedata.ib.server.IBLiveDataServer;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.resolver.DefaultDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.IBJmsTopicNameResolver;
import com.opengamma.livedata.resolver.IdentityIdResolver;
import com.opengamma.livedata.server.MapLastKnownValueStoreProvider;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.distribution.EmptyMarketDataSenderFactory;
import com.opengamma.livedata.server.distribution.MarketDataSenderFactory;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataServerTypes;

/**
 * Component factory to create an {@link IBLiveDataServer Interactive Brokers live data server}.
 */
@BeanDefinition
public class IBLiveDataServerFactory extends AbstractStandardLiveDataServerComponentFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(IBLiveDataServerFactory.class);

  private static final String IB_LIVE_SOURCE_NAME = "Interactive Brokers live market data";
  private static final String IB_NORM_RULESET_ID = "InteractiveBrokers";

  /**
   * The host running the Interactive Brokers Trade Workstation (TWS).
   */
  @PropertyDefinition(validate = "notNull")
  private String _host;

  /**
   * The port on which to open a socket to the TWS host.
   */
  @PropertyDefinition(validate = "notNull")
  private int _port;
  
  public IBLiveDataServerFactory() {
  }

  @Override
  protected StandardLiveDataServer initServer(ComponentRepository repo) {
    s_logger.info("Initing Interactive Brokers live data server...");
    
    IBLiveDataServer server = new IBLiveDataServer(true, getHost(), getPort());
    
    // TODO create ruleset to map IB field names to canonical OG form
    NormalizationRuleSet rulesIB = new NormalizationRuleSet(IB_NORM_RULESET_ID);
    Collection<NormalizationRuleSet> rules = ImmutableList.of(StandardRules.getNoNormalization(), rulesIB);
    
    // TODO create IB specific IdResolver if necessary
    DefaultDistributionSpecificationResolver distSpecResolver = new DefaultDistributionSpecificationResolver(
        new IdentityIdResolver(),
        new StandardRuleResolver(rules),
        new IBJmsTopicNameResolver());
    server.setDistributionSpecificationResolver(distSpecResolver);
    
    // TODO create IB specific entitlement checker if necessary
    // Note: since we connect to a TWS instance that is already authenticated, 
    // entitlement checking is probably not needed, so the default permissive one should be ok.
    server.setEntitlementChecker(new PermissiveLiveDataEntitlementChecker());
    
    MarketDataSenderFactory senderFactory = new EmptyMarketDataSenderFactory();
    //MarketDataSenderFactory senderFactory = new JmsSenderFactory(getJmsConnector());
    server.setMarketDataSenderFactory(senderFactory);
    
    // cache values in a FudgeHistoryStore which is backed by a simple map
    server.setLkvStoreProvider(new MapLastKnownValueStoreProvider());
    
    s_logger.info("Init Interactive Brokers live data server finished");
    return server;
  }

  @Override
  protected LiveDataMetaData createMetaData(ComponentRepository repo) {
    return new LiveDataMetaData(ImmutableList.of(ExternalSchemes.IB_CONTRACT), LiveDataServerTypes.STANDARD, IB_LIVE_SOURCE_NAME);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code IBLiveDataServerFactory}.
   * @return the meta-bean, not null
   */
  public static IBLiveDataServerFactory.Meta meta() {
    return IBLiveDataServerFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(IBLiveDataServerFactory.Meta.INSTANCE);
  }

  @Override
  public IBLiveDataServerFactory.Meta metaBean() {
    return IBLiveDataServerFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3208616:  // host
        return getHost();
      case 3446913:  // port
        return getPort();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3208616:  // host
        setHost((String) newValue);
        return;
      case 3446913:  // port
        setPort((Integer) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_host, "host");
    JodaBeanUtils.notNull(_port, "port");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      IBLiveDataServerFactory other = (IBLiveDataServerFactory) obj;
      return JodaBeanUtils.equal(getHost(), other.getHost()) &&
          JodaBeanUtils.equal(getPort(), other.getPort()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getHost());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPort());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the host running the Interactive Brokers Trade Workstation (TWS).
   * @return the value of the property, not null
   */
  public String getHost() {
    return _host;
  }

  /**
   * Sets the host running the Interactive Brokers Trade Workstation (TWS).
   * @param host  the new value of the property, not null
   */
  public void setHost(String host) {
    JodaBeanUtils.notNull(host, "host");
    this._host = host;
  }

  /**
   * Gets the the {@code host} property.
   * @return the property, not null
   */
  public final Property<String> host() {
    return metaBean().host().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the port on which to open a socket to the TWS host.
   * @return the value of the property, not null
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets the port on which to open a socket to the TWS host.
   * @param port  the new value of the property, not null
   */
  public void setPort(int port) {
    JodaBeanUtils.notNull(port, "port");
    this._port = port;
  }

  /**
   * Gets the the {@code port} property.
   * @return the property, not null
   */
  public final Property<Integer> port() {
    return metaBean().port().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IBLiveDataServerFactory}.
   */
  public static class Meta extends AbstractStandardLiveDataServerComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code host} property.
     */
    private final MetaProperty<String> _host = DirectMetaProperty.ofReadWrite(
        this, "host", IBLiveDataServerFactory.class, String.class);
    /**
     * The meta-property for the {@code port} property.
     */
    private final MetaProperty<Integer> _port = DirectMetaProperty.ofReadWrite(
        this, "port", IBLiveDataServerFactory.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "host",
        "port");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3208616:  // host
          return _host;
        case 3446913:  // port
          return _port;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends IBLiveDataServerFactory> builder() {
      return new DirectBeanBuilder<IBLiveDataServerFactory>(new IBLiveDataServerFactory());
    }

    @Override
    public Class<? extends IBLiveDataServerFactory> beanType() {
      return IBLiveDataServerFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code host} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> host() {
      return _host;
    }

    /**
     * The meta-property for the {@code port} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> port() {
      return _port;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
