
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.ex;

import com.top_logic.dsa.DatabaseAccessException;

/** 
 * This exception can be used within the EventHadlers, when problems occur in
 * changing data.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DataChangeException extends DatabaseAccessException {

    /**
     * Creates an exception with the given message.
     *
     * @param    aMessage    The message describing the problem.
     */
    public DataChangeException (String aMessage) {
        super (aMessage);
    }

    /**
     * Creates an exception with the given message and exception.
     *
     * @param    aMessage       The message describing the problem.
     * @param    anException    The exception this exception resulting from.
     */
    public DataChangeException (String aMessage, Throwable anException) {
        super (aMessage, anException);
    }

}
