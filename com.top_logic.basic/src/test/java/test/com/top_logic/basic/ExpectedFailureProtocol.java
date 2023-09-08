/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.AssertionFailedError;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;

/**
 * {@link Protocol} that reports errors as {@link ExpectedFailure} exceptions.
 * 
 * <p>
 * This implementation is useful in test cases, where a failure situation is
 * tested. This allows catching exactly the {@link ExpectedFailure} exception
 * and leaving e.g. {@link AssertionError} and {@link AssertionFailedError}
 * uncaught.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpectedFailureProtocol extends BufferingProtocol {

	@Override
	protected RuntimeException createAbort(String message, Throwable cause) {
		return new ExpectedFailure(message, cause);
	}
	
}
