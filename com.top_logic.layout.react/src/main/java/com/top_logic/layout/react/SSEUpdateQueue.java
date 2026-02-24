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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
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
 *
 * <p>
 * A periodic heartbeat is sent to all connections to keep them alive and to detect dead connections
 * early. Without the heartbeat, intermediaries (proxies, load balancers) may silently drop idle
 * connections, leaving half-open connections that neither the server nor the client can detect.
 * </p>
 */
public class SSEUpdateQueue {

	private static final String SESSION_ATTRIBUTE_KEY = "tl.react.sseQueue";

	/**
	 * Heartbeat message sent as a regular SSE data event so the client can track connection
	 * liveness.
	 */
	private static final String HEARTBEAT_MESSAGE = "data: [\"Heartbeat\",{}]\n\n";

	/** Interval between heartbeat messages in seconds. */
	private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

	private final ConcurrentLinkedQueue<SSEEvent> _pendingEvents = new ConcurrentLinkedQueue<>();

	private final List<AsyncContext> _connections = new CopyOnWriteArrayList<>();

	private final Map<String, CommandListener> _controls = new ConcurrentHashMap<>();

	private volatile ScheduledFuture<?> _heartbeatTask;

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
		ensureHeartbeat();
	}

	/**
	 * Removes a previously registered SSE connection.
	 */
	public void removeConnection(AsyncContext asyncContext) {
		_connections.remove(asyncContext);
		cancelHeartbeatIfEmpty();
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
			String message = toDataMessage(toJson(event));
			if (message == null) {
				continue;
			}
			for (AsyncContext ctx : _connections) {
				writeOrRemove(ctx, message);
			}
		}
	}

	private synchronized void ensureHeartbeat() {
		if (_heartbeatTask == null || _heartbeatTask.isDone()) {
			_heartbeatTask = SchedulerService.getInstance().scheduleAtFixedRate(
				this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
		}
	}

	private synchronized void cancelHeartbeatIfEmpty() {
		if (_connections.isEmpty() && _heartbeatTask != null) {
			_heartbeatTask.cancel(false);
			_heartbeatTask = null;
		}
	}

	private void sendHeartbeat() {
		for (AsyncContext ctx : _connections) {
			writeOrRemove(ctx, HEARTBEAT_MESSAGE);
		}
		cancelHeartbeatIfEmpty();
	}

	/**
	 * Writes a message to the given connection, removing it if the write fails.
	 */
	private void writeOrRemove(AsyncContext ctx, String message) {
		try {
			writeToConnection(ctx, message);
		} catch (IOException ex) {
			Logger.warn("SSE write failed, removing dead connection.", ex, SSEUpdateQueue.class);
			_connections.remove(ctx);
		}
	}

	/**
	 * Writes a complete SSE message to a connection.
	 *
	 * <p>
	 * Synchronizes on the {@link AsyncContext} to prevent interleaving between heartbeat writes and
	 * event writes from different threads.
	 * </p>
	 *
	 * @throws IOException
	 *         If the write fails, indicating a dead connection.
	 */
	private static void writeToConnection(AsyncContext ctx, String message) throws IOException {
		synchronized (ctx) {
			PrintWriter writer = ctx.getResponse().getWriter();
			writer.write(message);
			writer.flush();
			if (writer.checkError()) {
				throw new IOException("SSE write failed (PrintWriter error flag set).");
			}
		}
	}

	private static String toDataMessage(String json) {
		if (json == null) {
			return null;
		}
		return "data: " + json + "\n\n";
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
