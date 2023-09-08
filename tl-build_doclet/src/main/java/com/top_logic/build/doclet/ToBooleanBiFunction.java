/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.function.BiFunction;

/**
 * Represents a function that accepts two arguments and produces a boolean-valued result. This
 * is the {@code boolean}-producing primitive specialization for {@link BiFunction}.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #applyAsBoolean(Object, Object)}.
 *
 * @param <T>
 *        the type of the first argument to the function
 * @param <U>
 *        the type of the second argument to the function
 *
 * @see BiFunction
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface ToBooleanBiFunction<T, U> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t
	 *        the first function argument
	 * @param u
	 *        the second function argument
	 * @return the function result
	 */
	boolean applyAsBoolean(T t, U u);
}
