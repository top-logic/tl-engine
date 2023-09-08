/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search;

/**
 * Superclass for all the Exceptions this package may throw.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class QueryException extends Exception {

    /**
     * Constructor.
     */
    public QueryException() {
	    super();
    }

    /** 
     * Constructor.
     *
     * @param rootCause - original reason for the Exception
     */
    public QueryException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * Constructor.
     * @param s a message string
     */
    public QueryException(String s) {
	    super(s);
    }

    /**
     * Constructor.
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public QueryException(String s, Throwable rootCause) {
	    super(s, rootCause);
    }
    
}
