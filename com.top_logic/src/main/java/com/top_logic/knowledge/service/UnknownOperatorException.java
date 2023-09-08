
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * Thrown to indicate that the operator is not be supported.
 *
 * @author Marco Perra
 */
public class UnknownOperatorException extends Exception {

    /**
     * Constructs an UnknownOperatorException with no specified detail message.
     *
     */
    public UnknownOperatorException () {
        super ();
    }

    /**
     * Constructs an UnknownOperatorException with the specified detail message.
     */
    public UnknownOperatorException (String msg) {
        super (msg);
    }
}

