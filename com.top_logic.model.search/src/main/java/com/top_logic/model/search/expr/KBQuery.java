/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.compile.eval.CompiledValue;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Execution of a pre-compiled knowledge base query.
 * 
 * <p>
 * {@link KBQuery} expressions are created internally during the query optimization process.
 * </p>
 * 
 * @see SearchExpressionFactory#query(TLClass, SetExpression, List)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBQuery extends SearchExpression {

	private final TLClass _classType;

	private final SetExpression _query;

	private final List<CompiledValue> _dynamic;

	KBQuery(TLClass classType, SetExpression query, List<CompiledValue> dynamicFilters) {
		_classType = classType;
		_query = query;
		_dynamic = dynamicFilters;
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

	/**
	 * List of {@link CompiledValue}s that dynamically creates a filter {@link Expression} at
	 * evaluation time based on the given arguments.
	 */
	public List<CompiledValue> getDynamicFilters() {
		return _dynamic;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		KnowledgeBase kb = definitions.getKnowledgeBase();

		List<CompiledValue> deferredFilterParts = Collections.emptyList();

		SetExpression query = getQuery();
		for (CompiledValue part : _dynamic) {
			try {
				Expression expression = part.buildExpression(definitions);
				query = ExpressionFactory.filter(query, expression);
			} catch (CompiledValue.IncompatibleTypes ex) {
				// Could not be resolved to valid expression. Store for later in memory evaluation.
				if (deferredFilterParts.size() == 0) {
					deferredFilterParts = new ArrayList<>();
				}
				deferredFilterParts.add(part);
			}
		}

		// The result is not filtered for security: access to the individual objects' data is secured
		// when their attributes are accessed, and the final result of a script is secured by the
		// caller. The deferred filter parts below are the query's own (non-security) predicates.
		List<TLObject> result = new ArrayList<>();
		try (CloseableIterator<TLObject> dbResult =
			kb.searchStream(ExpressionFactory.queryResolved(query, TLObject.class))) {
			dbResult:
			while (dbResult.hasNext()) {
				TLObject match = dbResult.next();
				for (CompiledValue deferred : deferredFilterParts) {
					if (!SearchExpression.asBoolean(deferred.eval(match, definitions))) {
						continue dbResult;
					}
				}
				result.add(match);
			}
		}

		return result;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitKBQuery(this, arg);
	}

}
