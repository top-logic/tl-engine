/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.ai.service.agents.background.context.ApplicationContext;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.context.ChatContext;

import junit.framework.TestCase;

/**
 * Tests for the {@link BackgroundContext} hierarchy:
 * {@link BackgroundContext}, {@link ChatContext}, and {@link ApplicationContext}.
 */
public class TestBackgroundContext extends TestCase {

	// --- BackgroundContext ---

	/** Base context stores agent name and trigger type. */
	public void testBackgroundContextFields() {
		BackgroundContext ctx = new BackgroundContext("TestAgent", "post-turn");

		assertEquals("TestAgent", ctx.getAgentName());
		assertEquals("post-turn", ctx.getTriggerType());
		assertNotNull(ctx.getTimestamp());
	}

	/** Timestamp is set to approximately now. */
	public void testBackgroundContextTimestampIsRecent() {
		long before = System.currentTimeMillis();
		BackgroundContext ctx = new BackgroundContext("agent", "trigger");
		long after = System.currentTimeMillis();

		long ts = ctx.getTimestamp().toEpochMilli();
		assertTrue("Timestamp should be >= before", ts >= before);
		assertTrue("Timestamp should be <= after", ts <= after);
	}

	// --- ChatContext.lastTurn() ---

	/** lastTurn factory sets trigger type to "post-turn". */
	public void testChatContextLastTurnTriggerType() {
		ChatContext ctx = ChatContext.lastTurn("ReviewAgent", "Hello", "Hi there", null);

		assertEquals("post-turn", ctx.getTriggerType());
	}

	/** lastTurn factory stores user message and agent response. */
	public void testChatContextLastTurnFields() {
		Object mockChat = new Object();
		ChatContext ctx = ChatContext.lastTurn("ReviewAgent", "What is X?", "X is Y.", mockChat);

		assertEquals("ReviewAgent", ctx.getAgentName());
		assertEquals("What is X?", ctx.getUserMessage());
		assertEquals("X is Y.", ctx.getAgentResponse());
		assertSame(mockChat, ctx.getChat());
	}

	/** lastTurn with null chat. */
	public void testChatContextLastTurnNullChat() {
		ChatContext ctx = ChatContext.lastTurn("agent", "msg", "resp", null);

		assertNull(ctx.getChat());
	}

	// --- ChatContext.preTurn() ---

	/** preTurn factory sets trigger type to "pre-turn". */
	public void testChatContextPreTurnTriggerType() {
		ChatContext ctx = ChatContext.preTurn("SafetyAgent", "Hello", new Object());

		assertEquals("pre-turn", ctx.getTriggerType());
	}

	/** preTurn factory stores the pending user message and chat object. */
	public void testChatContextPreTurnFields() {
		Object mockChat = new Object();
		ChatContext ctx = ChatContext.preTurn("SafetyAgent", "Check this", mockChat);

		assertEquals("SafetyAgent", ctx.getAgentName());
		assertEquals("Check this", ctx.getPendingUserMessage());
		assertSame(mockChat, ctx.getChat());
		assertNull("UserMessage should be null for preTurn", ctx.getUserMessage());
		assertNull("AgentResponse should be null for preTurn", ctx.getAgentResponse());
	}

	/** lastTurn factory does not set pending user message. */
	public void testChatContextLastTurnNoPendingMessage() {
		ChatContext ctx = ChatContext.lastTurn("agent", "msg", "resp", null);

		assertNull("PendingUserMessage should be null for lastTurn", ctx.getPendingUserMessage());
	}

	// --- ApplicationContext.scheduled() ---

	/** scheduled factory sets trigger type and source. */
	public void testApplicationContextScheduled() {
		ApplicationContext ctx = ApplicationContext.scheduled("DailyReport");

		assertEquals("DailyReport", ctx.getAgentName());
		assertEquals("scheduled", ctx.getTriggerType());
		assertEquals("scheduler", ctx.getTriggerSource());
	}

	// --- Inheritance ---

	/** ChatContext is-a BackgroundContext. */
	public void testChatContextIsBackgroundContext() {
		ChatContext ctx = ChatContext.lastTurn("agent", "msg", "resp", null);

		assertTrue(ctx instanceof BackgroundContext);
	}

	/** ApplicationContext is-a BackgroundContext. */
	public void testApplicationContextIsBackgroundContext() {
		ApplicationContext ctx = ApplicationContext.scheduled("agent");

		assertTrue(ctx instanceof BackgroundContext);
	}
}
