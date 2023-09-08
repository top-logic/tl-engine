/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.filt;

import java.util.Comparator;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;

/**
 * This Comparator compares two Dataobjects by their Identifiers.
 *  
 * @author   <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class DOIdComparator implements Comparator {

	/** Constant indicating ascending order */
	public static final boolean ASCENDING  = true;

	/** Constant indicating descending order */
	public static final boolean DESCENDING = false;

	/** The DatatObjects are compared according to this ascending. */
	private boolean ascending; 

	/** 
	 * Create a Comparator.
	 *
	 * @param isAscending use ASCENDING or DESCENDING.
	 */
	public DOIdComparator(boolean isAscending) {
		this.ascending 	= isAscending;
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
	public int compare(Object o1, Object o2)  {
		
		if (o1 instanceof DataObject &&
			o2 instanceof DataObject) {
            
			DataObject do1 = (DataObject) o1;
			DataObject do2 = (DataObject) o2;
			
			TLID val1 = do1.getIdentifier();
			TLID val2 = do2.getIdentifier();
						              
			int result = ((Comparable) val1).compareTo(val2);
			if (result != 0) {
				result = this.ascending ? result : -result;
			}
			return result;   // all equal, well
		}
		return 0;
	}

	/** 
     * Test equality for Comparators. 
	 */
	@Override
	public boolean equals(Object obj)  {
    	if (obj == this) {
    		return true;
    	}
		if (obj instanceof DOIdComparator) {
			DOIdComparator other = (DOIdComparator) obj;
			
			return this.ascending == other.ascending;
		}
		return false;
	}
    
    /** 
     * Overriden to correspond with equals.
     */
    @Override
	public int hashCode() {
        return this.ascending ? 0x7654321 : 0x1234567;
    }
}
