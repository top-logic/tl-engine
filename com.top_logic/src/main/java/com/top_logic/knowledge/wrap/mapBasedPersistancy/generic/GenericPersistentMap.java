/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.mapBasedPersistancy.generic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.knowledge.wrap.mapBasedPersistancy.BasicMapBasedPersistancyAware;

/**
 * Example implementation of {@link com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware}.
 * 
 * This implementation is based on a map and also implements the map interface.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class GenericPersistentMap extends BasicMapBasedPersistancyAware implements Map {
	
	/** The map holding the data */
	private Map map;

	/** constructor */
	public GenericPersistentMap() {
		super();
		this.map = new HashMap();
	}
	
	/** constructor */	
	public GenericPersistentMap(Map anInitial) {
		super();
		if (anInitial != null) {
			this.map = anInitial;
		} else {
			this.map = new HashMap();
		}
	}

	/**
	 * Well, we just use a given map as data container
	 * 
	 * @see com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware#setUpFromValueMap(java.util.Map)
	 */
	@Override
	public void setUpFromValueMap(Map someData) {
		if (someData != null) {
			this.map = someData;
		} else {
			this.map = new HashMap();
		}
	}

	/**
	 * Just copy the data contained in the data map.
	 * 
	 * @see com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancyAware#getValueMap()
	 */
	@Override
	public Map<String, Object> getValueMap() {
		Map theResult = super.getValueMap();
		theResult.putAll(this.map);
		return theResult;
	}

	@Override
	public int size() {
		return this.map.size();
	}
	@Override
	public void clear() {
		this.map.clear();
	}
	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}
	@Override
	public boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {
		return this.map.containsValue(value);
	}
	@Override
	public Collection values() {
		return this.map.values();
	}
	@Override
	public void putAll(Map t) {
		this.map.putAll(t);
	}
	@Override
	public Set entrySet() {
		return this.map.entrySet();
	}
	@Override
	public Set keySet() {
		return this.map.keySet();
	}
	@Override
	public Object get(Object key) {
		return this.map.get(key);
	}
	@Override
	public Object remove(Object key) {
		return this.map.remove(key);
	}
	@Override
	public Object put(Object key, Object value) {
		return this.map.put(key, value);
	}

}
