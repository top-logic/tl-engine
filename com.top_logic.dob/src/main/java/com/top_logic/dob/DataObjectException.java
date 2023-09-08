/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

/** Generic superclass for Exceptions found in this package and subpackages.
 *
 * @author 	<a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DataObjectException extends RuntimeException {

    /** Constructs a new DataObjectException with the specified
     * error message.
     */
    public DataObjectException (String message) {
        super (message);
    }

    /** 
     * Constructor.
     *
     * @param rootCause - original reason for the Exception
     */
    public DataObjectException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * Constructor.
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public DataObjectException(String s, Throwable rootCause) {
	    super(s, rootCause);
    }
}
