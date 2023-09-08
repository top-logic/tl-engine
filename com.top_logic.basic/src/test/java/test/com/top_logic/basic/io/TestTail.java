/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.Tail;

/**
 * Testcase for {@link Tail}.
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestTail extends TestCase {

	public TestTail(String test) {
		super(test);
	}

	/**
	 * Test using two Files.
	 */
	public void testFiles() throws IOException, InterruptedException {
		File tmpFile = BasicTestCase.createTestFile("tail", ".txt");
		File outFile = BasicTestCase.createTestFile("out", ".txt");
		try {
			FileWriter fw = new FileWriter(tmpFile);
			try {
				PrintWriter out = new PrintWriter(fw, true);
				final FileWriter output = new FileWriter(outFile.getAbsolutePath());
				try {
					final FileReader input = new FileReader(tmpFile.getAbsolutePath());
					try {
						final Tail tail = Tail.createTail(input, new PrintWriter(output));
						assertNotNull(tail.toString());
						Thread t = new Thread() {
							@Override
							public void run() {
								tail.observe();
							}
						};
						for (int i = 0; i < 10; i++) {
							out.println("Line " + i);
						}
						t.start();
						Thread.sleep(100);
						for (int i = 10; i < 20; i++) {
							out.println("Line " + i);
						}
						Thread.sleep(200);
						tail.stop();
						t.join(200);
					} finally {
						input.close();
					}
				} finally {
					output.close();
				}
			} finally {
				fw.close();
			}
			assertTrue(tmpFile.delete());
			tmpFile = null;

			String result = FileUtilities.readFileToString(outFile);

			assertTrue(outFile.delete());
			outFile = null;

			assertTrue(result.indexOf("Line 0") == 0);
			assertTrue(result.indexOf("Line 19") > 0);

		} finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
			if (outFile != null) {
				outFile.delete();
			}
		}
	}

	/*
	 * public void testTailFilePrintStream() { fail("Not yet implemented"); }
	 * public void testToString() { fail("Not yet implemented"); } public void
	 * testObserve() { fail("Not yet implemented"); } public void testFilter() {
	 * fail("Not yet implemented"); } public void testMain() {
	 * fail("Not yet implemented"); }
	 */

	/**
	 * Return the suite of tests to execute.
	 */
	public static Test suite() {
		return new TestSuite(TestTail.class);
		// return new TestTail ("testPrintStream");
	}

	/**
	 * main function for direct testing.
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

}
