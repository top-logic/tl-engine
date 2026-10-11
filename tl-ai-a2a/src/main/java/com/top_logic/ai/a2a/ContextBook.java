/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * Which A2A context ids belong to which local conversation, in both directions.
 *
 * <p>
 * A local conversation talks to its peers in one default context id and may open further, labelled
 * conversations ({@code askAgent(conversation="new", topic=...)}) that later calls continue by their
 * label. A conversation that is itself an incoming A2A context uses that context's id, so a call back
 * to the asking peer arrives in the peer's own context. When this application sends a context id, it
 * remembers the conversation's subject with it; a peer that calls back in that context lands on that
 * subject.
 * </p>
 *
 * <p>
 * In memory and bounded; a restart forgets it. The conversations themselves are agent sessions.
 * </p>
 */
public final class ContextBook {

	private static final int MAX_ENTRIES = 5000;

	/** Local conversation id ("" for an unrecorded run) to its remote conversations. */
	private final Map<String, Local> _locals = lru();

	/** A2A context id to the subject of the local conversation that sent it. */
	private final Map<String, Object> _subjects = lru();

	/** Remote task id to the remote agent that runs it and its context. */
	private final Map<String, RemoteTask> _remoteTasks = lru();

	/**
	 * A conversation with remote agents, as the local conversation that holds it knows it.
	 *
	 * @param label
	 *        A short topic, by which a later call continues it.
	 * @param agent
	 *        The remote agent last talked to in it.
	 */
	public record RemoteConversation(String contextId, String label, String agent, String lastTaskId,
			TaskState lastState) {
		// Pure value.
	}

	/**
	 * The context a call talks in, and its label.
	 */
	public record Choice(String contextId, String label) {
		// Pure value.
	}

	/**
	 * A task this application started at a peer.
	 *
	 * @param agent
	 *        The configured name of the remote agent.
	 */
	public record RemoteTask(String agent, String contextId) {
		// Pure value.
	}

	private static final class Local {

		String _defaultContext;

		final Map<String, RemoteConversation> _byContext = new LinkedHashMap<>();

		final Map<String, String> _lastByAgent = new HashMap<>();
	}

	/**
	 * The default context id of a local conversation towards its peers, created on first use.
	 *
	 * @param conversationId
	 *        The local conversation, or <code>null</code> for an unrecorded run.
	 */
	public synchronized String contextFor(String conversationId) {
		Local local = local(conversationId);
		if (local._defaultContext == null) {
			local._defaultContext = A2AJson.newId();
		}
		return local._defaultContext;
	}

	/**
	 * Binds an incoming context to the local conversation that serves it: the conversation's own calls
	 * use its id by default.
	 */
	public synchronized void bind(String conversationId, String contextId) {
		local(conversationId)._defaultContext = contextId;
	}

	/**
	 * Which context a call to {@code agent} talks in.
	 *
	 * @param choice
	 *        {@code continue} or empty: the conversation last used with this agent, else the default
	 *        one; {@code new} or {@code new:<label>}: a fresh conversation; otherwise the label or the
	 *        context id of a known conversation, or a context id from elsewhere.
	 * @param topic
	 *        A label for a new conversation, or <code>null</code>.
	 * @param message
	 *        The message, whose first words label a new conversation without a topic.
	 */
	public synchronized Choice choose(String conversationId, String agent, String choice, String topic,
			String message) {
		Local local = local(conversationId);
		String wanted = choice == null ? "" : choice.trim();
		String label = topic == null || topic.isBlank() ? null : topic.trim();
		if (wanted.isEmpty() || wanted.equalsIgnoreCase("continue")) {
			String last = local._lastByAgent.get(agent);
			String contextId = last != null ? last : contextFor(conversationId);
			RemoteConversation known = local._byContext.get(contextId);
			return new Choice(contextId, known != null ? known.label()
				: label != null ? label : contextId.equals(local._defaultContext) ? "main" : shortLabel(message));
		}
		if (wanted.equalsIgnoreCase("new") || wanted.regionMatches(true, 0, "new:", 0, 4)) {
			String name = wanted.length() > 4 ? wanted.substring(4).trim() : label;
			return new Choice(A2AJson.newId(), unique(local, name == null || name.isEmpty() ? shortLabel(message) : name));
		}
		for (RemoteConversation conversation : local._byContext.values()) {
			if (wanted.equalsIgnoreCase(conversation.label()) || wanted.equals(conversation.contextId())) {
				return new Choice(conversation.contextId(), conversation.label());
			}
		}
		// A context id from elsewhere: a peer named it in a message.
		return new Choice(wanted, label != null ? label : shortLabel(message));
	}

	/**
	 * Records an exchange in a conversation; a <code>null</code> argument keeps what is known.
	 */
	public synchronized void record(String conversationId, String agent, String contextId, String label,
			String taskId, TaskState state) {
		if (contextId == null) {
			return;
		}
		Local local = local(conversationId);
		RemoteConversation before = local._byContext.remove(contextId);
		local._byContext.put(contextId, new RemoteConversation(contextId,
			label != null || before == null ? label : before.label(), agent,
			taskId != null || before == null ? taskId : before.lastTaskId(),
			state != null || before == null ? state : before.lastState()));
		local._lastByAgent.put(agent, contextId);
	}

	/**
	 * The remote conversations of a local conversation, most recently used first.
	 */
	public synchronized List<RemoteConversation> conversations(String conversationId) {
		List<RemoteConversation> result = new ArrayList<>(local(conversationId)._byContext.values());
		Collections.reverse(result);
		return result;
	}

	/**
	 * Remembers the subject of the conversation that sent {@code contextId}; <code>null</code> keeps
	 * what is remembered.
	 */
	public synchronized void rememberSubject(String contextId, Object subject) {
		if (subject != null) {
			_subjects.put(contextId, subject);
		}
	}

	/**
	 * The subject remembered for a context this application sent, or <code>null</code>.
	 */
	public synchronized Object rememberedSubject(String contextId) {
		return _subjects.get(contextId);
	}

	/**
	 * Remembers which remote agent runs a task.
	 */
	public synchronized void rememberTask(String taskId, RemoteTask task) {
		_remoteTasks.put(taskId, task);
	}

	/**
	 * The remote agent running the task, or <code>null</code>.
	 */
	public synchronized RemoteTask remoteTask(String taskId) {
		return _remoteTasks.get(taskId);
	}

	private Local local(String conversationId) {
		return _locals.computeIfAbsent(conversationId == null ? "" : conversationId, key -> new Local());
	}

	private static String unique(Local local, String label) {
		String result = label;
		for (int n = 2; isTaken(local, result); n++) {
			result = label + " " + n;
		}
		return result;
	}

	private static boolean isTaken(Local local, String label) {
		return local._byContext.values().stream().anyMatch(conversation -> label.equalsIgnoreCase(conversation.label()));
	}

	/**
	 * The first words of a message, as a label.
	 */
	private static String shortLabel(String message) {
		if (message == null || message.isBlank()) {
			return "conversation";
		}
		String[] words = message.strip().split("\\s+");
		StringBuilder out = new StringBuilder(words[0]);
		for (int n = 1; n < words.length && n < 4 && out.length() < 30; n++) {
			out.append(' ').append(words[n]);
		}
		return out.toString();
	}

	private static <V> Map<String, V> lru() {
		return new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, V> eldest) {
				return size() > MAX_ENTRIES;
			}
		};
	}
}
