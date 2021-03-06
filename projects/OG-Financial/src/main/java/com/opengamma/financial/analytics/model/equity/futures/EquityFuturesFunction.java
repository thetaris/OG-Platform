/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturePricerFactory;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturesPricer;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturesPricingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.simpleinstruments.definition.SimpleFutureDefinition;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.SimpleFutureConverter;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 * This function will produce all valueRequirements that the EquityFutureSecurity offers. A trade may produce additional generic ones, e.g. date and number of contracts..
 */
public class EquityFuturesFunction extends AbstractFunction.NonCompiledInvoker {

  // TODO: Refactor - this is a field name, like PX_LAST - We can't reference BloombergConstants.BBG_FIELD_DIVIDEND_YIELD here
  private static final String DIVIDEND_YIELD_FIELD = "EQY_DVD_YLD_EST";
  private static final SimpleFutureConverter CONVERTER = new SimpleFutureConverter();  // TODO: Had been EquityFutureConverter();

  private final String _valueRequirementName;
  private final EquityFuturesPricingMethod _pricingMethod;
  private final String _pricingMethodName;

  /**
   * @param valueRequirementName String describes the value requested
   * @param pricingMethodName String corresponding to enum EquityFuturesPricingMethod {MARK_TO_MARKET or COST_OF_CARRY, DIVIDEND_YIELD}
   */
  public EquityFuturesFunction(final String valueRequirementName, final String pricingMethodName)  {
    Validate.notNull(valueRequirementName, "value requirement name");
    Validate.notNull(pricingMethodName, "pricing method name");
    Validate.isTrue(valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE)
            || valueRequirementName.equals(ValueRequirementNames.VALUE_RHO)
            || valueRequirementName.equals(ValueRequirementNames.PV01)
            || valueRequirementName.equals(ValueRequirementNames.VALUE_DELTA)
            || valueRequirementName.equals(ValueRequirementNames.SPOT)
            || valueRequirementName.equals(ValueRequirementNames.FORWARD)
            || valueRequirementName.equals(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES),
            "EquityFuturesFunction provides the following values PRESENT_VALUE, VALUE_DELTA, VALUE_RHO, PV01, FORWARD, and SPOT. Please choose one.");

    _valueRequirementName = valueRequirementName;

    Validate.isTrue(pricingMethodName.equals(EquityFuturePricerFactory.MARK_TO_MARKET)
                 || pricingMethodName.equals(EquityFuturePricerFactory.COST_OF_CARRY)
                 || pricingMethodName.equals(EquityFuturePricerFactory.DIVIDEND_YIELD),
        "OG-Analytics provides the following pricing methods for EquityFutureSecurity: MARK_TO_MARKET, DIVIDEND_YIELD and COST_OF_CARRY. Please choose one.");

    _pricingMethod = EquityFuturesPricingMethod.valueOf(pricingMethodName);
    _pricingMethodName = pricingMethodName;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties(target).get()));
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final String calcMethod = getPricingMethodName();
    final ValueProperties.Builder properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CALCULATION_METHOD, calcMethod)
      .withAny(ValuePropertyNames.CURVE)
      .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    return properties;
  }

  protected ValueProperties.Builder createValueProperties(final ComputationTarget target,
      final String fundingCurveName, final String curveConfigName, final String pricingMethodName) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties.Builder properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, ccy.getCode())
      .with(ValuePropertyNames.CURVE, fundingCurveName)
      .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveConfigName)
      .with(ValuePropertyNames.CALCULATION_METHOD, pricingMethodName);
    return properties;
  }

  protected SimpleFutureDataBundle getFutureDataBundle(final FutureSecurity security, final FunctionInputs inputs,
        final HistoricalTimeSeriesBundle timeSeriesBundle, final ValueRequirement desiredValue) {
    Double spotUnderlyer = null;
    switch(getPricingMethodEnum()) {
      case MARK_TO_MARKET:
        Double marketPrice = getMarketPrice(security, inputs);
        if (getValueRequirementName() == ValueRequirementNames.SPOT) {
          spotUnderlyer = getSpot(security, inputs);
        }
        return new SimpleFutureDataBundle(null, marketPrice, spotUnderlyer, null, null);
      case COST_OF_CARRY:
        spotUnderlyer = getSpot(security, inputs);
        Double costOfCarry = getCostOfCarry(security, inputs);
        return new SimpleFutureDataBundle(null, null, spotUnderlyer, null, costOfCarry);
      case DIVIDEND_YIELD:
        spotUnderlyer = getSpot(security, inputs);
        Double dividendYield = timeSeriesBundle.get(DIVIDEND_YIELD_FIELD, getSpotAssetId(security)).getTimeSeries().getLatestValue();
        dividendYield /= 100.0;
        final String fundingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String curveConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        YieldAndDiscountCurve fundingCurve = getYieldCurve(security, inputs, fundingCurveName, curveConfigName);
        return new SimpleFutureDataBundle(fundingCurve, null, spotUnderlyer, dividendYield, null);
      default:
        throw new OpenGammaRuntimeException("Unhandled pricingMethod");
    }
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final Trade trade = target.getTrade();
    final FutureSecurity security = (FutureSecurity) trade.getSecurity();
    // Get reference price
    final HistoricalTimeSeriesBundle timeSeriesBundle = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final Double lastMarginPrice = timeSeriesBundle.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle()).getTimeSeries().getLatestValue();
    // Build the analytic's version of the security - the derivative
    final ZonedDateTime valuationTime = executionContext.getValuationClock().zonedDateTime();
    // final EquityFutureDefinition definition = CONVERTER.visitEquityFutureTrade(trade, lastMarginPrice); // TODO: Clean this up
    final SimpleFutureDefinition simpleDefn = (SimpleFutureDefinition) security.accept(CONVERTER);
    final EquityFutureDefinition defn = new EquityFutureDefinition(simpleDefn.getExpiry(), simpleDefn.getSettlementDate(),
        simpleDefn.getReferencePrice(), simpleDefn.getCurrency(), simpleDefn.getUnitAmount());
    final EquityFuture derivative = defn.toDerivative(valuationTime, lastMarginPrice);
    // Build the DataBundle it requires
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final SimpleFutureDataBundle dataBundle = getFutureDataBundle(security, inputs, timeSeriesBundle, desiredValue);
    // Call OG-Analytics
    final double value = getComputedValue(derivative, dataBundle, trade);
    final ValueSpecification specification = new ValueSpecification(_valueRequirementName, target.toSpecification(),
        createValueProperties(target, getFundingCurveName(desiredValue), getCurveConfigName(desiredValue), getPricingMethodName()).get());
    return Collections.singleton(new ComputedValue(specification, value));
  }

  /**
   * Given _valueRequirement and _pricingMethod supplied, this calls to OG-Analytics.
   * @return the required value computed and scaled by the number of contracts
   */
  private double getComputedValue(EquityFuture derivative, SimpleFutureDataBundle bundle, Trade trade) {
    final double value;
    EquityFuturesPricer pricer = EquityFuturePricerFactory.getMethod(getPricingMethodName());
    if (_valueRequirementName.equals(ValueRequirementNames.PRESENT_VALUE)) {
      value = pricer.presentValue(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.VALUE_DELTA)) {
      value = pricer.spotDelta(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.VALUE_RHO)) {
      value = pricer.ratesDelta(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.PV01)) {
      value = pricer.pv01(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.SPOT)) {
      value = pricer.spotPrice(derivative, bundle);
    } else if (_valueRequirementName.equals(ValueRequirementNames.FORWARD)) {
      value = pricer.forwardPrice(derivative, bundle);
    } else {
      throw new OpenGammaRuntimeException("_valueRequirementName," + _valueRequirementName + ", unexpected. Should have been recognized in the constructor.");
    }
    return value;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof FutureSecurity;  // was: EquityFutureSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final FutureSecurity security = (FutureSecurity)  target.getTrade().getSecurity();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    // Spot
    final ValueRequirement refPriceReq = getReferencePriceRequirement(context, security);
    if (refPriceReq == null) {
      return null;
    }
    requirements.add(refPriceReq);
    // Funding curve
    final String fundingCurveName = getFundingCurveName(desiredValue);
    if (fundingCurveName == null) {
      return null;
    }
    // Curve configuration
    final String curveConfigName = getCurveConfigName(desiredValue);
    if (curveConfigName == null) {
      return null;
    }
    switch (getPricingMethodEnum()) {
      case MARK_TO_MARKET:
        requirements.add(getMarketPriceRequirement(security));
        if (getValueRequirementName() == ValueRequirementNames.SPOT) {
          requirements.add(getSpotAssetRequirement(security));
        }
        break;
      case COST_OF_CARRY:
        requirements.add(getSpotAssetRequirement(security));
        requirements.add(getCostOfCarryRequirement(security));
        break;
      case DIVIDEND_YIELD:
        final ValueRequirement curveReq = getDiscountCurveRequirement(fundingCurveName, curveConfigName, security);
        if (curveReq == null) {
          return null;
        }
        requirements.add(curveReq);
        final ValueRequirement dividendYieldReq = getDividendYieldRequirement(context, security);
        if (dividendYieldReq == null) {
          return null;
        }
        requirements.add(dividendYieldReq);
        requirements.add(getSpotAssetRequirement(security));
        break;
      default:
        throw new OpenGammaRuntimeException("Unhandled _pricingMethod=" + _pricingMethod);
    }
    return requirements;
  }

  protected String getFundingCurveName(ValueRequirement desiredValue) {
    final Set<String> fundingCurves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if (fundingCurves == null || fundingCurves.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.CURVE);
      return null;
    }
    final String fundingCurveName = fundingCurves.iterator().next();
    return fundingCurveName;
  }

  protected String getCurveConfigName(ValueRequirement desiredValue) {
    final Set<String> curveConfigNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveConfigNames == null || curveConfigNames.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      return null;
    }
    final String curveConfigName = curveConfigNames.iterator().next();
    return curveConfigName;
  }

  protected ValueRequirement getDiscountCurveRequirement(String fundingCurveName, final String curveCalculationConfigName, FutureSecurity security) {
    ValueProperties properties = ValueProperties.builder()
      .with(ValuePropertyNames.CURVE, fundingCurveName)
      .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
      .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }

  protected YieldAndDiscountCurve getYieldCurve(final FutureSecurity security, final FunctionInputs inputs, final String fundingCurveName, final String curveCalculationConfigName) {
    final ValueRequirement curveRequirement = getDiscountCurveRequirement(fundingCurveName, curveCalculationConfigName, security);
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveRequirement);
    }
    return (YieldAndDiscountCurve) curveObject;
  }

  private ValueRequirement getDividendYieldRequirement(FutureSecurity security) {
    ExternalId id = getSpotAssetId(security);
    return new ValueRequirement(MarketDataRequirementNames.DIVIDEND_YIELD, id);
  }

  @SuppressWarnings("unused")
  private Double getDividendYield(FutureSecurity security, FunctionInputs inputs) {
    ValueRequirement dividendRequirement = getDividendYieldRequirement(security);
    final Object dividendObject = inputs.getValue(dividendRequirement);
    if (dividendObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + dividendRequirement);
    }
    return (Double) dividendObject;
  }

  private ValueRequirement getSpotAssetRequirement(FutureSecurity security) {
    ExternalId spotAssetId = getSpotAssetId(security);
    ValueRequirement req = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotAssetId);
    return req;
  }

  protected ExternalId getSpotAssetId(FutureSecurity sec) {
    try {
      ExternalId spotAssetId = ((EquityFutureSecurity) sec).getUnderlyingId();
      return spotAssetId;
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException(sec.getContractCategory() + " failed to find spot asset. "
          + "COST_OF_CARRY and DIVIDEND_YIELD models are only available to Futures where Spot asset prices are available. "
          + "Contact Quant if spot asset should be available for this future.");
    }
  }

  protected Double getSpot(FutureSecurity security, FunctionInputs inputs) {
    ValueRequirement spotRequirement = getSpotAssetRequirement(security);
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    }
    return (Double) spotObject;
  }

  private ValueRequirement getCostOfCarryRequirement(FutureSecurity security) {

    return new ValueRequirement(MarketDataRequirementNames.COST_OF_CARRY, getSpotAssetId(security));
  }

  protected Double getCostOfCarry(FutureSecurity security, FunctionInputs inputs) {
    ValueRequirement costOfCarryRequirement = getCostOfCarryRequirement(security);
    final Object costOfCarryObject = inputs.getValue(costOfCarryRequirement);
    if (costOfCarryObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + costOfCarryRequirement);
    }
    return (Double) costOfCarryObject;
  }

  private ValueRequirement getMarketPriceRequirement(Security security) {
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, security.getUniqueId());
  }

  protected Double getMarketPrice(Security security, FunctionInputs inputs) {
    ValueRequirement marketPriceRequirement = getMarketPriceRequirement(security);
    final Object marketPriceObject = inputs.getValue(marketPriceRequirement);
    if (marketPriceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + marketPriceRequirement);
    }
    return (Double) marketPriceObject;
  }

  private ValueRequirement getReferencePriceRequirement(final FunctionCompilationContext context, final FutureSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    ExternalIdBundle idBundle = security.getExternalIdBundle();
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      s_logger.warn("Failed to find time series for: " + idBundle.toString());
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)), true, DateConstraint.VALUATION_TIME, true);
  }

  private ValueRequirement getDividendYieldRequirement(final FunctionCompilationContext context, final FutureSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(ExternalIdBundle.of(getSpotAssetId(security)), null, null, null, DIVIDEND_YIELD_FIELD, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, DIVIDEND_YIELD_FIELD,
        DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)), true, DateConstraint.VALUATION_TIME, true);
  }

  /**
   * Gets the valueRequirementName.
   * @return the valueRequirementName
   */
  protected final String getValueRequirementName() {
    return _valueRequirementName;
  }

  /**
   * Gets the pricingMethod.
   * @return the pricingMethod
   */
  protected final EquityFuturesPricingMethod getPricingMethodEnum() {
    return _pricingMethod;
  }

  /**
   * Gets the pricingMethodName.
   * @return the pricingMethodName
   */
  protected final String getPricingMethodName() {
    return _pricingMethodName;
  }


  private static final Logger s_logger = LoggerFactory.getLogger(EquityFuturesFunction.class);
}
