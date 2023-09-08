/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
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
 * Algorithm producing expressions for accessing the value of a {@link Symbol}.
 * 
 *          com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class DeReferencer implements SymbolVisitor<SQLExpression, Void> {
	
	private static final Void none = null;

	private ExpressionVisitor<SQLExpression, Void> _expressionEvaluator;
	
	public void initExpressionEvaluator(ExpressionVisitor<SQLExpression, Void> expressionEvaluator) {
		this._expressionEvaluator = expressionEvaluator;
	}

	@Override
	public SQLExpression visitFlexAttributeSymbol(FlexAttributeSymbol sym, Void arg) {
		MetaObject expectedType = sym.getType();
		String columnName;
		if (expectedType == MOPrimitive.LONG) {
			columnName = AbstractFlexDataManager.LONG_DATA_DBNAME;
		}
		else if (expectedType == MOPrimitive.DOUBLE) {
			columnName = AbstractFlexDataManager.DOUBLE_DATA_DBNAME;
		}
		else if (expectedType == MOPrimitive.STRING) {
			columnName = AbstractFlexDataManager.VARCHAR_DATA_DBNAME;
		}
		else {
			//  TODO bhu Automatically created
			throw new UnsupportedOperationException();
		}
		return column(sym.getDataTableAlias(), columnName);
	}

	@Override
	public SQLExpression visitPrimitiveSymbol(PrimitiveSymbol sym, Void arg) {
		return sym.getDefinition().visit(_expressionEvaluator, none);
	}

	@Override
	public SQLExpression visitRowAttributeSymbol(RowAttributeSymbol sym, Void arg) {
		return RowAttributeSymbol.createColumn(sym);
	}

	@Override
	public SQLExpression visitReferenceSymbol(ReferenceSymbol sym, Void arg) {
		return sym.createExpression(sym.getDefinition().getAccessType());
	}

	@Override
	public SQLExpression visitLiteralItemSymbol(LiteralItemSymbol sym, Void arg) {
		throw new AssertionError("Literal item symbols must not be dereferenced in SQL.");
	}

	@Override
	public SQLExpression visitTableSymbol(TableSymbol sym, Void arg) {
		// TODO: Use dereferencing for accessing all result columns? 
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitTupleSymbol(TupleSymbol sym, Void arg) {
		// TODO: Use dereferencing for accessing all result columns? 
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitNullSymbol(NullSymbol sym, Void arg) {
		throw new AssertionError("Null may not be dereferenced.");
	}
}
