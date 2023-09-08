/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;


/**
 * {@link SearchExpression} returning either the {@link #getIfClause()} result, or the
 * {@link #getElseClause()} result depending on the evaluation of a {@link Boolean}
 * {@link #getCondition()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IfElse extends SearchExpression {

	private SearchExpression _condition;

	private SearchExpression _ifClause;

	private SearchExpression _elseClause;

	/**
	 * Creates a {@link IfElse}.
	 * 
	 * @param condition
	 *        See {@link #getCondition()}.
	 * @param ifClause
	 *        See {@link #getIfClause()}.
	 * @param elseClause
	 *        See {@link #getElseClause()}.
	 */
	IfElse(SearchExpression condition, SearchExpression ifClause, SearchExpression elseClause) {
		_condition = condition;
		_ifClause = ifClause;
		_elseClause = elseClause;
	}

	/**
	 * The {@link Boolean} condition causing {@link #getIfClause()} or {@link #getElseClause()} to
	 * be evaluated.
	 */
	public SearchExpression getCondition() {
		return _condition;
	}

	/**
	 * @see #getCondition()
	 */
	public void setCondition(SearchExpression condition) {
		_condition = condition;
	}

	/**
	 * The {@link SearchExpression} being evaluated, if {@link #getCondition()} evaluates to
	 * <code>true</code>.
	 */
	public SearchExpression getIfClause() {
		return _ifClause;
	}

	/**
	 * @see #getIfClause()
	 */
	public void setIfClause(SearchExpression ifClause) {
		_ifClause = ifClause;
	}

	/**
	 * The {@link SearchExpression} being evaluated, if {@link #getCondition()} evaluates to
	 * <code>false</code>.
	 */
	public SearchExpression getElseClause() {
		return _elseClause;
	}

	/**
	 * @see #getElseClause()
	 */
	public void setElseClause(SearchExpression elseClause) {
		_elseClause = elseClause;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitIfElse(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		boolean conditionResult = isTrue(getCondition().evalWith(definitions, args));
		if (conditionResult) {
			return getIfClause().evalWith(definitions, args);
		} else {
			return getElseClause().evalWith(definitions, args);
		}
	}

}