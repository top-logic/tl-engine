/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.junit;

import org.junit.Assume;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import junit.framework.TestCase;

/**
 * Test for {@link AssumptionAwareRunner}.
 */
public class TestAssumptionAwareRunner extends TestCase {

	/**
	 * Test fixture with a test whose {@link Assume assumption} is violated and a test that passes.
	 */
	public static class Fixture extends TestCase {

		/**
		 * A test with a violated assumption.
		 */
		public void testViolatedAssumption() {
			Assume.assumeTrue("Assumption of the fixture.", false);
			fail("Not reached.");
		}

		/**
		 * A passing test.
		 */
		public void testPassing() {
			// Passes.
		}
	}

	/**
	 * Tests that a violated assumption is reported as a skipped test, not as a failure.
	 */
	public void testReportsViolatedAssumptionAsSkipped() throws Throwable {
		Result result = new JUnitCore().run(Request.runner(new AssumptionAwareRunner(Fixture.class)));

		assertEquals(2, result.getRunCount());
		assertEquals(0, result.getFailureCount());
		assertEquals(1, result.getAssumptionFailureCount());
	}

	/**
	 * A test with a violated assumption that the <code>TestAll</code> of the module reports as
	 * skipped.
	 */
	public void testSkippedInTestAll() {
		Assume.assumeTrue("A violated assumption is reported as skipped.", false);
		fail("Not reached.");
	}

}
