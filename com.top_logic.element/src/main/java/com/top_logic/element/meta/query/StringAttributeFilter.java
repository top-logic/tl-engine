/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Search for a given String as sub string of the
 * value of a given StringMetaAttribute.
 *
 * @author    <a href="mailto:fma@top-logic.com"></a>
 */
public class StringAttributeFilter extends SimpleAttributeFilter {

	private String pattern;

	/** pattern.toLowerCase() */
    private String patternLC;

    /**
     * Comment for <code>SORT_ORDER</code>
     */
    protected static final Integer SORT_ORDER = Integer.valueOf(7);

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public StringAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * Creates a new filter for the given attribute with the given pattern
	 *
	 * @param anAttribute	the attribute
	 * @param aPattern		the search pattern (full-text)
	 * @param doNegate		if true retain the elements not matching the pattern.
	 * @param isRelevant 	the relevant flag
	 */
	public StringAttributeFilter(TLStructuredTypePart anAttribute, String aPattern, boolean doNegate, boolean isRelevant,
			String anAccessPath) {
		super(anAttribute, doNegate, isRelevant, anAccessPath);
		pattern   = aPattern;
		patternLC = (pattern != null) ? aPattern.toLowerCase() : null;
		
	}

	/**
     * returns true if the given String matches to the pattern of this filter
     *
     * @param value	the value to check
     * @return true if the value matches the pattern
	 */
	@Override
	protected boolean checkValue(Object value) {
		if (StringServices.isEmpty(patternLC)) {
			return StringServices.isEmpty(value);
		} else if (value instanceof String) {
			return ((String)value).toLowerCase().indexOf(patternLC) >= 0;
		}

		return false;
	}

	/**
	 * @see BasicCollectionFilter#getSortOrder()
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

        theMap.put(KEY_VALUE1, this.pattern);

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

        Object theValue = aValueMap.get(KEY_VALUE1);
        if (theValue != null && !(theValue instanceof String)) {
            throw new IllegalArgumentException ("Value for key " + KEY_VALUE1 + " must be a String!");	// TODO KBU check with empty filters
        }
        pattern = (String) theValue;
        patternLC = (pattern != null) ? pattern.toLowerCase() : null;
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), pattern, null, domain);
    }
    
    /**
     * Allow access to pattern for special (DPM) cases.
     */
    public String getPattern() {
        return pattern;
    }


}

