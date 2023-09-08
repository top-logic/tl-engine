/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Map} that reports each add and remove operation to internal
 * {@link #beforeAdd(Object, Object)}, {@link #afterAdd(Object, Object)} and
 * {@link #afterRemove(Object, Object)} hook.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObservableMap<K, V> extends HashMap<K, V> {

	/**
	 * Creates a {@link ObservableMap}.
	 */
	public ObservableMap() {
		super();
	}
	
	@Override
	public V put(K key, V newValue) {
		beforeAdd(key, newValue);
		V oldValue = super.put(key, newValue);
		afterRemove(key, oldValue);
		afterAdd(key, newValue);
		return oldValue;
	}
	
	@Override
	public V remove(Object key) {
		V oldValue = super.remove(key);
		afterRemove(key, oldValue);
		return oldValue;
	}

	/**
	 * Hook that is informed before each add operation.
	 * 
	 * @param key
	 *        The key being added.
	 * @param newValue
	 *        The value being added.
	 */
	protected void beforeAdd(K key, V newValue) {
		// Hook for sub classes.
	}
	
	/**
	 * Hook that is informed after each add operation.
	 * 
	 * @param key
	 *        The added key.
	 * @param newValue
	 *        The added value.
	 */
	protected void afterAdd(K key, V newValue) {
		// Hook for sub classes.
	}

	/**
	 * Hook that is informed after each remove operation.
	 * 
	 * @param key
	 *        The removed key.
	 * @param oldValue
	 *        The removed value.
	 */
	protected void afterRemove(Object key, V oldValue) {
		// Hook for sub classes.
	}

}