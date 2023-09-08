/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyFactory;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Checks objects by applying a filter on the values of
 * a WrapperSetMetaAttribute. An object is retained if
 * at least one of the values matches the inner filter.
 *
 * @author    <a href="mailto:kbu@top-logic.com"></a>
 */
public class WrapperSetFilter extends MetaAttributeFilter {

    /** key for value Map to store a value. */
    protected static final String KEY_INNER = "inner";

	/** The internal filter. */
	protected CollectionFilter innerFilter;

    /**
     * Comment for <code>SORT_ORDER</code>
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(100);

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public WrapperSetFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * Constructor with an internal Filter to filter the Annotations
	 *
	 * @param anAttribute	the Attribute
	 * @param aFilter 		the internal Filter
	 * @param doNegate		if true retain the elements not matching the condition
	 * @param isRelevant 	the relevant flag
	 */
	public WrapperSetFilter(TLStructuredTypePart anAttribute, CollectionFilter aFilter, boolean doNegate, boolean isRelevant, String anAccessPath) {
		super(anAttribute, doNegate, isRelevant, anAccessPath);
		this.innerFilter = aFilter;
	}

	/**
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aList) throws NoSuchAttributeException, AttributeException {
    	if (!this.isRelevant()) {
    		return aList;
    	}

		List theResult = new ArrayList();

		// Iterate over the elements
		Iterator theElements = aList.iterator();
		while (theElements.hasNext()) {
			Wrapper theElement = this.getBaseObject((Wrapper)theElements.next());

			// Iterate over the inner elements
			Iterator theInnerElts = ((Collection) AttributeOperations.getAttributeValue(theElement, this.getAttribute())).iterator();
			boolean found = false;
			while (!found && theInnerElts.hasNext()) {
				Wrapper theInnerElt = (Wrapper) theInnerElts.next();
				List theInnerEltList = new ArrayList();
				theInnerEltList.add (theInnerElt);	// just one element...

				if (!this.innerFilter.filter(theInnerEltList).isEmpty()) {
					found = true;
				}
			}

			if (found != this.getNegate()) {
				theResult.add (theElement);
			}
		 }


		 return theResult;
	}

	/**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		if (this.innerFilter instanceof BasicCollectionFilter) {
			// TODO KBU does this make sense???
			return ((BasicCollectionFilter) this.innerFilter).getSortOrder();
		}
		else {
			return SORT_ORDER;
		}
	}

	/**
	 * Add inner filter values map (prefixed).
	 *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        // Add the keys of the inner filter with a prefix
        Map theInnerMap = this.innerFilter.getValueMap();
        if (theInnerMap != null) {
            Iterator theInnerKeys = theInnerMap.keySet().iterator();
            while (theInnerKeys.hasNext()) {
                String theInnerKey = (String) theInnerKeys.next();
                Object theInnerVal = theInnerMap.get(theInnerKey);

                theMap.put(KEY_INNER + KEY_SEPARATOR + theInnerKey, theInnerVal);
            }
        }

        return theMap;
    }

    /**
     * Get keys for inner filter and set it up.
     *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        // Add the keys of the inner filter with a prefix
        Map theInnerMap = new HashMap();
        Iterator theKeys = aValueMap.keySet().iterator();
        while (theKeys.hasNext()) {
            String theKey = (String) theKeys.next();

            if (theKey.startsWith(KEY_INNER + KEY_SEPARATOR)) {
                String theInnerKey = theKey.substring(0, (KEY_INNER + KEY_SEPARATOR).length());
                Object theInnerVal = aValueMap.get(theKey);
                theInnerMap.put(KEY_INNER + KEY_SEPARATOR + theInnerKey, theInnerVal);
            }
        }

        this.innerFilter = (CollectionFilter) MapBasedPersistancyFactory.getInstance().getObject(theInnerMap);

        if (this.innerFilter == null) {
            throw new IllegalArgumentException("Inner filter must not be null!");
        }
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
        if (this.innerFilter instanceof MetaAttributeFilter) {
			return ((MetaAttributeFilter) this.innerFilter).getSearchValuesAsUpdate(updateContainer, type, null);
        }
        else {
            return null;
        }
    }

}
