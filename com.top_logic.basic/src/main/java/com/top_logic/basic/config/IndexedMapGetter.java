/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.top_logic.basic.config.annotation.Indexed;

/**
 * {@link MethodImplementation} for a {@link Indexed} annotated getter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class IndexedMapGetter implements MethodImplementation {

	private final PropertyDescriptor _collectionProperty;

	public IndexedMapGetter(PropertyDescriptor collectionProperty) {
		_collectionProperty = collectionProperty;
	}

	@Override
	public Object invoke(ReflectiveConfigItem impl, Method method, Object[] args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object collection = impl.value(_collectionProperty);
		return ((Map<?, ?>) collection).get(args[0]);
	}

}
