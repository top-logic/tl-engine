/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import java.util.List;


/**
 * Factory for {@link Condition}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Conditions {

	/**
	 * A {@link True} condition.
	 */
	public static True ifTrue() {
		return True.INSTANCE;
	}

	/**
	 * A {@link False} condition.
	 */
	public static False ifFalse() {
		return False.INSTANCE;
	}

	/**
	 * An {@link Equals} condition.
	 * 
	 * @param test
	 *        See {@link Equals#test()}.
	 * @param expected
	 *        See {@link Equals#expected()}.
	 */
	public static Equals ifEquals(ItemFunction test, Object expected) {
		return new Equals(test, expected);
	}

	/**
	 * And combination of conditions.
	 */
	public static Condition and(List<Condition> conditions) {
		return and(conditions.toArray(new Condition[conditions.size()]));
	}

	/**
	 * And combination of conditions.
	 */
	public static Condition and(Condition... conditions) {
		Condition[] minimizedConditions = minimizeAnd(conditions);
		switch (minimizedConditions.length) {
			case 0:
				return ifTrue();
			case 1:
				return minimizedConditions[0];
			default:
				return new And(minimizedConditions);
		}
	}

	/**
	 * Or combination of conditions.
	 */
	public static Condition or(List<Condition> conditions) {
		return or(conditions.toArray(new Condition[conditions.size()]));
	}

	/**
	 * Or combination of conditions.
	 */
	public static Condition or(Condition... conditions) {
		Condition[] minimizedConditions = minimizeOr(conditions);
		switch (minimizedConditions.length) {
			case 0:
				return ifFalse();
			case 1:
				return minimizedConditions[0];
			default:
				return new Or(minimizedConditions);
		}
	}

	private static Condition[] minimizeAnd(Condition... conditions) {
		return minimize(ifFalse(), ifTrue(), conditions);
	}

	private static Condition[] minimizeOr(Condition... conditions) {
		return minimize(ifTrue(), ifFalse(), conditions);
	}

	private static Condition[] minimize(Condition nullInstance, Condition identityInstance, Condition... conditions) {
		int copyLength = 0;
		Condition[] copy = null;
		for (int n = 0, cnt = conditions.length; n < cnt; n++) {
			Condition condition = conditions[n];

			if (condition.equals(nullInstance)) {
				return new Condition[] { condition };
			}
			else if (condition.equals(identityInstance)) {
				if (copy == null) {
					copy = new Condition[cnt];
					System.arraycopy(conditions, 0, copy, 0, n);
					copyLength = n;
				}
			}
			else {
				if (copy != null) {
					copy[copyLength++] = condition;
				}
			}
		}

		if (copy == null) {
			return conditions;
		} else {
			Condition[] trimmedConditions = new Condition[copyLength];
			System.arraycopy(copy, 0, trimmedConditions, 0, copyLength);
			return trimmedConditions;
		}
	}

}
