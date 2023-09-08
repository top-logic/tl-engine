
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

/**
 * Indicates that there is no session for a HTTP request.
 *
 * @author  Michael Gänsler
 */
public class NoSessionFoundException extends Exception {

    /**
     * Constructs a NoSessionFoundException with no specified detail message.
     *
     */
    public NoSessionFoundException () {
        super ();
    }

    /**
     * Constructs a NoSessionFoundException with the specified detail message.
     *
     * @param     aMessage  The message string contains the exception message.
     */
    public NoSessionFoundException (String aMessage) {
        super (aMessage);
    }

    /**
     * Delivers the description of the instance of this exception.
     *
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
        return getClass ().getName () + " ["
        // + "varName = " + String.valueOf ( varName )
        + "]";
    }

}

