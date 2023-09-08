
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;


/** 
 * Extends DOAttributeComparator to compare attributes as Strings.
 * 
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DOStringAttributeComparator extends DOAttributeComparator {

    /** The actual Comparator used, usually a Collator */
	Comparator<? super String> collator;
           
    /** 
     * Create a Comparator using an Arry of Attribute names.
     *
     * @param ascending use ASCENDING or DESCENDING order per entry.
     */
	public DOStringAttributeComparator(String attrNames[], boolean ascending[], Locale l) {
		super(attrNames, ascending);
        collator = Collator.getInstance(l);
    }

    /** 
     * Create a Comparator using a single Attribute name and
     * sorting for the default Locale.
     */
    public DOStringAttributeComparator(String attrName) {
        super(attrName);
        collator = Collator.getInstance();
    }

    /** 
     * Create a Comparator using a single Attribute name and
     * sorting for the default Locale.
     *
     * @param attrName  the name of the string attribute to be compared.
     * @param ascending use ASCENDING or DESCENDING.
     */
	public DOStringAttributeComparator(String attrName, boolean ascending) {
		super(attrName, ascending);
        collator = Collator.getInstance();
    }

    /** 
     * Create a Comparator using a single Attribute name and
     * sorting for a given Locale.
     *
     * @param attrName  the name of the string attribute to be compared.
     * @param ascending use ASCENDING or DESCENDING.
     */
	public DOStringAttributeComparator(String attrName, boolean ascending, Locale l) {
		super(attrName, ascending);
        collator = Collator.getInstance(l);
    }

    /** 
     * Create a Comparator using a single Attribute name and
     * sorting for a given Locale.
     */
    public DOStringAttributeComparator(String attrName, Locale l) {
        super(attrName);
        collator = Collator.getInstance(l);
    }

    /** 
     * Create a Comparator using a single Attribute name and
     * sorting by a given (String) Comparator.
     *
     * @param aCollator might in fact be any Comparator not only Collator.
     */
	public DOStringAttributeComparator(String attrName, Comparator<? super String> aCollator) {
        super(attrName);
        collator = aCollator;
    }

    /** 
     * Create a Comparator using an Array of Attribute names and
     * sorting for the default Locale.
     */
    public DOStringAttributeComparator(String attrNames[]) {
        super(attrNames);
        collator = Collator.getInstance();
    }

    /** 
     * Create a Comparator using an Array of Attribute names and
     * sorting for a given Locale.
     */
    public DOStringAttributeComparator(String attrNames[], Locale l) {
        super(attrNames);
        collator = Collator.getInstance(l);
    }

    /** 
     * Create a Comparator using an Array of Attribute names and
     * sorting by a given (String) Comparator.
     *
     * @param aCollator might in fact be any Comparator not only Collator.
     */
	public DOStringAttributeComparator(String attrNames[], Comparator<? super String> aCollator) {
        super(attrNames);
        collator = aCollator;
    }

    /** 
     * Overridden to care for String values.
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
    @Override
	protected int compareValues(Object val1, Object val2) {
		return compareToString(collator, val1, val2);
	}

	/**
	 * Compares the given values using their canonical string representation.
	 */
	public static int compareToString(Comparator<? super String> collator2, Object val1, Object val2) {
		if (val1 == null) {
            if (val2 != null) {
                return 1;   // null > vale
            }
            else {
                return 0;   // null == null;
            }
        }
        // val1 != null
        if (val2 == null) {
            return -1;   // null < vale
        }
		return collator2.compare(val1.toString(),val2.toString());
	}

}
