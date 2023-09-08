/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Exception to be thrown from points in the control flow, of which the
 * programmer thinks that they are unreachable.
 * 
 * In case you actually can provoke this in a Testcase (and this way cover it)
 * it is a hint that usage of this class was wrong.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnreachableAssertion extends AssertionError {

	/**
	 * Marks the point in the control flow, from which this assertion is thrown
	 * as unreachable.
	 * 
	 * @param reason
	 *     Describes, why the point of throwing is unreachable.
	 */
	public UnreachableAssertion(String reason) {
		super(reason);
	}

	/**
	 * Marks the exception handler, from which this assertion is thrown
	 * as unreachable.
	 */
	public UnreachableAssertion(Throwable cause) {
		initCause(cause);
	}
	
	/**
	 * Marks the exception handler, from which this assertion is thrown
	 * as unreachable.
	 * 
	 * @param reason
	 *     Describes, why the point of throwing is unreachable.
	 */
	public UnreachableAssertion(String reason, Throwable cause) {
		super(reason);
		
		initCause(cause);
	}
	
}
