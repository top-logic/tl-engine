/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Base class for {@link Accessor} implementations that only operate on certain row types.
 * 
 * <p>
 * For all rows of type other that the supported type, <code>null</code> is returned.
 * </p>
 * 
 * @param <T>
 *        The row type this {@link Accessor} operates on.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypeSafeAccessor<T> extends ReadOnlyAccessor<Object> implements Mapping<Object, Object> {

	@Override
	public Object getValue(Object object, String property) {
		if (getType().isInstance(object)) {
			@SuppressWarnings("unchecked")
			T typedObject = (T) object;
			return getValueTyped(typedObject, property);
		}
		return getDefaultValue(object, property);
	}

	@Override
	public Object map(Object input) {
		return getValue(input, null);
	}

	/**
	 * Retrieve a default value, if the given object does not match {@link #getType()}.
	 * 
	 * @param property
	 *        See {@link #getValue(Object, String)}.
	 * @param object
	 *        See {@link #getValue(Object, String)}
	 *
	 * @return The default value to finally retrieve.
	 */
	protected Object getDefaultValue(Object object, String property) {
		return null;
	}

	/**
	 * The static type, this accessor operates on.
	 */
	protected abstract Class<? extends T> getType();

	/**
	 * Accesses a property on a row of a certain type.
	 * 
	 * @param object
	 *        See {@link #getValue(Object, String)}.
	 * @param property
	 *        See {@link #getValue(Object, String)}.
	 * @return The column value.
	 */
	protected abstract Object getValueTyped(T object, String property);

}