/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

/**
 * Abstract superclass to visitor that visits the {@link Symbol} hierarchy.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AbstractSymbolVisitor<R, A> implements SymbolVisitor<R, A> {

	@Override
	public R visitLiteralItemSymbol(LiteralItemSymbol sym, A arg) {
		return visitAbstractItemSymbol(sym, arg);
	}

	@Override
	public R visitTableSymbol(TableSymbol sym, A arg) {
		return visitAbstractItemSymbol(sym, arg);
	}

	@Override
	public R visitReferenceSymbol(ReferenceSymbol sym, A arg) {
		return visitAbstractItemSymbol(sym, arg);
	}

	@Override
	public R visitTupleSymbol(TupleSymbol sym, A arg) {
		return visitAbstractSymbol(sym, arg);
	}

	@Override
	public R visitRowAttributeSymbol(RowAttributeSymbol sym, A arg) {
		return visitAttributeSymbol(sym, arg);
	}

	@Override
	public R visitFlexAttributeSymbol(FlexAttributeSymbol sym, A arg) {
		return visitAttributeSymbol(sym, arg);
	}

	@Override
	public R visitPrimitiveSymbol(PrimitiveSymbol sym, A arg) {
		return visitAbstractSymbol(sym, arg);
	}

	@Override
	public R visitNullSymbol(NullSymbol sym, A arg) {
		return visitAbstractSymbol(sym, arg);
	}

	/**
	 * Default visit case for all {@link AbstractItemSymbol}.
	 * 
	 * @param sym
	 *        The visited {@link Symbol}
	 * @param arg
	 *        The visit argument
	 */
	protected R visitAbstractItemSymbol(AbstractItemSymbol sym, A arg) {
		return visitAbstractSymbol(sym, arg);
	}

	/**
	 * Default visit case for all {@link AttributeSymbol}.
	 * 
	 * @param sym
	 *        The visited {@link Symbol}
	 * @param arg
	 *        The visit argument
	 */
	protected R visitAttributeSymbol(AttributeSymbol sym, A arg) {
		return visitAbstractSymbol(sym, arg);
	}

	/**
	 * Default visit case for all {@link AbstractItemSymbol}.
	 * 
	 * @param sym
	 *        The visited {@link Symbol}
	 * @param arg
	 *        The visit argument
	 */
	protected R visitAbstractSymbol(AbstractSymbol sym, A arg) {
		return null;
	}

}

