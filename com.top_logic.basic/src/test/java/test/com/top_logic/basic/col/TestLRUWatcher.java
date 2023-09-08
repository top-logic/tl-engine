/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Random;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.col.LRU;
import com.top_logic.basic.col.LRUWatcher;

/**
 * Testcase for the {@link com.top_logic.basic.col.LRUWatcher}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestLRUWatcher extends BasicTestCase {

    /** Number of testLRUs to use for testing. */
    public static final int COUNT     = 20;     // Must at leat sleep once
    
    /** Number of times to execute tha actual run.*/
    public static final int LOOP      = 10000;

    /** Maximum (randomized) expiration time (in millis). */
    public static final int EXP_TIME  = 20000;

    /** Internal time to step the virtual time (in millis). */
    public static final int STEP_TIME = 100;


    /** A Randomized to reproduce the Result */
    Random rand;

    /** The current time used for testing- */
    long   testingMillis;

    /**
     * Constructor for TestLRUWatcher.
     * 
     * @param name function to execute for testing
     */
    public TestLRUWatcher(String name) {
        super(name);
    }


    /** Setup up the Random to a well defined value */
    @Override
	public void setUp() {
        rand = new Random(0x2004071991704002L);
    }

    /**
     * Release the Randomizer.
     */
    @Override
	protected void tearDown() throws Exception {
        rand = null;
    }

    /** When using only one (or a few) elements there may be a race condition */
    public void testFewElems() throws InterruptedException {
        
        TestedLRUWatcher mw = new TestedLRUWatcher();
        
        for (int i=0; i < COUNT; i++) {
            mw.register(new TestingLRU(0));
            testingMillis += STEP_TIME;
            Thread.sleep(10);
        }
        while (mw.size() > 0) {
            Thread.sleep(10);
        }
    }
    
    /** When using only a few elements there may be a race condition */
    public void testFewElems0() throws InterruptedException {

        TestedLRUWatcher mw = new TestedLRUWatcher();

        for (int i=0; i < COUNT; i++) {
            mw.register(new TestingLRU(0));
            while (mw.size() > 0) {
                Thread.sleep(10);
            }
        }
        while (mw.size() > 0) {
            Thread.sleep(50);
        }
    }

    /** When using only a few elements there may be a race condition */
    public void testFewElems1() throws InterruptedException {
        
        TestedLRUWatcher mw = new TestedLRUWatcher();
        
        for (int i=0; i < COUNT; i++) {
            mw.register(new TestingLRU(0));
            while (mw.size() > 1) {
                Thread.sleep(10);
            }
        }
        while (mw.size() > 0) {
            Thread.sleep(10);
        }
    }
    
    class TestingLRU implements LRU {
        
        /** fake static expriation time */ 
        long nextExp;
        
        public TestingLRU(long aNextExp) {
            this.nextExp = aNextExp;
        }
        
        /**
         * Implemented to Test the Watcher with some random data.
         */
        @Override
		public long nextExpiration() {
            return nextExp;
        }

        /**
         * @retunr true when nextExp is 0 -> remove me from Watcher.
         */
        @Override
		public long removeExpired() { return nextExpiration(); }

    }
    
    /** Override the LRUMapWatcher to make size() acessible */
    class TestedLRUWatcher extends LRUWatcher {
        
        /** Allow access to current numer or LRUs */
        public synchronized int size() {
            return this.lrus.size();
        }

        /** Allow access to the internal Thread */
        public synchronized Thread getThread() {
            return this.thread;
        }
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite() {
        ActiveTestSuite aSuite = new ActiveTestSuite();
        
        aSuite.addTest(TestUtils.tryEnrichTestnames(new TestSuite(TestLRUWatcher.class), "ActiveTestSuite 1"));
        aSuite.addTest(TestUtils.tryEnrichTestnames(new TestSuite(TestLRUWatcher.class), "ActiveTestSuite 2"));
        aSuite.addTest(TestUtils.tryEnrichTestnames(new TestSuite(TestLRUWatcher.class), "ActiveTestSuite 3"));
        
		return ModuleTestSetup.setupModule(aSuite);
    }

    /** main function for direct testing.
     */
    public static void main(String[] args) {
        
        junit.textui.TestRunner.run(suite());
    }



}
