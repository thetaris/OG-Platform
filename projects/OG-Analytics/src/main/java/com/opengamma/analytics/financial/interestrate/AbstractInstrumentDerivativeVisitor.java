/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureForward;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyForward;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalForward;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIbor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborSpread;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.CrossCurrencySwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FixedFloatSwap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.FloatingRateNote;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TenorSwap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public abstract class AbstractInstrumentDerivativeVisitor<S, T> implements InstrumentDerivativeVisitor<S, T> {

  @Override
  public T visit(final InstrumentDerivative derivative, final S data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T[] visit(final InstrumentDerivative[] derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T visit(final InstrumentDerivative derivative) {
    ArgumentChecker.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T[] visit(final InstrumentDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurity()");
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransaction()");
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurity()");
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransaction()");
  }

  @Override
  public T visitBillSecurity(final BillSecurity bill, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillSecurity()");
  }

  @Override
  public T visitBillTransaction(final BillTransaction bill, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillTransaction()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMS()");
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpread()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityCouponIborRatchet()");
  }

  @Override
  public T visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIbor()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIbor()");
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIbor()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitCDSDerivative(final ISDACDSDerivative cds, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurity()");
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransaction()");
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurity()");
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransaction()");
  }

  @Override
  public T visitBillSecurity(final BillSecurity bill) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillSecurity()");
  }

  @Override
  public T visitBillTransaction(final BillTransaction bill) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBillTransaction()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitCash(final Cash cash, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  @Override
  public T visitCash(final Cash cash) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMS()");
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpread()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final Annuity<? extends Payment> genericAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityCouponIborRatchet()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIbor()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIbor()");
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIbor()");
  }

  @Override
  public T visitFixedCouponSwap(final SwapFixedCoupon<?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitCDSDerivative(final ISDACDSDerivative cds) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  // -----     Payment and coupon     -----

  @Override
  public T visitCouponFixed(final CouponFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponFixed(final CouponFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCouponIborSpread(final CouponIborSpread payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborSpread()");
  }

  @Override
  public T visitCouponIborSpread(final CouponIborSpread payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborSpread()");
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearing()");
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearing()");
  }

  @Override
  public T visitCouponIborCompounded(final CouponIborCompounded payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborCompounded()");
  }

  @Override
  public T visitCouponIborCompounded(final CouponIborCompounded payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborCompounded()");
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOIS()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponOIS()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  // -----     Inflation     -----

  @Override
  public T visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthly()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthly()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthlyGearing()");
  }

  @Override
  public T visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponMonthlyGearing()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolation()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolation()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolationGearing()");
  }

  @Override
  public T visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponInflationZeroCouponInterpolationGearing()");
  }

  @Override
  public T visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedSecurity()");
  }

  @Override
  public T visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedSecurity()");
  }

  @Override
  public T visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedTransaction()");
  }

  @Override
  public T visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondCapitalIndexedTransaction()");
  }

  // -----     Futures     -----

  @Override
  public T visitBondFuture(final BondFuture bondFuture, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitBondFuture(final BondFuture bondFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureSecurity()");
  }

  @Override
  public T visitInterestRateFuture(final InterestRateFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureSecurity()");
  }

  @Override
  public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureSecurity()");
  }

  @Override
  public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureSecurity()");
  }

  @Override
  public T visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureTransaction()");
  }

  @Override
  public T visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFederalFundsFutureSecurity()");
  }

  @Override
  public T visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDeliverableSwapFuturesSecurity()");
  }

  @Override
  public T visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDeliverableSwapFuturesSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransaction()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransaction()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitBondFutureOptionPremiumSecurity(final BondFutureOptionPremiumSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitBondFutureOptionPremiumTransaction(final BondFutureOptionPremiumTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureOptionPremiumSecurity()");
  }

  // -----     Annuity     -----

  // -----     Swap     -----

  @Override
  public T visitSwap(final Swap<?, ?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  // -----     Deposit     -----

  @Override
  public T visitDepositIbor(final DepositIbor deposit, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositIbor()");
  }

  @Override
  public T visitDepositIbor(final DepositIbor deposit) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositIbor()");
  }

  @Override
  public T visitDepositCounterpart(final DepositCounterpart deposit, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositCounterpart()");
  }

  @Override
  public T visitDepositCounterpart(final DepositCounterpart deposit) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositCounterpart()");
  }

  @Override
  public T visitDepositZero(final DepositZero deposit, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositZero()");
  }

  @Override
  public T visitDepositZero(final DepositZero deposit) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitDepositZero()");
  }

  // -----     Forex     -----

  @Override
  public T visitForex(final Forex derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForex(final Forex derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForex()");
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwap()");
  }

  @Override
  public T visitForexSwap(final ForexSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexSwap()");
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanilla()");
  }

  @Override
  public T visitForexOptionVanilla(final ForexOptionVanilla derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionVanilla()");
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrier()");
  }

  @Override
  public T visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionSingleBarrier()");
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForward()");
  }

  @Override
  public T visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableForward()");
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOption()");
  }

  @Override
  public T visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexNonDeliverableOption()");
  }

  @Override
  public T visitForexOptionDigital(final ForexOptionDigital derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionDigital()");
  }

  @Override
  public T visitForexOptionDigital(final ForexOptionDigital derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexOptionDigital()");
  }

  //-----     Commodity     -----

  @Override
  public T visitMetalForward(final MetalForward future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitMetalForward()");
  }

  @Override
  public T visitMetalForward(final MetalForward future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitMetalForward()");
  }

  @Override
  public T visitMetalFuture(final MetalFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitMetalFuture()");
  }

  @Override
  public T visitMetalFuture(final MetalFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitMetalFuture()");
  }

  @Override
  public T visitMetalFutureOption(final MetalFutureOption future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitMetalFutureOption()");
  }

  @Override
  public T visitMetalFutureOption(final MetalFutureOption future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitMetalFutureOption()");
  }

  @Override
  public T visitAgricultureForward(final AgricultureForward future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAgricultureForward()");
  }

  @Override
  public T visitAgricultureForward(final AgricultureForward future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAgricultureForward()");
  }

  @Override
  public T visitAgricultureFuture(final AgricultureFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAgricultureFuture()");
  }

  @Override
  public T visitAgricultureFuture(final AgricultureFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAgricultureFuture()");
  }

  @Override
  public T visitAgricultureFutureOption(final AgricultureFutureOption future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAgricultureFutureOption()");
  }

  @Override
  public T visitAgricultureFutureOption(final AgricultureFutureOption future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAgricultureFutureOption()");
  }

  @Override
  public T visitEnergyForward(final EnergyForward future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEnergyForward()");
  }

  @Override
  public T visitEnergyForward(final EnergyForward future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEnergyForward()");
  }

  @Override
  public T visitEnergyFuture(final EnergyFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEnergyFuture()");
  }

  @Override
  public T visitEnergyFuture(final EnergyFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEnergyFuture()");
  }

  @Override
  public T visitEnergyFutureOption(final EnergyFutureOption future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEnergyFutureOption()");
  }

  @Override
  public T visitEnergyFutureOption(final EnergyFutureOption future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEnergyFutureOption()");
  }

  //  -----     Deprecated     -----

  @Override
  public T visitForexForward(final ForexForward fx, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexForward()");
  }

  @Override
  public T visitForexForward(final ForexForward fx) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForexForward()");
  }

  @Override
  public T visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityCouponIborSpread()");
  }

  @Override
  public T visitAnnuityCouponIborSpread(final AnnuityCouponIborSpread annuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitAnnuityCouponIborSpread()");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFloatingRateNote");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFloatingRateNote()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitCrossCurrencySwap(final CrossCurrencySwap ccs) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCrossCurrencySwap()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor annuityCouponIbor, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitCrossCurrencySwap(final CrossCurrencySwap ccs, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCrossCurrencySwap()");
  }

}
