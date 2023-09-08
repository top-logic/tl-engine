/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.col.ObservableMapProxy;
import com.top_logic.basic.tools.NameBuilder;

/**
 * Observable {@link Map} implementation that sends notifications through its owning
 * {@link AbstractConfigItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class PropertyMap<K, V> extends ObservableMapProxy<K, V> {

	private static final int NO_INDEX = -1;

	private LinkedHashMap<K, V> _impl;

	private final AbstractConfigItem _container;

	private final PropertyDescriptorImpl _property;

	/**
	 * Creates a {@link PropertyMap}.
	 * 
	 * @param container
	 *        The container that handles notification.
	 * @param property
	 *        The property for which this {@link PropertyMap} holds elements.
	 * @param initialValue
	 *        The initial contents.
	 */
	public PropertyMap(AbstractConfigItem container, PropertyDescriptorImpl property,
			Map<? extends K, ? extends V> initialValue) {
		_impl = new LinkedHashMap<>(initialValue);
		_container = container;
		_property = property;
	}

	@Override
	protected Map<K, V> impl() {
		return _impl;
	}

	@Override
	public V put(K key, V value) {
		checkLegalValue(Collections.singletonMap(key, value));
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (m.isEmpty()) {
			return;
		}

		checkLegalValue(m);
		super.putAll(m);
	}

	@Override
	protected void afterPut(K key, V value) {
		receiver().add(_property, NO_INDEX, value);
	}

	@Override
	protected void afterRemove(Object key, V oldValue) {
		receiver().remove(_property, NO_INDEX, oldValue);
	}

	Change receiver() {
		return _container.onChange(_property);
	}

	private void checkLegalValue(Map<? extends K, ? extends V> m) {
		PropertyDescriptorImpl.checkCorrectMapValues(_property, m);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("property", _property)
			.add("map", _impl)
			.build();
	}

}
