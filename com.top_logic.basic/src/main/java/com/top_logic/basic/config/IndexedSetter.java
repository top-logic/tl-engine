/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Setter for an indexed {@link PropertyDescriptor property}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
final class IndexedSetter implements MethodImplementation {
	public static final MethodImplementation INSTANCE = new IndexedSetter();

	private IndexedSetter() {
		// Singleton constructor.
	}
	
	@Override
	public Object invoke(ReflectiveConfigItem impl, Method method, Object[] args) {
		PropertyDescriptor property = impl.descriptorImpl().getPropertiesByMethod().get(method);

		int index = ((Integer) args[0]).intValue();
		
		Object newElement = args[1];
		if (newElement != null) {
			// Dynamic type check.
			Class<?> expectedElementType = property.getElementType();
			if (expectedElementType.isPrimitive()) {
				expectedElementType = PropertyDescriptorImpl.getPrimitiveWrapperClass(expectedElementType);
			}
			
			if (! expectedElementType.isInstance(newElement)) {
				throw new IllegalArgumentException("Invalid collection element, expected '" + expectedElementType.getName() + "', got '" + newElement.getClass().getName() + "'.");
			}
		}

		List result = (List) impl.value(property);

		Object nullValue;
		Class<?> elementType = property.getElementType();
		if (elementType.isPrimitive()) {
			nullValue = PropertyDescriptorImpl.getPrimitiveDefault(elementType);
		} else {
			nullValue = null;
		}
		for (int n = result.size(); n < index; n++) {
			result.add(nullValue);
		}
		
		if (index < result.size()) {
			result.set(index, newElement);
		} else {
			result.add(index, newElement);
		}

		return null;
	}

}