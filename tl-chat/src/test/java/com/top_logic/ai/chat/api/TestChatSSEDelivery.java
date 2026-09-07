/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import jakarta.servlet.AsyncContext;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Drives {@link ChatSSEHandler} over its real delivery path - register, resolve through
 * {@link ChatEventHandlerRegistry}, buffer, flush, write - and asserts the SSE frames that reach
 * the wire.
 *
 * <p>
 * Only the HTTP transport is stubbed: the handler runs with its real scheduled flush task and
 * writes into a {@link StringWriter} instead of a servlet response. This is the regression guard
 * for the SSE path across the extraction of {@link ChatEventHandler}: the browser must keep seeing
 * the same frames, produced by the same lookup, in the same order.
 * </p>
 */
public class TestChatSSEDelivery extends TestCase {

	/** Generous enough for the handler's 50ms flush cycle on a loaded machine. */
	private static final long FLUSH_TIMEOUT_MS = 5000;

	private TaskProgressTracker _tracker;

	private StringWriter _wire;

	private ChatSSEHandler _handler;

	@Override
	protected void setUp() {
		_tracker = new TaskProgressTracker();
		_wire = new StringWriter();
		AsyncContext asyncContext = (AsyncContext) Proxy.newProxyInstance(
			getClass().getClassLoader(),
			new Class<?>[] { AsyncContext.class },
			(proxy, method, args) -> null);
		_handler = new ChatSSEHandler(_tracker, asyncContext, new PrintWriter(_wire));
		_handler.start();
	}

	@Override
	protected void tearDown() {
		_handler.stop();
		ChatEventHandlerRegistry.unregister(_handler);
	}

	/**
	 * A started handler is the one {@link ChatProcessor} resolves for its tracker, and the events
	 * routed to it that way - streaming text, a tool call, an approval, the run lifecycle - reach
	 * the wire as the same SSE frames as before the seam was extracted.
	 */
	public void testAgentEventsReachTheWire() throws InterruptedException {
		ChatEventHandler resolved = ChatEventHandlerRegistry.find(_tracker);
		assertSame("ChatProcessor must resolve the started SSE handler for its tracker.",
			_handler, resolved);

		for (AgentEvent event : List.of(
			new AgentEvent.RunStarted("run1", 0, Instant.now(), "claude-opus-5"),
			new AgentEvent.TurnStarted("run1", 1, Instant.now()),
			new AgentEvent.TextDelta("run1", 1, Instant.now(), "Hello"),
			new AgentEvent.ToolCallStarted("run1", 1, Instant.now(), "tc-1", "search", "{}", ToolMode.READ),
			new AgentEvent.ApprovalRequired("run1", 1, Instant.now(), "tc-2", "ar-1", "write", "{}",
				"Writes data", Duration.ofMinutes(30)),
			new AgentEvent.ApprovalResolved("run1", 1, Instant.now(), "tc-2", "ar-1",
				ApprovalOutcome.APPROVED))) {
			resolved.onEvent(event);
		}

		String wire = awaitFrame("event: approval_resolved");
		assertFrames(wire,
			"event: run_started",
			"event: turn_started",
			"event: text_delta",
			"event: tool_started",
			"event: approval_required",
			"event: approval_resolved");
		assertTrue("The streamed text must reach the wire.", wire.contains("Hello"));
		assertTrue("The tool name must reach the wire.", wire.contains("search"));
	}

	/** The end-of-run notification is written directly, bypassing the flush queue. */
	public void testMessageCompleteReachesTheWire() {
		ChatEventHandlerRegistry.find(_tracker).sendMessageComplete(MessageCompleteEvent.create()
			.setContentHtml("<p>done</p>")
			.setContentText("done"));

		assertFrames(_wire.toString(), "event: message_complete");
	}

	/** A stopped handler writes nothing further and is no longer resolvable. */
	public void testStoppedHandlerIsSilent() {
		_handler.stop();
		int written = _wire.toString().length();

		assertNull("A stopped handler must not be resolved for its tracker.",
			ChatEventHandlerRegistry.find(_tracker));
		_handler.sendMessageComplete(MessageCompleteEvent.create().setContentText("late"));
		assertEquals("A stopped handler must not write to a closed connection.",
			written, _wire.toString().length());
	}

	private String awaitFrame(String frame) throws InterruptedException {
		long deadline = System.currentTimeMillis() + FLUSH_TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			String wire = _wire.toString();
			if (wire.contains(frame)) {
				return wire;
			}
			Thread.sleep(20);
		}
		return _wire.toString();
	}

	private static void assertFrames(String wire, String... frames) {
		int position = 0;
		for (String frame : frames) {
			int found = wire.indexOf(frame, position);
			assertTrue("Missing SSE frame '" + frame + "' (in order) on the wire:\n" + wire,
				found >= 0);
			position = found + frame.length();
		}
	}
}
