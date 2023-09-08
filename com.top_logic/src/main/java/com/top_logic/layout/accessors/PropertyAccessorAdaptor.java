/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.PropertyAccessor;

/**
 * An adaptor for using a {@link PropertyAccessor} where an {@link Accessor} is required.
 * <p>
 * All properties are dispatched to the given {@link PropertyAccessor}.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class PropertyAccessorAdaptor<T> implements Accessor<T> {

	private final PropertyAccessor<T> _propertyAccessor;

	/**
	 * Creates a {@link PropertyAccessorAdaptor}.
	 * 
	 * @param propertyAccessor
	 *        Is not allowed to be null.
	 */
	public PropertyAccessorAdaptor(PropertyAccessor<T> propertyAccessor) {
		_propertyAccessor = requireNonNull(propertyAccessor);
	}

	@Override
	public Object getValue(T object, String property) {
		return _propertyAccessor.getValue(object);
	}

	@Override
	public void setValue(T object, String property, Object value) {
		_propertyAccessor.setValue(object, value);
	}

}
