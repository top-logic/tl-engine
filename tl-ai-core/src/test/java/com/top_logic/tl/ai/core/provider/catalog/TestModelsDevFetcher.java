/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests for {@link ModelsDevFetcher}.
 */
public class TestModelsDevFetcher extends TestCase {

	/** */
	public void testParseAllFieldsPresent() {
		String json = """
			{
			  "anthropic": {
			    "id": "anthropic",
			    "name": "Anthropic",
			    "models": {
			      "claude-opus-4-7": {
			        "id": "claude-opus-4-7",
			        "name": "Claude Opus 4.7",
			        "family": "claude-opus",
			        "status": "beta",
			        "attachment": true,
			        "reasoning": true,
			        "tool_call": true,
			        "temperature": true,
			        "structured_output": true,
			        "open_weights": false,
			        "interleaved": true,
			        "knowledge": "2026-01-31",
			        "release_date": "2026-04-17",
			        "last_updated": "2026-04-17",
			        "modalities": { "input": ["text", "image", "pdf"], "output": ["text"] },
			        "cost": {
			          "input": 5, "output": 25,
			          "cache_read": 0.5, "cache_write": 6.25,
			          "context_over_200k": {
			            "input": 10, "output": 37.5,
			            "cache_read": 1, "cache_write": 12.5
			          }
			        },
			        "limit": { "context": 1000000, "output": 128000 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("anthropic", Set.of("claude-opus-4-7"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		assertEquals(1, result.size());
		ModelInfo info = result.get("claude-opus-4-7");
		assertNotNull(info);
		assertEquals("claude-opus-4-7", info.id());
		assertEquals("claude-opus", info.family());

		// Limits
		assertEquals(1000000, info.limits().contextWindow());
		assertEquals(128000, info.limits().maxOutput());
		assertEquals(0, info.limits().maxInput());

		// Capabilities
		assertTrue(info.capabilities().reasoning());
		assertTrue(info.capabilities().toolCall());
		assertTrue(info.capabilities().temperature());
		assertTrue(info.capabilities().attachment());
		assertTrue(info.capabilities().structuredOutput());
		assertNotNull(info.capabilities().modalities());
		assertEquals(List.of("text", "image", "pdf"), info.capabilities().modalities().input());
		assertEquals(List.of("text"), info.capabilities().modalities().output());

		// Interleaved (boolean true form)
		assertNotNull(info.capabilities().interleaved());
		assertNull(info.capabilities().interleaved().field());

		// Cost
		assertEquals(5.0, info.cost().input(), 0.001);
		assertEquals(25.0, info.cost().output(), 0.001);
		assertNotNull(info.cost().cacheRead());
		assertEquals(0.5, info.cost().cacheRead(), 0.001);
		assertNotNull(info.cost().cacheWrite());
		assertEquals(6.25, info.cost().cacheWrite(), 0.001);
		assertNull(info.cost().reasoning());
		assertNull(info.cost().inputAudio());
		assertNull(info.cost().outputAudio());

		// Tiered cost
		assertNotNull(info.cost().tieredCost());
		assertEquals(10.0, info.cost().tieredCost().input(), 0.001);
		assertEquals(37.5, info.cost().tieredCost().output(), 0.001);
		assertNotNull(info.cost().tieredCost().cacheRead());
		assertEquals(1.0, info.cost().tieredCost().cacheRead(), 0.001);
		assertNotNull(info.cost().tieredCost().cacheWrite());
		assertEquals(12.5, info.cost().tieredCost().cacheWrite(), 0.001);

		// Dates
		assertNotNull(info.dates());
		assertEquals("2026-01-31", info.dates().knowledgeCutoff());
		assertEquals("2026-04-17", info.dates().releaseDate());
		assertEquals("2026-04-17", info.dates().lastUpdated());

		// Metadata
		assertFalse(info.openWeights());
		assertEquals("beta", info.status());
	}

	/** */
	public void testInterleavedObjectForm() {
		String json = """
			{
			  "deepseek": {
			    "id": "deepseek",
			    "name": "DeepSeek",
			    "models": {
			      "deepseek-r1": {
			        "id": "deepseek-r1",
			        "reasoning": true,
			        "tool_call": true,
			        "temperature": true,
			        "interleaved": { "field": "reasoning_content" },
			        "cost": { "input": 0.55, "output": 2.19 },
			        "limit": { "context": 131072, "output": 8192 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("deepseek", Set.of("deepseek-r1"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		ModelInfo info = result.get("deepseek-r1");
		assertNotNull(info);
		assertNotNull(info.capabilities().interleaved());
		assertEquals("reasoning_content", info.capabilities().interleaved().field());
	}

	/** */
	public void testInterleavedFalseIsNull() {
		String json = """
			{
			  "openai": {
			    "id": "openai",
			    "name": "OpenAI",
			    "models": {
			      "gpt-4o": {
			        "id": "gpt-4o",
			        "reasoning": false,
			        "tool_call": true,
			        "temperature": true,
			        "interleaved": false,
			        "cost": { "input": 2.5, "output": 10 },
			        "limit": { "context": 128000, "output": 16384 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("openai", Set.of("gpt-4o"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		ModelInfo info = result.get("gpt-4o");
		assertNotNull(info);
		assertNull(info.capabilities().interleaved());
	}

	/** */
	public void testStatusAbsentIsNull() {
		String json = """
			{
			  "openai": {
			    "id": "openai",
			    "name": "OpenAI",
			    "models": {
			      "gpt-4o": {
			        "id": "gpt-4o",
			        "reasoning": false,
			        "tool_call": true,
			        "temperature": true,
			        "cost": { "input": 2.5, "output": 10 },
			        "limit": { "context": 128000, "output": 16384 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("openai", Set.of("gpt-4o"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		ModelInfo info = result.get("gpt-4o");
		assertNotNull(info);
		assertNull(info.status());
	}

	/** */
	public void testDeprecatedStatus() {
		String json = """
			{
			  "openai": {
			    "id": "openai",
			    "name": "OpenAI",
			    "models": {
			      "gpt-3.5-turbo": {
			        "id": "gpt-3.5-turbo",
			        "status": "deprecated",
			        "reasoning": false,
			        "tool_call": true,
			        "temperature": true,
			        "cost": { "input": 0.5, "output": 1.5 },
			        "limit": { "context": 16384, "output": 4096 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("openai", Set.of("gpt-3.5-turbo"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		ModelInfo info = result.get("gpt-3.5-turbo");
		assertNotNull(info);
		assertEquals("deprecated", info.status());
	}

	/** */
	public void testAudioAndReasoningCosts() {
		String json = """
			{
			  "openai": {
			    "id": "openai",
			    "name": "OpenAI",
			    "models": {
			      "gpt-4o-audio": {
			        "id": "gpt-4o-audio",
			        "reasoning": false,
			        "tool_call": true,
			        "temperature": true,
			        "cost": {
			          "input": 2.5, "output": 10,
			          "reasoning": 7.5,
			          "input_audio": 40, "output_audio": 80
			        },
			        "limit": { "context": 128000, "output": 16384, "input": 100000 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("openai", Set.of("gpt-4o-audio"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		ModelInfo info = result.get("gpt-4o-audio");
		assertNotNull(info);

		assertNotNull(info.cost().reasoning());
		assertEquals(7.5, info.cost().reasoning(), 0.001);
		assertNotNull(info.cost().inputAudio());
		assertEquals(40.0, info.cost().inputAudio(), 0.001);
		assertNotNull(info.cost().outputAudio());
		assertEquals(80.0, info.cost().outputAudio(), 0.001);

		assertEquals(100000, info.limits().maxInput());
	}

	/** */
	public void testUnrequestedModelsSkipped() {
		String json = """
			{
			  "anthropic": {
			    "id": "anthropic",
			    "name": "Anthropic",
			    "models": {
			      "claude-sonnet-4-20250514": {
			        "id": "claude-sonnet-4-20250514",
			        "limit": { "context": 200000, "output": 64000 }
			      },
			      "claude-haiku-3-20240307": {
			        "id": "claude-haiku-3-20240307",
			        "limit": { "context": 200000, "output": 4096 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("anthropic", Set.of("claude-sonnet-4-20250514"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		assertEquals(1, result.size());
		assertNotNull(result.get("claude-sonnet-4-20250514"));
		assertNull(result.get("claude-haiku-3-20240307"));
	}

	/** */
	public void testUnrequestedProvidersSkipped() {
		String json = """
			{
			  "anthropic": {
			    "id": "anthropic",
			    "name": "Anthropic",
			    "models": {
			      "claude-sonnet-4-20250514": {
			        "id": "claude-sonnet-4-20250514",
			        "limit": { "context": 200000, "output": 64000 }
			      }
			    }
			  },
			  "openai": {
			    "id": "openai",
			    "name": "OpenAI",
			    "models": {
			      "gpt-4o": {
			        "id": "gpt-4o",
			        "limit": { "context": 128000, "output": 16384 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("openai", Set.of("gpt-4o"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		assertEquals(1, result.size());
		assertNotNull(result.get("gpt-4o"));
		assertNull(result.get("claude-sonnet-4-20250514"));
	}

	/** */
	public void testMissingOptionalFields() {
		String json = """
			{
			  "openai": {
			    "id": "openai",
			    "name": "OpenAI",
			    "models": {
			      "gpt-4o-mini": {
			        "id": "gpt-4o-mini",
			        "reasoning": false,
			        "tool_call": true,
			        "temperature": true,
			        "cost": { "input": 0.15, "output": 0.6 },
			        "limit": { "context": 128000, "output": 16384 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("openai", Set.of("gpt-4o-mini"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		assertEquals(1, result.size());
		ModelInfo info = result.get("gpt-4o-mini");
		assertNotNull(info);
		assertEquals("gpt-4o-mini", info.id());
		assertNull(info.family());

		assertNull(info.capabilities().modalities());
		assertFalse(info.capabilities().reasoning());
		assertTrue(info.capabilities().toolCall());
		assertFalse(info.capabilities().attachment());
		assertFalse(info.capabilities().structuredOutput());

		assertNull(info.dates());
		assertFalse(info.openWeights());

		assertEquals(0.15, info.cost().input(), 0.001);
		assertEquals(0.6, info.cost().output(), 0.001);
		assertNull(info.cost().cacheRead());
		assertNull(info.cost().cacheWrite());
		assertNull(info.cost().reasoning());
		assertNull(info.cost().tieredCost());
	}

	/** */
	public void testMissingContextReturnsNull() {
		String json = """
			{
			  "anthropic": {
			    "id": "anthropic",
			    "name": "Anthropic",
			    "models": {
			      "broken-model": {
			        "id": "broken-model",
			        "limit": { "context": 0, "output": 100 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("anthropic", Set.of("broken-model"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		assertTrue(result.isEmpty());
	}

	/** */
	public void testMissingIdReturnsNull() {
		String json = """
			{
			  "anthropic": {
			    "id": "anthropic",
			    "name": "Anthropic",
			    "models": {
			      "no-id-model": {
			        "limit": { "context": 100000, "output": 4096 }
			      }
			    }
			  }
			}
			""";

		Map<String, Set<String>> requested = Map.of("anthropic", Set.of("no-id-model"));
		Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, requested);

		assertTrue(result.isEmpty());
	}
}
