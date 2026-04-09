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
import com.top_logic.tl.ai.core.provider.StreamHandler;
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
	 * Creates an AgentConfig with no tools.
	 */
	private static AgentConfig config() {
		return new AgentConfig(MODEL, "You are a test assistant.", List.of(),
			AgentConfig.DEFAULT_MAX_TURNS, AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * Creates an AgentConfig with the given tools and max turns.
	 */
	private static AgentConfig config(List<ToolSchema> tools, int maxTurns) {
		return new AgentConfig(MODEL, "You are a test assistant.", tools,
			maxTurns, AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS);
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
	 * Creates a tool executor that returns a successful result with the given text.
	 */
	private static ToolExecutor echoExecutor() {
		return (toolCall, context) -> new ToolResultMessage(
			"tr-" + toolCall.id(), Instant.now(),
			toolCall.id(), toolCall.name(),
			new ToolState.Completed("Result of " + toolCall.name(),
				Instant.now(), Instant.now(), false));
	}

	/**
	 * LLM responds with text only, no tool calls. Loop completes in 1 turn.
	 */
	public void testSimpleCompletion() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Hi there!")));

		AgentLoop loop = new AgentLoop(config(), provider, echoExecutor());
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

		ToolSchema calcTool = new ToolSchema("calculate", "Does math", "{}");
		ToolCall toolCall = new ToolCall("tc1", "calculate", "{\"expr\":\"2+2\"}");

		// Turn 1: LLM requests tool. Turn 2: LLM responds with text.
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("The answer is 4.")));

		AgentLoop loop = new AgentLoop(config(List.of(calcTool), 10), provider, echoExecutor());
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

		ToolSchema readTool = new ToolSchema("read_file", "Reads a file", "{}");
		ToolCall tc1 = new ToolCall("tc1", "read_file", "{\"path\":\"a.txt\"}");
		ToolCall tc2 = new ToolCall("tc2", "read_file", "{\"path\":\"b.txt\"}");

		// Turn 1: LLM requests 2 tools. Turn 2: LLM responds with text.
		LLMProvider provider = mockProvider(
			List.of(tc1, tc2),
			List.of(new TextContent("Both files read.")));

		AgentLoop loop = new AgentLoop(config(List.of(readTool), 10), provider, echoExecutor());
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

		ToolSchema tool = new ToolSchema("do_thing", "Does a thing", "{}");
		int maxTurns = 3;

		// All 3 turns return tool calls — never returns text-only.
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "do_thing", "{}")),
			List.of(new ToolCall("tc2", "do_thing", "{}")),
			List.of(new ToolCall("tc3", "do_thing", "{}")));

		AgentLoop loop = new AgentLoop(config(List.of(tool), maxTurns), provider, echoExecutor());
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

		AgentLoop loop = new AgentLoop(config(), failingProvider, echoExecutor());
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

		ToolSchema tool = new ToolSchema("risky_tool", "Might fail", "{}");
		ToolCall toolCall = new ToolCall("tc1", "risky_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("The tool failed, but I recovered.")));

		ToolExecutor failingExecutor = (tc, ctx) -> new ToolResultMessage(
			"tr-" + tc.id(), Instant.now(),
			tc.id(), tc.name(),
			new ToolState.Failed("Something went wrong", Instant.now(), Instant.now()));

		AgentLoop loop = new AgentLoop(config(List.of(tool), 10), provider, failingExecutor);
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

		ToolSchema tool = new ToolSchema("my_tool", "A tool", "{}");
		ToolCall toolCall = new ToolCall("tc1", "my_tool", "{\"x\":1}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done.")));

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

		AgentLoop loop = new AgentLoop(config(List.of(tool), 10), provider, echoExecutor());
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

		AgentLoop loop = new AgentLoop(config(), failingProvider, echoExecutor());
		loop.run(conv, listener);

		assertEquals(List.of("turn_start:1", "error:AUTHENTICATION", "turn_complete:1"), events);
	}

	/**
	 * Verifies that onToolError is called when a tool fails.
	 */
	public void testListenerOnToolError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema tool = new ToolSchema("bad_tool", "Fails", "{}");
		ToolCall toolCall = new ToolCall("tc1", "bad_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		ToolExecutor failingExecutor = (tc, ctx) -> new ToolResultMessage(
			"tr-" + tc.id(), Instant.now(),
			tc.id(), tc.name(),
			new ToolState.Failed("Boom", Instant.now(), Instant.now()));

		List<String> events = new ArrayList<>();
		AgentListener listener = new AgentListener() {
			@Override
			public void onToolError(ToolCall tc, String error) {
				events.add("tool_error:" + tc.name() + ":" + error);
			}
		};

		AgentLoop loop = new AgentLoop(config(List.of(tool), 10), provider, failingExecutor);
		loop.run(conv, listener);

		assertEquals(List.of("tool_error:bad_tool:Boom"), events);
	}

	/**
	 * Token usage is accumulated across turns.
	 */
	public void testUsageAccumulation() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema tool = new ToolSchema("tool", "T", "{}");
		ToolCall toolCall = new ToolCall("tc1", "tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done")));

		AgentLoop loop = new AgentLoop(config(List.of(tool), 10), provider, echoExecutor());
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
		AgentLoop loop = new AgentLoop(config(), provider, echoExecutor());
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.ABORTED, result.stopReason());
		assertEquals(0, result.turnsUsed());
		// Clear interrupt flag for subsequent tests
		Thread.interrupted();
	}

	/**
	 * Thread interrupted between tool calls — loop returns ABORTED.
	 */
	public void testCancellationDuringToolExecution() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema tool = new ToolSchema("tool", "T", "{}");
		ToolCall tc1 = new ToolCall("tc1", "tool", "{}");
		ToolCall tc2 = new ToolCall("tc2", "tool", "{}");

		LLMProvider provider = mockProvider(List.of(tc1, tc2));

		ToolExecutor interruptingExecutor = (tc, ctx) -> {
			if ("tc1".equals(tc.id())) {
				Thread.currentThread().interrupt();
			}
			return new ToolResultMessage(
				"tr-" + tc.id(), Instant.now(),
				tc.id(), tc.name(),
				new ToolState.Completed("ok", Instant.now(), Instant.now(), false));
		};

		AgentLoop loop = new AgentLoop(config(List.of(tool), 10), provider, interruptingExecutor);
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

		ToolSchema tool = new ToolSchema("tool", "T", "{}");
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

		AgentLoop loop = new AgentLoop(config(List.of(tool), 10), highUsageProvider,
			echoExecutor(), memory);
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

		AgentLoop loop = new AgentLoop(config(), provider, echoExecutor());
		AgentResult result = loop.run(conv, new AgentListener() {});

		assertEquals(StopReason.COMPLETE, result.stopReason());
	}

	/**
	 * run(ToolContext, AgentListener) throws when no memory is configured.
	 */
	public void testRunWithoutMemoryThrows() {
		AgentLoop loop = new AgentLoop(config(), mockProvider(List.of(new TextContent("Hi"))), echoExecutor());
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

		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
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

		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
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

		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
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

		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
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

		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());

		assertTrue("Conversation should contain generic rejection",
			conv.messages().stream()
				.filter(ToolResultMessage.class::isInstance)
				.map(ToolResultMessage.class::cast)
				.anyMatch(tr -> tr.state() instanceof ToolState.Failed f
					&& f.error().contains("User denied tool 'edit_model'")));
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
		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
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
		AgentLoop loop = new AgentLoop(config(List.of(toolSpec), 10), provider, registry);
		loop.run(conv, ctx, listener);

		assertEquals(List.of("approval:edit"), events);
	}

	private static SecurityContext developmentSecurity() {
		return new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};
	}
}
