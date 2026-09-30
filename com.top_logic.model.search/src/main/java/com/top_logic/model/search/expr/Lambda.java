/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.NamedConstant;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} representing the binding of a local variable in a certain {@link #getBody()
 * scope}.
 * 
 * <p>
 * The semantic of {@link Lambda} is the following:
 * </p>
 * 
 * <ol>
 * <li>The actual argument is bound to the variable with name {@link #getName()}.</li>
 * <li>The expression {@link #getBody()} is evaluated. Within this expression, the actual argument
 * can be accessed through a local variable reference {@link Var} with the name {@link #getName()}.</li>
 * <li>The result of the whole expression is the result of the evaluation of {@link #getBody()}.</li>
 * </ol>
 * 
 * @see Var Accessing a local variable.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Lambda extends SearchExpression implements Definition {

	private final NamedConstant _key;

	private Object _name;

	private SearchExpression _body;


	/**
	 * Creates a {@link Lambda}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param body
	 *        See {@link #getBody()}.
	 */
	Lambda(Object name, SearchExpression body) {
		_name = name;
		_body = body;

		_key = new NamedConstant(name.toString());
	}

	/**
	 * The name of the bound variable.
	 */
	public Object getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public void setName(Object name) {
		_name = name;
	}

	/**
	 * The {@link SearchExpression} that is evaluated with the given variable binding in scope.
	 */
	public SearchExpression getBody() {
		return _body;
	}

	/**
	 * @see #getBody()
	 */
	public void setBody(SearchExpression body) {
		_body = body;
	}

	@Override
	public NamedConstant getKey() {
		return _key;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitLambda(this, arg);
	}

	@Override
	public Object internalEval(EvalContext innerDefinitions, Args innerArgs) {
		if (!innerArgs.hasValue()) {
			// Keep the function as function value. Since the function body may depend on
			// definitions from the outside, the original context must be kept.
			EvalContext contextSnapshot = innerDefinitions.snapshot();

			return new Lambda(_name, _body) {
				@Override
				public Object internalEval(EvalContext outerDefinitions, Args outerArgs) {
					if (!outerArgs.hasValue()) {
						return this;
					}

					// Copy to avoid race conditions.
					EvalContext execSnapshot = contextSnapshot.snapshot();

					execSnapshot.defineVar(_key, outerArgs.value());
					return _body.evalWith(execSnapshot, outerArgs.next());
				}
			};
		}
		innerDefinitions.defineVar(_key, innerArgs.value());
		return _body.evalWith(innerDefinitions, innerArgs.next());
	}

}