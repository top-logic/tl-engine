/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.reporting.flex.chart.config.util.KeyCompare;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class Partition implements Comparable<Partition> {

	private final List<Object> _objects;

	private final Comparable<?> _key;

	private final Partition _parent;

	private Map<Object, Object> _map;

	/**
	 * Creates a new {@link Partition}
	 * 
	 * @param parent
	 *        the parent-partition, only null in case of root-partition
	 * @param key
	 *        see {@link #getKey()}
	 * @param objects
	 *        see {@link #getObjects()}
	 */
	public Partition(Partition parent, Comparable<?> key, List<? extends Object> objects) {
		_parent = parent;
		_key = key == null ? StringServices.EMPTY_STRING : key;
		_objects = new ArrayList<>(objects);
	}

	/**
	 * the parent-partition, only null in case of root-partition
	 */
	public Partition getParent() {
		return _parent;
	}

	/**
	 * the objects in this partition
	 */
	public List<Object> getObjects() {
		return _objects;
	}

	/**
	 * the key of this partition; must not be <code>null</code>
	 */
	@SuppressWarnings("rawtypes")
	public Comparable getKey() {
		return _key;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Partition o) {
		return KeyCompare.INSTANCE.compare(getKey(), o.getKey());
	}

	/**
	 * Same as {@link TypedAnnotatable#set(Property, Object)}
	 */
	public <T> void put(Property<T> key, T value) {
		if (_map == null) {
			_map = new HashMap<>();
		}
		_map.put(key, value);
	}

	/**
	 * Shortcut to annotate an object based on its class. This allows convenient typed getter (see
	 * {@link #get(Class)})
	 */
	public void put(Object value) {
		if (_map == null) {
			_map = new HashMap<>();
		}
		_map.put(value.getClass(), value);
	}

	/**
	 * Same as {@link TypedAnnotatable#get(Property)}
	 */
	public <T> T get(Property<T> key) {
		if (_map == null) {
			return key.getDefaultValue(null);
		}
		Object storedValue = _map.get(key);
		if (storedValue != null || _map.containsKey(key)) {
			@SuppressWarnings("unchecked")
			T typeSafeValue = (T) storedValue;
			return typeSafeValue;
		}
		return key.getDefaultValue(null);
	}

	/**
	 * the annotated object of the given class or null if no such object exists.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> key) {
		if (_map == null) {
			return null;
		}
		return (T) _map.get(key);
	}

}