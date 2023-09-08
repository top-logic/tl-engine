/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.exception;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The UnsupportedException is thrown if a something is not supported.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class UnsupportedException extends ReportingException {

	/**
	 * Creates a {@link UnsupportedException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public UnsupportedException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
	 * Creates a {@link UnsupportedException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public UnsupportedException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * @see TopLogicException#TopLogicException(Class, String)
	 */
	@Deprecated
    public UnsupportedException(Class aClass, String aKey) {
        super(aClass, aKey);
    }

    /** 
     * @see TopLogicException#TopLogicException(Class, String, Throwable)
     */
	@Deprecated
    public UnsupportedException(Class aClass, String aKey, Throwable aCause) {
        super(aClass, aKey, aCause);
    }
    
}

