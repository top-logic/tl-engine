/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.client;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.top_logic.ai.a2a.ContextBook;
import com.top_logic.ai.a2a.client.RemoteAgents.RemoteAgent;
import com.top_logic.ai.a2a.protocol.A2AError;
import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * One exchange of a local agent with a remote A2A agent: send a message (a new one, or the answer to
 * the remote agent's question), follow the remote task as a stream - reporting its progress - until
 * it ends, asks back or the wait ends, and report the outcome.
 *
 * <p>
 * Independent of the tool API: {@code A2AClientTools} adapts it, and a test drives it against an
 * in-process server.
 * </p>
 */
public final class RemoteExchange {

	/** How much of the remote agent's answer or question the caller's model reads at once. */
	public static final int MAX_ANSWER_CHARS = 4000;

	/** How much of an artifact without a reference the caller's model reads. */
	private static final int MAX_EXCERPT_CHARS = 300;

	/** How long a unary call besides the wait may take. */
	private static final Duration CALL_TIMEOUT = Duration.ofSeconds(60);

	private final RemoteAgents _agents;

	private final ContextBook _contexts;

	/**
	 * Who calls: the local application, its agent and conversation.
	 *
	 * @param conversationId
	 *        The local conversation, or <code>null</code>.
	 * @param subject
	 *        The local conversation's subject, remembered with the context id, or <code>null</code>.
	 * @param subjectLabel
	 *        The subject's label, sent to the peer, or <code>null</code>.
	 */
	public record Caller(String app, String agent, String conversationId, Object subject, String subjectLabel) {
		// Pure value.
	}

	/**
	 * The outcome of an exchange, as far as it went.
	 */
	public static final class Outcome {

		String _agent;

		String _label;

		String _taskId;

		String _contextId;

		TaskState _state;

		String _statusText;

		Object _statusData;

		final Map<String, Map<String, Object>> _artifacts = new LinkedHashMap<>();

		long _waitedSeconds;

		/** The remote task id, or <code>null</code>. */
		public String taskId() {
			return _taskId;
		}

		/** The context id. */
		public String contextId() {
			return _contextId;
		}

		/** The last known state, or <code>null</code>. */
		public TaskState state() {
			return _state;
		}

		/** The text of the last status message (answer, question or error), or <code>null</code>. */
		public String statusText() {
			return _statusText;
		}

		/** The artifacts received, in order. */
		public List<Map<String, Object>> artifacts() {
			return new ArrayList<>(_artifacts.values());
		}

		void acceptTask(Map<String, Object> task) {
			_taskId = first(Json.str(task, "id"), _taskId);
			_contextId = first(Json.str(task, "contextId"), _contextId);
			acceptStatus(Json.obj(task, "status"));
			for (Object artifact : Json.list(task, "artifacts")) {
				acceptArtifact(Json.asObject(artifact));
			}
		}

		void acceptStatus(Map<String, Object> status) {
			TaskState state = TaskState.parse(Json.str(status, "state"));
			_state = first(state, _state);
			Map<String, Object> message = Json.obj(status, "message");
			if (message != null && state != TaskState.WORKING) {
				_statusText = A2AJson.plainText(message);
				_statusData = null;
				for (Map<String, Object> part : A2AJson.parts(message)) {
					_statusData = first(A2AJson.partData(part), _statusData);
				}
			}
		}

		void acceptArtifact(Map<String, Object> artifact) {
			if (artifact != null) {
				String id = first(Json.str(artifact, "artifactId"), Json.str(artifact, "name"));
				_artifacts.put(id == null ? String.valueOf(_artifacts.size()) : id, artifact);
			}
		}
	}

	/**
	 * Creates a {@link RemoteExchange}.
	 */
	public RemoteExchange(RemoteAgents agents, ContextBook contexts) {
		_agents = agents;
		_contexts = contexts;
	}

	/**
	 * Sends a new message to a remote agent and follows the task.
	 *
	 * @param conversation
	 *        Which remote conversation, see {@link ContextBook#choose}.
	 * @param data
	 *        A structured payload sent as a data part, or <code>null</code>.
	 * @param refs
	 *        References handed over instead of their content.
	 * @param progress
	 *        Receives one line per remote progress step.
	 * @param cancelled
	 *        Whether the local run was cancelled.
	 */
	public Outcome ask(Caller caller, String agentName, String text, String conversation, String topic, Object data,
			List<String> refs, long waitMillis, Consumer<String> progress, BooleanSupplier cancelled)
			throws IOException, InterruptedException {
		RemoteAgent agent = _agents.get(agentName);
		ContextBook.Choice choice = _contexts.choose(caller.conversationId(), agent.name(), conversation, topic, text);
		_contexts.rememberSubject(choice.contextId(), caller.subject());
		List<Map<String, Object>> parts = new ArrayList<>();
		parts.add(A2AJson.textPart(text));
		if (data != null) {
			parts.add(A2AJson.dataPart(data));
		}
		for (String ref : refs) {
			parts.add(Json.map("url", ref, "mediaType", ArtifactRef.MEDIA_TYPE));
		}
		Outcome outcome = new Outcome();
		outcome._label = choice.label();
		return exchange(caller, agent, A2AJson.message(A2AJson.ROLE_USER, choice.contextId(), null, parts),
			waitMillis, progress, cancelled, outcome);
	}

	/**
	 * Answers a remote agent's question and follows the task again.
	 *
	 * @param agentName
	 *        The remote agent; <code>null</code>: the one remembered for the task.
	 */
	public Outcome reply(Caller caller, String agentName, String taskId, String text, long waitMillis,
			Consumer<String> progress, BooleanSupplier cancelled) throws IOException, InterruptedException {
		ContextBook.RemoteTask known = _contexts.remoteTask(taskId);
		RemoteAgent agent = agentOf(agentName, known, taskId);
		Map<String, Object> message = A2AJson.message(A2AJson.ROLE_USER, known == null ? null : known.contextId(),
			taskId, List.of(A2AJson.textPart(text)));
		return exchange(caller, agent, message, waitMillis, progress, cancelled, new Outcome());
	}

	/**
	 * Reads a remote task and, while it is still working, follows it until it ends or asks back, at
	 * most {@code waitMillis}.
	 *
	 * @param waitMillis
	 *        0: answer the current state at once.
	 */
	public Outcome follow(Caller caller, String agentName, String taskId, long waitMillis, Consumer<String> progress,
			BooleanSupplier cancelled) throws IOException, InterruptedException {
		RemoteAgent agent = agentOf(agentName, _contexts.remoteTask(taskId), taskId);
		Outcome outcome = get(agent, taskId);
		for (ContextBook.RemoteConversation known : _contexts.conversations(caller.conversationId())) {
			if (known.contextId().equals(outcome._contextId)) {
				outcome._label = known.label();
			}
		}
		if (waitMillis > 0 && outcome._state != null && !outcome._state.endsWait()) {
			long start = System.currentTimeMillis();
			try {
				_agents.client().stream(_agents.endpoint(agent), agent.token(), "SubscribeToTask", Json.map("id", taskId),
					follower(outcome, agent, progress), waitMillis, cancelled);
			} catch (A2AError ex) {
				if (ex.code() != A2AError.UNSUPPORTED_OPERATION) {
					throw ex;
				}
				// The task ended between reading and subscribing.
			}
			outcome.acceptTask(Json.asObject(call(agent, "GetTask", taskId)));
			outcome._waitedSeconds = (System.currentTimeMillis() - start) / 1000;
		}
		_contexts.record(caller.conversationId(), agent.name(), outcome._contextId, null, taskId, outcome._state);
		return outcome;
	}

	private RemoteAgent agentOf(String agentName, ContextBook.RemoteTask known, String taskId) {
		if (agentName != null && !agentName.isBlank()) {
			return _agents.get(agentName);
		}
		if (known == null) {
			throw new A2AError(A2AError.TASK_NOT_FOUND, "Unknown task " + taskId + "; name the agent it belongs to.");
		}
		return _agents.get(known.agent());
	}

	private Outcome get(RemoteAgent agent, String taskId) throws IOException, InterruptedException {
		Outcome outcome = new Outcome();
		outcome._agent = agent.name();
		outcome.acceptTask(Json.asObject(call(agent, "GetTask", taskId)));
		return outcome;
	}

	private Object call(RemoteAgent agent, String method, String taskId) throws IOException, InterruptedException {
		return _agents.client().call(_agents.endpoint(agent), agent.token(), method, Json.map("id", taskId), CALL_TIMEOUT);
	}

	private Outcome exchange(Caller caller, RemoteAgent agent, Map<String, Object> message, long waitMillis,
			Consumer<String> progress, BooleanSupplier cancelled, Outcome outcome)
			throws IOException, InterruptedException {
		message.put("metadata", Json.map(A2AJson.CALLER_METADATA, Json.map("app", caller.app(), "agent", caller.agent(),
			"conversationId", caller.conversationId(), "subject", caller.subjectLabel())));
		outcome._agent = agent.name();
		outcome._contextId = Json.str(message, "contextId");
		outcome._taskId = Json.str(message, "taskId");

		long start = System.currentTimeMillis();
		_agents.client().stream(_agents.endpoint(agent), agent.token(), "SendStreamingMessage",
			Json.map("message", message), follower(outcome, agent, progress), waitMillis, cancelled);
		outcome._waitedSeconds = (System.currentTimeMillis() - start) / 1000;

		if (outcome._taskId != null) {
			try {
				if (cancelled.getAsBoolean() && (outcome._state == null || !outcome._state.endsWait())) {
					call(agent, "CancelTask", outcome._taskId);
					outcome._state = TaskState.CANCELED;
				} else {
					// The authoritative final state, with every artifact.
					outcome.acceptTask(Json.asObject(call(agent, "GetTask", outcome._taskId)));
				}
			} catch (IOException | A2AError ex) {
				// What the stream said stands.
			}
		}
		_contexts.record(caller.conversationId(), agent.name(), outcome._contextId, outcome._label, outcome._taskId,
			outcome._state);
		return outcome;
	}

	/**
	 * Reads the events of a remote task's stream into {@code outcome}, reporting progress, until the
	 * task ends or asks back.
	 */
	private A2AClient.StreamListener follower(Outcome outcome, RemoteAgent agent, Consumer<String> progress) {
		return result -> {
			Map<String, Object> task = Json.obj(result, "task");
			Map<String, Object> update = Json.obj(result, "statusUpdate");
			Map<String, Object> artifact = Json.obj(Json.obj(result, "artifactUpdate"), "artifact");
			if (task != null) {
				outcome.acceptTask(task);
			} else if (update != null) {
				outcome._taskId = first(Json.str(update, "taskId"), outcome._taskId);
				outcome._contextId = first(Json.str(update, "contextId"), outcome._contextId);
				outcome.acceptStatus(Json.obj(update, "status"));
				if (outcome._state == TaskState.WORKING) {
					String note = A2AJson.plainText(Json.obj(Json.obj(update, "status"), "message"));
					progress.accept(agent.displayName() + ": " + (note.isBlank() ? "working" : note));
				}
			} else if (artifact != null) {
				outcome.acceptArtifact(artifact);
				return true;
			} else if (Json.obj(result, "message") != null) {
				// A direct message instead of a task: the whole answer.
				outcome._state = TaskState.COMPLETED;
				outcome._statusText = A2AJson.plainText(Json.obj(result, "message"));
				return false;
			}
			if (outcome._taskId != null) {
				_contexts.rememberTask(outcome._taskId, new ContextBook.RemoteTask(agent.name(), outcome._contextId));
			}
			return outcome._state == null || !outcome._state.endsWait();
		};
	}

	/**
	 * The outcome as a tool result for the calling model: the facts in the clear, and what the remote
	 * agent wrote inside {@code envelope} - its answer (or question) up to {@link #MAX_ANSWER_CHARS},
	 * and its artifacts as references only, never their bodies.
	 *
	 * @param envelope
	 *        Wraps the remote content: <code>(source, body) -&gt; marked text</code>.
	 */
	public static String format(Outcome outcome, BiFunction<String, String, String> envelope) {
		TaskState state = outcome._state;
		StringBuilder out = new StringBuilder("Remote agent: ").append(outcome._agent);
		if (outcome._label != null) {
			out.append("\nconversation: \"").append(outcome._label).append('"');
		}
		out.append("\nstate: ").append(state == null ? "UNKNOWN" : state.name());
		out.append("\ntaskId: ").append(outcome._taskId);
		out.append("\ncontextId: ").append(outcome._contextId);
		out.append("\n\n");
		if (state == TaskState.COMPLETED) {
			out.append("The remote agent finished. Its answer and the references to its artifacts follow.");
		} else if (state == TaskState.INPUT_REQUIRED) {
			out.append("The remote agent asks you a question (\"question\" below) and waits. Answer it with "
				+ "replyAgent(taskId=\"").append(outcome._taskId).append("\", message=...) - from what you know, or ask "
				+ "your user first if you cannot answer it yourself.");
		} else if (state == null || !state.isTerminal()) {
			out.append("The remote agent is still working (waited ").append(outcome._waitedSeconds)
				.append(" s). Wait for it with getAgentTask(taskId=\"").append(outcome._taskId)
				.append("\") - do not send it another message meanwhile.");
		} else {
			out.append("The remote task ended without a result (").append(state.name()).append(").");
		}

		Map<String, Object> answer = null;
		for (Map<String, Object> artifact : outcome.artifacts()) {
			if (A2AJson.ANSWER.equals(Json.str(artifact, "name"))) {
				answer = artifact;
			}
		}
		List<String> content = new ArrayList<>();
		if (outcome._statusText != null) {
			String heading = state == TaskState.INPUT_REQUIRED ? "question" : state == TaskState.COMPLETED ? "answer" : "message";
			String details = state == TaskState.INPUT_REQUIRED && outcome._statusData != null
				? "\n\nquestion details: " + Json.write(outcome._statusData) : "";
			content.add(heading + ":\n" + capped(outcome._statusText, refOf(answer)) + details);
		}
		StringBuilder artifacts = new StringBuilder();
		for (Map<String, Object> artifact : outcome.artifacts()) {
			if (artifact != answer) {
				artifacts.append(describe(artifact));
			}
		}
		if (artifacts.length() > 0) {
			content.add("artifacts (read one with fetchArtifact(ref=..., section=...)):" + artifacts);
		}
		if (!content.isEmpty()) {
			out.append("\n\n").append(envelope.apply("a2a:" + outcome._agent, String.join("\n\n", content)));
		}
		return out.toString();
	}

	/**
	 * One artifact as a list entry: name, media type, size, description and its reference - or, for a
	 * peer that hands out no references, a glimpse, never the body.
	 */
	private static String describe(Map<String, Object> artifact) {
		String name = Json.str(artifact, "name");
		String description = Json.str(artifact, "description");
		String ref = refOf(artifact);
		List<Map<String, Object>> parts = A2AJson.parts(artifact);
		String mediaType = parts.isEmpty() ? null : Json.str(parts.get(0), "mediaType");
		String text = !parts.isEmpty() && ArtifactRef.isRef(A2AJson.partUrl(parts.get(0))) ? null : A2AJson.text(artifact);
		StringBuilder out = new StringBuilder("\n- ").append(name == null ? "?" : name)
			.append(" (").append(mediaType == null ? "text" : mediaType)
			.append(text == null ? "" : ", " + text.length() + " characters").append(")");
		if (description != null) {
			out.append(" - ").append(description);
		}
		if (ref != null) {
			out.append("\n  ref: ").append(ref);
		} else if (text != null) {
			String excerpt = text.length() <= MAX_EXCERPT_CHARS ? text : text.substring(0, MAX_EXCERPT_CHARS) + "…";
			out.append("\n  excerpt: ").append(excerpt.replace('\n', ' '));
		}
		return out.toString();
	}

	private static String capped(String text, String fullRef) {
		if (text.length() <= MAX_ANSWER_CHARS) {
			// The reference lets the caller hand the answer on without repeating it.
			return fullRef == null ? text : text + "\n[answer ref: " + fullRef + "]";
		}
		return text.substring(0, MAX_ANSWER_CHARS) + "\n[... cut after " + MAX_ANSWER_CHARS + " of " + text.length()
			+ " characters" + (fullRef == null ? "" : " - full answer: fetchArtifact(ref=\"" + fullRef + "\")") + "]";
	}

	private static String refOf(Map<String, Object> artifact) {
		String ref = Json.str(Json.obj(artifact, "metadata"), "ref");
		if (ref != null) {
			return ref;
		}
		List<Map<String, Object>> parts = A2AJson.parts(artifact);
		String url = parts.isEmpty() ? null : A2AJson.partUrl(parts.get(0));
		return ArtifactRef.isRef(url) ? url : null;
	}

	private static <T> T first(T a, T b) {
		return a != null ? a : b;
	}
}
