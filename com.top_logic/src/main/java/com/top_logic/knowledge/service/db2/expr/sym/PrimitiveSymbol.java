/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.knowledge.search.Expression;

/**
 * {@link Symbol} representing a value of primitive type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrimitiveSymbol extends AbstractSymbol {

	private TupleSymbol parentSymbol;

	public PrimitiveSymbol(Expression definition) {
		super(definition);
	}
	
	@Override
	public Expression getDefinition() {
		return (Expression) super.getDefinition();
	}

	@Override
	public TupleSymbol getParent() {
		return parentSymbol;
	}

	@Override
	public void initParent(TupleSymbol newParent) {
		assert this.parentSymbol == null : "Must initialize parent only once.";
		assert newParent != null : "Parent must not be null.";
		
		this.parentSymbol = newParent;
	}

	@Override
	public <R, A> R visit(SymbolVisitor<R, A> v, A arg) {
		return v.visitPrimitiveSymbol(this, arg);
	}
	
}