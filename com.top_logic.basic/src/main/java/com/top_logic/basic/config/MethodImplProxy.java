/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Proxy for {@link MethodImplementation}.
 * <p>
 * Has to be initialized with {@link #init(MethodImplementation)}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class MethodImplProxy implements MethodImplementation {

	private MethodImplementation _impl;

	/**
	 * Initializes this {@link MethodImplProxy}.
	 * <p>
	 * The proxy cannot be used before this method is called. <br/>
	 * Is allowed to be called only once.
	 * </p>
	 * 
	 * @param impl
	 *        Is not allowed to be <code>null</code>.
	 */
	void init(MethodImplementation impl) {
		if (_impl != null) {
			throw new IllegalStateException("Proxy is already initialized.");
		}
		if (impl == null) {
			throw new NullPointerException(MethodImplementation.class.getSimpleName() + " is not allowed to be null.");
		}
		_impl = impl;
	}

	@Override
	public Object invoke(ReflectiveConfigItem impl, Method method, Object[] args) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		if (_impl == null) {
			throw new IllegalStateException("Proxy is not yet initialized.");
		}
		return _impl.invoke(impl, method, args);
	}

}
