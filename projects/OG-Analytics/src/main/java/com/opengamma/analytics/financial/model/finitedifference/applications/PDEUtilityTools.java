/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PDEUtilityTools {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(INTERPOLATOR_1D, INTERPOLATOR_1D);

  public static Map<Double, Interpolator1DDataBundle> getInterpolatorDataBundle(final PDEFullResults1D res) {
    final int tNodes = res.getNumberTimeNodes();
    final int xNodes = res.getNumberSpaceNodes();

    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = res.getTimeValue(i);

      for (int j = 0; j < xNodes; j++) {
        final double k = res.getSpaceValue(j);
        final DoublesPair tk = new DoublesPair(t, k);
        out.put(tk, res.getFunctionValue(j, i));
      }
    }

    final Map<Double, Interpolator1DDataBundle> dataBundle = GRID_INTERPOLATOR2D.getDataBundle(out);
    return dataBundle;
  }

  /**
   * Take the terminal result for a forward PDE (i.e. forward option prices) and returns a map between strikes and implied volatilities 
   * @param forwardCurve The forward curve
   * @param expiry The expiry of this option strip 
   * @param prices The results from the PDE solver 
   * @param minK The minimum strike to return 
   * @param maxK The maximum strike to return 
   * @param isCall true for call 
   * @return A map between strikes and implied volatilities 
   */
  public static Map<Double, Double> priceToImpliedVol(final ForwardCurve forwardCurve, final double expiry, final PDETerminalResults1D prices, final double minK, final double maxK,
      final boolean isCall) {
    final int n = prices.getNumberSpaceNodes();
    final double fwd = forwardCurve.getForward(expiry);
    final Map<Double, Double> out = new HashMap<Double, Double>(n);
    for (int j = 0; j < n; j++) {
      final double k = prices.getSpaceValue(j);
      if (k >= minK && k <= maxK) {
        final double price = prices.getFunctionValue(j);
        try {
          final double impVol = BlackFormulaRepository.impliedVolatility(price, 1.0, k, expiry, isCall);
          if (Math.abs(impVol) > 1e-15) {
            out.put(k, impVol);
          }
        } catch (final Exception e) {
        }
      }
    }
    return out;
  }

  /**
   * Takes the results from a forward PDE solve - grid of option prices by maturity and strike and returns a map between a DoublesPair (i.e. maturity and strike) and
   * the Black implied volatility
   * @param forwardCurve The forward
   * @param prices The forward (i.e. not discounted) option prices
   * @param minT Data before this time is ignored (not included in map)
   * @param maxT Data after this time is ignored (not included in map)
   * @param minK Strikes less than this are ignored (not included in map)
   * @param maxK Strikes greater than this are ignored (not included in map)
   * @param isCall true if call
   * @return The price to implied volatility map
   */
  public static Map<DoublesPair, Double> priceToImpliedVol(final ForwardCurve forwardCurve, final PDEFullResults1D prices, final double minT, final double maxT, final double minK, final double maxK,
      final boolean isCall) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      final double forward = forwardCurve.getForward(t);
      if (t >= minT && t <= maxT) {
        for (int j = 0; j < xNodes; j++) {
          final double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            final double price = prices.getFunctionValue(j, i);

            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(price, forward, k, t, isCall);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
              }
            } catch (final Exception e) {
            }
          }
        }
      }
    }
    return out;
  }

  public static Map<DoublesPair, Double> modifiedPriceToImpliedVol(final PDEFullResults1D prices, final double minT, final double maxT,
      final double minM, final double maxM, final boolean isCall) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      if (t >= minT && t <= maxT) {
        for (int j = 0; j < xNodes; j++) {
          final double m = prices.getSpaceValue(j);
          if (m >= minM && m <= maxM) {
            final double price = prices.getFunctionValue(j, i);

            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(price, 1.0, m, t, isCall);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
              }
            } catch (final Exception e) {
            }
          }
        }
      }
    }

    return out;
  }

  /**
   * Takes the results from a forward PDE solve - grid of option prices by maturity and strike and returns a map between a DoublesPair (i.e. maturity and strike) and
   * the Black implied volatility
   * @param forwardCurve The forward
   * @param discountCurve The discount curve
   * @param prices The option prices
   * @param minT Data before this time is ignored (not included in map)
   * @param maxT Data after this time is ignored (not included in map)
   * @param minK Strikes less than this are ignored (not included in map)
   * @param maxK Strikes greater than this are ignored (not included in map)
   * @return The price to implied volatility map
   */
  public static Map<DoublesPair, Double> priceToImpliedVol(final ForwardCurve forwardCurve,
      final YieldAndDiscountCurve discountCurve, final PDEFullResults1D prices, final double minT, final double maxT, final double minK, final double maxK) {
    final int xNodes = prices.getNumberSpaceNodes();
    final int tNodes = prices.getNumberTimeNodes();
    final int n = xNodes * tNodes;
    final Map<DoublesPair, Double> out = new HashMap<DoublesPair, Double>(n);

    for (int i = 0; i < tNodes; i++) {
      final double t = prices.getTimeValue(i);
      final double forward = forwardCurve.getForward(t);
      final double df = discountCurve.getDiscountFactor(t);
      if (t >= minT && t <= maxT) {
        for (int j = 0; j < xNodes; j++) {
          final double k = prices.getSpaceValue(j);
          if (k >= minK && k <= maxK) {
            final double forwardPrice = prices.getFunctionValue(j, i) / df;
            try {
              final double impVol = BlackFormulaRepository.impliedVolatility(forwardPrice, forward, k, t, true);
              if (Math.abs(impVol) > 1e-15) {
                final DoublesPair pair = new DoublesPair(prices.getTimeValue(i), prices.getSpaceValue(j));
                out.put(pair, impVol);
              }
            } catch (final Exception e) {
            }
          }
        }
      }
    }
    return out;
  }

  public static void printSurface(final String name, final PDEFullResults1D res) {
    final PrintStream out = System.out;
    printSurface(name, res, out);
  }

  public static void printSurface(final String name, final PDEFullResults1D res, final PrintStream out) {
    final int tNodes = res.getNumberTimeNodes();
    final int xNodes = res.getNumberSpaceNodes();

    out.println(name);
    for (int i = 0; i < xNodes; i++) {
      final double k = res.getSpaceValue(i);
      out.print("\t" + k);
    }
    out.print("\n");

    for (int j = 0; j < tNodes; j++) {
      final double t = res.getTimeValue(j);
      out.print(t);
      for (int i = 0; i < xNodes; i++) {
        out.print("\t" + res.getFunctionValue(i, j));
      }
      out.print("\n");
    }
    out.print("\n");
  }

  public static void printSurface(final String name, final Surface<Double, Double, Double> surface, final double xMin, final double xMax, final double yMin, final double yMax) {
    printSurface(name, surface, xMin, xMax, yMin, yMax, 100, 100);
  }

  public static void printSurface(final String name, final Surface<Double, Double, Double> surface, final double xMin, final double xMax, final double yMin, final double yMax, final int xSteps,
      final int ySteps) {

    Validate.isTrue(xMax > xMin, "need xMax > xMin");
    Validate.isTrue(yMax > yMin, "need yMax > yMin");
    Validate.isTrue(xSteps > 0, "need xSteps > 0");
    Validate.isTrue(ySteps > 0, "need ySteps > 0");

    StringBuffer result = new StringBuffer(name);
    result.append("\n");
    for (int i = 0; i <= ySteps; i++) {
      final double y = yMin + ((yMax - yMin) * i) / ySteps;
      result.append("\t");
      result.append(y);
    }
    result.append("\n");

    for (int j = 0; j <= xSteps; j++) {
      final double t = xMin + ((xMax - xMin) * j) / xSteps;
      result.append(t);
      for (int i = 0; i <= ySteps; i++) {
        final double k = yMin + ((yMax - yMin) * i) / ySteps;
        result.append("\t");
        result.append(surface.getZValue(t, k));
      }
      result.append("\n");
    }
    result.append("\n");
    System.out.println(result);
  }

  /** 
   * This form takes vectors of x (typically expiry) and y (typically strike) 
   *
   * @param name The name
   * @param surface The surface
   * @param x The x values
   * @param y The y values
   */
  public static void printSurface(final String name, final Surface<Double, Double, Double> surface, final double[] x, final double[] y) {
    Validate.isTrue(x.length > 0, "The x-array was empty");
    Validate.isTrue(y.length > 0, "The y-array was empty");

    StringBuffer result = new StringBuffer(name);
    result.append("\n");
    for (int i = 0; i < y.length; i++) {
      result.append("\t");
      result.append(y[i]);
    }
    result.append("\n");
    for (int j = 0; j < x.length; j++) {
      final double t = x[j];
      result.append(t);
      for (int i = 0; i < y.length; i++) {
        final double k = y[i];
        result.append("\t");
        result.append(surface.getZValue(t, k));
      }
      result.append("\n");
    }
    result.append("\n");
    System.out.println(result);
  }

  public static void printSurfaceInterpolate(final String name, final PDEFullResults1D res) {

    final Map<Double, Interpolator1DDataBundle> dataBundle = getInterpolatorDataBundle(res);
    final double tMin = res.getTimeValue(0);
    final double tMax = res.getTimeValue(res.getNumberTimeNodes() - 1);
    final double kMin = res.getSpaceValue(0);
    final double kMax = res.getSpaceValue(res.getNumberSpaceNodes() - 1);
    printSurface(name, dataBundle, tMin, tMax, kMin, kMax, 100, 100);
  }

  public static void printSurface(final String name, final Map<Double, Interpolator1DDataBundle> dataBundle, final double tMin, final double tMax, final double kMin, final double kMax,
      final int xSteps, final int ySteps) {

    System.out.println(name);
    for (int i = 0; i <= ySteps; i++) {
      final double k = kMin + ((kMax - kMin) * i) / ySteps;
      System.out.print("\t" + k);
    }
    System.out.print("\n");

    for (int j = 0; j <= xSteps; j++) {
      final double t = tMin + ((tMax - tMin) * j) / xSteps;
      System.out.print(t);
      for (int i = 0; i <= ySteps; i++) {
        final double k = kMin + ((kMax - kMin) * i) / ySteps;
        final DoublesPair tk = new DoublesPair(t, k);

        System.out.print("\t" + GRID_INTERPOLATOR2D.interpolate(dataBundle, tk));
      }
      System.out.print("\n");
    }
  }

}
