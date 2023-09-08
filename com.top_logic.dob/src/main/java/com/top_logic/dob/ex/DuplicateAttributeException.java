/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.ex;

import com.top_logic.dob.DataObjectException;

/** This Exception is thrown by MetaObject on appropriate circumstances.
 *
 * @author 	Klaus Halfmann / Marco Perra
 */
public class DuplicateAttributeException extends DataObjectException {

    /** Constructs a new DuplicateAttributeException with the specified
     * error message.
     */
    public DuplicateAttributeException (String message) {
        super (message);
    }
}
