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
 * {@link SearchExpression} reference to a local variable in the current scope.
 * 
 * @see Lambda
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Var extends SearchExpression {
	private Object _name;

	/**
	 * @see #getDef()
	 */
	private Definition _def;

	private NamedConstant _key;

	/**
	 * Creates a {@link Var}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	Var(Object name) {
		_name = name;
	}

	/**
	 * {@link Lambda#getName() Name} the accessed local variable from its {@link #getDef() definition}.
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
	 * The {@link Lambda} in scope that defined the referenced variable.
	 */
	public Definition getDef() {
		return _def;
	}

	/**
	 * Initialized {@link #getDef()} and {@link #getName()}.
	 */
	public void setDef(Definition def) {
		_def = def;
		_key = def.getKey();
	}

	/**
	 * {@link Lambda#getKey()} of {@link #getDef()}.
	 */
	public NamedConstant getKey() {
		return _key;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitVar(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return definitions.getVar(_key);
	}
}