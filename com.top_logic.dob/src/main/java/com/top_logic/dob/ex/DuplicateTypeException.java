/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.ex;

import com.top_logic.dob.DataObjectException;

/** This exception should be thrown when a type is defined twice in the repository
 *
 * @author  Karsten Buch
 */
public class DuplicateTypeException extends DataObjectException {

    /**
     * Constructs a new DuplicateTypeException with the specified
     * error message.
     */
    public DuplicateTypeException (String message) {
        super (message);
    }
}
