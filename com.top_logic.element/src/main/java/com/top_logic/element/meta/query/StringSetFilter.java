/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link CollectionFilter} that checks whether the value of a {@link #getAttribute()} contains some strings.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class StringSetFilter extends MetaAttributeFilter {


	/** the values as a list of Strings. */
    protected List searchStrings;

    /** the unparsed comma separated value list. */
    protected String searchDefinition;

    /**
     * Comment for <code>SORT_ORDER</code>
     */
	protected static final Integer SORT_ORDER = Integer.valueOf(1);

	/** key for value Map to store wrapper ids. */
    protected static final String KEY_STRINGS = "searchstrings";

	/**
	 * CTor that gets all values from a Map
	 *
	 * @param aValueMap the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or some of its values do not match the filter's constraints.
	 */
	public StringSetFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);

		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * CTor with attribute, search value list and negation flag.
	 *
	 * @param anAttribute
	 *        the attribute
	 * @param aStringList
	 *        a list containing a single entry with the comma separated search strings
	 * @param doNegate
	 *        if true negate result
	 * @param isRelevant
	 *        the relevant flag
	 * @throws IllegalArgumentException
	 *         if some attributes do not match constraints
	 */
	public StringSetFilter(TLStructuredTypePart anAttribute, List aStringList, boolean doNegate, boolean isRelevant, String anAccessPath)
			throws IllegalArgumentException {
		super(anAttribute, doNegate, isRelevant, anAccessPath);

		this.searchDefinition = (aStringList == null || aStringList.isEmpty()) ? null : (String) aStringList.get(0);
		this.searchStrings    = this.getStrings(this.searchDefinition);
	}

	/**
	 * Get the search strings from the comma separated list
	 *
	 * @param aStringList the search strings from the comma separated list
	 * @return the search strings from the comma separated list
	 */
	protected List getStrings(String aStringList) {
		return (aStringList == null) ? Collections.EMPTY_LIST : StringServices.toList(aStringList, ',');
	}

	/**
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aList) throws NoSuchAttributeException, AttributeException {
    	if (!this.isRelevant()) {
    		return aList;
    	}

		List result = new Vector();
		Iterator iter = aList.iterator();
		while(iter.hasNext()){
			Wrapper theElement = this.getBaseObject((Wrapper)iter.next());
			try {
				if(check(theElement) != this.getNegate()){
					result.add(theElement);
				}
			} catch (NoSuchAttributeException e) {
				Logger.error("Not an attribute of " + theElement, e, this);
			} catch (AttributeException e) {
				Logger.error("Failed to get value from " + theElement, e, this);
			}
		}
		return result;
	}

	/**
	 * Checks if the given attributed matches the condition of the filter
	 *
	 * @param anElement
	 *        the object
	 * @return true if the object matches the filter
	 * @throws AttributeException
	 *         if there is a failures getting the attribute value
	 * @throws NoSuchAttributeException
	 *         if the attribute isn't one of anElement's
	 */
	protected boolean check(Wrapper anElement) throws NoSuchAttributeException, AttributeException {
		if (this.searchStrings == null || this.searchStrings.isEmpty()) {
			return ((Collection) AttributeOperations.getAttributeValue(anElement, this.getAttribute())).isEmpty();
		}

		ArrayList theValue = new ArrayList(((Collection) AttributeOperations.getAttributeValue(anElement, this.getAttribute())));
		Iterator theStrings = theValue.iterator();
		while (theStrings.hasNext()) {
			String theString = (String) theStrings.next();
			if (!StringServices.isEmpty(theString)) {
				Iterator theSearchStrings = this.searchStrings.iterator();
				while (theSearchStrings.hasNext()) {
					String theSearchString = (String) theSearchStrings.next();
					if (theString.indexOf(theSearchString) >= 0) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return SORT_ORDER;
	}

	/**
	 * Add wrapper list
	 *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#getValueMap()
     */
    @Override
	public Map<String, Object> getValueMap() {
        Map theMap = super.getValueMap();

        theMap.put(KEY_STRINGS, this.searchDefinition);

        return theMap;
    }

    /**
     * Set up wrapper list
     *
     * @see com.top_logic.element.meta.query.MetaAttributeFilter#setUpFromValueMap(java.util.Map)
     */
    @Override
	public void setUpFromValueMap(Map aValueMap)
            throws IllegalArgumentException {
        super.setUpFromValueMap(aValueMap);

        Object theIDs = aValueMap.get(KEY_STRINGS);
        if ((theIDs != null) && !(theIDs instanceof String)) {
            throw new IllegalArgumentException ("Value for key " + KEY_STRINGS + " must be a String!");
        }

        this.searchDefinition = (String) theIDs;
        this.searchStrings    = this.getStrings(this.searchDefinition);
    }

    @Override
	public AttributeUpdate getSearchValuesAsUpdate(AttributeUpdateContainer updateContainer, TLStructuredType type,
			String domain) {
    	Collection theValue;
    	if (StringServices.isEmpty(this.searchDefinition)) {
    		theValue = null;
    	}
    	else {
    		theValue = Collections.singletonList(this.searchDefinition);
    	}
		return AttributeUpdateFactory.createAttributeUpdateForSearch(
			updateContainer, type, getAttribute(), theValue, null, domain);
    }


}
