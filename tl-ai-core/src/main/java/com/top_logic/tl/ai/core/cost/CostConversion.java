/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicLong;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;

/**
 * Converts a cost from US dollars, the currency of every price this product reads, into the
 * engine's system currency, at the exchange rate the administrators keep for US dollars.
 *
 * <p>
 * A run records what it cost in US dollars ({@link Money#USD}), as the models.dev catalog and the
 * prices of a model override state them. The cost is converted only where it is shown or compared
 * with a budget: into the system currency of {@link CurrencySystem} (euro unless the application
 * configures another), at the rate of the engine's currency "USD" that is valid on the day the run
 * started. A run therefore keeps its amount when a later dated rate is entered, and the money budget
 * is stated in the system currency.
 * </p>
 *
 * <p>
 * <b>Which rate.</b> A dated exchange rate of the currency "USD" that is valid on the day wins, as
 * the engine's {@code Currency.getConversionFactor(Date)} decides it. Without one the currency's own
 * conversion factor counts - the "Umrechnungsfaktor" an administrator edits under Administration
 * &gt; Basisadministration &gt; Currencies, which is the one rate the engine's screens can enter -
 * unless it is exactly 1: {@link CurrencySystem} creates every currency with the factor 1, so that
 * value says nothing, and taking it would make a dollar worth a euro.
 * </p>
 *
 * <p>
 * <b>Without a rate nothing breaks.</b> When the engine has no currency "USD", or no rate of it
 * for the day, the amount is shown in US dollars and the money budget is not enforced;
 * {@link #warnNoRate(String)} says so in the log, at most once per {@link #WARN_INTERVAL}. A system
 * currency that is US dollars needs no rate.
 * </p>
 *
 * <p>
 * <b>The direction of a rate.</b> The engine's {@code Amount} converts an amount into another
 * currency as amount * factor(own currency) / factor(target currency), and the factor of the system
 * currency is 1. A rate of the currency "USD" therefore says how many units of the system
 * currency one US dollar is worth: with the system currency EUR, a rate of 0.92 makes one dollar
 * 0.92 euro.
 * </p>
 */
public final class CostConversion {

	/**
	 * The conversion factor {@link CurrencySystem} gives every currency it creates, which therefore
	 * says nothing about the currency's value.
	 */
	private static final double CREATED_FACTOR = 1.0;

	/** How often the log is told that a cost could not be converted. */
	public static final Duration WARN_INTERVAL = Duration.ofHours(1);

	/** When the log was last told; {@code 0} for never. */
	private static final AtomicLong LAST_WARNING = new AtomicLong();

	/**
	 * The rates a test put in place of the engine's, or {@code null}.
	 *
	 * @see #override(String, Rates)
	 */
	private static volatile Substitute _override;

	private CostConversion() {
		// Static utility.
	}

	/**
	 * The rate of US dollars per day: where a test takes its rates from instead of the engine, and
	 * what {@link CostConversion#usdRates()} hands out.
	 *
	 * @see CostConversion#override(String, Rates)
	 */
	@FunctionalInterface
	public interface Rates {

		/**
		 * How many units of the system currency one US dollar is worth on the given day, or empty
		 * when there is no rate for it.
		 */
		OptionalDouble usdRate(Date day);
	}

	private record Substitute(String systemCurrency, Rates rates) {
		// Pure data.
	}

	/**
	 * The ISO 4217 code of the currency costs are shown in when they can be converted: the engine's
	 * system currency, or {@link Money#USD} while the engine keeps none.
	 */
	public static String systemCurrency() {
		Substitute overridden = _override;
		if (overridden != null) {
			return overridden.systemCurrency();
		}
		Currency system = system();
		return system == null ? Money.USD : system.getName();
	}

	/**
	 * How many units of the {@link #systemCurrency() system currency} one US dollar is worth on the
	 * given day.
	 *
	 * @param day
	 *        The day the cost was made; {@code null} for now.
	 * @return The rate, {@code 1} when the system currency is US dollars; empty when the engine has
	 *         no currency "USD", neither a dated rate of it valid on the day nor a conversion factor
	 *         other than 1, no system currency, or cannot read its rates - this never throws.
	 */
	public static OptionalDouble usdRate(Date day) {
		return usdRates().usdRate(day);
	}

	/**
	 * The {@link #usdRate(Date) rate} of any day, with the currencies looked up once.
	 *
	 * <p>
	 * For a caller that converts many costs in one go, such as the sum a budget is checked against:
	 * finding the system currency and the currency "USD" is the part of {@link #usdRate(Date)} that
	 * costs, and it does not depend on the day. The days' rates are still read from the currency's
	 * dated rates per call, so every day gets exactly the rate {@link #usdRate(Date)} would give it.
	 * </p>
	 *
	 * <p>
	 * Meant for one such go and then dropped: a rate an administrator enters afterwards is seen only
	 * by a lookup made afterwards.
	 * </p>
	 *
	 * @return The rates; a day of {@code null} means now. Never throws, like {@link #usdRate(Date)}.
	 */
	public static Rates usdRates() {
		Rates resolved;
		try {
			resolved = resolveRates();
		} catch (RuntimeException ex) {
			noRate(ex);
			return day -> OptionalDouble.empty();
		}
		return day -> {
			Date at = day == null ? new Date() : day;
			try {
				return resolved.usdRate(at);
			} catch (RuntimeException ex) {
				noRate(ex);
				return OptionalDouble.empty();
			}
		};
	}

	private static void noRate(RuntimeException ex) {
		// Nothing that needs a cost may fail over a rate that cannot be read - not a run, not the
		// monitor, not the start of the application. On a new database the engine's own cache of
		// the rates refuses to read while the initial data is still being written.
		Logger.info("The exchange rate of USD cannot be read, costs are taken as having none: " + ex.getMessage(),
			CostConversion.class);
	}

	private static Rates resolveRates() {
		Substitute overridden = _override;
		if (overridden != null) {
			return Money.USD.equals(overridden.systemCurrency()) ? at -> OptionalDouble.of(1) : overridden.rates();
		}
		Currency system = system();
		if (system == null) {
			return at -> OptionalDouble.empty();
		}
		if (Money.USD.equals(system.getName())) {
			return at -> OptionalDouble.of(1);
		}
		Currency usd = Currency.getCurrencyInstance(Money.USD);
		if (usd == null) {
			return at -> OptionalDouble.empty();
		}
		return at -> readRate(system, usd, at);
	}

	private static OptionalDouble readRate(Currency system, Currency usd, Date at) {
		double factor;
		SortedMap<Object, KnowledgeAssociation> rates = usd.getExchangeRateKAs();
		if (rates != null && (!rates.headMap(at).isEmpty() || rates.containsKey(at))) {
			factor = usd.getConversionFactor(at);
		} else {
			factor = staticFactor(usd);
			if (factor == CREATED_FACTOR) {
				return OptionalDouble.empty();
			}
		}
		double rate = factor / system.getConversionFactor();
		if (!(rate > 0) || Double.isInfinite(rate)) {
			return OptionalDouble.empty();
		}
		return OptionalDouble.of(rate);
	}

	/**
	 * The conversion factor a currency carries apart from its dated rates, read from its row: asking
	 * the currency for it logs a warning on every call.
	 */
	private static double staticFactor(Currency currency) {
		Object value = currency.tHandle().getAttributeValue(UnitWrapper.FACTOR);
		return value instanceof Number number ? number.doubleValue() : CREATED_FACTOR;
	}

	/**
	 * The given cost in the currency it is shown in.
	 *
	 * @param cost
	 *        A cost; an amount in US dollars is converted, any other is returned as it is.
	 * @param day
	 *        The day the cost was made, whose rate applies; {@code null} for now.
	 * @return The amount in the {@link #systemCurrency() system currency}, or in US dollars when
	 *         there is no rate for the day.
	 */
	public static Money shown(Money cost, Date day) {
		if (!Money.USD.equals(cost.currency())) {
			return cost;
		}
		OptionalDouble rate = usdRate(day);
		if (rate.isEmpty()) {
			return cost;
		}
		return new Money(cost.amount() * rate.getAsDouble(), systemCurrency());
	}

	/**
	 * The given amount as a person of the given locale reads it.
	 *
	 * <p>
	 * An amount in the engine's system currency is written with that currency's own number format
	 * and its code, as the application writes amounts elsewhere. Any other amount - US dollars,
	 * because there was no rate - is written the way {@link Money#format(Locale)} writes it.
	 * </p>
	 */
	public static String format(Money amount, Locale locale) {
		if (_override == null) {
			try {
				Currency system = system();
				if (system != null && system.getName().equals(amount.currency())) {
					String spec = system.getFormatSpec();
					if (!StringServices.isEmpty(spec)) {
						NumberFormat format = system.getFormat(locale);
						return format.format(amount.amount()) + " " + amount.currency();
					}
				}
			} catch (RuntimeException ex) {
				// A format spec or a currency the engine cannot read; the plain form below is still right.
			}
		}
		return amount.format(locale);
	}

	/**
	 * Tells the log that a cost could not be converted into the system currency, at most once per
	 * {@link #WARN_INTERVAL}.
	 *
	 * @param consequence
	 *        What does not happen because of it, for example that a budget is not checked.
	 * @return Whether the log was told now; {@code false} when it was told within the interval.
	 */
	public static boolean warnNoRate(String consequence) {
		long now = System.currentTimeMillis();
		long last = LAST_WARNING.get();
		if (last != 0 && now - last < WARN_INTERVAL.toMillis()) {
			return false;
		}
		if (!LAST_WARNING.compareAndSet(last, now)) {
			return false;
		}
		Logger.warn("No exchange rate from USD to " + systemCurrency() + " is valid today: costs are shown in USD"
			+ " and " + consequence + ". Set the conversion factor of the currency USD under Administration >"
			+ " Basisadministration > Currencies (how many units of the system currency one US dollar is worth).",
			CostConversion.class);
		return true;
	}

	/**
	 * Converts with the given rates instead of the engine's until the handle is closed.
	 *
	 * <p>
	 * <b>A test seam</b>, the counterpart of {@link RunCosts#overridePrices}: a test of the budget
	 * would otherwise have to boot the currency system and enter rates. Nothing in production calls
	 * it.
	 * </p>
	 *
	 * @param systemCurrency
	 *        The ISO 4217 code that stands in for the system currency.
	 * @param rates
	 *        The rate of a day; ignored when the system currency is US dollars.
	 */
	public static RunLimitSettings.Overridden override(String systemCurrency, Rates rates) {
		Substitute before = _override;
		_override = new Substitute(systemCurrency, rates);
		LAST_WARNING.set(0);
		return () -> {
			_override = before;
			LAST_WARNING.set(0);
		};
	}

	/** The engine's system currency, or {@code null} while the currency system is not running. */
	private static Currency system() {
		if (!CurrencySystem.Module.INSTANCE.isActive() || !PersistencyLayer.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			return Currency.getSystemCurrency();
		} catch (RuntimeException ex) {
			Logger.warn("The system currency cannot be read: " + ex.getMessage(), ex, CostConversion.class);
			return null;
		}
	}
}
