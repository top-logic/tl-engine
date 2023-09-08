/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Indexed;

/**
 * {@link MethodImplementation} for a {@link Indexed} annotated getter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class IndexedCollectionGetter implements MethodImplementation {

	private final PropertyDescriptor _collectionProperty;

	private final PropertyDescriptor _keyProperty;

	public IndexedCollectionGetter(PropertyDescriptor collectionProperty, PropertyDescriptor keyProperty) {
		_collectionProperty = collectionProperty;
		_keyProperty = keyProperty;
	}

	@Override
	public Object invoke(ReflectiveConfigItem impl, Method method, Object[] args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object collection = impl.value(_collectionProperty);
		ConfigurationAccess configAccess = _collectionProperty.getConfigurationAccess();
		for (Object value : (Collection<?>) collection) {
			ConfigurationItem valueConfig = configAccess.getConfig(value);
			Object keyValue = valueConfig.value(_keyProperty);
			if (StringServices.equals(keyValue, args[0])) {
				return value;
			}
		}
		return null;
	}

}
