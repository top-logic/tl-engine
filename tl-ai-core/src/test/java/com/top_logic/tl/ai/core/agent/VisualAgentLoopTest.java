/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProvider;
import com.top_logic.tl.ai.core.tools.Tool;
import com.top_logic.tl.ai.core.tools.ToolMode;
import com.top_logic.tl.ai.core.tools.ToolDispatcher;
import com.top_logic.tl.ai.core.tools.ToolExecutionContext;
import com.top_logic.tl.ai.core.tools.ToolResult;

/**
 * Visual agent loop test — prints the full agent loop lifecycle to stdout.
 *
 * <p>Two modes:</p>
 * <ul>
 *   <li><b>Mock mode</b> (no API key): simulates a 3-turn conversation with tool calls</li>
 *   <li><b>Live mode</b> (ANTHROPIC_API_KEY set): runs a real agent loop against the API</li>
 * </ul>
 *
 * <p>Run manually:</p>
 * <pre>
 * mvn test -pl tl-ai-core -Dtest=VisualAgentLoopTest -DskipTests=false
 * source env.sh &amp;&amp; mvn test -pl tl-ai-core -Dtest=VisualAgentLoopTest -DskipTests=false
 * </pre>
 */
public class VisualAgentLoopTest extends TestCase {

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_DIM = "\u001B[2m";

	/**
	 * Simulated 3-turn agent loop with tool calls, a tool error, and final completion.
	 */
	public void testMockAgentLoop() {
		System.out.println("\n" + ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "  Agent Loop — Mock Simulation (3 turns)" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);

		ToolSchema weatherSpec = new ToolSchema("get_weather",
			"Gets the current weather for a city",
			"{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}}}");

		ToolSchema calcSpec = new ToolSchema("calculate",
			"Evaluates a math expression",
			"{\"type\":\"object\",\"properties\":{\"expr\":{\"type\":\"string\"}}}");

		// Turn 1: LLM calls get_weather
		// Turn 2: LLM calls calculate (will fail) + get_weather again
		// Turn 3: LLM responds with final text
		LLMProvider provider = mockProviderWithDelays(
			List.of(
				new ToolCall("tc1", "get_weather", "{\"city\":\"Berlin\"}")),
			List.of(
				new ToolCall("tc2", "calculate", "{\"expr\":\"20 * 9/5 + 32\"}"),
				new ToolCall("tc3", "get_weather", "{\"city\":\"Munich\"}")),
			List.of(
				new TextContent("Based on my research: Berlin is 20°C and sunny. "),
				new TextContent("Munich is 15°C and cloudy. "),
				new TextContent("I couldn't convert the temperature because the calculator "),
				new TextContent("was unavailable, but 20°C is about 68°F.")));

		ToolDispatcher registry = new ToolDispatcher();
		registry.register(new Tool() {
			@Override public String name() { return "get_weather"; }
			@Override public String description() { return "Gets the current weather for a city"; }
			@Override public ToolSchema schema() { return weatherSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				sleep(100);
				String city = args.containsKey("city") ? String.valueOf(args.get("city")) : "Unknown";
				String temp = "Berlin".equals(city) ? "20°C, sunny" : "15°C, cloudy";
				return ToolResult.of("Weather in " + city + ": " + temp);
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.register(new Tool() {
			@Override public String name() { return "calculate"; }
			@Override public String description() { return "Evaluates a math expression"; }
			@Override public ToolSchema schema() { return calcSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				sleep(100);
				throw new RuntimeException("Calculator service unavailable");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		Conversation conv = new Conversation();
		conv.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("What's the weather in Berlin and Munich? Convert Berlin's temp to Fahrenheit."))));

		AgentParams config = new AgentParams("mock-model", "You are a helpful assistant.",
			10, 4096);

		AgentLoop loop = new AgentLoop(config, provider, registry);
		AgentResult result = loop.run(conv, printingListener());

		printResult(result, conv);
	}

	/**
	 * Live agent loop against Anthropic API with a tool.
	 */
	public void testLiveAgentLoop() throws Exception {
		String apiKey = System.getenv("ANTHROPIC_API_KEY");
		if (apiKey == null || apiKey.isEmpty()) {
			System.out.println("\nSKIP: testLiveAgentLoop — no ANTHROPIC_API_KEY (source env.sh)");
			return;
		}

		System.out.println("\n" + ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "  Agent Loop — Live (Anthropic API)" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);

		AnthropicProvider provider = new AnthropicProvider(apiKey);

		ToolSchema toolSpec = new ToolSchema("get_current_time",
			"Returns the current server time in ISO-8601 format.",
			"{\"type\":\"object\",\"properties\":{}}");

		ToolDispatcher registry = new ToolDispatcher();
		registry.register(new Tool() {
			@Override public String name() { return "get_current_time"; }
			@Override public String description() { return "Returns the current server time in ISO-8601 format."; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of(Instant.now().toString());
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		Conversation conv = new Conversation();
		conv.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("What time is it right now? Use the tool to check."))));

		AgentParams config = new AgentParams("claude-haiku-4-5-20251001",
			"You are a helpful assistant. Use tools when appropriate. Be concise.",
			5, 1024);

		AgentLoop loop = new AgentLoop(config, provider, registry);
		AgentResult result = loop.run(conv, printingListener());

		printResult(result, conv);
	}

	private static AgentEventListener printingListener() {
		return e -> {
			if (e instanceof AgentEvent.TurnStarted t) {
				System.out.println("\n" + ANSI_BLUE + "── Turn " + t.turnNumber() + " ──" + ANSI_RESET);
			} else if (e instanceof AgentEvent.TurnCompleted t) {
				System.out.println(ANSI_DIM + "\n  tokens: " + t.turnUsage().inputTokens() + " in / "
					+ t.turnUsage().outputTokens() + " out" + ANSI_RESET);
			} else if (e instanceof AgentEvent.TextDelta d) {
				System.out.print(d.text());
			} else if (e instanceof AgentEvent.ToolCallStarted s) {
				System.out.println(ANSI_YELLOW + "  ⚡ " + s.toolName()
					+ "(" + truncate(s.argsJson(), 60) + ")" + ANSI_RESET);
			} else if (e instanceof AgentEvent.ToolCallCompleted c) {
				System.out.println(ANSI_GREEN + "  ✓ " + c.toolName()
					+ " → " + truncate(c.result(), 80) + ANSI_RESET);
			} else if (e instanceof AgentEvent.ToolCallFailed f) {
				System.out.println(ANSI_RED + "  ✗ " + f.toolName()
					+ " → " + f.errorMessage() + ANSI_RESET);
			} else if (e instanceof AgentEvent.ProviderError p) {
				System.out.println(ANSI_RED + "\n  PROVIDER ERROR: " + p.kind()
					+ " — " + p.message() + ANSI_RESET);
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
				sleep(50); // simulate streaming delay
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
