/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.tl.ai.core.limits.BudgetConfig;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;

/**
 * What a request costs in the installation's currency, and how an amount is written.
 */
public class TestRunCosts extends TestCase {

	/** Reading the configuration asks the agent services whether they run, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestRunCosts.class));
	}

	private static final ModelCost ONE_DOLLAR_PER_MILLION = new ModelCost(1, 1, null, null, null, null, null, null);

	/** Without a currency configured, costs are in US dollars, the currency of the prices. */
	public void testTheDefaultCurrencyIsUsd() {
		try (RunLimitSettings.Overridden prices = RunCosts.overridePrices(model -> ONE_DOLLAR_PER_MILLION);
				RunLimitSettings.Overridden config = RunLimitSettings.override(RunLimitSettings.newConfig())) {
			Money cost = RunCosts.costOf("any", new TokenUsage(2_000_000, 0, 0, 0, 0));
			assertEquals("USD", cost.currency());
			assertEquals(2.0, cost.amount(), 1e-12);
		}
	}

	/** A configured currency converts the dollar cost at the configured rate. */
	public void testTheCostIsConvertedAtTheConfiguredRate() {
		try (RunLimitSettings.Overridden prices = RunCosts.overridePrices(model -> ONE_DOLLAR_PER_MILLION);
				RunLimitSettings.Overridden config = RunLimitSettings.override(currency("eur", 0.92))) {
			Money cost = RunCosts.costOf("any", new TokenUsage(1_000_000, 1_000_000, 0, 0, 0));
			assertEquals("The code is written as ISO 4217 writes it.", "EUR", cost.currency());
			assertEquals(1.84, cost.amount(), 1e-12);
		}
	}

	/** A model without a price costs nothing, in the installation's currency. */
	public void testAnUnpricedModelCostsNothing() {
		try (RunLimitSettings.Overridden prices = RunCosts.overridePrices(model -> null);
				RunLimitSettings.Overridden config = RunLimitSettings.override(currency("EUR", 0.92))) {
			assertEquals(Money.zero("EUR"), RunCosts.costOf("local-model", new TokenUsage(1_000_000, 0, 0, 0, 0)));
		}
	}

	/** A currency code nobody knows, or a rate that is not positive, cannot be used. */
	public void testAnUnusableCurrencyIsRefused() {
		assertRefused(currency("EURO", 1));
		assertRefused(currency("EUR", 0));
		assertRefused(currency("EUR", -1));
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

	private static void assertRefused(RunLimitsConfig config) {
		try {
			RunLimitSettings.currencyOf(config);
			fail("Must be refused: " + config.getCurrency() + " at " + config.getExchangeRate());
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	private static RunLimitsConfig currency(String code, double rate) {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(BudgetConfig.CURRENCY), code);
		config.update(config.descriptor().getProperty(BudgetConfig.EXCHANGE_RATE), Double.valueOf(rate));
		return config;
	}
}
