/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sched;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.basic.sched.ScheduledThread;
import com.top_logic.basic.thread.CancelScope;

/**
 * Tests for {@link com.top_logic.basic.sched.Batch} class.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestBatch extends TestCase {
    
    /**
     * Constructor for TestBatch.
     * 
     * @param name method name of the test to execute.
     */
    public TestBatch(String  name) {
        super(name);
    }

    /** Main TestCase for now (class is actually very simple). */
    public void testSignalStop() throws InterruptedException {
        
        long now = System.currentTimeMillis();
        
        ExampleBatch theBatch = new ExampleBatch("Example1");
        ScheduledThread sThread = new ScheduledThread(theBatch);
        sThread.start(System.currentTimeMillis());
        assertTrue(sThread.signalStop());
        assertTrue(theBatch.getShouldStop());
        Thread.sleep(50); // allow propagation of interrupt
        assertTrue(theBatch.interrupted);
        assertTrue(theBatch.getStarted() >= now);
        assertTrue(theBatch.getStarted() <= System.currentTimeMillis());
        sThread.join();
        
        now = System.currentTimeMillis();
        theBatch = new ExampleBatch("Example2");
        theBatch.allowStop = false;
        sThread = new ScheduledThread(theBatch);
        sThread.start(System.currentTimeMillis());
        assertTrue(!sThread.signalStop());
        assertTrue(theBatch.getShouldStop());
        Thread.sleep(50); // allow propagation of interrupt
        assertTrue("Batch not interrupted ?", theBatch.interrupted);
        assertTrue(theBatch.getStarted() >= now);
        assertTrue(theBatch.getStarted() <= System.currentTimeMillis());
        sThread.join();
   }

	/**
	 * {@link ScheduledThread#signalStop()} while the target works outside an interruptible section
	 * does not interrupt the thread.
	 */
	public void testSignalStopDoesNotInterruptWork() throws InterruptedException {
		CountDownLatch working = new CountDownLatch(1);
		CountDownLatch stopSignalled = new CountDownLatch(1);
		boolean[] interruptedAfterStop = new boolean[1];
		boolean[] cancelled = new boolean[1];
		BatchImpl batch = new BatchImpl("Work") {
			@Override
			public void run() {
				working.countDown();
				// Busy work that only notices the stop by polling, as a database access would.
				long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
				while (stopSignalled.getCount() > 0 && System.nanoTime() < deadline) {
					Thread.onSpinWait();
				}
				interruptedAfterStop[0] = Thread.currentThread().isInterrupted();
				cancelled[0] = CancelScope.isCurrentCancelled();
			}
		};
		ScheduledThread thread = new ScheduledThread(batch);
		thread.start(System.currentTimeMillis());
		assertTrue(working.await(10, TimeUnit.SECONDS));
		assertTrue(thread.signalStop());
		stopSignalled.countDown();
		thread.join(10000);
		assertFalse(thread.isAlive());
		assertFalse("The stop must not interrupt the thread outside an interruptible section.",
			interruptedAfterStop[0]);
		assertTrue("The target runs in the scope the stop cancels.", cancelled[0]);
	}

	/**
	 * {@link ScheduledThread#signalStop()} ends a {@link CancelScope#sleep(long)} of the target at
	 * once.
	 */
	public void testSignalStopEndsSleep() throws InterruptedException {
		CountDownLatch sleeping = new CountDownLatch(1);
		boolean[] aborted = new boolean[1];
		boolean[] interruptedAfterwards = new boolean[1];
		BatchImpl batch = new BatchImpl("Sleep") {
			@Override
			public void run() {
				sleeping.countDown();
				try {
					CancelScope.sleep(TimeUnit.MINUTES.toMillis(1));
				} catch (AbortExecutionException ex) {
					aborted[0] = true;
				} catch (InterruptedException ex) {
					// Not expected: the stop must not leave a foreign interrupt.
				}
				interruptedAfterwards[0] = Thread.currentThread().isInterrupted();
			}
		};
		ScheduledThread thread = new ScheduledThread(batch);
		thread.start(System.currentTimeMillis());
		assertTrue(sleeping.await(10, TimeUnit.SECONDS));
		long before = System.nanoTime();
		assertTrue(thread.signalStop());
		thread.join(10000);
		assertFalse("The stop must end the sleep.", thread.isAlive());
		assertTrue(System.nanoTime() - before < TimeUnit.SECONDS.toNanos(10));
		assertTrue("The sleep ends with the cancellation of the scope.", aborted[0]);
		assertFalse("The scope's interrupt must not outlive the sleep.", interruptedAfterwards[0]);
	}

    /** Return the suite of tests to execute.
     */
    public static Test suite () {

        return new TestSuite (TestBatch.class);
        // return new TestBatch("testSignalStop");
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
}
