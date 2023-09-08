/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * Utility for asserting the reachability or unreachability of certain points of
 * the control flow in test cases.
 * 
 * <p>
 * To use this class, perform the following steps:
 * </p>
 * 
 * <ol>
 * <li>Create a new intance and assign it to a <code>final</code> local
 * variable (to have access to it even in method-local anonymous inner classes).
 * </li>
 * 
 * <li>Call the {@link #touch()} method from the point in the control flow that
 * you want to formulate an assertion about.</li>
 * 
 * <li>At the end of your test, either call {@link #assertReached(String)}, or
 * {@link #assertNotReached(String)} on your {@link ControlFlowAssertion}
 * instance (depending on whether you expect the marked point of the control
 * flow of being reached by your test case or not).</li>
 * </ol>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ControlFlowAssertion extends Assert {
	boolean reached;
	
	/**
	 * Create a new {@link ControlFlowAssertion} instance, which is marked as
	 * "not reached" by default.
	 */
	public ControlFlowAssertion() {
		init();
	}

	private void init() {
		reached = false;
	}

	/**
	 * Marks this {@link ControlFlowAssertion} as beeing reached.
	 * 
	 * <p>
	 * This method is being called from the point in your test case, that you
	 * want to formulate a reachability assertion about.
	 * </p>
	 */
	public void touch() {
		reached = true;
	}

	/**
	 * Same as {@link #assertReached(String)} with an empty message.
	 */
	public void assertReached() {
		assertReached(null);
	}

	/**
	 * Throws an {@link AssertionFailedError}, if this
	 * {@link ControlFlowAssertion} was not yet {@link #touch() reached}.
	 * 
	 * @param message
	 *     A message that should explain, why this
	 *     {@link ControlFlowAssertion} should have been reached.
	 */
	public void assertReached(String message) {
		if (! reached) {
			message = (message != null) ? message + " " : "";
			message += "Should have been reached, but was not.";
			
			fail(message);
		}
	}

	/**
	 * Same as {@link #assertNotReached(String)} with an empty message.
	 */
	public void assertNotReached() {
		assertNotReached(null);
	}

	/**
	 * Throws an {@link AssertionFailedError}, if this
	 * {@link ControlFlowAssertion} was {@link #touch() reached}.
	 * 
	 * @param message
	 *     A message that should explain, why this
	 *     {@link ControlFlowAssertion} should not have been reached.
	 */
	public void assertNotReached(String message) {
		if (reached) {
			message = (message != null) ? message + " " : "";
			message += "Should have not been reached, but was reached.";
			
			fail(message);
		}
	}

	/**
	 * Same as {@link #assertReachability(boolean, String)} with an empty
	 * message.
	 */
	public void assertReachability(boolean reachability) {
		assertReachability(reachability, null);
	}

	/**
	 * Forwards the call either to {@link #assertNotReached(String)}, or
	 * {@link #assertReached(String)}, depending on the given reachability
	 * argument.
	 * 
	 * @param reachability
	 *     If <code>true</code>, assert that this
	 *     {@link ControlFlowAssertion} has been {@link #touch() reached}.
	 *     Otherwise, assert that it has not been reached.
	 * @param message
	 *     See {@link #assertNotReached(String)} or
	 *     {@link #assertReached(String)}, depending on the asserted
	 *     reachability.
	 */
	public void assertReachability(boolean reachability, String message) {
		if (reachability) {
			assertReached(message);
		} else {
			assertNotReached(message);
		}
	}
	
	/**
	 * Resets the status of this {@link ControlFlowAssertion} to its
	 * {@link #ControlFlowAssertion() initial conditions}.
	 */
	public void reset() {
		init();
	}
}
