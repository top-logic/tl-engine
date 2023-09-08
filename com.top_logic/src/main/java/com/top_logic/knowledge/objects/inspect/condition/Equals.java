/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;


/**
 * Equality test {@link Condition}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Equals extends Condition {

	private final ItemFunction _test;

	private final Object _expected;

	/**
	 * Creates a {@link Equals}.
	 * 
	 * @see Conditions#ifEquals(ItemFunction, Object)
	 */
	Equals(ItemFunction expr, Object expected) {
		_test = expr;
		_expected = expected;
	}

	/**
	 * The {@link ItemFunction} to evaluate.
	 * 
	 * @see #expected()
	 */
	public ItemFunction test() {
		return _test;
	}

	/**
	 * The expected result of evaluating {@link #test()} for this {@link Equals} condition to hold.
	 * 
	 * @see #test()
	 */
	public Object expected() {
		return _expected;
	}

	@Override
	public String toString() {
		return _test + "=" + _expected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_expected == null) ? 0 : _expected.hashCode());
		result = prime * result + ((_test == null) ? 0 : _test.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Equals other = (Equals) obj;
		if (_expected == null) {
			if (other._expected != null)
				return false;
		} else if (!_expected.equals(other._expected))
			return false;
		if (_test == null) {
			if (other._test != null)
				return false;
		} else if (!_test.equals(other._test))
			return false;
		return true;
	}

	@Override
	public <R, A> R visit(ConditionVisitor<R, A> v, A arg) {
		return v.visitEquals(this, arg);
	}
}