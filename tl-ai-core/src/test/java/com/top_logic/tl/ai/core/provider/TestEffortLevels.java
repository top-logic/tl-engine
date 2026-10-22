/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.EffortLevels.Kind;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelCapabilities;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelLimits;

/**
 * Which models offer an effort setting, and where the chosen level belongs.
 */
public class TestEffortLevels extends TestCase {

	private static final AgentModelAssignment ASSIGNMENT = new AgentModelAssignment(
		"some-model", "some-provider", null, null, null, null, null, null, null, null, null, null, null, null, null);

	/** Anthropic reasoning models take a thinking effort, with a fourth level the others lack. */
	public void testAnthropic() {
		ModelInfo model = reasoning(true);
		assertEquals(Kind.THINKING_EFFORT, EffortLevels.kindOf("anthropic", model));
		assertEquals(List.of("low", "medium", "high", "max"), EffortLevels.forModel("anthropic", model));
		assertTrue(EffortLevels.isValid("anthropic", model, "max"));
	}

	/** OpenAI and xAI take a reasoning effort, which has no "max". */
	public void testOpenAiAndXai() {
		ModelInfo model = reasoning(true);
		for (String provider : List.of("openai", "xai")) {
			assertEquals(provider, Kind.REASONING_EFFORT, EffortLevels.kindOf(provider, model));
			assertEquals(provider, List.of("low", "medium", "high"), EffortLevels.forModel(provider, model));
			assertFalse(provider, EffortLevels.isValid(provider, model, "max"));
		}
	}

	/** A model that cannot reason offers nothing, whoever serves it. */
	public void testWithoutReasoning() {
		ModelInfo model = reasoning(false);
		assertEquals(Kind.NONE, EffortLevels.kindOf("anthropic", model));
		assertEquals(List.of(), EffortLevels.forModel("anthropic", model));
		assertFalse(EffortLevels.isValid("anthropic", model, "high"));
	}

	/** An unknown provider or an uncatalogued model offers nothing rather than guessing. */
	public void testUnknown() {
		assertEquals(Kind.NONE, EffortLevels.kindOf("mistral", reasoning(true)));
		assertEquals(Kind.NONE, EffortLevels.kindOf("anthropic", null));
		assertEquals(Kind.NONE, EffortLevels.kindOf(null, reasoning(true)));
	}

	/** Blank is never a level, so a caller need not special-case "no choice". */
	public void testBlankIsNotALevel() {
		assertFalse(EffortLevels.isValid("anthropic", reasoning(true), null));
		assertFalse(EffortLevels.isValid("anthropic", reasoning(true), "  "));
		assertFalse(EffortLevels.isValid("anthropic", reasoning(true), "enormous"));
	}

	/** The two setters write to their own field and leave the other alone. */
	public void testAssignmentKeepsTheOtherField() {
		AgentModelAssignment thinking = ASSIGNMENT.withThinkingEffort("high");
		assertEquals("high", thinking.thinkingEffort());
		assertNull(thinking.reasoningEffort());
		assertEquals("high", thinking.effectiveEffort());

		AgentModelAssignment both = thinking.withReasoningEffort("low");
		assertEquals("high", both.thinkingEffort());
		assertEquals("low", both.reasoningEffort());
		// Thinking wins when both are set: it is the one Anthropic reads.
		assertEquals("high", both.effectiveEffort());

		assertNull(ASSIGNMENT.effectiveEffort());
		assertEquals("some-model", thinking.model());
	}

	private static ModelInfo reasoning(boolean canReason) {
		return new ModelInfo("some-model", "family", new ModelLimits(1000, 100, 0),
			new ModelCapabilities(canReason, true, true, true, false, null, null), null, null, false, "");
	}
}
