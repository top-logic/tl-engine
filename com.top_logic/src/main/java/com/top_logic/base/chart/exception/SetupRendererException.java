/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.exception;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The SetupRendererException is thrown if during the setup 
 * of a renderer an error occured.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SetupRendererException extends ChartException {

	/**
	 * Creates a {@link SetupRendererException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public SetupRendererException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

    /**
	 * Creates a {@link SetupRendererException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}
	 */
	public SetupRendererException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * @see ChartException#ChartException(Class, String)
	 */
	@Deprecated
    public SetupRendererException(Class aClass, String aKey) {
        super(aClass, aKey);
    }

    /** 
     * @see ChartException#ChartException(Class, String, Throwable)
     */
	@Deprecated
    public SetupRendererException(Class aClass, String aKey, Throwable aCause) {
        super(aClass, aKey, aCause);
    }

    
    
}

