/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Super class for filters on SimpleMetaAttributes.
 *
 * @author    <a href="mailto:fma@top-logic.com"></a>
 */
public abstract class SimpleAttributeFilter extends MetaAttributeFilter implements Filter<Wrapper> {

    /** key for value Map to store a value. */
    public static final String KEY_VALUE1 = "val1";

    /** key for value Map to store a value. */
    public static final String KEY_VALUE2 = "val2";

	/**
	 * CTor with an attribute
	 *
	 * @param anAttribute the attribute for filtering
	 * @param doNegate the negation flag
	 * @param isRelevant the relevant flag
	 */
	public SimpleAttributeFilter(TLStructuredTypePart anAttribute, boolean doNegate, boolean isRelevant, String anAccessPath) {
		super(anAttribute, doNegate, isRelevant, anAccessPath);
	}

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public SimpleAttributeFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aList) throws NoSuchAttributeException, AttributeException {
    	if (!this.isRelevant()) {
    		return aList;
    	}

		List result = new ArrayList();
		Iterator iter   = aList.iterator();
		boolean  negate = this.getNegate();
		while(iter.hasNext()){
            Wrapper theElement    = (Wrapper)iter.next();
            Wrapper theBaseObject = this.getBaseObject(theElement);

			if(accept(theBaseObject) != negate){
				result.add(theElement);
			}
		}
		return result;
	}

	/**
	 * Checks if the given attributed matches the condition of the filter
	 *
	 * @param anAttr
	 *        - the object
	 * @return true if the object matches the filter
	 * @throws AttributeException
	 *         if there is a failures getting the attribute value
	 * @throws NoSuchAttributeException
	 *         if the attribute isn't one of anElement's
	 */
	@Override
	public boolean accept(Wrapper anAttr) {
		try {
			Object theValue = AttributeOperations.getAttributeValue(anAttr, this.getAttribute());
			return this.checkValue(theValue);
		} catch (NoSuchAttributeException e) {
			Logger.error("Not an attribute of " + anAttr, e, this);
			return false;
		} catch (AttributeException e) {
			Logger.error("Failed to get value from " + anAttr, e, this);
			return false;
		}
	}
	
	/**
	 * Check if the given value matches this filter
	 *
	 * @param aValue	the value
	 * @return true if the value matches this filter
	 */
	protected abstract boolean checkValue(Object aValue);
}
