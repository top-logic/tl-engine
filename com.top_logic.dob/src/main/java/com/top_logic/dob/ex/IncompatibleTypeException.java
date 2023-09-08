
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.ex;

import com.top_logic.dob.DataObjectException;

/** Thrown when Attributes are set with incompatible types
 *
 * @author 	Marco Perra
 */
public class IncompatibleTypeException extends DataObjectException {

    /**
     * Constructs a new IncompatibleTypeException with the specified
     * error message.
     */
    public IncompatibleTypeException (String message) {
        super (message);
    }

    /**
     * Constructs a new IncompatibleTypeException based on some other cause.
     *
     * @param   rootCause the original cause for the Exception
     */
    public IncompatibleTypeException (Throwable rootCause) {
        super (rootCause);
    }
    
}
