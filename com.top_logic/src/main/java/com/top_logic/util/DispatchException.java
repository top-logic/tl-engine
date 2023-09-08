
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

/**
 * This class handles all exceptions that can be thrown in the 
 * context of dispatching requests to system components.
 * 
 * @author    Bettina Hofstaetter
 */
public class DispatchException extends Exception {    // Constants

	public DispatchException (String aMessage, Throwable nested ) {
        super (aMessage, nested);
    }   
	
}
