/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
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
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.RoleToolAccess.DeclaredGroups;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.core.security.ToolChangeSet;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.error.TopLogicException;

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

	/**
	 * An over-long <em>failed</em> result costs the same as an over-long successful one - it sits
	 * in the conversation for every later request of the run - so the same budget bounds it and the
	 * same event reports that it did. What must survive the cut is the failed flag itself and the
	 * {@code synthetic} bit the end-of-turn rollback asks.
	 */
	public void testOversizedFailureIsTruncated() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Call it."))));

		ToolCall toolCall = new ToolCall("tc1", "failing", "{}");
		List<List<Message>> requests = new ArrayList<>();
		LLMProvider provider = recording(requests, mockProvider(
			List.of(toolCall),
			List.of(new TextContent("done"))));

		ToolCatalog registry = new ToolCatalog();
		registry.register(failingTool("failing", OVERSIZED_RESULT));
		registry.freeze();

		List<AgentEvent> events = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(10), provider, registry, SMALL_RESULT_CAP);
		loop.run(conv, events::add);

		ToolState.Failed failed = firstFailedResult(conv);
		assertTrue("failure must be shortened", failed.error().length() < OVERSIZED_RESULT.length());
		assertTrue("the failure must fit the budget plus the notice",
			failed.error().length() <= SMALL_RESULT_CAP + 64);
		assertFalse("the middle must be dropped", failed.error().contains(MIDDLE_MARKER));
		assertTrue("truncation notice present", failed.error().contains("truncated"));
		assertTrue("a truncation event is emitted",
			events.stream().anyMatch(AgentEvent.ToolCallResultTruncated.class::isInstance));
		assertTrue("a tool reporting its own failure stays synthetic", failed.synthetic());

		// What the next request carried is what actually costs tokens.
		assertEquals("two provider calls", 2, requests.size());
		ToolState sentBack = sentToolState(requests.get(1), "tc1");
		assertTrue("the failure reaches the provider as a failure", sentBack instanceof ToolState.Failed);
		assertEquals("the provider sees the truncated text",
			failed.error(), ((ToolState.Failed) sentBack).error());
	}

	/**
	 * A failure within the budget is passed on byte for byte - no notice appended, no event, and
	 * still a failure.
	 */
	public void testShortFailureIsNotTruncated() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Call it."))));

		String message = "No invoice matches number C-2026-0042.";
		ToolCall toolCall = new ToolCall("tc1", "failing", "{}");
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(failingTool("failing", message));
		registry.freeze();

		List<AgentEvent> events = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(10), provider, registry, SMALL_RESULT_CAP);
		loop.run(conv, events::add);

		ToolState.Failed failed = firstFailedResult(conv);
		assertEquals("a failure within the budget is untouched", message, failed.error());
		assertFalse("no truncation event for a short failure",
			events.stream().anyMatch(AgentEvent.ToolCallResultTruncated.class::isInstance));
		assertTrue("still flagged as a failure", failed.synthetic());
	}

	/**
	 * A failure that arrives by <em>throwing</em> is bounded the same way, and stays
	 * non-synthetic: truncating the sentence must not cost the turn its rollback.
	 */
	public void testOversizedThrownFailureKeepsRollbackBit() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Call it."))));

		ToolCall toolCall = new ToolCall("tc1", "throwing", "{}");
		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("done")));

		ToolSchema schema = new ToolSchema("throwing", "Throws", "{}");
		ToolCatalog registry = new ToolCatalog();
		registry.register(ToolStubs.builder("throwing").description("").schema(schema)
			.execute((args, context) -> {
				throw new IllegalArgumentException(OVERSIZED_RESULT);
			}).build());
		registry.freeze();

		List<AgentEvent> events = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(10), provider, registry, SMALL_RESULT_CAP);
		loop.run(conv, events::add);

		ToolState.Failed failed = firstFailedResult(conv);
		assertTrue("the thrown message must be shortened",
			failed.error().length() < OVERSIZED_RESULT.length());
		assertTrue("truncation notice present", failed.error().contains("truncated"));
		assertTrue("a truncation event is emitted",
			events.stream().anyMatch(AgentEvent.ToolCallResultTruncated.class::isInstance));
		assertFalse("a thrown failure still rolls the turn back", failed.synthetic());
	}

	/** A read tool answering with {@link ToolResult#failure(String)}. */
	private static Tool failingTool(String name, String text) {
		ToolSchema schema = new ToolSchema(name, "Reports a failure", "{}");
		return ToolStubs.builder(name).description("").schema(schema)
			.execute((args, context) -> ToolResult.failure(text)).build();
	}

	/** The state of the named tool call as it was sent to the provider. */
	private static ToolState sentToolState(List<Message> sent, String toolCallId) {
		return sent.stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.filter(m -> toolCallId.equals(m.toolCallId()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("tool result " + toolCallId + " not sent"))
			.state();
	}

	/** Extracts the single recorded failed tool result from a completed conversation. */
	private static ToolState.Failed firstFailedResult(Conversation conv) {
		ToolResultMessage message = conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("no tool result recorded"));
		assertTrue("tool result failed", message.state() instanceof ToolState.Failed);
		return (ToolState.Failed) message.state();
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
		return ToolStubs.builder(name).description("").schema(schema)
			.execute((args, context) -> new ToolResult(text, null, truncationHint)).build();
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
	 * What a failed run leaves behind: the history it started from, and nothing of its own.
	 *
	 * <p>
	 * The regression: a run that died in the provider halfway through left an assistant turn
	 * whose tool calls had no results. The next send replayed it, the provider refused it for the
	 * same reason, and the chat was dead for good. Only the message that started the failed run
	 * stays, so the user does not have to type it again.
	 * </p>
	 */
	public void testAFailedRunLeavesNothingBehind() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Erzähl mir was."))));
		conv.add(new AssistantMessage("a1", T0, List.of(new TextContent("Gerne.")), USAGE, "end_turn"));
		conv.add(new UserMessage("u2", T0, List.of(new TextContent("Und jetzt das Modell erkunden."))));
		List<Message> beforeTheRun = conv.messages();

		ToolSchema schema = new ToolSchema("echo_tool", "Echoes", "{}");
		AtomicInteger calls = new AtomicInteger();
		LLMProvider diesOnTheSecondCall = (request, handler) -> {
			if (calls.getAndIncrement() > 0) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
					"messages.1.content.1.thinking.signature: Field required");
			}
			ToolCall call = new ToolCall("tc1", "echo_tool", "{\"message\":\"hi\"}");
			handler.onEvent(new StreamEvent.MessageComplete("msg-0", MODEL, "tool_use", USAGE, List.of(call)));
		};

		AgentLoop loop = new AgentLoop(config(), diesOnTheSecondCall, echoRegistry("echo_tool", schema));
		AgentResult result = loop.run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(StopReason.ERROR, result.stopReason());
		assertEquals("The assistant turn and its tool result are gone with the failed run.",
			beforeTheRun, conv.messages());
	}

	/** A run that fails on its first call has added nothing yet, and takes nothing away. */
	public void testAFailedFirstCallKeepsTheQuestion() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hallo"))));
		List<Message> beforeTheRun = conv.messages();

		LLMProvider failing = (request, handler) -> {
			throw new ProviderException(ProviderException.Kind.OVERLOADED, 529, "Overloaded");
		};

		new AgentLoop(config(), failing, emptyRegistry()).run(conv, (AgentEventListener) e -> {
			/** empty listener */
		});

		assertEquals(beforeTheRun, conv.messages());
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
		registry.register(ToolStubs.builder("risky_tool").description("Might fail").schema(toolSpec)
			.execute((args, context) -> {
				throw new RuntimeException("Something went wrong");
			}).build());
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
		registry.register(ToolStubs.builder("bad_tool").description("Fails").schema(toolSpec)
			.execute((args, context) -> {
				throw new RuntimeException("Boom");
			}).build());
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
		registry.register(ToolStubs.builder("tool").description("T").schema(toolSpec).execute((args, context) -> {
				Thread.currentThread().interrupt();
				return ToolResult.of("ok");
			}).build());
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
		registry.register(ToolStubs.builder("create_class").description("Creates a class").schema(toolSpec)
			.mode(ToolMode.STRUCTURAL).execute((args, context) -> {
				fail("Tool should not execute in production mode");
				return ToolResult.of("");
			}).build());
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

	/** A write tool with a dry run and an early check, counting how often it really ran. */
	public static class CheckedWriter {

		final List<Boolean> _runs = new ArrayList<>();

		@ToolMethod(description = "Writes", mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE,
			dryRun = "dryRun", check = "refuse", marking = AnswerMarking.NONE)
		public String write(@P("what") String what, @P(value = "dry run", required = false) Boolean dryRun) {
			_runs.add(Boolean.TRUE.equals(dryRun));
			return Boolean.TRUE.equals(dryRun) ? "would write " + what : "wrote " + what;
		}

		String refuse(String what, Boolean dryRun) {
			return "nothing".equals(what) ? "Nothing to write." : null;
		}
	}

	/**
	 * The dry run of a write tool changes nothing: nobody is asked to approve it, and it runs as a
	 * read, which is what keeps it out of the turn's transaction. The real call is asked about.
	 */
	public void testADryRunIsNotPutUpForApproval() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Write"))));
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "write", "{\"what\":\"x\",\"dryRun\":true}")),
			List.of(new ToolCall("tc2", "write", "{\"what\":\"x\"}")),
			List.of(new TextContent("Done")));
		CheckedWriter writer = new CheckedWriter();
		ToolCatalog registry = new ToolCatalog();
		AnnotatedTools.from(writer).forEach(registry::register);
		registry.freeze();
		List<ApprovalRequest> approvals = new ArrayList<>();
		List<AgentEvent.ToolCallStarted> started = new ArrayList<>();

		new AgentLoop(config(), provider, registry).run(conv,
			new ToolContext(developmentSecurity(), request -> {
				approvals.add(request);
				return new ApprovalResult.Approved();
			}),
			event -> {
				if (event instanceof AgentEvent.ToolCallStarted call) {
					started.add(call);
				}
			});

		assertEquals(List.of(true, false), writer._runs);
		assertEquals("Only the real call is put up for approval.", 1, approvals.size());
		assertEquals("tc2", approvals.get(0).toolCallId());
		assertEquals(ToolMode.READ, started.get(0).mode());
		assertEquals(ToolMode.DATA_WRITE, started.get(1).mode());
	}

	/**
	 * A call its tool refuses before it runs is answered with the refusal, without an approval
	 * prompt and without running.
	 */
	public void testACallTheToolRefusesIsNotPutUpForApproval() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Write"))));
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "write", "{\"what\":\"nothing\"}")),
			List.of(new TextContent("Done")));
		CheckedWriter writer = new CheckedWriter();
		ToolCatalog registry = new ToolCatalog();
		AnnotatedTools.from(writer).forEach(registry::register);
		registry.freeze();
		AtomicInteger approvals = new AtomicInteger();

		new AgentLoop(config(), provider, registry).run(conv,
			new ToolContext(developmentSecurity(), request -> {
				approvals.incrementAndGet();
				return new ApprovalResult.Approved();
			}),
			listener());

		assertEquals(0, approvals.get());
		assertTrue("The tool must not run.", writer._runs.isEmpty());
		assertTrue(conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed f && f.error().equals("Nothing to write.")));
	}

	/**
	 * A tool that cannot act in the run is not offered, and is asked again for every request: the
	 * {@code present} tool of a page that names no channel is withheld from nobody, it has nothing to
	 * do there (#649). Once its place exists, the next request offers it.
	 */
	public void testAToolThatCannotActInTheRunIsNotOffered() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Show me the customers"))));
		AtomicBoolean place = new AtomicBoolean();
		ToolCatalog registry = new ToolCatalog();
		registry.register(ToolStubs.builder("search")
			.execute((args, context) -> {
				// What the run is attached to changes between two requests: a pane appears.
				place.set(true);
				return ToolResult.of("3 customers");
			})
			.build());
		registry.register(ToolStubs.builder("show")
			.unavailable(context -> place.get() ? null : "This page has no place to show anything.")
			.build());
		registry.freeze();
		LLMProvider responses = mockProvider(
			List.of(new ToolCall("tc1", "search", "{}")),
			List.of(new TextContent("Done")));
		List<ProviderRequest> requests = new ArrayList<>();
		LLMProvider capturing = (request, handler) -> {
			requests.add(request);
			responses.stream(request, handler);
		};

		new AgentLoop(config(), capturing, registry).run(conv,
			new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE), listener());

		assertEquals(2, requests.size());
		assertEquals(List.of("search"), requests.get(0).tools().stream().map(ToolSchema::name).toList());
		assertEquals(List.of("search", "show"), requests.get(1).tools().stream().map(ToolSchema::name).toList());
	}

	/**
	 * A call that names a tool which cannot act in the run anyway - a model remembering it from an
	 * earlier turn - is answered with the tool's reason, and the tool does not run.
	 */
	public void testACallForAToolThatCannotActIsAnsweredWithItsReason() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Show me the customers"))));
		AtomicInteger runs = new AtomicInteger();
		ToolCatalog registry = new ToolCatalog();
		registry.register(ToolStubs.builder("show")
			.unavailable(context -> "This page has no place to show anything.")
			.execute((args, context) -> {
				runs.incrementAndGet();
				return ToolResult.of("shown");
			})
			.build());
		registry.freeze();
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "show", "{}")),
			List.of(new TextContent("Here they are, in text.")));

		new AgentLoop(config(), provider, registry).run(conv,
			new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE), listener());

		assertEquals("The tool must not run.", 0, runs.get());
		assertTrue(conv.messages().stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed f
				&& f.error().equals("This page has no place to show anything.")));
	}

	private static Tool globalTool(String name, ToolSchema schema) {
		return ToolStubs.builder(name).description("").schema(schema).mode(ToolMode.STRUCTURAL)
			.result("Result of " + name).build();
	}

	private static Tool readTool(String name, ToolSchema schema) {
		return ToolStubs.builder(name).description("").schema(schema).result("Result of " + name).build();
	}

	private static Tool externalTool(String name, ToolSchema schema) {
		return ToolStubs.builder(name).description("").schema(schema).mode(ToolMode.EXTERNAL).concurrencySafe(false)
			.result("Result of " + name).build();
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

		private ToolChangeSet _changes = ToolChangeSet.EMPTY;

		private int _changeReads;

		/** Makes the scope report the given pending changes to the pre-commit guard. */
		void withChanges(ToolChangeSet changes) {
			_changes = changes;
		}

		/** How often the loop asked for the pending changes. */
		int changeReads() {
			return _changeReads;
		}

		/** */
		@Override
		public ToolChangeSet changes() {
			_changeReads++;
			return _changes;
		}

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

	// --- The untrusted-content rule is part of every request. ---

	/**
	 * Every provider request carries the standing rule about {@code <untrusted_*>} blocks: once, after
	 * the run's own prompt.
	 *
	 * <p>
	 * The rule is what the tags mean. Marking a tool result no longer explains itself - the note that
	 * used to precede every marked block is stated once per run instead - so a run whose system prompt
	 * lacks the rule hands the model tags it was never told about. Pinned here, on the loop, because
	 * this is where the rule is composed: {@link AgentFactory} is one of several places that build an
	 * {@link AgentParams}, and the tags are emitted by the tool executions of this same loop.
	 * </p>
	 *
	 * <p>
	 * Two turns, so that "once" is actually tested: a rule appended per turn would accumulate in the
	 * prompt of a long run.
	 * </p>
	 */
	public void testEveryRequestCarriesTheUntrustedContentRule() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Read ticket 7."))));

		ToolCatalog registry = echoRegistry("read", new ToolSchema("read", "Reads", "{}"));
		LLMProvider responses = mockProvider(
			List.of(new ToolCall("tc1", "read", "{}")),
			List.of(new TextContent("Done.")));

		List<ProviderRequest> requests = new ArrayList<>();
		LLMProvider capturing = (request, handler) -> {
			requests.add(request);
			responses.stream(request, handler);
		};

		AgentResult result = new AgentLoop(config(10), capturing, registry).run(conv, listener());

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("a tool turn and an answer turn", 2, requests.size());
		for (ProviderRequest request : requests) {
			String prompt = request.systemPrompt();
			assertTrue("the run keeps its own instructions first, got:\n" + prompt,
				prompt.startsWith("You are a test assistant."));
			assertTrue("and the standing rule follows them, got:\n" + prompt,
				prompt.endsWith(UntrustedEnvelope.standingRule()));
			assertEquals("stated once per run, not once per turn", 1,
				prompt.split("### Untrusted content", -1).length - 1);
		}
	}

	/**
	 * A run without a system prompt of its own still carries the rule.
	 *
	 * <p>
	 * {@link AgentParams} allows a {@code null} prompt, and such a run still executes the tools that
	 * emit marked blocks. Tags with nothing explaining them is the one outcome the hoist must not
	 * produce.
	 * </p>
	 */
	public void testAPromptlessRunStillCarriesTheRule() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		List<ProviderRequest> requests = new ArrayList<>();
		LLMProvider responses = mockProvider(List.of(new TextContent("Hi")));
		LLMProvider capturing = (request, handler) -> {
			requests.add(request);
			responses.stream(request, handler);
		};

		AgentParams noPrompt = new AgentParams(MODEL, null, 5, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
		new AgentLoop(noPrompt, capturing, emptyRegistry()).run(conv, listener());

		assertEquals(1, requests.size());
		assertEquals(UntrustedEnvelope.standingRule(), requests.get(0).systemPrompt());
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
	 * A context carrying the given model access checks, which the pre-commit guard consults.
	 */
	private static SecurityContext enforcedSecurity(ModelSecurity model) {
		return new SecurityContext() {
			@Override public boolean hasToolPermission(Tool tool) { return true; }
			@Override public ExecutionMode executionMode() { return ExecutionMode.DEVELOPMENT; }
			@Override public ModelSecurity modelSecurity() { return model; }
		};
	}

	/**
	 * A {@link ModelSecurity} that refuses every write, create and delete, and permits every read.
	 */
	private static ModelSecurity refusesEveryWrite() {
		return new ModelSecurity() {
			@Override public boolean canRead(TLObject obj) { return true; }
			@Override public boolean canRead(TLObject obj, TLStructuredTypePart attribute) { return true; }
			@Override public boolean canWrite(TLObject obj) { return false; }
			@Override public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) { return false; }
			@Override public boolean canDelete(TLObject obj) { return false; }
			@Override public boolean canCreate(TLClass type, TLObject context) { return false; }
			@Override public boolean canCreate(TLObject parent, TLStructuredTypePart attr, TLClass type) { return false; }
			@Override public boolean canSetInitial(TLObject draft, TLStructuredTypePart attr) { return false; }
			@Override public boolean canPerform(TLObject obj, BoundCommandGroup operation) { return false; }
			@Override public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
				return List.copyOf(objects);
			}
			@Override public Set<TLClass> accessibleTypes(BoundCommandGroup operation) { return Set.of(); }
		};
	}

	/** A stand-in for a model object the loop only ever passes on. */
	private static TLObject someObject(String name) {
		return (TLObject) java.lang.reflect.Proxy.newProxyInstance(TLObject.class.getClassLoader(),
			new Class<?>[] { TLObject.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "toString" -> name;
				case "hashCode" -> name.hashCode();
				case "equals" -> proxy == args[0];
				default -> null;
			});
	}

	/**
	 * A write the run's principal may not make is caught before the commit, and the whole turn is
	 * rolled back - not only the forbidden part.
	 */
	public void testPreCommitGuardRollsBackTheWholeTurn() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "edit_model", "{}")),
			List.of(new TextContent("Done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack r) {
				events.add("tx_rollback:" + r.reason());
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		scope.withChanges(new ToolChangeSet(List.of(), Map.of(someObject("secret1"), Set.of()), List.of()));
		ToolContext ctx =
			new ToolContext(enforcedSecurity(refusesEveryWrite()), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		loop.run(conv, ctx, listener);

		assertFalse("The turn must not commit.", events.contains("tx_commit"));
		assertEquals(List.of("tx_rollback:Pre-commit security check failed"), events);
		assertFalse("The scope must not be active any more.", scope.isActive());

		String notice = frameworkNotice(conv);
		assertNotNull("The model must be told that its writes are gone.", notice);
		assertTrue("The notice must name the operation, but was: " + notice, notice.contains("Write"));
		assertTrue("The notice must say the rollback is all-or-nothing, but was: " + notice,
			notice.contains("all-or-nothing"));
		assertFalse("The notice must not quote the object, but was: " + notice, notice.contains("secret1"));
	}

	/** A turn whose writes the principal may make commits as before. */
	public void testPreCommitGuardLetsPermittedWritesThrough() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "edit_model", "{}")),
			List.of(new TextContent("Done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			} else if (e instanceof AgentEvent.TransactionRolledBack) {
				events.add("tx_rollback");
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		scope.withChanges(new ToolChangeSet(List.of(), Map.of(someObject("note1"), Set.of()), List.of()));
		ToolContext ctx =
			new ToolContext(enforcedSecurity(ModelSecurity.UNCHECKED), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		loop.run(conv, ctx, listener);

		assertEquals(List.of("tx_commit"), events);
		assertNull("Nothing was rolled back, so nothing has to be corrected.", frameworkNotice(conv));
	}

	/**
	 * A read-only turn opens no transaction, so the guard is never consulted and the change set is
	 * never read.
	 */
	public void testReadOnlyTurnNeverAsksTheGuard() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Search"))));

		ToolSchema toolSpec = new ToolSchema("search", "Searches", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "search", "{}")),
			List.of(new TextContent("Found.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(readTool("search", toolSpec));
		registry.freeze();

		TestableTransactionScope scope = new TestableTransactionScope();
		scope.withChanges(new ToolChangeSet(List.of(), Map.of(someObject("secret1"), Set.of()), List.of()));
		ToolContext ctx =
			new ToolContext(enforcedSecurity(refusesEveryWrite()), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		loop.run(conv, ctx, listener -> { });

		assertEquals("A read-only turn must not pay for the change set.", 0, scope.changeReads());
		assertNull(frameworkNotice(conv));
	}

	/**
	 * A writing turn whose principal may make every change it made is committed.
	 *
	 * <p>
	 * The guard runs for every run now, so the change set is read - and read once. What decides is the
	 * run's model access rights, and here they grant everything.
	 * </p>
	 */
	public void testWritingTurnThePrincipalMayMakeIsCommitted() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Edit"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "edit_model", "{}")),
			List.of(new TextContent("Done")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<String> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			if (e instanceof AgentEvent.TransactionCommitted) {
				events.add("tx_commit");
			}
		};

		TestableTransactionScope scope = new TestableTransactionScope();
		scope.withChanges(new ToolChangeSet(List.of(), Map.of(someObject("secret1"), Set.of()), List.of()));
		// developmentSecurity() carries ModelSecurity.UNCHECKED, which permits every change.
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		loop.run(conv, ctx, listener);

		assertEquals(List.of("tx_commit"), events);
		assertEquals("The guard reads the change set once per writing turn.", 1, scope.changeReads());
	}

	/**
	 * The text of the {@code [Framework notice]} the loop added, or <code>null</code> if it added
	 * none.
	 */
	private static String frameworkNotice(Conversation conv) {
		for (Message message : conv.messages()) {
			if (message instanceof UserMessage user) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent text && text.text().startsWith("[Framework notice]")) {
						return text.text();
					}
				}
			}
		}
		return null;
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
	 * No transaction is opened for an {@link ToolMode#EXTERNAL} tool call.
	 *
	 * <p>
	 * Driven through the loop rather than by reading {@code ensureTransaction}, because what has to
	 * hold is that no {@code TransactionBegan} reaches a listener. An external call cannot be
	 * enrolled in a transaction of ours in any useful sense: the remote system committed it and
	 * {@code rollbackTurn} has no reach into another application. A transaction opened for it would
	 * only ever commit an empty change set, while telling a client watching the turn's events that
	 * the call was taken back when the turn rolled back.
	 * </p>
	 */
	public void testNoTransactionForExternalTool() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("File it"))));

		ToolSchema toolSpec = new ToolSchema("mcp__jira__createIssue", "Files an issue", "{}");
		ToolCall toolCall = new ToolCall("tc1", "mcp__jira__createIssue", "{}");

		LLMProvider provider = mockProvider(
			List.of(toolCall),
			List.of(new TextContent("Filed.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(externalTool("mcp__jira__createIssue", toolSpec));
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
		assertFalse("An external call must not open a transaction.", events.contains("tx_begin"));
		assertFalse("Nothing was opened, so nothing may be committed.", events.contains("tx_commit"));
		assertFalse("The scope must have stayed untouched.", scope.isActive());
	}

	/**
	 * An {@link ToolMode#EXTERNAL} tool can be pre-authorised for an unattended run through the
	 * existing tool-access configuration, and runs without anybody being asked.
	 *
	 * <p>
	 * The full chain, because the point of the acceptance criterion is that no new mechanism was
	 * needed: the policy grants {@code egress} to a role and names it in the
	 * {@code <background-allowlist>}; the run is built as a background run, which is what
	 * {@code AiToolAccessService} does for a scheduler job, an MCP client or an
	 * {@code aiAgentChatAsync} call; the loop's own approval callback is
	 * {@link ApprovalCallback#AUTO_APPROVE}, which is what an unattended run carries. The tool then
	 * executes - asserted by its own side effect, not by a decision object.
	 * </p>
	 *
	 * <p>
	 * So {@code EXTERNAL} means "ask a human by default", not "unusable unattended". The
	 * administrator's two statements - the role grant and the allowlist entry - are what replaces
	 * the human.
	 * </p>
	 */
	public void testExternalToolPreAuthorisedForBackgroundRun() {
		ToolAccessPolicy policy = egressPolicy();
		allowInBackground(policy, ToolGroups.EGRESS);

		Conversation conv = new Conversation();
		AtomicInteger calls = new AtomicInteger();
		AgentResult result = runExternalTool(policy, conv, calls);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("The allowlisted external tool must actually have run.", 1, calls.get());
		assertNull("Nothing may have failed: " + firstToolFailure(conv), firstToolFailure(conv));
	}

	/**
	 * The same run without the allowlist entry: the role grant alone does not reach an unattended
	 * run.
	 *
	 * <p>
	 * The negative half, so that the positive one cannot pass because the ceiling is not applied at
	 * all. The message has to send the administrator to the control that decided, which is why it is
	 * asserted and not just the refusal.
	 * </p>
	 */
	public void testExternalToolDeniedInBackgroundRunWithoutAllowlist() {
		ToolAccessPolicy policy = egressPolicy();
		allowInBackground(policy);

		Conversation conv = new Conversation();
		AtomicInteger calls = new AtomicInteger();
		AgentResult result = runExternalTool(policy, conv, calls);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("Nothing may have run.", 0, calls.get());
		String failure = firstToolFailure(conv);
		assertNotNull("The model must be told why the call did not happen.", failure);
		assertTrue("The refusal must name the control that decided, but was: " + failure,
			failure.contains("background allowlist"));
	}

	/** The error of the first failed tool result in the conversation, or <code>null</code>. */
	private static String firstToolFailure(Conversation conv) {
		for (Message message : conv.messages()) {
			if (message instanceof ToolResultMessage toolResult
					&& toolResult.state() instanceof ToolState.Failed failed) {
				return failed.error();
			}
		}
		return null;
	}

	/**
	 * Runs one turn that calls a single {@link ToolMode#EXTERNAL} tool in the {@code egress} group,
	 * as an unattended run authorized against the given policy.
	 *
	 * @param conv
	 *        The conversation to run, filled with the user turn here; the caller keeps it in order
	 *        to inspect the tool results afterwards.
	 * @param calls
	 *        Incremented by the tool when it actually executes.
	 */
	private static AgentResult runExternalTool(ToolAccessPolicy policy, Conversation conv,
			AtomicInteger calls) {
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("File it"))));

		ToolSchema toolSpec = new ToolSchema("createGiteaIssue", "Files an issue", "{}");
		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "createGiteaIssue", "{}")),
			List.of(new TextContent("Filed.")));

		ToolCatalog registry = new ToolCatalog();
		registry.register(ToolStubs.builder("createGiteaIssue").description("Files an issue").schema(toolSpec)
			.mode(ToolMode.EXTERNAL).group(ToolGroups.EGRESS).concurrencySafe(false).execute((args, ctx) -> {
				calls.incrementAndGet();
				return ToolResult.of("Issue #1 created.");
			}).build());
		registry.freeze();

		// A background run: no human, so the policy's background allowlist is the ceiling, and the
		// callback auto-approves whatever survives it.
		ToolAuthorization unattended = new ToolAuthorization(
			new RoleToolAccess(policy, DeclaredGroups.NONE), null, Set.of("Reviewer"), true);
		ToolContext ctx = new ToolContext(TopLogicSecurityContext.enforcedRun(unattended),
			ApprovalCallback.AUTO_APPROVE, new TestableTransactionScope());

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		return loop.run(conv, ctx, e -> {
			// The decisions are asserted on the conversation and the tool's own side effect.
		});
	}

	/** A policy whose only role holds {@link ToolGroups#EGRESS} and nothing else. */
	private static ToolAccessPolicy egressPolicy() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName("Reviewer");
		role.getGrants().add(groupGrant(ToolGroups.EGRESS));
		policy.getToolAccess().put(role.getName(), role);
		// An explicitly empty baseline, so that a grant this test does not make cannot come from
		// RoleToolAccess.DEFAULT_BASELINE.
		policy.setBaseline(TypedConfiguration.newConfigItem(ToolAccessPolicy.Baseline.class));
		return policy;
	}

	/** Declares the policy's background allowlist with the given capability groups. */
	private static void allowInBackground(ToolAccessPolicy policy, String... groups) {
		ToolAccessPolicy.BackgroundAllowlist allowlist =
			TypedConfiguration.newConfigItem(ToolAccessPolicy.BackgroundAllowlist.class);
		for (String group : groups) {
			allowlist.getGrants().add(groupGrant(group));
		}
		policy.setBackgroundAllowlist(allowlist);
	}

	private static ToolAccessPolicy.Grant groupGrant(String group) {
		ToolAccessPolicy.Grant grant = TypedConfiguration.newConfigItem(ToolAccessPolicy.Grant.class);
		grant.setGroup(group);
		return grant;
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
		registry.register(ToolStubs.builder("edit_model").description("Edits").schema(toolSpec)
			.mode(ToolMode.STRUCTURAL).execute((args, context) -> {
				throw new RuntimeException("Something went wrong");
			}).build());
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
	 * A failed commit must correct the picture the model was left with, in the very same provider
	 * request that first carries the tool results it corrects.
	 *
	 * <p>
	 * The bug this pins: the tool of the turn reported {@code COMPLETED} before the turn's SQL ever
	 * ran, the commit then failed, and the model was told nothing - so it answered the user that the
	 * write had happened. See {@link TestCommitFailureReason} for the engine-level failure this
	 * simulates.
	 * </p>
	 */
	public void testCommitFailureTellsTheModel() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Create a document"))));

		ToolSchema toolSpec = new ToolSchema("edit_model", "Edits", "{}");
		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", toolSpec));
		registry.freeze();

		List<List<Message>> requests = new ArrayList<>();
		LLMProvider provider = recording(requests, mockProvider(
			List.of(new ToolCall("tc1", "edit_model", "{}")),
			List.of(new TextContent("Created."))));

		TransactionScope failingCommitScope = new TestableTransactionScope() {
			@Override
			public void commit() {
				throw new TransactionException(
					TransactionScope.COMMIT_FAILED_PREFIX + "Database operation failed.");
			}
		};
		ToolContext ctx = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE,
			failingCommitScope);

		AgentLoop loop = new AgentLoop(config(), provider, registry);
		AgentResult result = loop.run(conv, ctx, listener -> {
			// no listener assertions here
		});

		assertEquals("A failed commit is not a failed run: the model still has to answer.",
			StopReason.COMPLETE, result.stopReason());

		String notice = frameworkNotice(conv);
		assertNotNull("The model must be told that the writes of the turn are gone.", notice);
		assertTrue("The notice must name the reason, but was: " + notice,
			notice.contains("Database operation failed."));
		assertTrue("The notice must say that a result reported as successful is void, but was: "
			+ notice, notice.contains("successful"));

		assertEquals("The loop must ask the provider again after the failed commit.",
			2, requests.size());
		assertTrue("The correction must reach the model in the same request as the tool result it"
			+ " corrects - otherwise the model has already formed the false belief.",
			containsNotice(requests.get(1)));
	}

	/**
	 * A commit failure that is not a {@link TransactionException} must still end the turn, not the
	 * run.
	 *
	 * <p>
	 * Issue #410. The engine does not wrap every way a commit can fail: a model constraint, a
	 * mandatory attribute left unset or an application {@code CommitChecker} arrives as a
	 * {@link TopLogicException}, which is neither a {@code KnowledgeBaseException} nor - before this
	 * was fixed - anything {@link TransactionScope#commit()} caught. Such a failure escaped the
	 * whole run and was announced to the browser as a provider error, which told the user the model
	 * had failed and to send again. {@link TransactionScope} now wraps it; this test pins the second
	 * gate, so that a failure family the engine adds later cannot reopen the same hole.
	 * </p>
	 */
	public void testEngineCommitFailureIsNotAProviderError() {
		Conversation conv = new Conversation();
		List<AgentEvent> events = new ArrayList<>();

		// A TopLogicException is exactly what ModelService.checkCommit throws for a constraint
		// violation, and the engine rethrows it unwrapped - it is no KnowledgeBaseException.
		AgentResult result = runRefusedWrite(conv, vetoingScope(
			() -> new TopLogicException(ResKey.text("Cannot store, constraints violated."))),
			events::add);

		assertEquals("A refused write is not a failed run.", StopReason.COMPLETE,
			result.stopReason());
		assertTrue("A refused commit is not a provider failure and must not be reported as one.",
			events.stream().noneMatch(AgentEvent.ProviderError.class::isInstance));
		assertTrue("Nothing was persisted, so no commit may be reported.",
			events.stream().noneMatch(AgentEvent.TransactionCommitted.class::isInstance));

		AgentEvent.TransactionRolledBack rollback = events.stream()
			.filter(AgentEvent.TransactionRolledBack.class::isInstance)
			.map(AgentEvent.TransactionRolledBack.class::cast)
			.findFirst().orElse(null);
		assertNotNull("The turn must be rolled back, and say so.", rollback);
		assertTrue("The rollback must name why the write was refused, but was: " + rollback.reason(),
			rollback.reason().contains("constraints violated"));

		String notice = frameworkNotice(conv);
		assertNotNull("The model must be told that the write of the turn is gone.", notice);
		assertTrue("The notice must name the refusal, but was: " + notice,
			notice.contains("constraints violated"));
	}

	/**
	 * A refused commit leaves a server-side ERROR line naming the run and the cause.
	 *
	 * <p>
	 * The half of issue #410 that cost the most time: the deployment that hit this found nothing at
	 * all in its log, so the only way to the real cause was to reproduce it with a debugger
	 * attached. The line has to carry the run id, because that is what joins it to the persisted
	 * {@code AgentExecution} row, and the exception, because the reason is in there.
	 * </p>
	 */
	public void testRefusedCommitIsLoggedWithTheRun() {
		Conversation conv = new Conversation();
		List<AgentEvent> events = new ArrayList<>();
		TransactionScope scope = vetoingScope(
			() -> new TransactionException(TransactionScope.COMMIT_FAILED_PREFIX
				+ "Cannot store, constraints violated."));

		CollectingLogListener log =
			new CollectingLogListener(Collections.singleton(Level.ERROR), true);
		List<LogEntry> entries;
		try {
			runRefusedWrite(conv, scope, events::add);
		} finally {
			entries = log.getLogEntries();
			log.deactivate();
		}

		String runId = events.stream()
			.filter(AgentEvent.RunStarted.class::isInstance)
			.map(e -> ((AgentEvent.RunStarted) e).runId())
			.findFirst().orElse(null);
		assertNotNull("The run must have started.", runId);

		LogEntry reported = entries.stream()
			.filter(entry -> entry.getMessage() != null && entry.getMessage().contains(runId))
			.findFirst().orElse(null);
		assertNotNull("A failed commit must leave a line naming its run, but the log held: "
			+ entries, reported);
		assertTrue("The line must say what failed, but was: " + reported.getMessage(),
			reported.getMessage().contains("could not be committed"));
		assertTrue("The cause is the point of the line: it must carry the exception.",
			reported.getException().hasValue());
		assertTrue("The reason must be readable from the logged cause, but was: "
			+ reported.getException().get().getMessage(),
			reported.getException().get().getMessage().contains("constraints violated"));
	}

	/**
	 * A refused commit reaches the persisted transcript as a retraction, not as a green write.
	 *
	 * <p>
	 * The fourth thing issue #410 cost: because the veto escaped the run, no
	 * {@code TransactionRolledBack} was ever emitted, so the recorded turn kept a {@code COMPLETED}
	 * write for a change no revision holds. A transcript reloaded from the database then fed the
	 * model that success again. The retraction itself is
	 * {@link com.top_logic.tl.ai.core.session.ConversationLogListener}'s, and tested there; what is
	 * pinned here is that the loop still emits the event it needs for it.
	 * </p>
	 */
	public void testRefusedCommitIsRecordedAsRolledBack() {
		ConversationLog log = new ConversationLog();
		log.startTurn();

		runRefusedWrite(new Conversation(), vetoingScope(
			() -> new TopLogicException(ResKey.text("Cannot store, constraints violated."))),
			new ConversationLogListener(log));

		// The run closed the turn on its way out, so it is in the finished ones.
		List<Turn> turns = log.turns();
		assertFalse("The run must have recorded a turn.", turns.isEmpty());
		Part.ToolCallPart call =
			(Part.ToolCallPart) turns.get(turns.size() - 1).findPart("tc1");
		assertNotNull("The write must be in the transcript.", call);
		assertEquals("A write whose turn was refused must not stay COMPLETED.",
			Part.ToolCallStatus.ROLLED_BACK, call.status());
		assertTrue("The retraction must carry why, but was: " + call.errorMessage(),
			call.errorMessage().contains("constraints violated"));
	}

	/**
	 * Runs one turn in which a write tool succeeds and the turn's commit is then refused.
	 *
	 * <p>
	 * The shape every case of issue #410 needs: the model calls one {@link ToolMode#STRUCTURAL}
	 * tool, the tool reports success, the commit of the turn fails, and the run is expected to carry
	 * on and answer.
	 * </p>
	 *
	 * @param conversation
	 *        Receives the user message, the tool result and the framework notice; pass a fresh one
	 *        unless the caller wants to read it afterwards.
	 */
	private AgentResult runRefusedWrite(Conversation conversation, TransactionScope scope,
			AgentEventListener listener) {
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Create an invoice"))));

		ToolCatalog registry = new ToolCatalog();
		registry.register(globalTool("edit_model", new ToolSchema("edit_model", "Edits", "{}")));
		registry.freeze();

		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "edit_model", "{}")),
			List.of(new TextContent("Created.")));
		ToolContext context =
			new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE, scope);

		return new AgentLoop(config(), provider, registry).run(conversation, context, listener);
	}

	/** A scope whose commit is refused with the given failure, leaving rollback possible. */
	private static TransactionScope vetoingScope(Supplier<RuntimeException> failure) {
		return new TestableTransactionScope() {
			@Override
			public void commit() {
				throw failure.get();
			}
		};
	}

	/**
	 * Whether the message history a provider request carried contains the {@code [Framework notice]}
	 * correction.
	 */
	private static boolean containsNotice(List<Message> messages) {
		for (Message message : messages) {
			if (message instanceof UserMessage user) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent text
						&& text.text().startsWith("[Framework notice]")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Wraps a provider so that the message history of every request is recorded, letting a test
	 * assert what the model was shown and in which round-trip.
	 */
	private static LLMProvider recording(List<List<Message>> requests, LLMProvider delegate) {
		return (request, handler) -> {
			requests.add(List.copyOf(request.messages()));
			delegate.stream(request, handler);
		};
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
		registry.register(ToolStubs.builder("slow_tool").description("Reports progress").schema(toolSpec)
			.execute((args, context) -> {
				ToolContext toolContext = (ToolContext) context;
				toolContext.progressReporter().progress("step 1");
				toolContext.progressReporter().progress("step 2");
				return ToolResult.of("completed");
			}).build());
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
		return ToolStubs.builder(toolName).description(toolName).schema(new ToolSchema(toolName, toolName, "{}"))
			.mode(mode).concurrencySafe(concurrencySafe).build();
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
			ToolStubs.builder("failing_tool").description("Fails").schema(failSpec).mode(ToolMode.STRUCTURAL)
				.execute((args, context) -> {
					throw new RuntimeException("boom");
			}).build());

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

	/**
	 * A run the user cancelled mid-stream ends as a cancel, not as a model error.
	 *
	 * <p>
	 * Issue #549. Cancelling closes the provider's response stream, and the provider reports the
	 * closed stream as {@code ProviderException(UNKNOWN, "Stream cancelled.")} - which the loop
	 * passed on as a {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ProviderError} ahead of
	 * the {@link StopReason#ABORTED} completion. The chat drew it as a red "the model reported an
	 * error" block with the English provider sentence, and the transcript persisted it as an
	 * {@link com.top_logic.tl.ai.core.session.Part.ErrorPart}, so a reload or a second window
	 * showed it again. Nothing failed: the user asked for the stop.
	 * </p>
	 */
	public void testCancelledStreamIsNotAProviderError() {
		ConversationMemory memory = new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), new TruncatingStrategy(2), 100_000, 1_000);
		memory.conversation().add(new UserMessage("u1", T0, List.of(new TextContent("Count slowly."))));

		CancellationToken token = new CancellationToken();
		// What AbstractStreamingProvider does when the cancel closes its stream mid-answer.
		LLMProvider cancelledMidStream = (request, handler) -> {
			handler.onEvent(new StreamEvent.TextDelta(0, "One, two, "));
			token.cancel();
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0, "Stream cancelled.");
		};

		ConversationLog log = new ConversationLog();
		log.startTurn();
		ConversationLogListener logListener = new ConversationLogListener(log);
		List<AgentEvent> events = new ArrayList<>();
		AgentEventListener listener = e -> {
			events.add(e);
			logListener.onEvent(e);
		};

		AgentLoop loop = new AgentLoop(config(), cancelledMidStream, emptyRegistry(), memory);
		AgentResult result = loop.run(new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, token), listener);

		assertEquals(StopReason.ABORTED, result.stopReason());
		assertNull("A cancel is no error, so the result names none.", result.errorKind());
		assertNull("A cancel is no error, so the result names none.", result.errorMessage());
		assertTrue("A cancel the user asked for must not be announced as a provider error.",
			events.stream().noneMatch(AgentEvent.ProviderError.class::isInstance));
		AgentEvent.RunCompleted completed = AgentEventAssertions.firstOfType(events, AgentEvent.RunCompleted.class);
		assertEquals(StopReason.ABORTED, completed.reason());

		List<Turn> turns = log.turns();
		List<Part> parts = turns.get(turns.size() - 1).assistantParts();
		assertTrue("The transcript must not record the cancel as an error, but was: " + parts,
			parts.stream().noneMatch(Part.ErrorPart.class::isInstance));
		assertTrue("What was streamed before the cancel stays.",
			parts.get(0) instanceof Part.TextPart text && text.text().equals("One, two, "));
		Part last = parts.get(parts.size() - 1);
		assertTrue("The turn must end with the cancel, after what was streamed, but was: " + parts,
			last instanceof Part.SystemPart system && system.eventType() == Part.SystemEventType.CANCELLED);
		assertEquals("The cancel is recorded once.", 1, parts.stream()
			.filter(p -> p instanceof Part.SystemPart system && system.eventType() == Part.SystemEventType.CANCELLED)
			.count());
	}

	/**
	 * The loop without memory classifies a cancelled stream the same way.
	 */
	public void testCancelledStreamIsNotAProviderErrorWithoutMemory() {
		Conversation conv = new Conversation();
		conv.add(new UserMessage("u1", T0, List.of(new TextContent("Count slowly."))));

		CancellationToken token = new CancellationToken();
		LLMProvider cancelledMidStream = (request, handler) -> {
			token.cancel();
			throw new ProviderException(ProviderException.Kind.UNKNOWN, 0, "Stream cancelled.");
		};

		List<AgentEvent> events = new ArrayList<>();
		AgentLoop loop = new AgentLoop(config(), cancelledMidStream, emptyRegistry());
		AgentResult result = loop.run(conv, new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, token), events::add);

		assertEquals(StopReason.ABORTED, result.stopReason());
		assertNull(result.errorKind());
		assertTrue("A cancel the user asked for must not be announced as a provider error.",
			events.stream().noneMatch(AgentEvent.ProviderError.class::isInstance));
	}
}
