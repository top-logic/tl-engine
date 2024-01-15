/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import java.util.Collection;

import com.top_logic.html.template.ConditionalTemplate;
import com.top_logic.html.template.RawTemplateFragment;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link TemplateExpression} conditionally computing a value.
 */
public class TestExpression implements TemplateExpression {

	private final TemplateExpression _test;

	private final TemplateExpression _thenExpression;

	private final TemplateExpression _elseExpression;

	/**
	 * Creates a {@link TestExpression}.
	 * 
	 * @param test
	 *        A boolean expression that decides about the evaluation.
	 * @param thenExpression
	 *        The expression that is evaluated, if the test results in <code>true</code>.
	 * @param elseExpression
	 *        The expression that is evaluated, if the test results in <code>false</code>.
	 */
	public TestExpression(TemplateExpression test, TemplateExpression thenExpression,
			TemplateExpression elseExpression) {
		_test = test;
		_thenExpression = thenExpression;
		_elseExpression = elseExpression;
	}

	/**
	 * The expression to evaluate as boolean.
	 */
	public TemplateExpression getTest() {
		return _test;
	}

	/**
	 * The expression to evaluate, if the {@link #getTest()} returns <code>true</code>.
	 */
	public TemplateExpression getThen() {
		return _thenExpression;
	}

	/**
	 * The expression to evaluate, if the {@link #getTest()} returns <code>false</code>.
	 */
	public TemplateExpression getElse() {
		return _elseExpression;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		return TestExpression.isTrue(_test.eval(context, properties)) ? _thenExpression.eval(context, properties)
			: _elseExpression.eval(context, properties);
	}

	@Override
	public RawTemplateFragment toFragment() {
		return new ConditionalTemplate(_test, _thenExpression.toFragment(), _elseExpression.toFragment());
	}

	/**
	 * Whether the given value is considered to be equivalent to <code>true</code> in a boolean
	 * context.
	 */
	public static boolean isTrue(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		if (value instanceof Collection<?>) {
			return !((Collection<?>) value).isEmpty();
		}
		if (value instanceof CharSequence) {
			return ((CharSequence) value).length() > 0;
		}
		return true;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}
}
