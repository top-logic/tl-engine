/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.tool.Tool;
import com.top_logic.tl.ai.core.tool.ToolMode;
import com.top_logic.tl.ai.core.tool.ToolRegistry;

/**
 * Tests for {@link AgentLoop}.
 */
public class TestAgentLoop extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-04T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private static final String MODEL = "test-model";

	/**
	 * Creates an AgentConfig with defaults.
	 */
	private static AgentConfig config() {
		return new AgentConfig(MODEL, "You are a test assistant.",
			AgentConfig.DEFAULT_MAX_TURNS, AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * Creates an AgentConfig with the given max turns.
	 */
	private static AgentConfig config(int maxTurns) {
		return new AgentConfig(MODEL, "You are a test assistant.",
			maxTurns, AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * Creates a frozen empty tool registry.
	 */
	private static ToolRegistry emptyRegistry() {
		ToolRegistry r = new ToolRegistry();
		r.freeze();
		return r;
	}

	/**
	 * Creates a frozen registry with a single echo tool.
	 */
	private static ToolRegistry echoRegistry(String name, ToolSchema schema) {
		ToolRegistry r = new ToolRegistry();
		r.register(readTool(name, schema));
		r.freeze();
		return r;
	}

	/**
	 * Creates a mock LLM provider that returns pre-configured responses.
	 *
	 * <p>Each call to {@link LLMProvider#stream} pops the next response from
	 * the queue and emits it as a {@link StreamEvent.MessageComplete}.</p>
	 */
	@SafeVarargs
	private static LLMProvider mockProvider(List<ContentPart>... responses) {
		AtomicInteger callIndex = new AtomicInteger(0);
		List<List<ContentPart>> responseList = List.of(responses);

		return (request, handler) -> {
			int idx = callIndex.getAndIncrement();
			if (idx >= responseList.size()) {
				throw new ProviderException(
					ProviderException.Kind.INVALID_REQUEST, 400,
					"Mock provider: no more responses (call " + idx + ")");
			}
			List<ContentPart> content = responseList.get(idx);

			// Emit text deltas for text content
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall tc) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, tc.id(), tc.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, tc.arguments()));
				}
			}

			String finishReason = content.stream().anyMatch(ToolCall.class::isInstance)
				? "tool_use" : "end_turn";

			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-" + idx, MODEL, finishReason, USAGE, content));
		};
	}

	/**
	 * LLM responds with text only, no tool calls. Loop completes in 1 turn.
	 */
	public void testSimpleCompletion() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Hi there!")));

		AgentLoop loop = new AgentLoop(config(), provider, emptyRegistry());
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(1, result.turnsUsed());
		assertEquals(USAGE, result.totalUsage());

		// Conversation should have: user + assistant
		assertEquals(2, conv.size());
	}

	/**
	 * LLM requests a tool, gets result, then responds with text. Loop runs 2 turns.
	 */
	public void testSingleToolCall() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("What is 2+2?"))));

		ToolSchema calcSpec = new ToolSchema("calculate", "Does math", "{}");
		ToolCall toolCall = new ToolCall("tc1", "calculate", "{\"expr\":\"2+2\"}");

		// Turn 1: LLM requests tool. Turn 2: LLM responds with text.
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("The answer is 4.")));

		ToolRegistry registry = echoRegistry("calculate", calcSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());

		// Conversation: user + assistant(tool_call) + tool_result + assistant(text)
		assertEquals(4, conv.size());
	}

	/**
	 * LLM requests multiple tools in one turn, all get executed.
	 */
	public void testMultipleToolCallsPerTurn() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Read two files"))));

		ToolSchema readSpec = new ToolSchema("read_file", "Reads a file", "{}");
		ToolCall tc1 = new ToolCall("tc1", "read_file", "{\"path\":\"a.txt\"}");
		ToolCall tc2 = new ToolCall("tc2", "read_file", "{\"path\":\"b.txt\"}");

		// Turn 1: LLM requests 2 tools. Turn 2: LLM responds with text.
		LLMProvider provider = mockProvider(
			List.of(tc1, tc2),
			List.of(new TextContent("Both files read.")));

		ToolRegistry registry = echoRegistry("read_file", readSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());

		// Conversation: user + assistant(2 tool calls) + 2 tool results + assistant(text)
		assertEquals(5, conv.size());
	}

	/**
	 * LLM keeps requesting tools until maxTurns is reached.
	 */
	public void testMaxTurns() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Loop forever"))));

		ToolSchema toolSpec = new ToolSchema("do_thing", "Does a thing", "{}");
		int maxTurns = 3;

		// All 3 turns return tool calls — never returns text-only.
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "do_thing", "{}")),
			List.of(new ToolCall("tc2", "do_thing", "{}")),
			List.of(new ToolCall("tc3", "do_thing", "{}")));

		ToolRegistry registry = echoRegistry("do_thing", toolSpec);

		AgentLoop loop = new AgentLoop(config(maxTurns), provider, registry);
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals(maxTurns, result.turnsUsed());
	}

	/**
	 * Provider throws on the first call. Loop returns ERROR after 1 turn.
	 */
	public void testProviderError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider failingProvider = (request, handler) -> {
			throw new ProviderException(
				ProviderException.Kind.AUTHENTICATION, 401, "Invalid API key");
		};

		AgentLoop loop = new AgentLoop(config(), failingProvider, emptyRegistry());
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.ERROR, result.stopReason());
		assertEquals(0, result.turnsUsed());
	}

	/**
	 * Tool executor returns a Failed state. Error is fed back to LLM which then completes.
	 */
	public void testToolError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Do something"))));

		ToolSchema toolSpec = new ToolSchema("risky_tool", "Might fail", "{}");
		ToolCall toolCall = new ToolCall("tc1", "risky_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("The tool failed, but I recovered.")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(new Tool() {
			@Override public String name() { return "risky_tool"; }
			@Override public String description() { return "Might fail"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public String execute(Map<String, Object> args, ToolContext context) {
				throw new RuntimeException("Something went wrong");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());

		// Conversation: user + assistant(tool_call) + tool_result(failed) + assistant(text)
		assertEquals(4, conv.size());
	}

	/**
	 * Verifies listener receives events in the correct order.
	 */
	public void testListenerEvents() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Use a tool"))));

		ToolSchema toolSpec = new ToolSchema("my_tool", "A tool", "{}");
		ToolCall toolCall = new ToolCall("tc1", "my_tool", "{\"x\":1}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done.")));

		ToolRegistry registry = echoRegistry("my_tool", toolSpec);

		List<String> events = new ArrayList<>();

		AgentListener listener = new AgentListener() {
			@Override
			public void onTurnStart(int turnNumber) {
				events.add("turn_start:" + turnNumber);
			}

			@Override
			public void onTurnComplete(int turnNumber, TokenUsage usage) {
				events.add("turn_complete:" + turnNumber);
			}

			@Override
			public void onTextDelta(String text) {
				events.add("text:" + text);
			}

			@Override
			public void onToolCallStart(ToolCall tc) {
				events.add("tool_start:" + tc.name());
			}

			@Override
			public void onToolCallComplete(ToolCall tc, ToolResultMessage result) {
				events.add("tool_complete:" + tc.name());
			}
		};

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		loop.run(conv, listener);

		assertEquals(List.of(
			"turn_start:1",
			"tool_start:my_tool",
			"tool_complete:my_tool",
			"turn_complete:1",
			"turn_start:2",
			"text:Done.",
			"turn_complete:2"
		), events);
	}

	/**
	 * Verifies that onError is called on provider failure, and turn_start/turn_complete are symmetric.
	 */
	public void testListenerOnError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider failingProvider = (request, handler) -> {
			throw new ProviderException(
				ProviderException.Kind.AUTHENTICATION, 401, "Bad key");
		};

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onTurnStart(int turnNumber) {
				events.add("turn_start:" + turnNumber);
			}

			@Override
			public void onTurnComplete(int turnNumber, TokenUsage usage) {
				events.add("turn_complete:" + turnNumber);
			}

			@Override
			public void onError(ProviderException error) {
				events.add("error:" + error.kind());
			}
		};

		AgentLoop loop = new AgentLoop(config(), failingProvider, emptyRegistry());
		loop.run(conv, listener);

		assertEquals(List.of("turn_start:1", "error:AUTHENTICATION", "turn_complete:1"), events);
	}

	/**
	 * Verifies that onToolError is called when a tool fails.
	 */
	public void testListenerOnToolError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("bad_tool", "Fails", "{}");
		ToolCall toolCall = new ToolCall("tc1", "bad_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(new Tool() {
			@Override public String name() { return "bad_tool"; }
			@Override public String description() { return "Fails"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public String execute(Map<String, Object> args, ToolContext context) {
				throw new RuntimeException("Boom");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onToolError(ToolCall tc, String error) {
				events.add("tool_error:" + tc.name() + ":" + error);
			}
		};

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		loop.run(conv, listener);

		assertEquals(1, events.size());
		assertTrue(events.get(0).startsWith("tool_error:bad_tool:"));
		assertTrue(events.get(0).contains("Boom"));
	}

	/**
	 * Token usage is accumulated across turns.
	 */
	public void testUsageAccumulation() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("tool", "T", "{}");
		ToolCall toolCall = new ToolCall("tc1", "tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done")));

		ToolRegistry registry = echoRegistry("tool", toolSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, new AgentListener() {});

		// 2 turns, each with USAGE (100 input, 50 output)
		assertEquals(200, result.totalUsage().inputTokens());
		assertEquals(100, result.totalUsage().outputTokens());
	}

	/**
	 * Thread interrupted before first turn — loop returns ABORTED immediately.
	 */
	public void testCancellationBeforeTurn() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Hi")));

		Thread.currentThread().interrupt();
		AgentLoop loop = new AgentLoop(config(), provider, emptyRegistry());
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.ABORTED, result.stopReason());
		assertEquals(0, result.turnsUsed());
		// Clear interrupt flag for subsequent tests
		Thread.interrupted();
	}

	/**
	 * Thread interrupted between turns — loop returns ABORTED.
	 *
	 * <p>Sets the interrupt flag during tool execution. The loop detects it
	 * at the start of the next turn iteration and returns ABORTED.</p>
	 */
	public void testCancellationDuringToolExecution() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("tool", "T", "{}");
		ToolCall tc1 = new ToolCall("tc1", "tool", "{}");

		// Two responses: first returns tool call, second would return text
		LLMProvider provider = mockProvider(
			List.of(tc1),
			List.of(new TextContent("Done")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(new Tool() {
			@Override public String name() { return "tool"; }
			@Override public String description() { return "T"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public String execute(Map<String, Object> args, ToolContext context) {
				Thread.currentThread().interrupt();
				return "ok";
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.ABORTED, result.stopReason());
		// Clear interrupt flag
		Thread.interrupted();
	}

	/**
	 * Agent loop with memory triggers compaction when token budget is exceeded.
	 */
	public void testMemoryAutoCompaction() {
		// Small context window: 300 tokens, 50 reserved -> 250 usable
		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(2),
			300, 50);

		// Add a user message with large content to push close to budget
		memory.add(new UserMessage("u1", T0,
			List.of(new TextContent("x".repeat(800))))); // 200 tokens

		ToolSchema toolSpec = new ToolSchema("tool", "T", "{}");
		ToolCall toolCall = new ToolCall("tc1", "tool", "{}");

		// Provider that reports high input token usage to trigger compaction
		AtomicInteger providerCalls = new AtomicInteger(0);
		LLMProvider highUsageProvider = (request, handler) -> {
			int idx = providerCalls.getAndIncrement();
			TokenUsage highUsage = new TokenUsage(250, 50, 0, 0, 0);
			if (idx == 0) {
				handler.onEvent(new StreamEvent.ToolCallStart(0, "tc1", "tool"));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, "{}"));
				handler.onEvent(new StreamEvent.MessageComplete(
					"msg-0", MODEL, "tool_use", highUsage, List.of(toolCall)));
			} else {
				handler.onEvent(new StreamEvent.TextDelta(0, "Done"));
				handler.onEvent(new StreamEvent.MessageComplete(
					"msg-1", MODEL, "end_turn", highUsage,
					List.of(new TextContent("Done"))));
			}
		};

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onCompactionStart(int tokensBefore) {
				events.add("compaction_start:" + tokensBefore);
			}

			@Override
			public void onCompactionComplete(CompactionResult result) {
				events.add("compaction_complete:" + result.messagesSummarized());
			}
		};

		ToolRegistry registry = echoRegistry("tool", toolSpec);

		AgentLoop loop = new AgentLoop(config(10), highUsageProvider,
			registry, memory);
		AgentResult result = loop.run(ToolContext.EMPTY, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Compaction should have been triggered",
			events.stream().anyMatch(e -> e.startsWith("compaction_start")));
	}

	/**
	 * Agent loop without memory (existing constructor) does not trigger compaction.
	 */
	public void testNoMemoryNoCompaction() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Hi")));

		AgentLoop loop = new AgentLoop(config(), provider, emptyRegistry());
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());
	}

	/**
	 * run(ToolContext, AgentListener) throws when no memory is configured.
	 */
	public void testRunWithoutMemoryThrows() {
		AgentLoop loop = new AgentLoop(config(), mockProvider(List.of(new TextContent("Hi"))), emptyRegistry());
		try {
			loop.run(ToolContext.EMPTY, new AgentListener() {});
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	private static AgentListener listener() {
		return new AgentListener() {};
	}

	/**
	 * GLOBAL tool in PRODUCTION mode is denied by system mode gate.
	 */
	public void testSystemModeGateDeniesGlobalInProduction() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Create a class"))));

		ToolSchema toolSpec = new ToolSchema("create_class", "Creates a class", "{}");
		ToolCall toolCall = new ToolCall("tc1", "create_class", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("I cannot create classes in production mode.")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(new Tool() {
			@Override public String name() { return "create_class"; }
			@Override public String description() { return "Creates a class"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public String execute(Map<String, Object> args, ToolContext context) {
				fail("Tool should not execute in production mode");
				return "";
			}
			@Override public ToolMode mode() { return ToolMode.GLOBAL; }
		});
		registry.freeze();

		ToolContext ctx = new ToolContext(productionSecurity(), ApprovalCallback.AUTO_APPROVE);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());

		assertTrue("Conversation should contain mode denial",
			conv.messages().stream()
				.filter(ToolResultMessage.class::isInstance)
				.map(ToolResultMessage.class::cast)
				.anyMatch(tr -> tr.state() instanceof ToolState.Failed f
					&& f.error().contains("maintenance or development mode")));
	}

	/**
	 * GLOBAL tool in DEVELOPMENT mode passes system gate. With auto-approve, tool executes.
	 */
	public void testSystemModeGateAllowsGlobalInDevelopment() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Create a class"))));

		ToolSchema toolSpec = new ToolSchema("create_class", "Creates a class", "{}");
		ToolCall toolCall = new ToolCall("tc1", "create_class", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Class created.")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("create_class", toolSpec));
		registry.freeze();

		SecurityContext devSecurity = new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
			@Override public PermissionDecision checkApproval(Tool tool, ToolCall tc) {
				return new PermissionDecision.Allowed();
			}
		};

		ToolContext ctx = new ToolContext(devSecurity, ApprovalCallback.AUTO_APPROVE);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());
	}

	/**
	 * READ tool in PRODUCTION mode passes system gate without approval.
	 */
	public void testSystemModeGateAllowsReadInProduction() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Search"))));

		ToolSchema toolSpec = new ToolSchema("search", "Searches", "{}");
		ToolCall toolCall = new ToolCall("tc1", "search", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Found results.")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(readTool("search", toolSpec));
		registry.freeze();

		ToolContext ctx = new ToolContext(productionSecurity(), ApprovalCallback.AUTO_APPROVE);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());
	}

	/**
	 * User rejects a tool via ApprovalCallback. LLM gets rejection with user comment.
	 */
	public void testUserRejectsToolCall() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit something"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits model", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit_model", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK, I won't edit.")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		ApprovalCallback rejectingCallback = request ->
			new ApprovalResult.Rejected("I don't want to edit the model right now.");

		ToolContext ctx = new ToolContext(developmentSecurity(), rejectingCallback);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());

		assertTrue("Conversation should contain user rejection",
			conv.messages().stream()
				.filter(ToolResultMessage.class::isInstance)
				.map(ToolResultMessage.class::cast)
				.anyMatch(tr -> tr.state() instanceof ToolState.Failed f
					&& f.error().contains("User denied")
					&& f.error().contains("I don't want to edit the model right now")));
	}

	/**
	 * User rejects without comment. LLM gets generic rejection message.
	 */
	public void testUserRejectsWithoutComment() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit something"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits model", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit_model", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK.")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		ApprovalCallback rejectingCallback = request -> new ApprovalResult.Rejected("");

		ToolContext ctx = new ToolContext(developmentSecurity(), rejectingCallback);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());

		assertTrue("Conversation should contain generic rejection",
			conv.messages().stream()
				.filter(ToolResultMessage.class::isInstance)
				.map(ToolResultMessage.class::cast)
				.anyMatch(tr -> tr.state() instanceof ToolState.Failed f
					&& f.error().contains("User denied tool 'edit_model'")));
	}

	/**
	 * ApprovedForSession skips approval on subsequent calls to the same tool.
	 */
	public void testApprovedForSessionSkipsSubsequentApproval() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit twice"))));

		ToolSchema toolSpec = new ToolSchema("edit", "Edits", "{}");
		ToolCall tc1 = new ToolCall("tc1", "edit", "{}");
		ToolCall tc2 = new ToolCall("tc2", "edit", "{}");

		LLMProvider provider = mockProvider(
			List.of(tc1),
			List.of(tc2),
			List.of(new TextContent("Done")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		AtomicInteger approvalCount = new AtomicInteger(0);
		ApprovalCallback sessionApprover = request -> {
			approvalCount.incrementAndGet();
			return new ApprovalResult.ApprovedForSession();
		};

		ToolContext ctx = new ToolContext(developmentSecurity(), sessionApprover);
		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("Approval should only be requested once — second call auto-approved",
			1, approvalCount.get());
	}

	/**
	 * TimedOut approval is treated as rejection.
	 */
	public void testApprovalTimedOutTreatedAsRejection() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		ApprovalCallback timeoutCallback = request -> new ApprovalResult.TimedOut();

		ToolContext ctx = new ToolContext(developmentSecurity(), timeoutCallback);
		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Conversation should contain timeout message",
			conv.messages().stream()
				.filter(ToolResultMessage.class::isInstance)
				.map(ToolResultMessage.class::cast)
				.anyMatch(tr -> tr.state() instanceof ToolState.Failed f
					&& f.error().contains("timed out")));
	}

	/**
	 * ApprovalRequest contains the actual tool arguments, not an empty map.
	 */
	public void testApprovalRequestContainsArguments() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("edit", "Edits",
			"{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}");
		ToolCall toolCall = new ToolCall("tc1", "edit", "{\"name\":\"Customer\"}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		List<ApprovalRequest> capturedRequests = new ArrayList<>();
		ApprovalCallback capturingCallback = request -> {
			capturedRequests.add(request);
			return new ApprovalResult.Approved();
		};

		ToolContext ctx = new ToolContext(developmentSecurity(), capturingCallback);
		AgentLoop loop = new AgentLoop(config(), provider, registry);
		loop.run(conv, ctx, listener());

		assertEquals(1, capturedRequests.size());
		ApprovalRequest req = capturedRequests.get(0);
		assertEquals("Customer", req.arguments().get("name"));
		assertNotNull(req.timeout());
	}

	private static Tool globalTool(String name, ToolSchema schema) {
		return new Tool() {
			@Override public String name() { return name; }
			@Override public String description() { return ""; }
			@Override public ToolSchema schema() { return schema; }
			@Override public String execute(Map<String, Object> args, ToolContext context) {
				return "Result of " + name;
			}
			@Override public ToolMode mode() { return ToolMode.GLOBAL; }
		};
	}

	private static Tool readTool(String name, ToolSchema schema) {
		return new Tool() {
			@Override public String name() { return name; }
			@Override public String description() { return ""; }
			@Override public ToolSchema schema() { return schema; }
			@Override public String execute(Map<String, Object> args, ToolContext context) {
				return "Result of " + name;
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
	}

	private static SecurityContext productionSecurity() {
		return new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.PRODUCTION; }
		};
	}

	/**
	 * Listener receives onPermissionDenied when system mode gate blocks a GLOBAL tool.
	 */
	public void testListenerOnPermissionDenied() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onPermissionDenied(ToolCall tc, String reason) {
				events.add("denied:" + tc.name());
			}
		};

		ToolContext ctx = new ToolContext(productionSecurity(), ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		loop.run(conv, ctx, listener);

		assertEquals(List.of("denied:edit"), events);
	}

	/**
	 * Listener receives onApprovalRequired when a tool needs user approval.
	 */
	public void testListenerOnApprovalRequired() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done")));

		ToolRegistry registry = new ToolRegistry();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onApprovalRequired(ToolCall tc, ApprovalRequest request) {
				events.add("approval:" + tc.name());
			}
		};

		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		loop.run(conv, ctx, listener);

		assertEquals(List.of("approval:edit"), events);
	}

	/**
	 * hasToolPermission(String) is checked for tools not found in the registry.
	 */
	public void testPermissionCheckForUnknownTool() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolCall toolCall = new ToolCall("tc1", "external_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		SecurityContext denyAll = new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public boolean hasToolPermission(String toolName) { return false; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onPermissionDenied(ToolCall tc, String reason) {
				events.add("denied:" + tc.name());
			}
		};

		ToolContext ctx = new ToolContext(denyAll, ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(10), provider, emptyRegistry());
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(List.of("denied:external_tool"), events);
	}

	/**
	 * In PRODUCTION mode, GLOBAL tool specs are not sent to the LLM at all.
	 */
	public void testToolSpecsFilteredBySecurityContext() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		ToolRegistry registry = new ToolRegistry();
		registry.register(readTool("search", new ToolSchema("search", "Searches", "{}")));
		registry.register(globalTool("edit", new ToolSchema("edit", "Edits", "{}")));
		registry.freeze();

		List<ProviderRequest> capturedRequests = new ArrayList<>();
		LLMProvider capturingProvider = (request, handler) -> {
			capturedRequests.add(request);
			handler.onEvent(new StreamEvent.TextDelta(0, "Hi"));
			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-0", MODEL, "end_turn", USAGE, List.of(new TextContent("Hi"))));
		};

		ToolContext ctx = new ToolContext(productionSecurity(), ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(), capturingProvider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(1, capturedRequests.size());
		List<ToolSchema> sentTools = capturedRequests.get(0).tools();
		assertEquals("Only READ tool should be sent", 1, sentTools.size());
		assertEquals("search", sentTools.get(0).name());
	}

	private static SecurityContext developmentSecurity() {
		return new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};
	}
}
