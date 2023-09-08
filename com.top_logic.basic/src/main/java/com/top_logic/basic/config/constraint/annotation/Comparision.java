/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.annotation;

import com.top_logic.basic.UnreachableAssertion;

/**
 * Type of comparison.
 * 
 * @see Bound#comparison()
 * @see ComparisonDependency#comparison()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Comparision {

	/**
	 * The base value must be greater than the reference value.
	 */
	GREATER,

	/**
	 * The base value must be smaller than the reference value.
	 */
	SMALLER,

	/**
	 * The base value must be greater than or equal to the reference value.
	 */
	GREATER_OR_EQUAL,

	/**
	 * The base value must be smaller than or equal to the reference value.
	 */
	SMALLER_OR_EQUAL;

	/**
	 * Tests whether this {@link Comparision} holds for the given values.
	 * 
	 * @param baseValue
	 *        The base value to compare.
	 * @param referenceValue
	 *        The reference value to compare with.
	 * @return Whether the given values are in the relation described by this {@link Comparision}.
	 */
	public boolean compare(double baseValue, double referenceValue) {
		switch (this) {
			case GREATER: {
				return baseValue > referenceValue;
			}
			case GREATER_OR_EQUAL: {
				return baseValue >= referenceValue;
			}
			case SMALLER: {
				return baseValue < referenceValue;
			}
			case SMALLER_OR_EQUAL: {
				return baseValue <= referenceValue;
			}
		}
		throw new UnreachableAssertion("No such comparison: " + this);
	}

	/**
	 * Tests whether this {@link Comparision} holds for the given values.
	 * 
	 * @param baseValue
	 *        The base value to compare.
	 * @param referenceValue
	 *        The reference value to compare with.
	 * @return Whether the given values are in the relation described by this {@link Comparision}.
	 */
	public <C extends Comparable<C>> boolean compare(C baseValue, C referenceValue) {
		int comparison = baseValue.compareTo(referenceValue);
		switch (this) {
			case GREATER: {
				return comparison > 0;
			}
			case GREATER_OR_EQUAL: {
				return comparison >= 0;
			}
			case SMALLER: {
				return comparison < 0;
			}
			case SMALLER_OR_EQUAL: {
				return comparison <= 0;
			}
		}
		throw new UnreachableAssertion("No such comparison: " + this);
	}

	/**
	 * The swapped {@link Comparision}.
	 * 
	 * <p>
	 * {@link #compare(Comparable, Comparable)} for <code>x</code> and <code>y</code> gives the same
	 * result as the swapped {@link Comparision} for <code>y</code> and <code>x</code>.
	 * </p>
	 */
	public Comparision swap() {
		switch (this) {
			case GREATER: {
				return SMALLER;
			}
			case GREATER_OR_EQUAL: {
				return SMALLER_OR_EQUAL;
			}
			case SMALLER: {
				return GREATER;
			}
			case SMALLER_OR_EQUAL: {
				return GREATER_OR_EQUAL;
			}
		}
		throw new UnreachableAssertion("No such comparison: " + this);
	}

}
