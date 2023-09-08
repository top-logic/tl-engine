/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.flex.exception;

import com.top_logic.base.chart.exception.ChartException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The FilterRefreshException indicates that the refreshing was aborted because 
 * an error occured during the refreshing.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class FilterRefreshException extends ChartException {

	/**
	 * Creates a {@link FilterRefreshException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public FilterRefreshException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

    /**
	 * Creates a {@link FilterRefreshException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public FilterRefreshException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * @see TopLogicException#TopLogicException(Class, String)
	 */
	@Deprecated
    public FilterRefreshException(Class aClass, String aKey) {
        super(aClass, aKey);
    }

    /** 
     * @see TopLogicException#TopLogicException(Class, String, Throwable)
     */
	@Deprecated
    public FilterRefreshException(Class aClass, String aKey, Throwable aCause) {
        super(aClass, aKey, aCause);
    }

}

