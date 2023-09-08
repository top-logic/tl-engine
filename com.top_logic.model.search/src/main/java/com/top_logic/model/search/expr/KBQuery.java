/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Execution of a pre-compiled knowledge base query.
 * 
 * <p>
 * {@link KBQuery} expressions are created internally during the query optimization process.
 * </p>
 * 
 * @see SearchExpressionFactory#query(TLClass, SetExpression)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBQuery extends SearchExpression {

	private final TLClass _classType;

	private final SetExpression _query;

	KBQuery(TLClass classType, SetExpression query) {
		_classType = classType;
		_query = query;
	}

	/**
	 * The {@link TLClass} type all results are compatible with.
	 */
	public TLClass getClassType() {
		return _classType;
	}

	/**
	 * The internal query.
	 */
	public SetExpression getQuery() {
		return _query;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return definitions.getKnowledgeBase().search(ExpressionFactory.queryResolved(getQuery(), TLObject.class));
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitKBQuery(this, arg);
	}

}
