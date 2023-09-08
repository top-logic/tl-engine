/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Comparator;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} creating a search key that compares in the opposite order than its
 * wrapped value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Desc extends UnaryOperation {

	/**
	 * Creates a {@link Desc}.
	 */
	public Desc(SearchExpression key) {
		super(key);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object key = getArgument().evalWith(definitions, args);
		Descending result = new Descending(key);
		return result;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitDesc(this, arg);
	}

	private static class Descending extends Number implements Comparable<Descending> {

		private static Comparator<Object> DESC = new Compare.Cmp() {
			@Override
			protected int compareValues(Object o1, Object o2) {
				return -super.compareValues(o1, o2);
			}

			@Override
			protected int nullCompareValue() {
				return 1;
			}
		};

		private Object _key;

		public Descending(Object key) {
			_key = key;
		}

		@Override
		public int compareTo(Descending other) {
			return DESC.compare(_key, other._key);
		}

		@Override
		public int intValue() {
			return -((Number) _key).intValue();
		}

		@Override
		public long longValue() {
			return -((Number) _key).longValue();
		}

		@Override
		public float floatValue() {
			return -((Number) _key).floatValue();
		}

		@Override
		public double doubleValue() {
			return -((Number) _key).doubleValue();
		}
	}

}
