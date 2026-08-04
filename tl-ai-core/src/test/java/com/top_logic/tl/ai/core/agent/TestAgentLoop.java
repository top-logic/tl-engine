/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventAssertions;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.memory.CompactionStrategy;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests for {@link AgentLoop}.
 */
public class TestAgentLoop extends TestCase {

	/**
	 * Test suite that boots the {@link com.top_logic.basic.thread.ThreadContextManager} module.
	 *
	 * <p>
	 * {@link AgentLoop} executes tool calls through
	 * {@link com.top_logic.tl.ai.core.tools.ToolExecutions#execute} (and, for concurrent batches,
	 * {@link com.top_logic.basic.thread.ThreadContextManager#inSystemInteraction}), which establishes
	 * a TopLogic interaction context so tools may access the {@code KnowledgeBase}. That requires the
	 * {@code ThreadContextManager} module to be started; the plain {@code TestCase} runner is not
	 * enough.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAgentLoop.class)));
	}

	private static final Instant T0 = Instant.parse("2026-04-04T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private static final String MODEL = "test-model";

	/**
	 * Creates an AgentParams with defaults.
	 */
	private static AgentParams config() {
		return new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * Creates an AgentParams with the given max turns.
	 */
	private static AgentParams config(int maxTurns) {
		return new AgentParams(MODEL, "You are a test assistant.",
			maxTurns, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/**
	 * Creates a frozen empty tool registry.
	 */
	private static ToolCatalog emptyRegistry() {
		ToolCatalog r = new ToolCatalog();
		r.freeze();
		return r;
	}

	/**
	 * Creates a frozen registry with a single echo tool.
	 */
	private static ToolCatalog echoRegistry(String name, ToolSchema schema) {
		ToolCatalog r = new ToolCatalog();
		r.register(readTool(name, schema));
		r.freeze();
		return r;
	}

	/**
	 * Creates a mock LLM provider that returns pre-configured responses.
	 *
	 * <p>
	 * Each call to {@link LLMProvider#stream} pops the next response from the queue and emits it as
	 * a {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete}.
	 * </p>
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
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

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

		ToolCatalog registry = echoRegistry("calculate", calcSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, result.turnsUsed());

		// Conversation: user + assistant(tool_call) + tool_result + assistant(text)
		assertEquals(4, conv.size());
	}

	/**
	 * A distinctive marker planted in the middle of an oversized result. The loop truncates by
	 * keeping head + tail and dropping the middle, so the marker's survival tells head-tail
	 * truncation from a whole result far more reliably than a length comparison alone.
	 */
	private static final String MIDDLE_MARKER = "CLICKABLE_REF_s1e40";

	/** An oversized result (~4 KB) with {@link #MIDDLE_MARKER} exactly in the middle. */
	private static final String OVERSIZED_RESULT =
		"A".repeat(2000) + MIDDLE_MARKER + "B".repeat(2000);

	/** Small enough that {@link #OVERSIZED_RESULT} is well over the loop's global cap. */
	private static final int SMALL_RESULT_CAP = 1000;

	/**
	 * A result carrying {@code truncationHint = Integer.MAX_VALUE} (as the browser page verbs do)
	 * must pass through the loop untouched even when it is far larger than the global cap.
	 */
	public void testMaxTruncationHintExemptsResultFromLoopTruncation() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Read the page."))));

		ToolCall toolCall = new ToolCall("tc1", "read_page", "{}");
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(resultTool("read_page", OVERSIZED_RESULT, Integer.MAX_VALUE));
		registry.freeze();

		List<AgentEvent> events = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(10), provider, registry, SMALL_RESULT_CAP);
		loop.run(conv, events::add);

		ToolState.Completed completed = firstToolResult(conv);
		assertEquals("browser-sized result must survive whole", OVERSIZED_RESULT, completed.result());
		assertTrue("the middle (where refs live) must be preserved",
			completed.result().contains(MIDDLE_MARKER));
		assertEquals("the exemption hint is preserved on the recorded result",
			Integer.MAX_VALUE, completed.truncationHint());
		assertFalse("no truncation event for an exempt result",
			events.stream().anyMatch(AgentEvent.ToolCallResultTruncated.class::isInstance));
	}

	/**
	 * A normal result (no hint) of the very same size is still truncated by the loop: head + tail
	 * kept, middle dropped. Proves the exemption above is the hint's doing, not the size.
	 */
	public void testResultWithoutHintIsTruncatedInTheMiddle() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Read the page."))));

		ToolCall toolCall = new ToolCall("tc1", "read_page", "{}");
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("done")));

		ToolCatalog registry = new ToolCatalog();
		// truncationHint -1 -> the global cap applies, exactly like a non-browser tool.
		registry.register(resultTool("read_page", OVERSIZED_RESULT, -1));
		registry.freeze();

		List<AgentEvent> events = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(10), provider, registry, SMALL_RESULT_CAP);
		loop.run(conv, events::add);

		ToolState.Completed completed = firstToolResult(conv);
		assertTrue("result must be shortened", completed.result().length() < OVERSIZED_RESULT.length());
		assertFalse("the middle must be dropped", completed.result().contains(MIDDLE_MARKER));
		assertTrue("truncation notice present", completed.result().contains("truncated"));
		assertTrue("a truncation event is emitted",
			events.stream().anyMatch(AgentEvent.ToolCallResultTruncated.class::isInstance));
	}

	/** Extracts the single recorded tool result from a completed conversation. */
	private static ToolState.Completed firstToolResult(Conversation conv) {
		ToolResultMessage message = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("no tool result recorded"));
		assertTrue("tool result completed", message.state() instanceof ToolState.Completed);
		return (ToolState.Completed) message.state();
	}

	/** A read tool returning a fixed text with a fixed {@code truncationHint}. */
	private static Tool resultTool(String name, String text, int truncationHint) {
		ToolSchema schema = new ToolSchema(name, "Returns a fixed result", "{}");
		return new Tool() {
			@Override public String name() { return name; }
			@Override public String description() { return ""; }
			@Override public ToolSchema schema() { return schema; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return new ToolResult(text, null, truncationHint);
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		};
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

		ToolCatalog registry = echoRegistry("read_file", readSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

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

		ToolCatalog registry = echoRegistry("do_thing", toolSpec);

		AgentLoop loop = new AgentLoop(config(maxTurns), provider, registry);
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

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
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(StopReason.ERROR, result.stopReason());
		assertEquals(1, result.turnsUsed());
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

		ToolCatalog registry = new ToolCatalog();
		registry.register(new Tool() {
			@Override public String name() { return "risky_tool"; }
			@Override public String description() { return "Might fail"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
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

		ToolCatalog registry = echoRegistry("my_tool", toolSpec);

		List<String> events = new ArrayList<>();

		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TurnStarted t) {
				events.add("turn_start:" + t.turnNumber());
			} else if (e instanceof AgentEvent.TurnCompleted t) {
				events.add("turn_complete:" + t.turnNumber());
			} else if (e instanceof AgentEvent.TextDelta d) {
				events.add("text:" + d.text());
			} else if (e instanceof AgentEvent.ToolCallStarted s) {
				events.add("tool_start:" + s.toolName());
			} else if (e instanceof AgentEvent.ToolCallCompleted c) {
				events.add("tool_complete:" + c.toolName());
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
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TurnStarted t) {
				events.add("turn_start:" + t.turnNumber());
			} else if (e instanceof AgentEvent.TurnCompleted t) {
				events.add("turn_complete:" + t.turnNumber());
			} else if (e instanceof AgentEvent.ProviderError p) {
				events.add("error:" + p.kind());
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

		ToolCatalog registry = new ToolCatalog();
		registry.register(new Tool() {
			@Override public String name() { return "bad_tool"; }
			@Override public String description() { return "Fails"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				throw new RuntimeException("Boom");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.ToolCallFailed f) {
				events.add("tool_error:" + f.toolName() + ":" + f.errorMessage());
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

		ToolCatalog registry = echoRegistry("tool", toolSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

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
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

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

		ToolCatalog registry = new ToolCatalog();
		registry.register(new Tool() {
			@Override public String name() { return "tool"; }
			@Override public String description() { return "T"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				Thread.currentThread().interrupt();
				return ToolResult.of("ok");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

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
		List<String> startedTriggers = new ArrayList<>();
		List<String> completedTriggers = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.CompactionStarted cs) {
				events.add("compaction_start:" + cs.tokensBefore());
				startedTriggers.add(cs.trigger());
			} else if (e instanceof AgentEvent.CompactionCompleted cc) {
				events.add("compaction_complete:" + cc.result().messagesSummarized());
				completedTriggers.add(cc.result().trigger());
			}
		};

		ToolCatalog registry = echoRegistry("tool", toolSpec);

		AgentLoop loop = new AgentLoop(config(10), highUsageProvider,
			registry, memory);
		AgentResult result = loop.run(ToolContext.EMPTY, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Compaction should have been triggered",
			events.stream().anyMatch(e -> e.startsWith("compaction_start")));
		// CompactionResult.trigger() must match what CompactionStarted already reported —
		// nothing downstream reads CompactionResult.trigger() directly, but it must not
		// silently disagree with the "proactive"/"reactive" label the UI actually shows.
		assertEquals(startedTriggers, completedTriggers);
		assertTrue("Proactive compaction's result must be labeled \"proactive\", not the "
			+ "ConversationMemory.compact() default of \"auto\"",
			completedTriggers.stream().allMatch("proactive"::equals));
	}

	/**
	 * Agent loop without memory (existing constructor) does not trigger compaction.
	 */
	public void testNoMemoryNoCompaction() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Hi")));

		AgentLoop loop = new AgentLoop(config(), provider, emptyRegistry());
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(StopReason.COMPLETE, result.stopReason());
	}

	/**
	 * run(ToolContext, AgentEventListener) throws when no memory is configured.
	 */
	public void testRunWithoutMemoryThrows() {
		AgentLoop loop = new AgentLoop(config(), mockProvider(List.of(new TextContent("Hi"))), emptyRegistry());
		try {
			loop.run(ToolContext.EMPTY, (AgentEventListener) e -> {
				/** empty listener */
			});
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	private static AgentEventListener listener() {
		return (AgentEventListener) e -> {
			/** empty listener */
		};
	}

	/**
	 * STRUCTURAL tool in PRODUCTION mode is denied by system mode gate.
	 */
	public void testSystemModeGateDeniesGlobalInProduction() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Create a class"))));

		ToolSchema toolSpec = new ToolSchema("create_class", "Creates a class", "{}");
		ToolCall toolCall = new ToolCall("tc1", "create_class", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("I cannot create classes in production mode.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(new Tool() {
			@Override public String name() { return "create_class"; }
			@Override public String description() { return "Creates a class"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				fail("Tool should not execute in production mode");
				return ToolResult.of("");
			}
			@Override public ToolMode mode() { return ToolMode.STRUCTURAL; }
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
	 * STRUCTURAL tool in DEVELOPMENT mode passes system gate. With auto-approve, tool executes.
	 */
	public void testSystemModeGateAllowsGlobalInDevelopment() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Create a class"))));

		ToolSchema toolSpec = new ToolSchema("create_class", "Creates a class", "{}");
		ToolCall toolCall = new ToolCall("tc1", "create_class", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Class created.")));

		ToolCatalog registry = new ToolCatalog();
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

		ToolCatalog registry = new ToolCatalog();
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

		ToolCatalog registry = new ToolCatalog();
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

		ToolCatalog registry = new ToolCatalog();
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

		ToolCatalog registry = new ToolCatalog();
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

		ToolCatalog registry = new ToolCatalog();
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

		ToolCatalog registry = new ToolCatalog();
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
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("Result of " + name);
			}
			@Override public ToolMode mode() { return ToolMode.STRUCTURAL; }
		};
	}

	private static Tool readTool(String name, ToolSchema schema) {
		return new Tool() {
			@Override public String name() { return name; }
			@Override public String description() { return ""; }
			@Override public ToolSchema schema() { return schema; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("Result of " + name);
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
	 * TransactionScope that tracks state without needing a real KB.
	 */
	private static class TestableTransactionScope extends TransactionScope {

		private boolean _active;

		private boolean _locked;

		/** */
		@Override
		public void acquireLock(String operation, com.top_logic.model.TLObject... targets) {
			// no-op for tests — no LockService available
		}

		/** */
		@Override
		public void releaseLock() {
			_locked = false;
		}

		/** */
		@Override
		public boolean holdsLock() {
			return _locked;
		}

		/** */
		@Override
		public void begin() {
			_active = true;
		}

		/** */
		@Override
		public void begin(com.top_logic.basic.util.ResKey commitMessage) {
			_active = true;
		}

		/** */
		@Override
		public void commit() {
			_active = false;
			releaseLock();
		}

		/** */
		@Override
		public void rollback() {
			_active = false;
			releaseLock();
		}

		/** */
		@Override
		public boolean isActive() {
			return _active;
		}

		/** */
		@Override
		public boolean isNoOp() {
			return false;
		}

		/** */
		@Override
		public void close() {
			rollback();
		}
	}

	/**
	 * Listener receives onPermissionDenied when system mode gate blocks a STRUCTURAL tool.
	 */
	public void testListenerOnPermissionDenied() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.ToolCallDenied d) {
				events.add("denied:" + d.toolName());
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

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.ApprovalRequired a) {
				events.add("approval:" + a.toolName());
			}
		};

		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		loop.run(conv, ctx, listener);

		assertEquals(List.of("approval:edit"), events);
	}

	/**
	 * A tool name the registry cannot resolve is "no such tool", never a permission problem.
	 *
	 * <p>
	 * The name has no tool group by construction, so asking the permission check about it would
	 * answer every hallucinated or misspelled name with "belongs to no tool group ... denied to
	 * everyone" and take away the hint the model uses to correct itself. Nothing is lost by not
	 * asking: a name the registry does not resolve cannot execute either way.
	 * </p>
	 */
	public void testUnknownToolIsNotReportedAsDenied() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolCall toolCall = new ToolCall("tc1", "external_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("OK")));

		SecurityContext denyByName = new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public boolean hasToolPermission(String toolName) { return false; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};

		List<String> denials = new ArrayList<>();
		List<String> results = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.ToolCallDenied d) {
				denials.add("denied:" + d.toolName());
			}
		};

		ToolContext ctx = new ToolContext(denyByName, ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(10), provider, emptyRegistry());
		AgentResult result = loop.run(conv, ctx, listener);

		for (Message message : conv.messages()) {
			if (message instanceof ToolResultMessage toolResult
					&& toolResult.state() instanceof ToolState.Failed failed) {
				results.add(failed.error());
			}
		}

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("An unresolvable name is not a denial.", List.of(), denials);
		assertEquals(1, results.size());
		assertTrue("Should say the tool does not exist, got: " + results.get(0),
			results.get(0).contains("Unknown tool"));
		assertFalse("Should not be phrased as a permission problem, got: " + results.get(0),
			results.get(0).contains("Permission denied"));
	}

	/**
	 * In PRODUCTION mode, STRUCTURAL tool specs are not sent to the LLM at all.
	 */
	public void testToolSpecsFilteredBySecurityContext() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		ToolCatalog registry = new ToolCatalog();
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

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(1, capturedRequests.size());
		List<ToolSchema> sentTools = capturedRequests.get(0).tools();
		assertEquals("Only READ tool should be sent", 1, sentTools.size());
		assertEquals("search", sentTools.get(0).name());
	}

	/**
	 * Provider always throws CONTEXT_WINDOW_EXCEEDED. With a no-op compaction strategy
	 * (returns messages unchanged), reactive retries exhaust and runWithMemory returns
	 * CONTEXT_OVERFLOW instead of ERROR.
	 */
	public void testContextOverflowAfterCompactionRetries() {
		// No-op compaction strategy: returns messages unchanged, so retries can never help
		CompactionStrategy noOpStrategy = (messages, targetTokens, estimator, tokensBefore) ->
			new CompactionResult(messages, 0, tokensBefore, tokensBefore, "reactive");

		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), noOpStrategy,
			100_000, 1_000);

		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider overflowProvider = (request, handler) -> {
			throw new ProviderException(
				ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400,
				"Request too large for context window");
		};

		List<AgentEvent> captured = new ArrayList<>();
		AgentEventListener capture = captured::add;

		AgentLoop loop = new AgentLoop(config(), overflowProvider, emptyRegistry(), memory);
		AgentResult result = loop.run(ToolContext.EMPTY, capture);

		assertEquals(StopReason.CONTEXT_OVERFLOW, result.stopReason());
		assertEquals(1, result.turnsUsed());

		// Verify reactive compaction was attempted
		long compactionCount = captured.stream()
			.filter(e -> e instanceof AgentEvent.CompactionStarted cs
				&& "reactive".equals(cs.trigger()))
			.count();
		assertTrue("Reactive compaction should have been attempted", compactionCount > 0);

		// RunCompleted must carry CONTEXT_OVERFLOW
		AgentEvent.RunCompleted runCompleted =
			AgentEventAssertions.firstOfType(captured, AgentEvent.RunCompleted.class);
		assertNotNull("RunCompleted event must be emitted", runCompleted);
		assertEquals(StopReason.CONTEXT_OVERFLOW, runCompleted.reason());
	}

	/**
	 * Provider throws CONTEXT_WINDOW_EXCEEDED in the non-memory run path.
	 * Without memory there is no compaction, but the stop reason still reports
	 * the overflow so callers can react to it.
	 */
	public void testContextWindowExceededWithoutMemoryIsOverflow() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider overflowProvider = (request, handler) -> {
			throw new ProviderException(
				ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED, 400,
				"Request too large for context window");
		};

		AgentLoop loop = new AgentLoop(config(), overflowProvider, emptyRegistry());
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(StopReason.CONTEXT_OVERFLOW, result.stopReason());
		assertEquals(1, result.turnsUsed());
	}

	private static SecurityContext developmentSecurity() {
		return new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};
	}

	/**
	 * Transaction is opened on first non-READ tool call, committed after turn.
	 */
	public void testTransactionOpenedOnWrite() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit_model", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionBegan) {
				events.add("tx_begin");
			} else if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Transaction should begin on write tool",
			events.contains("tx_begin"));
		assertTrue("Transaction should be committed",
			events.contains("tx_commit"));
	}

	/**
	 * No transaction opened for read-only turns.
	 */
	public void testNoTransactionForReadOnly() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Search"))));

		ToolSchema toolSpec = new ToolSchema("search", "Searches", "{}");
		ToolCall toolCall = new ToolCall("tc1", "search", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Found.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(readTool("search", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionBegan) {
				events.add("tx_begin");
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertFalse("No transaction for read-only tools", events.contains("tx_begin"));
	}

	/**
	 * Transaction is rolled back when a tool throws.
	 */
	public void testTransactionRolledBackOnError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		ToolCall tc1 = new ToolCall("tc1", "edit_model", "{}");

		LLMProvider provider = mockProvider(
			List.of(tc1),
			List.of(new TextContent("OK")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(new Tool() {
			@Override public String name() { return "edit_model"; }
			@Override public String description() { return "Edits"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				throw new RuntimeException("Something went wrong");
			}
			@Override public ToolMode mode() { return ToolMode.STRUCTURAL; }
		});
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionBegan) {
				events.add("tx_begin");
			} else if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack r) {
				events.add("tx_rollback:" + r.reason());
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Transaction should begin", events.contains("tx_begin"));
		assertFalse("Transaction should NOT commit", events.contains("tx_commit"));
		assertTrue("Transaction should rollback",
			events.stream().anyMatch(e -> e.startsWith("tx_rollback:")));
		assertFalse("Scope should not be active after rollback", scope.isActive());
	}

	/**
	 * When {@link TransactionScope#commit()} throws, the listener receives a
	 * rollback event with the failure reason and no commit event.
	 */
	public void testCommitFailureTriggersRollback() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit_model", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionBegan) {
				events.add("tx_begin");
			} else if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack r) {
				events.add("tx_rollback:" + r.reason());
			}
		};

		TransactionScope failingCommitScope = new TestableTransactionScope() {
			@Override
			public void commit() {
				// Simulate a KB commit failure — leave state intact so rollback() can clean up.
				// The message is shaped exactly as the real TransactionScope shapes it, because
				// the loop is expected to pass it on unchanged rather than prefix it again.
				throw new TransactionException("Commit failed: constraint violation");
			}
		};
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE,
			failingCommitScope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Transaction should begin", events.contains("tx_begin"));
		assertFalse("Transaction should NOT report commit on failure",
			events.contains("tx_commit"));
		String rollback = events.stream().filter(e -> e.startsWith("tx_rollback:")).findFirst()
			.orElse(null);
		assertNotNull("Transaction should rollback with the commit-failure reason", rollback);
		assertEquals("The event carries the bare reason: it already says a rollback happened, and"
			+ " the chat prefixes it once more.", "tx_rollback:constraint violation", rollback);
	}

	/**
	 * A denied write tool must not acquire the lock or open a transaction.
	 * The security gate runs before {@code ensureTransaction}.
	 */
	public void testDeniedWriteToolDoesNotOpenTransaction() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		ToolCall toolCall = new ToolCall("tc1", "edit_model", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Cannot edit.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionBegan) {
				events.add("tx_begin");
			} else if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack) {
				events.add("tx_rollback");
			} else if (e instanceof AgentEvent.ToolCallDenied d) {
				events.add("denied:" + d.toolName());
			}
		};

		// Security context that denies all tool permissions
		SecurityContext denyAll = new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return false; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(denyAll, ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Tool should be denied", events.contains("denied:edit_model"));
		assertFalse("Transaction must not begin for a denied tool",
			events.contains("tx_begin"));
		assertFalse("No commit for a denied tool", events.contains("tx_commit"));
		assertFalse("No rollback for a denied tool — the scope never opened",
			events.contains("tx_rollback"));
		assertFalse("Scope should never have become active", scope.isActive());
		assertFalse("Scope should never have held the lock", scope.holdsLock());
	}

	/**
	 * Tools that need approval are executed sequentially (approval is interactive).
	 * READ tools that don't need approval run concurrently.
	 */
	public void testGlobalToolsForcedSequential() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema readSpec = new ToolSchema("read1", "Reads", "{}");
		ToolSchema readSpec2 = new ToolSchema("read2", "Reads", "{}");
		ToolSchema globalSpec = new ToolSchema("write1", "Writes", "{}");
		ToolCall tcRead1 = new ToolCall("tc1", "read1", "{}");
		ToolCall tcRead2 = new ToolCall("tc2", "read2", "{}");
		ToolCall tcGlobal = new ToolCall("tc3", "write1", "{}");

		LLMProvider provider = mockProvider(
			List.of(tcRead1, tcRead2, tcGlobal),
			List.of(new TextContent("Done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(readTool("read1", readSpec));
		registry.register(readTool("read2", readSpec2));
		registry.register(globalTool("write1", globalSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.ToolCallStarted s) {
				events.add("start:" + s.toolName());
			} else if (e instanceof AgentEvent.ToolCallCompleted c) {
				events.add("complete:" + c.toolName());
			} else if (e instanceof AgentEvent.ApprovalRequired a) {
				events.add("approval:" + a.toolName());
			}
		};

		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE);
		AgentLoop loop = new AgentLoop(config(), provider, registry);
		loop.run(conv, ctx, listener);

		// write1 should have approval event (it's STRUCTURAL, needs approval in dev mode)
		assertTrue("STRUCTURAL tool should trigger approval",
			events.contains("approval:write1"));
		// All tools should have start and complete events (listener passed everywhere)
		assertTrue(events.contains("start:read1"));
		assertTrue(events.contains("start:read2"));
		assertTrue(events.contains("complete:read1"));
		assertTrue(events.contains("complete:read2"));
		assertTrue(events.contains("complete:write1"));
	}

	/**
	 * Response validator rejects the LLM's response. Loop stops with REJECTED, but the
	 * message is still added to the conversation for audit trail.
	 */
	public void testResponseValidatorRejectsResponse() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Bad response")));

		AgentParams params = new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS,
			Map.of(), msg -> false);

		List<AgentEvent> captured = new ArrayList<>();
		AgentEventListener capture = captured::add;

		AgentLoop loop = new AgentLoop(params, provider, emptyRegistry());
		AgentResult result = loop.run(conv, capture);

		assertEquals(StopReason.REJECTED, result.stopReason());
		assertEquals(1, result.turnsUsed());

		// Message was still added to conversation for audit trail
		assertEquals(2, conv.size());
		assertTrue(conv.messages().get(1) instanceof AssistantMessage);

		// RunCompleted must carry REJECTED reason
		AgentEvent.RunCompleted runCompleted =
			AgentEventAssertions.firstOfType(captured, AgentEvent.RunCompleted.class);
		assertNotNull("RunCompleted event must be emitted", runCompleted);
		assertEquals(StopReason.REJECTED, runCompleted.reason());

		// TurnCompleted must precede RunCompleted
		int turnCompletedIdx = -1;
		int runCompletedIdx = -1;
		for (int i = 0; i < captured.size(); i++) {
			if (captured.get(i) instanceof AgentEvent.TurnCompleted) {
				turnCompletedIdx = i;
			}
			if (captured.get(i) instanceof AgentEvent.RunCompleted) {
				runCompletedIdx = i;
			}
		}
		assertTrue("TurnCompleted must be emitted", turnCompletedIdx >= 0);
		assertTrue("TurnCompleted must precede RunCompleted",
			turnCompletedIdx < runCompletedIdx);
	}

	/**
	 * Response validator rejects via the memory-backed run path.
	 */
	public void testResponseValidatorRejectsResponseWithMemory() {
		ConversationMemory memory = ConversationMemory.withDefaults(
			new TruncatingStrategy(), 100_000);
		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Bad response")));

		AgentParams params = new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS,
			Map.of(), msg -> false);

		List<AgentEvent> captured = new ArrayList<>();
		AgentEventListener capture = captured::add;

		AgentLoop loop = new AgentLoop(params, provider, emptyRegistry(), memory);
		AgentResult result = loop.run(ToolContext.EMPTY, capture);

		assertEquals(StopReason.REJECTED, result.stopReason());
		assertEquals(1, result.turnsUsed());

		// RunCompleted must carry REJECTED reason
		AgentEvent.RunCompleted runCompleted =
			AgentEventAssertions.firstOfType(captured, AgentEvent.RunCompleted.class);
		assertNotNull("RunCompleted event must be emitted", runCompleted);
		assertEquals(StopReason.REJECTED, runCompleted.reason());

		// TurnCompleted must precede RunCompleted
		int turnCompletedIdx = -1;
		int runCompletedIdx = -1;
		for (int i = 0; i < captured.size(); i++) {
			if (captured.get(i) instanceof AgentEvent.TurnCompleted) {
				turnCompletedIdx = i;
			}
			if (captured.get(i) instanceof AgentEvent.RunCompleted) {
				runCompletedIdx = i;
			}
		}
		assertTrue("TurnCompleted must be emitted", turnCompletedIdx >= 0);
		assertTrue("TurnCompleted must precede RunCompleted",
			turnCompletedIdx < runCompletedIdx);
	}

	/**
	 * Response validator that accepts passes through normally.
	 */
	public void testResponseValidatorAcceptsResponse() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider provider = mockProvider(List.of(new TextContent("Good response")));

		AgentParams params = new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS,
			Map.of(), msg -> true);

		AgentLoop loop = new AgentLoop(params, provider, emptyRegistry());
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(1, result.turnsUsed());
	}

	/**
	 * Tool that uses {@link ProgressReporter} emits
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolProgress} events.
	 */
	public void testToolProgressEventsEmitted() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Do work"))));

		ToolSchema toolSpec = new ToolSchema("slow_tool", "Reports progress", "{}");
		ToolCall toolCall = new ToolCall("tc1", "slow_tool", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Done.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(new Tool() {
			@Override public String name() { return "slow_tool"; }
			@Override public String description() { return "Reports progress"; }
			@Override public ToolSchema schema() { return toolSpec; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				ToolContext toolContext = (ToolContext) context;
				toolContext.progressReporter().progress("step 1");
				toolContext.progressReporter().progress("step 2");
				return ToolResult.of("completed");
			}
			@Override public ToolMode mode() { return ToolMode.READ; }
		});
		registry.freeze();

		List<AgentEvent.ToolProgress> progressEvents = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.ToolProgress tp) {
				progressEvents.add(tp);
			}
		};

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		AgentResult result = loop.run(conv, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals(2, progressEvents.size());
		assertEquals("step 1", progressEvents.get(0).message());
		assertEquals("step 2", progressEvents.get(1).message());
		assertEquals("slow_tool", progressEvents.get(0).toolName());
		assertEquals("slow_tool", progressEvents.get(1).toolName());
		assertEquals("tc1", progressEvents.get(0).toolCallId());
		assertEquals("tc1", progressEvents.get(1).toolCallId());
	}

	/** */
	public void testEventBookendsAndToolCallMatching() {
		List<AgentEvent> captured = new ArrayList<>();
		AgentEventListener capture = captured::add;

		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("What is 2+2?"))));

		ToolSchema calcSpec = new ToolSchema("calculate", "Does math", "{}");
		ToolCall toolCall = new ToolCall("tc1", "calculate", "{\"expr\":\"2+2\"}");

		// Turn 1: LLM requests tool. Turn 2: LLM responds with text.
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("The answer is 4.")));

		ToolCatalog registry = echoRegistry("calculate", calcSpec);

		AgentLoop loop = new AgentLoop(config(10), provider, registry);
		loop.run(conv, capture);

		AgentEventAssertions.assertBookends(captured);
		AgentEventAssertions.assertToolCallsMatched(captured);
		AgentEventAssertions.assertTurnNumbersMonotone(captured);
		assertNotNull(AgentEventAssertions.firstOfType(captured, AgentEvent.RunStarted.class));
		assertEquals(1, AgentEventAssertions.allOfType(captured, AgentEvent.RunCompleted.class).size());
	}

	/**
	 * A DATA_WRITE tool must not share a concurrent batch with a preceding
	 * concurrency-safe READ tool.
	 */
	public void testPartitionBatchesBreaksOnWriteTool() {
		ToolCatalog registry = new ToolCatalog();
		registry.register(stubTool("read1", ToolMode.READ, true));
		registry.register(stubTool("write1", ToolMode.DATA_WRITE, true));
		registry.freeze();

		AgentLoop loop = new AgentLoop(config(), mockProvider(List.of()), registry);
		ToolContext ctx = new ToolContext(
			com.top_logic.tl.ai.core.agent.SecurityContext.UNRESTRICTED,
			com.top_logic.tl.ai.core.agent.approval.ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);

		ToolCall c1 = new ToolCall("id1", "read1", "{}");
		ToolCall c2 = new ToolCall("id2", "write1", "{}");

		List<List<ToolCall>> batches = loop.partitionBatches(List.of(c1, c2), ctx);

		assertEquals("Expect two batches: reads then write", 2, batches.size());
		assertEquals(1, batches.get(0).size());
		assertEquals("read1", batches.get(0).get(0).name());
		assertEquals(1, batches.get(1).size());
		assertEquals("write1", batches.get(1).get(0).name());
	}

	/**
	 * Two consecutive READ tools may share a concurrent batch.
	 */
	public void testPartitionBatchesGroupsConsecutiveReads() {
		ToolCatalog registry = new ToolCatalog();
		registry.register(stubTool("read1", ToolMode.READ, true));
		registry.register(stubTool("read2", ToolMode.READ, true));
		registry.freeze();

		AgentLoop loop = new AgentLoop(config(), mockProvider(List.of()), registry);
		ToolContext ctx = new ToolContext(
			com.top_logic.tl.ai.core.agent.SecurityContext.UNRESTRICTED,
			com.top_logic.tl.ai.core.agent.approval.ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);

		ToolCall c1 = new ToolCall("id1", "read1", "{}");
		ToolCall c2 = new ToolCall("id2", "read2", "{}");

		List<List<ToolCall>> batches = loop.partitionBatches(List.of(c1, c2), ctx);

		assertEquals("Two reads collapse into one batch", 1, batches.size());
		assertEquals(2, batches.get(0).size());
	}

	/**
	 * A USER_SCOPED tool must not share a concurrent batch with a preceding
	 * concurrency-safe READ tool. USER_SCOPED tools trigger
	 * {@code ensureTransaction}, which opens a KB transaction in the worker's
	 * interaction-scoped DBContext; that transaction is auto-rolled-back when the
	 * worker's interaction unbinds, leaving the shared {@code TransactionScope}
	 * holding a zombie TX that the agent's end-of-turn commit then fails on.
	 */
	public void testPartitionBatchesBreaksOnUserScopedTool() {
		ToolCatalog registry = new ToolCatalog();
		registry.register(stubTool("read1", ToolMode.READ, true));
		registry.register(stubTool("userScoped1", ToolMode.USER_SCOPED, true));
		registry.freeze();

		AgentLoop loop = new AgentLoop(config(), mockProvider(List.of()), registry);
		ToolContext ctx = new ToolContext(
			com.top_logic.tl.ai.core.agent.SecurityContext.UNRESTRICTED,
			com.top_logic.tl.ai.core.agent.approval.ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);

		ToolCall c1 = new ToolCall("id1", "read1", "{}");
		ToolCall c2 = new ToolCall("id2", "userScoped1", "{}");

		List<List<ToolCall>> batches = loop.partitionBatches(List.of(c1, c2), ctx);

		assertEquals("Expect two batches: read then user-scoped", 2, batches.size());
		assertEquals(1, batches.get(0).size());
		assertEquals("read1", batches.get(0).get(0).name());
		assertEquals(1, batches.get(1).size());
		assertEquals("userScoped1", batches.get(1).get(0).name());
	}

	/** Minimal {@link Tool} stub used by partitionBatches tests. */
	private static Tool stubTool(String toolName, ToolMode mode, boolean concurrencySafe) {
		return new Tool() {
			@Override public String name() { return toolName; }
			@Override public String description() { return toolName; }
			@Override public ToolSchema schema() { return new ToolSchema(toolName, toolName, "{}"); }
			@Override public ToolMode mode() { return mode; }
			@Override public boolean isConcurrencySafe() { return concurrencySafe; }
			@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
				return ToolResult.of("ok");
			}
		};
	}

	/**
	 * Creates a {@link ToolRegistry} that executes tools inline, without the
	 * {@code ThreadContextManager} interaction wrapping of {@link ToolCatalog} —
	 * usable in plain unit tests without started TopLogic modules.
	 */
	private static ToolRegistry stubRegistry(Tool... tools) {
		Map<String, Tool> byName = new HashMap<>();
		for (Tool tool : tools) {
			byName.put(tool.name(), tool);
		}
		return new ToolRegistry() {
			@Override
			public Tool getTool(String name) {
				return byName.get(name);
			}

			@Override
			public List<ToolSchema> toolSchemas(Predicate<Tool> filter) {
				return byName.values().stream().filter(filter).map(Tool::schema).toList();
			}

			@Override
			public ToolResultMessage execute(ToolCall toolCall, ToolContext context) {
				Instant start = Instant.now();
				Tool tool = byName.get(toolCall.name());
				if (tool == null) {
					return new ToolResultMessage(UUID.randomUUID().toString(), start,
						toolCall.id(), toolCall.name(),
						new ToolState.Failed("Unknown tool: '" + toolCall.name() + "'.", start, start));
				}
				try {
					ToolResult result = tool.execute(Map.of(), context);
					return new ToolResultMessage(UUID.randomUUID().toString(), Instant.now(),
						toolCall.id(), toolCall.name(),
						new ToolState.Completed(result.text(), start, Instant.now(), false));
				} catch (Exception e) {
					return new ToolResultMessage(UUID.randomUUID().toString(), Instant.now(),
						toolCall.id(), toolCall.name(),
						new ToolState.Failed(String.valueOf(e.getMessage()), start, Instant.now()));
				}
			}
		};
	}

	/**
	 * The third consecutive identical tool call is blocked with a synthetic error result.
	 */
	public void testConsecutiveDuplicateCallBlocked() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("lookup", "Looks up", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc2", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc3", "lookup", "{\"q\":1}")),
			List.of(new TextContent("Done.")));

		AgentLoop loop = new AgentLoop(config(), provider, stubRegistry(readTool("lookup", toolSpec)));
		AgentResult result = loop.run(conv, e -> { /* empty listener */ });

		assertEquals(StopReason.COMPLETE, result.stopReason());
		List<ToolResultMessage> failures = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.filter(tr -> tr.state() instanceof ToolState.Failed)
			.toList();
		assertEquals(1, failures.size());
		assertEquals("tc3", failures.get(0).toolCallId());
		ToolState.Failed failed = (ToolState.Failed) failures.get(0).state();
		assertTrue(failed.error().contains("DUPLICATE CALL BLOCKED"));
	}

	/**
	 * A different tool call in between resets the duplicate counter, so a later
	 * legitimate re-read is not blocked.
	 */
	public void testDuplicateCounterResetsOnDifferentCall() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("lookup", "Looks up", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc2", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc3", "lookup", "{\"q\":2}")),
			List.of(new ToolCall("tc4", "lookup", "{\"q\":1}")),
			List.of(new TextContent("Done.")));

		AgentLoop loop = new AgentLoop(config(), provider, stubRegistry(readTool("lookup", toolSpec)));
		AgentResult result = loop.run(conv, e -> { /* empty listener */ });

		assertEquals(StopReason.COMPLETE, result.stopReason());
		boolean anyBlocked = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed);
		assertFalse("Re-read after a different call must not be blocked", anyBlocked);
	}

	/**
	 * A model alternating between two distinct calls without making progress (A, B, A, B, ...)
	 * is eventually blocked too, even though no single call repeats consecutively and the
	 * plain consecutive-repeat counter alone would never catch it.
	 */
	public void testAlternatingDuplicateCallsBlocked() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("lookup", "Looks up", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc2", "lookup", "{\"q\":2}")),
			List.of(new ToolCall("tc3", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc4", "lookup", "{\"q\":2}")),
			List.of(new TextContent("Done.")));

		AgentLoop loop = new AgentLoop(config(), provider, stubRegistry(readTool("lookup", toolSpec)));
		AgentResult result = loop.run(conv, e -> { /* empty listener */ });

		assertEquals(StopReason.COMPLETE, result.stopReason());
		List<ToolResultMessage> failures = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.filter(tr -> tr.state() instanceof ToolState.Failed)
			.toList();
		assertEquals(1, failures.size());
		assertEquals("tc4", failures.get(0).toolCallId());
		ToolState.Failed failed = (ToolState.Failed) failures.get(0).state();
		assertTrue(failed.error().contains("DUPLICATE CALL BLOCKED"));
		assertTrue(failed.error().contains("alternating"));
	}

	/**
	 * A genuine 3-way rotation (A, B, C, A, B, C) is not a period-2 oscillation and must not
	 * be blocked by the alternation guard.
	 */
	public void testThreeWayRotationNotBlocked() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolSchema toolSpec = new ToolSchema("lookup", "Looks up", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc2", "lookup", "{\"q\":2}")),
			List.of(new ToolCall("tc3", "lookup", "{\"q\":3}")),
			List.of(new ToolCall("tc4", "lookup", "{\"q\":1}")),
			List.of(new ToolCall("tc5", "lookup", "{\"q\":2}")),
			List.of(new ToolCall("tc6", "lookup", "{\"q\":3}")),
			List.of(new TextContent("Done.")));

		AgentLoop loop = new AgentLoop(config(), provider, stubRegistry(readTool("lookup", toolSpec)));
		AgentResult result = loop.run(conv, e -> { /* empty listener */ });

		assertEquals(StopReason.COMPLETE, result.stopReason());
		boolean anyBlocked = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed);
		assertFalse("A 3-way rotation must not trip the period-2 alternation guard", anyBlocked);
	}

	/**
	 * A synthetic failure (unknown tool) in the same turn as a successful write must not
	 * roll back the write: the tool never executed, so nothing can be corrupted.
	 */
	public void testUnknownToolDoesNotRollBackTurn() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		LLMProvider provider = mockProvider(
			List.of(
				new ToolCall("tc1", "edit_model", "{}"),
				new ToolCall("tc2", "no_such_tool", "{}")),
			List.of(new TextContent("Done.")));

		ToolRegistry registry = stubRegistry(globalTool("edit_model", toolSpec));

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack) {
				events.add("tx_rollback");
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Successful write must be committed", events.contains("tx_commit"));
		assertFalse("Synthetic failure must not roll back the turn", events.contains("tx_rollback"));
	}

	/** Provider with an int-typed parameter, for triggering a real argument-coercion failure. */
	public static class CoercionFailureProvider {
		@ToolMethod(name = "count_items", description = "Counts", mode = ToolMode.READ)
		public String count(@P("How many") int amount) {
			return "counted " + amount;
		}
	}

	/**
	 * A synthetic failure from unresolvable tool arguments (going through the real
	 * ToolCatalog, not a stub) in the same turn as a successful write must not roll back
	 * the write either — same guarantee as {@link #testUnknownToolDoesNotRollBackTurn}, but
	 * for the ToolCatalog-level synthetic marking instead of the AgentLoop-level one.
	 */
	public void testArgumentCoercionFailureDoesNotRollBackTurn() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema editSpec = new ToolSchema("edit_model", "Edits", "{}");
		LLMProvider provider = mockProvider(
			List.of(
				new ToolCall("tc1", "edit_model", "{}"),
				new ToolCall("tc2", "count_items", "{\"amount\": \"not-a-number\"}")),
			List.of(new TextContent("Done.")));

		ToolCatalog catalog = new ToolCatalog();
		catalog.register(globalTool("edit_model", editSpec));
		catalog.register(new CoercionFailureProvider());
		catalog.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack) {
				events.add("tx_rollback");
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, catalog);
		AgentResult result = loop.run(conv, ctx, listener);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertTrue("Successful write must be committed", events.contains("tx_commit"));
		assertFalse("Argument-coercion failure never reached the tool body — must not roll back",
			events.contains("tx_rollback"));
	}

	/**
	 * When a tool execution failure rolls back the turn, the model is informed that
	 * successful writes of the same turn were discarded.
	 */
	public void testRollbackNoticeAddedToConversation() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema okSpec = new ToolSchema("edit_model", "Edits", "{}");
		ToolSchema failSpec = new ToolSchema("failing_tool", "Fails", "{}");
		LLMProvider provider = mockProvider(
			List.of(
				new ToolCall("tc1", "edit_model", "{}"),
				new ToolCall("tc2", "failing_tool", "{}")),
			List.of(new TextContent("Done.")));

		ToolRegistry registry = stubRegistry(
			globalTool("edit_model", okSpec),
			new Tool() {
				@Override public String name() { return "failing_tool"; }
				@Override public String description() { return "Fails"; }
				@Override public ToolSchema schema() { return failSpec; }
				@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
					throw new RuntimeException("boom");
				}
				@Override public ToolMode mode() { return ToolMode.STRUCTURAL; }
			});

		TestableTransactionScope scope = new TestableTransactionScope();
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, e -> { /* empty listener */ });

		assertEquals(StopReason.COMPLETE, result.stopReason());
		boolean noticePresent = conv.messages().stream()
			.filter(UserMessage.class::isInstance)
			.map(UserMessage.class::cast)
			.flatMap(m -> m.content().stream())
			.filter(TextContent.class::isInstance)
			.map(TextContent.class::cast)
			.anyMatch(t -> t.text().contains("rolled back"));
		assertTrue("Model must be told about the rollback", noticePresent);
	}

	/**
	 * An unexpected runtime failure still emits a terminal
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.RunCompleted} so event consumers are
	 * not left waiting, and the exception propagates.
	 */
	public void testRunCompletedEmittedOnUnexpectedError() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		LLMProvider brokenProvider = (request, handler) -> {
			// Stream ends without MessageComplete → IllegalStateException in the collector.
		};

		List<AgentEvent> captured = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(), brokenProvider, emptyRegistry());
		try {
			loop.run(conv, captured::add);
			fail("Expected the runtime failure to propagate");
		} catch (IllegalStateException expected) {
			// expected
		}

		AgentEvent.RunCompleted runCompleted =
			AgentEventAssertions.firstOfType(captured, AgentEvent.RunCompleted.class);
		assertNotNull("Terminal RunCompleted must be emitted", runCompleted);
		assertEquals(StopReason.ERROR, runCompleted.reason());
	}
}
