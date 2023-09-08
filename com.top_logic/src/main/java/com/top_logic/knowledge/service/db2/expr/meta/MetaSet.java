/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.meta;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;

/**
 * {@link SetExpression} that works as place holder for an concrete {@link SetExpression}.
 * 
 * @see MetaValue place holder for {@link Expression}
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public class MetaSet extends SetExpression implements MetaVariable<SetExpression> {

	private final String name;
	private SetExpression binding;

	MetaSet(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public SetExpression getBinding() {
		return this.binding;
	}

	@Override
	public void bind(SetExpression expr) {
		this.binding = expr;
	}
	
	@Override
	public <R, A> R visitSetExpr(SetExpressionVisitor<R, A> v, A arg) {
		return ((MetaSetExpressionVisitor<R, A>) v).visitMetaSet(this, arg);
	}

}
