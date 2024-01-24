/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Address;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Test cases for {@link com.top_logic.knowledge.wrap.WrapperFactory}.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestWrapperFactory extends BasicTestCase {
    
    private static final String SUPER_KO = KBTestMeta.TEST_B;
    private static final String SUB_KO   = KBTestMeta.TEST_D;

    /** Number of Objects to test when fetching Wrappers in parallel. */
    private static final int ADR_COUNT = 200;

	private static TLID[] id = new TLID[ADR_COUNT];

    /** 
     * Constructor for a special test.
     *
     * @param name function name of the test to execute.
     */
    public TestWrapperFactory(String name) {
        super(name);
    }

	/**
	 * Tests performance of just fetching wrapper. Used to check whether <tt>synchronized</tt> or
	 * <tt>ReadWriteLock</tt> to synchronize creation and fetching is faster.
	 */
	public void testFetchWrapperPerformance() throws DataObjectException, InterruptedException {
		KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		int numberObjects = 1000;
		ArrayList<KnowledgeObject> kos = new ArrayList<>(numberObjects);
		Transaction createTX = theKB.beginTransaction();
		for (int i = 0; i < numberObjects; i++) {
			kos.add(theKB.createKnowledgeObject(SUPER_KO));
		}
		createTX.commit();

		// must fetch first one time as during this the wrapper is constructed
		final ArrayList<Wrapper> wrappers = new ArrayList<>(kos.size());
		for (KnowledgeObject ko : kos) {
			wrappers.add(WrapperFactory.getWrapper(ko));
		}

		fetchWrappers(kos, wrappers);
	}

	/**
	 * Tests performance of fetching and constructing wrappers. Used to check whether
	 * <tt>synchronized</tt> or <tt>ReadWriteLock</tt> to synchronize creation and fetching is
	 * faster.
	 */
	public void testFetchCreateWrapperPerformance() throws DataObjectException, InterruptedException {
		KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		int numberObjects = 1000;
		ArrayList<KnowledgeObject> kos = new ArrayList<>(numberObjects);
		Transaction createTX = theKB.beginTransaction();
		for (int i = 0; i < numberObjects; i++) {
			kos.add(theKB.createKnowledgeObject(SUPER_KO));
		}
		createTX.commit();

		fetchWrappers(kos, null);
	}

	private void fetchWrappers(final ArrayList<KnowledgeObject> kos,
			final List<?> wrappers) throws InterruptedException {
		int numberThreads = 1;
		final int numberFetches = 1000000;
		final CountDownLatch startSignal = new CountDownLatch(1);
		final CountDownLatch doneSignal = new CountDownLatch(numberThreads);
		for (int i = 0; i < numberThreads; i++) {
			final int threadId = i;
			new Thread("Thread: " + threadId) {

				final Random r = new Random(156235 << threadId);

				final int numberObjects = kos.size();

				@Override
				public void run() {
					try {
						startSignal.await();
						for (int j = 0; j < numberFetches; j++) {
							int koIndex = r.nextInt(numberObjects);
							// fetch or create Wrapper
							Wrapper fetchedWrapper = WrapperFactory.getWrapper(kos.get(koIndex));
							if (wrappers != null) {
								assertSame(wrappers.get(koIndex), fetchedWrapper);
							}
						}
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					} finally {
						doneSignal.countDown();
					}
				}

			}.start();
		}
		// ensure all Threads are created and started
		Thread.sleep(10);
		StopWatch startedWatch = StopWatch.createStartedWatch();
		startSignal.countDown();
		doneSignal.await();
		startedWatch.stop();
		System.out.println("Needed " + StopWatch.toStringNanos(startedWatch.getElapsedNanos()) + " for "
			+ numberFetches + " fetches in each of " + numberThreads + " threads.");
	}
    
    /**
     * Test the getWrapper methods.
     */
    public void testGetWrapper() throws Exception {
        // init the wrapper factory
        KnowledgeBase theKB =
            KBSetup.getKnowledgeBase();
        KnowledgeBase theDefaultKB =
            KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        // create test objects
		KnowledgeObject superObject = theKB.createKnowledgeObject(SUPER_KO);
		KnowledgeObject subObject = theKB.createKnowledgeObject(SUB_KO);
        
        try {
            // get wrappers via the different getWrapper methods
			List<Wrapper> superWrappers = new ArrayList<>(3);
			Wrapper superWrapper = WrapperFactory.getWrapper(superObject);

			superWrappers.add(superWrapper);
			assertSame("Factory return different wrappers for same object (super object).", superWrapper,
				superWrappers.get(0));

			superWrappers.add(WrapperFactory.getWrapper(KBUtils.getObjectName(superObject), SUPER_KO, theKB));
			assertSame("Factory return different wrappers for same object (super object).", superWrapper,
				superWrappers.get(1));

            if (theKB == theDefaultKB) {
				superWrappers.add(WrapperFactory.getWrapper(KBUtils.getObjectName(superObject), SUPER_KO));
				assertSame("Factory return different wrappers for same object (super object).", superWrapper,
					superWrappers.get(2));
            }

			List<Wrapper> subWrappers = new ArrayList<>(3);
			final Wrapper subWrapper = WrapperFactory.getWrapper(subObject);

			subWrappers.add(subWrapper);
			assertSame("Factory return different wrappers for same object (sub object).", subWrapper,
				subWrappers.get(0));

			subWrappers.add(WrapperFactory.getWrapper(KBUtils.getObjectName(subObject), SUB_KO, theKB));
			assertSame("Factory return different wrappers for same object (sub object).", subWrapper,
				subWrappers.get(1));

            if (theKB == theDefaultKB) {
				subWrappers.add(WrapperFactory.getWrapper(KBUtils.getObjectName(subObject), SUB_KO));
				assertSame("Factory return different wrappers for same object (sub object).", subWrapper,
					subWrappers.get(2));
            }

            // Get new Wrappers
            List<Wrapper> theSuperWrappers2 = new ArrayList<>(3);

			final Wrapper subWrapper2 = WrapperFactory.getWrapper(superObject);

			theSuperWrappers2.add(subWrapper2);
			assertSame("Factory return different wrappers for same object (super object).", subWrapper2,
				theSuperWrappers2.get(0));

			theSuperWrappers2.add(WrapperFactory.getWrapper(KBUtils.getObjectName(superObject), SUPER_KO, theKB));
			assertSame("Factory return different wrappers for same object (super object).", subWrapper2,
				theSuperWrappers2.get(1));

            // getWrapper just with id works only for the default kb
            if (theKB == theDefaultKB) {
				theSuperWrappers2.add(WrapperFactory.getWrapper(KBUtils.getObjectName(superObject), SUPER_KO));
				assertSame("Factory return different wrappers for same object (super object).", subWrapper2,
					theSuperWrappers2.get(2));
            }

            List<Wrapper> theSubWrappers2 = new ArrayList<>(3);

			final Wrapper subWrapper3 = WrapperFactory.getWrapper(subObject);

			theSubWrappers2.add(subWrapper3);
			assertSame("Factory return different wrappers for same object (sub object).", subWrapper3,
				theSubWrappers2.get(0));

			theSubWrappers2.add(WrapperFactory.getWrapper(KBUtils.getObjectName(subObject), SUB_KO, theKB));
			assertSame("Factory return different wrappers for same object (sub object).", subWrapper3,
				theSubWrappers2.get(1));

            // getWrapper just with id works only for the default kb
            if (theKB == theDefaultKB) {
				theSubWrappers2.add(WrapperFactory.getWrapper(KBUtils.getObjectName(subObject), SUB_KO));
				assertSame("Factory return different wrappers for same object (sub object).", subWrapper3,
					theSubWrappers2.get(2));
            }
        }
        finally {         // clean up
			theKB.delete(superObject);
			theKB.delete(subObject);
        }
     }

    /**
     * Add some AdressKOs to be checked later. 
     */
    public void addAdressKOs() throws Exception {
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
		try (Transaction theTX = theKB.beginTransaction()) {
			for (int adrNo = 0; adrNo < ADR_COUNT; adrNo++) {
				KnowledgeObject theKO = theKB.createKnowledgeObject(Address.OBJECT_NAME);
				id[adrNo] = theKO.getObjectName();

				theKO.setAttributeValue("street1", "Street " + adrNo);
				theKO.setAttributeValue("city", "City " + adrNo);
				theKO.setAttributeValue("zip", "0 " + adrNo);
				theKO.setAttributeValue("country", "GERMANY");
			}

			theTX.commit();
        }
    }
    
    /**
     * Called via an active testSuite
     * 
     * @throws    Exception   Test method may throw exception. 
     */
    public void fetchAdressWrappers() throws Exception {
        KnowledgeBase kBase = KBSetup.getKnowledgeBase();
        AbstractWrapper.getWrappersFromCollection(kBase.getAllKnowledgeObjects(Address.OBJECT_NAME));
    }

    /**
     * Called via an active testSuite
     * 
     * @throws    Exception   Test method may throw exception. 
     */
    public void fetchAdressWrappersTypeId() throws Exception {
        KnowledgeBase  theKB      = KBSetup.getKnowledgeBase();

        for (int adrNo=0; adrNo < ADR_COUNT; adrNo++ ) {
			WrapperFactory.getWrapper(id[adrNo], Address.OBJECT_NAME, theKB);
        }
    }

    /* The following wrappers provide a wrapper hierarchy for testing. */

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite() {
		if (false) {
			return KBSetup.getSingleKBTest(new TestWrapperFactory(
				"testFetchWrapperPerformance"));
		}
    	return KBSetup.getKBTest(TestWrapperFactory.class, new TestFactory() {
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(suiteName);
		        suite.addTestSuite(TestWrapperFactory.class);
		        suite.addTest(new TestWrapperFactory("addAdressKOs"));
		        ActiveTestSuite parSuite = new ActiveTestSuite("test.com.top_logic.knowledge.wrap.TestWrapperFactory (Active)");
				parSuite.addTest(TestUtils.tryEnrichTestnames(new TestWrapperFactory("fetchAdressWrappers"), "ActiveTestSuite 1"));
				parSuite.addTest(TestUtils.tryEnrichTestnames(new TestWrapperFactory("fetchAdressWrappersTypeId"), "ActiveTestSuite 1"));
				parSuite.addTest(TestUtils.tryEnrichTestnames(new TestWrapperFactory("fetchAdressWrappers"), "ActiveTestSuite 2"));
				parSuite.addTest(TestUtils.tryEnrichTestnames(new TestWrapperFactory("fetchAdressWrappersTypeId"), "ActiveTestSuite 2"));
		        suite.addTest(parSuite);
				return suite;
			}
    	});
    }

    /** Main function for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        
        // KBSetup.CREATE_TABLES = false;
        
        junit.textui.TestRunner.run(suite());
    }
    
}
