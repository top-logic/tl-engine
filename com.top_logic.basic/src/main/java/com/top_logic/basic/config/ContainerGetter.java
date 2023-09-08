/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.Method;

import com.top_logic.basic.config.annotation.Container;


/**
 * Getter for {@link Container} properties.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class ContainerGetter implements MethodImplementation {

	/** Singleton {@link ContainerGetter} instance. */
	public static final ContainerGetter INSTANCE = new ContainerGetter();

	/** Private to ensure singleton property. Use {@link #INSTANCE} instead. */
	private ContainerGetter() {
		/* Private singleton constructor */
	}

	@Override
	public ConfigurationItem invoke(ReflectiveConfigItem impl, Method method, Object[] args) {
		return impl.container();
	}

}
