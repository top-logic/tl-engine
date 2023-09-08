/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.List;
import java.util.Map;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Filter that allows a serach range (from and to value)
 *
 * @author    <a href="mailto:fma@top-logic.com"></a>
 */
public abstract class MinMaxFilter extends SimpleAttributeFilter {

	private Object startValue;
	private Object endValue;


	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public MinMaxFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * CTor that sets the start and end of the search range.
	 * The List must not be <code>null</code> and contain
	 * exactely the two values for start and end.
	 *
	 * @param anAttr		the attribute for the search
	 * @param aStartEndList	the List (cf. above)
	 * @param doNegate		if true, negate the filter result.
	 * @param isRelevant 	the relevant flag
	 */
	public MinMaxFilter(TLStructuredTypePart anAttr, List aStartEndList, boolean doNegate, boolean isRelevant, String anAccessPath) {
		super(anAttr, doNegate, isRelevant, anAccessPath);
		if ((aStartEndList != null) && (aStartEndList.size() == 2)) {
			startValue = aStartEndList.get(0);
			endValue   = aStartEndList.get(1);
		}
	}

	/**
	 * CTor that sets the start and end of the search range.
	 *
	 * @param anAttr		the attribute for the search
	 * @param aStartValue	the start value
	 * @param anEndValue	the end value
	 * @param doNegate		if true, negate the filter result.
	 * @param isRelevant 	the relevant flag
	 */
	public MinMaxFilter(TLStructuredTypePart anAttr, Object aStartValue, Object anEndValue, boolean doNegate, boolean isRelevant, String anAccessPath) {
		super(anAttr, doNegate, isRelevant, anAccessPath);
		startValue = aStartValue;
		endValue   = anEndValue;
	}

	/**
	 * Get the start value of the search range
	 *
	 * @return the start value of the search range (may be <code>null</code>).
	 */
	protected Object getStartValue(){
		return startValue;
	}

	/**
	 * Get the start value of the search range
	 *
	 * @return the start value of the search range (may be <code>null</code>).
	 */
	protected Object getEndValue(){
		return endValue;
	}

	/**
	 * Add search range
	 *
     * @see com.top_logic.element.meta.query.SimpleAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        theMap.put(KEY_VALUE1, this.startValue);
        theMap.put(KEY_VALUE2, this.endValue);

        return theMap;
    }

    /**
     * Get and set search range
     * Note: type checks have to be done in sub classes!!
     *
     * @see com.top_logic.element.meta.query.SimpleAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        this.startValue = aValueMap.get(KEY_VALUE1);
        this.endValue   = aValueMap.get(KEY_VALUE2);
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), startValue, endValue, domain);
    }

}
