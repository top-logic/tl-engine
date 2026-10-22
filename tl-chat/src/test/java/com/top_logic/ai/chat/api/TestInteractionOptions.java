/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import junit.framework.TestCase;

import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.UserInteractionEvent;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;

/**
 * Verifies that {@link DtoMapper#toUserInteractionEvent(UserInteractionRequest)} extracts the
 * choice options regardless of whether the model emitted them as a JSON array of bare strings
 * ({@code ["A","B"]}) or as a JSON array of objects ({@code [{"label":"A"}]}).
 */
public class TestInteractionOptions extends TestCase {

	private static UserInteractionRequest choiceRequest(String options) {
		return new UserInteractionRequest(
			"interaction-test-1", "Shall I proceed with updating the tool?", "Approve", "choice",
			options, false, 0L, new CompletableFuture<>());
	}

	/**
	 * String-array form as produced by the agent prompts, e.g.
	 * {@code askUser(type="choice", options=["Approve", "Revise"])}.
	 */
	public void testStringArrayOptions() {
		UserInteractionEvent event =
			DtoMapper.toUserInteractionEvent(choiceRequest("[\"Approve\", \"Revise\"]"));

		List<InteractionOptionDto> options = event.getOptions();
		assertEquals("Both string options must be parsed into selectable options.", 2, options.size());
		assertEquals("Approve", options.get(0).getLabel());
		assertEquals("Revise", options.get(1).getLabel());
	}

	/** Object-array form as documented on the tool, e.g. {@code [{"label":"Approve"}]}. */
	public void testObjectArrayOptions() {
		UserInteractionEvent event = DtoMapper.toUserInteractionEvent(
			choiceRequest("[{\"label\":\"Approve\"},{\"label\":\"Revise\",\"description\":\"Change it\"}]"));

		List<InteractionOptionDto> options = event.getOptions();
		assertEquals(2, options.size());
		assertEquals("Approve", options.get(0).getLabel());
		assertEquals("Revise", options.get(1).getLabel());
		assertEquals("Change it", options.get(1).getDescription());
	}

	/** A mixed array (strings and objects) must still yield one option per element. */
	public void testMixedOptions() {
		UserInteractionEvent event = DtoMapper.toUserInteractionEvent(
			choiceRequest("[\"Approve\", {\"label\":\"Revise\"}]"));

		List<InteractionOptionDto> options = event.getOptions();
		assertEquals(2, options.size());
		assertEquals("Approve", options.get(0).getLabel());
		assertEquals("Revise", options.get(1).getLabel());
	}
}
