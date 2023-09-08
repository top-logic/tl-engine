/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

/**
 * {@link RuntimeException} that can be used in test cases to check handling of
 * unexpected exceptions.
 *
 * @see ExpectedError Corresponding error
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpectedFailure extends RuntimeException {

	public ExpectedFailure() {
		super();
	}

	public ExpectedFailure(String message, Throwable cause) {
		super(message, cause);
	}

	public ExpectedFailure(String message) {
		super(message);
	}

	public ExpectedFailure(Throwable cause) {
		super(cause);
	}
	
}
