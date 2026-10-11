/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.util.Locale;
import java.util.OptionalDouble;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;

/**
 * What a request costs, in US dollars, and how an amount is written.
 */
public class TestRunCosts extends TestCase {

	/** Reading the configuration asks the agent services whether they run, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestRunCosts.class));
	}

	private static final ModelCost ONE_DOLLAR_PER_MILLION = new ModelCost(1, 1, null, null, null, null, null, null);

	/** A request costs what the prices say, in US dollars - the currency of the prices. */
	public void testTheCostIsInUsd() {
		try (RunLimitSettings.Overridden prices = RunCosts.overridePrices(model -> ONE_DOLLAR_PER_MILLION)) {
			Money cost = RunCosts.costOf("any", new TokenUsage(2_000_000, 1_000_000, 0, 0, 0));
			assertEquals("USD", cost.currency());
			assertEquals(3.0, cost.amount(), 1e-12);
		}
	}

	/**
	 * The cost stays in US dollars whatever the system currency is: a run records it as the prices
	 * state it, and it is converted only where it is shown.
	 */
	public void testTheCostIsNotConvertedWhenItIsMade() {
		try (RunLimitSettings.Overridden prices = RunCosts.overridePrices(model -> ONE_DOLLAR_PER_MILLION);
				RunLimitSettings.Overridden rates = CostConversion.override("EUR", day -> OptionalDouble.of(0.92))) {
			assertEquals(new Money(1, "USD"), RunCosts.costOf("any", new TokenUsage(1_000_000, 0, 0, 0, 0)));
		}
	}

	/** A model without a price costs nothing. */
	public void testAnUnpricedModelCostsNothing() {
		try (RunLimitSettings.Overridden prices = RunCosts.overridePrices(model -> null)) {
			assertEquals(Money.zero("USD"), RunCosts.costOf("local-model", new TokenUsage(1_000_000, 0, 0, 0, 0)));
		}
	}

	/** An amount is written the way the reader's language writes money, always with two decimals. */
	public void testAnAmountIsWrittenInTheReadersLanguage() {
		assertEquals("12,40 €", new Money(12.4, "EUR").format(Locale.GERMANY));
		assertEquals("€12.40", new Money(12.4, "EUR").format(Locale.UK));
		assertEquals("15,00 €", new Money(15, "EUR").format(Locale.GERMANY));
		assertEquals("$0.01", new Money(0.006, "USD").format(Locale.US));
	}

	/** Amounts in one currency add up; amounts in two do not. */
	public void testOnlyAmountsInOneCurrencyAddUp() {
		assertEquals(new Money(3, "EUR"), new Money(1, "EUR").plus(new Money(2, "EUR")));
		assertEquals(new Money(1, "EUR"), new Money(1, "EUR").plus(Money.zero("USD")));
		try {
			new Money(1, "EUR").plus(new Money(1, "USD"));
			fail("Euros and dollars do not add up.");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}
}
