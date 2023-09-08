/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Abstract {@link TypedAnnotatable} that cares for escaping <code>null</code> value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTypedAnnotatable implements TypedAnnotatable {

	@Override
	public <T> T get(Property<T> property) {
		return property.externalize(this, getStorageValue(property));
	}

	/**
	 * Returns the value stored for the given {@link TypedAnnotatable.Property}
	 * 
	 * @param property
	 *        The property to get stored value for.
	 * 
	 * @return The value formerly stored via
	 *         {@link #putStorageValue(TypedAnnotatable.Property, Object)}
	 */
	protected abstract Object getStorageValue(Property<?> property);

	@Override
	public <T> T set(Property<T> property, T value) {
		return property.externalize(this, putStorageValue(property, property.internalize(value)));
	}

	/**
	 * Stores the value for the given {@link TypedAnnotatable.Property}
	 * 
	 * @param property
	 *        The property to store value for.
	 * @param storageValue
	 *        The new storage value for the property.
	 * @return The value formerly stored for the given property, or <code>null</code> if none was
	 *         stored.
	 */
	protected abstract Object putStorageValue(Property<?> property, Object storageValue);

	@Override
	public <T> T setIfAbsent(Property<T> property, T value) {
		Object storedValue = putStorageValueIfAbsent(property, property.internalize(value));
		if (storedValue == null) {
			// Nothing was stored before.
			return null;
		}
		return property.externalize(this, storedValue);
	}

	/**
	 * Stores the value for the given {@link TypedAnnotatable.Property}, if it is not set yet.
	 * 
	 * @param property
	 *        The property to store value for.
	 * @param storageValue
	 *        The new storage value for the property.
	 * @return The value stored for the given property if there is one, or <code>null</code> if none
	 *         was stored before.
	 */
	protected abstract Object putStorageValueIfAbsent(Property<?> property, Object storageValue);

	@Override
	public <T> T reset(Property<T> property) {
		return property.externalize(this, removeStoredValue(property));
	}

	/**
	 * Removes the value formerly stored for the given {@link TypedAnnotatable.Property}
	 * 
	 * @param property
	 *        The property to remove value for.
	 * @return The formerly stored value, or <code>null</code> if none was stored before.
	 */
	protected abstract Object removeStoredValue(Property<?> property);

}

