/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import java.util.Map;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * A data object used to hold user data.
 * This data object is used for external users. 
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class UserDataObject extends ExampleDataObject {

    /** The meta object name for the data object. */
    public static final String META_OBJECT_NAME = "person";

    /** The unique ID of this instance. */
	protected TLID identifier;

    /** Create the Object with the given Map.
     *
     * @param someMap as an Example of the Implementation
     */
    public UserDataObject(Map someMap) {
        super (someMap);

		this.identifier = StringID.createRandomID();
    }
    
    /* (non-Javadoc)
     * @see com.top_logic.mig.dataobjects.simple.ExampleDataObject#getMetaObjectName()
     */
    @Override
	public String getMetaObjectName() {
        return META_OBJECT_NAME;
    }

    /* (non-Javadoc)
     * @see com.top_logic.mig.dataobjects.DataObject#getIdentifier()
     */
    @Override
	public TLID getIdentifier() {
        return this.identifier;
    }

    /* (non-Javadoc)
     * @see com.top_logic.mig.dataobjects.DataObject#setIdentifier(java.lang.String)
     */
    @Override
	public void setIdentifier(TLID anIdentifier) {
        this.identifier = anIdentifier;
    }

}
