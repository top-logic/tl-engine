/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

/**
 * Reduce operation that combines an arbitrary number of values to a single value.
 * 
 * @param <R>
 *        the result type of the combination.
 * @param <A>
 *        the argument type of the combination.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Reduce<R, A> {

	/**
	 * The result, if the number of values to combine is empty.
	 * 
	 * <p>
	 * If the types R and A are the same, it is required that {@link #combine(Object, Object)} with
	 * the {@link #neutral()} element returns the other argument.
	 * </p>
	 */
	R neutral();

	/**
	 * Combines the given argument with the result of the combination of all previous inputs.
	 * 
	 * @param current
	 *        The current result of the combination so far.
	 * @param arg
	 *        The next value to combine.
	 * @return The new combine result.
	 */
	R combine(R current, A arg);

	/**
	 * Whether combination can stop early, since the current result won't change any further.
	 * 
	 * @param current
	 *        the current result of the combination.
	 * @return Whether combination can be aborted.
	 */
	boolean isZero(R current);

}
