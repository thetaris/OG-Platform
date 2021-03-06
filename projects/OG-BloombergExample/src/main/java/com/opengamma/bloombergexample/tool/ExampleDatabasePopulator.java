/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.tool;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.bbg.loader.BloombergSecurityLoader;
import com.opengamma.bloombergexample.generator.BloombergExamplePortfolioGeneratorTool;
import com.opengamma.bloombergexample.loader.CurveNodeHistoricalDataLoader;
import com.opengamma.bloombergexample.loader.DemoEquityOptionCollarPortfolioLoader;
import com.opengamma.bloombergexample.loader.ExampleCurveAndSurfaceDefinitionLoader;
import com.opengamma.bloombergexample.loader.ExampleCurveConfigurationLoader;
import com.opengamma.bloombergexample.loader.ExampleEquityPortfolioLoader;
import com.opengamma.bloombergexample.loader.ExampleMultiCurrencySwapPortfolioLoader;
import com.opengamma.bloombergexample.loader.ExampleTimeSeriesRatingLoader;
import com.opengamma.bloombergexample.loader.ExampleViewsPopulator;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.analytics.volatility.surface.FXOptionVolatilitySurfaceConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyPairsConfigPopulator;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Single class that populates the database with data for running the example server.
 * <p>
 * It is designed to run against the HSQLDB example database.
 */
@Scriptable
public class ExampleDatabasePopulator extends AbstractTool<IntegrationToolContext> {

  /**
   * Example configuration for tools.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:toolcontext/toolcontext-bloombergexample.properties";
  /**
   * The name of the generated example FX portfolio.
   */
  public static final String FX_PORTFOLIO_NAME = "FX Portfolio";

  /**
   * The name of the multi-currency swap portfolio.
   */
  public static final String MULTI_CURRENCY_SWAP_PORTFOLIO_NAME = "MultiCurrency Swap Portfolio";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleDatabasePopulator.class);

  //-------------------------------------------------------------------------

  /**
   * Main method to run the tool.
   * No arguments are needed.
   *
   * @param args  the arguments, unused
   */
  public static void main(final String[] args) { // CSIGNORE
    s_logger.info("Populating example database");
    try {
      new ExampleDatabasePopulator().initAndRun(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null, IntegrationToolContext.class);
    } catch (final Exception ex) {
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
    }
  }

  private final Set<ExternalIdBundle> _futuresToLoad = new HashSet<ExternalIdBundle>();
  private final Set<ExternalId> _historicalDataToLoad = new HashSet<ExternalId>();

  @Override
  protected void doRun() {
    loadCurrencyConfiguration();
    loadCurveAndSurfaceDefinitions();
    loadCurveCalculationConfigurations();
    loadCurveNodeHistoricalData();
    loadFutures();
    loadTimeSeriesRating();
    loadEquityPortfolio();
    // Note: FX rates have to be loaded before the FX portfolio can be generated
    _historicalDataToLoad.add(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "EURUSD Curncy"));
    loadHistoricalData();
    loadVolSurfaceData();
    loadFXPortfolio();
    loadMultiCurrencySwapPortfolio();
    loadEquityOptionPortfolio();
    loadViews();
  }

  /**
   * Logging helper. All stages must go through this. When run as part of the Windows install, the logger is customized to recognize messages formatted in this fashion and route them towards the
   * progress indicators.
   */
  private static final class Log {

    private final String _str;

    private Log(final String str) {
      s_logger.info("{}", str);
      _str = str;
    }

    private void done() {
      s_logger.debug("{} - finished", _str);
    }

    private void fail(final RuntimeException e) {
      s_logger.error("{} - failed - {}", _str, e.getMessage());
      throw e;
    }

  }

  private BloombergExamplePortfolioGeneratorTool portfolioGeneratorTool() {
    final BloombergExamplePortfolioGeneratorTool tool = new BloombergExamplePortfolioGeneratorTool();
    tool.setCounterPartyGenerator(new StaticNameGenerator(AbstractPortfolioGeneratorTool.DEFAULT_COUNTER_PARTY));
    return tool;
  }

  private void loadCurrencyConfiguration() {
    final Log log = new Log("Loading currency reference data");
    try {
      final ConfigMaster configMaster = getToolContext().getConfigMaster();
      CurrencyPairsConfigPopulator.populateCurrencyPairsConfigMaster(configMaster);
      CurrencyMatrixConfigPopulator.populateCurrencyMatrixConfigMaster(configMaster);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurveAndSurfaceDefinitions() {
    final Log log = new Log("Creating curve and surface definitions");
    try {
      final ExampleCurveAndSurfaceDefinitionLoader curveLoader = new ExampleCurveAndSurfaceDefinitionLoader();
      curveLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurveCalculationConfigurations() {
    final Log log = new Log("Creating curve calculation configurations");
    try {
      final ExampleCurveConfigurationLoader curveConfigLoader = new ExampleCurveConfigurationLoader();
      curveConfigLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadEquityOptionPortfolio() {
    final Log log = new Log("Loading example Equity Option portfolio");
    try {
      final DemoEquityOptionCollarPortfolioLoader loader = new DemoEquityOptionCollarPortfolioLoader();
      loader.setNumOptions(2);
      loader.setNumMembers(8);
      loader.setNumContracts(new BigDecimal(500));
      loader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadCurveNodeHistoricalData() {
    final Log log = new Log("Loading curve node historical data");
    try {
      final CurveNodeHistoricalDataLoader curveNodeHistoricalDataLoader = new CurveNodeHistoricalDataLoader();
      curveNodeHistoricalDataLoader.run(getToolContext());
      _historicalDataToLoad.addAll(curveNodeHistoricalDataLoader.getCurveNodesExternalIds());
      _historicalDataToLoad.addAll(curveNodeHistoricalDataLoader.getInitialRateExternalIds());
      _futuresToLoad.addAll(curveNodeHistoricalDataLoader.getFuturesExternalIds());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadVolSurfaceData() {
    final Log log = new Log("Creating volatility surface configurations");
    try {
      FXOptionVolatilitySurfaceConfigPopulator.populateVolatilitySurfaceConfigMaster(getToolContext().getConfigMaster());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadTimeSeriesRating() {
    final Log log = new Log("Creating time series configurations");
    try {
      final ExampleTimeSeriesRatingLoader timeSeriesRatingLoader = new ExampleTimeSeriesRatingLoader();
      timeSeriesRatingLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFutures() {
    final Log log = new Log("Loading Futures reference data");
    try {
      final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
      final SecurityProvider securityProvider = getToolContext().getSecurityProvider();
      final BloombergSecurityLoader securityLoader = new BloombergSecurityLoader(securityProvider, securityMaster);
      securityLoader.loadSecurity(_futuresToLoad);
      _futuresToLoad.clear();
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadHistoricalData() {
    final Log log = new Log("Loading historical reference data");
    try {
      final BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(
          getToolContext().getHistoricalTimeSeriesMaster(),
          getToolContext().getHistoricalTimeSeriesProvider(),
          new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));
      for (final SecurityDocument doc : readEquitySecurities()) {
        final Security security = doc.getSecurity();
        loader.addTimeSeries(ImmutableSet.of(security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER)), "UNKNOWN", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
      }
      loader.addTimeSeries(_historicalDataToLoad, "UNKNOWN", "PX_LAST", LocalDate.now().minusYears(1), LocalDate.now());
      _historicalDataToLoad.clear();
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private Iterable<SecurityDocument> readEquitySecurities() {
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setSecurityType(EquitySecurity.SECURITY_TYPE);
    return SecuritySearchIterator.iterable(securityMaster, request);
  }

  private void loadEquityPortfolio() {
    final Log log = new Log("Loading example Equity portfolio");
    try {
      final ExampleEquityPortfolioLoader equityLoader = new ExampleEquityPortfolioLoader();
      equityLoader.run(getToolContext());
      _historicalDataToLoad.add(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "SPX Index"));
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadMultiCurrencySwapPortfolio() {
    final Log log = new Log("Creating example multi-currency swap portfolio");
    try {
      final ExampleMultiCurrencySwapPortfolioLoader multiCurrSwapLoader = new ExampleMultiCurrencySwapPortfolioLoader();
      multiCurrSwapLoader.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadFXPortfolio() {
    final Log log = new Log("Creating example FX portfolio");
    try {
      portfolioGeneratorTool().run(getToolContext(), FX_PORTFOLIO_NAME, "EuroDollarFX", true, null);
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

  private void loadViews() {
    final Log log = new Log("Creating example view definitions");
    try {
      final ExampleViewsPopulator populator = new ExampleViewsPopulator();
      populator.run(getToolContext());
      log.done();
    } catch (final RuntimeException t) {
      log.fail(t);
    }
  }

}
