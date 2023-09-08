/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.exception;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The ChartException is the global exception for charts.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ChartException extends TopLogicException {

	/**
	 * Creates a {@link ChartException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public ChartException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * Creates a {@link ChartException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public ChartException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
	 * @see TopLogicException#TopLogicException(Class, String)
	 */
	@Deprecated
    public ChartException(Class aClass, String aKey) {
        super(aClass, aKey);
    }

    /** 
     * @see TopLogicException#TopLogicException(Class, String, Throwable)
     */
	@Deprecated
    public ChartException(Class aClass, String aKey, Throwable aCause) {
        super(aClass, aKey, aCause);
    }

}

