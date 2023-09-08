/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Map;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Filter boolean attributes
 *
 * @author    <a href="mailto:fma@top-logic.com"></a>
 */
public class BooleanAttributeFilter extends SimpleAttributeFilter{

	/** The value that must be matched. */
	private Boolean value;

    /**
     * Comment for <code>SORT_ORDER</code>
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(4);

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public BooleanAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * CTor that sets the value that must be matched.
	 *
	 * @param anAttribute	the attribute
	 * @param aValue		the value to be matched
	 * @param doNegate		the negation flag
	 * @param isRelevant 	the relevant flag
	 */
	public BooleanAttributeFilter(TLStructuredTypePart anAttribute, Boolean aValue, boolean doNegate, boolean isRelevant,
			String anAccessPath) {
		super (anAttribute, doNegate, isRelevant, anAccessPath);
		this.value = aValue;
	}

	/**
	 * @see com.top_logic.element.meta.query.SimpleAttributeFilter#checkValue(java.lang.Object)
	 */
	@Override
	protected boolean checkValue(Object aValue) {
		if (value == null) {
			return aValue == null;
		}
		boolean attributeNotSetCond = (aValue==null && value.equals(Boolean.FALSE));
		return attributeNotSetCond || value.equals(aValue);
	}

	/**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return SORT_ORDER;
	}

	/**
	 * Add search value
	 *
     * @see com.top_logic.element.meta.query.SimpleAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        theMap.put(KEY_VALUE1, this.value);

        return theMap;
    }

    /**
     * Get and set search value
     *
     * @see com.top_logic.element.meta.query.SimpleAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        Object storedValue = aValueMap.get(KEY_VALUE1);
        if (storedValue != null && !(storedValue instanceof Boolean)) {
            throw new IllegalArgumentException ("Value for key " + KEY_VALUE1 + " must be a Boolean!");
        }
        this.value = (Boolean) storedValue;
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), value, null, domain);
    }

}
