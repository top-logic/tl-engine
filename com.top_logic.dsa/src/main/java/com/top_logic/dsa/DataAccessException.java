
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa;

/** 
 * This exception is is thrown during alls sorts of data access.
 * 
 * @author  Klaus Halfmann
 * @author  Steffen Bögelsack
 */
public class DataAccessException extends RuntimeException {

    /**
     * Default CTor.
     */
    public DataAccessException (String aMessage) {
        super (aMessage);
    }

    /**
     * CTor with embedded exception
     */
    public DataAccessException (Exception anException) {
        super (anException);

	}
	
    /**
     * CTor with message and embedded exception.
     */
	public DataAccessException (String aMessage, Exception anException) 
	{
		super (aMessage, anException);
	}
}
