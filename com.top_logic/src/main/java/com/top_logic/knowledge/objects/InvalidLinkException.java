
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import com.top_logic.dob.DataObjectException;

/**
 * Thrown in case a Source or Destaintion Object of a KA cannot be found.
 *
 * @author Marco Perra
 */
public class InvalidLinkException extends DataObjectException {


    /**
     * Constructs a new InvalidLinkException with the specified
     * error message.
     */
    public InvalidLinkException (String message) {
        super (message);
    }

    /**
     * Constructs a new InvalidLinkException with some other, inner Exception
     */
    public InvalidLinkException (Throwable inner) {
        super (inner);
    }
}
