/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.List;
import java.util.Map;

import com.top_logic.model.TLStructuredTypePart;

/**
 * Filters based on a range (start, end) of numbers.
 *
 * @author    <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 */
public class NumberAttributeFilter extends MinMaxFilter {

    /**
     * Comment for <code>SORT_ORDER</code>
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(6);

	/**
	 * CTor with relevant params
	 *
	 * @param anAttribute	the Attribute
	 * @param aStartEndList	the range list with 2 elements (start, end).
	 * 						At least one of the elements must not be <code>null</code>.
	 * @param doNegate		if true retain the elements that do not match the range
	 * @param isRelevant 	the relevant flag
	 */
	public NumberAttributeFilter(TLStructuredTypePart anAttribute, List aStartEndList, boolean doNegate, boolean isRelevant, String anAccessPath){
		super(anAttribute, aStartEndList, doNegate, isRelevant, anAccessPath);
	}

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public NumberAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}


	/**
     * Returns if the given Date matches to the pattern of this filter.
     *
     * @param     aValue    The value to be checked, may be <code>null</code>.
     * @return    <code>true</code>, if the give value is contained in the interval.
	 */
	@Override
	protected boolean checkValue(Object aValue) {
		if (aValue != null && !(aValue instanceof Number)) {
			return false;
		}

		Number endValue = getEndNumber();
		Number startValue = getStartNumber();
		if(endValue==null && startValue == null){
			return aValue == null;
		}
		if(aValue==null){ // no date given, but a boundary set: return false
			return false;
		}
		double  theValue = ((Number) aValue).doubleValue();
		boolean after = true;
		if(endValue !=null){
			after= endValue.doubleValue()>= theValue;
		}

		boolean before = true;
		if(after && startValue !=null){
			before = startValue.doubleValue() <= theValue;
		}
		return after && before;
	}

	/**
	 * Get the start of the search range
	 *
	 * @return the start of the search range. May be <code>null</code>.
	 */
	private Number getStartNumber(){
		return (Number) getStartValue();
	}

	/**
	 * Get the end of the search range
	 *
	 * @return the end of the search range. May be <code>null</code>.
	 */
	private Number getEndNumber(){
		return (Number)getEndValue();
	}

	/**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return SORT_ORDER;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return 	"NumberAttributeFilter ["+getStartNumber()+" - "+getEndNumber() +"]";
	}

	/**
     * @see com.top_logic.element.meta.query.MinMaxFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        try {
            // Just to check types....
            this.getStartNumber();
            this.getEndNumber();
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Error in setup: " + ex);
        }

    }

}
