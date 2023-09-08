
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * Exception is thrown if a Identifier is not found in tables.
 *
 * @author Jörg Connotte
 */
public class UnknownIdentifierException extends Exception {

    
    public UnknownIdentifierException () {
        super ();
    }

    public UnknownIdentifierException (String message) {
        super (message);
    }

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
        return new StringBuffer (getClass ().getName ()).append (" [")
            .append (']').toString ();
    }

}
