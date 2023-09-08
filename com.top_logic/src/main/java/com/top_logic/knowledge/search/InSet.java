/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Expression that checks, whether the context object is in a certain set.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InSet extends ContextExpression {

	private SetExpression setExpr;

	/* package protected */InSet(Expression context, SetExpression setExpr) {
		super(context);
		this.setExpr = setExpr;
	}
	
	public SetExpression getSetExpr() {
		return setExpr;
	}
	
	public void setSetExpr(SetExpression setExpr) {
		this.setExpr = setExpr;
	}
	
	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return v.visitInSet(this, arg);
	}

}
