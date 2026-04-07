/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentConfig;
import com.top_logic.tl.ai.core.agent.AgentListener;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;

/**
 * Visual test for the tool system — prints the full pipeline to stdout.
 *
 * <p>Demonstrates annotated tools registered via {@link ToolRegistry}, schema filtering
 * by {@link ToolMode}, and end-to-end execution through the {@link AgentLoop}.</p>
 *
 * <p>Two modes:</p>
 * <ul>
 *   <li><b>Mock mode</b> (no API key): simulates a multi-turn conversation with tool calls</li>
 *   <li><b>Live mode</b> (ANTHROPIC_API_KEY set): runs against the real API</li>
 * </ul>
 *
 * <p>Run manually:</p>
 * <pre>
 * mvn test -pl tl-ai-core -Dtest=VisualToolRegistryTest -DskipTests=false
 * source env.sh &amp;&amp; mvn test -pl tl-ai-core -Dtest=VisualToolRegistryTest -DskipTests=false
 * </pre>
 */
public class VisualToolRegistryTest extends TestCase {

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_DIM = "\u001B[2m";
	private static final String ANSI_MAGENTA = "\u001B[35m";

	// --- Annotated tool providers ---

	/** */
	static class MathTools {

		/** */
		@ToolMethod(description ="Add two numbers together", mode = ToolMode.READ, concurrencySafe = true)
		public String add(@P("first number") int a, @P("second number") int b) {
			return String.valueOf(a + b);
		}

		/** */
		@ToolMethod(description ="Multiply two numbers", mode = ToolMode.READ, concurrencySafe = true)
		public String multiply(@P("first number") int a, @P("second number") int b) {
			return String.valueOf(a * b);
		}
	}

	/** */
	static class WeatherTools {

		/** */
		@ToolMethod(description ="Get the current weather for a city", name = "get_weather",
			mode = ToolMode.READ, concurrencySafe = true)
		public String getWeather(@P("city name") String city) {
			return switch (city.toLowerCase()) {
				case "berlin" -> "20°C, sunny";
				case "munich" -> "15°C, cloudy";
				case "hamburg" -> "12°C, rainy";
				default -> "22°C, partly cloudy";
			};
		}
	}

	/** */
	static class AdminTools {

		/** */
		@ToolMethod(description = "Delete all records from a table")
		public String deleteAll(@P("table name") String table) {
			return "Deleted all records from " + table;
		}
	}

	/**
	 * Mock mode: demonstrates registry, filtering, and multi-turn tool execution.
	 */
	public void testMockToolRegistry() {
		header("Tool System — Mock Simulation");

		// Build registry with all tools
		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		registry.register(new WeatherTools());
		registry.register(new AdminTools());

		printRegistryInfo(registry);

		// Simulate a "regular user" filter — no GLOBAL tools
		printFilterDemo(registry);

		// Run agent loop with regular-user tools only
		System.out.println(ANSI_BLUE + "\n── Agent Loop (regular user, no GLOBAL tools) ──" + ANSI_RESET);

		LLMProvider provider = mockProviderWithDelays(
			// Turn 1: LLM calls get_weather + add
			List.of(
				new ToolCall("tc1", "get_weather", "{\"city\":\"Berlin\"}"),
				new ToolCall("tc2", "add", "{\"a\":20,\"b\":12}")),
			// Turn 2: LLM calls multiply
			List.of(
				new ToolCall("tc3", "multiply", "{\"a\":3,\"b\":7}")),
			// Turn 3: LLM responds with text
			List.of(
				new TextContent("Here's what I found: Berlin is 20°C and sunny. "),
				new TextContent("20 + 12 = 32, and 3 × 7 = 21.")));

		AgentConfig config = new AgentConfig("mock-model",
			"You are a helpful assistant with math and weather tools.",
			registry.toolSchemas(t -> t.mode() != ToolMode.GLOBAL), 10, 4096);

		Conversation conv = new Conversation();
		conv.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("What's the weather in Berlin? Also compute 20+12 and 3*7."))));

		AgentLoop loop = new AgentLoop(config, provider, registry);
		AgentResult result = loop.run(conv, printingListener());

		printResult(result, conv);
	}

	/**
	 * Live mode: real Anthropic API with annotated tools via ToolRegistry.
	 */
	public void testLiveToolRegistry() {
		String apiKey = System.getenv("ANTHROPIC_API_KEY");
		if (apiKey == null || apiKey.isEmpty()) {
			System.out.println("\nSKIP: testLiveToolRegistry — no ANTHROPIC_API_KEY (source env.sh)");
			return;
		}

		header("Tool System — Live (Anthropic API)");

		ToolRegistry registry = new ToolRegistry();
		registry.register(new MathTools());
		registry.register(new WeatherTools());

		printRegistryInfo(registry);

		AnthropicProvider provider = new AnthropicProvider(apiKey);

		AgentConfig config = new AgentConfig("claude-haiku-4-5-20251001",
			"You are a helpful assistant. Use the available tools to answer questions. Be concise.",
			registry.toolSchemas(), 5, 1024);

		Conversation conv = new Conversation();
		conv.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent(
				"What is 17 + 25? Also what's the weather in Berlin?"))));

		AgentLoop loop = new AgentLoop(config, provider, registry);
		AgentResult result = loop.run(conv, printingListener());

		printResult(result, conv);
	}

	// --- Registry info printing ---

	private static void printRegistryInfo(ToolRegistry registry) {
		System.out.println(ANSI_MAGENTA + "\nRegistered tools:" + ANSI_RESET);
		for (Tool tool : registry.tools()) {
			String mode = tool.mode().name();
			String safe = tool.isConcurrencySafe() ? "yes" : "no";
			System.out.println("  " + ANSI_YELLOW + tool.name() + ANSI_RESET
				+ " — " + tool.description()
				+ ANSI_DIM + " [mode=" + mode + ", concurrent=" + safe + "]" + ANSI_RESET);
		}
		System.out.println(ANSI_DIM + "  Total: " + registry.tools().size() + " tools, "
			+ registry.toolSchemas().size() + " schemas" + ANSI_RESET);
	}

	private static void printFilterDemo(ToolRegistry registry) {
		System.out.println(ANSI_MAGENTA + "\nFiltering demo:" + ANSI_RESET);

		int all = registry.toolSchemas().size();
		int readOnly = registry.toolSchemas(t -> t.mode() == ToolMode.READ).size();
		int global = registry.toolSchemas(t -> t.mode() == ToolMode.GLOBAL).size();
		int userVisible = registry.toolSchemas(t -> t.mode() != ToolMode.GLOBAL).size();

		System.out.println("  All tools:           " + all);
		System.out.println("  READ only:           " + readOnly);
		System.out.println("  GLOBAL only:         " + global);
		System.out.println("  Regular user sees:   " + userVisible + " (excludes GLOBAL)");
	}

	// --- Shared helpers (same pattern as VisualAgentLoopTest) ---

	private static void header(String title) {
		System.out.println("\n" + ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "  " + title + ANSI_RESET);
		System.out.println(ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);
	}

	private static AgentListener printingListener() {
		return new AgentListener() {
			@Override
			public void onTurnStart(int turnNumber) {
				System.out.println("\n" + ANSI_BLUE + "── Turn " + turnNumber + " ──" + ANSI_RESET);
			}

			@Override
			public void onTurnComplete(int turnNumber, TokenUsage usage) {
				System.out.println(ANSI_DIM + "\n  tokens: " + usage.inputTokens() + " in / "
					+ usage.outputTokens() + " out" + ANSI_RESET);
			}

			@Override
			public void onTextDelta(String text) {
				System.out.print(text);
			}

			@Override
			public void onToolCallStart(ToolCall toolCall) {
				System.out.println(ANSI_YELLOW + "  ⚡ " + toolCall.name()
					+ "(" + truncate(toolCall.arguments(), 60) + ")" + ANSI_RESET);
			}

			@Override
			public void onToolCallComplete(ToolCall toolCall, ToolResultMessage result) {
				if (result.state() instanceof ToolState.Completed c) {
					System.out.println(ANSI_GREEN + "  ✓ " + toolCall.name()
						+ " → " + truncate(c.result(), 80) + ANSI_RESET);
				}
			}

			@Override
			public void onToolError(ToolCall toolCall, String error) {
				System.out.println(ANSI_RED + "  ✗ " + toolCall.name()
					+ " → " + error + ANSI_RESET);
			}

			@Override
			public void onError(ProviderException error) {
				System.out.println(ANSI_RED + "\n  PROVIDER ERROR: " + error.kind()
					+ " — " + error.getMessage() + ANSI_RESET);
			}
		};
	}

	private static void printResult(AgentResult result, Conversation conv) {
		System.out.println("\n" + ANSI_CYAN + "───────────────────────────────────────────" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "  Result" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "───────────────────────────────────────────" + ANSI_RESET);
		System.out.println("  Stop reason:  " + result.stopReason());
		System.out.println("  Turns used:   " + result.turnsUsed());
		System.out.println("  Total tokens: " + result.totalUsage().inputTokens() + " in / "
			+ result.totalUsage().outputTokens() + " out ("
			+ result.totalUsage().totalTokens() + " total)");
		System.out.println("  Messages:     " + conv.size());
		System.out.println();
	}

	private static String truncate(String s, int max) {
		return s.length() <= max ? s : s.substring(0, max) + "...";
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@SafeVarargs
	private static LLMProvider mockProviderWithDelays(List<ContentPart>... responses) {
		int[] callIndex = {0};
		List<List<ContentPart>> responseList = List.of(responses);

		return (request, handler) -> {
			int idx = callIndex[0]++;
			if (idx >= responseList.size()) {
				throw new ProviderException(
					ProviderException.Kind.INVALID_REQUEST, 400,
					"Mock: no more responses");
			}
			List<ContentPart> content = responseList.get(idx);

			for (int i = 0; i < content.size(); i++) {
				sleep(50);
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall tc) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, tc.id(), tc.name()));
					sleep(30);
					handler.onEvent(new StreamEvent.ToolCallDelta(i, tc.arguments()));
				}
			}

			String finishReason = content.stream().anyMatch(ToolCall.class::isInstance)
				? "tool_use" : "end_turn";
			TokenUsage usage = new TokenUsage(150 + idx * 100, 40 + idx * 20, 0, 0, 0);

			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-" + idx, "mock-model", finishReason, usage, content));
		};
	}
}
