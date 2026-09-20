/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.limits.TokenAllowance;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A run that spends the last of its principal's budget stops between turns.
 *
 * <p>
 * The loop half of Gitea #343. A run whose budget was already spent does not get here at all - it is
 * refused where every run enters - so what is left to decide is what happens to a run that is
 * <em>working</em> when the ceiling is reached. It finishes the turn it has begun and stops there,
 * with {@link StopReason#BUDGET_EXHAUSTED} and everything it had answered.
 * </p>
 *
 * <p>
 * The model below never stops by itself: it asks for the same tool every turn, so the only thing
 * that can end these runs is the budget or {@code maxTurns}, and which of the two it was is what
 * every test here asserts.
 * </p>
 */
public class TestBudgetStopsTheRun extends TestCase {

	/** Tool execution runs in a system interaction, so the thread context has to be up. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestBudgetStopsTheRun.class)));
	}

	private static final String MODEL = "test-model";

	/** What one turn of the model below costs. */
	private static final int TOKENS_PER_TURN = 100;

	private static final ToolSchema LOOK = new ToolSchema("look", "Looks.", "{\"type\":\"object\"}");

	/** Three turns' worth of budget ends the run after the third turn. */
	public void testTheRunStopsWhenTheBudgetIsSpent() {
		List<AgentEvent> events = run(TokenAllowance.of(3 * TOKENS_PER_TURN), 10);

		AgentEvent.RunCompleted completed = completion(events);
		assertEquals(StopReason.BUDGET_EXHAUSTED, completed.reason());
		assertEquals("The turn that spent the last of it is finished, not abandoned.",
			3, completed.turnNumber());
		assertEquals("Three turns ran, and the fourth was never started.", 3, turnsStarted(events));
	}

	/** Every turn it did run is a whole turn, reported as completed. */
	public void testTheLastTurnIsFinished() {
		List<AgentEvent> events = run(TokenAllowance.of(2 * TOKENS_PER_TURN), 10);

		assertEquals(2, turnsStarted(events));
		assertEquals("A turn that started is a turn that completed; the budget is checked between.",
			2, count(events, AgentEvent.TurnCompleted.class));
		assertEquals(StopReason.BUDGET_EXHAUSTED, completion(events).reason());
	}

	/** An admitted run always gets one turn, however little is left. */
	public void testAnAdmittedRunAlwaysGetsOneTurn() {
		List<AgentEvent> events = run(TokenAllowance.of(1), 10);

		assertEquals("Refusing before the first turn is the entry point's job, not the loop's.",
			1, turnsStarted(events));
		assertEquals(StopReason.BUDGET_EXHAUSTED, completion(events).reason());
		assertEquals(1, completion(events).turnNumber());
	}

	/** Without a budget the run ends the way it always did. */
	public void testAnUnlimitedRunIsUnaffected() {
		List<AgentEvent> events = run(TokenAllowance.UNLIMITED, 3);

		assertEquals(StopReason.MAX_TURNS, completion(events).reason());
		assertEquals(3, turnsStarted(events));
	}

	/** The same holds for the memory-backed loop, which is the one the chat runs. */
	public void testTheMemoryBackedLoopStopsToo() {
		ConversationMemory memory = new ConversationMemory(seeded(), new ByteBasedTokenEstimator(),
			new TruncatingStrategy(4), 1_000_000, 900_000);
		List<AgentEvent> events = new ArrayList<>();
		new AgentLoop(params(10), provider(), catalog(), memory)
			.run(context(TokenAllowance.of(2 * TOKENS_PER_TURN)), events::add);

		assertEquals(StopReason.BUDGET_EXHAUSTED, completion(events).reason());
		assertEquals(2, turnsStarted(events));
	}

	/** What a delegate spends counts against the same budget, because it shares the allowance. */
	public void testADelegatesSpendCountsAgainstTheParent() {
		TokenAllowance allowance = TokenAllowance.of(3 * TOKENS_PER_TURN);
		// Stand in for a child run: it holds the parent's allowance and records into it.
		ToolContext.unrestricted().withTokenAllowance(allowance).forDelegation(TransactionScope.NOOP)
			.tokenAllowance().record(new TokenUsage(2 * TOKENS_PER_TURN, 0, 0, 0, 0));

		List<AgentEvent> events = run(allowance, 10);

		assertEquals(StopReason.BUDGET_EXHAUSTED, completion(events).reason());
		assertEquals("Only one turn was left after the delegate's spend.", 1, turnsStarted(events));
	}

	private static List<AgentEvent> run(TokenAllowance allowance, int maxTurns) {
		List<AgentEvent> events = new ArrayList<>();
		AgentEventListener capture = events::add;
		new AgentLoop(params(maxTurns), provider(), catalog())
			.run(seeded(), context(allowance), capture);
		return events;
	}

	private static ToolContext context(TokenAllowance allowance) {
		return ToolContext.unrestricted().withTokenAllowance(allowance);
	}

	private static Conversation seeded() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(java.util.UUID.randomUUID().toString(), java.time.Instant.now(),
			List.of(new TextContent("Keep going."))));
		return conversation;
	}

	private static AgentParams params(int maxTurns) {
		return new AgentParams(MODEL, "You are a test assistant.", maxTurns,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	/** A model that never finishes: every turn asks for the tool again, and costs the same. */
	private static LLMProvider provider() {
		return (request, handler) -> handler.onEvent(new StreamEvent.MessageComplete(
			"msg", MODEL, "tool_use", new TokenUsage(TOKENS_PER_TURN, 0, 0, 0, 0),
			List.of(new ToolCall("call-" + request.messages().size(), LOOK.name(), "{}"))));
	}

	private static ToolCatalog catalog() {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(ToolStubs.builder(LOOK.name()).description(LOOK.description()).schema(LOOK).build());
		catalog.freeze();
		return catalog;
	}

	private static AgentEvent.RunCompleted completion(List<AgentEvent> events) {
		for (AgentEvent event : events) {
			if (event instanceof AgentEvent.RunCompleted completed) {
				return completed;
			}
		}
		fail("The run emitted no completion event.");
		return null;
	}

	private static int turnsStarted(List<AgentEvent> events) {
		return count(events, AgentEvent.TurnStarted.class);
	}

	private static int count(List<AgentEvent> events, Class<? extends AgentEvent> type) {
		int n = 0;
		for (AgentEvent event : events) {
			if (type.isInstance(event)) {
				n++;
			}
		}
		return n;
	}
}
