/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.exception;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The CreateException is thrown if an error occured during the creation of a dom document.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class CreateException extends TopLogicException {

	/**
	 * Creates a {@link CreateException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public CreateException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
	 * Creates a {@link CreateException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public CreateException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * Creates a {@link CreateException}.
	 * 
	 */
	@Deprecated
	public CreateException(Class aClass, String aKey) {
		super(aClass, aKey);
	}

	@Deprecated
	public CreateException(Class aClass, String aKey, Throwable aCause) {
		super(aClass, aKey, aCause);
	}
}
