/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster;

/**
 * Generic exception used by storage classes.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class ClusterManagerException extends RuntimeException {

    /**
     * Constructs a new ClusterManagerException.
     */
    public ClusterManagerException() {
        super();
    }

    /**
     * Constructs a new ClusterManagerException with the specified detail message.
     *
     * @param aMessage
     *            the detail message
     */
    public ClusterManagerException(String aMessage) {
        super(aMessage);
    }

    /**
     * Constructs a new ClusterManagerException with the specified cause.
     *
     * @param aCause
     *            the cause
     */
    public ClusterManagerException(Throwable aCause) {
        super(aCause);
    }

    /**
     * Constructs a new ClusterManagerException with the specified detail message and cause.
     *
     * @param aMessage
     *            the detail message
     * @param aCause
     *            the cause
     */
    public ClusterManagerException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

}
