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
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A run that reaches its turn limit ends with an answer: one last turn
 * offers no tool, tells the model the limit is reached, and the run ends with the limit as its stop
 * reason (#775).
 *
 * <p>
 * Before, a run at {@code max-turns} executed that turn's tool calls and stopped - with a tool result
 * as its last message and no answer, so a subagent reported nothing to its lead.
 * </p>
 */
public class TestRunLimitFinalTurn extends TestCase {

	private static final Instant T0 = Instant.parse("2026-10-11T08:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private static final String MODEL = "test-model";

	private static final String FINAL = "Final report: what I found so far.";

	/** The calls each tool ran, by tool name. */
	private final Map<String, AtomicInteger> _runs = Map.of("fetch", new AtomicInteger());

	/** What every request of the run looked like. */
	private final List<ProviderRequest> _requests = new ArrayList<>();

	/**
	 * A model that never stops calling tools ends at its turn limit with an answer, written in a turn
	 * that offered the tools but allowed none.
	 */
	public void testTheLastAllowedTurnIsAnAnswer() {
		Conversation conversation = question();
		AgentLoop loop = new AgentLoop(config(3), provider(request -> List.of(call("fetch"))), registry());
		List<AgentEvent.FinalTurnRequested> announced = new ArrayList<>();

		AgentResult result = loop.run(conversation, event -> {
			if (event instanceof AgentEvent.FinalTurnRequested finalTurn) {
				announced.add(finalTurn);
			}
		});

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals(3, result.turnsUsed());
		assertEquals("The run's own log hears of the notice, as an event.", 1, announced.size());
		assertEquals(3, announced.get(0).turnNumber());
		assertEquals("Turns 1 and 2 work, turn 3 answers.", 2, _runs.get("fetch").get());
		assertEquals(FINAL, lastText(conversation));
		ProviderRequest last = _requests.get(2);
		assertEquals(ToolChoice.NONE, last.toolChoice());
		assertFalse("The tools stay in the request, so the cached prefix stays the same.", last.tools().isEmpty());
		assertTrue(lastUserText(last), lastUserText(last).contains("limit of 3 turns"));
		assertNotSame(ToolChoice.NONE, _requests.get(1).toolChoice());
	}

	/** The same through the memory-backed loop a chat and a subagent run on. */
	public void testTheLastAllowedTurnIsAnAnswerWithMemory() {
		Conversation conversation = question();
		AgentLoop loop = new AgentLoop(config(3), provider(request -> List.of(call("fetch"))), registry());

		AgentResult result = loop.run(conversation, new TruncatingStrategy(), ToolContext.unrestricted(), silent());

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals(2, _runs.get("fetch").get());
		assertEquals(FINAL, lastText(conversation));
	}

	/**
	 * A provider that calls a tool although none may be called: the call is dropped, nothing runs it,
	 * and the conversation holds no call without a result.
	 */
	public void testACallInTheLastTurnIsDropped() {
		Conversation conversation = question();
		AgentLoop loop = new AgentLoop(config(2),
			(request, handler) -> {
				_requests.add(request);
				emit(handler, List.of(new TextContent(FINAL), call("fetch")));
			}, registry());

		AgentResult result = loop.run(conversation, silent());

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals("Only the first turn's call ran.", 1, _runs.get("fetch").get());
		AssistantMessage last = (AssistantMessage) conversation.messages().get(conversation.messages().size() - 1);
		assertTrue(last.content().stream().noneMatch(ToolCall.class::isInstance));
		assertEquals(FINAL, lastText(conversation));
	}

	/**
	 * A provider that ignores the request not to call tools and answers the last turn with tool calls
	 * only: the run asks once more for the answer in text, instead of ending with nothing (#819).
	 */
	public void testALastReplyOfToolCallsOnlyIsAskedOnceMore() {
		Conversation conversation = question();
		AtomicInteger finalRequests = new AtomicInteger();
		AgentLoop loop = new AgentLoop(config(3), (request, handler) -> {
			_requests.add(request);
			if (request.toolChoice() == ToolChoice.NONE && finalRequests.incrementAndGet() > 1) {
				emit(handler, List.of(new TextContent(FINAL)));
			} else {
				emit(handler, List.of(call("fetch")));
			}
		}, registry());
		AgentResult result = loop.run(conversation, silent());

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals("Asked once more, with no tool callable again.", 2, finalRequests.get());
		assertEquals(2, _runs.get("fetch").get());
		ProviderRequest retry = _requests.get(_requests.size() - 1);
		assertEquals(ToolChoice.NONE, retry.toolChoice());
		assertTrue(lastUserText(retry), lastUserText(retry).contains("contained no text"));
		assertEquals(FINAL, lastText(conversation));
		assertTrue("No call without a result stays in the conversation.", conversation.messages().stream()
			.filter(AssistantMessage.class::isInstance).skip(2)
			.allMatch(m -> ((AssistantMessage) m).content().stream().noneMatch(ToolCall.class::isInstance)));
	}

	/** A model that answers the retry with tool calls only again: the run ends, it does not loop. */
	public void testTheLastReplyIsAskedForOnlyOnce() {
		Conversation conversation = question();
		AgentLoop loop = new AgentLoop(config(3), (request, handler) -> {
			_requests.add(request);
			emit(handler, List.of(call("fetch")));
		}, registry());

		AgentResult result = loop.run(conversation, new TruncatingStrategy(), ToolContext.unrestricted(), silent());

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals("Two working turns, the last turn, and one retry.", 4, _requests.size());
		assertEquals(2, _runs.get("fetch").get());
	}

	/** A run limited to one turn has no turn to spare and works in it, as it always did. */
	public void testAOneTurnRunWorksInItsTurn() {
		Conversation conversation = question();
		AgentLoop loop = new AgentLoop(config(1), provider(request -> List.of(call("fetch"))), registry());

		AgentResult result = loop.run(conversation, silent());

		assertEquals(StopReason.MAX_TURNS, result.stopReason());
		assertEquals(1, _runs.get("fetch").get());
		assertNotSame(ToolChoice.NONE, _requests.get(0).toolChoice());
	}

	private ToolCatalog registry() {
		ToolCatalog result = new ToolCatalog();
		result.register(ToolStubs.builder("fetch").description("Reads a page.")
			.schema(new ToolSchema("fetch", "Reads a page.", "{}"))
			.execute((args, context) -> {
				_runs.get("fetch").incrementAndGet();
				return ToolResult.of("page text");
			}).build());
		result.freeze();
		return result;
	}

	/** A model that answers {@link #FINAL} when it may call no tool, and what the script says otherwise. */
	private LLMProvider provider(Function<ProviderRequest, List<ContentPart>> script) {
		return (request, handler) -> {
			_requests.add(request);
			emit(handler, request.toolChoice() == ToolChoice.NONE ? List.of(new TextContent(FINAL)) : script.apply(request));
		};
	}

	private static void emit(com.top_logic.tl.ai.core.provider.StreamHandler handler, List<ContentPart> content) {
		for (int i = 0; i < content.size(); i++) {
			if (content.get(i) instanceof TextContent text) {
				handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
			} else if (content.get(i) instanceof ToolCall tc) {
				handler.onEvent(new StreamEvent.ToolCallStart(i, tc.id(), tc.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(i, tc.arguments()));
			}
		}
		boolean calls = content.stream().anyMatch(ToolCall.class::isInstance);
		handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, calls ? "tool_use" : "end_turn", USAGE, content));
	}

	private static final AtomicInteger IDS = new AtomicInteger();

	/** A call of the tool, with arguments of its own, so that the duplicate-call check never applies. */
	private static ToolCall call(String tool) {
		int id = IDS.incrementAndGet();
		return new ToolCall("call-" + id, tool, "{\"n\": " + id + "}");
	}

	private static AgentParams config(int maxTurns) {
		return new AgentParams(MODEL, "You are a test assistant.", maxTurns, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	private static Conversation question() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Research this."))));
		return conversation;
	}

	private static AgentEventListener silent() {
		return event -> {
			// Only the outcome matters here.
		};
	}

	private static String lastText(Conversation conversation) {
		List<Message> messages = conversation.messages();
		Message last = messages.get(messages.size() - 1);
		assertTrue("The run ends with an answer, not with " + last, last instanceof AssistantMessage);
		StringBuilder text = new StringBuilder();
		for (ContentPart part : ((AssistantMessage) last).content()) {
			if (part instanceof TextContent content) {
				text.append(content.text());
			}
		}
		return text.toString();
	}

	private static String lastUserText(ProviderRequest request) {
		List<Message> messages = request.messages();
		for (int n = messages.size() - 1; n >= 0; n--) {
			if (messages.get(n) instanceof UserMessage user) {
				return String.valueOf(user.content());
			}
		}
		return "";
	}

	/**
	 * The suite, in a thread context: tool calls run in a TopLogic interaction.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRunLimitFinalTurn.class)));
	}
}
