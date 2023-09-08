/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.meta;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.SetExpression;

/**
 * {@link Expression} that works as place holder for an concrete {@link Expression}.
 * 
 * @see MetaSet place holder for {@link SetExpression}
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public class MetaValue extends Expression implements MetaVariable<Expression> {

	private final String name;
	
	private Expression binding;

	MetaValue(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void bind(Expression expr) {
		this.binding = expr;
	}
	
	@Override
	public Expression getBinding() {
		return this.binding;
	}
	
	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return ((MetaExpressionVisitor<R, A>) v).visitMetaValue(this, arg);
	}

}
