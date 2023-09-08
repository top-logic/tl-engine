/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office;

/**
 * Base exception for errors occuring in working with MS-Office.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class OfficeException extends Exception {

    /**
     * Constructor for this exception.
     * 
     * @param    aMessage    The description of the problem occured, may not be
     *                       <code>null</code>.
     */
    public OfficeException(String aMessage) {
        super(aMessage);
    }

    /**
     * Constructor for this exception.
     * 
     * @param    aMessage    The description of the problem occured, may not be
     *                       <code>null</code>.
     * @param    aCause      The original exception thrown (may be <code>null</code>.
     */
    public OfficeException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }
}
