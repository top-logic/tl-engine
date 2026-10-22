/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInboxBinding;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * What a background subagent is shown of its inbox at the start of a turn: the lead's messages under
 * the lead's opening line, the user's under the user's, each announced so that its log records it and
 * the chat can tell the user which of their messages were read.
 */
public class TestInboxDelivery extends TestCase {

	private final SubagentInbox _inbox = new SubagentInbox();

	private final Conversation _conversation = new Conversation();

	private final List<AgentEvent> _events = new ArrayList<>();

	/** The user's words are not passed off as the lead's, and the user has the last word. */
	public void testTheUsersMessagesAreTheirsAndComeLast() {
		SubagentInbox.Message fromUser = SubagentInbox.Message.of(SubagentInbox.Sender.USER, "Nur 2026.");
		SubagentInbox.Message fromLead = SubagentInbox.Message.of(SubagentInbox.Sender.LEAD, "Auch 2025.");
		SubagentInbox.Message again = SubagentInbox.Message.of(SubagentInbox.Sender.USER, "Und nach Ort.");
		_inbox.post(fromUser);
		_inbox.post(fromLead);
		_inbox.post(again);

		deliver();

		List<String> texts = texts();
		assertEquals(2, texts.size());
		assertEquals(AgentLoop.LEAD_MESSAGE_NOTICE + "\n\nAuch 2025.", texts.get(0));
		assertEquals(AgentLoop.USER_MESSAGE_NOTICE + "\n\nNur 2026.\n\nUnd nach Ort.", texts.get(1));
		assertFalse("The user's words never stand under the lead's line.", texts.get(0).contains("Nur 2026."));

		assertEquals(2, _events.size());
		assertTrue(_events.get(0) instanceof AgentEvent.LeadMessageDelivered);
		AgentEvent.UserMessageDelivered read = (AgentEvent.UserMessageDelivered) _events.get(1);
		assertEquals("The model saw what the event says.", texts.get(1), read.text());
		assertEquals("The receipt names the messages read, in the order sent.",
			List.of(fromUser.id(), again.id()), read.messages().stream().map(SubagentInbox.Message::id).toList());
	}

	/** A turn with nothing in the inbox is untouched, and so is a run that reads no inbox. */
	public void testNothingIsInjectedWithoutMessages() {
		deliver();
		AgentLoop.injectInboxMessages(_conversation, _events::add, "run", 1);

		assertTrue(_conversation.messages().isEmpty());
		assertTrue(_events.isEmpty());
	}

	/** A message is read once. */
	public void testAMessageIsReadOnce() {
		_inbox.post(SubagentInbox.Message.of(SubagentInbox.Sender.USER, "Nur 2026."));

		deliver();
		deliver();

		assertEquals(1, texts().size());
		assertEquals(1, _events.size());
	}

	private void deliver() {
		try (SubagentInboxBinding.Binding bound = SubagentInboxBinding.bind(_inbox)) {
			AgentLoop.injectInboxMessages(_conversation, _events::add, "run", 1);
		}
	}

	private List<String> texts() {
		List<String> texts = new ArrayList<>();
		for (Message message : _conversation.messages()) {
			UserMessage user = (UserMessage) message;
			texts.add(((TextContent) user.content().get(0)).text());
		}
		return texts;
	}
}
