/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Date;
import java.util.Map;

import com.top_logic.util.error.TopLogicException;

/**
 * Utility class for (Excel) importers to map keys to expected types.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractImporter {

    /** 
     * Return a date from the given map accessed by the given key.
     * 
     * @param    someValues    The map of values to get the date from.
     * @param    aKey          The accessing key for getting a date from the given map.
     * @return   The requested date or <code>null</code>.
     * @throws   TopLogicException    If found value is no date.
     */
    protected Date getDate(Map someValues, String aKey) {
        Object theObject = someValues.get(aKey);

        if (theObject instanceof Date) {
            return (Date) theObject;
        }
        else if (theObject == null) {
            return null;
        }
        else {
            throw new TopLogicException(AbstractImporter.class, "get.date", new Object[] {aKey});
        }
    }

    /** 
     * Return a string from the given map accessed by the given key.
     * 
     * @param    someValues    The map of values to get the string from.
     * @param    aKey          The accessing key for getting a string from the given map.
     * @return   The requested string or <code>null</code>.
     * @throws   TopLogicException    If found value is no string.
     */
    protected String getString(Map someValues, String aKey) {
        Object theObject = someValues.get(aKey);

        if (theObject instanceof String) {
            return (String) theObject;
        }
        else if (theObject == null) {
            return null;
        }
        else {
            throw new TopLogicException(AbstractImporter.class, "get.string", new Object[] {aKey});
        }
    }
    
    /** 
     * Return a number from the given map accessed by the given key.
     * 
     * @param    someValues    The map of values to get the number from.
     * @param    aKey          The accessing key for getting a number from the given map.
     * @return   The requested number or <code>null</code>.
     * @throws   TopLogicException    If found value is no number.
     */
    protected Number getNumber(Map someValues, String aKey) {
        Object theObject = someValues.get(aKey);
        
        if (theObject instanceof Number) {
            return (Number) theObject;
        }
        else if (theObject == null) {
            return null;
        }
        else {
            throw new TopLogicException(AbstractImporter.class, "get.number", new Object[] {aKey});
        }
    }

    /** 
     * Return <code>true</code> for a value which is "ja".
     * 
     * @param    someValues    The map of values to get the information from.
     * @param    aKey          The accessing key for getting a boolean from the given map.
     * @return   <code>true</code> if value is "ja".
     * @throws   TopLogicException    If found value is no string.
     */
    protected boolean getBoolean(Map someValues, String aKey) {
        Object theObject = someValues.get(aKey);
        
        if (theObject instanceof String) {
            return "ja".equalsIgnoreCase((String) theObject);
        }
        else if (theObject == null) {
            return false;
        }
        else {
            throw new TopLogicException(AbstractImporter.class, "get.boolean", new Object[] {aKey});
        }
    }

}

