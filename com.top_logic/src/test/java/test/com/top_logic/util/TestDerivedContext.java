/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractKBCache;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.util.ItemByNameCache;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Test case for {@link ThreadContextManager#inDerivedContext(SubSessionContext, InContext)}.
 */
public class TestDerivedContext extends BasicTestCase {

	private static final long TIMEOUT_SECONDS = 60;

	private Person _person;

	private Person _committed;

	private TLContext _origin;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("derivedContextUser");

		ThreadContextManager manager = TLContextManager.getManager();
		_origin = (TLContext) manager.newSubSessionContext();
		_origin.setSessionContext(manager.newSessionContext());
		_origin.setPerson(_person);
		_origin.setCurrentLocale(Locale.JAPANESE);
		_origin.setCurrentTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
	}

	@Override
	protected void tearDown() throws Exception {
		if (_committed != null) {
			Person committed = _committed;
			_committed = null;
			// The session revision of the test thread does not yet contain the commit of another
			// sub-session: delete in a new interaction.
			runInNewThread(() -> ThreadContext.inSystemContext(TestDerivedContext.class, () -> {
				TestPerson.deletePersonAndUser(committed);
				return null;
			}));
		}
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_origin = null;
		super.tearDown();
	}

	/**
	 * Tests that background work in a derived context rebuilds a {@link AbstractKBCache} after an
	 * interaction of its origin has committed while the background work was running.
	 */
	public void testConcurrentCommitInOrigin() throws Exception {
		Throwable problem = runAlongsideCommit(ThreadContextManager::inDerivedContext);
		if (problem != null) {
			throw new AssertionError("Background work in a derived context failed.", problem);
		}
	}

	/**
	 * Tests that background work sharing the sub-session of a concurrently committing interaction
	 * ends up with an invalid session revision.
	 *
	 * <p>
	 * This is the situation {@link ThreadContextManager#inDerivedContext(SubSessionContext, InContext)}
	 * avoids.
	 * </p>
	 */
	public void testConcurrentCommitInSharedSubSession() throws Exception {
		Throwable problem = runAlongsideCommit(ThreadContextManager::inContext);
		assertNotNull("Background work sharing the sub-session must fail.", problem);
		assertInstanceof(problem, KnowledgeBaseRuntimeException.class);
		assertTrue(problem.getMessage(), problem.getMessage().startsWith("Session revision invalid"));
	}

	/**
	 * Runs background work for {@link #_origin} that pins its session revision, commits a change in
	 * another interaction of {@link #_origin}, and lets the background work build a new
	 * {@link AbstractKBCache} afterwards.
	 *
	 * @param background
	 *        Runs the background work for the given sub-session.
	 * @return The problem of the background work, <code>null</code> if it succeeded.
	 */
	private Throwable runAlongsideCommit(BiConsumer<SubSessionContext, InContext> background)
			throws Exception {
		DBKnowledgeBase kb = (DBKnowledgeBase) PersistencyLayer.getKnowledgeBase();
		CountDownLatch revisionPinned = new CountDownLatch(1);
		CountDownLatch commitDone = new CountDownLatch(1);

		FutureTask<Void> work = start(() -> {
			try {
				background.accept(_origin, () -> {
					kb.getSessionRevision();
					revisionPinned.countDown();
					await(commitDone);

					ItemByNameCache<String> cache =
						new ItemByNameCache<>(kb, Person.OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, String.class);
					try {
						assertNotNull(cache.lookup(_person.getName()));
					} finally {
						cache.invalidate();
					}
					assertEquals(((UpdateChainView) kb.getSessionUpdateChain()).current().getRevision(),
						kb.getSessionRevision());
				});
			} finally {
				// Do not let the committing thread wait for background work that failed early.
				revisionPinned.countDown();
			}
			return null;
		});
		await(revisionPinned);

		FutureTask<Void> commit = start(() -> {
			ThreadContextManager.inContext(_origin, () -> {
				_committed = TestPerson.createPerson("derivedContextCommitted");
			});
			return null;
		});
		commit.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		commitDone.countDown();

		try {
			work.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return null;
		} catch (ExecutionException ex) {
			return ex.getCause();
		}
	}

	/**
	 * Tests that a derived context has the identity of its origin, but is a sub-session of its own
	 * that is not registered in the session.
	 */
	public void testIdentity() throws Exception {
		Object personalConfiguration = _origin.getPersonalConfiguration();

		runInNewThread(() -> ThreadContextManager.inDerivedContext(_origin, () -> {
			TLContext context = TLContext.getContext();
			assertNotSame(_origin, context);
			assertSame(_origin, context.getOrigin());
			assertSame(_origin.getSessionContext(), context.getSessionContext());
			assertSame(_person, context.getPerson());
			assertSame(_person, TLContext.currentUser());
			assertEquals(_origin.getContextId(), context.getContextId());
			assertEquals(Locale.JAPANESE, context.getCurrentLocale());
			assertEquals(TimeZone.getTimeZone("Pacific/Auckland"), context.getCurrentTimeZone());
			assertSame(personalConfiguration, context.getPersonalConfiguration());
			assertFalse("A derived context is not registered in the session.",
				_origin.getSessionContext().getSubSessions().containsValue(context));

			TLContext nested = runInNewThread(() -> ThreadContextManager.inDerivedContext(context, () -> {
				TLContext nestedContext = TLContext.getContext();
				assertNotSame(context, nestedContext);
				assertSame("The origin of a derived context is the first origin.", _origin,
					nestedContext.getOrigin());
				assertSame(_person, nestedContext.getPerson());
				return nestedContext;
			}));
			assertNotNull(nested);
			return context;
		}));

		assertSame("A sub-session that is not derived is its own origin.", _origin, _origin.getOrigin());
	}

	/**
	 * Tests that a context without person passes its context id to a derived context.
	 */
	public void testSystemIdentity() throws Exception {
		String contextId = ThreadContextManager.systemContextId(TestDerivedContext.class);
		_origin.setPerson(null);
		_origin.setContextId(contextId);

		String derivedContextId = runInNewThread(
			() -> ThreadContextManager.inDerivedContext(_origin, () -> TLContext.getContext().getContextId()));
		assertEquals(contextId, derivedContextId);
	}

	/**
	 * Tests that a derived context cannot be entered from a thread that has an interaction.
	 */
	public void testRequiresThreadWithoutInteraction() throws Exception {
		runInNewThread(() -> {
			ThreadContextManager.inContext(_origin, () -> {
				try {
					ThreadContextManager.inDerivedContext(_origin, () -> fail("Must not be executed."));
					fail("A derived context requires a thread without interaction.");
				} catch (IllegalStateException ex) {
					// Expected.
				}
			});
			return null;
		});
	}

	private static <T> T runInNewThread(Callable<T> job) throws Exception {
		try {
			return start(job).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

	private static <T> FutureTask<T> start(Callable<T> job) {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestDerivedContext.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		return task;
	}

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("Timeout waiting for the other thread.", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestDerivedContext.class));
	}

}
