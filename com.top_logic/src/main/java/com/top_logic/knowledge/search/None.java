/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * The literal empty set.
 * 
 * @see SetLiteral A set of literal values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class None extends SetExpression {

	None() {
		// Default constructor.
	}
	
	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitNone(this, arg);
	}

}
