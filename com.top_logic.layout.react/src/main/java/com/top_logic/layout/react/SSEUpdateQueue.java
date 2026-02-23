/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.Logger;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.react.protocol.SSEEvent;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Queue for delivering SSE events to connected React clients.
 *
 * <p>
 * Each HTTP session has one {@link SSEUpdateQueue} stored as a session attribute. SSE connections
 * register with the queue and receive events as they are enqueued.
 * </p>
 */
public class SSEUpdateQueue {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.sseQueue";

	private final ConcurrentLinkedQueue<SSEEvent> _pendingEvents = new ConcurrentLinkedQueue<>();

	private final List<AsyncContext> _connections = new CopyOnWriteArrayList<>();

	private final Map<String, CommandListener> _controls = new ConcurrentHashMap<>();

	/**
	 * Retrieves or creates the {@link SSEUpdateQueue} for the given session.
	 */
	public static SSEUpdateQueue forSession(HttpSession session) {
		SSEUpdateQueue queue = (SSEUpdateQueue) session.getAttribute(SESSION_ATTRIBUTE_KEY);
		if (queue == null) {
			queue = new SSEUpdateQueue();
			session.setAttribute(SESSION_ATTRIBUTE_KEY, queue);
		}
		return queue;
	}

	/**
	 * Registers a {@link CommandListener} (typically a {@link ReactControl}) so that it can be
	 * looked up by ID for command dispatch.
	 */
	public void registerControl(CommandListener control) {
		_controls.put(control.getID(), control);
	}

	/**
	 * Looks up a previously registered control by its ID.
	 *
	 * @return The control, or {@code null} if not found.
	 */
	public CommandListener getControl(String controlId) {
		return _controls.get(controlId);
	}

	/**
	 * Registers an SSE connection to receive events.
	 */
	public void addConnection(AsyncContext asyncContext) {
		_connections.add(asyncContext);
	}

	/**
	 * Removes a previously registered SSE connection.
	 */
	public void removeConnection(AsyncContext asyncContext) {
		_connections.remove(asyncContext);
	}

	/**
	 * Enqueues an event and immediately flushes it to all connected SSE clients.
	 */
	public void enqueue(SSEEvent event) {
		_pendingEvents.add(event);
		flush();
	}

	/**
	 * Flushes all pending events to all connected SSE clients.
	 */
	public void flush() {
		SSEEvent event;
		while ((event = _pendingEvents.poll()) != null) {
			String json = toJson(event);
			if (json == null) {
				continue;
			}
			for (AsyncContext ctx : _connections) {
				try {
					PrintWriter writer = ctx.getResponse().getWriter();
					writer.write("data: ");
					writer.write(json);
					writer.write("\n\n");
					writer.flush();
				} catch (IOException ex) {
					Logger.warn("Failed to write SSE event, removing connection.", ex, SSEUpdateQueue.class);
					_connections.remove(ctx);
				}
			}
		}
	}

	private static String toJson(SSEEvent event) {
		try {
			StringW sw = new StringW();
			try (JsonWriter jsonWriter = new JsonWriter(sw)) {
				event.writeTo(jsonWriter);
			}
			return sw.toString();
		} catch (IOException ex) {
			Logger.error("Failed to serialize SSE event to JSON.", ex, SSEUpdateQueue.class);
			return null;
		}
	}

}
