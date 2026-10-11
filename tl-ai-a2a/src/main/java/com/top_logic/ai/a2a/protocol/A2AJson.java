/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Builders and readers for the A2A v1.0 data objects (Message, Part, Artifact, TaskStatus) as JSON
 * trees.
 */
public final class A2AJson {

	/** Role of the requesting side. */
	public static final String ROLE_USER = "ROLE_USER";

	/** Role of the answering agent. */
	public static final String ROLE_AGENT = "ROLE_AGENT";

	/** Media type of a structured data part. */
	public static final String JSON_MEDIA_TYPE = "application/json";

	/** Name of the artifact that carries an agent's final answer. */
	public static final String ANSWER = "answer";

	/**
	 * Message metadata entry in which a tl-ai caller names itself:
	 * <code>{"app", "agent", "conversationId", "subject"}</code>.
	 */
	public static final String CALLER_METADATA = "tlAi";

	private A2AJson() {
		// Static utility.
	}

	/**
	 * A fresh unique id (messages, tasks, contexts, artifacts).
	 */
	public static String newId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * A text part.
	 */
	public static Map<String, Object> textPart(String text) {
		return Json.map("text", text == null ? "" : text);
	}

	/**
	 * A structured data part.
	 */
	public static Map<String, Object> dataPart(Object data) {
		return Json.map("data", data, "mediaType", JSON_MEDIA_TYPE);
	}

	/**
	 * A message.
	 *
	 * @param role
	 *        {@link #ROLE_USER} or {@link #ROLE_AGENT}.
	 */
	public static Map<String, Object> message(String role, String contextId, String taskId,
			List<Map<String, Object>> parts) {
		return Json.map("messageId", newId(), "contextId", contextId, "taskId", taskId, "role", role,
			"parts", new ArrayList<>(parts));
	}

	/**
	 * A message with a single text part.
	 */
	public static Map<String, Object> textMessage(String role, String contextId, String taskId, String text) {
		return message(role, contextId, taskId, List.of(textPart(text)));
	}

	/**
	 * An artifact.
	 */
	public static Map<String, Object> artifact(String name, String description, List<Map<String, Object>> parts) {
		return Json.map("artifactId", newId(), "name", name, "description", description,
			"parts", new ArrayList<>(parts));
	}

	/**
	 * A task status.
	 */
	public static Map<String, Object> status(TaskState state, Map<String, Object> message, Instant timestamp) {
		return Json.map("state", state.wireName(), "message", message, "timestamp", timestamp.toString());
	}

	/**
	 * The state of a task, or <code>null</code>.
	 */
	public static TaskState state(Map<String, Object> task) {
		return TaskState.parse(Json.str(Json.obj(task, "status"), "state"));
	}

	/**
	 * The parts of a message or artifact.
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> parts(Object messageOrArtifact) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object part : Json.list(messageOrArtifact, "parts")) {
			if (part instanceof Map) {
				result.add((Map<String, Object>) part);
			}
		}
		return result;
	}

	/**
	 * The text of a part, or <code>null</code> if it is no text part.
	 */
	public static String partText(Map<String, Object> part) {
		return Json.str(part, "text");
	}

	/**
	 * The value of a data part, or <code>null</code>.
	 */
	public static Object partData(Map<String, Object> part) {
		return part.containsKey("text") ? null : part.get("data");
	}

	/**
	 * The URL of a file part, or <code>null</code>.
	 */
	public static String partUrl(Map<String, Object> part) {
		return Json.str(part, "url");
	}

	/**
	 * The calling application as a tl-ai caller names itself in the message metadata, or
	 * <code>null</code>.
	 */
	public static String peerOf(Map<String, Object> message) {
		return Json.str(Json.obj(Json.obj(message, "metadata"), CALLER_METADATA), "app");
	}

	/**
	 * Everything a message or artifact says, as one text for a model: text parts verbatim, data parts
	 * as JSON blocks, references and files as their URL.
	 */
	public static String text(Object messageOrArtifact) {
		return text(messageOrArtifact, data -> null);
	}

	/**
	 * The text of a message or artifact, with each data part rendered by a function first.
	 *
	 * @param data
	 *        Renders a data part's value as text, or answers <code>null</code> to render it as JSON.
	 */
	public static String text(Object messageOrArtifact, Function<Object, String> data) {
		List<String> pieces = new ArrayList<>();
		for (Map<String, Object> part : parts(messageOrArtifact)) {
			String text = partText(part);
			Object value = partData(part);
			String url = partUrl(part);
			String name = Json.str(part, "filename");
			String label = name == null ? "" : " " + name;
			if (text != null) {
				pieces.add(text);
			} else if (value != null) {
				String rendered = data.apply(value);
				pieces.add(rendered != null ? rendered : "```json\n" + Json.write(value) + "\n```");
			} else if (ArtifactRef.isRef(url)) {
				pieces.add("[artifact" + label + ": " + url + " - read it, or one section of it, with fetchArtifact(ref=\""
					+ url + "\")]");
			} else if (url != null) {
				pieces.add("[file" + label + ": " + url + "]");
			} else if (part.get("raw") != null) {
				pieces.add("[inline file" + label + " omitted]");
			}
		}
		return String.join("\n\n", pieces);
	}

	/**
	 * Only the text parts of a message or artifact, joined.
	 */
	public static String plainText(Object messageOrArtifact) {
		List<String> pieces = new ArrayList<>();
		for (Map<String, Object> part : parts(messageOrArtifact)) {
			if (partText(part) != null) {
				pieces.add(partText(part));
			}
		}
		return String.join("\n\n", pieces);
	}
}
