/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.Protocol;

/**
 * {@link Protocol} that terminates execution upon the first error in a way conformant to JUnit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssertProtocol extends CheckingProtocol {

	/**
	 * Creates a {@link AssertProtocol}.
	 * 
	 * @see CheckingProtocol#CheckingProtocol()
	 */
	public AssertProtocol() {
		super();
	}

	/**
	 * Creates a {@link AssertProtocol}.
	 *
	 * @see CheckingProtocol#CheckingProtocol(String)
	 */
	public AssertProtocol(String description) {
		super(description);
	}

	@Override
	protected void reportError(String message, Throwable ex) {
		super.reportError(message, ex);
		throw createAbort(message, ex);
	}

}
