/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
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
 * @see SearchExpressionFactory#query(TLClass, SetExpression, List)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBQuery extends SearchExpression {

	private final TLClass _classType;

	private final SetExpression _query;

	private final List<Parameter> _parameters;


	KBQuery(TLClass classType, SetExpression query, List<Parameter> parameters) {
		_classType = classType;
		_query = query;
		_parameters = parameters;
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
	 * This method returns the parameters.
	 * 
	 * @return Returns the parameters.
	 */
	public List<Parameter> getParameters() {
		return _parameters;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		RevisionQueryArguments queryArgs = ExpressionFactory.revisionArgs();
		List<ParameterDeclaration> paramDecls;
		if (_parameters.isEmpty()) {
			paramDecls = ExpressionFactory.NO_QUERY_PARAMETERS;
		} else {
			paramDecls = new ArrayList<>();
			Object[] arguments = new Object[_parameters.size()];
			int argIdx = 0;
			for (Parameter param : _parameters) {
				paramDecls.add(ExpressionFactory.paramDecl(param.type(), param.name()));
				arguments[argIdx++] = definitions.getVarOrNull(param.argumentKey());
			}
			queryArgs.setArguments(arguments);

		}
		RevisionQuery<TLObject> query =
			ExpressionFactory.queryResolved(paramDecls, getQuery(), ExpressionFactory.NO_ORDER, TLObject.class);
		return definitions.getKnowledgeBase().search(query, queryArgs);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitKBQuery(this, arg);
	}

	/**
	 * Parameter object for a {@link KBQuery}.
	 * 
	 * @param name
	 *        Name of a {@link com.top_logic.knowledge.search.Parameter} in the
	 *        {@link KBQuery#getQuery() query}.
	 * @param type
	 *        Expected type for the {@link com.top_logic.knowledge.search.Parameter}.
	 * @param argumentKey
	 *        The key to get the value for the parameter {@link #name()} from the
	 *        {@link EvalContext}.
	 */
	public static record Parameter(String name, MetaObject type, NamedConstant argumentKey) {

		/**
		 * Creates a new {@link Parameter} with random {@link Parameter#name()}.
		 */
		public Parameter(MetaObject type, NamedConstant argumentKey) {
			this(StringServices.randomUUID(), type, argumentKey);
		}

	}

}
