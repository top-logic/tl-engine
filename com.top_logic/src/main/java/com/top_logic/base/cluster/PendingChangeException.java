/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster;

/**
 * This exception is thrown if the confirmed value of a property cannot be determined
 * because of a pending value change. The Exception can be asked for the old and new value
 * in that case.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class PendingChangeException extends RuntimeException {

    /** The property of the pending value change. */
    private String property;

    /** The last confirmed value. */
    private Object oldValue;

    /** The pending new value. */
    private Object newValue;


    /**
     * Creates a new instance of this class.
     *
     * @param property
     *        the id of the change to wait for confirmation
     * @param oldValue
     *        the last confirmed value
     * @param newValue
     *        the pending new value
     */
    public PendingChangeException(String property, Object oldValue, Object newValue) {
        super("A value change for the property '" + property + "' is pending. Last confirmed value: '" + oldValue + "'; Pending value: '" + newValue + "'");
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }


    /**
     * Gets the property of the pending value change.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Gets the last confirmed value.
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Gets the pending new value.
     */
    public Object getNewValue() {
        return newValue;
    }

}
