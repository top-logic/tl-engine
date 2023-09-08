
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa;


/** 
 * The DatabaseAccessException is thrown by Datsources for many reasons.
 * 
 * @author  Klaus Halfmann / Cristian Balan
 */
public class DatabaseAccessException extends RuntimeException {

    /**
     * Standard CTor using a String message.
     */
    public DatabaseAccessException (String aMessage) {
        super (aMessage);
    }   
    
    /** 
     * Construct a DatabaseAccessException from some other Exception.
     *
     * @param rootCause - original reason for the Exception
     */
    public DatabaseAccessException(Throwable rootCause) {
        super(rootCause);
    }
    
    /**
     * Constructor with a Message and some embedded Exception.
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public DatabaseAccessException(String s, Throwable rootCause) {
        super(s, rootCause);
    }
}
