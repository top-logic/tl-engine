/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.TaskState;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.AgentTask;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.util.model.ModelService;

/**
 * That the agent's in-memory plan reaches the model: what {@link TaskStore} says, the session's
 * {@link AgentTask} rows say too.
 *
 * <p>
 * Runs against a real {@link com.top_logic.knowledge.service.KnowledgeBase} because the point of
 * the mirror is where the writing happens - the store's writer thread at HEAD, not the agent thread
 * whose view is pinned to an older revision. Same harness as
 * {@link TestInteractionRecorderRevision}.
 * </p>
 *
 * <p>
 * Also that every committed change is announced on
 * {@link AgentExecutionStore.ChangeListener} - the seam the browser push rides on.
 * </p>
 */
public class TestAgentTaskMirror extends BasicTestCase {

	/** Creating tasks in the store creates the rows, numbered in plan order. */
	public void testCreatedTasksBecomeRows() {
		AgentSession session = newSession("mirror-create");
		TaskStore store = new TaskStore();
		try (AgentTaskMirror mirror = AgentTaskMirror.attach(store, session, null)) {
			store.createTask("Read the invoice", "Open the PDF and read it", "Reading the invoice");
			store.createTask("Book it", "Create the booking record", "Booking it");
		}

		List<AgentTask> rows = tasksOf(session);
		assertEquals("Both tasks are mirrored.", 2, rows.size());
		assertEquals("1", rows.get(0).getTaskId());
		assertEquals("Read the invoice", rows.get(0).getSubject());
		assertEquals("Open the PDF and read it", rows.get(0).getContent());
		assertEquals(TaskStore.STATUS_PENDING, rows.get(0).getStatus());
		assertEquals(Integer.valueOf(1), rows.get(0).getOrdinal());
		assertEquals("Book it", rows.get(1).getSubject());
		assertEquals(Integer.valueOf(2), rows.get(1).getOrdinal());
	}

	/** A status change reaches the row; no second row appears for the same task. */
	public void testStatusChangeUpdatesTheSameRow() {
		AgentSession session = newSession("mirror-update");
		TaskStore store = new TaskStore();
		try (AgentTaskMirror mirror = AgentTaskMirror.attach(store, session, null)) {
			store.createTask("Read the invoice", null, null);
			store.updateTask("1", TaskStore.STATUS_IN_PROGRESS, null, null, null, null, null, null);

			List<AgentTask> running = tasksOf(session);
			assertEquals(1, running.size());
			assertEquals(TaskStore.STATUS_IN_PROGRESS, running.get(0).getStatus());

			store.updateTask("1", TaskStore.STATUS_COMPLETED, "Invoice read", null, null, null, null, null);
		}

		List<AgentTask> rows = tasksOf(session);
		assertEquals("Still one row - the task was updated, not re-created.", 1, rows.size());
		assertEquals(TaskStore.STATUS_COMPLETED, rows.get(0).getStatus());
		assertEquals("Invoice read", rows.get(0).getSubject());
	}

	/**
	 * A completed plan stays; a deleted task goes and the ones behind it move up.
	 *
	 * <p>
	 * The store drops a task whose status is set to {@code deleted}, so the mirror deletes its row
	 * too - "completed" is what a finished task looks like, and that is kept so a reload still shows
	 * what the run did.
	 * </p>
	 */
	public void testDeletedTaskGoesAndTheRestIsRenumbered() {
		AgentSession session = newSession("mirror-delete");
		TaskStore store = new TaskStore();
		try (AgentTaskMirror mirror = AgentTaskMirror.attach(store, session, null)) {
			store.createTask("First", null, null);
			store.createTask("Second", null, null);
			store.createTask("Third", null, null);
			store.updateTask("2", TaskStore.STATUS_COMPLETED, null, null, null, null, null, null);
			store.updateTask("1", TaskStore.STATUS_DELETED, null, null, null, null, null, null);
		}

		List<AgentTask> rows = tasksOf(session);
		assertEquals("The deleted task is gone.", 2, rows.size());
		assertEquals("Second", rows.get(0).getSubject());
		assertEquals("The remaining plan reads 1..n.", Integer.valueOf(1), rows.get(0).getOrdinal());
		assertEquals(TaskStore.STATUS_COMPLETED, rows.get(0).getStatus());
		assertEquals("Third", rows.get(1).getSubject());
		assertEquals(Integer.valueOf(2), rows.get(1).getOrdinal());
	}

	/** Syncing the same plan twice writes nothing - the call is idempotent. */
	public void testSyncingTheSamePlanTwiceWritesNothing() {
		AgentSession session = newSession("mirror-idempotent");
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		List<TaskState> plan = List.of(new TaskState("1", "Only task", null, TaskStore.STATUS_PENDING, 1));

		assertTrue("The first sync creates the row.", store.syncTasks(session, plan, null));
		assertFalse("The second one has nothing left to write.", store.syncTasks(session, plan, null));
		assertEquals(1, tasksOf(session).size());

		assertTrue("Clearing the plan deletes the row.", store.syncTasks(session, List.of(), null));
		assertEquals(0, tasksOf(session).size());
	}

	/** A run without a recorded session is not mirrored, and mutating its store is not a failure. */
	public void testNoSessionIsNotMirrored() {
		TaskStore store = new TaskStore();
		try (AgentTaskMirror mirror = AgentTaskMirror.attach(store, null, null)) {
			store.createTask("Nowhere to go", null, null);
		}
		// Reaching here without an exception is the assertion.
	}

	/**
	 * Every committed change is announced once, and a write that changed nothing is not.
	 *
	 * <p>
	 * This is what a live view hangs off: without the notification a model-bound element only learns
	 * of a background run's writes on the next browser command or on the SSE heartbeat.
	 * </p>
	 */
	public void testCommittedChangesAreAnnounced() {
		AtomicInteger announced = new AtomicInteger();
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentExecutionStore.ChangeListener listener = owner -> announced.incrementAndGet();
		store.addChangeListener(listener);
		try {
			AgentSession session = newSession("mirror-notify");
			assertEquals("Creating the session is a change.", 1, announced.get());

			store.updateTitle(session, "Renamed");
			assertEquals("So is renaming it.", 2, announced.get());

			List<TaskState> plan = List.of(new TaskState("1", "Only task", null, TaskStore.STATUS_PENDING, 1));
			store.syncTasks(session, plan, null);
			assertEquals("So is writing the plan.", 3, announced.get());

			store.syncTasks(session, plan, null);
			assertEquals("Syncing the same plan again writes nothing, so it announces nothing.",
				3, announced.get());
		} finally {
			store.removeChangeListener(listener);
		}
	}

	/**
	 * The session's rows, read at a revision that includes the writer thread's commits.
	 *
	 * <p>
	 * The mirror writes from the store's writer thread in its own session, so this thread's stable
	 * view has to be advanced first - exactly what the chat servlet does at request begin.
	 * </p>
	 */
	private static List<AgentTask> tasksOf(AgentSession session) {
		HistoryUtils.updateSessionRevision();
		return new ArrayList<>(AgentExecutionStore.getInstance().tasksOf(session));
	}

	private static AgentSession newSession(String title) {
		AgentSession session = AgentExecutionStore.getInstance().createSession(SessionKind.CHAT, null, title);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAgentTaskMirror.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
