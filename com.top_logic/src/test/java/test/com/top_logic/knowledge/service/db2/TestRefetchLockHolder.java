/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import com.top_logic.basic.sql.CommitContext;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

import test.com.top_logic.basic.ReflectionUtils;

/**
 * Tests that a {@link RefetchTimeout} of {@link DBKnowledgeBase} names the thread holding the
 * refetch lock.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestRefetchLockHolder extends AbstractDBKnowledgeBaseTest {

	private static final long WAIT_TIMEOUT_SECONDS = 60;

	/**
	 * Name of the field of {@link DBKnowledgeBase} holding the hook run after the commit has been
	 * published locally, before the refetch lock is released.
	 */
	static final String COMMIT_PUBLISHED_HOOK = "_commitPublishedHook";

	private CountDownLatch _inCommit;

	private CountDownLatch _release;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_inCommit = new CountDownLatch(1);
		_release = new CountDownLatch(1);
	}

	public void testTimeoutNamesBlockingCommit() throws Throwable {
		TestFuture blockingCommit = inParallel(new BlockingCommit());
		try {
			assertTrue("Blocking commit did not start.", _inCommit.await(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));

			try {
				kb().refetch();
				fail("Refetch must time out while a commit holds the refetch lock.");
			} catch (RefetchTimeout ex) {
				String message = ex.getMessage();
				assertTrue(message, message.contains("thread 'inParallel:BlockingCommit'"));
				assertTrue(message, message.contains("during commit of revision"));

				Throwable holderTrace = ex.getCause();
				assertNotNull("No stack trace of the lock holder.", holderTrace);
				assertTrue(Arrays.toString(holderTrace.getStackTrace()),
					Arrays.stream(holderTrace.getStackTrace())
						.anyMatch(element -> element.getClassName().equals(TestRefetchLockHolder.class.getName())
							&& element.getMethodName().equals("awaitRelease")));
			}
		} finally {
			_release.countDown();
		}
		blockingCommit.check();

		// The lock is released after the commit, a refetch succeeds again.
		kb().refetch();
	}

	void awaitRelease() {
		_inCommit.countDown();
		try {
			assertTrue("Commit was not released.", _release.await(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Commit that blocks after having acquired the refetch lock until the test releases it.
	 *
	 * <p>
	 * The commit blocks in the commit-published hook of {@link DBKnowledgeBase}, i.e. after the
	 * database commit but before the refetch lock is released. Blocking before the database commit
	 * (e.g. in {@link CommittableAdapter#commit(CommitContext)}) would keep the new revision row
	 * locked: On databases with locking reads (MS SQL Server) the refetch then blocks in the
	 * database while reading the last revision and never reaches the refetch lock.
	 * </p>
	 */
	private class BlockingCommit implements Execution {

		@Override
		public void run() throws Exception {
			Transaction tx = begin();
			newB("b1");
			ReflectionUtils.setValue(kb(), COMMIT_PUBLISHED_HOOK, (Runnable) () -> awaitRelease());
			try {
				commit(tx);
			} finally {
				ReflectionUtils.setValue(kb(), COMMIT_PUBLISHED_HOOK, null);
			}
		}

	}

	public static Test suite() {
		return suite(TestRefetchLockHolder.class);
	}

}
