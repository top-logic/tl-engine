/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericValueMapImpl extends LinkedHashMap implements GenericValueMap {

    private final String type;
    
	private final TLID identifier;
    
    private String[] attrs;

    /**
	 * Creates a {@link GenericValueMapImpl}.
	 */
	public GenericValueMapImpl(String aType, String anIdentifier, int aSize) {
		this(aType, StringID.valueOf(anIdentifier), aSize);
	}

	/**
	 * Creates a {@link GenericValueMapImpl}.
	 */
	public GenericValueMapImpl(String aType, TLID anIdentifier, int aSize) {
        super(aSize);
        this.type       = aType;
		this.identifier = anIdentifier;
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValueMap#getAttributeNames()
     */
    @Override
	public synchronized String[] getAttributeNames() {
        if (attrs == null) {
            Set theKeys = this.keySet();
            attrs = new String[theKeys.size()];
            int i=0;
            for (Iterator theIt = theKeys.iterator(); theIt.hasNext();) {
                attrs[i] = (String) theIt.next();
                i++;
            }
        }
        return attrs;
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValueMap#getAttributeValue(java.lang.String)
     */
    @Override
	public Object getAttributeValue(String anAttribute) throws NoSuchAttributeException {
        if (this.containsKey(anAttribute)) {
            return this.get(anAttribute);
        }
        throw new NoSuchAttributeException("No attribute '" + anAttribute + "'");
    }

    @Override
	public TLID getIdentifier() {
        return this.identifier;
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValueMap#getType()
     */
    @Override
	public String getType() {
        return this.type;
    }

    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericValueMap#setAttributeValue(java.lang.String, java.lang.Object)
     */
    @Override
	public void setAttributeValue(String anAttribute, Object aValue) {
        if (! this.containsKey(anAttribute)) {
            this.attrs = null;
        }
        this.put(anAttribute, aValue);
    }

    @Override
	public boolean hasAttribute(String anAttribute) {
        return this.containsKey(anAttribute);
    }
    
}

