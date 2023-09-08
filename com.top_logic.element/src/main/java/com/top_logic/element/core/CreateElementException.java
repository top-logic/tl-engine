/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import com.top_logic.element.ElementException;


/**
 * Whenever a creation of an element fails, this exception will inform about the reason of failure.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class CreateElementException extends ElementException {

    /**
     * @param    aMessage    The message explaining the problem.
     */
    public CreateElementException(String aMessage) {
        super(aMessage);
    }

    /**
     * @param    aMessage    The message explaining the problem.
     * @param    aCause      The root cause for this exception.
     */
    public CreateElementException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

}
