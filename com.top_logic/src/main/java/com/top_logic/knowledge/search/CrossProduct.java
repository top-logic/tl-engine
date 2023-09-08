/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;


/**
 * The cross product of sets given by {@link SetExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CrossProduct extends SetExpression {

	private List<SetExpression> expressions;

	/**
	 * @see ExpressionFactory#crossProduct(List)
	 */
	CrossProduct(List<SetExpression> expressions) {
		this.expressions = expressions;
	}

	/**
	 * The {@link SetExpression}s that deliver the source sets of the cross
	 * product.
	 */
	public List<SetExpression> getExpressions() {
		return expressions;
	}
	
	public void setExpressions(List<SetExpression> expressions) {
		this.expressions = expressions;
	}
	
	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitCrossProduct(this, arg);
	}

}
