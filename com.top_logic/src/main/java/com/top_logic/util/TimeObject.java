
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Date;


/**
 * Object encapsulating creation and change times.
 *
 * @author   <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TimeObject {

    /** The time, this object has been created. */
    private Date created;

    /** The time, this object has been changed for the last time. */
    private Date changed;

    /**
     * Default constructor for creating this object.
     */
    public TimeObject () {
        this.changed = this.created = new Date ();
    }

    /**
     * Returns the date, this object has been created.
     *
     * @return   The date of creation.
     */
    public Date getCreationDate () {
        return (this.created);
    }

    /**
     * Returns the date, this object has been changed.
     *
     * @return   The date of change.
     */
    public Date getChangedDate () {
        return (this.changed);
    }

    /**
     * Modify the change date to the current time and returns this.
     *
     * @return   The new date of change.
     */
    public Date change () {
        return this.changed = new Date ();
    }
}
