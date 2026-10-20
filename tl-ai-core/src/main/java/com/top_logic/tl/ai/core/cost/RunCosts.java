/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.cost;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;

/**
 * What a model request cost, in US dollars.
 *
 * <p>
 * The prices come from {@link LLMProviderService}: the models.dev catalog, or a price an
 * administrator put on a model override, both in US dollars, which is also the currency a run
 * records its cost in; {@link CostConversion} converts it where it is shown or compared with a
 * budget. A model with no price - a local model nobody priced, a
 * model the catalog does not list - costs nothing. That is logged once per model, so that an
 * administrator who expected a figure finds out why there is none.
 * </p>
 *
 * @see CostCalculator
 * @see CostConversion
 */
public final class RunCosts {

	/** The models already reported as having no price, so that each is reported once. */
	private static final Set<String> UNPRICED = ConcurrentHashMap.newKeySet();

	/**
	 * The prices a test put in place of the configured ones, or {@code null}.
	 *
	 * @see #overridePrices(Function)
	 */
	private static volatile Function<String, ModelCost> _override;

	private RunCosts() {
		// Static utility.
	}

	/**
	 * What one request to the given model cost, in US dollars.
	 *
	 * @param model
	 *        The model that answered the request; {@code null} costs nothing.
	 * @param usage
	 *        What the request used; {@code null} counts as nothing.
	 */
	public static Money costOf(String model, TokenUsage usage) {
		if (usage == null || usage.totalTokens() == 0) {
			return zero();
		}
		return new Money(CostCalculator.costOf(priceOf(model), usage).total(), Money.USD);
	}

	/** Nothing, in US dollars. */
	public static Money zero() {
		return Money.zero(Money.USD);
	}

	/**
	 * The prices of the given model, or {@code null} when it has none.
	 *
	 * <p>
	 * {@code null} also while {@link LLMProviderService} is not running: a unit test or a boot step
	 * that runs a model without the service costs nothing rather than failing.
	 * </p>
	 */
	public static ModelCost priceOf(String model) {
		if (model == null) {
			return null;
		}
		Function<String, ModelCost> overridden = _override;
		if (overridden != null) {
			return overridden.apply(model);
		}
		LLMProviderService service = LLMProviderService.getInstance();
		if (service == null) {
			return null;
		}
		ModelInfo info = service.getModel(model);
		ModelCost price = info == null ? null : info.cost();
		if (price == null && UNPRICED.add(model)) {
			Logger.info("Model '" + model + "' has no price; its runs count as costing nothing. Set a price"
				+ " on its entry in the model overrides of LLMProviderService to count them.", RunCosts.class);
		}
		return price;
	}

	/**
	 * Answers {@link #priceOf(String)} from the given function until the handle is closed.
	 *
	 * <p>
	 * <b>A test seam</b>, the counterpart of {@link RunLimitSettings#override}: a test that wants a
	 * run to cost something would otherwise have to boot {@link LLMProviderService} with a catalog.
	 * Nothing in production calls it.
	 * </p>
	 *
	 * @param prices
	 *        The price of a model, {@code null} for one without a price.
	 */
	public static RunLimitSettings.Overridden overridePrices(Function<String, ModelCost> prices) {
		Function<String, ModelCost> before = _override;
		_override = prices;
		return () -> _override = before;
	}
}
