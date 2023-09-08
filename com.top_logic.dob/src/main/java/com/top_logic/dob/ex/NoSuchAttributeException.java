
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.ex;

import com.top_logic.dob.DataObjectException;

/**
 * Thrown in case a DataObject ore Metaobject does not contain an Attribute.
 *
 * @author  Marco Perra
 */
public class NoSuchAttributeException extends DataObjectException {

    /**
     * Constructs a new NoSuchAttributeException with the specified
     * error message.
     */
    public NoSuchAttributeException (String message) {
        super (message);
    }

    /**
     * Constructs a new NoSuchAttributeException based on some other cause.
     *
     * @param   rootCause the original cause for the Exception
     */
    public NoSuchAttributeException (Throwable rootCause) {
        super (rootCause);
    }
}
