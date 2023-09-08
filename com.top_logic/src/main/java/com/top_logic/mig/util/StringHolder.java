/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.util;

import java.io.Serializable;

/**
 * A simple string holder, that, in contrast to the CORBA-implementation, is
 * serializable.
 * 
 * @author Navid Vahdat
 *
 */
public class StringHolder implements Serializable {
    
    /** The String value held by this StringHolder  object.*/
    public String value;

    /**
     * Constructs a new StringHolder object with its value field initialized to
     * null.
     */
    public StringHolder() {
    }
    
    /**
     * Constructs a new StringHolder object with its value field initialized to
     * the given String.
     * @param initial the String with which to initialize the value field of the
     * newly-created StringHolder object
     */
    public StringHolder(String initial){
        value = initial;
    }

    /** Return some String reasonable fror Debugging */    
    @Override
	public String toString () {
        return "StringHolder: " + value;
    }

              
    /**
     * Returns the value.
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
