/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatJoinLog.Message;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * What the join diagnostic says about one rendered window, and - more importantly - when it says
 * nothing.
 */
public class TestChatJoinLog extends TestCase {

	/** A page whose elements all agree gets the tally and no warning, however many they are. */
	public void testOneChatIdNeverWarns() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "support", "chat-conversation");
		log.join("w1", "support", "chat-rail");
		log.join("w1", "support", "chat-progress");

		List<Message> messages = log.messages();
		assertEquals(1, messages.size());
		assertFalse(messages.get(0).warning());
		assertEquals("Chat elements in window 'w1': 'support' (3).", messages.get(0).text());
	}

	/** A single element on a page is the whole page: still no warning. */
	public void testSingleElementNeverWarns() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "default", "chat-conversation");

		List<Message> messages = log.messages();
		assertEquals(1, messages.size());
		assertFalse(messages.get(0).warning());
	}

	/**
	 * Two ids that both hold a transcript are not a typo - they are the shape the transport cannot
	 * carry.
	 *
	 * <p>
	 * This used to be the example of a page that is fine. It is not: every {@code /chat/*} call is
	 * keyed by the window alone, so both conversations attach to one active session and one run,
	 * and each shows the other's answer. No spelling warning (both ids hold several elements), but
	 * the transport warning.
	 * </p>
	 */
	public void testTwoConversationsInOneWindowWarn() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "left", "chat-conversation");
		log.join("w1", "left", "chat-rail");
		log.join("w1", "right", "chat-conversation");
		log.join("w1", "right", "chat-rail");

		List<Message> messages = log.messages();
		assertEquals(2, messages.size());
		assertFalse(messages.get(0).warning());
		assertTrue(messages.get(1).warning());
		String text = messages.get(1).text();
		assertTrue(text, text.contains("'left'") && text.contains("'right'"));
		assertTrue(text, text.contains("one conversation per window"));
	}

	/** Several elements of one chat, and the second id holding no transcript, is silent. */
	public void testOnlyATranscriptCountsAsAConversation() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "left", "chat-conversation");
		log.join("w1", "left", "chat-rail");
		log.join("w1", "right", "chat-sources");
		log.join("w1", "right", "chat-rail");

		assertEquals("A second chat id that shows no transcript takes no turns.", 1, log.messages().size());
	}

	/**
	 * A dock and the page it hides on both render before the browser decides which of them shows.
	 *
	 * <p>
	 * They are the same conversation, so there is one id and nothing to warn about - which is what
	 * makes "give them one chat-id" the advice the warning ends with.
	 * </p>
	 */
	public void testADockOnThePagesOwnChatIsSilent() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "chat", "chat-conversation");
		log.join("w1", "chat", "chat-rail");
		log.join("w1", "chat", "chat-dock");
		log.join("w1", "chat", "chat-conversation");

		List<Message> messages = log.messages();
		assertEquals(1, messages.size());
		assertFalse(messages.get(0).warning());
	}

	/** The typo: a second id that nobody else joined. */
	public void testLoneElementBesideASharedChatWarns() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "support", "chat-conversation");
		log.join("w1", "support", "chat-progress");
		log.join("w1", "suport", "chat-rail");

		List<Message> messages = log.messages();
		assertEquals(2, messages.size());
		assertFalse(messages.get(0).warning());
		assertEquals("Chat elements in window 'w1': 'support' (2), 'suport' (1).", messages.get(0).text());

		Message warning = messages.get(1);
		assertTrue(warning.warning());
		assertTrue("Names the element that found nobody: " + warning.text(),
			warning.text().startsWith("<chat-rail chat-id='suport'> joins no other chat element in window 'w1'."));
		assertTrue("Names what it could have joined: " + warning.text(),
			warning.text().contains("'support' (2)"));
		assertTrue("Says what to do: " + warning.text(),
			warning.text().contains("<chat chat-id='...'>"));
	}

	/** Several lonely ids are one warning, not one each. */
	public void testSeveralLoneElementsAreOneWarning() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "a", "chat-conversation");
		log.join("w1", "b", "chat-rail");

		List<Message> messages = log.messages();
		assertEquals(2, messages.size());
		Message warning = messages.get(1);
		assertTrue(warning.text().startsWith(
			"<chat-conversation chat-id='a'>, <chat-rail chat-id='b'> joins no other chat element"));
		assertTrue("Nothing to point at: " + warning.text(),
			warning.text().contains("No other chat id in window 'w1' holds more than one element."));
	}

	/** Two windows are two pages: neither the tally nor the rule crosses them. */
	public void testWindowsAreCountedApart() {
		ChatJoinLog log = new ChatJoinLog();
		log.join("w1", "support", "chat-conversation");
		log.join("w2", "other", "chat-rail");

		List<Message> messages = log.messages();
		assertEquals("One tally each, no warning: each window has a single chat id.", 2, messages.size());
		assertFalse(messages.get(0).warning());
		assertFalse(messages.get(1).warning());
		assertEquals("Chat elements in window 'w1': 'support' (1).", messages.get(0).text());
		assertEquals("Chat elements in window 'w2': 'other' (1).", messages.get(1).text());
	}

	/** Nothing rendered, nothing said. */
	public void testEmptyLogSaysNothing() {
		assertTrue(new ChatJoinLog().messages().isEmpty());
	}

	/**
	 * The wiring: joins recorded during one render are tallied together and reported once, when the
	 * render ends.
	 */
	public void testOneReportPerRender() {
		List<Message> said = new ArrayList<>();
		Consumer<Message> previous = ChatJoinLog.sink;
		ChatJoinLog.sink = said::add;
		try {
			ThreadContextManager.inInteraction("test-render-1", () -> {
				ChatJoinLog.record("w1", "support", "chat-conversation");
				ChatJoinLog.record("w1", "support", "chat-rail");
				assertTrue("Nothing is said while the render is still running.", said.isEmpty());
			});
			assertEquals(1, said.size());
			assertEquals("Chat elements in window 'w1': 'support' (2).", said.get(0).text());

			ThreadContextManager.inInteraction("test-render-2", () -> {
				ChatJoinLog.record("w1", "support", "chat-conversation");
			});
			assertEquals("The next render starts a tally of its own.", 2, said.size());
			assertEquals("Chat elements in window 'w1': 'support' (1).", said.get(1).text());
		} finally {
			ChatJoinLog.sink = previous;
		}
	}

	/** Outside a render there is nothing to tally, and recording a join must not fail. */
	public void testOutsideAnInteractionNothingHappens() {
		List<Message> said = new ArrayList<>();
		Consumer<Message> previous = ChatJoinLog.sink;
		ChatJoinLog.sink = said::add;
		try {
			ChatJoinLog.record("w1", "support", "chat-conversation");
			assertTrue(said.isEmpty());
		} finally {
			ChatJoinLog.sink = previous;
		}
	}

	/** Boots the module system, so that an interaction can be opened. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatJoinLog.class));
	}
}
