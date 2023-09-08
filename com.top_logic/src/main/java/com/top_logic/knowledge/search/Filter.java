/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link SetExpression} that filters out selected objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Filter extends SetExpression {

	private SetExpression source;
	private Expression filter;

	Filter(SetExpression source, Expression filter) {
		this.source = source;
		this.filter = filter;
	}
	
	public SetExpression getSource() {
		return source;
	}
	
	public void setSource(SetExpression source) {
		this.source = source;
	}
	
	public Expression getFilter() {
		return filter;
	}
	
	public void setFilter(Expression filter) {
		this.filter = filter;
	}
	
	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitFilter(this, arg);
	}

}
