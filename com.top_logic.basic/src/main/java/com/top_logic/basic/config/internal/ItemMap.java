/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.ObservableMapProxy;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.tools.NameBuilder;

/**
 * Observable {@link Map} implementation that sends notifications through its owning
 * {@link ItemChangeHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class ItemMap<K, V> extends ObservableMapProxy<K, V> {

	private static final int NO_INDEX = -1;

	private LinkedHashMap<K, V> _impl;

	private final ItemChangeHandler _handler;

	private final PropertyDescriptor _property;

	private boolean _modified;

	/**
	 * Creates a {@link ItemMap}.
	 * 
	 * @param handler
	 *        The container that handles notification.
	 * @param property
	 *        The property for which this {@link ItemMap} holds elements.
	 */
	public ItemMap(ItemChangeHandler handler, PropertyDescriptor property) {
		_impl = new LinkedHashMap<>();
		_handler = handler;
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
		receiver().__notifyAdd(_property, NO_INDEX, value);
		markModified();
	}

	@Override
	protected void afterRemove(Object key, V oldValue) {
		receiver().__notifyRemove(_property, NO_INDEX, oldValue);
		markModified();
	}

	/**
	 * Whether this map has been modified after construction (either creating the containing
	 * configuration item or reading it from XML).
	 */
	public boolean isModified() {
		return _modified;
	}

	@FrameworkInternal
	private void markModified() {
		setModified(true);
	}

	/**
	 * Sets the modified state of this map.
	 * 
	 * @see #isModified()
	 */
	@FrameworkInternal
	public void setModified(boolean value) {
		_modified = value;
	}

	/**
	 * Updates this map to the new state.
	 *
	 * @param value
	 *        The new contents.
	 * @param isSet
	 *        The new {@link #isModified()} marker.
	 */
	@FrameworkInternal
	public void set(Map<? extends K, ? extends V> value, boolean isSet) {
		clear();
		putAll(value);
		setModified(isSet);
	}

	/**
	 * Resets the modification marker.
	 * 
	 * @see #isModified()
	 */
	@FrameworkInternal
	public void markUnmodified() {
		_modified = false;
	}

	ItemChangeHandler receiver() {
		return _handler;
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
