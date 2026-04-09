/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentConfig;
import com.top_logic.tl.ai.core.agent.AgentListener;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * Visual test for the conversation memory system — prints the full lifecycle to stdout.
 *
 * <p>Demonstrates three scenarios:</p>
 * <ol>
 *   <li><b>Token estimation</b> — shows how different message types are estimated</li>
 *   <li><b>Truncation compaction</b> — drops oldest messages, preserves tool pairing</li>
 *   <li><b>Agent loop with auto-compaction</b> — full end-to-end with a tiny context window</li>
 * </ol>
 *
 * <p>Run manually:</p>
 * <pre>
 * mvn test -pl tl-ai-core -Dtest=VisualConversationMemoryTest -DskipTests=false
 * </pre>
 */
public class VisualConversationMemoryTest extends TestCase {

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_DIM = "\u001B[2m";
	private static final String ANSI_MAGENTA = "\u001B[35m";
	private static final String ANSI_BOLD = "\u001B[1m";
	private static final String ANSI_WHITE = "\u001B[37m";

	private static final Instant T0 = Instant.parse("2026-04-07T12:00:00Z");

	private final TokenEstimator _estimator = new ByteBasedTokenEstimator();

	/**
	 * Shows how the ByteBasedTokenEstimator estimates tokens for each message type.
	 */
	public void testTokenEstimation() {
		header("Token Estimation — bytes/4 heuristic");

		UserMessage shortUser = new UserMessage("u1", T0,
			List.of(new TextContent("Hello, world!")));
		UserMessage longUser = new UserMessage("u2", T0,
			List.of(new TextContent("x".repeat(1000))));
		AssistantMessage assistant = new AssistantMessage("a1", T0,
			List.of(new TextContent("Here is a detailed analysis of the situation...")),
			new TokenUsage(500, 80, 0, 0, 0), "end_turn");
		ToolResultMessage toolResult = new ToolResultMessage("tr1", T0,
			"tc1", "read_file",
			new ToolState.Completed("package com.example;\n\npublic class Foo {\n    // ...\n}",
				T0, T0, false));
		CompactionMarker marker = new CompactionMarker("m1", T0, 8, 5000, "auto");

		System.out.println(ANSI_MAGENTA + "\n  Message type estimates:" + ANSI_RESET);
		printEstimate("UserMessage (13 chars)", shortUser);
		printEstimate("UserMessage (1000 chars)", longUser);
		printEstimate("AssistantMessage (text)", assistant);
		printEstimate("ToolResultMessage (code)", toolResult);
		printEstimate("CompactionMarker", marker);

		System.out.println(ANSI_DIM + "\n  Formula: UTF-8 bytes / 4  (images = fixed 2000)" + ANSI_RESET);
		System.out.println();
	}

	/**
	 * Demonstrates the TruncatingStrategy dropping old messages and maintaining tool pairing.
	 */
	public void testTruncationCompaction() {
		header("Truncation Compaction — drop oldest, preserve recent");

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Explain the architecture"))));
		conv.add(new AssistantMessage("a1", T0,
			List.of(new TextContent("x".repeat(800))), // big response
			TokenUsage.EMPTY, "end_turn"));
		conv.add(new UserMessage("u2", T0, List.of(new TextContent("Now show me the config"))));
		conv.add(new AssistantMessage("a2", T0,
			List.of(
				new TextContent("Let me read that file."),
				new ToolCall("tc1", "read_file", "{\"path\":\"config.xml\"}")),
			TokenUsage.EMPTY, "tool_use"));
		conv.add(new ToolResultMessage("tr1", T0, "tc1", "read_file",
			new ToolState.Completed("<config>" + "x".repeat(600) + "</config>", T0, T0, false)));
		conv.add(new AssistantMessage("a3", T0,
			List.of(new TextContent("Here's what I found in the config.")),
			TokenUsage.EMPTY, "end_turn"));
		conv.add(new UserMessage("u3", T0, List.of(new TextContent("Thanks, now fix the bug"))));
		conv.add(new AssistantMessage("a4", T0,
			List.of(new TextContent("I'll fix that right away.")),
			TokenUsage.EMPTY, "end_turn"));

		int tokensBefore = _estimator.estimateTokens(conv.messages());

		System.out.println(ANSI_MAGENTA + "\n  Before compaction:" + ANSI_RESET);
		printConversation(conv.messages());
		System.out.println(ANSI_DIM + "  Total: " + tokensBefore + " estimated tokens, "
			+ conv.size() + " messages" + ANSI_RESET);

		// Target: 200 tokens, preserve last 4
		TruncatingStrategy strategy = new TruncatingStrategy(4);
		CompactionResult result = strategy.compact(conv.messages(), 200, _estimator);

		System.out.println(ANSI_MAGENTA + "\n  After compaction (target: 200 tokens, preserve last 4):" + ANSI_RESET);
		printConversation(result.replacementMessages());
		System.out.println(ANSI_DIM + "  Tokens: " + result.tokensBefore() + " → " + result.tokensAfter()
			+ " (" + ANSI_GREEN + "-" + (result.tokensBefore() - result.tokensAfter()) + ANSI_DIM
			+ "), " + result.messagesSummarized() + " messages dropped" + ANSI_RESET);
		System.out.println();
	}

	/**
	 * Demonstrates the SummarizingStrategy replacing old messages with an LLM-generated summary.
	 */
	public void testSummarizingCompaction() {
		header("Summarizing Compaction — LLM summary replaces old messages");

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("x".repeat(400)))));
		conv.add(new AssistantMessage("a1", T0,
			List.of(new TextContent("x".repeat(400))),
			TokenUsage.EMPTY, "end_turn"));
		conv.add(new UserMessage("u2", T0, List.of(new TextContent("x".repeat(400)))));
		conv.add(new AssistantMessage("a2", T0,
			List.of(new TextContent("x".repeat(400))),
			TokenUsage.EMPTY, "end_turn"));
		conv.add(new UserMessage("u3", T0, List.of(new TextContent("recent question"))));
		conv.add(new AssistantMessage("a3", T0,
			List.of(new TextContent("recent answer")),
			TokenUsage.EMPTY, "end_turn"));

		int tokensBefore = _estimator.estimateTokens(conv.messages());

		System.out.println(ANSI_MAGENTA + "\n  Before compaction:" + ANSI_RESET);
		printConversation(conv.messages());
		System.out.println(ANSI_DIM + "  Total: " + tokensBefore + " estimated tokens, "
			+ conv.size() + " messages" + ANSI_RESET);

		String mockSummary = "The user asked about a complex topic. "
			+ "I provided a detailed explanation covering the architecture, "
			+ "key files involved, and the recommended approach.";

		LLMProvider summaryProvider = mockSummaryProvider(mockSummary);
		SummarizingStrategy strategy = new SummarizingStrategy(summaryProvider, "summary-model", 4096);
		CompactionResult result = strategy.compact(conv.messages(), 250, _estimator);

		System.out.println(ANSI_MAGENTA + "\n  After summarization (target: 250 tokens):" + ANSI_RESET);
		printConversation(result.replacementMessages());
		System.out.println(ANSI_DIM + "  Tokens: " + result.tokensBefore() + " → " + result.tokensAfter()
			+ " (" + ANSI_GREEN + "-" + (result.tokensBefore() - result.tokensAfter()) + ANSI_DIM
			+ "), " + result.messagesSummarized() + " messages summarized" + ANSI_RESET);
		System.out.println();
	}

	/**
	 * Demonstrates hybrid token estimation: API baseline + heuristic for new messages.
	 */
	public void testHybridEstimation() {
		header("Hybrid Token Estimation — API baseline + heuristic delta");

		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(),
			200_000, 33_000);

		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Hello, world!"))));
		memory.add(new AssistantMessage("a1", T0,
			List.of(new TextContent("Hi there!")),
			TokenUsage.EMPTY, "end_turn"));

		int pureEstimate = memory.estimateTokens();
		System.out.println(ANSI_MAGENTA + "\n  Phase 1: Pure heuristic (no baseline)" + ANSI_RESET);
		System.out.println("    Estimated tokens: " + ANSI_YELLOW + pureEstimate + ANSI_RESET
			+ ANSI_DIM + "  (bytes/4 of all messages)" + ANSI_RESET);

		memory.updateTokenBaseline(new TokenUsage(847, 42, 0, 0, 0));

		System.out.println(ANSI_MAGENTA + "\n  Phase 2: After API response (baseline set)" + ANSI_RESET);
		System.out.println("    API said: " + ANSI_GREEN + "847" + ANSI_RESET + " input tokens");
		System.out.println("    Estimated: " + ANSI_YELLOW + memory.estimateTokens() + ANSI_RESET
			+ ANSI_DIM + "  (847 baseline + 0 new)" + ANSI_RESET);

		memory.add(new UserMessage("u2", T0,
			List.of(new TextContent("Now explain the architecture in detail."))));

		int newMsgTokens = _estimator.estimateTokens(
			new UserMessage("u2", T0, List.of(new TextContent("Now explain the architecture in detail."))));

		System.out.println(ANSI_MAGENTA + "\n  Phase 3: New message added after baseline" + ANSI_RESET);
		System.out.println("    New message estimate: " + ANSI_YELLOW + newMsgTokens + ANSI_RESET + " tokens");
		System.out.println("    Total estimate: " + ANSI_YELLOW + memory.estimateTokens() + ANSI_RESET
			+ ANSI_DIM + "  (847 baseline + " + newMsgTokens + " new = " + (847 + newMsgTokens) + ")" + ANSI_RESET);

		System.out.println(ANSI_MAGENTA + "\n  Budget:" + ANSI_RESET);
		System.out.println("    Context window:  " + ANSI_WHITE + "200,000" + ANSI_RESET);
		System.out.println("    Reserved:        " + ANSI_WHITE + " 33,000" + ANSI_RESET);
		System.out.println("    Usable:          " + ANSI_GREEN + "167,000" + ANSI_RESET);
		System.out.println("    Should compact?  " + ANSI_GREEN + "no" + ANSI_RESET
			+ ANSI_DIM + "  (" + memory.estimateTokens() + " < 167,000)" + ANSI_RESET);
		System.out.println();
	}

	/**
	 * Full end-to-end: agent loop with a tiny context window that triggers auto-compaction.
	 */
	public void testAgentLoopAutoCompaction() {
		header("Agent Loop with Auto-Compaction");

		System.out.println(ANSI_DIM + "\n  Scenario: 500-token context window, 100 reserved → 400 usable"
			+ "\n  The agent calls tools that produce large results, forcing compaction mid-run."
			+ ANSI_RESET);

		// Tiny context: 500 tokens, 100 reserved → 400 usable
		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(2),
			500, 100);

		// Seed with a user message
		memory.add(new UserMessage("u1", T0,
			List.of(new TextContent("Read 3 files and summarize what you find."))));

		ToolSchema readTool = new ToolSchema("read_file", "Read a file's contents",
			"{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}}}");

		// Mock provider: turn 1 reads 2 files, turn 2 reads 1 file, turn 3 text answer
		AtomicInteger callIdx = new AtomicInteger(0);
		LLMProvider provider = (request, handler) -> {
			int idx = callIdx.getAndIncrement();
			sleep(30);

			// Report high input tokens to simulate growing context
			TokenUsage usage = new TokenUsage(200 + idx * 150, 50, 0, 0, 0);

			switch (idx) {
				case 0 -> {
					ToolCall tc1 = new ToolCall("tc1", "read_file", "{\"path\":\"App.java\"}");
					ToolCall tc2 = new ToolCall("tc2", "read_file", "{\"path\":\"Config.xml\"}");
					handler.onEvent(new StreamEvent.ToolCallStart(0, "tc1", "read_file"));
					handler.onEvent(new StreamEvent.ToolCallDelta(0, "{\"path\":\"App.java\"}"));
					handler.onEvent(new StreamEvent.ToolCallStart(1, "tc2", "read_file"));
					handler.onEvent(new StreamEvent.ToolCallDelta(1, "{\"path\":\"Config.xml\"}"));
					handler.onEvent(new StreamEvent.MessageComplete(
						"msg-0", "mock", "tool_use", usage, List.of(tc1, tc2)));
				}
				case 1 -> {
					ToolCall tc3 = new ToolCall("tc3", "read_file", "{\"path\":\"Schema.sql\"}");
					handler.onEvent(new StreamEvent.ToolCallStart(0, "tc3", "read_file"));
					handler.onEvent(new StreamEvent.ToolCallDelta(0, "{\"path\":\"Schema.sql\"}"));
					handler.onEvent(new StreamEvent.MessageComplete(
						"msg-1", "mock", "tool_use", usage, List.of(tc3)));
				}
				default -> {
					String text = "Here's what I found across the 3 files: "
						+ "App.java contains the main entry point, "
						+ "Config.xml has the database settings, "
						+ "and Schema.sql defines the table structure.";
					handler.onEvent(new StreamEvent.TextDelta(0, text));
					handler.onEvent(new StreamEvent.MessageComplete(
						"msg-2", "mock", "end_turn", usage, List.of(new TextContent(text))));
				}
			}
		};

		// Tool registry with a read_file tool that returns sizable file contents
		com.top_logic.tl.ai.core.tool.ToolRegistry registry = new com.top_logic.tl.ai.core.tool.ToolRegistry();
		registry.register(new com.top_logic.tl.ai.core.tool.Tool() {
			@Override public String name() { return "read_file"; }
			@Override public String description() { return "Read a file's contents"; }
			@Override public com.top_logic.tl.ai.core.provider.ToolSchema schema() { return readTool; }
			@Override public String execute(java.util.Map<String, Object> args, ToolContext context) {
				sleep(50);
				String path = String.valueOf(args.getOrDefault("path", ""));
				if (path.contains("App.java")) {
					return "package com.example;\n\n" + "// Application entry point\n".repeat(20);
				} else if (path.contains("Config.xml")) {
					return "<config>\n" + "  <db host='localhost' port='5432'/>\n".repeat(15) + "</config>";
				} else {
					return "CREATE TABLE users (\n" + "  id SERIAL PRIMARY KEY,\n".repeat(10) + ");";
				}
			}
			@Override public com.top_logic.tl.ai.core.tool.ToolMode mode() {
				return com.top_logic.tl.ai.core.tool.ToolMode.READ;
			}
		});
		registry.freeze();

		AgentConfig config = new AgentConfig("mock-model",
			"You are a helpful assistant.", 10, 4096);

		AgentLoop loop = new AgentLoop(config, provider, registry, memory);
		AgentResult result = loop.run(ToolContext.EMPTY, memoryListener(memory));

		System.out.println("\n" + ANSI_CYAN + "───────────────────────────────────────────" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "  Result" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "───────────────────────────────────────────" + ANSI_RESET);
		System.out.println("  Stop reason:     " + result.stopReason());
		System.out.println("  Turns used:      " + result.turnsUsed());
		System.out.println("  Final messages:  " + memory.messages().size());
		System.out.println("  Final tokens:    ~" + memory.estimateTokens());

		System.out.println(ANSI_MAGENTA + "\n  Final conversation state:" + ANSI_RESET);
		printConversation(memory.messages());
		System.out.println();
	}

	// --- Helpers ---

	private void printEstimate(String label, Message msg) {
		int tokens = _estimator.estimateTokens(msg);
		String bar = ANSI_GREEN + "█".repeat(Math.min(tokens / 5 + 1, 40)) + ANSI_RESET;
		System.out.printf("  %-30s %s %s%d tokens%s%n",
			label, bar, ANSI_YELLOW, tokens, ANSI_RESET);
	}

	private static void printConversation(List<Message> messages) {
		for (int i = 0; i < messages.size(); i++) {
			Message msg = messages.get(i);
			String prefix = ANSI_DIM + "    [" + i + "] " + ANSI_RESET;
			switch (msg) {
				case UserMessage u -> {
					String text = textOf(u.content());
					System.out.println(prefix + ANSI_BLUE + "User: " + ANSI_RESET + truncate(text, 70));
				}
				case AssistantMessage a -> {
					String text = textOf(a.content());
					boolean hasTools = a.content().stream().anyMatch(ToolCall.class::isInstance);
					if (hasTools) {
						long toolCount = a.content().stream().filter(ToolCall.class::isInstance).count();
						System.out.println(prefix + ANSI_YELLOW + "Assistant: " + ANSI_RESET
							+ truncate(text, 40) + ANSI_DIM + " [+" + toolCount + " tool call(s)]" + ANSI_RESET);
					} else {
						System.out.println(prefix + ANSI_YELLOW + "Assistant: " + ANSI_RESET + truncate(text, 70));
					}
				}
				case ToolResultMessage tr -> {
					String result = switch (tr.state()) {
						case ToolState.Completed c -> truncate(c.result(), 50);
						case ToolState.Failed f -> "ERROR: " + f.error();
						default -> "...";
					};
					System.out.println(prefix + ANSI_GREEN + "Tool(" + tr.toolName() + "): "
						+ ANSI_RESET + result);
				}
				case SystemMessage s -> {
					System.out.println(prefix + ANSI_MAGENTA + "System: " + ANSI_RESET
						+ truncate(s.content(), 70));
				}
				case CompactionMarker m -> {
					System.out.println(prefix + ANSI_RED + ANSI_BOLD
						+ "── COMPACTION ── " + ANSI_RESET + ANSI_DIM
						+ m.messagesSummarized() + " msgs dropped, was "
						+ m.tokensBefore() + " tokens, trigger=" + m.trigger()
						+ ANSI_RESET);
				}
			}
		}
	}

	private static String textOf(List<ContentPart> parts) {
		StringBuilder sb = new StringBuilder();
		for (ContentPart part : parts) {
			if (part instanceof TextContent t) {
				sb.append(t.text());
			}
		}
		return sb.toString();
	}

	private static AgentListener memoryListener(ConversationMemory memory) {
		return new AgentListener() {
			@Override
			public void onTurnStart(int turnNumber) {
				System.out.println("\n" + ANSI_BLUE + "── Turn " + turnNumber
					+ " ──" + ANSI_RESET + ANSI_DIM + "  (~"
					+ memory.estimateTokens() + "/" + memory.usableTokens()
					+ " tokens)" + ANSI_RESET);
			}

			@Override
			public void onTurnComplete(int turnNumber, TokenUsage usage) {
				System.out.println(ANSI_DIM + "  tokens: " + usage.inputTokens() + " in / "
					+ usage.outputTokens() + " out"
					+ "  |  memory: ~" + memory.estimateTokens() + "/" + memory.usableTokens()
					+ ANSI_RESET);
			}

			@Override
			public void onTextDelta(String text) {
				System.out.print(text);
			}

			@Override
			public void onToolCallStart(ToolCall toolCall) {
				System.out.println(ANSI_YELLOW + "  ⚡ " + toolCall.name()
					+ "(" + truncate(toolCall.arguments(), 40) + ")" + ANSI_RESET);
			}

			@Override
			public void onToolCallComplete(ToolCall toolCall, ToolResultMessage result) {
				if (result.state() instanceof ToolState.Completed c) {
					System.out.println(ANSI_GREEN + "  ✓ " + toolCall.name()
						+ " → " + c.result().length() + " chars" + ANSI_RESET);
				}
			}

			@Override
			public void onToolError(ToolCall toolCall, String error) {
				System.out.println(ANSI_RED + "  ✗ " + toolCall.name()
					+ " → " + error + ANSI_RESET);
			}

			@Override
			public void onCompactionStart(int tokensBefore) {
				System.out.println("\n" + ANSI_RED + ANSI_BOLD
					+ "  ⚠ COMPACTION TRIGGERED" + ANSI_RESET
					+ ANSI_DIM + "  (tokens: " + tokensBefore
					+ " ≥ budget: " + memory.usableTokens() + ")" + ANSI_RESET);
			}

			@Override
			public void onCompactionComplete(CompactionResult result) {
				System.out.println(ANSI_GREEN + "  ✓ Compacted: "
					+ result.tokensBefore() + " → " + result.tokensAfter() + " tokens"
					+ " (" + result.messagesSummarized() + " messages dropped)"
					+ ANSI_RESET);
			}

			@Override
			public void onError(ProviderException error) {
				System.out.println(ANSI_RED + "\n  PROVIDER ERROR: " + error.kind()
					+ " — " + error.getMessage() + ANSI_RESET);
			}
		};
	}

	private static void header(String title) {
		System.out.println("\n" + ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);
		System.out.println(ANSI_CYAN + "  " + title + ANSI_RESET);
		System.out.println(ANSI_CYAN + "═══════════════════════════════════════════" + ANSI_RESET);
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

	private static LLMProvider mockSummaryProvider(String summaryText) {
		TokenUsage usage = new TokenUsage(100, 50, 0, 0, 0);
		return (request, handler) -> {
			handler.onEvent(new StreamEvent.TextDelta(0, summaryText));
			handler.onEvent(new StreamEvent.MessageComplete(
				"sum-1", "summary-model", "end_turn", usage,
				List.of(new TextContent(summaryText))));
		};
	}
}
