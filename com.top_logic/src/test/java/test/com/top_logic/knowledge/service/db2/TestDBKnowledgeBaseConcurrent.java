/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Random;

import junit.framework.Test;
import junit.textui.TestRunner;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Test case for parallel access to {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDBKnowledgeBaseConcurrent extends AbstractDBKnowledgeBaseClusterTest {
		
	private   static final int PARALLEL_THREADS = 20;
	protected static final int SEQUENTIAL_CHANGES = 100;
	protected static final int SEQUENTIAL_LOOKUPS = 1000;
	
	private static final long TIMEOUT = 10 * 60 * 1000;

	/**
	 * Create a random mix of new,commit/  new, failed commit()/  
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
							int type = Math.abs(rnd.nextInt()) % 8;
							switch (type) {
							case 0: {
								{
									Transaction tx = begin();
									newB("b" + threadId + "-" + objectId);
									commit(tx);
								}
								break;
							}
							case 1: {
								{
									Transaction tx = begin();
									newB("b" + threadId + "-" + objectId);
									kb().addCommittable(COMMIT_FAILURE);
									commit(tx, true);
								}
								break;
							}
							case 2: {
								{
									Transaction tx = begin();
									newC("c" + threadId + "-" + objectId);
									commit(tx);
								}
								break;
							}
							case 3: {
								{
									Transaction tx = beginNode2();
									newBNode2("b" + threadId + "-" + objectId);
									commitNode2(tx);
								}
								break;
							}
							case 4: {
								{
									Transaction tx = beginNode2();
									newBNode2("b" + threadId + "-" + objectId);
									kbNode2().addCommittable(COMMIT_FAILURE);
									commitNode2Fail(tx);
								}
								break;
							}
							case 5: {
								{
									Transaction tx = beginNode2();
									newCNode2("c" + threadId + "-" + objectId);
									commitNode2(tx);
								}
								break;
							}
							case 6: {
								kb().refetch();
								break;
							}
							case 7: {
								kbNode2().refetch();
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
						int type = threadId % 6;
						for (int objectId = 0; objectId < SEQUENTIAL_CHANGES; objectId++) {
							switch (type) {
							case 0: {
								{
									Transaction tx = begin();
									newB("b" + threadId + "-" + objectId);
									commit(tx);
								}
								break;
							}
							case 1: {
								{
									Transaction tx = begin();
									newC("c" + threadId + "-" + objectId);
									commit(tx);
								}
								break;
							}
							case 2: {
								{
									Transaction tx = begin();
									newD("d" + threadId + "-" + objectId);
									commit(tx);
								}
								break;
							}
							case 3: {
								{
									Transaction tx = beginNode2();
									newBNode2("b" + threadId + "-" + objectId);
									commitNode2(tx);
								}
								break;
							}
							case 4: {
								{
									Transaction tx = beginNode2();
									newCNode2("c" + threadId + "-" + objectId);
									commitNode2(tx);
								}
								break;
							}
							case 5: {
								{
									Transaction tx = beginNode2();
									newDNode2("d" + threadId + "-" + objectId);
									commitNode2(tx);
								}
								break;
							}
							}
						}
					}
				};
			}
		});
	}
	
	public void testConcurrentLookup() throws Throwable {
		{
			// Create objects in first node. 
			Transaction tx = begin();
			for (int objectId = 0; objectId < SEQUENTIAL_LOOKUPS; objectId++) {
				newB("b" + objectId);
				newC("c" + objectId);
				newD("d" + objectId);
			}
			commit(tx);
		}

		// Lookup created objects in second node (requiring fetching them from DB). 
		refetchNode2();
		parallelTest(PARALLEL_THREADS, TIMEOUT, new ExecutionFactory() {
			@Override
			public Execution createExecution(final int threadId) {
				return new Execution() {
					@Override
					public void run() throws DataObjectException {
						DBKnowledgeBase kbNode2 = kbNode2();
						String type;
						String name;
						switch ((threadId % 3)) {
						case 0: {
								type = B_NAME;
							name = "b";
							break;
						}
						case 1: {
								type = C_NAME;
							name = "c";
							break;
						}
						case 2: {
								type = D_NAME;
							name = "d";
							break;
						}
						default: {
							throw new UnreachableAssertion("See switch statement.");
						}
						}

						for (int objectId = 0; objectId < SEQUENTIAL_LOOKUPS; objectId++) {
							DataObject b = kbNode2.getObjectByAttribute(type, A1_NAME, name + objectId);
							assertNotNull(b);
						}
					}
				};
			}
		});
	}
	
    public static Test suite() {
		// Switch to true to activate single test.
		if (false) {
			return runOneTest(TestDBKnowledgeBaseConcurrent.class, "testConcurrentCreates");
		}

        return suite(TestDBKnowledgeBaseConcurrent.class);
    }
    

    /** 
     * Main function for direct testing.
     */
    public static void main (String[] args) {

        Logger.configureStdout("WARN");
        TestRunner.run (suite ());
    }

}
