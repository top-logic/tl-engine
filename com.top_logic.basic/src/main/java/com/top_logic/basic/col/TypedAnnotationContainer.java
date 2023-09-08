/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.annotation.Inspectable;


/**
 * Default implementation of {@link TypedAnnotatable}.
 * 
 * <p>
 * Note: This {@link TypedAnnotatable} Must not be used multi threaded. See also
 * {@link ConcurrentTypedAnnotationContainer}.
 * </p>
 * 
 * @see LazyTypedAnnotatable
 * @see ConcurrentTypedAnnotationContainer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypedAnnotationContainer extends AbstractTypedAnnotatable {

	@Inspectable
	private final Map<Property<?>, Object> _values;

	/**
	 * Creates a new {@link TypedAnnotationContainer}.
	 */
	public TypedAnnotationContainer() {
		_values = createStorage();
	}

	/**
	 * Creates the storage {@link Map} for this {@link TypedAnnotatable}.
	 */
	protected Map<Property<?>, Object> createStorage() {
		return new HashMap<>();
	}

	@Override
	protected Object getStorageValue(Property<?> property) {
		return _values.get(property);
	}

	@Override
	protected Object putStorageValue(Property<?> property, Object storageValue) {
		return _values.put(property, storageValue);
	}

	@Override
	protected Object putStorageValueIfAbsent(Property<?> property, Object storageValue) {
		return _values.putIfAbsent(property, storageValue);
	}

	@Override
	public boolean isSet(Property<?> property) {
		return _values.containsKey(property);
	}

	@Override
	protected Object removeStoredValue(Property<?> property) {
		return _values.remove(property);
	}

}
