/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.util.model.ModelService;

/**
 * What {@code <chat-conversation subject="…">} stamps a conversation with (Issue #508).
 *
 * <p>
 * Two halves. The rule -
 * {@link ChatConversationControl#subjectFor(Object, boolean)} - decides from what the browser
 * reported and what stands on the channel; the store writes it. Both are tested here, and together
 * they are the page's promise: a conversation started beside a project is found again from that
 * project, and nothing else is moved there.
 * </p>
 *
 * <p>
 * The control itself is not built: it needs a rendering window. The one decision it makes of its own
 * is the rule below, which is why that is a method rather than three lines inside the command
 * handler.
 * </p>
 */
public class TestChatConversationSubject extends BasicTestCase {

	private static Person user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("subject-owner");
		}
	}

	/** A conversation this window created is about what the page was showing. */
	public void testACreatedConversationTakesTheChannelsObject() {
		TLObject project = session("a project stands in for the host's object");

		assertSame(project, ChatConversationControl.subjectFor(project, true));
	}

	/** A one-row selection names one object, which is what a selection channel usually holds. */
	public void testASingleSelectionIsTheObject() {
		TLObject project = session("the only selected row");

		assertSame(project, ChatConversationControl.subjectFor(List.of(project), true));
	}

	/**
	 * A conversation that was opened rather than created keeps the subject it was started with.
	 *
	 * <p>
	 * This is the case the browser has to report, because the server cannot see it: a reload
	 * re-opens what this window had, and a session picked from a list is opened too. Stamping either
	 * would backfill a conversation into whatever the page happens to show now.
	 * </p>
	 */
	public void testAnOpenedConversationIsNotStamped() {
		TLObject project = session("open beside an older conversation");

		assertNull(ChatConversationControl.subjectFor(project, false));
	}

	/** Nothing on the channel means the page was showing nothing, which stamps nothing. */
	public void testAnEmptyChannelStampsNothing() {
		assertNull(ChatConversationControl.subjectFor(null, true));
		assertNull(ChatConversationControl.subjectFor(List.of(), true));
	}

	/** Several objects name no single thing the conversation could be about. */
	public void testAMultipleSelectionStampsNothing() {
		TLObject one = session("first of a multi-selection");
		TLObject two = session("second of a multi-selection");

		assertNull(ChatConversationControl.subjectFor(List.of(one, two), true));
	}

	/** Anything that is not a model object is not a subject either. */
	public void testANonObjectStampsNothing() {
		assertNull(ChatConversationControl.subjectFor("Project 4711", true));
		assertNull(ChatConversationControl.subjectFor(List.of("Project 4711"), true));
	}

	/**
	 * End to end: the object the rule produced is what the scoped list finds, and a conversation
	 * started without one is not in it.
	 */
	public void testTheStampedConversationIsWhatAScopedListFinds() {
		TLObject project = session("the object this page is about");
		AgentSession stamped = (AgentSession) session("about that object");
		AgentSession unstamped = (AgentSession) session("about nothing in particular");

		TLObject subject = ChatConversationControl.subjectFor(project, true);
		assertTrue(AgentExecutionStore.getInstance().setSubject(stamped, subject));
		HistoryUtils.updateSessionRevision();

		Set<? extends TLObject> scoped =
			project.tReferers(TlAiExecutionFactory.getSubjectAgentSessionAttr());
		assertEquals("The conversation that was started here, and no other.", Set.of(stamped), scoped);
		assertFalse("A conversation with no subject is in no scoped list.", scoped.contains(unstamped));
	}

	/**
	 * Any persisted object will do as the host's own: what is under test knows nothing about
	 * projects.
	 */
	private static TLObject session(String title) {
		TLObject session = ChatHistoryService.createSession(title, user);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatConversationSubject.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
