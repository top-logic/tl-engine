/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.thread.BindingSnapshot;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.I18NConstants;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link ActingPerson}: who acts in a call, why a call has no acting person, and how
 * the acting person reaches another thread.
 */
public class TestActingPerson extends BasicTestCase {

	private static final long TIMEOUT_SECONDS = 60;

	private Person _anna;

	private Person _ben;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_anna = TestPerson.createPerson("actingPersonAnna");
		_ben = TestPerson.createPerson("actingPersonBen");
	}

	@Override
	protected void tearDown() throws Exception {
		Person anna = _anna;
		Person ben = _ben;
		_anna = null;
		_ben = null;
		// Another thread may have changed the accounts after the session revision of this thread.
		runInNewThread(() -> ThreadContext.inSystemContext(TestActingPerson.class, () -> {
			for (Person person : new Person[] { anna, ben }) {
				if (person.tValid()) {
					TestPerson.deletePersonAndUser(person);
				}
			}
			return null;
		}));
		super.tearDown();
	}

	/** In a request of a present person, the person acts. */
	public void testPresentPersonActs() {
		Person acting = asActingPerson(_anna, () -> {
			assertSame(_anna, ActingPerson.handOver());
			return ActingPerson.require();
		});
		assertSame(_anna, acting);
	}

	/**
	 * A run of an account that no person started, e.g. a scheduled task in the person context of its
	 * account, has no acting person.
	 */
	public void testAccountWithoutActingPerson() {
		TLContextManager.inPersonContext(_anna, () -> {
			assertSame(_anna, TLContext.currentUser());
			assertRefused(I18NConstants.ERROR_NO_ACTING_PERSON);
		});
	}

	/** A system context has no account and no acting person. */
	public void testSystemContext() {
		ThreadContext.inSystemContext(TestActingPerson.class, () -> assertRefused(I18NConstants.ERROR_NO_ACCOUNT));
	}

	/**
	 * A step in which another account acts than the acting person - a person context of another
	 * account, a system context - gets no acting person.
	 */
	public void testOtherAccountActs() {
		asActingPerson(_anna, () -> {
			TLContextManager.inPersonContext(_ben, () -> assertRefused(I18NConstants.ERROR_OTHER_ACCOUNT_ACTS));
			ThreadContext.inSystemContext(TestActingPerson.class,
				() -> assertRefused(I18NConstants.ERROR_OTHER_ACCOUNT_ACTS));
			return ActingPerson.require();
		});
	}

	/** The anonymous account is never an acting person: all visitors share it. */
	public void testAnonymousAccount() {
		Person anonymous = PersonManager.getManager().getAnonymous();
		assertNotNull(anonymous);
		asActingPerson(anonymous, () -> {
			assertRefused(I18NConstants.ERROR_NO_ACTING_PERSON);
			return null;
		});
	}

	/** Closing a binding restores the acting person bound before. */
	public void testBindingRestores() {
		TLContextManager.inPersonContext(_anna, () -> {
			try (ThreadLocalBinding<Person>.Handle outer = ActingPerson.bind(_anna)) {
				try (ThreadLocalBinding<Person>.Handle inner = ActingPerson.bind(null)) {
					assertRefused(I18NConstants.ERROR_NO_ACTING_PERSON);
				}
				assertSame(_anna, ActingPerson.require());
			}
			assertRefused(I18NConstants.ERROR_NO_ACTING_PERSON);
		});
	}

	/**
	 * A run that a person started stops acting for the person when the account is locked, also when
	 * the run reads the knowledge base at a revision before the lock.
	 */
	public void testLockEndsActingPersonInPinnedRun() throws Exception {
		assertEndsWhileRunning(() -> {
			try (Transaction tx = kb().beginTransaction()) {
				_anna.setLocked(true);
				tx.commit();
			}
		}, false);
	}

	/**
	 * A run that a person started stops acting for the person when the account is deleted, also when
	 * the run reads the knowledge base at a revision before the delete.
	 */
	public void testDeleteEndsActingPersonInPinnedRun() throws Exception {
		assertEndsWhileRunning(() -> TestPerson.deletePersonAndUser(_anna), false);
	}

	/**
	 * A run that a person started stops acting for the person when the account is deleted, also
	 * after the run caught up with the delete, as a chat run does at each turn: the account is then a
	 * deleted object for the run.
	 */
	public void testDeleteEndsActingPersonInRunThatCaughtUp() throws Exception {
		assertEndsWhileRunning(() -> TestPerson.deletePersonAndUser(_anna), true);
	}

	/**
	 * Runs a run of Anna in a sub-session derived from her window, as a chat run does. The run reads
	 * the knowledge base at a fixed revision; meanwhile another thread changes the account.
	 *
	 * @param catchUp
	 *        Whether the run moves its revision to the change before it asks for the acting person.
	 */
	private void assertEndsWhileRunning(Runnable change, boolean catchUp) throws Exception {
		TLContext window = window(_anna);
		CountDownLatch pinned = new CountDownLatch(1);
		CountDownLatch changed = new CountDownLatch(1);

		FutureTask<Void> run = start(() -> {
			try (ThreadLocalBinding<Person>.Handle acting = ActingPerson.bind(_anna)) {
				ThreadContextManager.inDerivedContext(window, () -> {
					assertSame(_anna, ActingPerson.require());
					// Fixes the revision at which this run reads the knowledge base.
					HistoryUtils.getSessionRevision();
					pinned.countDown();
					await(changed);

					assertTrue("The run still reads the account before the change.",
						_anna.tValid() && !_anna.isLocked());
					if (catchUp) {
						HistoryUtils.updateSessionRevision();
					}
					assertRefused(I18NConstants.ERROR_NO_ACTING_PERSON);
				});
			} finally {
				// Lets this thread go on also when the run failed before; the failure is reported below.
				pinned.countDown();
			}
			return null;
		});
		await(pinned);

		try {
			if (!run.isDone()) {
				runInNewThread(() -> ThreadContext.inSystemContext(TestActingPerson.class, () -> {
					change.run();
					return null;
				}));
			}
		} finally {
			changed.countDown();
		}

		get(run);
	}

	/**
	 * A {@link BindingSnapshot} carries the acting person to a worker thread, as for the tool calls
	 * of a parallel batch and for a detached subagent. A thread without it has no acting person.
	 */
	public void testSnapshotCarriesActingPerson() throws Exception {
		TLContext window = window(_anna);
		BindingSnapshot snapshot = asActingPerson(_anna, BindingSnapshot::capture);

		Person carried = runInNewThread(() -> ThreadContextManager.inDerivedContext(window, () -> {
			try (BindingSnapshot.Established established = snapshot.establish()) {
				return ActingPerson.require();
			}
		}));
		assertSame(_anna, carried);

		runInNewThread(() -> ThreadContextManager.inDerivedContext(window, () -> {
			assertRefused(I18NConstants.ERROR_NO_ACTING_PERSON);
			return null;
		}));
	}

	private static <T> T asActingPerson(Person person, ComputationEx2<T, RuntimeException, RuntimeException> job) {
		return TLContextManager.inPersonContext(person, () -> {
			try (ThreadLocalBinding<Person>.Handle acting = ActingPerson.bind(person)) {
				return job.run();
			}
		});
	}

	private static void assertRefused(ResKey reason) {
		assertNull(ActingPerson.handOver());
		try {
			ActingPerson.require();
			fail("No acting person expected.");
		} catch (TopLogicException ex) {
			assertEquals(reason, ex.getErrorKey());
		}
	}

	/** The sub-session of a browser window of the given person. */
	private static TLContext window(Person person) {
		ThreadContextManager manager = TLContextManager.getManager();
		TLContext window = (TLContext) manager.newSubSessionContext();
		window.setSessionContext(manager.newSessionContext());
		window.setPerson(person);
		return window;
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static <T> T runInNewThread(Callable<T> job) throws Exception {
		return get(start(job));
	}

	private static <T> FutureTask<T> start(Callable<T> job) {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestActingPerson.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		return task;
	}

	private static <T> T get(FutureTask<T> task) throws Exception {
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
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestActingPerson.class));
	}

}
