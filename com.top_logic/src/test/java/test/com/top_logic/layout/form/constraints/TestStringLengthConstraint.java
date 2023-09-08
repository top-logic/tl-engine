/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.StringLengthConstraint;

/**
 * Testcase for the StringLengthConstraint
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestStringLengthConstraint extends BasicTestCase {

	public static Test suite() {
		TestSuite theSuite = new TestSuite(TestStringLengthConstraint.class);
		return TLTestSetup.createTLTestSetup(theSuite);
	}

	public void testAllowEmpty() throws Exception {
		StringLengthConstraint constraint = new StringLengthConstraint(4, 5, true);

		constraint.check("");
		constraint.check("   ");
		constraint.check(null);

		try {
			constraint.check("123");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		constraint.check("1234");
		constraint.check("12345");

		try {
			constraint.check("123456");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}
	}

	public void testUpperBound() throws Exception {
		StringLengthConstraint constraint = new StringLengthConstraint(-1, 5);

		constraint.check("");
		constraint.check(null);
		constraint.check("1234");
		constraint.check("12345");

		try {
			constraint.check("123456");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}
	}

	public void testLowerBound() throws Exception {
		StringLengthConstraint constraint = new StringLengthConstraint(5);

		try {
			constraint.check("");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		try {
			constraint.check(null);
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		try {
			constraint.check("1234");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		constraint.check("12345");
		constraint.check("123456");
	}

	public void testLowerUpperBound() throws Exception {
		StringLengthConstraint constraint = new StringLengthConstraint(4, 5);

		try {
			constraint.check("");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		try {
			constraint.check(null);
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		try {
			constraint.check("123");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}

		constraint.check("1234");
		constraint.check("12345");

		try {
			constraint.check("123456");
			fail("Expected CheckException");
		} catch (CheckException e) {
			// expected
		}
	}
}
