/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class ComplexAttributeFilter extends SimpleAttributeFilter {

    /**
     * Comment for <code>SORT_ORDER</code>
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(4);

    /** Prefix for the value attributes. */
    protected static final String VALUE_PREFIX = "_value_";


    /** The value that must be matched. */
    private Collection values;

    /**
     * CTor with an attribute
     *
     * @param anAttribute the attribute for filtering
     * @param aValue the search value
     * @param doNegate the negation flag
     * @param isRelevant the relevant flag
     */
    public ComplexAttributeFilter(TLStructuredTypePart anAttribute, Collection aValue, boolean doNegate, boolean isRelevant, String anAccessPath) {
        super(anAttribute, doNegate, isRelevant, anAccessPath);

        this.values = aValue;
    }

    /**
     * CTor that gets all values from a Map
     *
     * @param aValueMap the Map with all values. Must not be <code>null</code>.
     * @throws IllegalArgumentException if the map is <code>null</code>
     * or some of its values do not match the filter's constraints.
     */
    public ComplexAttributeFilter(Map aValueMap) throws IllegalArgumentException {
        super(aValueMap);
    }
    /**
     * @see com.top_logic.element.meta.query.SimpleAttributeFilter#checkValue(java.lang.Object)
     */
    @Override
	protected boolean checkValue(Object aValue) {
        if (values == null) {
            return aValue == null;
        }

        if (aValue == null) {
            return false;
        }

        Iterator iter = values.iterator();
        while (iter.hasNext()) {
            Object elem = iter.next();
            if (elem != null && elem.equals(aValue)){
                return true;
            }
        }
        return false;
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), values, null, domain);
    }

    /**
     * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
     */
    @Override
	public Integer getSortOrder() {
        return SORT_ORDER;
    }

    /**
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        this.values = new ArrayList();
        Iterator theMapKeys = aValueMap.keySet().iterator();
        while (theMapKeys.hasNext()) {
            String theKey = (String) theMapKeys.next();
            if (theKey.startsWith(VALUE_PREFIX)) {
				Object theValue = getStorageMapping().getBusinessObject(aValueMap.get(theKey));
                values.add(theValue);
            }
        }
    }

    /**
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        if (values != null){
            int counter = 0;
            Iterator valueIt = values.iterator();
            while (valueIt.hasNext()) {
                Object currentValue = valueIt.next();
				Object theValue = getStorageMapping().getStorageObject(currentValue);
                theMap.put(VALUE_PREFIX + counter, theValue);
				counter++;
            }

        }
        return theMap;
    }

	private StorageMapping<?> getStorageMapping() {
		return ((TLPrimitive) getAttribute().getType()).getStorageMapping();
	}

}

