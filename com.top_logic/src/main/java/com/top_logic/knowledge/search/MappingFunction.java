/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Function} that evaluates a certain {@link #getExpr()} on the context object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MappingFunction extends Function {

	private Expression expr;

	public MappingFunction(Expression expr) {
		this.expr = expr;
	}
	
	public Expression getExpr() {
		return expr;
	}
	
	public void setExpr(Expression expr) {
		this.expr = expr;
	}
	
}
