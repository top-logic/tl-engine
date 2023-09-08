/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.evt;

import java.util.EventObject;


/** 
 * The event holding the information about a change in an adaptor.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DataChangeEvent extends EventObject {

    /** Bitmask to extravt the Change Type of mode only */
    public static final int CHANGEMASK      = 0x000F;

    /** Bitmask to extract the Container Type of mode only */
    public static final int CONTAINERMASK   = 0x0F00;

    /** mode value indicating that something will be/was created. */
    public static final int CREATED     = 0x0000;

    /** mode value indicating that something will be/was modified. */
    public static final int MODIFIED    = 0x0001;

    /** mode value indicating that something will be/was deleted. */
    public static final int DELETED     = 0x0002;

    /** mode value indicating that something will be/was deleted recursicly. */
    public static final int DELETEDREC  = 0x0004;

    /** mode value used as bitmask to indicate that a container is involved */
    public static final int ENTRY       = 0x0000;

    /** mode value indication that a new Entry will be/was created. */
    public static final int ENTRY_CREATED   = ENTRY | CREATED;

    /** mode value indication that an existing Entry will be/was modified. */
    public static final int ENTRY_MODIFIED  = ENTRY | MODIFIED;

    /** mode value indication that an existing Entry will be/was deleted. */
    public static final int ENTRY_DELETED   = ENTRY | DELETED;

    /** mode value indication that an existing Entry will be/was deleted recusrivly.
     *
     * This will not happen for File-like DSAs but for others this may well happen.
     */
    public static final int ENTRY_DELETEDREC = ENTRY | DELETEDREC;
    
    /** mode value used as bitmask to indicate that a container is involved */
    public static final int CONTAINER       =  0x0100;
    
    /** mode value indication that a new Container will be/was created. */
    public static final int CONTAINER_CREATED       = CONTAINER | CREATED;

    /** mode value indication that an existing Entry will be/was modified. */
    public static final int CONTAINER_MODIFIED      = CONTAINER | MODIFIED;

    /** mode value indication that an existing Entry will be/was deleted. */
    public static final int CONTAINER_DELETED       = CONTAINER | DELETED;

    /** mode value indication that an existing Entry will be/was deleted. */
    public static final int CONTAINER_DELETEDREC    = CONTAINER | DELETEDREC;
    
    /** @deprecated please use ENTRY_CREATED. */
    @Deprecated
	public static final int DATA_CREATED    = ENTRY_CREATED;

    /** @deprecated please use ENTRY_MODIFIED. */
    @Deprecated
	public static final int DATA_MODIFIED   = ENTRY_MODIFIED;

    /** @deprecated please use ENTRY_DELETED. */
    @Deprecated
	public static final int DATA_DELETED    = ENTRY_DELETED;

    /** @deprecated use the new modes */
    @Deprecated
	public static final String DOCUMENT     = "Document";

    /** @deprecated use the new CONTAINER_ modes */
    @Deprecated
	public static final String DIRECTORY    = "WebFolder";

    /** The held mode. */
    private int mode;

    /** The held message. */
    private Object message;

    /** Create a new DataChangeEvent with given mode and message */
    public DataChangeEvent (Object aSource, int aMode, 
                            Object aMessage) {
        super (aSource);

        this.mode    = aMode;
        this.message = aMessage;
    }

    /**
     * Returns the debug output of this instance.
     *
     * @return    The string representation of this instance.
     */
    @Override
	public String toString () {
        return (this.getClass ().getName () + " [" +
                        "mode: " + this.mode +
                        ", message: " + this.message + 
                        ", source: " + this.getSource () +
                        ']');
    }

    /**
     * Information about the kind of change.
     *
     * @return    The mode of change.
     */
    public int getChangeMode () {
        return (this.mode);
    }

    /**
     * Check if the mode indicated a Container (in contrast to an Entry).
     *
     * @return  true when the cange indicates a Change to a container.
     */
    public boolean isContainer () {
        return 0 != (mode & CONTAINER);
    }

    /**
     * Message of change.
     *
     * @return    The message of change.
     */
    public Object getMessage () {
        return (this.message);
    }
}
