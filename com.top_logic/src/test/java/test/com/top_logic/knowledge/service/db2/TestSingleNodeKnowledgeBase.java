/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Tests {@link DBKnowledgeBase} with single node optimization.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSingleNodeKnowledgeBase extends AbstractDBKnowledgeBaseTest {

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new SingleNodeOptimizedTestSetup(self);
	}

	private static final int PARALLEL_THREADS = 20;

	private static final int SEQUENTIAL_CHANGES = 100;

	private static final long TIMEOUT = 10 * 60 * 1000;

	/**
	 * Create a random mix of new,commit/ new, failed commit()/
	 */
	public void testConcurrentRefetchRollback() throws Throwable {
		parallelTest(PARALLEL_THREADS, TIMEOUT, new ExecutionFactory() {
			@Override
			public Execution createExecution(final int threadId) {
				return new Execution() {
					@Override
					public void run() throws DataObjectException, RefetchTimeout {
						Random rnd = new Random(threadId);
						for (int objectId = 0; objectId < SEQUENTIAL_CHANGES; objectId++) {
							int type = Math.abs(rnd.nextInt()) % 4;
							switch (type) {
								case 0: {
									Transaction tx = begin();
									newB("b" + threadId + "-" + objectId);
									commit(tx);
									break;
								}
								case 1: {
									Transaction tx = begin();
									newB("b" + threadId + "-" + objectId);
									kb().addCommittable(COMMIT_FAILURE);
									commit(tx, true);
									break;
								}
								case 2: {
									Transaction tx = begin();
									newC("c" + threadId + "-" + objectId);
									commit(tx);
									break;
								}
								case 3: {
									kb().refetch();
									break;
								}
								default:
									fail("No such job." + type);
							}
						}
					}
				};
			}
		});
	}

	public void testConcurrentCreates() throws Throwable {
		parallelTest(PARALLEL_THREADS, TIMEOUT, new ExecutionFactory() {
			@Override
			public Execution createExecution(final int threadId) {
				return new Execution() {
					@Override
					public void run() throws DataObjectException {
						int type = threadId % 3;
						for (int objectId = 0; objectId < SEQUENTIAL_CHANGES; objectId++) {
							switch (type) {
								case 0: {
									Transaction tx = begin();
									newB("b" + threadId + "-" + objectId);
									commit(tx);
									break;
								}
								case 1: {
									Transaction tx = begin();
									newC("c" + threadId + "-" + objectId);
									commit(tx);
									break;
								}
								case 2: {
									Transaction tx = begin();
									newD("d" + threadId + "-" + objectId);
									commit(tx);
									break;
								}
							}
						}
					}
				};
			}
		});
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSingleNodeKnowledgeBase}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestSingleNodeKnowledgeBase.class, "");
		}
		return suite(TestSingleNodeKnowledgeBase.class);
	}

}
