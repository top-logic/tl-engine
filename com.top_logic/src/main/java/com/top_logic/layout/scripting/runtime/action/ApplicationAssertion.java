/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.layout.scripting.runtime.Application;

/**
 * {@link RuntimeException} that reports a failure in a scripted
 * {@link Application}.
 * 
 * <p>
 * This exception class is a regular {@link RuntimeException} that neither
 * collides with java.lang assertions nor depends on JUnit, which is not
 * available in a deployment.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplicationAssertion extends RuntimeException {

	public ApplicationAssertion() {
		super();
	}

	public ApplicationAssertion(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationAssertion(String message) {
		super(message);
	}

	public ApplicationAssertion(Throwable cause) {
		super(cause);
	}

	
}
