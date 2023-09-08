
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.ex;

import com.top_logic.dob.DataObjectException;

/**
 * Thrown when a (Metabject) Type is not found somewhere
 *
 * @author  Klaus Halfmann / Marco Perra
 */
public class UnknownTypeException extends DataObjectException {

    /**
     * Constructs a new UnknownTypeException with the specified
     * error message.
     */
    public UnknownTypeException (String message) {
        super (message);
    }

    /**
     * Constructs a new UnknownTypeException from some other Exception.
     */
    public UnknownTypeException (Throwable th) {
        super (th);
    }

    /**
     * New Exception from message and other Exception.
     */
    public UnknownTypeException (String message, Throwable th) {
        super (message, th);
    }
}
