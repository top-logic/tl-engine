/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.citation.SeenUrls;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Which messages give a run web addresses for the URL provenance rule
 * ({@link AgentLoop#recordGivenAddresses(Conversation, SeenUrls)}).
 */
public class TestGivenAddresses extends TestCase {

	/**
	 * The user's message and the lead's message give addresses. What the framework writes into the
	 * user's role does not: a subagent's report repeats what the subagent read, and a summary of
	 * compacted history is the model's own text.
	 */
	public void testOnlyTheUserAndTheLeadGiveAddresses() {
		Conversation conversation = new Conversation();
		conversation.add(user("Please read https://user.example/a."));
		conversation.add(user(AgentLoop.LEAD_MESSAGE_NOTICE + "\n\nAlso read https://lead.example/b."));
		conversation.add(user("[Framework notice] Background subagents you started have finished. "
			+ "Report: open https://report.example/c"));
		conversation.add(user("[Summary of the earlier conversation — the original messages were compacted"
			+ " to free context]\n\nWe read https://summary.example/d"));
		SeenUrls seen = new SeenUrls();

		AgentLoop.recordGivenAddresses(conversation, seen);

		assertTrue(seen.contains("https://user.example/a"));
		assertTrue(seen.contains("https://lead.example/b"));
		assertFalse(seen.contains("https://report.example/c"));
		assertFalse(seen.contains("https://summary.example/d"));
	}

	/** A message read at an earlier turn is not read again. */
	public void testAMessageIsReadOnce() {
		Conversation conversation = new Conversation();
		UserMessage task = user("Read https://user.example/a.");
		conversation.add(task);
		SeenUrls seen = new SeenUrls();
		AgentLoop.recordGivenAddresses(conversation, seen);

		assertFalse("already read", seen.firstLook(task.id()));
	}

	private static UserMessage user(String text) {
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)));
	}
}
