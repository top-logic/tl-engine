/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.license;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link Exception} to indicate that a licence is invalid.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class InvalidLicenceException extends I18NException {

	/**
	 * Creates a new {@link InvalidLicenceException}.
	 * 
	 * @see I18NException#I18NException(ResKey, Throwable)
	 */
	public InvalidLicenceException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * Removes the stack trace from this exception.
	 */
	public InvalidLicenceException dropStack() {
		InvalidLicenceException result = new InvalidLicenceException(getErrorKey());
		result.setStackTrace(new StackTraceElement[0]);
		return result;
	}

}

