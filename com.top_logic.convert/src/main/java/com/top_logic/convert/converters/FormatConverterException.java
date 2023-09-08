
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.convert.converters;

/**
 * Thrown to indicate that the invoked method is not supported.
 * This is a RuntimeException.

 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class FormatConverterException extends RuntimeException {

    /**
     * Constructs an FormatConverterException with a specified detail message.
     *
     * @param   aMessage    The description of the exception.
     */
    public FormatConverterException (String aMessage) {
        super (aMessage);
    }

    /**
     * Constructs an FormatConverterException with a specified detail message.
     *
     * @param   aMessage    The description of the exception.
     */
    public FormatConverterException (String aMessage, Throwable aThrowable) {
        super (aMessage, aThrowable);
    }

    /**
     * Constructs an FormatConverterException with some other cause
     *
     * @param   aThrowable    The original cause
     */
    public FormatConverterException (Throwable aThrowable) {
        super (aThrowable);
    }
}
