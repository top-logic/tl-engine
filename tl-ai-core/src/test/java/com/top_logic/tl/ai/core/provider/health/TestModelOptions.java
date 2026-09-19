/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * Tests that a run's sampling options are rebuilt for the model it fails over to rather than
 * carried across as they were.
 *
 * @see ModelOptions#forTarget(Map, String, ModelInfo)
 */
public class TestModelOptions extends TestCase {

	private static final Map<String, Object> ANTHROPIC_RUN = Map.of(
		"temperature", Double.valueOf(0.4),
		"topP", Double.valueOf(0.9),
		"top_k", Integer.valueOf(40),
		"cacheTtl", "1h",
		"thinking_effort", "medium");

	private static final Map<String, Object> OPENAI_RUN = Map.of(
		"temperature", Double.valueOf(0.4),
		"reasoning_effort", "high",
		"reasoning_summary", "auto");

	/** Anthropic's word for thinking does not reach an OpenAI model, nor its cache or top-k. */
	public void testAnthropicOptionsDoNotTravelToOpenAI() {
		Map<String, Object> result = ModelOptions.forTarget(ANTHROPIC_RUN, "openai", model(true, true));

		assertEquals(Map.of("temperature", Double.valueOf(0.4), "topP", Double.valueOf(0.9)), result);
	}

	/** And OpenAI's word for it does not reach an Anthropic model. */
	public void testOpenAIOptionsDoNotTravelToAnthropic() {
		Map<String, Object> result = ModelOptions.forTarget(OPENAI_RUN, "anthropic", model(true, true));

		assertEquals(Map.of("temperature", Double.valueOf(0.4)), result);
	}

	/** Staying with the same vendor keeps everything it understands. */
	public void testSameVendorKeepsItsOwnOptions() {
		Map<String, Object> result = ModelOptions.forTarget(ANTHROPIC_RUN, "anthropic", model(true, true));

		assertEquals(ANTHROPIC_RUN, result);
	}

	/** A model that takes no temperature is sent none - sending one is an error, not a no-op. */
	public void testTemperatureIsDroppedForAModelThatRefusesIt() {
		Map<String, Object> result = ModelOptions.forTarget(ANTHROPIC_RUN, "anthropic", model(false, true));

		assertFalse(result.containsKey("temperature"));
		assertFalse(result.containsKey("topP"));
		assertEquals("medium", result.get("thinking_effort"));
	}

	/** A model that does not reason is not asked to. */
	public void testReasoningIsDroppedForAModelWithoutIt() {
		Map<String, Object> result = ModelOptions.forTarget(OPENAI_RUN, "openai", model(true, false));

		assertEquals(Map.of("temperature", Double.valueOf(0.4)), result);
	}

	/**
	 * A model the catalog does not know keeps the vendor-neutral keys, which is what the
	 * {@code AgentFactory} does with an unknown model too.
	 */
	public void testUnknownTargetKeepsTheNeutralKeys() {
		Map<String, Object> result = ModelOptions.forTarget(ANTHROPIC_RUN, "anthropic", null);

		assertEquals(ANTHROPIC_RUN, result);
	}

	/** A provider nothing could be said about gets only what every provider understands. */
	public void testUnknownProviderGetsOnlyTheNeutralKeys() {
		Map<String, Object> result = ModelOptions.forTarget(ANTHROPIC_RUN, null, model(true, true));

		assertEquals(Map.of("temperature", Double.valueOf(0.4), "topP", Double.valueOf(0.9)), result);
	}

	private static ModelInfo model(boolean temperature, boolean reasoning) {
		return new ModelInfo("test", "Test",
			new ModelInfo.ModelLimits(200_000, 8_192, 200_000),
			new ModelInfo.ModelCapabilities(reasoning, true, temperature, false, false,
				new ModelInfo.ModelModalities(List.of("text"), List.of("text")), null),
			null, null, false, null);
	}
}
