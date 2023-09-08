/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Explicit access to the context object of the surrounding {@link SetExpression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ContextExpression extends Expression {

	/** @see #getContext() */
	private Expression _context;

	ContextExpression(Expression context) {
		_context = context;
	}

	/**
	 * Returns the context in which this {@link ContextExpression} is evaluated.
	 */
	public Expression getContext() {
		return _context;
	}

	/**
	 * Sets value of {@link #getContext()}
	 * 
	 * @see #getContext()
	 */
	public void setContext(Expression context) {
		_context = context;
	}

}
