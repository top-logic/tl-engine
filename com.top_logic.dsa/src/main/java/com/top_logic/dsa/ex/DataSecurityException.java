/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.ex;

/** 
 * This exception can be used within the adaptors, when problems occure in
 * changing data.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DataSecurityException extends DataChangeException {

    /**
     * Creates an exception with the given message.
     *
     * @param    aMessage    The message describing the problem.
     */
    public DataSecurityException (String aMessage) {
        super (aMessage);
    }

    /**
     * Creates an exception with the given message and exception.
     *
     * @param    aMessage       The message describing the problem.
     * @param    anException    The exception this exception resulting from.
     */
    public DataSecurityException (String aMessage, Throwable anException) {
        super (aMessage, anException);
    }

}
