/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * A chat whose window state is rebuilt - reloaded because another window wrote to it, or loaded
 * again after the window's cache dropped it - listens to its subagents again at once, so that one
 * finishing or asking its lead before the window sends anything still wakes the lead.
 */
public class TestSubagentWakerAfterReload extends BasicTestCase {

	private static Person owner;

	private static final List<String> CHATS = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("waker-after-reload");
			for (int i = 0; i < 8; i++) {
				TLObject chat = ChatHistoryService.createSession("Chat " + i, owner);
				CHATS.add(chat.tHandle().getObjectName().toExternalForm());
			}
			HistoryUtils.updateSessionRevision();
		}
	}

	/** Another window wrote to the chat, so this window reloads it: the waker is there again. */
	public void testAReloadedChatStillWakesItsLead() {
		TaskProgressTracker window = new TaskProgressTracker();
		ChatSessionManager chats = window.getSessionManager();
		String chat = CHATS.get(0);
		assertTrue(chats.switchSession(chat, owner));
		SubagentJobRegistry registry = window.getActiveSubagentRegistry();
		assertNotNull("Spawning attaches the waker.", chats.getSubagentWaker(chat));

		chats.markChangedElsewhere(chat);
		assertTrue("The next request of the window reloads the chat.", chats.switchSession(chat, owner));

		assertNotNull("The reloaded chat listens to its subagents before anything else asks for them.",
			chats.getSubagentWaker(chat));
		assertSame(registry, window.getActiveSubagentRegistry());
	}

	/** The window's cache dropped the chat; coming back to it listens to its subagents again. */
	public void testAChatLoadedAgainStillWakesItsLead() {
		TaskProgressTracker window = new TaskProgressTracker();
		ChatSessionManager chats = window.getSessionManager();
		String chat = CHATS.get(1);
		assertTrue(chats.switchSession(chat, owner));
		window.getActiveSubagentRegistry();
		for (int i = 2; i <= 7; i++) {
			assertTrue(chats.switchSession(CHATS.get(i), owner));
		}
		assertNull("Dropped from the cache.", chats.getSubagentWaker(chat));

		assertTrue(chats.switchSession(chat, owner));

		assertNotNull(chats.getSubagentWaker(chat));
	}

	/** A chat that never started a subagent in this window gets no registry by being opened. */
	public void testOpeningAChatCreatesNoRegistry() {
		TaskProgressTracker window = new TaskProgressTracker();
		ChatSessionManager chats = window.getSessionManager();
		String chat = CHATS.get(2);

		assertTrue(chats.switchSession(chat, owner));

		assertNull(chats.getSubagentWaker(chat));
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentWakerAfterReload.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
