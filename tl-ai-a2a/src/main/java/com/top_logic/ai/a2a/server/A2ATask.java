/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * One A2A task on the serving side: its state, history and artifacts, and the ordered stream events a
 * subscriber receives.
 *
 * <p>
 * Thread-safe: the agent run changes it on its own thread, request threads read and wait on it, all
 * on this object's monitor.
 * </p>
 */
public final class A2ATask {

	/** Metadata entry: the calling application, as it named itself, else the owning account. */
	public static final String PEER = "peer";

	private final String _id;

	private final String _contextId;

	private final String _agent;

	private final String _owner;

	private TaskState _state = TaskState.SUBMITTED;

	private Map<String, Object> _statusMessage;

	private Instant _statusTime = Instant.now();

	private final List<Map<String, Object>> _history = new ArrayList<>();

	private final List<Map<String, Object>> _artifacts = new ArrayList<>();

	private final Map<String, Object> _metadata = new LinkedHashMap<>();

	private final List<Map<String, Object>> _events = new ArrayList<>();

	private Consumer<String> _answerHandler;

	private Runnable _cancelHandler;

	/**
	 * Creates a task in state {@link TaskState#SUBMITTED}.
	 *
	 * @param agent
	 *        The path the serving agent is exposed under.
	 * @param owner
	 *        The account name of the client that created it; only that client sees it.
	 */
	public A2ATask(String id, String contextId, String agent, String owner) {
		_id = id;
		_contextId = contextId;
		_agent = agent;
		_owner = owner;
	}

	/**
	 * A task read back from its persisted JSON ({@link #toJson(Integer)}) after a restart.
	 *
	 * <p>
	 * A task that had not finished lost its run with the restart: it reads as
	 * {@link TaskState#FAILED}, saying so, and a message to it continues its conversation in a new
	 * task.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public static A2ATask restored(Map<String, Object> json, String agent, String owner) {
		A2ATask task = new A2ATask(Json.str(json, "id"), Json.str(json, "contextId"), agent, owner);
		Map<String, Object> status = Json.obj(json, "status");
		TaskState state = TaskState.parse(Json.str(status, "state"));
		synchronized (task) {
			if (state != null && state.isTerminal()) {
				task._state = state;
				task._statusMessage = Json.copy(Json.obj(status, "message"));
			} else {
				task._state = TaskState.FAILED;
				task._statusMessage = A2AJson.textMessage(A2AJson.ROLE_AGENT, task._contextId, task._id,
					"The application restarted while this task was " + (state == null ? "open" : state.name())
						+ "; its run is gone. Send your message (or your answer) again in the same context - the"
						+ " conversation continues.");
			}
			try {
				task._statusTime = Instant.parse(Json.str(status, "timestamp"));
			} catch (RuntimeException ex) {
				// No usable time stored: now.
			}
			for (Object message : Json.list(json, "history")) {
				task._history.add(Json.copy((Map<String, Object>) message));
			}
			for (Object artifact : Json.list(json, "artifacts")) {
				task._artifacts.add(Json.copy((Map<String, Object>) artifact));
			}
			Map<String, Object> metadata = Json.copy(Json.obj(json, "metadata"));
			if (metadata != null) {
				metadata.remove("agent");
				task._metadata.putAll(metadata);
			}
		}
		return task;
	}

	/** The task id. */
	public String id() {
		return _id;
	}

	/** The context (conversation) id. */
	public String contextId() {
		return _contextId;
	}

	/** The path under which the serving agent is exposed. */
	public String agent() {
		return _agent;
	}

	/** The account name of the client that owns the task. */
	public String owner() {
		return _owner;
	}

	/** The current state. */
	public synchronized TaskState state() {
		return _state;
	}

	/** The message of the current status, or <code>null</code>. */
	public synchronized Map<String, Object> statusMessage() {
		return Json.copy(_statusMessage);
	}

	/** The calling application: {@link #PEER} metadata, else the owner. */
	public synchronized String peer() {
		return _metadata.get(PEER) instanceof String peer ? peer : _owner;
	}

	/**
	 * Sets a metadata entry (shown in the task's {@code metadata}).
	 */
	public synchronized void putMetadata(String key, Object value) {
		_metadata.put(key, value);
	}

	/**
	 * The artifact with the given name or id, or <code>null</code>.
	 */
	public synchronized Map<String, Object> artifact(String name) {
		Map<String, Object> result = null;
		for (Map<String, Object> artifact : _artifacts) {
			if (name.equals(Json.str(artifact, "name")) || name.equals(Json.str(artifact, "artifactId"))) {
				result = artifact;
			}
		}
		return Json.copy(result);
	}

	/**
	 * Appends a message of the client to the history.
	 */
	public synchronized void addHistory(Map<String, Object> message) {
		Map<String, Object> copy = Json.copy(message);
		copy.putIfAbsent("taskId", _id);
		copy.putIfAbsent("contextId", _contextId);
		_history.add(copy);
	}

	/**
	 * Moves the task to {@code state}.
	 *
	 * @param message
	 *        The status message, or <code>null</code>.
	 * @return Whether the transition happened. A terminal task does not move again, so a run that
	 *         ends after its task was canceled cannot overwrite the cancellation.
	 */
	public synchronized boolean moveTo(TaskState state, Map<String, Object> message) {
		if (!_state.canMoveTo(state)) {
			return false;
		}
		_state = state;
		_statusTime = Instant.now();
		_statusMessage = Json.copy(message);
		if (_statusMessage != null) {
			_statusMessage.putIfAbsent("taskId", _id);
			_statusMessage.putIfAbsent("contextId", _contextId);
			if (state != TaskState.WORKING) {
				// Progress notes of a working task are stream events, not conversation history.
				_history.add(Json.copy(_statusMessage));
			}
		}
		if (!state.isInterrupted()) {
			_answerHandler = null;
		}
		_events.add(Json.map("statusUpdate", Json.map("taskId", _id, "contextId", _contextId,
			"status", A2AJson.status(state, _statusMessage, _statusTime))));
		notifyAll();
		return true;
	}

	/**
	 * Adds an artifact.
	 */
	public synchronized void addArtifact(Map<String, Object> artifact) {
		_artifacts.add(Json.copy(artifact));
		_events.add(Json.map("artifactUpdate", Json.map("taskId", _id, "contextId", _contextId,
			"artifact", Json.copy(artifact), "append", Boolean.FALSE, "lastChunk", Boolean.TRUE)));
		notifyAll();
	}

	/**
	 * Puts the task into {@link TaskState#INPUT_REQUIRED} with {@code question} as status message;
	 * the client's next answer goes to {@code answerHandler}.
	 *
	 * @return Whether the task now waits for input; <code>false</code> if it already ended.
	 */
	public synchronized boolean requestInput(Map<String, Object> question, Consumer<String> answerHandler) {
		Consumer<String> before = _answerHandler;
		_answerHandler = answerHandler;
		if (!moveTo(TaskState.INPUT_REQUIRED, question)) {
			_answerHandler = before;
			return false;
		}
		return true;
	}

	/**
	 * Delivers the client's answer to a task in {@link TaskState#INPUT_REQUIRED} and moves it back to
	 * {@link TaskState#WORKING}.
	 *
	 * @return Whether the task was waiting for input and took the answer.
	 */
	public boolean answer(Map<String, Object> answerMessage) {
		Consumer<String> handler;
		synchronized (this) {
			if (_state != TaskState.INPUT_REQUIRED || _answerHandler == null) {
				return false;
			}
			handler = _answerHandler;
			addHistory(answerMessage);
			moveTo(TaskState.WORKING, null);
		}
		handler.accept(A2AJson.text(answerMessage));
		return true;
	}

	/**
	 * Sets what stops the run behind this task.
	 */
	public synchronized void setCancelHandler(Runnable cancelHandler) {
		_cancelHandler = cancelHandler;
	}

	/**
	 * Stops the task's run and moves it to {@link TaskState#CANCELED}.
	 *
	 * @return Whether the task was still active.
	 */
	public boolean cancel(String reason) {
		Runnable cancel;
		synchronized (this) {
			if (!moveTo(TaskState.CANCELED, A2AJson.textMessage(A2AJson.ROLE_AGENT, _contextId, _id, reason))) {
				return false;
			}
			cancel = _cancelHandler;
		}
		if (cancel != null) {
			cancel.run();
		}
		return true;
	}

	/**
	 * Waits until there are events at or after {@code index}, or the timeout passes.
	 *
	 * @return The events from {@code index} on; empty after a timeout.
	 */
	public synchronized List<Map<String, Object>> awaitEvents(int index, long timeoutMillis)
			throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		while (_events.size() <= index) {
			long left = deadline - System.currentTimeMillis();
			if (left <= 0) {
				return List.of();
			}
			wait(left);
		}
		return Json.copy(new ArrayList<>(_events.subList(index, _events.size())));
	}

	/**
	 * Waits until the task ends or asks, at most {@code timeoutMillis}.
	 */
	public synchronized void awaitEndOfWait(long timeoutMillis) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		for (long left = timeoutMillis; !_state.endsWait() && left > 0; left = deadline - System.currentTimeMillis()) {
			wait(left);
		}
	}

	/**
	 * The task as JSON.
	 *
	 * @param historyLength
	 *        The maximum number of history messages, <code>null</code> for all.
	 */
	public synchronized Map<String, Object> toJson(Integer historyLength) {
		List<Map<String, Object>> history = _history;
		if (historyLength != null && historyLength >= 0 && history.size() > historyLength) {
			history = history.subList(history.size() - historyLength, history.size());
		}
		Map<String, Object> metadata = Json.copy(_metadata);
		metadata.put("agent", _agent);
		return Json.map("id", _id, "contextId", _contextId,
			"status", A2AJson.status(_state, _statusMessage, _statusTime),
			"artifacts", _artifacts.isEmpty() ? null : Json.copy(_artifacts),
			"history", history.isEmpty() ? null : Json.copy(history),
			"metadata", metadata);
	}

	/**
	 * The task as JSON together with the number of stream events it includes, read atomically, so a
	 * subscriber that continues at that index misses nothing and repeats nothing.
	 */
	public synchronized Snapshot snapshot() {
		return new Snapshot(toJson(null), _events.size());
	}

	/**
	 * A task's JSON and the event index it is current with.
	 */
	public record Snapshot(Map<String, Object> task, int eventIndex) {
		// Pure value.
	}
}
