/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Drives {@link ChatSSEHandler} over its real delivery path - register, resolve through
 * {@link ChatEventHandlerRegistry}, buffer, flush, write - and asserts the frames that reach the
 * window's event stream.
 *
 * <p>
 * Only the last step is stubbed: the handler runs with its real scheduled flush task and a real
 * window, and each patch it would put on the window's queue is written into a {@link StringWriter}
 * instead, one {@code event: <name>} line per frame followed by the patch. The browser must keep
 * seeing the same frames, produced by the same lookup, in the same order.
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
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		SSEUpdateQueue queue = registry.getOrCreateQueue("w1");
		_handler = new ChatSSEHandler(_tracker, null, registry, "w1", queue, "t1") {
			@Override
			void deliver(String patch) {
				_wire.write(render(patch));
			}
		};
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

	/** The end-of-run notification reaches the wire. */
	public void testMessageCompleteReachesTheWire() throws InterruptedException {
		ChatEventHandlerRegistry.find(_tracker).sendMessageComplete(MessageCompleteEvent.create()
			.setContentHtml("<p>done</p>")
			.setContentText("done"));

		assertFrames(awaitFrame("event: message_complete"), "event: message_complete");
	}

	/**
	 * The end-of-run notification must not overtake the text it completes.
	 *
	 * <p>
	 * It used to be written straight to the stream while the deltas waited in the flush queue, so it
	 * arrived first and the browser - having closed the message on it - dropped every delta that
	 * followed. Every answer was rendered with its last fragment missing.
	 * </p>
	 */
	public void testMessageCompleteDoesNotOvertakeTheDeltas() throws InterruptedException {
		ChatEventHandler resolved = ChatEventHandlerRegistry.find(_tracker);
		resolved.onEvent(new AgentEvent.TextDelta("run1", 1, Instant.now(), "Die Hauptstadt ist"));
		resolved.onEvent(new AgentEvent.TextDelta("run1", 1, Instant.now(), " L"));
		resolved.sendMessageComplete(MessageCompleteEvent.create()
			.setContentText("Die Hauptstadt ist Lissabon."));

		String wire = awaitFrame("event: message_complete");
		assertFrames(wire, "event: text_delta", "event: text_delta", "event: message_complete");
	}

	/** A provider error keeps its place in the stream too. */
	public void testProviderErrorKeepsItsPlace() throws InterruptedException {
		ChatEventHandler resolved = ChatEventHandlerRegistry.find(_tracker);
		resolved.onEvent(new AgentEvent.TextDelta("run1", 1, Instant.now(), "Teil"));
		resolved.sendProviderError(ProviderErrorEvent.create()
			.setKind("OVERLOADED")
			.setErrorMessage("busy"));

		assertFrames(awaitFrame("event: provider_error"), "event: text_delta", "event: provider_error");
	}

	/** A stopped handler writes nothing further and is no longer resolvable. */
	public void testStoppedHandlerIsSilent() throws InterruptedException {
		_handler.stop();
		int written = _wire.toString().length();

		assertNull("A stopped handler must not be resolved for its tracker.",
			ChatEventHandlerRegistry.find(_tracker));
		_handler.sendMessageComplete(MessageCompleteEvent.create().setContentText("late"));
		Thread.sleep(3 * 50);
		assertEquals("A stopped handler must not write to its window any more.",
			written, _wire.toString().length());
	}

	/** One {@code event: <name>} line per frame of the patch, then the patch itself. */
	@SuppressWarnings("unchecked")
	private static String render(String patch) {
		StringBuilder text = new StringBuilder();
		try {
			for (Map<String, Object> frame : (List<Map<String, Object>>) ((Map<String, Object>) JSON
				.fromString(patch)).get("frames")) {
				text.append("event: ").append(frame.get("event")).append('\n');
			}
		} catch (Exception ex) {
			throw new AssertionError("A patch must be JSON: " + patch, ex);
		}
		return text.append(patch).append("\n\n").toString();
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
