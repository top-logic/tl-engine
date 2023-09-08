/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.model.TLStructuredTypePart;

/**
 * Filter based of date type attributes.
 *
 * @author    <a href="mailto:fma@top-logic.com">Franka Mausa</a>
 */
public class DateAttributeFilter extends MinMaxFilter {

	private DayIntervall interval;

    /**
     * Comment for <code>SORT_ORDER</code>
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(5);

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public DateAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}


	/**
	 * CTor with all relevant params
	 *
	 * @param anAttr		the Attribute
	 * @param aStartEndList	a list of 2 elements (start-date, end-date).
	 * 						At least one of the element must not be <code>null</code>.
	 * @param doNegate		if true the elements that are not in the interval are returned
	 * @param isRelevant 	the relevant flag
	 */
	public DateAttributeFilter(TLStructuredTypePart anAttr, List aStartEndList, boolean doNegate, boolean isRelevant,
			String anAccessPath) {
		super(anAttr, aStartEndList, doNegate, isRelevant, anAccessPath);
		init();
	}

	/**
	 * Get the current interval.
	 *
	 * @return the current interval.
	 */
	protected DayIntervall getIntervall(){
		return interval;
	}

	/**
	 * Init the interval
	 */
	protected void init() {
		Date startDate = (Date) getStartValue();
		Date endDate   = (Date) getEndValue();
		interval       = new DayIntervall(startDate, endDate);
	}

	/**
     * Returns if the given Date matches to the pattern of this filter.
     *
	 * @param     aValue    The value to be checked, may be <code>null</code>.
	 * @return    <code>true</code>, if the give value is contained in the interval.
	 */
	@Override
	protected boolean checkValue(Object aValue) {
		if (aValue == null || aValue instanceof Date) {
            return (this.interval.contains((Date) aValue));
        }
        else {
			return false;
		}
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
		return "DateAttributeFilter: "+interval.getDurationString();
	}

	/**
     * @see com.top_logic.element.meta.query.MinMaxFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        try {
            this.init();
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Error in setup: " + ex);
        }
    }


}
