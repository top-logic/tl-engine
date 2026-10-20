/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * An amount of money in one currency.
 *
 * <p>
 * A {@code double}, not a {@code BigDecimal}: what this product computes is an estimate - a token
 * count the provider reported, times a price a public list states - and no figure here is ever
 * booked. Two decimals are what a person reads, and the formatting rounds to them.
 * </p>
 *
 * @param amount
 *        The amount; never negative.
 * @param currency
 *        The ISO 4217 code of the currency, for example {@code "EUR"}; never {@code null}.
 */
public record Money(double amount, String currency) {

	/** The currency every price list this product reads is written in. */
	public static final String USD = "USD";

	/** */
	public Money {
		Objects.requireNonNull(currency, "Currency must not be null.");
	}

	/** Nothing, in the given currency. */
	public static Money zero(String currency) {
		return new Money(0, currency);
	}

	/**
	 * This amount plus the given one.
	 *
	 * <p>
	 * The sum is in this amount's currency. Two amounts in different currencies are not added: a
	 * sum of a dollar figure and a euro figure would be a third figure in neither. Costs are added
	 * in US dollars and converted afterwards (see {@link CostConversion}).
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         If the other amount is in another currency and is not zero.
	 */
	public Money plus(Money other) {
		if (other.amount == 0) {
			return this;
		}
		if (amount == 0) {
			return other;
		}
		if (!currency.equals(other.currency)) {
			throw new IllegalArgumentException(
				"Cannot add an amount in " + other.currency + " to an amount in " + currency + ".");
		}
		return new Money(amount + other.amount, currency);
	}

	/**
	 * This amount as a person of the given locale writes it: {@code 12,40 €} for German,
	 * {@code €12.40} for English.
	 *
	 * <p>
	 * Always two decimals. A currency code that Java does not know - which the configuration does
	 * not let through - is written after the number as it is.
	 * </p>
	 */
	public String format(Locale locale) {
		NumberFormat format = NumberFormat.getCurrencyInstance(locale);
		try {
			format.setCurrency(Currency.getInstance(currency));
		} catch (IllegalArgumentException ex) {
			NumberFormat plain = NumberFormat.getNumberInstance(locale);
			plain.setMinimumFractionDigits(2);
			plain.setMaximumFractionDigits(2);
			return plain.format(amount) + " " + currency;
		}
		format.setMinimumFractionDigits(2);
		format.setMaximumFractionDigits(2);
		return format.format(amount);
	}
}
