
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.ex;

import com.top_logic.dsa.DatabaseAccessException;

/**
 * This exception should be thrown when a database is not known in the DataAccessService
 *
 * @author Karsten Buch
 */
public class UnknownDBException extends DatabaseAccessException {

    /**
     * Constructs a new UnknownDBException with the specified
     * error message.
     */
    public UnknownDBException (String message) {
        super (message);
    }
    
    /** 
     * Construct a UnknownDBException from some other Exception.
     *
     * @param rootCause - original reason for the Exception
     */
    public UnknownDBException(Throwable rootCause) {
        super(rootCause);
    }
    
    /**
     * Constructor with a Message and some embedded Exception.
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public UnknownDBException(String s, Throwable rootCause) {
        super(s, rootCause);
    }
    
    
}
