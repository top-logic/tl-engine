/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.cost.CostCalculator;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.catalog.ModelCatalogEntry.OverrideConfig;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCost;

/**
 * Where a model's prices come from: the models.dev catalog, or a model override an administrator
 * wrote.
 */
public class TestModelPrices extends TestCase {

	private static final double DELTA = 1e-12;

	/** The prices the catalog states are the ones a request is charged at. */
	public void testTheCatalogPriceIsUsed() {
		String json = """
			{
			  "openai": {
			    "models": {
			      "gpt-test": {
			        "id": "gpt-test",
			        "cost": { "input": 1.25, "output": 10, "cache_read": 0.125 },
			        "limit": { "context": 400000, "output": 128000 }
			      }
			    }
			  }
			}
			""";
		ModelInfo info = ModelsDevFetcher.parseJson(json, Map.of("openai", Set.of("gpt-test"))).get("gpt-test");

		double cost = CostCalculator.costOf(info.cost(), new TokenUsage(200_000, 100_000, 0, 800_000, 0)).total();
		assertEquals("0.2 M at 1.25 + 0.1 M at 10 + 0.8 M cached at 0.125.", 0.25 + 1.0 + 0.1, cost, DELTA);
	}

	/** A local model is priced by its override; unset cache and reasoning prices fall back. */
	public void testAnOverrideGivesALocalModelAPrice() {
		OverrideConfig override = override("qwen-local", Double.valueOf(0.20), Double.valueOf(0.60));

		ModelInfo info = ModelCatalogEntry.overrideToModelInfo(override, null);

		ModelCost price = info.cost();
		assertNotNull(price);
		assertEquals(0.20, price.input(), DELTA);
		assertEquals(0.60, price.output(), DELTA);
		assertEquals("1 M input + 1 M cached (at the input price) + 1 M output.", 0.20 + 0.20 + 0.60,
			CostCalculator.costOf(price, new TokenUsage(1_000_000, 1_000_000, 0, 1_000_000, 0)).total(), DELTA);
	}

	/** A local model nobody priced has no price, so its runs cost nothing. */
	public void testAnOverrideWithoutAPriceLeavesALocalModelUnpriced() {
		ModelInfo info = ModelCatalogEntry.overrideToModelInfo(override("qwen-local", null, null), null);

		assertNull(info.cost());
		assertEquals(0.0, CostCalculator.costOf(info.cost(), new TokenUsage(1_000_000, 0, 0, 0, 0)).total());
	}

	/** An override written only for the limits of a catalog model keeps the catalog's prices. */
	public void testAnOverrideWithoutAPriceKeepsTheCatalogPrice() {
		ModelCost catalogPrice = new ModelCost(3, 15, 0.3, 3.75, null, null, null, null);
		ModelInfo catalog = new ModelInfo("claude-test", null, new ModelInfo.ModelLimits(200_000, 64_000, 0),
			null, catalogPrice, null, false, null);

		ModelInfo info = ModelCatalogEntry.overrideToModelInfo(override("claude-test", null, null), catalog);

		assertSame(catalogPrice, info.cost());
	}

	/** An override that names a price replaces the catalog's. */
	public void testAnOverridePriceReplacesTheCatalogPrice() {
		ModelCost catalogPrice = new ModelCost(3, 15, 0.3, 3.75, null, null, null, null);
		ModelInfo catalog = new ModelInfo("claude-test", null, new ModelInfo.ModelLimits(200_000, 64_000, 0),
			null, catalogPrice, null, false, null);

		ModelInfo info = ModelCatalogEntry.overrideToModelInfo(
			override("claude-test", Double.valueOf(2), Double.valueOf(10)), catalog);

		assertEquals(2.0, info.cost().input(), DELTA);
		assertEquals(10.0, info.cost().output(), DELTA);
		assertNull("Nothing of the catalog's price list is mixed in.", info.cost().cacheRead());
	}

	/** The read-only catalog entry an administrator sees shows the prices that are charged. */
	public void testTheCatalogEntryShowsThePrices() {
		ModelInfo catalog = new ModelInfo("claude-test", null, new ModelInfo.ModelLimits(200_000, 64_000, 0),
			new ModelInfo.ModelCapabilities(true, true, true, false, false, null, null),
			new ModelCost(3, 15, 0.3, null, null, null, null, null), null, false, null);

		ModelCatalogEntry.Config entry = ModelCatalogEntry.toConfig("claude-test", catalog);

		assertEquals(Double.valueOf(3), entry.getInputPrice());
		assertEquals(Double.valueOf(15), entry.getOutputPrice());
		assertEquals(Double.valueOf(0.3), entry.getCacheReadPrice());
		assertNull(entry.getCacheWritePrice());
	}

	private static OverrideConfig override(String name, Double input, Double output) {
		OverrideConfig config = TypedConfiguration.newConfigItem(OverrideConfig.class);
		config.update(config.descriptor().getProperty(OverrideConfig.NAME_ATTRIBUTE), name);
		config.update(config.descriptor().getProperty(OverrideConfig.INPUT_PRICE), input);
		config.update(config.descriptor().getProperty(OverrideConfig.OUTPUT_PRICE), output);
		return config;
	}
}
