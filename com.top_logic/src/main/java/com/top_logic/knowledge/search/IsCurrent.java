/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Expression} that checks whether the context expression is a current object.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsCurrent extends ContextExpression {

	IsCurrent(Expression context) {
		super(context);
	}

	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return v.visitIsCurrent(this, arg);
	}

}

