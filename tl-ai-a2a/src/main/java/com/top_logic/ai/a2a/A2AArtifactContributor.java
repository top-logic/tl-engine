/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import com.top_logic.model.TLObject;

/**
 * Application hook: artifacts a completed A2A task returns besides the agent's answer.
 *
 * <p>
 * Runs after the agent's run completed, in the {@code TLContext} of the account the run acted as, so
 * it reads the conversation's subject and what the run created. Contributors are asked in order
 * ({@code <artifact-contributors>} on {@link A2AService}); one that throws is logged and skipped.
 * </p>
 */
public interface A2AArtifactContributor {

	/**
	 * Adds artifacts for a completed task.
	 */
	void contribute(Request request, Sink sink);

	/**
	 * The completed task.
	 *
	 * @param agent
	 *        The exposed agent's path.
	 * @param agentName
	 *        The tl-ai agent that served it.
	 * @param request
	 *        The text of the message the task answered.
	 * @param answer
	 *        The agent's final answer.
	 * @param subject
	 *        The conversation's subject, or <code>null</code>.
	 */
	record Request(String agent, String agentName, String contextId, String taskId, String request,
			String answer, TLObject subject) {
		// Pure value.
	}

	/**
	 * Receives the artifacts.
	 */
	interface Sink {

		/**
		 * Adds a text artifact.
		 *
		 * @param mediaType
		 *        E.g. {@code text/markdown}; <code>null</code> for plain text.
		 */
		void text(String name, String description, String text, String mediaType);

		/**
		 * Adds a structured artifact (a JSON value).
		 */
		void data(String name, String description, Object json);

		/**
		 * Adds an artifact by reference: the caller gets the reference and reads what it needs with
		 * {@code fetchArtifact(ref, section)}.
		 *
		 * @param ref
		 *        <code>tl-artifact://&lt;app&gt;/&lt;kind&gt;/&lt;id&gt;[@version]</code>, published by an
		 *        {@link A2AArtifactSource}.
		 * @param mediaType
		 *        The media type of what the reference names, or <code>null</code>.
		 */
		void reference(String name, String description, String ref, String mediaType);
	}
}
