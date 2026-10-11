/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * The chat's frames on the event stream of the browser window, and how long a window counts as
 * showing the chat.
 *
 * <p>
 * A page that held a chat stream of its own beside the window's took two of the six connections a
 * browser opens to one server, so three tabs took all six and no click reached the server any
 * more (#867). {@link ChatSSEHandler} puts the frames on the window's stream instead.
 * </p>
 */
public class TestChatWindowStream extends TestCase {

	/** Generous enough for the handler's 50ms flush cycle on a loaded machine. */
	private static final long FLUSH_TIMEOUT_MS = 5000;

	private static final String WINDOW = "w1";

	private ReactWindowRegistry _registry;

	private SSEUpdateQueue _queue;

	private TaskProgressTracker _tracker;

	private ChatSSEHandler _handler;

	@Override
	protected void setUp() {
		_registry = new ReactWindowRegistry("test-session");
		_queue = _registry.getOrCreateQueue(WINDOW);
		_tracker = new TaskProgressTracker();
		_handler = new ChatSSEHandler(_tracker, null, _registry, WINDOW, _queue, "t1");
	}

	@Override
	protected void tearDown() {
		_handler.stop();
		ChatEventHandlerRegistry.unregister(_handler);
	}

	/**
	 * The frames a start produces go out at once, as one event of the window's queue, and the
	 * handler is the one the run finds for the window's conversation.
	 */
	public void testStartPutsOnePatchOnTheWindowsQueue() {
		_handler.start();

		assertSame(_handler, ChatEventHandlerRegistry.find(_tracker));
		assertEquals("The connected frames go out at once, as one event.", 1, _queue.pendingEventCount());
	}

	/** The patch names each frame and carries its payload as JSON, in the order the frames came. */
	@SuppressWarnings("unchecked")
	public void testPatchCarriesTheFramesInOrder() throws Exception {
		String patch = ChatSSEHandler.patch(List.of(
			ChatSSEHandler.frame("run_started", "{\"runId\":\"r1\"}"),
			ChatSSEHandler.frame("text_delta", "{\"text\":\"Hallo \\\"Welt\\\"\",\"end\":5}")));

		List<Map<String, Object>> frames =
			(List<Map<String, Object>>) ((Map<String, Object>) JSON.fromString(patch)).get("frames");
		assertEquals(2, frames.size());
		assertEquals("run_started", frames.get(0).get("event"));
		assertEquals("r1", ((Map<String, Object>) frames.get(0).get("data")).get("runId"));
		assertEquals("text_delta", frames.get(1).get("event"));
		assertEquals("Hallo \"Welt\"", ((Map<String, Object>) frames.get(1).get("data")).get("text"));
	}

	/** The listener id has a prefix of its own, so it cannot name a control of the window. */
	public void testListenerIdHasItsOwnPrefix() {
		assertTrue(_handler.writesTo("tl-chat-stream:t1"));
		assertFalse(_handler.writesTo("tl-chat-stream:t2"));
		assertFalse(_handler.writesTo("t1"));
	}

	/** Only a short id of letters, digits, dash and underscore is accepted from the browser. */
	public void testTokenRule() {
		assertTrue(ChatSSEHandler.isToken("0b7c-4f_A9"));
		assertFalse(ChatSSEHandler.isToken(null));
		assertFalse(ChatSSEHandler.isToken(""));
		assertFalse(ChatSSEHandler.isToken("v12\",\"x"));
		assertFalse(ChatSSEHandler.isToken("a:b"));
		assertFalse(ChatSSEHandler.isToken("x".repeat(65)));
	}

	/** The window counts as shown while it is known, served by the same queue and not unloaded. */
	public void testWindowShown() {
		assertTrue(_handler.isWindowShown());
		assertFalse(new ChatSSEHandler(_tracker, null, _registry, "unknown", _queue, "t1").isWindowShown());
		assertFalse(new ChatSSEHandler(_tracker, null, _registry, WINDOW, new SSEUpdateQueue(), "t1").isWindowShown());

		_registry.getWindow(WINDOW).markUnloaded();

		assertFalse(_handler.isWindowShown());
	}

	/**
	 * The handler counts as the window's chat UI while the page is there, and no longer once it is
	 * left. What depends on it: waking a lead for subagent news, and marking a result seen.
	 */
	public void testHandlerLeavesTheRegistryWhenThePageIsLeft() throws InterruptedException {
		_handler.start();
		assertSame(_handler, ChatEventHandlerRegistry.find(_tracker));

		_registry.getWindow(WINDOW).markUnloaded();

		long deadline = System.currentTimeMillis() + FLUSH_TIMEOUT_MS;
		while (_handler.isActive() && System.currentTimeMillis() < deadline) {
			Thread.sleep(20);
		}
		assertFalse("The handler must stop once its window's page is left.", _handler.isActive());
		assertNull(ChatEventHandlerRegistry.find(_tracker));
	}

}
