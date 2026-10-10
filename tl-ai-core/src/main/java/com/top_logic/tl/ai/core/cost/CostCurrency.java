/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.util.Currency;
import java.util.Objects;

/**
 * The one currency this installation states its costs in, and how many units of it one US dollar
 * is worth.
 *
 * <p>
 * Every price this product reads is in US dollars - the models.dev catalog and the prices an
 * administrator puts on a model override alike. A cost is converted into this currency once, when
 * a run records it, at the rate configured at that moment. The rate is a fixed number of the
 * configuration; nothing fetches a live one, because a budget must not move while nobody changes
 * it.
 * </p>
 *
 * @param code
 *        The ISO 4217 code, for example {@code "EUR"}.
 * @param usdRate
 *        How many units of {@link #code()} one US dollar is worth; {@code 1} for US dollars.
 */
public record CostCurrency(String code, double usdRate) {

	/** US dollars, the currency of the price lists, at the rate of one. */
	public static final CostCurrency USD = new CostCurrency(Money.USD, 1);

	/**
	 * Creates a {@link CostCurrency}.
	 *
	 * @throws IllegalArgumentException
	 *         If the code is not an ISO 4217 currency code, or the rate is not a positive number.
	 */
	public CostCurrency {
		Objects.requireNonNull(code, "Currency code must not be null.");
		Currency.getInstance(code);
		if (!(usdRate > 0) || Double.isInfinite(usdRate)) {
			throw new IllegalArgumentException(
				"The exchange rate must be a positive number, not " + usdRate + ".");
		}
	}

	/** The given amount in US dollars, converted into this currency. */
	public Money fromUsd(double usd) {
		return new Money(usd * usdRate, code);
	}

	/** Nothing, in this currency. */
	public Money zero() {
		return Money.zero(code);
	}
}
