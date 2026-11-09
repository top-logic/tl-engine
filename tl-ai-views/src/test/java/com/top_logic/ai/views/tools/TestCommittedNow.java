/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.util.ItemByNameCache;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * The context {@link CommittedNow} commits in while the agent thread that called it waits with its
 * interaction open.
 */
public class TestCommittedNow extends BasicTestCase {

	private static final long TIMEOUT_SECONDS = 60;

	private static final String COMMITTED = "committedNowCreated";

	private Person _person;

	private TLContext _run;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("committedNowUser");

		ThreadContextManager manager = TLContextManager.getManager();
		_run = (TLContext) manager.newSubSessionContext();
		_run.setSessionContext(manager.newSessionContext());
		_run.setPerson(_person);
	}

	@Override
	protected void tearDown() throws Exception {
		// The session revision of the test thread does not yet contain the commit of the work:
		// delete in a new interaction.
		inNewThread(() -> ThreadContext.inSystemContext(TestCommittedNow.class, () -> {
			Person committed = Person.byName(COMMITTED);
			if (committed != null) {
				TestPerson.deletePersonAndUser(committed);
			}
			return null;
		}));
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_run = null;
		super.tearDown();
	}

	/**
	 * The work runs as the caller's user in a sub-session derived from the caller's, and its commit
	 * leaves the caller able to read the knowledge base - before and after it caught up with the
	 * commit.
	 */
	public void testCommitWhileTheCallerWaits() throws Exception {
		DBKnowledgeBase kb = (DBKnowledgeBase) PersistencyLayer.getKnowledgeBase();
		AtomicReference<TLContext> workContext = new AtomicReference<>();

		inNewThread(() -> ThreadContextManager.inContext(_run,
			(ComputationEx2<Void, RuntimeException, RuntimeException>) () -> {
				// The agent thread has read the knowledge base before: its revision is pinned.
				kb.getSessionRevision();

				String name = CommittedNow.run(() -> {
					workContext.set(TLContext.getContext());
					return TestPerson.createPerson(COMMITTED).getName();
				});
				assertEquals(COMMITTED, name);

				assertNotNull("The caller still reads the knowledge base after the commit.",
					lookup(kb, _person.getName()));
				assertNull("The caller reads the state it began with until it catches up.", lookup(kb, COMMITTED));

				HistoryUtils.updateSessionRevision();
				assertNotNull("Having caught up, the caller reads what the work committed.", lookup(kb, COMMITTED));
				return null;
			}));

		TLContext context = workContext.get();
		assertNotNull(context);
		assertNotSame("The work must not share the sub-session of the waiting caller.", _run, context);
		assertSame("The work is done for the caller's sub-session.", _run, context.getOrigin());
		assertSame(_person, context.getPerson());
	}

	/** Builds a new cache of the persons by name, which requires a valid session revision. */
	private static KnowledgeItem lookup(DBKnowledgeBase kb, String name) {
		ItemByNameCache<String> cache =
			new ItemByNameCache<>(kb, Person.OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, String.class);
		try {
			return cache.lookup(name);
		} finally {
			cache.invalidate();
		}
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestCommittedNow.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

	/** A knowledge base with persons, the user the work runs for. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestCommittedNow.class));
	}
}
