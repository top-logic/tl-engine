/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Initial set that contains all objects of a certain concrete type (no subtypes are considered).
 * 
 * @see AnyOf
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllOf extends TypedSetExpression {
	
	/**
	 * Creates a {@link AllOf}.
	 * 
	 * @param typeName
	 *        See {@link #getTypeName()}.
	 */
	AllOf(String typeName) {
		super(typeName);
	}

	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitAllOf(this, arg);
	}

}
