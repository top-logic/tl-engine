/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.TaskState;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.AgentTask;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.util.model.ModelService;

/**
 * What a conversation is about: {@code tl.ai.execution:AgentSession#subject} (Issue #508).
 *
 * <p>
 * The reference names {@code tl.model:TLObject}, because which object a conversation belongs to is
 * the host's question - a project, an invoice, a contract. Nothing else in this repository points
 * at that type, so the first thing these tests establish is that the model boots with it and that
 * an object of an unrelated type can stand in the reference at all.
 * </p>
 *
 * <p>
 * The rest is what a host's scoped session list depends on: the backwards navigation the
 * {@code rows} expression uses, that deleting the subject empties that list rather than breaking
 * it, and that a session started without a subject is simply not in it - no backfill.
 * </p>
 *
 * <p>
 * Runs against a real {@link com.top_logic.knowledge.service.KnowledgeBase}, and reads through
 * {@link HistoryUtils#updateSessionRevision()}, because the store writes on its own thread at HEAD.
 * Same harness as {@link TestAgentTaskMirror}.
 * </p>
 */
public class TestSessionSubject extends BasicTestCase {

	/** An object of an unrelated type stands in the reference and is found by navigating back. */
	public void testSubjectIsWrittenAndFoundBackwards() {
		AgentTask subject = newSubject("subject-found");
		AgentSession session = newSession("about the first task");

		assertTrue("The session had no subject yet.", store().setSubject(session, subject));
		HistoryUtils.updateSessionRevision();

		assertEquals("The subject is the object it was stamped with.", subject, session.getSubject());
		assertEquals("What a scoped <data-list> asks for.", Set.of(session), sessionsAbout(subject));
	}

	/** A session that was started without an object keeps none, and no scoped list shows it. */
	public void testSessionWithoutSubjectIsInNoScopedList() {
		AgentTask subject = newSubject("subject-unrelated");
		AgentSession unstamped = newSession("about nothing in particular");

		assertNull("Nothing stamps a session that was opened without an object.", unstamped.getSubject());
		assertEquals("An existing session is not backfilled into somebody's list.",
			Set.of(), sessionsAbout(subject));
	}

	/** Stamping is a one-off: re-opening a conversation beside another object does not move it. */
	public void testTheSubjectIsWrittenOnlyOnce() {
		AgentTask first = newSubject("subject-first");
		AgentTask second = newSubject("subject-second");
		AgentSession session = newSession("about the first one");

		assertTrue(store().setSubject(session, first));
		HistoryUtils.updateSessionRevision();
		assertFalse("The session is already about something.", store().setSubject(session, second));
		HistoryUtils.updateSessionRevision();

		assertEquals("The first object is still the subject.", first, session.getSubject());
		assertEquals(Set.of(session), sessionsAbout(first));
		assertEquals(Set.of(), sessionsAbout(second));
	}

	/** No object, no stamp - and no write either. */
	public void testNoSubjectStampsNothing() {
		AgentSession session = newSession("about nothing");

		assertFalse(store().setSubject(session, null));
		HistoryUtils.updateSessionRevision();
		assertNull(session.getSubject());
	}

	/**
	 * Deleting the subject empties the scoped list; it does not break it.
	 *
	 * <p>
	 * The conversation outlives the object it was about - which is why the reference keeps the
	 * default deletion policy, and why a session whose subject is gone is still a session.
	 * </p>
	 */
	public void testDeletingTheSubjectLeavesTheSessionIntact() {
		AgentSession owner = newSession("holds the tasks");
		AgentTask doomed = onlyTaskOf(owner, "subject-doomed");
		AgentSession session = newSession("about the doomed task");
		assertTrue(store().setSubject(session, doomed));
		HistoryUtils.updateSessionRevision();
		assertEquals(Set.of(session), sessionsAbout(doomed));

		// The plan the mirror writes is the plan the store is told; an empty one deletes the row.
		store().syncTasks(owner, List.of(), null);
		HistoryUtils.updateSessionRevision();

		assertEquals("The subject is gone from the database.", 0, store().tasksOf(owner).size());
		assertNull("Deleting the object cleared the reference.", session.getSubject());
		assertEquals("The conversation itself is untouched.", "about the doomed task", session.getTitle());
	}

	/**
	 * Navigating back <em>from the deleted object itself</em> fails - which is a fact about the
	 * engine that a host writing a scoped list has to know.
	 *
	 * <p>
	 * The session is safe either way: the test above shows its reference cleared and the row
	 * intact. This is the other side, and it is not about {@code subject} at all -
	 * {@link TLObject#tReferers} throws for any deleted object, and TL-Script's {@code referers}
	 * calls exactly that. So a {@code rows} expression of the form
	 * {@code ifElse(isEmpty($x), list(), $x.referers(...))} answers while {@code $x} is empty and
	 * while it is live, and throws in the window where the channel still holds an object that was
	 * deleted meanwhile - a window every channel-bound element on such a page shares (a
	 * {@code <form>} over the same channel is in the same position).
	 * </p>
	 *
	 * <p>
	 * Pinned here rather than left to be rediscovered: it is the one thing about this feature that
	 * a host cannot guess from the model, and TL-Script has no "is this still there" to guard it
	 * with.
	 * </p>
	 */
	public void testNavigatingBackFromTheDeletedObjectItselfFails() {
		AgentSession owner = newSession("holds the task that goes");
		AgentTask doomed = onlyTaskOf(owner, "subject-vanishing");
		AgentSession session = newSession("about the vanishing task");
		assertTrue(store().setSubject(session, doomed));
		HistoryUtils.updateSessionRevision();

		store().syncTasks(owner, List.of(), null);
		HistoryUtils.updateSessionRevision();

		try {
			sessionsAbout(doomed);
			fail("Expected the engine to refuse a lookup from a deleted object.");
		} catch (IllegalStateException expected) {
			// What the engine does today. Should it ever start answering "nothing", this test says
			// so rather than a page failing to.
		}
	}

	/**
	 * The way round it that a host has: ask the sessions, not the deleted object.
	 *
	 * <p>
	 * Reading a cleared reference is empty, so a scan plus a filter answers where navigating back
	 * throws - at the cost of being a scan. This is what a page that must survive the deletion of
	 * the object its channel holds has to write today.
	 * </p>
	 */
	public void testTheSessionSideAnswersEvenAfterTheSubjectIsDeleted() {
		AgentSession owner = newSession("holds the task that goes, too");
		AgentTask doomed = onlyTaskOf(owner, "subject-vanishing-2");
		AgentSession session = newSession("about the other vanishing task");
		assertTrue(store().setSubject(session, doomed));
		HistoryUtils.updateSessionRevision();

		store().syncTasks(owner, List.of(), null);
		HistoryUtils.updateSessionRevision();

		assertNull("The reference is cleared, so no scan matches the deleted object.",
			session.getSubject());
	}

	/** The sessions a {@code rows} expression would list for {@code subject}. */
	private static Set<? extends TLObject> sessionsAbout(TLObject subject) {
		return subject.tReferers(TlAiExecutionFactory.getSubjectAgentSessionAttr());
	}

	/**
	 * An object of a type that has nothing to do with the reference's declaration: one task row of a
	 * session of its own.
	 */
	private static AgentTask newSubject(String name) {
		return onlyTaskOf(newSession(name + " (holder)"), name);
	}

	private static AgentTask onlyTaskOf(AgentSession holder, String name) {
		store().syncTasks(holder, List.of(new TaskState("1", name, null, TaskStore.STATUS_PENDING, 1)), null);
		HistoryUtils.updateSessionRevision();
		return store().tasksOf(holder).get(0);
	}

	private static AgentSession newSession(String title) {
		AgentSession session = store().createSession(SessionKind.CHAT, null, title);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	private static AgentExecutionStore store() {
		return AgentExecutionStore.getInstance();
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSessionSubject.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
