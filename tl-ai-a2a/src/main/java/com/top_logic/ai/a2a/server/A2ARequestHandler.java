/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.top_logic.ai.a2a.A2AExchange;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * The A2A v1.0 JSON-RPC binding: method dispatch, the task lifecycle rules, streaming, and the tasks
 * this server knows in memory.
 *
 * <p>
 * Independent of the servlet API and of tl-ai: the transport hands in the request body and the
 * authenticated caller, the {@link TaskBackend} runs the agents. That keeps the protocol rules in
 * one place a test can drive.
 * </p>
 *
 * <h2>Rules beyond the specification text</h2>
 * <ul>
 * <li>A client sees only its own tasks; a foreign task id reads as unknown. A context id is shared:
 * it is the conversation, and a peer calls back in the context it was asked in.</li>
 * <li>One context runs one task at a time. A message to a context whose task waits for input answers
 * that task, if it comes from the same client and peer; a message to a context whose task is working
 * is refused.</li>
 * <li>A message naming a finished task continues its conversation with a new task.</li>
 * <li>{@code SendMessage} waits for the end of the task (or its question) unless the request says
 * {@code returnImmediately: true}, at most {@code maxBlockingMillis}.</li>
 * </ul>
 */
public class A2ARequestHandler {

	/** How often an idle stream sends a keep-alive comment. */
	private static final long KEEP_ALIVE_MILLIS = 15_000;

	/** How many tasks are kept in memory; the oldest finished ones go first. */
	private static final int MAX_TASKS = 1000;

	/**
	 * Runs the agent behind a task.
	 */
	public interface TaskBackend {

		/**
		 * Starts the run for a new task, asynchronously. The task is registered and its history holds
		 * the client's message; the backend moves it on and installs a cancel handler.
		 *
		 * @param requestMetadata
		 *        The {@code metadata} of the request and of the message, merged; never
		 *        <code>null</code>.
		 */
		void start(A2ATask task, Map<String, Object> message, Map<String, Object> requestMetadata);

		/**
		 * A task this server ran before it restarted, read back from where the backend persists its
		 * tasks, or <code>null</code>.
		 *
		 * @param owner
		 *        The account asking; only its own tasks are found.
		 */
		default A2ATask findTask(String taskId, String owner) {
			return null;
		}
	}

	/**
	 * Where a response goes.
	 */
	public interface Transport {

		/** Answers with a JSON document. */
		void sendJson(String json) throws IOException;

		/** Starts an event stream. */
		EventSink openStream() throws IOException;
	}

	/**
	 * An open server-sent-event stream.
	 */
	public interface EventSink {

		/** Sends one event whose data is the given JSON. */
		void send(String json) throws IOException;

		/** Sends a comment that keeps the connection open. */
		void keepAlive() throws IOException;
	}

	private final Map<String, A2ATask> _tasks = new LinkedHashMap<>();

	private final TaskBackend _backend;

	private final Predicate<String> _knownAgent;

	private final long _maxBlockingMillis;

	private final Consumer<A2AExchange> _observer;

	/**
	 * Creates an {@link A2ARequestHandler}.
	 *
	 * @param knownAgent
	 *        Whether an agent path is exposed.
	 * @param observer
	 *        Learns of what happens in the tasks this handler starts, see
	 *        {@link A2ATask#observeWith(Consumer)}; <code>null</code> for none.
	 */
	public A2ARequestHandler(TaskBackend backend, Predicate<String> knownAgent, long maxBlockingMillis,
			Consumer<A2AExchange> observer) {
		_backend = backend;
		_knownAgent = knownAgent;
		_maxBlockingMillis = maxBlockingMillis;
		_observer = observer;
	}

	/**
	 * Serves one JSON-RPC request.
	 *
	 * @param version
	 *        The {@code A2A-Version} header, or <code>null</code>.
	 * @param caller
	 *        The authenticated account name.
	 * @param agent
	 *        The agent path from the URL.
	 */
	public void handle(String body, String version, String caller, String agent, Transport transport)
			throws IOException {
		Object id = null;
		try {
			Object request;
			try {
				request = Json.parse(body);
			} catch (IllegalArgumentException ex) {
				throw new A2AError(A2AError.PARSE_ERROR, "Invalid JSON payload: " + ex.getMessage());
			}
			id = Json.get(request, "id");
			String method = Json.str(request, "method");
			if (!"2.0".equals(Json.get(request, "jsonrpc")) || method == null) {
				throw new A2AError(A2AError.INVALID_REQUEST, "Not a JSON-RPC 2.0 request.");
			}
			if (version != null && !version.isBlank() && !version.trim().matches("1\\.0(\\..*)?")) {
				throw new A2AError(A2AError.VERSION_NOT_SUPPORTED,
					"A2A version " + version.trim() + " is not supported; this agent speaks 1.0.");
			}
			if (!_knownAgent.test(agent)) {
				throw new A2AError(A2AError.INVALID_PARAMS, "No agent '" + agent + "' is exposed here.");
			}
			Map<String, Object> params = Json.obj(request, "params");
			if (params == null) {
				params = Json.map();
			}
			switch (method) {
				case "SendMessage" -> sendResult(transport, id, Json.map("task", send(caller, agent, params)));
				case "SendStreamingMessage" -> stream(transport, id, startOrContinue(caller, agent, params));
				case "GetTask" -> sendResult(transport, id,
					ownTask(caller, agent, Json.str(params, "id")).toJson(Json.integer(params, "historyLength")));
				case "CancelTask" -> sendResult(transport, id, cancel(caller, agent, Json.str(params, "id")));
				case "SubscribeToTask" -> subscribe(transport, id, caller, agent, Json.str(params, "id"));
				case "ListTasks" -> sendResult(transport, id, list(caller, agent, params));
				case "CreateTaskPushNotificationConfig", "GetTaskPushNotificationConfig",
						"ListTaskPushNotificationConfigs", "DeleteTaskPushNotificationConfig" ->
					throw new A2AError(A2AError.PUSH_NOT_SUPPORTED, "Push notifications are not supported.");
				default -> throw new A2AError(A2AError.METHOD_NOT_FOUND, "Method not found: " + method);
			}
		} catch (A2AError ex) {
			transport.sendJson(Json.write(response(id, "error", ex.toJson())));
		}
	}

	private static void sendResult(Transport transport, Object id, Object result) throws IOException {
		transport.sendJson(Json.write(response(id, "result", result)));
	}

	/**
	 * A JSON-RPC response; the id is written even when it is <code>null</code>, as JSON-RPC requires
	 * for a request whose id could not be read.
	 */
	private static Map<String, Object> response(Object id, String key, Object value) {
		Map<String, Object> response = Json.map("jsonrpc", "2.0");
		response.put("id", id);
		response.put(key, value);
		return response;
	}

	private Map<String, Object> send(String caller, String agent, Map<String, Object> params) {
		A2ATask task = startOrContinue(caller, agent, params);
		Map<String, Object> configuration = Json.obj(params, "configuration");
		if (!Json.bool(configuration, "returnImmediately", false)) {
			task.awaitEndOfWait(_maxBlockingMillis);
		}
		return task.toJson(Json.integer(configuration, "historyLength"));
	}

	/**
	 * Starts a new task for the message, or delivers the message to the task that waits for it.
	 */
	private A2ATask startOrContinue(String caller, String agent, Map<String, Object> params) {
		Map<String, Object> message = Json.obj(params, "message");
		if (message == null || A2AJson.parts(message).isEmpty()) {
			throw new A2AError(A2AError.INVALID_PARAMS, "params.message with at least one part is required.");
		}
		String role = Json.str(message, "role");
		if (role != null && !A2AJson.ROLE_USER.equals(role)) {
			throw new A2AError(A2AError.INVALID_PARAMS, "A client sends messages with role ROLE_USER.");
		}
		Map<String, Object> metadata = Json.map();
		if (Json.obj(params, "metadata") != null) {
			metadata.putAll(Json.obj(params, "metadata"));
		}
		if (Json.obj(message, "metadata") != null) {
			metadata.putAll(Json.obj(message, "metadata"));
		}

		String peer = A2AJson.peerOf(message);
		String contextId = Json.str(message, "contextId");
		String taskId = Json.str(message, "taskId");
		if (taskId != null) {
			A2ATask task = ownTask(caller, agent, taskId);
			if (contextId != null && !contextId.equals(task.contextId())) {
				throw new A2AError(A2AError.INVALID_PARAMS,
					"Task " + taskId + " belongs to context " + task.contextId() + ", not " + contextId + ".");
			}
			if (task.answer(message)) {
				return task;
			}
			if (!task.state().isTerminal()) {
				throw new A2AError(A2AError.UNSUPPORTED_OPERATION, "Task " + taskId
					+ " is still working and does not wait for input; wait for it or cancel it.");
			}
			// A finished task: the message continues its conversation.
			contextId = task.contextId();
		}
		if (contextId == null) {
			contextId = A2AJson.newId();
		}

		A2ATask task;
		synchronized (_tasks) {
			A2ATask active = activeTask(contextId);
			if (active != null) {
				// Peers that share one account are told apart by the application they name, so one of
				// them cannot answer the question another one was asked.
				if (active.agent().equals(agent) && active.owner().equals(caller)
					&& (peer == null || peer.equals(active.peer())) && active.answer(message)) {
					return active;
				}
				throw new A2AError(A2AError.UNSUPPORTED_OPERATION, "Context " + contextId
					+ " is busy with another task; try again when it has finished, or use another context id.");
			}
			task = new A2ATask(A2AJson.newId(), contextId, agent, caller);
			if (peer != null) {
				task.putMetadata(A2ATask.PEER, peer);
			}
			task.observeWith(_observer);
			task.addHistory(message);
			add(task);
		}
		_backend.start(task, Json.copy(message), metadata);
		return task;
	}

	private A2ATask ownTask(String caller, String agent, String taskId) {
		if (taskId == null) {
			throw new A2AError(A2AError.INVALID_PARAMS, "A task id is required.");
		}
		A2ATask task = task(taskId, caller);
		if (task == null || !task.owner().equals(caller) || !task.agent().equals(agent)) {
			throw new A2AError(A2AError.TASK_NOT_FOUND, "Task not found: " + taskId);
		}
		return task;
	}

	/**
	 * The task with the given id - in memory, or read back by the backend - or <code>null</code>.
	 */
	public A2ATask task(String taskId, String owner) {
		synchronized (_tasks) {
			A2ATask task = _tasks.get(taskId);
			if (task != null) {
				return task;
			}
		}
		A2ATask restored = _backend.findTask(taskId, owner);
		if (restored != null) {
			synchronized (_tasks) {
				add(restored);
			}
		}
		return restored;
	}

	private Map<String, Object> cancel(String caller, String agent, String taskId) {
		A2ATask task = ownTask(caller, agent, taskId);
		if (!task.cancel("Canceled by the client.")) {
			throw new A2AError(A2AError.TASK_NOT_CANCELABLE,
				"Task " + taskId + " is already " + task.state().wireName() + ".");
		}
		return task.toJson(null);
	}

	private Map<String, Object> list(String caller, String agent, Map<String, Object> params) {
		String contextId = Json.str(params, "contextId");
		TaskState status = TaskState.parse(Json.str(params, "status"));
		Integer pageSize = Json.integer(params, "pageSize");
		int limit = pageSize == null || pageSize <= 0 ? 50 : Math.min(pageSize, 100);
		List<A2ATask> matching = new ArrayList<>();
		synchronized (_tasks) {
			for (A2ATask task : _tasks.values()) {
				if (task.owner().equals(caller) && task.agent().equals(agent)
					&& (contextId == null || task.contextId().equals(contextId))
					&& (status == null || task.state() == status)) {
					matching.add(task);
				}
			}
		}
		Collections.reverse(matching);
		Integer historyLength = Json.integer(params, "historyLength");
		List<Object> tasks = new ArrayList<>();
		for (A2ATask task : matching.subList(0, Math.min(limit, matching.size()))) {
			tasks.add(task.toJson(historyLength));
		}
		return Json.map("tasks", tasks, "nextPageToken", "", "pageSize", limit, "totalSize", matching.size());
	}

	private void subscribe(Transport transport, Object id, String caller, String agent, String taskId)
			throws IOException {
		A2ATask task = ownTask(caller, agent, taskId);
		if (task.state().isTerminal()) {
			throw new A2AError(A2AError.UNSUPPORTED_OPERATION, "Task " + taskId + " has finished.");
		}
		stream(transport, id, task);
	}

	/**
	 * Streams a task: its current state first, then every change, until it ends or asks.
	 */
	private void stream(Transport transport, Object id, A2ATask task) throws IOException {
		A2ATask.Snapshot snapshot = task.snapshot();
		EventSink sink = transport.openStream();
		sink.send(Json.write(response(id, "result", Json.map("task", snapshot.task()))));
		if (A2AJson.state(snapshot.task()).endsWait()) {
			return;
		}
		int index = snapshot.eventIndex();
		while (true) {
			List<Map<String, Object>> events = task.awaitEvents(index, KEEP_ALIVE_MILLIS);
			if (events.isEmpty()) {
				sink.keepAlive();
			}
			for (Map<String, Object> event : events) {
				index++;
				sink.send(Json.write(response(id, "result", event)));
				TaskState state = A2AJson.state(Json.obj(event, "statusUpdate"));
				if (state != null && state.endsWait()) {
					return;
				}
			}
		}
	}

	private A2ATask activeTask(String contextId) {
		A2ATask result = null;
		for (A2ATask task : _tasks.values()) {
			if (task.contextId().equals(contextId) && !task.state().isTerminal()) {
				result = task;
			}
		}
		return result;
	}

	private void add(A2ATask task) {
		_tasks.put(task.id(), task);
		for (Iterator<A2ATask> it = _tasks.values().iterator(); it.hasNext() && _tasks.size() > MAX_TASKS;) {
			if (it.next().state().isTerminal()) {
				it.remove();
			}
		}
	}
}
