/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider.catalog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
		assertEquals(1, info.cost().tiers().size());
		ModelInfo.TieredCost tier = info.cost().tiers().get(0);
		assertEquals(200_000, tier.threshold());
		assertEquals(10.0, tier.input(), 0.001);
		assertEquals(37.5, tier.output(), 0.001);
		assertNotNull(tier.cacheRead());
		assertEquals(1.0, tier.cacheRead(), 0.001);
		assertNotNull(tier.cacheWrite());
		assertEquals(12.5, tier.cacheWrite(), 0.001);

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
		assertEquals(List.of(), info.cost().tiers());
	}

	/** Each context tier of the tier list applies from its own size; a tier of another type is skipped. */
	public void testParseTierList() {
		String json = """
			{
			  "anthropic": {
			    "models": {
			      "claude-haiku-5-5": {
			        "id": "claude-haiku-5-5",
			        "cost": {
			          "input": 0.1, "output": 0.5, "cache_read": 0.01, "cache_write": 0.125,
			          "tiers": [
			            { "input": 1, "output": 5, "tier": { "type": "context", "size": 272000 } },
			            { "input": 9, "output": 9, "tier": { "type": "batch", "size": 1000 } },
			            { "tier": { "type": "context", "size": 100000 },
			              "input": 0.5, "output": 2.5, "cache_read": 0.05, "cache_write": 0.625 }
			          ]
			        },
			        "limit": { "context": 400000, "output": 64000 }
			      }
			    }
			  }
			}
			""";

		ModelInfo info = ModelsDevFetcher.parseJson(json, Map.of("anthropic", Set.of("claude-haiku-5-5")))
			.get("claude-haiku-5-5");

		List<ModelInfo.TieredCost> tiers = info.cost().tiers();
		assertEquals(List.of(
			new ModelInfo.TieredCost(100_000, 0.5, 2.5, 0.05, 0.625),
			new ModelInfo.TieredCost(272_000, 1, 5, null, null)), tiers);
	}

	/** A model that states the same threshold in both formats gets one tier, the one of the tier list. */
	public void testTierListWinsOverContextOver200k() {
		String json = """
			{
			  "openai": {
			    "models": {
			      "gpt-test": {
			        "id": "gpt-test",
			        "cost": {
			          "input": 1, "output": 2,
			          "context_over_200k": { "input": 3, "output": 4 },
			          "tiers": [ { "input": 5, "output": 6, "tier": { "type": "context", "size": 200000 } } ]
			        },
			        "limit": { "context": 400000, "output": 64000 }
			      }
			    }
			  }
			}
			""";

		ModelInfo info = ModelsDevFetcher.parseJson(json, Map.of("openai", Set.of("gpt-test"))).get("gpt-test");

		assertEquals(List.of(new ModelInfo.TieredCost(200_000, 5, 6, null, null)), info.cost().tiers());
	}

	/**
	 * The bundled snapshot knows the models the {@code anthropic} provider offers without
	 * configuration, so they have a context window and prices offline.
	 */
	public void testSnapshotKnowsTheDefaultClaudeModels() {
		Set<String> defaults = Set.of("claude-sonnet-5-5", "claude-opus-5-5", "claude-haiku-5-5");

		Map<String, ModelInfo> result = ModelsDevFetcher.loadSnapshot(Map.of("anthropic", defaults));

		assertEquals(defaults, result.keySet());
		for (ModelInfo info : result.values()) {
			assertTrue(info.id(), info.limits().contextWindow() > 0);
			assertTrue(info.id(), info.cost().input() > 0);
		}
		assertEquals("Haiku 5.5 is charged at a higher price above 100 000 prompt tokens.",
			100_000, result.get("claude-haiku-5-5").cost().tiers().get(0).threshold());
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

	/**
	 * A model the catalog lists only as {@code <name>@default} is found under its plain name.
	 */
	public void testDefaultVersionFound() {
		String json = """
			{
			  "google-vertex": {
			    "models": {
			      "claude-sonnet-5-5@default": {
			        "id": "claude-sonnet-5-5@default",
			        "limit": { "context": 1000000, "output": 128000 }
			      }
			    }
			  }
			}
			""";

		Map<String, ModelInfo> result =
			ModelsDevFetcher.parseJson(json, Map.of("google-vertex", Set.of("claude-sonnet-5-5")));

		ModelInfo info = result.get("claude-sonnet-5-5");
		assertNotNull(info);
		assertEquals("claude-sonnet-5-5", info.id());
		assertEquals(1000000, info.limits().contextWindow());
		assertEquals(1, result.size());
	}

	/**
	 * An entry under the plain name takes precedence over the default version, in either order.
	 */
	public void testExactEntryTakesPrecedence() {
		String exactFirst = """
			{
			  "p": {
			    "models": {
			      "m": { "id": "m", "limit": { "context": 1000, "output": 10 } },
			      "m@default": { "id": "m@default", "limit": { "context": 2000, "output": 10 } }
			    }
			  }
			}
			""";
		String defaultFirst = """
			{
			  "p": {
			    "models": {
			      "m@default": { "id": "m@default", "limit": { "context": 2000, "output": 10 } },
			      "m": { "id": "m", "limit": { "context": 1000, "output": 10 } }
			    }
			  }
			}
			""";

		for (String json : List.of(exactFirst, defaultFirst)) {
			Map<String, ModelInfo> result = ModelsDevFetcher.parseJson(json, Map.of("p", Set.of("m")));
			assertEquals(1000, result.get("m").limits().contextWindow());
			assertEquals(1, result.size());
		}
	}

	/**
	 * The bundled catalog gives Claude on Google Vertex AI its 1M context under the plain name.
	 */
	public void testBundledVertexClaude() {
		ModelInfo info = ModelsDevFetcher.loadSnapshot(Map.of("google-vertex", Set.of("claude-sonnet-5-5")))
			.get("claude-sonnet-5-5");

		assertNotNull(info);
		assertEquals(1_000_000, info.limits().contextWindow());
	}

	/**
	 * The size of the byte buffer through which the catalog stream is decoded.
	 *
	 * <p>
	 * No single read from the catalog stream requests more than this: the catalog is decoded piece
	 * by piece and never read as a whole.
	 * </p>
	 */
	private static final int DECODER_BUFFER_SIZE = 8192;

	/**
	 * A catalog stream with many providers that are not requested and one requested model is read
	 * piece by piece and results in exactly the requested model.
	 */
	public void testSnapshotIsReadAsStream() {
		int skippedProviders = 20_000;
		CatalogStream in = new CatalogStream(skippedProviders, "anthropic", "claude-wanted");

		Map<String, ModelInfo> result =
			ModelsDevFetcher.loadSnapshot(in, Map.of("anthropic", Set.of("claude-wanted")));

		assertEquals(Set.of("claude-wanted"), result.keySet());
		assertEquals(4096, result.get("claude-wanted").limits().contextWindow());
		assertTrue("Catalog not read up to the requested provider.", in.isGeneratedCompletely());
		assertTrue("Catalog must be larger than one read: " + in.getBytesRead(),
			in.getBytesRead() > 100 * DECODER_BUFFER_SIZE);
		assertTrue("Largest read: " + in.getMaxRequest(), in.getMaxRequest() <= DECODER_BUFFER_SIZE);
	}

	/**
	 * A snapshot that is no valid JSON results in an empty catalog, not in an exception.
	 */
	public void testBrokenSnapshotIsEmpty() {
		Map<String, Set<String>> requested = Map.of("anthropic", Set.of("claude-wanted"));

		assertEquals(Map.of(), ModelsDevFetcher.loadSnapshot(stream("{\"anthropic\": {\"models\": "), requested));
		assertEquals(Map.of(), ModelsDevFetcher.loadSnapshot(stream("<html>login</html>"), requested));
		assertEquals(Map.of(), ModelsDevFetcher.loadSnapshot(stream(
			"{\"anthropic\": {\"models\": {\"claude-wanted\": "
				+ "{\"id\": \"claude-wanted\", \"limit\": {\"context\": \"many\"}}}}}"),
			requested));
	}

	private static InputStream stream(String json) {
		return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Catalog generated while it is read, with a number of providers that are not requested before
	 * one provider with the requested model.
	 *
	 * <p>
	 * Records the largest number of bytes requested by a single read and refuses to be read as a
	 * whole.
	 * </p>
	 */
	private static final class CatalogStream extends InputStream {

		private final int _skippedProviders;

		private final String _provider;

		private final String _model;

		/** Number of the next part to generate: 0 is the opening, then the providers. */
		private int _nextPart;

		private byte[] _part = new byte[0];

		private int _pos;

		private long _bytesRead;

		private int _maxRequest;

		CatalogStream(int skippedProviders, String provider, String model) {
			_skippedProviders = skippedProviders;
			_provider = provider;
			_model = model;
		}

		/**
		 * Whether all parts up to the requested provider at the end were handed out.
		 */
		boolean isGeneratedCompletely() {
			return _nextPart > _skippedProviders + 1;
		}

		long getBytesRead() {
			return _bytesRead;
		}

		int getMaxRequest() {
			return _maxRequest;
		}

		@Override
		public int read() {
			byte[] buffer = new byte[1];
			int n = read(buffer, 0, 1);
			return n < 0 ? -1 : buffer[0] & 0xFF;
		}

		@Override
		public int read(byte[] buffer, int off, int len) {
			_maxRequest = Math.max(_maxRequest, len);
			if (len == 0) {
				return 0;
			}
			while (_pos == _part.length) {
				if (!nextPart()) {
					return -1;
				}
			}
			int n = Math.min(len, _part.length - _pos);
			System.arraycopy(_part, _pos, buffer, off, n);
			_pos += n;
			_bytesRead += n;
			return n;
		}

		@Override
		public byte[] readAllBytes() throws IOException {
			throw new AssertionError("Catalog must not be read as a whole.");
		}

		@Override
		public byte[] readNBytes(int len) throws IOException {
			throw new AssertionError("Catalog must not be read as a whole.");
		}

		private boolean nextPart() {
			int part = _nextPart++;
			String text;
			if (part == 0) {
				text = "{";
			} else if (part <= _skippedProviders) {
				String id = "provider-" + part;
				text = "\"" + id + "\": {\"id\": \"" + id + "\", \"name\": \"Provider \u00e4\u00f6\u00fc " + part
					+ "\", \"models\": {\"model-" + part + "\": {\"id\": \"model-" + part
					+ "\", \"limit\": {\"context\": 1000, \"output\": 100}}}},";
			} else if (part == _skippedProviders + 1) {
				text = "\"" + _provider + "\": {\"models\": {\"" + _model + "\": {\"id\": \"" + _model
					+ "\", \"limit\": {\"context\": 4096, \"output\": 1024}}}}}";
			} else {
				return false;
			}
			_part = text.getBytes(StandardCharsets.UTF_8);
			_pos = 0;
			return true;
		}
	}
}
