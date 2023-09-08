/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.search.QueryPart;

/**
 * {@link Symbol} created for a definition with an error.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ErrorSymbol extends TableSymbol {

	/**
	 * Creates a {@link ErrorSymbol}.
	 * 
	 * @param definition
	 *        The erroneous definition.
	 */
	public ErrorSymbol(QueryPart definition) {
		super(definition, null);
	}

	@Override
	public <R, A> R visit(SymbolVisitor<R, A> v, A arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initParent(TupleSymbol tupleSymbol) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Symbol getParent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasOwnLifetime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createTypeExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createRevisionExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createHistoryContextExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createRevMinExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createRevMaxExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createIdentifierExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression createBranchExpr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MOClass getType() {
		// return MetaObject.INVALID_TYPE;
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTableAlias() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TableSymbol dereference(MOClass concreteType) {
		return this;
	}
}