/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.NumberComparator;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing a comparison operation between {@link #getLeft()} and
 * {@link #getRight()}.
 * 
 * <p>
 * The result of the operation is smaller than zero, zero, or greater than zero, if the left value
 * is smaller, equal, or greater that the right value.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Compare extends BinaryOperation implements BooleanExpression {

	/**
	 * Creates a {@link Compare}.
	 * 
	 * @param left
	 *        See {@link #getLeft()}.
	 * @param right
	 *        See {@link #getRight()}.
	 */
	Compare(SearchExpression left, SearchExpression right) {
		super(left, right);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object left = getLeft().evalWith(definitions, args);
		Object right = getRight().evalWith(definitions, args);
		
		return compute(left, right);
	}

	/**
	 * Actually computes the comparison based on the given concret values.
	 */
	public final Integer compute(Object left, Object right) {
		return Integer.valueOf(Cmp.NULL_LARGEST.compare(left, right));
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitCompare(this, arg);
	}

	/**
	 * Compares two potentially <code>null</code> {@link Comparable}s in a way that
	 * <code>null</code> is the greatest value.
	 */
	public static abstract class Cmp implements Comparator<Object> {

		/**
		 * Singleton {@link Compare.Cmp} instance.
		 */
		public static final Cmp NULL_SMALLEST = new Cmp() {
			@Override
			protected int nullCompareValue() {
				return -1;
			}
		};

		/**
		 * Singleton {@link Compare.Cmp} instance.
		 */
		public static final Cmp NULL_LARGEST = new Cmp() {
			@Override
			protected int nullCompareValue() {
				return 1;
			}
		};

		/**
		 * Creates a {@link Cmp}.
		 */
		protected Cmp() {
			// Singleton constructor.
		}

		@Override
		public int compare(Object o1, Object o2) {
			return compareNull(o1, o2);
		}

		/**
		 * Comparison of two potentially <code>null</code> values.
		 */
		protected final int compareNull(Object o1, Object o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				} else {
					return nullCompareValue();
				}
			} else {
				if (o2 == null) {
					return -nullCompareValue();
				} else {
					return compareValues(o1, o2);
				}
			}
		}

		/**
		 * Comparison result of <code>null</code> and a non-<code>null</code> value.
		 */
		protected abstract int nullCompareValue();

		/**
		 * Compares two non-<code>null</code> values synthesizing compare semantics for
		 * {@link List}s.
		 *
		 * @param o1
		 *        The first compare key.
		 * @param o2
		 *        The second compare key.
		 * @return The comparison result for the compare keys.
		 */
		protected int compareValues(Object o1, Object o2) {
			if (o1 instanceof List<?>) {
				if (o2 instanceof List<?>) {
					return compareList(asList(o1), asList(o2));
				} else {
					return compareDirect(first(asList(o1)), o2);
				}
			} else {
				if (o2 instanceof List<?>) {
					return compareDirect(o1, first(asList(o2)));
				} else {
					return compareDirect(o1, o2);
				}
			}
		}

		private List<?> asList(Object key) {
			return (List<?>) key;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private static int compareDirect(Object key1, Object key2) {
			if (key1 instanceof Number && key2 instanceof Number) {
				return NumberComparator.INSTANCE.compare((Number) key1, (Number) key2);
			}
			return ((Comparable) key1).compareTo(key2);
		}

		private static <T> T first(List<T> list) {
			if (list.isEmpty()) {
				return null;
			} else {
				return list.get(0);
			}
		}

		private int compareList(List<?> list1, List<?> list2) {
			int length1 = list1.size();
			int lenght2 = list2.size();
			for (int n = 0, cnt = Math.min(length1, lenght2); n < cnt; n++) {
				int elementResult = compareNull(list1.get(n), list2.get(n));
				if (elementResult != 0) {
					return elementResult;
				}
			}
			return length1 < lenght2 ? -1 : length1 > lenght2 ? 1 : 0;
		}
	}

}
