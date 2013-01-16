/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import javax.time.CalendricalException;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Utilities and constants for {@code Security}.
 * <p>
 * This is a thread-safe static utility class.
 */
@PublicAPI
public class ExternalSchemes {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExternalSchemes.class);

  // --------------------------- SCHEMES FOR SECURITIES AND RATES -----------------------------
  /**
   * Identification scheme for the ISIN code.
   */
  public static final ExternalScheme ISIN = ExternalScheme.of("ISIN");
  /**
   * Identification scheme for the CUSIP code.
   */
  public static final ExternalScheme CUSIP = ExternalScheme.of("CUSIP");
  /**
   * Identification scheme for SEDOL1.
   */
  public static final ExternalScheme SEDOL1 = ExternalScheme.of("SEDOL1");
  /**
   * Identification scheme for Bloomberg BUIDs.
   */
  public static final ExternalScheme BLOOMBERG_BUID = ExternalScheme.of("BLOOMBERG_BUID");
  /**
   * Identification scheme for weak Bloomberg BUIDs.
   * A weak ID permits the underlying market data to return old data.
   */
  public static final ExternalScheme BLOOMBERG_BUID_WEAK = ExternalScheme.of("BLOOMBERG_BUID_WEAK");
  /**
   * Identification scheme for Bloomberg tickers.
   */
  public static final ExternalScheme BLOOMBERG_TICKER = ExternalScheme.of("BLOOMBERG_TICKER");
  /**
   * Identification scheme for weak Bloomberg tickers.
   * A weak ID permits the underlying market data to return old data.
   */
  public static final ExternalScheme BLOOMBERG_TICKER_WEAK = ExternalScheme.of("BLOOMBERG_TICKER_WEAK");
  /**
   * Identification scheme for Bloomberg tickers.
   */
  public static final ExternalScheme BLOOMBERG_TCM = ExternalScheme.of("BLOOMBERG_TCM");
  /**
   * Identification scheme for Reuters RICs.
   */
  public static final ExternalScheme RIC = ExternalScheme.of("RIC");
  /**
   * Identification scheme for ActivFeed tickers.
   */
  public static final ExternalScheme ACTIVFEED_TICKER = ExternalScheme.of("ACTIVFEED_TICKER");
  /**
   * Identification scheme for OpenGamma synthetic instruments.
   */
  public static final ExternalScheme OG_SYNTHETIC_TICKER = ExternalScheme.of("OG_SYNTHETIC_TICKER");
  /**
   * Identification scheme for Tullet-Prebon SURF tickers.
   */
  public static final ExternalScheme SURF = ExternalScheme.of("SURF");
  /**
   * Identification scheme for ICAP market data feed tickers.
   */
  public static final ExternalScheme ICAP = ExternalScheme.of("ICAP");
  /**
   * Identification scheme for Interactive Brokers contracts.
   */
  public static final ExternalScheme IB_CONTRACT = ExternalScheme.of("IB_CONTRACT");

  //-------------------- SCHEMES FOR REGIONS ---------------------

  /**
   * Identification scheme for the ISO alpha 2 country code ISO standard.
   */
  public static final ExternalScheme ISO_COUNTRY_ALPHA2 = ExternalScheme.of("ISO_COUNTRY_ALPHA2");

  /**
   * Identification scheme for the ISO alpha 3 currency code ISO standard.
   */
  public static final ExternalScheme ISO_CURRENCY_ALPHA3 = ExternalScheme.of("ISO_CURRENCY_ALPHA3");

  /**
   * Identification scheme for the Copp Clark version of UN/LOCODE , formatted without spaces.
   */
  public static final ExternalScheme COPP_CLARK_LOCODE = ExternalScheme.of("COPP_CLARK_LOCODE");

  /**
   * Identification scheme for the UN/LOCODE 2010-2 code standard, formatted without spaces.
   */
  public static final ExternalScheme UN_LOCODE_2010_2 = ExternalScheme.of("UN_LOCODE_2010_2");

  /**
   * Identification scheme for the tz database time-zone standard.
   */
  public static final ExternalScheme TZDB_TIME_ZONE = ExternalScheme.of("TZDB_TIME_ZONE");

  /**
   * Identification scheme for financial activity.
   * This currently tends to be the country code, but can be more complex.
   */
  public static final ExternalScheme FINANCIAL = ExternalScheme.of("FINANCIAL_REGION");

  // --------------------- SCHEMES FOR EXCHANGES ---------------------------
  /**
   * Identification scheme for the MIC exchange code ISO standard.
   */
  public static final ExternalScheme ISO_MIC = ExternalScheme.of("ISO_MIC");


  /**
   * Restricted constructor.
   */
  protected ExternalSchemes() {
  }

  //------------------ METHODS FOR SECURITIES AND RATES ----------------------
  /**
   * Creates an ISIN code.
   * <p>
   * This is the international standard securities identifying number.
   * The first two characters are the ISO country code, followed by a 9 character
   * alphanumeric national code and a single numeric check-digit.
   * Example might be {@code US0231351067}.
   * 
   * @param code  the ISIN code, not null
   * @return the security identifier, not null
   */
  public static ExternalId isinSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    }
    //    if (code.matches("[A-Z]{2}[A-Z0-9]{9}[0-9]") == false) {
    //      throw new IllegalArgumentException("ISIN code is invalid: " + code);
    //    }
    return ExternalId.of(ISIN, code);
  }

  /**
   * Creates a CUSIP code.
   * <p>
   * This is the national securities identifying number for USA and Canada.
   * The code should be 8 alphanumeric characters followed by a single numeric check-digit.
   * Example might be {@code 023135106}.
   * 
   * @param code  the CUSIP code, not null
   * @return the security identifier, not null
   */
  public static ExternalId cusipSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    }
    //    if (code.matches("[A-Z0-9]{8}[0-9]?") == false) {
    //      throw new IllegalArgumentException("CUSIP code is invalid: " + code);
    //    }
    return ExternalId.of(CUSIP, code);
  }

  /**
   * Creates a SEDOL code.
   * <p>
   * This is the national securities identifying number for UK and Ireland.
   * The code should be 6 alphanumeric characters followed by a single numeric check-digit.
   * Example might be {@code 0263494}.
   * 
   * @param code  the SEDOL code, not null
   * @return the security identifier, not null
   */
  public static ExternalId sedol1SecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    }
    //    if (code.matches("[A-Z0-9]{6}[0-9]?") == false) {
    //      throw new IllegalArgumentException("SEDOL1 code is invalid: " + code);
    //    }
    return ExternalId.of(SEDOL1, code);
  }

  /**
   * Creates a Bloomberg BIUD code.
   * <p>
   * This is the BUID code supplied by Bloomberg.
   * Examples might be {@code EQ0010599500001000} or {@code IX6572023-0}.
   * 
   * @param code  the Bloomberg BIUD code, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergBuidSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("BUID code is invalid: " + code);
    }
    return ExternalId.of(BLOOMBERG_BUID, code);
  }

  /**
   * Creates a Bloomberg ticker.
   * <p>
   * This is the ticker supplied by Bloomberg.
   * Examples might be {@code MCO US Equity} or {@code CSCO US 01/21/12 C30 Equity}.
   * 
   * @param ticker  the Bloomberg ticker, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(BLOOMBERG_TICKER, ticker);
  }

  /**
   * Creates a Synthetic ticker.
   * <p>
   * This is the ticker used mainly by OG-Examples
   * 
   * @param ticker  the OG-Synthetic ticker, not null
   * @return the security identifier, not null
   */
  public static ExternalId syntheticSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(OG_SYNTHETIC_TICKER, ticker);
  }

  /**
   * Creates a Bloomberg ticker coupon maturity identifier.
   * <p>
   * This is the ticker combined with a coupon and a maturity supplied by Bloomberg.
   * Example might be {@code T 4.75 15/08/43 Govt}.
   * 
   * @param tickerWithoutSector  the Bloomberg ticker without the sector, not null
   * @param coupon  the coupon, not null
   * @param maturity  the maturity date, not null
   * @param marketSector  the sector, not null
   * @return the security identifier, not null
   */
  public static ExternalId bloombergTCMSecurityId(final String tickerWithoutSector, final String coupon, final String maturity, final String marketSector) {
    ArgumentChecker.notNull(tickerWithoutSector, "tickerWithoutSector");
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(maturity, "maturity");
    ArgumentChecker.notNull(marketSector, "marketSector");
    if (StringUtils.isEmpty(tickerWithoutSector)) {
      throw new IllegalArgumentException("Ticker (without sector) must not be empty");
    }
    if (StringUtils.isEmpty(coupon)) {
      throw new IllegalArgumentException("Coupon must not be empty, ticker = " + tickerWithoutSector);
    }
    if (StringUtils.isEmpty(maturity)) {
      throw new IllegalArgumentException("Maturity must not be empty, ticker = " + tickerWithoutSector + ", coupon = " + coupon);
    }
    if (StringUtils.isEmpty(marketSector)) {
      throw new IllegalArgumentException("Market sector must not be empty, ticker = " + tickerWithoutSector + ", coupon = " + coupon + ", maturity = " + maturity);
    }
    Double couponDbl;
    try {
      couponDbl = Double.parseDouble(coupon);
    } catch (final NumberFormatException ex) {
      throw new IllegalArgumentException("Coupon must be a valid double, ticker=" + tickerWithoutSector + ", coupon=" + coupon, ex);
    }
    if (s_logger.isDebugEnabled()) {
      try {
        LocalDate.parse(maturity, DateTimeFormatters.pattern("MM/dd/YY"));
      } catch (final UnsupportedOperationException uoe) {
        s_logger.warn("Problem parsing maturity " + maturity + " ticker=" + tickerWithoutSector + ", coupon=" + coupon);
      } catch (final CalendricalException ce) {
        s_logger.warn("Problem parsing maturity " + maturity + " ticker=" + tickerWithoutSector + ", coupon=" + coupon);
      }
    }
    return ExternalId.of(BLOOMBERG_TCM, tickerWithoutSector + " " + couponDbl + " " + maturity + " " + marketSector);
  }

  /**
   * Creates a Reuters RIC code.
   * <p>
   * This is the RIC code supplied by Reuters.
   * Example might be {@code MSFT.OQ}.
   * 
   * @param code  the BIUD code, not null
   * @return the security identifier, not null
   */
  public static ExternalId ricSecurityId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("RIC code is invalid: " + code);
    }
    return ExternalId.of(RIC, code);
  }

  /**
   * Creates an ActivFeed ticker.
   * <p>
   * This is the ticker used by ActivFeed.
   * Examples might be {@code IBM.N} or {@code C/04H.CB}.
   * 
   * @param ticker  the ActivFeed ticker, not null
   * @return the security identifier, not null
   */
  public static ExternalId activFeedTickerSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "code");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(ACTIVFEED_TICKER, ticker);
  }

  /**
   * Creates a Tullett-Prebon ticker.
   * <p>
   * This is the ticker used by Tullett-Prebon.
   * An example is {@code ASIRSUSD20Y30S03L}.
   * 
   * @param ticker The Tullett-Prebon ticker, not null
   * @return The security identifier, not null
   */
  public static ExternalId tullettPrebonSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(SURF, ticker);
  }

  /**
   * Creates an ICAP ticker.
   * <p>
   * This is the ticker used by ICAP.
   * 
   * @param ticker The ICAP ticker, not null
   * @return The security identifier, not null
   */
  public static ExternalId icapSecurityId(final String ticker) {
    ArgumentChecker.notNull(ticker, "ticker");
    if (ticker.length() == 0) {
      throw new IllegalArgumentException("Ticker is invalid: " + ticker);
    }
    return ExternalId.of(ICAP, ticker);
  }

  /**
   * Creates an Interactive Brokers unique ID.
   * <p>
   * This UID is comprised of two key parts: <ol>
   * <li>the contract identifier (Conid). This code is supplied by Interactive Brokers. 
   * <li>the exchange symbol, as defined by Interactive Brokers.
   * </ol>
   * Examples might be: <ul>
   * <li>{@code 12087792@IDEALPRO} for the EUR.USD Forex rate 
   * <li>{@code 825711@DTB} for the DAX Index (DTB is the IB symbol for EUREX)
   * <li>{@code 1411277@FWB} for the IBM stock (FWB stands for Frankfurter Wertpapierboerse) 
   * </ul>
   * 
   * TODO: determine whether the exchange symbol can always be queried via the IB interface using the contract ID, 
   * in which case this method would become obsolete and replaced by {@link ExternalSchemes#ibContractID(String)}.
   *  
   * @param code the IB UID, not null
   * @return the security identifier, not null
   * @see ExternalSchemes#ibContractID(String)
   */
  public static ExternalId ibUID(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("Interactive Brokers unique ID is invalid: " + code);
    }
    return ExternalId.of(IB_CONTRACT, code);
  }

  /**
   * Creates an Interactive Brokers contract ID.
   * <p>
   * This code represents the contract identifier (Conid) as supplied by Interactive Brokers. <br/>
   * Examples might be: <ul>
   * <li>{@code 12087792} for the EUR.USD Forex rate traded at IDEALPRO 
   * <li>{@code 825711} for the DAX Index traded at DTB (the IB symbol for EUREX)
   * <li>{@code 1411277} for the IBM stock traded at FWB (stands for Frankfurter Wertpapierboerse) 
   * </ul>
   * 
   * @param code the IB contract ID, not null
   * @return the security identifier, not null
   */
  public static ExternalId ibContractID(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.length() == 0) {
      throw new IllegalArgumentException("Interactive Brokers contract ID is invalid: " + code);
    }
    return ExternalId.of(IB_CONTRACT, code);
  }

  // -------------------------- METHODS FOR REGIONS ---------------------------

  /**
   * Creates an identifier for a financial location.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static ExternalId financialRegionId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z+]+") == false) {
      throw new IllegalArgumentException("Code is invalid: " + code);
    }
    return ExternalId.of(ExternalSchemes.FINANCIAL, code);
  }

  /**
   * Creates a tz database time-zone code.
   * <p>
   * Examples might be {@code Europe/London} or {@code Asia/Hong_Kong}.
   * 
   * @param zone  the time-zone, not null
   * @return the region identifier, not null
   */
  public static ExternalId timeZoneRegionId(final TimeZone zone) {
    ArgumentChecker.notNull(zone, "zone");
    return ExternalId.of(ExternalSchemes.TZDB_TIME_ZONE, zone.getID());
  }

  /**
   * Creates a Copp Clark location code, formatted without spaces.
   * This is based on UN/LOCODE.
   * <p>
   * Examples might be {@code GBHOH} or {@code AEDXB}.
   * 
   * @param locode  the Copp Clark LOCODE, not null
   * @return the region identifier, not null
   */
  public static ExternalId coppClarkRegionId(final String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("Copp Clark LOCODE is invalid: " + locode);
    }
    return ExternalId.of(ExternalSchemes.COPP_CLARK_LOCODE, locode);
  }

  /**
   * Creates a UN/LOCODE 2010-2 code, formatted without spaces.
   * <p>
   * Examples might be {@code GBHOH} or {@code AEDXB}.
   * 
   * @param locode  the UN/LOCODE, not null
   * @return the region identifier, not null
   */
  public static ExternalId unLocode20102RegionId(final String locode) {
    ArgumentChecker.notNull(locode, "locode");
    if (locode.matches("[A-Z]{2}[A-Z0-9]{3}") == false) {
      throw new IllegalArgumentException("UN/LOCODE is invalid: " + locode);
    }
    return ExternalId.of(ExternalSchemes.UN_LOCODE_2010_2, locode);
  }

  /**
   * Creates an ISO alpha 3 currency code.
   * <p>
   * Examples might be {@code GBP} or {@code USD}.
   * 
   * @param currency  the currency, not null
   * @return the region identifier, not null
   */
  public static ExternalId currencyRegionId(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return ExternalId.of(ExternalSchemes.ISO_CURRENCY_ALPHA3, currency.getCode());
  }

  /**
   * Creates an ISO alpha 2 country code.
   * <p>
   * Examples might be {@code GB} or {@code US}.
   * 
   * @param country  the country, not null
   * @return the region identifier, not null
   */
  public static ExternalId countryRegionId(final Country country) {
    ArgumentChecker.notNull(country, "country");
    return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, country.getCode());
  }

  //---------------------- METHODS FOR EXCHANGES ---------------------
  /**
   * Creates an ISO MIC code.
   * <p>
   * Examples might be {@code XLON} or {@code XNYS}.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static ExternalId isoMicExchangeId(final String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z0-9]{4}([-][A-Z0-9]{3})?") == false) {
      throw new IllegalArgumentException("ISO MIC code is invalid: " + code);
    }
    return ExternalId.of(ExternalSchemes.ISO_MIC, code);
  }

}
