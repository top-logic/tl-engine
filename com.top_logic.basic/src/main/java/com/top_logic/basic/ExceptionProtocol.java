/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The class {@link ExceptionProtocol} generates {@link RuntimeException}s from
 * a given type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExceptionProtocol extends AbstractProtocol {

	private static final Class<?>[] CONSTR_SIGNATURE = new Class<?>[] { String.class, Throwable.class };

	private final Constructor<? extends RuntimeException> defaultConstr;
	private final Constructor<? extends RuntimeException> enhancedConstr;

	/**
	 * Creates a {@link ExceptionProtocol} with given
	 * exception class.
	 * 
	 * @param exClass
	 *        the implementation class of the thrown {@link RuntimeException}
	 *        
	 * @throws IllegalArgumentException
	 *         iff the given class has not both, the default constructor and the
	 *         one with signature {@link #CONSTR_SIGNATURE}
	 */
	public ExceptionProtocol(Class<? extends RuntimeException> exClass) throws IllegalArgumentException {
		try {
			defaultConstr = exClass.getConstructor();
			enhancedConstr = exClass.getConstructor(CONSTR_SIGNATURE);
		} catch (SecurityException ex) {
			throw new IllegalArgumentException(ex);
		} catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Creates a {@link ExceptionProtocol} with given
	 * exception class.
	 * 
	 * @param exClass
	 *        the implementation class of the thrown {@link RuntimeException}
	 * @param chain
	 *        see {@link AbstractProtocol#AbstractProtocol(Protocol)}
	 *        
	 * @throws IllegalArgumentException
	 *         iff the given class has not both, the default constructor and the
	 *         one with signature {@link #CONSTR_SIGNATURE}
	 */
	public ExceptionProtocol(Class<? extends RuntimeException> exClass, Protocol chain) {
		super(chain);
		try {
			defaultConstr = exClass.getConstructor();
			enhancedConstr = exClass.getConstructor(CONSTR_SIGNATURE);
		} catch (SecurityException ex) {
			throw new IllegalArgumentException(ex);
		} catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	protected void reportError(String message, Throwable cause) {
		throw getException(message, cause);
	}

	private RuntimeException getException(String message, Throwable cause) {
		RuntimeException exception;
		try {
			exception = enhancedConstr.newInstance(message, cause);
		} catch (IllegalArgumentException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			exception = new RuntimeException(message, cause);
		} catch (InstantiationException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			exception = new RuntimeException(message, cause);
		} catch (IllegalAccessException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			exception = new RuntimeException(message, cause);
		} catch (InvocationTargetException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			exception = new RuntimeException(message, cause);
		}
		return exception;
	}

	@Override
	protected RuntimeException reportFatal(String message, Throwable ex) {
		throw getException(message, ex);
	}

	@Override
	protected RuntimeException createAbort() {
		RuntimeException result;
		try {
			result = defaultConstr.newInstance();
		} catch (IllegalArgumentException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			result = new RuntimeException();
		} catch (InstantiationException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			result = new RuntimeException();
		} catch (IllegalAccessException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			result = new RuntimeException();
		} catch (InvocationTargetException ex) {
			Logger.info("Unable to instantiate Exception", ex, ExceptionProtocol.class);
			result = new RuntimeException();
		}
		return result;
	}

}
