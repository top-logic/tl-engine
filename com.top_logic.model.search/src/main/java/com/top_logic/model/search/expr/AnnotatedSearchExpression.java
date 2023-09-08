/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} annotated with source code.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AnnotatedSearchExpression extends SearchExpression {

	private final String _source;

	private final SearchExpression _code;

	/**
	 * Creates a {@link AnnotatedSearchExpression}.
	 *
	 * @param source
	 *        See {@link #getSource()}.
	 * @param code
	 *        See {@link #getExecutable()}.
	 */
	public AnnotatedSearchExpression(String source, SearchExpression code) {
		_source = source;
		_code = code;
	}

	/**
	 * The source code of {@link #getExecutable()}.
	 */
	public String getSource() {
		return _source;
	}

	/**
	 * The compiled {@link #getSource()}.
	 */
	public SearchExpression getExecutable() {
		return _code;
	}

	@Override
	protected Object internalEval(EvalContext definitions, Args args) {
		return _code.evalWith(definitions, args);
	}

	@Override
	public String toString() {
		return getSource();
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return _code.visit(visitor, arg);
	}

}
