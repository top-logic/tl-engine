/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.thread;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadHelper;
import com.top_logic.basic.util.Computation;

/**
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestThreadHelper extends TestCase {

    public void testRun() throws InterruptedException {
        
        ByteArrayOutputStream bos    = new ByteArrayOutputStream(512);
        PrintStream           err    = new PrintStream(bos);
		InterruptedException problem = BasicTestCase.runWithSystemErr(err, new Computation<InterruptedException>() {

			@Override
			public InterruptedException run() {
				try {
					long sleep = 100;
					ThreadHelper th = new ThreadHelper(sleep);
					th.start();
					ThreadHelper.showThreads(th.getThreadGroup());
					ThreadHelper.showSiblingThreads(th);
					Thread.sleep(sleep << 1);
					th.interrupt();
					th.join(sleep);
					assertFalse(th.isAlive());
					return null;
				} catch (InterruptedException ex) {
					return ex;
				}
			}
		});
		if (problem != null) {
			throw problem;
        }
		err.close();
		assertTrue(bos.size() > 300); // as Debugged 477
    }

    /** Test showing all Threads */
	public void testShowAlllThreads() {
        
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
        PrintStream           err    = new PrintStream(bos);
		BasicTestCase.runWithSystemErr(err, new Computation<Void>() {

			@Override
			public Void run() {
				ThreadHelper.showAllThreads();
				assertTrue(bos.size() > 100); // as Debugged 291
				// System.out.println(bos);
				return null;
			}
		});
    }
    
    /** Return the suite of Tests to execute */
    public static Test suite () {
        return new TestSuite(TestThreadHelper.class);
    }

    /** Main function for direct testing */
    public static void main (String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }
}