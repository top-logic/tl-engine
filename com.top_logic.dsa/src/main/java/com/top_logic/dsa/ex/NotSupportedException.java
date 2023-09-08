/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.ex;

import com.top_logic.dsa.DatabaseAccessException;

/**
 * This exception is thrown when a DataSourceAdaptor implementation doesn't support a method.
 *
 * @author  Karsten Buch / Klaus Halfmann
 */
public class NotSupportedException extends DatabaseAccessException {

    /**
     * Standard CTor using a String message.
     */
    public NotSupportedException (String aMessage) {
        super (aMessage);
    }   
    
    /** 
     * Construct a DatabaseAccessException from some other Exception.
     *
     * @param rootCause - original reason for the Exception
     */
    public NotSupportedException(Throwable rootCause) {
        super(rootCause);
    }
    
    /**
     * Constructor with a Message and some embedded Exception.
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public NotSupportedException(String s, Throwable rootCause) {
        super(s, rootCause);
    }
    
}
