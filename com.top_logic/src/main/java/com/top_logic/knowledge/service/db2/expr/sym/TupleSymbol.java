/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import java.util.List;

import com.top_logic.knowledge.search.QueryPart;

/**
 * {@link Symbol} representing tuple-valued definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TupleSymbol extends AbstractSymbol {

	private final List<Symbol> symbols;
	private Symbol parentSymbol;

	public TupleSymbol(QueryPart definition, List<Symbol> symbols) {
		super(definition);
		this.symbols = symbols;
	}
	
	public List<Symbol> getSymbols() {
		return symbols;
	}
	
	@Override
	public Symbol getParent() {
		return this.parentSymbol;
	}
	
	@Override
	public void initParent(TupleSymbol newParent) {
		assert this.parentSymbol == null : "Must initialize parent only once.";
		assert newParent != null : "Parent must not be null.";
		
		this.parentSymbol = newParent;
	}
	
	@Override
	public <R, A> R visit(SymbolVisitor<R,A> v, A arg) {
		return v.visitTupleSymbol(this, arg);
	}
}