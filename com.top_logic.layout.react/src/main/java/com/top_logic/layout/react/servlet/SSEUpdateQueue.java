/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.AsyncContext;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.protocol.StateEvent;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Queue for delivering SSE events to a connected React client in a single browser window.
 *
 * <p>
 * Each browser window has one {@link SSEUpdateQueue} managed by the
 * {@link com.top_logic.layout.react.window.ReactWindowRegistry}. A single SSE connection
 * registers with the queue and receives events as they are enqueued.
 * </p>
 *
 * <p>
 * A periodic heartbeat is sent to the connection to keep it alive and to detect dead connections
 * early. Without the heartbeat, intermediaries (proxies, load balancers) may silently drop idle
 * connections, leaving half-open connections that neither the server nor the client can detect.
 * </p>
 */
public class SSEUpdateQueue {

	/**
	 * Heartbeat message sent as a regular SSE data event so the client can track connection
	 * liveness.
	 */
	private static final String HEARTBEAT_MESSAGE = "data: [\"Heartbeat\",{}]\n\n";

	/** Interval between heartbeat messages in seconds. */
	private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

	private final ConcurrentLinkedQueue<SSEEvent> _pendingEvents = new ConcurrentLinkedQueue<>();

	private final Map<String, ReactCommandTarget> _controls = new ConcurrentHashMap<>();

	private final AtomicInteger _nextId = new AtomicInteger(1);

	private volatile SSEConnection _connection;

	private volatile boolean _shutdown;

	private volatile ScheduledFuture<?> _heartbeatTask;

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
	 * Sets the SSE connection for this queue, replacing any existing one.
	 */
	public void setConnection(AsyncContext asyncContext) {
		SSEConnection old = _connection;
		SSEConnection newConn = new SSEConnection(asyncContext);
		_connection = newConn;
		Logger.info("SSE connection set for queue@" + System.identityHashCode(this)
			+ ", replacing=" + (old != null), SSEUpdateQueue.class);
		if (old != null) {
			try {
				old.getContext().complete();
			} catch (Exception ex) {
				// Old connection already closed, ignore.
			}
		}
		sendFullState(newConn);
		ensureHeartbeat();
	}

	/**
	 * Clears the SSE connection if it matches the given async context.
	 */
	public void clearConnection(AsyncContext asyncContext) {
		SSEConnection current = _connection;
		if (current != null && current.getContext() == asyncContext) {
			_connection = null;
			Logger.info("SSE connection cleared for queue@" + System.identityHashCode(this),
				SSEUpdateQueue.class);
		}
		cancelHeartbeatIfEmpty();
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
	private void sendFullState(SSEConnection connection) {
		for (ReactCommandTarget control : _controls.values()) {
			if (control instanceof ReactControl) {
				ReactControl rc = (ReactControl) control;
				StateEvent event = StateEvent.create()
					.setControlId(rc.getID())
					.setState(rc.stateAsJSON());
				String message = toDataMessage(toJson(event));
				if (message != null) {
					writeOrDisconnect(connection, message);
				}
			}
		}
	}

	/**
	 * Enqueues an event and immediately flushes it to the connected SSE client.
	 */
	public void enqueue(SSEEvent event) {
		if (_shutdown) {
			Logger.info("enqueue REJECTED (shutdown) on queue@" + System.identityHashCode(this),
				SSEUpdateQueue.class);
			return;
		}
		_pendingEvents.add(event);
		Logger.info("enqueue on queue@" + System.identityHashCode(this)
			+ ", connection=" + (_connection != null),
			SSEUpdateQueue.class);
		flush();
	}

	/**
	 * Flushes all pending events to the connected SSE client.
	 */
	public void flush() {
		SSEConnection conn = _connection;
		if (conn == null) {
			return;
		}
		SSEEvent event;
		while ((event = _pendingEvents.poll()) != null) {
			String message = toDataMessage(toJson(event));
			if (message == null) {
				continue;
			}
			if (!writeOrDisconnect(conn, message)) {
				return;
			}
		}
	}

	/**
	 * Shuts down this queue, cancelling the heartbeat, closing the connection, and clearing all
	 * state.
	 */
	public void shutdown() {
		_shutdown = true;
		synchronized (this) {
			if (_heartbeatTask != null) {
				_heartbeatTask.cancel(false);
				_heartbeatTask = null;
			}
		}
		SSEConnection conn = _connection;
		_connection = null;
		if (conn != null) {
			try {
				conn.getContext().complete();
			} catch (Exception ex) {
				// Connection already closed, ignore.
			}
		}
		_pendingEvents.clear();
		_controls.clear();
	}

	private synchronized void ensureHeartbeat() {
		if (_heartbeatTask == null || _heartbeatTask.isDone()) {
			_heartbeatTask = SchedulerService.getInstance().scheduleAtFixedRate(
				this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
		}
	}

	private synchronized void cancelHeartbeatIfEmpty() {
		if (_connection == null && _heartbeatTask != null) {
			_heartbeatTask.cancel(false);
			_heartbeatTask = null;
		}
	}

	private void sendHeartbeat() {
		SSEConnection conn = _connection;
		if (conn != null) {
			writeOrDisconnect(conn, HEARTBEAT_MESSAGE);
		}
		cancelHeartbeatIfEmpty();
	}

	/**
	 * Writes a message to the given connection, clearing it if the write fails.
	 */
	private boolean writeOrDisconnect(SSEConnection connection, String message) {
		try {
			writeToConnection(connection.getContext(), message);
			return true;
		} catch (IOException ex) {
			Logger.info("SSE connection lost for queue@" + System.identityHashCode(this),
				SSEUpdateQueue.class);
			_connection = null;
			return false;
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

	/**
	 * Wraps an {@link AsyncContext} for an SSE connection.
	 */
	static final class SSEConnection {

		private final AsyncContext _context;

		SSEConnection(AsyncContext context) {
			_context = context;
		}

		AsyncContext getContext() {
			return _context;
		}
	}

}
