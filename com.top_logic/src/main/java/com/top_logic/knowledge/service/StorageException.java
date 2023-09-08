/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * Generic exception used by storage classes.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class StorageException extends Exception {

    /**
     * Constructs a new StorageException.
     */
    public StorageException() {
        super();
    }

    /**
     * Constructs a new StorageException with the specified detail message.
     *
     * @param aMessage
     *            the detail message
     */
    public StorageException(String aMessage) {
        super(aMessage);
    }

    /**
     * Constructs a new StorageException with the specified cause.
     *
     * @param aCause
     *            the cause
     */
    public StorageException(Throwable aCause) {
        super(aCause);
    }

    /**
     * Constructs a new StorageException with the specified detail message and cause.
     *
     * @param aMessage
     *            the detail message
     * @param aCause
     *            the cause
     */
    public StorageException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

}
