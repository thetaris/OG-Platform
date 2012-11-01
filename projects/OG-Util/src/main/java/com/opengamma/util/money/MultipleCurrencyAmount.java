/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.opengamma.util.ArgumentChecker;

/**
 * A map of currency amounts keyed by currency.
 * <p>
 * This is a container holding multiple {@link CurrencyAmount} instances.
 * The amounts do not necessarily the same worth or value in each currency.
 * <p>
 * This class behaves as a set - if an amount is added with the same currency as one of the
 * elements, the amounts are added. For example, adding EUR 100 to the container
 * (EUR 200, CAD 100) would give (EUR 300, CAD 100).
 * <p>
 * This class is immutable and thread-safe.
 */
public final class MultipleCurrencyAmount implements Iterable<CurrencyAmount>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The backing list of amounts.
   */
  private final TreeMap<Currency, CurrencyAmount> _amounts;

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a currency and amount.
   * 
   * @param currency  the currency, not null
   * @param amount  the amount
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Currency currency, final double amount) {
    TreeMap<Currency, CurrencyAmount> map = new TreeMap<Currency, CurrencyAmount>();
    map.put(currency, CurrencyAmount.of(currency, amount));
    return new MultipleCurrencyAmount(map);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a paired array of currencies and amounts.
   * 
   * @param currencies  the currencies, not null
   * @param amounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Currency[] currencies, final double[] amounts) {
    ArgumentChecker.noNulls(currencies, "currencies");
    ArgumentChecker.notNull(amounts, "amounts");
    final int length = currencies.length;
    ArgumentChecker.isTrue(length == amounts.length, "Currency array and amount array must be the same length");
    List<CurrencyAmount> list = new ArrayList<CurrencyAmount>(length);
    for (int i = 0; i < length; i++) {
      list.add(CurrencyAmount.of(currencies[i], amounts[i]));
    }
    return of(list);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a paired list of currencies and amounts.
   * 
   * @param currencies  the currencies, not null
   * @param amounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final List<Currency> currencies, final List<Double> amounts) {
    ArgumentChecker.noNulls(currencies, "currencies");
    ArgumentChecker.noNulls(amounts, "amounts");
    final int length = currencies.size();
    ArgumentChecker.isTrue(length == amounts.size(), "Currency array and amount array must be the same length");
    List<CurrencyAmount> list = new ArrayList<CurrencyAmount>(length);
    for (int i = 0; i < length; i++) {
      list.add(CurrencyAmount.of(currencies.get(i), amounts.get(i)));
    }
    return of(list);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a map of currency to amount.
   * 
   * @param amountMap  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Map<Currency, Double> amountMap) {
    ArgumentChecker.notNull(amountMap, "amountMap");
    TreeMap<Currency, CurrencyAmount> map = new TreeMap<Currency, CurrencyAmount>();
    for (Entry<Currency, Double> entry : amountMap.entrySet()) {
      ArgumentChecker.notNull(entry.getValue(), "amount");
      map.put(entry.getKey(), CurrencyAmount.of(entry.getKey(), entry.getValue()));
    }
    return new MultipleCurrencyAmount(map);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a list of {@code CurrencyAmount}.
   * 
   * @param currencyAmounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final CurrencyAmount... currencyAmounts) {
    ArgumentChecker.notNull(currencyAmounts, "currencyAmounts");
    return of(Arrays.asList(currencyAmounts));
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a list of {@code CurrencyAmount}.
   * 
   * @param currencyAmounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Iterable<CurrencyAmount> currencyAmounts) {
    ArgumentChecker.notNull(currencyAmounts, "currencyAmounts");
    TreeMap<Currency, CurrencyAmount> map = new TreeMap<Currency, CurrencyAmount>();
    for (CurrencyAmount currencyAmount : currencyAmounts) {
      ArgumentChecker.notNull(currencyAmount, "currencyAmount");
      CurrencyAmount existing = map.get(currencyAmount.getCurrency());
      if (existing != null) {
        map.put(currencyAmount.getCurrency(), existing.plus(currencyAmount));
      } else {
        map.put(currencyAmount.getCurrency(), currencyAmount);
      }
    }
    return new MultipleCurrencyAmount(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a {@code MultipleCurrencyAmount} from a list of {@code CurrencyAmount}.
   * 
   * @param currencyAmounts  the amounts, not null
   */
  private MultipleCurrencyAmount(final TreeMap<Currency, CurrencyAmount> currencyAmounts) {
    _amounts = currencyAmounts;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of stored amounts.
   * 
   * @return the number of amounts
   */
  public int size() {
    return _amounts.size();
  }

  /**
   * Iterates though the currency-amounts.
   * 
   * @return the iterator, not null
   */
  @Override
  public Iterator<CurrencyAmount> iterator() {
    return _amounts.values().iterator();
  }

  /**
   * Gets the currency amounts as an array.
   * 
   * @return the independent, modifiable currency amount array, not null
   */
  public CurrencyAmount[] getCurrencyAmounts() {
    return _amounts.values().toArray(new CurrencyAmount[_amounts.size()]);
  }

  /**
   * Gets the amount for the specified currency.
   * 
   * @param currency  the currency to find an amount for, not null
   * @return the amount
   * @throws IllegalArgumentException if the currency is not present
   */
  public double getAmount(final Currency currency) {
    CurrencyAmount currencyAmount = getCurrencyAmount(currency);
    if (currencyAmount == null) {
      throw new IllegalArgumentException("Do not have an amount with currency " + currency);
    }
    return currencyAmount.getAmount();
  }

  /**
   * Gets the {@code CurrencyAmount} for the specified currency.
   * 
   * @param currency  the currency to find an amount for, not null
   * @return the amount, null if no amount for the currency
   */
  public CurrencyAmount getCurrencyAmount(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return _amounts.get(currency);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * If the currency is already present, the amount is added to the existing amount.
   * If the currency is not yet present, the currency-amount is added to the map.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currencyAmountToAdd  the amount to add, in the same currency, not null
   * @return an amount based on this with the specified amount added, not null
   */
  public MultipleCurrencyAmount plus(final CurrencyAmount currencyAmountToAdd) {
    ArgumentChecker.notNull(currencyAmountToAdd, "currencyAmountToAdd");
    return plus(currencyAmountToAdd.getCurrency(), currencyAmountToAdd.getAmount());
  }

  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * If the currency is already present, the amount is added to the existing amount.
   * If the currency is not yet present, the currency-amount is added to the map.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currency  the currency to add to, not null
   * @param amountToAdd  the amount to add
   * @return an amount based on this with the specified amount added, not null
   */
  public MultipleCurrencyAmount plus(final Currency currency, final double amountToAdd) {
    ArgumentChecker.notNull(currency, "currency");
    TreeMap<Currency, CurrencyAmount> copy = new TreeMap<Currency, CurrencyAmount>(_amounts);
    CurrencyAmount existing = getCurrencyAmount(currency);
    if (existing != null) {
      copy.put(currency, existing.plus(amountToAdd));
    } else {
      copy.put(currency, CurrencyAmount.of(currency, amountToAdd));
    }
    return new MultipleCurrencyAmount(copy);
  }

  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * If the currency is already present, the amount is added to the existing amount.
   * If the currency is not yet present, the currency-amount is added to the map.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param multipleCurrencyAmountToAdd  the currency to add to, not null
   * @return an amount based on this with the specified amount added, not null
   */
  public MultipleCurrencyAmount plus(final MultipleCurrencyAmount multipleCurrencyAmountToAdd) {
    ArgumentChecker.notNull(multipleCurrencyAmountToAdd, "multipleCurrencyAmountToAdd");
    MultipleCurrencyAmount result = this;
    for (CurrencyAmount currencyAmount : multipleCurrencyAmountToAdd) {
      result = result.plus(currencyAmount);
    }
    return result;
  }


  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with all the amounts multiplied by the factor.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param factor The multiplicative factor.
   * @return An amount based on this with all the amounts multiplied by the factor. Not null
   */
  public MultipleCurrencyAmount multipliedBy(final double factor) {
    TreeMap<Currency, Double> map = new TreeMap<Currency, Double>();
    for (CurrencyAmount currencyAmount : this) {
      map.put(currencyAmount.getCurrency(), currencyAmount.getAmount() * factor);
    }
    return MultipleCurrencyAmount.of(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified currency.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * Any previous amount for the specified currency is replaced.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currency  the currency to replace, not null
   * @param amount  the new amount
   * @return an amount based on this with the specified currency replaced, not null
   */
  public MultipleCurrencyAmount with(final Currency currency, final double amount) {
    ArgumentChecker.notNull(currency, "currency");
    TreeMap<Currency, CurrencyAmount> copy = new TreeMap<Currency, CurrencyAmount>(_amounts);
    copy.put(currency, CurrencyAmount.of(currency, amount));
    return new MultipleCurrencyAmount(copy);
  }

  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} without the specified currency.
   * <p>
   * This removes the specified currency from this monetary amount, returning a new object.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currency  the currency to replace, not null
   * @return an amount based on this with the specified currency removed, not null
   */
  public MultipleCurrencyAmount without(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    TreeMap<Currency, CurrencyAmount> copy = new TreeMap<Currency, CurrencyAmount>(_amounts);
    if (copy.remove(currency) == null) {
      return this;
    }
    return new MultipleCurrencyAmount(copy);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this amount equals another amount.
   * <p>
   * The comparison checks the currency-amount map.
   * 
   * @param obj  the other amount, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && getClass() == obj.getClass()) {
      MultipleCurrencyAmount other = (MultipleCurrencyAmount) obj;
      return _amounts.equals(other._amounts);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the amount.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _amounts.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount as a string.
   * <p>
   * The format includes each currency-amount.
   * 
   * @return the currency amount, not null
   */
  @Override
  public String toString() {
    return _amounts.values().toString();
  }

}