/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.ExceptionUtil;

/**
 * For invoking default methods of interfaces.
 * <p>
 * Every interface has to define the following constant {@value #LOOKUP}:
 * </p>
 * <code>
 * // Necessary for the {@link DefaultMethodInvoker}. <br/>
 * Lookup LOOKUP = MethodHandles.lookup();
 * </code>
 * <p>
 * This is necessary to get the necessary access rights to call default methods. In Java 8 only the
 * interface itself is allowed to call its default methods. The
 * {@link java.lang.invoke.MethodHandles.Lookup} encapsulates these access rights. Making it
 * publicly accessible allows the {@link DefaultMethodInvoker} to get it via reflection and use it
 * to call default methods.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DefaultMethodInvoker {

	/**
	 * The name of the public static field storing the {@link java.lang.invoke.MethodHandles.Lookup}
	 * in the target interface.
	 * <p>
	 * <em>Important:</em>The interface needs to define a constant with this name as explained in
	 * the class comment: {@link DefaultMethodInvoker}
	 * </p>
	 */
	public static final String LOOKUP = "LOOKUP";

	/** The singleton {@link DefaultMethodInvoker} instance. */
	public static final DefaultMethodInvoker INSTANCE = new DefaultMethodInvoker();

	private final ConcurrentHashMap<Method, MethodHandle> _methodHandles = new ConcurrentHashMap<>();

	/** Reduce the visibility to ensure there is only one {@link DefaultMethodInvoker}. */
	private DefaultMethodInvoker() {
		// Ditto.
	}

	/**
	 * Calls the given default method on the given object.
	 * <p>
	 * <em>Important:</em>The interface needs to define the {@link #LOOKUP} constant as explained in
	 * the class comment: {@link DefaultMethodInvoker}
	 * </p>
	 */
	public Object invoke(Object self, Method method, Object... arguments) throws Throwable {
		return getMethodHandle(method).bindTo(self).invokeWithArguments(arguments);
	}

	private MethodHandle getMethodHandle(Method method) {
		return _methodHandles.computeIfAbsent(method, DefaultMethodInvoker::getMethodHandleUncached);
	}

	private static MethodHandle getMethodHandleUncached(Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		try {
			return getLookup(declaringClass).unreflectSpecial(method, declaringClass);
		} catch (IllegalAccessException exception) {
			throw errorIllegalAccess(exception, method, declaringClass);
		}
	}

	private static Lookup getLookup(Class<?> targetType) {
		try {
			return (Lookup) targetType.getField(LOOKUP).get(null);
		} catch (NoSuchFieldException exception) {
			throw errorNoSuchField(exception, targetType);
		} catch (IllegalAccessException exception) {
			throw errorIllegalAccess(exception, targetType);
		} catch (SecurityException exception) {
			throw errorSecurity(exception, targetType);
		}
	}

	private static RuntimeException errorNoSuchField(NoSuchFieldException exception, Class<?> targetType) {
		String message = "Cannot call default methods in type " + targetType.getCanonicalName()
			+ " as it doesn't define the necessary '" + LOOKUP + "' constant.";
		throw ExceptionUtil.rethrow(message, exception);
	}

	private static RuntimeException errorIllegalAccess(IllegalAccessException exception, Class<?> targetType) {
		String message = "Cannot call default methods in type " + targetType.getCanonicalName()
			+ " as the necessary '" + LOOKUP + "' constant is not visible.";
		throw ExceptionUtil.rethrow(message, exception);
	}

	private static RuntimeException errorSecurity(SecurityException exception, Class<?> targetType) {
		String message = "Cannot call default methods in type " + targetType.getCanonicalName()
			+ " as a SecurityManager forbids access to the necessary '" + LOOKUP + "' constant.";
		throw ExceptionUtil.rethrow(message, exception);
	}

	private static RuntimeException errorIllegalAccess(IllegalAccessException exception, Method method,
			Class<?> declaringClass) {
		String message = "Cannot call default method " + declaringClass.getCanonicalName() + "." + method.getName()
			+ " as the access was denied, either because of visibility and security reasons,"
			+ " or because the method has no varargs parameter but was called as if, or because the '" + LOOKUP
			+ "' constant is only defined in super class.";
		throw ExceptionUtil.rethrow(message, exception);
	}

}
