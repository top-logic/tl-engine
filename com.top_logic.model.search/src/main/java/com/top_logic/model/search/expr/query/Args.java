/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import java.util.NoSuchElementException;

import com.top_logic.model.search.expr.SearchExpression;

/**
 * Linked list of arguments for evaluating a {@link SearchExpression}.
 * 
 * @see SearchExpression#evalWith(com.top_logic.model.search.expr.EvalContext, Args)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Args {
	
	/**
	 * The empty argument list with no arguments at all.
	 */
	public static Args none() {
		return None.INSTANCE;
	}

	/**
	 * A single argument.
	 * 
	 * @param value
	 *        The argument value.
	 * @return The argument list.
	 */
	public static Args some(Object value) {
		return cons(value, none());
	}

	/**
	 * A list of arguments of arbitrary length.
	 * 
	 * @param values
	 *        The argument values to encode.
	 * @return The argument list.
	 */
	public static Args some(Object... values) {
		Args result = none();
		for (int n = values.length - 1; n >= 0; n--) {
			result = cons(values[n], result);
		}
		return result;
	}

	/**
	 * Prepends the given value to the given argument list resulting in a new enlarged argument
	 * list.
	 * 
	 * @param value
	 *        The argument value to prepend to the given list.
	 * @param next
	 *        The argument list to prepend.
	 * @return The enlarged list.
	 */
	public static Args cons(Object value, Args next) {
		return new Arg(value, next);
	}

	/**
	 * Whether this argument list consists at least of one element.
	 */
	public abstract boolean hasValue();

	/**
	 * The first value in this argument list.
	 */
	public abstract Object value();

	/**
	 * The argument list without the first element.
	 * 
	 * @see #value()
	 */
	public abstract Args next();

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('(');
		Args args = this;
		boolean first = true;
		while (args.hasValue()) {
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}
			result.append(args.value());
			args = args.next();
		}
		result.append(')');
		return result.toString();
	}

	private static class Arg extends Args {

		private final Object _value;

		private final Args _next;

		public Arg(Object value, Args next) {
			_value = SearchExpression.normalizeValue(value);
			_next = next;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		@Override
		public Object value() {
			return _value;
		}

		@Override
		public Args next() {
			return _next;
		}
	}

	private static class None extends Args {
		/**
		 * Singleton {@link Args.None} instance.
		 */
		public static final Args.None INSTANCE = new Args.None();

		private None() {
			// Singleton constructor.
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		@Override
		public Object value() {
			throw new NoSuchElementException("No arguments.");
		}

		@Override
		public Args next() {
			throw new NoSuchElementException("No arguments.");
		}
	}

}
