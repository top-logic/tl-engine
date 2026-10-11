/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.util.Date;
import java.util.Locale;
import java.util.OptionalDouble;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.currency.Currency;

/**
 * Converting a cost from US dollars into the engine's system currency, against the engine's own
 * currencies and their dated exchange rates.
 *
 * <p>
 * A real knowledge base and a running {@link CurrencySystem}, because what is pinned here is how
 * the engine's rates are meant: the direction of a rate, which rate a day takes, and that a currency
 * without a dated rate has none. Every rate is entered in a transaction that is rolled back, so the
 * tests do not see each other's rates.
 * </p>
 */
public class TestCostConversion extends BasicTestCase {

	private static final long DAY = 24 * 3600_000L;

	/**
	 * A rate of the currency USD says how many units of the system currency one dollar is worth: with
	 * the system currency EUR, 0.92 makes ten dollars 9.20 euros.
	 */
	public void testARateSaysWhatADollarIsWorthInTheSystemCurrency() {
		assertEquals("EUR", CostConversion.systemCurrency());
		Currency usd = usd();
		try (Transaction tx = kb().beginTransaction()) {
			usd.saveExchangeRatio(daysAgo(2), Double.valueOf(0.92));

			assertEquals(0.92, CostConversion.usdRate(null).getAsDouble(), 1e-9);
			Money shown = CostConversion.shown(new Money(10, Money.USD), null);
			assertEquals("EUR", shown.currency());
			assertEquals(9.20, shown.amount(), 1e-9);
			tx.rollback();
		}
	}

	/** Each day takes the newest rate entered for a day up to it, so an old run keeps its amount. */
	public void testEachDayTakesTheRateValidOnIt() {
		Currency usd = usd();
		try (Transaction tx = kb().beginTransaction()) {
			usd.saveExchangeRatio(daysAgo(10), Double.valueOf(0.5));
			usd.saveExchangeRatio(daysAgo(2), Double.valueOf(0.9));

			assertEquals(0.5, CostConversion.usdRate(daysAgo(5)).getAsDouble(), 1e-9);
			assertEquals(0.9, CostConversion.usdRate(daysAgo(1)).getAsDouble(), 1e-9);
			assertEquals(5.0, CostConversion.shown(new Money(10, Money.USD), daysAgo(5)).amount(), 1e-9);
			assertEquals(9.0, CostConversion.shown(new Money(10, Money.USD), daysAgo(1)).amount(), 1e-9);
			tx.rollback();
		}
	}

	/**
	 * A day before the first dated rate has none while the currency's factor is the 1 it was created
	 * with, and the cost stays in US dollars.
	 */
	public void testADayBeforeTheFirstRateHasNone() {
		Currency usd = usd();
		try (Transaction tx = kb().beginTransaction()) {
			usd.saveExchangeRatio(daysAgo(2), Double.valueOf(0.9));

			assertTrue(CostConversion.usdRate(daysAgo(3)).isEmpty());
			assertEquals(new Money(10, Money.USD), CostConversion.shown(new Money(10, Money.USD), daysAgo(3)));
			tx.rollback();
		}
		assertTrue("Without any rate, no day has one.", CostConversion.usdRate(null).isEmpty());
	}

	/**
	 * Without a dated rate the currency's conversion factor counts - the one rate the engine's screens
	 * can enter - and a dated rate wins over it for the days it covers.
	 */
	public void testTheConversionFactorCountsWithoutADatedRate() {
		Currency usd = usd();
		try (Transaction tx = kb().beginTransaction()) {
			usd.setConversionFactor(Double.valueOf(0.8));

			assertEquals(0.8, CostConversion.usdRate(null).getAsDouble(), 1e-9);
			assertEquals(new Money(8, "EUR"), CostConversion.shown(new Money(10, Money.USD), null));

			usd.saveExchangeRatio(daysAgo(2), Double.valueOf(0.9));
			assertEquals("The dated rate wins for its days.", 0.9, CostConversion.usdRate(null).getAsDouble(), 1e-9);
			tx.rollback();
		}
	}

	/** A factor of exactly 1 is the one every currency is created with: it is no rate. */
	public void testTheFactorACurrencyIsCreatedWithIsNoRate() {
		Currency usd = usd();
		try (Transaction tx = kb().beginTransaction()) {
			usd.setConversionFactor(Double.valueOf(1.0));

			assertTrue(CostConversion.usdRate(null).isEmpty());
			tx.rollback();
		}
	}

	/** An amount in the system currency is written with that currency's own format and its code. */
	public void testTheSystemCurrencyWritesItsOwnFormat() {
		assertEquals("1.234,50 EUR", CostConversion.format(new Money(1234.5, "EUR"), Locale.GERMANY));
		assertEquals("Not the system currency: written as Java writes money.",
			new Money(12.4, Money.USD).format(Locale.US), CostConversion.format(new Money(12.4, Money.USD), Locale.US));
	}

	/** Without a rate the log is told once, and not again within the interval. */
	public void testTheWarningIsRateLimited() {
		try (var rates = CostConversion.override("EUR", day -> OptionalDouble.empty())) {
			assertTrue(CostConversion.warnNoRate("the money budget is not checked"));
			assertFalse(CostConversion.warnNoRate("the money budget is not checked"));
		}
	}

	/**
	 * A rate that cannot be read is no rate: nothing that needs a cost fails over it. On a new
	 * database the engine refused to read its rates while the initial data was written, and the
	 * start of the policy service took the application down with it.
	 */
	public void testARateThatCannotBeReadIsNone() {
		try (var rates = CostConversion.override("EUR", day -> {
			throw new IllegalStateException("The rates cannot be read now.");
		})) {
			assertTrue(CostConversion.usdRate(null).isEmpty());
			assertEquals(new Money(10, Money.USD), CostConversion.shown(new Money(10, Money.USD), null));
		}
	}

	/** A substituted rate wins over the engine's, for the tests of the budget. */
	public void testASubstituteWinsOverTheEngine() {
		try (var rates = CostConversion.override("CHF", day -> OptionalDouble.of(0.8))) {
			assertEquals("CHF", CostConversion.systemCurrency());
			assertEquals(new Money(8, "CHF"), CostConversion.shown(new Money(10, Money.USD), null));
		}
		assertEquals("EUR", CostConversion.systemCurrency());
	}

	private static Currency usd() {
		Currency usd = Currency.getCurrencyInstance(Money.USD);
		if (usd != null) {
			return usd;
		}
		try (Transaction tx = kb().beginTransaction()) {
			Currency created = Currency.createCurrency(Money.USD, 2000);
			created.setBaseUnit(Currency.getSystemCurrency());
			tx.commit();
			return created;
		}
	}

	private static Date daysAgo(int days) {
		return new Date(System.currentTimeMillis() - days * DAY);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/** A knowledge base and the currency system. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestCostConversion.class), CurrencySystem.Module.INSTANCE));
	}
}
