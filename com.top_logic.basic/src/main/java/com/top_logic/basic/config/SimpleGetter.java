/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Method;

/**
 * Getter for a simple (non-indexed) {@link PropertyDescriptor property}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
final class SimpleGetter implements MethodImplementation {
	public static final MethodImplementation INSTANCE = new SimpleGetter();

	private SimpleGetter() {
		// Singleton constructor.
	}
	
	@Override
	public Object invoke(ReflectiveConfigItem impl, Method method, Object[] args) {
		PropertyDescriptor property = impl.descriptorImpl().getPropertiesByMethod().get(method);

		return impl.value(property);
	}
}