/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Intersection of two {@link SetExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Intersection extends BinarySetExpression {

	Intersection(SetExpression expr1, SetExpression expr2) {
		super(expr1, expr2);
	}

	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitIntersection(this, arg);
	}

}
