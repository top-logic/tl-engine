/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

/**
 * Test cases if '\' can be used correctly.
 * 
 * @author <a href=mailto:tbe@top-logic.com>jst</a>
 */
public class TestBackslash extends TestCase {

	/** Test if a string literal consisting of a single character can be defined. */
	public void testCharLiteral() throws Throwable {
		String toParse = "<%def (example_var as \"\\376\") %>" + "<%= example_var %>";
		String theExpected = "\376";
		String theResult = ModelUtils.doExpand(toParse);
		assertEquals(theExpected, theResult);
	}

	/** Test if a string literal consisting of a single '\' can be defined. */
	public void testBackslashLiteral() throws Throwable {
		String toParse = "<%def (example_var as \"\\\\\") %>" + "<%= example_var %>";
		String theExpected = "\\";
		try {
			String theResult = ModelUtils.doExpand(toParse);
			assertEquals(theExpected, theResult);
		} catch (Exception ex) {
			throw new AssertionFailedError("Ticket #9701: A string literal consisting of a backslash is not possible.")
				.initCause(ex);
		}
	}

	/** Test if a string literal starting with a '\' can be defined. */
	public void testBackslashStartingLiteral() throws Throwable {
		String toParse = "<%def (example_var as \"\\\\Post\") %>" + "<%= example_var %>";
		String theExpected = "\\Post";
		String theResult = ModelUtils.doExpand(toParse);
		assertEquals(theExpected, theResult);
	}

	/** Test if a string literal ending with a '\' can be defined. */
	public void testBackslashEndingLiteral() throws Throwable {
		String toParse = "<%def (example_var as \"Pre\\\\\") %>" + "<%= example_var %>";
		String theExpected = "Pre\\";
		try {
			String theResult = ModelUtils.doExpand(toParse);
			assertEquals(theExpected, theResult);
		} catch (Exception ex) {
			throw new AssertionFailedError("Ticket #9701: A string literal ending with a backslash is not possible.")
				.initCause(ex);
		}
	}

	/** Test if a string literal containing a '\' can be defined. */
	public void testBackslashContainingLiteral() throws Throwable {
		String toParse = "<%def (example_var as \"Pre\\\\Post\") %>" + "<%= example_var %>";
		String theExpected = "Pre\\Post";
		String theResult = ModelUtils.doExpand(toParse);
		assertEquals(theExpected, theResult);
	}

	/**
	 * Creates a suite containing the tests in this class and taking care of the necessary setups
	 * and teardowns.
	 */
	public static Test suite() {
		// The setup is needed for some of the error messages that are being thrown when something
		// is broken. If everything is green, the setup would not be needed.
		return ModuleTestSetup.setupModule(TestBackslash.class);
	}

}
