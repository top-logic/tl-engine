/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A run that reaches its turn limit or its budget of tool calls ends with an answer: one last turn
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
	private final Map<String, AtomicInteger> _runs = Map.of("fetch", new AtomicInteger(), "note", new AtomicInteger());

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
		assertEquals(StopReason.MAX_TURNS, announced.get(0).limit());
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
	 * A budget of three tool calls: the call beyond it is answered without running, the next turn
	 * is the answer, and the run says which limit it hit.
	 */
	public void testABudgetOfToolCalls() {
		Conversation conversation = question();
		AgentLoop loop = new AgentLoop(config(20), provider(request -> List.of(call("fetch"), call("fetch"))),
			registry());
		loop.limitToolCalls(3);

		AgentResult result = loop.run(conversation, silent());

		assertEquals(StopReason.MAX_TOOL_CALLS, result.stopReason());
		assertEquals("Two calls in turn 1, one of two in turn 2, the answer in turn 3.", 3, result.turnsUsed());
		assertEquals(3, _runs.get("fetch").get());
		List<String> notRun = results(conversation).stream()
			.filter(r -> r.state() instanceof ToolState.Failed failed && failed.error().startsWith("Not run"))
			.map(ToolResultMessage::toolCallId).toList();
		assertEquals("The call beyond the budget has its result, without having run.", 1, notRun.size());
		assertEquals(FINAL, lastText(conversation));
		assertTrue(lastUserText(_requests.get(2)), lastUserText(_requests.get(2)).contains("budget of 3 tool calls"));
	}

	/** Asking, loading a skill and recording a finding are interaction tools and spend nothing. */
	public void testInteractionToolsSpendNoBudget() {
		Conversation conversation = question();
		AtomicInteger turn = new AtomicInteger();
		AgentLoop loop = new AgentLoop(config(20), provider(request -> switch (turn.incrementAndGet()) {
			case 1 -> List.of(call("note"), call("note"), call("fetch"));
			case 2 -> List.of(call("note"), call("fetch"));
			default -> List.of(call("note"));
		}), registry());
		loop.limitToolCalls(2);

		AgentResult result = loop.run(conversation, silent());

		assertEquals(StopReason.MAX_TOOL_CALLS, result.stopReason());
		assertEquals(2, _runs.get("fetch").get());
		assertEquals(3, _runs.get("note").get());
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

	/** A limit can be narrowed, never widened, and a run of one turn keeps it. */
	public void testALimitOnlyNarrows() {
		AgentLoop loop = new AgentLoop(config(5), provider(request -> List.of(call("fetch"))), registry());
		assertEquals(0, loop.toolCallLimit());
		loop.limitToolCalls(25);
		loop.limitToolCalls(30);
		assertEquals(25, loop.toolCallLimit());
		loop.limitToolCalls(10);
		loop.limitToolCalls(0);
		assertEquals(10, loop.toolCallLimit());
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
		result.register(ToolStubs.builder("note").description("Notes a finding.")
			.schema(new ToolSchema("note", "Notes a finding.", "{}")).group(ToolGroups.INTERACTION)
			.execute((args, context) -> {
				_runs.get("note").incrementAndGet();
				return ToolResult.of("noted");
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

	private static List<ToolResultMessage> results(Conversation conversation) {
		List<ToolResultMessage> result = new ArrayList<>();
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage toolResult) {
				result.add(toolResult);
			}
		}
		return result;
	}

	/**
	 * The suite, in a thread context: tool calls run in a TopLogic interaction.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRunLimitFinalTurn.class)));
	}
}
