/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.tool.state.StatefullElement;

/**
 * Filter for {@link com.top_logic.tool.state.StatefullElement}.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class StatefulElementFilter extends BasicCollectionFilter {

	protected static final String KEY_ELT_STATE = "eltState";

	private String eltStateKey;

	public StatefulElementFilter(Map aValueMap) throws IllegalArgumentException {
		super(aValueMap);
	}

	public StatefulElementFilter(boolean doNegate, boolean isRelevant, String aStateKey) {
		super(doNegate, isRelevant);

		this.eltStateKey = aStateKey;
	}

	/** 
	 * @see com.top_logic.element.meta.query.BasicCollectionFilter#getSortOrder()
	 */
	@Override
	public Integer getSortOrder() {
		return Integer.valueOf(100); // We don't have a good performance
	}

	/** 
	 * @see com.top_logic.element.meta.query.CollectionFilter#filter(java.util.Collection)
	 */
	@Override
	public Collection filter(Collection aCollection)
			throws NoSuchAttributeException, AttributeException {
    	if (!this.isRelevant()) {
    		return aCollection;
    	}
    	
        List result = new ArrayList(aCollection.size());
        Iterator iter = aCollection.iterator();
        while(iter.hasNext()){
            StatefullElement theElement = (StatefullElement) iter.next();
            if (theElement.getState().getKey().equals(this.getSearchStateKey()) != this.getNegate()) {
                result.add(theElement);
            }
        }
        return result;  
	}
	
	/** 
	 * @see com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware#setUpFromValueMap(java.util.Map)
	 */
	@Override
	public void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
		super.setUpFromValueMap(aValueMap);
		
		this.setSearchStateKey((String) aValueMap.get(KEY_ELT_STATE));
		
		if (this.getSearchStateKey() == null) {
			throw new IllegalArgumentException("ElementState key must be specified");
		}
	}
	
	/** 
	 * @see com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware#getValueMap()
	 */
	@Override
	public Map<String, Object> getValueMap() {
		Map theMap = super.getValueMap();
		
		theMap.put(KEY_ELT_STATE, this.getSearchStateKey());
		
		return theMap;
	}
	
	/** 
	 * Set the current search value, i.e. state key
	 * 
	 * @param aKey the state key search value
	 */
	protected void setSearchStateKey(String aKey) {
		this.eltStateKey = aKey;
	}
	
	/** 
	 * Get the current search value, i.e. state key
	 * 
	 * @return the state key search value
	 */
	protected String getSearchStateKey() {
		return this.eltStateKey;
	}

}
