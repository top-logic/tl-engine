/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

/**
 * This exception is thrown by {@link com.top_logic.element.structured.util.NumberHandler}s to
 * indicate that a problem occured during the generate number prozess. 
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class GenerateNumberException extends Exception {

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     */
    public GenerateNumberException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. 
     */
    public GenerateNumberException(String message) {
        super(message);
    }

}
