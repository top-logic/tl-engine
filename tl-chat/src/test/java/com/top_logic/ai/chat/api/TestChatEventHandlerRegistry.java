/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Verifies the {@link ChatEventHandler} seam: a chat UI that is not {@link ChatSSEHandler} can
 * register with {@link ChatEventHandlerRegistry} and receives the events {@link ChatProcessor}
 * routes through the registry.
 */
public class TestChatEventHandlerRegistry extends TestCase {

	/**
	 * A chat UI that is not the SSE handler: it just records what it is handed.
	 */
	private static final class RecordingHandler implements ChatEventHandler {

		private final TaskProgressTracker _tracker;

		final List<AgentEvent> events = new ArrayList<>();

		final List<MessageCompleteEvent> completions = new ArrayList<>();

		final List<ProviderErrorEvent> errors = new ArrayList<>();

		boolean _active = true;

		RecordingHandler(TaskProgressTracker tracker) {
			_tracker = tracker;
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _tracker;
		}

		@Override
		public boolean isActive() {
			return _active;
		}

		@Override
		public void onEvent(AgentEvent event) {
			events.add(event);
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			completions.add(event);
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			errors.add(event);
		}
	}

	private TaskProgressTracker _tracker;

	private RecordingHandler _handler;

	@Override
	protected void setUp() {
		_tracker = new TaskProgressTracker();
		_handler = new RecordingHandler(_tracker);
		ChatEventHandlerRegistry.register(_handler);
	}

	@Override
	protected void tearDown() {
		ChatEventHandlerRegistry.unregister(_handler);
	}

	/**
	 * The registry is typed by the interface, so a non-SSE implementation is found for its
	 * tracker - this is the lookup {@link ChatProcessor} performs per event.
	 */
	public void testNonSseHandlerIsFound() {
		assertSame("A registered non-SSE chat UI must be found for its tracker.",
			_handler, ChatEventHandlerRegistry.find(_tracker));
	}

	/**
	 * The full agent event stream a chat UI needs - streaming text, tool lifecycle, approvals, run
	 * lifecycle - reaches a registered implementation unchanged.
	 */
	public void testAgentEventsReachTheRegisteredHandler() {
		List<AgentEvent> sent = List.of(
			new AgentEvent.RunStarted("run1", 0, Instant.now(), "claude-opus-5"),
			new AgentEvent.TurnStarted("run1", 1, Instant.now()),
			new AgentEvent.TextDelta("run1", 2, Instant.now(), "Hello"),
			new AgentEvent.ToolCallStarted("run1", 3, Instant.now(), "tc-1", "search", "{}", ToolMode.READ),
			new AgentEvent.ApprovalRequired("run1", 4, Instant.now(), "tc-2", "ar-1", "write", "{}",
				"Writes data"),
			new AgentEvent.ApprovalResolved("run1", 5, Instant.now(), "tc-2", "ar-1",
				ApprovalOutcome.APPROVED),
			new AgentEvent.RunCompleted("run1", 6, Instant.now(), StopReason.COMPLETE,
				TokenUsage.EMPTY));

		for (AgentEvent event : sent) {
			ChatEventHandlerRegistry.find(_tracker).onEvent(event);
		}

		assertEquals("Every event routed through the registry must arrive, in order.",
			sent, _handler.events);
	}

	/**
	 * The two notifications {@link ChatProcessor} produces itself - the rendered final message and
	 * a provider error - are part of the contract, so a non-SSE UI can render a completed run and
	 * a failed one.
	 */
	public void testRunCompletionNotificationsReachTheRegisteredHandler() {
		ChatEventHandler found = ChatEventHandlerRegistry.find(_tracker);
		found.sendMessageComplete(MessageCompleteEvent.create()
			.setContentHtml("<p>done</p>")
			.setContentText("done"));
		found.sendProviderError(ProviderErrorEvent.create()
			.setKind("RATE_LIMIT")
			.setErrorMessage("too many requests"));

		assertEquals(1, _handler.completions.size());
		assertEquals("<p>done</p>", _handler.completions.get(0).getContentHtml());
		assertEquals(1, _handler.errors.size());
		assertEquals("RATE_LIMIT", _handler.errors.get(0).getKind());
	}

	/** Handlers of a different tracker must not be addressed. */
	public void testLookupIsScopedToTheTracker() {
		assertNull("A tracker without a registered chat UI must resolve to nothing.",
			ChatEventHandlerRegistry.find(new TaskProgressTracker()));
	}

	/**
	 * A handler whose connection is gone is skipped, mirroring the SSE reconnection case: the
	 * browser reopens the stream, and the run must keep streaming into the new handler.
	 */
	public void testInactiveHandlerIsSkipped() {
		_handler._active = false;
		assertNull("An inactive handler must not be handed events.",
			ChatEventHandlerRegistry.find(_tracker));

		RecordingHandler reconnected = new RecordingHandler(_tracker);
		try {
			ChatEventHandlerRegistry.register(reconnected);
			assertSame("The reconnected handler must take over the tracker.",
				reconnected, ChatEventHandlerRegistry.find(_tracker));
		} finally {
			ChatEventHandlerRegistry.unregister(reconnected);
		}
	}

	/**
	 * The SSE handler resolves through the same registry with the same semantics it had when the
	 * lookup lived on {@link ChatSSEHandler} itself: it is found while its connection is open and
	 * gone once it is stopped.
	 */
	public void testSseHandlerUsesTheSameRegistry() {
		TaskProgressTracker sseTracker = new TaskProgressTracker();
		ChatSSEHandler sse = new ChatSSEHandler(sseTracker, null, new PrintWriter(new StringWriter()));
		try {
			ChatEventHandlerRegistry.register(sse);

			assertTrue("A fresh SSE handler is active.", sse.isActive());
			assertSame("The SSE handler must resolve through the shared registry.",
				sse, ChatEventHandlerRegistry.find(sseTracker));

			sse.stop();

			assertFalse("A stopped SSE handler is no longer active.", sse.isActive());
			assertNull("A stopped SSE handler must not be handed events.",
				ChatEventHandlerRegistry.find(sseTracker));
		} finally {
			ChatEventHandlerRegistry.unregister(sse);
		}
	}

	/** An unregistered handler is no longer addressed. */
	public void testUnregisterRemovesTheHandler() {
		ChatEventHandlerRegistry.unregister(_handler);
		assertNull("An unregistered handler must not be found.",
			ChatEventHandlerRegistry.find(_tracker));
	}
}
