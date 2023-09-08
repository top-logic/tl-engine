/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Method for getting or setting a {@link PropertyDescriptor property} value or
 * implementing a visit method in a {@link ConfigurationItem} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/ 
interface MethodImplementation {

	/**
	 * Accesses (get or set) the given property.
	 * @param impl
	 *        The generic implementation of reflective access.
	 * @param method
	 *        The originally invoked method (getter, setter, visit method).
	 * @param args
	 *        The arguments of the access method.
	 * 
	 * @return The result, the given method does expect.
	 * 
	 * 
	 * @throws IllegalAccessException
	 *         if this <code>Method</code> object enforces Java language access
	 *         control and the underlying method is inaccessible.
	 * @throws IllegalArgumentException
	 *         if the method is an instance method and the specified object
	 *         argument is not an instance of the class or interface declaring
	 *         the underlying method (or of a subclass or implementor thereof);
	 *         if the number of actual and formal parameters differ; if an
	 *         unwrapping conversion for primitive arguments fails; or if, after
	 *         possible unwrapping, a parameter value cannot be converted to the
	 *         corresponding formal parameter type by a method invocation
	 *         conversion.
	 * @throws InvocationTargetException
	 *         if the underlying method throws an exception.
	 * 
	 * @see Method#invoke(Object, Object[])
	 */
	Object invoke(ReflectiveConfigItem impl, Method method, Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
	
}