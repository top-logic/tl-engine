/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Set of {@link #getMapping() mapped} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapTo extends SetExpression {

	private SetExpression source;
	private Expression mapping;

	MapTo(SetExpression source, Expression mapping) {
		this.source = source;
		this.mapping = mapping;
	}
	
	public SetExpression getSource() {
		return source;
	}
	
	public void setSource(SetExpression source) {
		this.source = source;
	}
	
	/**
	 * Expression to evaluate on all objects of the {@link #getSource()} set.
	 */
	public Expression getMapping() {
		return mapping;
	}
	
	public void setMapping(Expression mapping) {
		this.mapping = mapping;
	}
	
	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitMapTo(this, arg);
	}

}
