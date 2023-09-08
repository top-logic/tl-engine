/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

/**
 * Visitor for {@link Symbol}s.
 * 
 * @param <R>
 *        Result type of the visit.
 * @param <A>
 *        Argument type of the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SymbolVisitor<R,A> {

	/**
	 * Visit case for {@link LiteralItemSymbol}s.
	 */
	R visitLiteralItemSymbol(LiteralItemSymbol sym, A arg);

	/**
	 * Visit case for {@link TableSymbol}s.
	 */
	R visitTableSymbol(TableSymbol sym, A arg);

	/**
	 * Visit case for {@link ReferenceSymbol}s.
	 */
	R visitReferenceSymbol(ReferenceSymbol sym, A arg);

	/**
	 * Visit case for {@link TupleSymbol}s.
	 */
	R visitTupleSymbol(TupleSymbol sym, A arg);

	/**
	 * Visit case for {@link RowAttributeSymbol}s.
	 */
	R visitRowAttributeSymbol(RowAttributeSymbol sym, A arg);

	/**
	 * Visit case for {@link FlexAttributeSymbol}s.
	 */
	R visitFlexAttributeSymbol(FlexAttributeSymbol sym, A arg);

	/**
	 * Visit case for {@link PrimitiveSymbol}s.
	 */
	R visitPrimitiveSymbol(PrimitiveSymbol sym, A arg);

	/**
	 * Visit case for {@link NullSymbol}s.
	 */
	R visitNullSymbol(NullSymbol sym, A arg);
}