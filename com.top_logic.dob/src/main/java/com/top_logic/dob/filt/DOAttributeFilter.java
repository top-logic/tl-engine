/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import com.top_logic.basic.col.Filter;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;


/** 
 * Abstarct Superclass to help filtering Objects by AttributeValues.
 * <p>
 *  In case the DataObject do not posee the attribute the order should still
 *  be stable (by using hasCodes()),
 *</p>
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class DOAttributeFilter implements Filter {

    /** The attribute used to filter the dataObjects */
    String  attribute;

    /** 
     * Create a Comparator using a single Attribute name.
     */
    public DOAttributeFilter(String attrName) {
       attribute = attrName;
    }

    /** override to do the actual filtering.
     *
     * @param  theValue of the Attribute to check.
     * @return true in case the Value matches the your desires.
     */
	@Override
	public abstract boolean test(Object theValue);

    /** 
     * Extract the Attributes Value and call test(Object).
     *
     * null-values will be considered. Missing attributes
     * will always in a flase test.
     *
     * @param  theObject DataObject where the value will be extracted from.
     * @return true in case the Object matches the your desires.
     */
    @Override
	public boolean accept(Object theObject) {
        try  { 
            DataObject theDo = (DataObject) theObject;
            Object     value = theDo.getAttributeValue(attribute);
            return     test(value); 
        }
        catch (NoSuchAttributeException ignored)  
        {  
            return false;
        }
    }
    
    /** Create a Filter that will accept when the value of an Attribute is null. */
    public static  DOAttributeFilter createNullFilter(String attrName) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return null == theValue;
            }
        };
    }

        /** Create a Filter fo an Equals Attribute condition */
    public static  DOAttributeFilter createEqualsFilter(String attrName, final Object testObject) {
        if (testObject == null) {
            return createNullFilter(attrName);
        }
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return  testObject.equals(theValue);
            }
        };
    }

    /** Create a Filter fo a Less Than Attribute condition */
    public static  DOAttributeFilter createLTFilter(String attrName, final Comparable testObject) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return theValue != null && testObject.compareTo(theValue) > 0;
            }
        };
    }

    /** Create a Filter fo a Less ore Equals Attribute condition */
    public static  DOAttributeFilter createLEFilter(String attrName, final Comparable testObject) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return theValue != null && testObject.compareTo(theValue) >= 0;
            }
        };
    }

    /** Create a Filter fo a Greater Attribute condition */
    public static  DOAttributeFilter createGTFilter(String attrName, final Comparable testObject) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  
            {
                return theValue != null && testObject.compareTo(theValue) < 0;
            }
        };
    }

    /** Create a Filter fo a Greater Or Equals Attribute condition */
    public static  DOAttributeFilter createGEFilter(String attrName, final Comparable testObject) {
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return theValue != null && testObject.compareTo(theValue) <= 0;
            }
        };
    }

    /** Create a Filter fo Contains Attribute condition */
    public static  DOAttributeFilter createContainsFilter(String attrName, final String testString) {
        if (testString == null) {
            throw new NullPointerException("testString");
        }
        return new DOAttributeFilter(attrName) {
            @Override
			public boolean test(Object theValue)  {
                return theValue != null && theValue.toString().indexOf(testString) >= 0;
            }
        };
    }
}
