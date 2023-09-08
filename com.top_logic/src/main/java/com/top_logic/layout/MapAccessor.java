/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Map;

/**
 * The MapAccessor is an accessor which delegates requests to a map. So the given object
 * parameters of the methods of this class have to be maps.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class MapAccessor implements Accessor {

    /** Instance of this MapAccessor without an inner accessor. */
    public static final MapAccessor INSTANCE = new MapAccessor();

    /** Special Attribute to resolve using {@link #innerAccessor}. */
    public static final String THIS_ATTR = "_this";

    /** Optional accessor to access the object in {@link #THIS_ATTR}. */
    protected Accessor innerAccessor;


    /**
     * Create a new MapAccessor using an (optional) innerAccessor. If using an optional
     * Accessor, the map must contain an entry {@link #THIS_ATTR} --> Object to use by the
     * innerAccessor.
     *
     * @param aInnerAccessor
     *            the accessor to use on the object mapped with the {@link #THIS_ATTR} key,
     *            when the key was not found in the map
     */
    public MapAccessor(Accessor aInnerAccessor) {
        this.innerAccessor = aInnerAccessor;
    }

    /**
     * Create a new MapAccessor without innerAcessor.
     */
    public MapAccessor() {
        this(null);
    }

    /**
	 * Assumes the given object <code>aObject</code> is a {@link Map}. If not, <code>null</code>
	 * will be returned.
	 * <p>
	 * The returned value is the entry in the map <code>aObject</code> under the key
	 * <code>aProperty</code>. If <code>aProperty</code> is not a key in <code>aObject</code> but
	 * there is a mapping for the key {@link #THIS_ATTR} in <code>aObject</code>, then
	 * the inner accessor is asked for the value of <code>aProperty</code> of the
	 * <code>aObject</code>-value of the key {@link #THIS_ATTR}.
	 * </p>
	 */
    @Override
	public Object getValue(Object aObject, String aProperty) {
        if (aObject instanceof Map) {
            Map theMap = (Map) aObject;
            Object theValue = theMap.get(aProperty);
            if (theValue == null && innerAccessor != null && !theMap.containsKey(aProperty)) {
                Object thisValue = theMap.get(THIS_ATTR);
                if (thisValue != null) {
                    theValue = innerAccessor.getValue(thisValue, aProperty);
                }
            }
            return theValue;
        }
        return null;
    }

    @Override
	public void setValue(Object aObject, String aProperty, Object aValue) {
        if (aObject instanceof Map) {
            ((Map)aObject).put(aProperty, aValue);
        }
    }

}
