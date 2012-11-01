/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;


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

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.ExternalId;

/**
 * The fixed interest rate swap leg.
 */
@BeanDefinition
public class FixedInterestRateLeg extends InterestRateLeg {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The rate.
   */
  @PropertyDefinition
  private double _rate;

  /**
   * Creates an instance.
   */
  FixedInterestRateLeg() { //For builder
  }

  /**
   * Creates an instance.
   * 
   * @param dayCount  the day count, not null
   * @param frequency  the frequency, not null
   * @param regionIdentifier  the region, not null
   * @param businessDayConvention  the business day convention, not null
   * @param notional  the notional, not null
   * @param eom  whether this is EOM
   * @param rate  the rate, not null
   */
  public FixedInterestRateLeg(DayCount dayCount, Frequency frequency, ExternalId regionIdentifier, BusinessDayConvention businessDayConvention,
      Notional notional, boolean eom, double rate) {
    super(dayCount, frequency, regionIdentifier, businessDayConvention, notional);
    setRate(rate);
    setEom(eom);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(SwapLegVisitor<T> visitor) {
    return visitor.visitFixedInterestRateLeg(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedInterestRateLeg}.
   * @return the meta-bean, not null
   */
  public static FixedInterestRateLeg.Meta meta() {
    return FixedInterestRateLeg.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(FixedInterestRateLeg.Meta.INSTANCE);
  }

  @Override
  public FixedInterestRateLeg.Meta metaBean() {
    return FixedInterestRateLeg.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3493088:  // rate
        return getRate();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3493088:  // rate
        setRate((Double) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FixedInterestRateLeg other = (FixedInterestRateLeg) obj;
      return JodaBeanUtils.equal(getRate(), other.getRate()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getRate());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rate.
   * @return the value of the property
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Sets the rate.
   * @param rate  the new value of the property
   */
  public void setRate(double rate) {
    this._rate = rate;
  }

  /**
   * Gets the the {@code rate} property.
   * @return the property, not null
   */
  public final Property<Double> rate() {
    return metaBean().rate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedInterestRateLeg}.
   */
  public static class Meta extends InterestRateLeg.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code rate} property.
     */
    private final MetaProperty<Double> _rate = DirectMetaProperty.ofReadWrite(
        this, "rate", FixedInterestRateLeg.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "rate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3493088:  // rate
          return _rate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FixedInterestRateLeg> builder() {
      return new DirectBeanBuilder<FixedInterestRateLeg>(new FixedInterestRateLeg());
    }

    @Override
    public Class<? extends FixedInterestRateLeg> beanType() {
      return FixedInterestRateLeg.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code rate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> rate() {
      return _rate;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}