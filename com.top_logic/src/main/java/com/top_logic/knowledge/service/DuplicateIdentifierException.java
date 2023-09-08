
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.dob.DataObjectException;

/**
 * This Exception is thrown in case an Idnetifier is found twice
 *
 * @author      Klaus Halfmann / Jörg Connotte
 */
public class DuplicateIdentifierException extends DataObjectException {

    public DuplicateIdentifierException (String message) {
        super (message);
    }
    
    /** 
     * Constructor with embedded Exception
     *
     * @param rootCause - original reason for the Exception
     */
    public DuplicateIdentifierException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * Constructor with message and embedded Exception
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public DuplicateIdentifierException(String s, Throwable rootCause) {
        super(s , rootCause);
    }
    

}
