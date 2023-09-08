/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * {@link Expression} to access a flex attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Flex extends TypedExpression {

	private final String name;

	Flex(Expression context, String typeName, String attributeName) {
		super(context, typeName);
		this.name = attributeName;
	}
	
	public String getAttributeName() {
		return name;
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitFlex(this, arg);
	}

}
