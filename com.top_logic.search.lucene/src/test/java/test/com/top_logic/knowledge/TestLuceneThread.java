/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.indexing.lucene.LuceneIndexingService;
import com.top_logic.knowledge.indexing.lucene.LuceneThread;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;

/**
 * Create a more LuceneThreads to simulate a cluster an feed it with some
 * content.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestLuceneThread extends BasicTestCase {
	
	public static final int NUM_CONTENT = 1000;

	public static final int WORD_LENGTH = 1024;

	public static final float SLEEP_PROP = 0.10f;

	public static final float WAIT_FOR_QUEUE = 100;

	public static final long SLEEP_TIME = 1000L;

	Random random;

	/** Each lucene thread simulates a different cluster node */
	LuceneThread[] luceneThread;

	/**
	 * Listener that feeds the lucene threads with modification in the
	 * {@link KnowledgeBase}
	 */
	private UpdateListener listener;

	/** number of simulated cluster nodes. */
	private int numberCluster = 5;

	/**
	 * Mapping from {@link TLID id of object} to some string representation of the object. May be
	 * accessed concurrently.
	 */
	private Map<TLID, String> _nameById;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_nameById = new ConcurrentHashMap<>();
		luceneThread = new LuceneThread[numberCluster];
		random = new Random(47111174);
		LuceneIndex index = LuceneIndex.Module.INSTANCE.getImplementationInstance();
		for (int i = 0; i < numberCluster; i++) {
			luceneThread[i] = new LuceneThread(index, this, kb().getHistoryManager());
			luceneThread[i].setName("TestLuceneThread_" + i);
			luceneThread[i].start();
		}
		listener = new UpdateListener() {

			@Override
			public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
				for (ObjectKey createdKeys : event.getCreatedObjectKeys()) {
					String description = "Comment#" + createdKeys.asString();
					String words = StringServices.getRandomWords(random, random.nextInt(WORD_LENGTH));
					KnowledgeObject ko = (KnowledgeObject) sender.resolveObjectKey(createdKeys);
					ContentObject content = DefaultIndexingService.createContent(ko, words, description);
					
					luceneThread[getThreadNumber(createdKeys)].add(content);
				}
				for (ObjectKey deletedKeys : event.getDeletedObjectKeys()) {
					luceneThread[getThreadNumber(deletedKeys)].delete(deletedKeys);
				}
			}

			/** 
			 * Resolves the thread number from the given {@link ObjectKey}
			 */
			private int getThreadNumber(ObjectKey key) {
				String id = name(key.getObjectName());
				return Integer.parseInt(id.substring(0, id.indexOf('_')));
			}
			
		};
		kb().addUpdateListener(listener);
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	protected void tearDown() throws Exception {
		kb().removeUpdateListener(listener);
		super.tearDown();
	}

	public void testCluster() throws InterruptedException {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener();
		StopWatch sw = StopWatch.createStartedWatch();
		parallelTest(numberCluster, Long.MAX_VALUE, new ExecutionFactory() {

			final AtomicInteger counter = new AtomicInteger();

			@Override
			public Execution createExecution(final int id) {
				return new Execution() {

					@Override
					public void run() throws Exception {
						testRandomComments(id, counter);
					}
				};
			}
		});
		logListener.assertNoErrorLogged("Lucene cluster test failed: ");
		System.out.println("Test needed " + sw.stop());
	}

	/**
	 * Feed a second LuceneThread with Comments.
	 */
	void testRandomComments(int threadNumber, AtomicInteger counter) throws InterruptedException, DataObjectException {

		List<KnowledgeObject> contents = new LinkedList<>();
		KnowledgeBase kBase = PersistencyLayer.getKnowledgeBase();

		int i = 0;
		while (i < NUM_CONTENT) {
			Transaction tx = kBase.beginTransaction();
			int numberCreations = 1 + random.nextInt(5);
			i += numberCreations;
			for (int j = 0; j < numberCreations; j++) {
				KnowledgeObject newContent = kb().createKnowledgeObject(KBTestMeta.TEST_B);
				assignName(newContent.getObjectName(), getNewObjectName(threadNumber, counter));
				contents.add(newContent);
				if (random.nextBoolean()) {
					KnowledgeObject delContent = contents.remove(random.nextInt(contents.size()));
					delContent.delete();
				}
			}
			tx.commit();

			if (luceneThread[threadNumber].queueSizes() > WAIT_FOR_QUEUE || random.nextFloat() <= SLEEP_PROP) {
				Thread.sleep(SLEEP_TIME);
			}
		}

		Transaction tx = kBase.beginTransaction();
		for (KnowledgeObject delObject : contents) {
			delObject.delete();
		}
		tx.commit();

		int numberSleeps = 0;
		while (luceneThread[threadNumber].queueSizes() > 0) {
			numberSleeps++;
			Thread.sleep(1000);
		}
		System.out.println(luceneThread[threadNumber] + " sleeps " + numberSleeps + " times.");
	}

	/** 
	 * Returns a new object name for the given thread number.
	 */
	private String getNewObjectName(int threadNumber, AtomicInteger counter) {
		return threadNumber + "_" + counter.incrementAndGet();
	}
	
	private void assignName(TLID id, String name) {
		_nameById.put(id, name);
	}

	String name(TLID id) {
		return _nameById.get(id);
	}

	public static Test suite() {
		Test t = new TestSuite(TestLuceneThread.class);
		t = ServiceTestSetup.createSetup(t, LuceneIndexingService.Module.INSTANCE);
		return KBSetup.getSingleKBTest(t);
	}

}
