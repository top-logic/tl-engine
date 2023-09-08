/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Initial set that contains all objects of a certain concrete type (including subtypes).
 * 
 * @see AllOf
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AnyOf extends TypedSetExpression {
	
	/**
	 * Creates a {@link AnyOf}.
	 * 
	 * @param typeName
	 *        See {@link #getTypeName()}.
	 */
	AnyOf(String typeName) {
		super(typeName);
	}

	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitAnyOf(this, arg);
	}

}
