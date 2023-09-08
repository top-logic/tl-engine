/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import java.util.Arrays;
import java.util.Comparator;

import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Compares two DataObjects according a list of given Attributes.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DOAttributeComparator implements Comparator<DataObject> {

    /** Constant indicating ascending order */
    public static final boolean ASCENDING   = true;

    /** Constant indicating descending order */
    public static final boolean DESCENDING  = false;

     /** The DatatObjects are compared according to these attributes. */
    protected String [] attributes;

    /** The DatatObjects are compared ascending / Descending according to these values */
    protected boolean [] ascendings;

    /** 
     * Create a Comparator using an Array of Attribute names.
     *
     * @param attrNames array of attribute-names to consider in the given order.
     * @param ascending use ASCENDING or DESCENDING oder per entry.
     */
	public DOAttributeComparator(String attrNames[], boolean ascending[]) {
        attributes = attrNames;
        ascendings = ascending;
    }

    /** 
     * Create a Comparator using a single Attribute name.
     *
     * @param attrName  the name of the attribute to be compared.
     * @param ascending use ASCENDING or DESCENDING.
     */
	public DOAttributeComparator(String attrName, boolean ascending) {
        attributes = new String[]  { attrName };
        ascendings = new boolean[] { ascending };
    }

    /** 
     * Create a Comparator using a two Attribute names.
     *
     * @param attr1  the name of the attribute to be compared first.
     * @param attr2  the name of the attribute to be compared second.
     * @param ascending use ASCENDING or DESCENDING.
     */
    public DOAttributeComparator(String attr1, String attr2
			, boolean ascending) {
        this(new String[]  { attr1      , attr2 },
			new boolean[] { ascending, ascending });
    }

    /** 
     * Create a stable, ASCENDING sorting comparator using a single attribute name.
     */
    public DOAttributeComparator(String attrName) {
		this(attrName, ASCENDING);
    }

    /** 
     * Create a stable, ASCENDING sorting comparator using two attribute names.
     */
    public DOAttributeComparator(String attr1, String attr2) {
        this (attr1, attr2, ASCENDING);
    }

    /** 
     * Create a stable, ASCENDING sorting comparator using an array of attribute names.
     */
    public DOAttributeComparator(String... attrNames) {
    	this.attributes = attrNames;
        int l = attributes.length;
        this.ascendings = new boolean[l];
        Arrays.fill(ascendings, ASCENDING);
    }

    /**
     * some reasonable String for debugging.
     */
    @Override
	public String toString() {
        return this.getClass().toString() 
            + ":" + StringServices.toString(attributes);
    }
    
    /** 
     * null resistant compare method as helper for compare().
     *<p>
     *  Any null-Object is considered <em>greater</em> than any
     *  other object. This mtehod ist static so it can be reused
     *  by other Comparators.
     *</p> 
     * <pre> Short table for the result codes
     *  val1 &lt;  val2     -
     *  val1 == val2     0
     *  val1 &gt;  val2     +
     *</pre>
     * @return a negative integer, zero, or a positive integer 
     *         as the first argument is less than, equal to, or greater than the second.
     */
    public static final int compareNullValues(Object val1, Object val2) {
        if (val1 == null) {
            if (val2 != null) {
                return 1;   // null > vale
            }
            else
                return 0;   // null == null;
        }
        // val1 != null
        if (val2 == null) {
            return -1;   // vale < null
        }
        // val1 != null && val2 != null
        if (val1 instanceof Comparable<?> &&
            val2 instanceof Comparable<?>) {
            
			@SuppressWarnings("unchecked")
			Comparable<Object> c1 = (Comparable<Object>) val1;
			@SuppressWarnings("unchecked")
			Comparable<Object> c2 = (Comparable<Object>) val2;
            return c1.compareTo(c2);
        }
        return 0;   // now idea how to compare these ....
    }

    /** 
     * null resistant compare method as helper for compare().
     *<p>
     *  Any null-Object is considered <em>greater</em> than any
     *  other obejct.
     *</p> 
     * <pre> Short table for the result codes
     *  val1 &lt;  val2     -
     *  val1 == val2     0
     *  val1 &gt;  val2     +
     *</pre>
     * @return a negative integer, zero, or a positive integer 
     *         as the first argument is less than, equal to, or greater than the second.
     */
    protected int compareValues(Object val1, Object val2) {
        return compareNullValues(val1, val2);
    }

    /** 
     * Compare two DataObjects according to the given attributes.
     *<p>
     * Note: this comparator imposes orderings that are 
     * inconsistent with DataObject.equals
     *</p>
     * @return a negative integer, zero, or a positive integer 
     *         as the first argument is less than, equal to, or greater than the second.
     */
    @Override
	public int compare(DataObject o1, DataObject o2) {
        
        if (o1 != null && o2 != null) {
			DataObject do1 = o1;
			DataObject do2 = o2;
            int len = attributes.length;
            for (int i=0; i < len; i++) {
                String attribute = attributes[i];
                Object val1 = null;
                Object val2 = null;

                try  { val1 = do1.getAttributeValue(attribute); }
                catch (NoSuchAttributeException ignored)  { /* ignored */ }

                try  { val2 = do2.getAttributeValue(attribute); }
                catch (NoSuchAttributeException ignored)  { /* ignored */ }
                
                int result = compareValues(val1, val2);
                if (result != 0) {
                    return ascendings[i] ? result : -result;  // done we are ....
                }
            }
            return 0;   // all equal, well
        }
        // else
        return 0;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ascendings);
		result = prime * result + Arrays.hashCode(attributes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DOAttributeComparator other = (DOAttributeComparator) obj;
		if (!Arrays.equals(ascendings, other.ascendings))
			return false;
		if (!Arrays.equals(attributes, other.attributes))
			return false;
		return true;
	}

}
