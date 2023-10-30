/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.expr.TestExpression;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} conditionally rendering a value.
 * 
 * @see TestExpression
 */
public class ConditionalTemplate implements RawTemplateFragment {

	private final TemplateExpression _test;

	private HTMLTemplateFragment _thenFragment;

	private HTMLTemplateFragment _elseFragment;

	/**
	 * Creates a {@link ConditionalTemplate}.
	 *
	 * @param test
	 *        The test to perform.
	 * @param thenFragment
	 *        The fragment to render if the test result is <code>true</code>.
	 * @param elseFragment
	 *        the fragment to render if the test result is <code>false</code>.
	 */
	public ConditionalTemplate(TemplateExpression test, HTMLTemplateFragment thenFragment,
			HTMLTemplateFragment elseFragment) {
		_test = test;
		_thenFragment = thenFragment;
		_elseFragment = elseFragment;
	}

	/**
	 * The test expression.
	 */
	public TemplateExpression getTest() {
		return _test;
	}

	/**
	 * The then part.
	 */
	public HTMLTemplateFragment getThenFragment() {
		return _thenFragment;
	}

	/**
	 * @see #getThenFragment()
	 */
	public void setThenFragment(HTMLTemplateFragment thenFragment) {
		_thenFragment = thenFragment;
	}

	/**
	 * The else part.
	 */
	public HTMLTemplateFragment getElseFragment() {
		return _elseFragment;
	}

	/**
	 * @see #getElseFragment()
	 */
	public void setElseFragment(HTMLTemplateFragment elseFragment) {
		_elseFragment = elseFragment;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		boolean testResult;
		try {
			testResult = TestExpression.isTrue(_test.eval(context, properties));
		} catch (RuntimeException exception) {
			HTMLTemplateUtils.renderError(context, out, exception);
			return;
		}
		if (testResult) {
			_thenFragment.write(context, out, properties);
		} else {
			_elseFragment.write(context, out, properties);
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
