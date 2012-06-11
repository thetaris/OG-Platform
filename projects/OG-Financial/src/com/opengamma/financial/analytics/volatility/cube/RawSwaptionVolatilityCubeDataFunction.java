/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
import com.opengamma.financial.analytics.volatility.SwaptionVolatilityCubeSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class RawSwaptionVolatilityCubeDataFunction extends AbstractFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawSwaptionVolatilityCubeDataFunction.class);

  @SuppressWarnings("unchecked")
  public static <X, Y, Z> Set<ValueRequirement> buildDataRequirements(final SwaptionVolatilityCubeSpecificationSource specificationSource,
      final SwaptionVolatilityCubeDefinitionSource definitionSource, final ZonedDateTime atInstant, final ComputationTarget target,
      final String specificationName, final String definitionName) {
    final String uniqueId = target.getUniqueId().getValue();
    final String fullSpecificationName = specificationName + "_" + uniqueId;
    final String fullDefinitionName = definitionName + "_" + uniqueId;
    final SwaptionVolatilityCubeSpecification specification = specificationSource.getSpecification(fullSpecificationName);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube specification name " + fullSpecificationName);
    }
    final SwaptionVolatilityCubeDefinition<X, Y, Z> definition = (SwaptionVolatilityCubeDefinition<X, Y, Z>) definitionSource.getDefinition(fullDefinitionName);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition name " + fullDefinitionName);
    }
    final CubeInstrumentProvider<X, Y, Z> provider = (CubeInstrumentProvider<X, Y, Z>) specification.getCubeInstrumentProvider();
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final X x : definition.getXs()) {
      for (final Y y : definition.getYs()) {
        for (final Z z : definition.getZs()) {
          final ExternalId identifier = provider.getInstrument(x, y, z);
          result.add(new ValueRequirement(provider.getDataFieldName(), identifier));
        }
      }
    }
    return result;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final InstantProvider atInstantProvider) {
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(compilationContext);
    final ConfigDBSwaptionVolatilityCubeDefinitionSource definitionSource = new ConfigDBSwaptionVolatilityCubeDefinitionSource(configSource);
    final ConfigDBSwaptionVolatilityCubeSpecificationSource specificationSource = new ConfigDBSwaptionVolatilityCubeSpecificationSource(configSource);
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new AbstractInvokingCompiledFunction() {

      @SuppressWarnings("unchecked")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String cubeName = desiredValue.getConstraint(ValuePropertyNames.CUBE);
        final String definitionName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
        final String specificationName = desiredValue.getConstraint(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
        final String uniqueId = target.getUniqueId().getValue();
        final String fullSpecificationName = specificationName + "_" + uniqueId;
        final String fullDefinitionName = definitionName + "_" + uniqueId;
        final SwaptionVolatilityCubeSpecification specification = specificationSource.getSpecification(fullSpecificationName);
        if (specification == null) {
          throw new OpenGammaRuntimeException("Could not get swaption volatility cube specification name " + fullSpecificationName);
        }
        final SwaptionVolatilityCubeDefinition<Object, Object, Object> definition = (SwaptionVolatilityCubeDefinition<Object, Object, Object>) definitionSource.getDefinition(fullDefinitionName);
        if (definition == null) {
          throw new OpenGammaRuntimeException("Could not get swaption volatility cube definition name " + fullDefinitionName);
        }
        final CubeInstrumentProvider<Object, Object, Object> provider = (CubeInstrumentProvider<Object, Object, Object>) specification.getCubeInstrumentProvider();
        final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
        final HashMap<VolatilityPoint, ExternalId> dataIds = new HashMap<VolatilityPoint, ExternalId>();
        final HashMap<VolatilityPoint, Double> deltas = new HashMap<VolatilityPoint, Double>();
        final HashMap<UniqueId, Double> otherData = new HashMap<UniqueId, Double>();
        for (final Object x : definition.getXs()) {
          final Tenor swapMaturity = (Tenor) x;
          for (final Object y : definition.getYs()) {
            final Tenor swaptionExpiry = (Tenor) y;
            for (final Object z : definition.getZs()) {
              final Double delta = (Double) z;
              final ExternalId id = provider.getInstrument(x, y, z);
              final ValueRequirement requirement = new ValueRequirement(provider.getDataFieldName(), id);
              final Object volatilityObject = inputs.getValue(requirement);
              if (volatilityObject != null) {
                final Double volatility = (Double) volatilityObject;
                final VolatilityPoint volatilityPoint = new VolatilityPoint(swapMaturity, swaptionExpiry, delta);
                dataPoints.put(volatilityPoint, volatility);
                dataIds.put(volatilityPoint, id);
                deltas.put(volatilityPoint, delta);
              } else {
                otherData.put(requirement.getTargetSpecification().getUniqueId(), delta);
              }
            }
          }
        }
        final VolatilityCubeData volatilityCubeData = new VolatilityCubeData();
        volatilityCubeData.setDataPoints(dataPoints);
        volatilityCubeData.setDataIds(dataIds);
        volatilityCubeData.setRelativeStrikes(deltas);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CUBE, cubeName)
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION, definitionName)
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION, specificationName)
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE, specification.getCubeQuoteType())
            .with(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS, specification.getQuoteUnits()).get();
        return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties), volatilityCubeData));
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return true;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return target.getType() == ComputationTargetType.PRIMITIVE && Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CUBE)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_QUOTE_TYPE)
            .withAny(SurfaceAndCubePropertyNames.PROPERTY_CUBE_UNITS).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> cubeNames = constraints.getValues(ValuePropertyNames.CUBE);
        if (cubeNames == null || cubeNames.size() != 1) {
          s_logger.info("Can only get a single cube; asked for " + cubeNames);
          return null;
        }
        final Set<String> definitionNames = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_DEFINITION);
        if (definitionNames == null || definitionNames.size() != 1) {
          return null;
        }
        final Set<String> specificationNames = constraints.getValues(SurfaceAndCubePropertyNames.PROPERTY_CUBE_SPECIFICATION);
        if (specificationNames == null || specificationNames.size() != 1) {
          return null;
        }
        final String definitionName = definitionNames.iterator().next();
        final String specificationName = specificationNames.iterator().next();
        return buildDataRequirements(specificationSource, definitionSource, atInstant, target, specificationName, definitionName);
      }
    };
  }
}