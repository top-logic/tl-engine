/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.knowledge.service.db2.expr.sym.FlexAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.LiteralItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.NullSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.PrimitiveSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ReferenceSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.RowAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;
import com.top_logic.knowledge.service.db2.expr.sym.SymbolVisitor;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.TupleSymbol;

/**
 * Visitor that translates a {@link Symbol} directly to a SQL expression.
 * 
 *          com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder
 *          
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class EvalValue implements SymbolVisitor<SQLExpression, Void> {

	public static final EvalValue INSTANCE = new EvalValue();

	@Override
	public SQLExpression visitRowAttributeSymbol(RowAttributeSymbol sym, Void arg) {
		return RowAttributeSymbol.createColumn(sym);
	}

	@Override
	public SQLExpression visitFlexAttributeSymbol(FlexAttributeSymbol sym, Void arg) {
		// TODO: Direct access to flex attributes.
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitLiteralItemSymbol(LiteralItemSymbol sym, Void arg) {
		throw new AssertionError("Tuples have no direct correspondence in SQL.");
	}

	@Override
	public SQLExpression visitNullSymbol(NullSymbol sym, Void arg) {
		throw new AssertionError("Null values have no direct correspondence in SQL.");
	}

	@Override
	public SQLExpression visitPrimitiveSymbol(PrimitiveSymbol sym, Void arg) {
		//  TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitReferenceSymbol(ReferenceSymbol sym, Void arg) {
		return sym.createExpression(sym.getDefinition().getAccessType());
	}

	@Override
	public SQLExpression visitTableSymbol(TableSymbol sym, Void arg) {
		return ReferenceSymbol.newTuple(sym.createBranchExpr(), sym.createIdentifierExpr(), sym.createRevisionExpr());
	}

	@Override
	public SQLExpression visitTupleSymbol(TupleSymbol sym, Void arg) {
		throw new AssertionError("Tuples have no direct correspondence in SQL.");
	}
}
