/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.protocol.StateEvent;

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
public class SSEUpdateQueue implements HttpSessionBindingListener {

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

	private final Map<String, ReactCommandTarget> _controls = new ConcurrentHashMap<>();

	private final AtomicInteger _nextId = new AtomicInteger(1);

	private volatile ScheduledFuture<?> _heartbeatTask;

	/**
	 * Retrieves or creates the {@link SSEUpdateQueue} for the given session.
	 *
	 * <p>
	 * Synchronizes on the session to prevent a race where two concurrent requests both see
	 * {@code null}, create independent queues, and the second {@code setAttribute} triggers
	 * {@code valueUnbound} on the first — clearing its registered controls.
	 * </p>
	 */
	public static SSEUpdateQueue forSession(HttpSession session) {
		SSEUpdateQueue queue = (SSEUpdateQueue) session.getAttribute(SESSION_ATTRIBUTE_KEY);
		if (queue == null) {
			synchronized (session) {
				queue = (SSEUpdateQueue) session.getAttribute(SESSION_ATTRIBUTE_KEY);
				if (queue == null) {
					queue = new SSEUpdateQueue();
					session.setAttribute(SESSION_ATTRIBUTE_KEY, queue);
				}
			}
		}
		return queue;
	}

	/**
	 * Allocates a unique control ID within this session.
	 *
	 * <p>
	 * IDs are prefixed with "v" to distinguish them from old-world control IDs.
	 * </p>
	 */
	public String allocateId() {
		return "v" + _nextId.getAndIncrement();
	}

	/**
	 * Registers a {@link ReactCommandTarget} (typically a {@link ReactControl}) so that it can be
	 * looked up by ID for command dispatch.
	 */
	public void registerControl(ReactCommandTarget control) {
		_controls.put(control.getID(), control);
	}

	/**
	 * Unregisters a previously registered control.
	 */
	public void unregisterControl(ReactCommandTarget control) {
		_controls.remove(control.getID(), control);
	}

	/**
	 * Looks up a previously registered control by its ID.
	 *
	 * @return The control, or {@code null} if not found.
	 */
	public ReactCommandTarget getControl(String controlId) {
		ReactCommandTarget control = _controls.get(controlId);
		if (control == null) {
			Logger.warn("SSEUpdateQueue@" + System.identityHashCode(this) + " getControl(" + controlId
				+ ") NOT FOUND. Registered IDs: " + _controls.keySet(), SSEUpdateQueue.class);
		}
		return control;
	}

	/**
	 * Registers an SSE connection to receive events.
	 */
	public void addConnection(AsyncContext asyncContext) {
		_connections.add(asyncContext);
		sendFullState(asyncContext);
		ensureHeartbeat();
	}

	/**
	 * Sends the full state of all registered {@link ReactControl}s to the given connection.
	 *
	 * <p>
	 * This is called when a new SSE connection is established (including reconnects) to ensure the
	 * client has the current state of all controls, recovering any state that may have been lost
	 * while the connection was down.
	 * </p>
	 */
	private void sendFullState(AsyncContext ctx) {
		for (ReactCommandTarget control : _controls.values()) {
			if (control instanceof ReactControl) {
				ReactControl rc = (ReactControl) control;
				StateEvent event = StateEvent.create()
					.setControlId(rc.getID())
					.setState(rc.stateAsJSON());
				String message = toDataMessage(toJson(event));
				if (message != null) {
					writeOrRemove(ctx, message);
				}
			}
		}
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
			Logger.info("SSE flush: " + _connections.size() + " connections, event="
				+ event.getClass().getSimpleName(), SSEUpdateQueue.class);
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

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		synchronized (this) {
			if (_heartbeatTask != null) {
				_heartbeatTask.cancel(false);
				_heartbeatTask = null;
			}
		}
		for (AsyncContext ctx : _connections) {
			try {
				ctx.complete();
			} catch (Exception ex) {
				// Connection already closed, ignore.
			}
		}
		_connections.clear();
		_controls.clear();
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
