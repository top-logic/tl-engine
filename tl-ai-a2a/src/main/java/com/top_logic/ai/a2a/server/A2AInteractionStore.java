/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;

/**
 * The "user" of an A2A run is the remote agent: a question the local agent asks with its
 * {@code askUser} tool becomes the task's {@code INPUT_REQUIRED} status, and the client's next
 * message on the task is the answer.
 *
 * <p>
 * {@code AskUserTool} parks the run's thread on the request's future, exactly as in a chat; this
 * store additionally publishes the question on the task and routes the answer back into the future.
 * So the run resumes where it asked - nothing is restarted, and the conversation goes on in the same
 * turn. Cancelling the task cancels the run's token, which resolves the parked question as
 * {@code cancelled} (the tool's own cancel hook).
 * </p>
 */
final class A2AInteractionStore extends UserInteractionStore {

	private final A2ATask _task;

	private final Runnable _onQuestion;

	/**
	 * @param onQuestion
	 *        Called on the asking thread once the question is published (persists the task).
	 */
	A2AInteractionStore(A2ATask task, Runnable onQuestion) {
		_task = task;
		_onQuestion = onQuestion;
	}

	@Override
	public UserInteractionRequest createRequest(String question, String header, String type, String options,
			boolean multiSelect) {
		UserInteractionRequest request = super.createRequest(question, header, type, options, multiSelect);
		boolean waiting = _task.requestInput(questionMessage(question, header, type, options, multiSelect),
			answer -> resolve(request.getId(), answer));
		if (!waiting) {
			// The task ended (canceled) while the agent was asking.
			resolve(request.getId(), "cancelled");
		} else {
			_onQuestion.run();
		}
		return request;
	}

	private Map<String, Object> questionMessage(String question, String header, String type, String options,
			boolean multiSelect) {
		StringBuilder text = new StringBuilder(question == null ? "" : question);
		Object parsedOptions = null;
		if ("choice".equals(type) && options != null && !options.isBlank()) {
			try {
				parsedOptions = Json.parse(options);
			} catch (IllegalArgumentException ex) {
				parsedOptions = options;
			}
			List<String> labels = labels(parsedOptions);
			if (!labels.isEmpty()) {
				text.append("\n\nOptions").append(multiSelect ? " (several possible)" : "").append(": ")
					.append(String.join(" | ", labels));
			}
		}
		List<Map<String, Object>> parts = new ArrayList<>();
		parts.add(A2AJson.textPart(text.toString()));
		parts.add(A2AJson.dataPart(Json.map("question", question, "header", header, "type", type,
			"options", parsedOptions, "multiSelect", multiSelect)));
		return A2AJson.message(A2AJson.ROLE_AGENT, _task.contextId(), _task.id(), parts);
	}

	private static List<String> labels(Object options) {
		List<String> result = new ArrayList<>();
		if (options instanceof List<?> list) {
			for (Object option : list) {
				if (option instanceof String label) {
					result.add(label);
				} else if (Json.str(option, "label") != null) {
					result.add(Json.str(option, "label"));
				}
			}
		}
		return result;
	}
}
