/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * Custom function that can be invoked in a generic way without reflection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericFunction<R> implements IGenericFunction<R> {

	/**
	 * The number of arguments that must be passed to {@link #invoke(Object...)}.
	 * 
	 * @return The number of expected arguments. Never negative. If {@link #hasVarArgs()} is true,
	 *         this is the minimum number of arguments.
	 */
	public abstract int getArgumentCount();

	/**
	 * <ul>
	 * <li>'true': This function takes a variable number of arguments, but needs at least
	 * {@link #getArgumentCount()}.</li>
	 * <li>'false': This function takes exactly {@link #getArgumentCount()} arguments.</li>
	 * </ul>
	 */
	public abstract boolean hasVarArgs();

	/**
	 * Access to a generic argument.
	 * 
	 * @param index
	 *        The argument index.
	 * @param args
	 *        The generic argument array.
	 * @return The value cast to the requested type.
	 */
	@SuppressWarnings("unchecked")
	protected final <T> T arg(int index, Object[] args) {
		return (T) args[index];
	}

}
