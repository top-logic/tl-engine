
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import com.top_logic.dob.DataObjectException;

/**
 * This Exception is thrown in case creation of some Object fails.
 *
 * @author  Marco Perra
 */
public class CreateException extends DataObjectException {

    /**
     * Constructs a new CreateException with the specified
     * error message.
     */
    public CreateException (String message) {
        super (message);
    }
    
    /** 
     * Constructor.
     *
     * @param rootCause - original reason for the Exception
     */
    public CreateException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * Constructor.
     *
     * @param s a message string
     * @param rootCause - original reason for the Exception
     */
    public CreateException(String s, Throwable rootCause) {
        super(s, rootCause);
    }
    
}
