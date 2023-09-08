/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.POIUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.util.Utils;

/**
 * Helper class for easier access to the excel value map.
 * This class holds a map cell identifier --> cell value.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class ExcelValueMapHelper {

    /** Stores the excel value map. */
    private Map valueMap;

    /** Stores the data sheet cell name prefix. */
    private String prefix;


    /**
	 * Creates a new instance of this class with an empty sheet name.
	 * 
	 * @param aValueMap
	 *        the value map as returned by {@link ExcelAccess#getValues(InputStream)};
	 */
    public ExcelValueMapHelper(Map aValueMap) {
        this(aValueMap, StringServices.EMPTY_STRING);
    }

    /**
	 * Creates a new instance of this class.
	 * 
	 * @param aValueMap
	 *        the value map as returned by {@link ExcelAccess#getValues(InputStream)};
	 * @param aSheetName
	 *        the excel data sheet to use
	 */
    public ExcelValueMapHelper(Map aValueMap, String aSheetName) {
        valueMap = aValueMap;
        setDataSheet(aSheetName);
    }


    /**
	 * Returns the value map as returned by {@link ExcelAccess#getValues(InputStream)};
	 * 
	 * @return the value map
	 */
    public Map getValues() {
        return valueMap;
    }

    /**
	 * Gets the data sheet prefix, consisting of <code>[dataSheetName] + '!'</code>.
	 *
	 * @return the data sheet prefix; may be empty but not <code>null</code>
	 */
    public String getDataSheetPrefix() {
        return prefix;
    }

    /**
     * Changes the excel data sheet to use.
     *
     * @param aSheetName
     *        the data sheet to use
     */
    public void setDataSheet(String aSheetName) {
        prefix = StringServices.isEmpty(aSheetName) ? StringServices.EMPTY_STRING : aSheetName + '!';
    }


    /**
     * Gets the value of the cell defined by row and column index.
     * For example [1, 0] gets cell "A2", [2, 4] gets cell "E3".
     *
     * @param aRow
     *        the row index of the desired cell
     * @param aColumn
     *        the column index of the desired cell
     * @return the value of the given cell; may be <code>null</code> if the given cell
     *         has no value
     */
    public Object get(int aRow, int aColumn) {
        return valueMap.get(prefix + POIUtil.convertToCellName(aRow, aColumn));
    }

    /**
     * Gets the value of the given cell.
     *
     * @param aCellName
     *        the cell to get the value from
     * @return the value of the given cell; may be <code>null</code> if the given cell
     *         has no value
     */
    public Object get(String aCellName) {
        return valueMap.get(prefix + aCellName);
    }

    /**
     * Searches for the given value in the excel value map and gets a list of excel
     * cell names (String) which contains the given value.
     *
     * @param aValue
     *        the value to search
     * @return a list of excel cells which contains the given value; may be empty
     */
    public List findValue(Object aValue) {
        List theResult = new ArrayList();
        Iterator it = valueMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry theEntry = (Map.Entry)it.next();
            Object theValue = theEntry.getValue();
            if (Utils.equals(aValue, theValue)) {
                String theKey = (String)theEntry.getKey();
                if (theKey.startsWith(prefix)) {
                    theResult.add(theKey.substring(prefix.length()));
                }
            }
        }
        return theResult;
    }

}
