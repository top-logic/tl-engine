/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;

/** 
 * Simple container for the information needed to identify embedded data objects
 * in the database.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Link {

    /** The type of the data object. */
    private String type;

    /** The ID of the data object. */
	private TLID id;

    /**
     * Constructor for Link.
     * 
     * @param    aString    The string containing the ID and the type.
     */
    public Link(String aString) {
        super();

        int thePos = aString.indexOf(' ');

        this.type = aString.substring(0, thePos);
		this.id = IdentifierUtil.fromExternalForm(aString.substring(thePos + 1));
    }

    /**
     * Constructor for Link.
     * 
     * @param    aType    The type of the object.
     * @param    anID     The name of the object.
     */
	public Link(String aType, TLID anID) {
        super();

        this.type = aType;
        this.id   = anID;
    }

    /**
     * Constructor for Link.
     * 
     * @param    anObject    The object to be represented.
     */
    public Link(DataObject anObject) {
        this(anObject.tTable().getName(), anObject.getIdentifier());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + '['
                           + "type: " + this.type
                           + ", id: " + this.id
                           + ']';
    }

    /**
     * Return a storable string representation of this instance.
     * 
     * @return    The string representation.
     */
    public String getDisplay() {
		return (this.type + ' ' + IdentifierUtil.toExternalForm(this.id));
    }

    /**
     * Returns the id.
     * 
     * @return    The held ID.
     */
	public TLID getID() {
        return (this.id);
    }

    /**
     * Returns the type.
     * 
     * @return    The held type.
     */
    public String getType() {
        return (this.type);
    }
}
