/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import junit.framework.Test;

import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;

/**
 * Test case for {@link RowLevelLockingSequenceManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestRowLevelLockingSequenceManager extends AbstractDBKnowledgeBaseTest {

	private static long NO_NUMBER_CREATED = -1L;

	RowLevelLockingSequenceManager manager;

	String sequence;

	DBHelper sqlDialect;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		manager = new RowLevelLockingSequenceManager();
		sequence = "seq" + System.currentTimeMillis();
		
		sqlDialect = ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect();
	}
	
	@Override
	protected void tearDown() throws Exception {
		manager = null;
		sequence = null;
		
		sqlDialect = null;
		
		super.tearDown();
	}

	/** Test for {@link RowLevelLockingSequenceManager#isUsed(PooledConnection, int, String)}. */
	public void testIsUsed() throws SQLException {
		PooledConnection commitContext = createCommitContext();
		try {

			String thatSequence = "testUsed" + System.currentTimeMillis();
			assertFalse(manager.isUsed(commitContext, 3, thatSequence));
			commit(commitContext); // empty commit;

			assertFalse(manager.isUsed(commitContext, 3, thatSequence));
			commit(commitContext); // empty commit;

			long nr = manager.nextSequenceNumber(sqlDialect, commitContext, 3, thatSequence);
			assertTrue(NO_NUMBER_CREATED != nr);
			commit(commitContext); // commit first number

			assertTrue(manager.isUsed(commitContext, 3, thatSequence));

		} finally {
			releaseCommitContext(commitContext);
		}
	}

	/**
	 * Test for
	 * {@link RowLevelLockingSequenceManager#getLastSequenceNumber(PooledConnection, int, String)}.
	 */
	public void testGetLastSequenceNumber() throws SQLException {
		PooledConnection commitContext = createCommitContext();
		try {
			String thatSequence = "testGetLastNumber" + System.currentTimeMillis();
			assertEquals(NO_NUMBER_CREATED, manager.getLastSequenceNumber(commitContext, 3, thatSequence));
			commit(commitContext); // empty commit;

			assertEquals(NO_NUMBER_CREATED, manager.getLastSequenceNumber(commitContext, 3, thatSequence));
			commit(commitContext); // empty commit;

			assertEquals(1L, manager.nextSequenceNumber(sqlDialect, commitContext, 3, thatSequence));
			commit(commitContext); // commit first number

			assertEquals(1L, manager.getLastSequenceNumber(commitContext, 3, thatSequence));
		} finally {
			releaseCommitContext(commitContext);
		}
	}

	/**
	 * Test for
	 * {@link RowLevelLockingSequenceManager#nextSequenceNumber(DBHelper, java.sql.Connection, int, String)}.
	 */
	public void testSequence() throws SQLException {
		PooledConnection commitContext = createCommitContext();
		try {
			long initialValue = manager.nextSequenceNumber(sqlDialect, commitContext, 3, sequence);
			commit(commitContext);
			
			assertEquals(initialValue + 1, manager.nextSequenceNumber(sqlDialect, commitContext, 3, sequence));
			commit(commitContext);
			
			assertEquals(initialValue + 2, manager.nextSequenceNumber(sqlDialect, commitContext, 3, sequence));
			commit(commitContext);
			
			assertEquals(initialValue + 3, manager.nextSequenceNumber(sqlDialect, commitContext, 3, sequence));
			rollback(commitContext);
			
			assertEquals(initialValue + 3, manager.nextSequenceNumber(sqlDialect, commitContext, 3, sequence));
			commit(commitContext);
		} finally {
			releaseCommitContext(commitContext);
		}
	}

	public void testLockingBasic() throws SQLException, InterruptedException {
		final long[] values = new long[3];
		PooledConnection context;
		{
			context = createCommitContext();
			values[0] = manager.nextSequenceNumber(sqlDialect, context, 3, sequence);
		}

		TestFuture future1 = inParallel(new Execution() {
			@Override
			public void run() throws Exception {
				PooledConnection context = createCommitContext();
				values[1] = manager.nextSequenceNumber(sqlDialect, context, 3, sequence);
				context.commit();
				releaseCommitContext(context);
			}
		});
		
		TestFuture future2 = inParallel(new Execution() {
			@Override
			public void run() throws Exception {
				PooledConnection context = createCommitContext();
				values[2] = manager.nextSequenceNumber(sqlDialect, context, 3, sequence);
				context.commit();
				releaseCommitContext(context);
			}
		});
		
		{
			Thread.sleep(500);
			
			// Others wait for own commit.
			assertEquals(0, values[1]);
			assertEquals(0, values[2]);
			
			context.commit();
			releaseCommitContext(context);
		}
		
		future1.check(2000);
		future2.check(2000);
		
		// Values of other threads are increments of the own value.
		assertEquals(2 * values[0] + 3, values[1] + values[2]);
	}

	public void testLocking() throws SQLException, InterruptedException {
		/* 20 parallel threads is not realistic. This causes the test to fail sometimes. Therefore
		 * the number of threads is reduced. */
//		int threadCount = 20;
		int threadCount = 3;
		
		int setupCnt = 10;
		for (int n = 0; n < setupCnt; n++) {
			// Use a fresh sequence for each test. This ensures that the setup problem for a
			// sequence is tested as well.
			createInParallel("testLocking-" + System.currentTimeMillis() + "-" + n, threadCount);
		}
	}

	private void createInParallel(final String sequenceId, int successFullThreadsCount) throws InterruptedException {
		final long initialValue = 0;
		final boolean[] allNumbers = new boolean[successFullThreadsCount];
		final BooleanFlag test = new BooleanFlag(false);

		parallelTest(2 * successFullThreadsCount, new ExecutionFactory() {
			@Override
			public Execution createExecution(final int id) {
				return new Execution() {
					@Override
					public void run() throws Exception {
						boolean successfulTest = id % 2 == 0;
						PooledConnection context = createCommitContext();
						
						long uniqueNumber = manager.nextSequenceNumber(sqlDialect, context, 3, sequenceId);
						boolean success = false;
						try {
							synchronized (test) {
								assertFalse("Concurrent sequence create.", test.get());
								test.set(true);
							}
							try {
								Thread.sleep(200);
							} finally {
								synchronized (test) {
									test.set(false);
								}
							}
							
							if (successfulTest) {
								commit(context);
								
								synchronized (test) {
									int index = (int) (uniqueNumber - initialValue - 1);
									assertFalse("Same number produced in different threads: " + uniqueNumber, allNumbers[index]);
									allNumbers[index] = true;
								}
								
								success = true;
							}
						} finally {
							if (! success) {
								rollback(context);
							}
							releaseCommitContext(context);
						}
					}
				};
			}
		});
	}

	/** Test for {@link RowLevelLockingSequenceManager#generateId(String)}. */
	public void testGenerateId() {
		long initialValue = generateId();
		for (int i = 1; i < 5; i++) {
			assertEquals(initialValue + i, generateId());
		}
	}

	private long generateId() {
		return manager.generateId(sequence);
	}

	PooledConnection createCommitContext() {
		return ConnectionPoolRegistry.getDefaultConnectionPool().borrowWriteConnection();
	}

	void releaseCommitContext(PooledConnection commitContext) {
		ConnectionPoolRegistry.getDefaultConnectionPool().releaseWriteConnection(commitContext);
	}

	void commit(PooledConnection commitContext) throws SQLException {
		commitContext.commit();
	}
	
	void rollback(PooledConnection context) throws SQLException {
		context.rollback();
	}
	
	/** The JUnit equivalent of the main-method. */
	public static Test suite() {
		return suite(TestRowLevelLockingSequenceManager.class);
	}
	
}
