/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.Union;

/**
 * {@link SetExpressionVisitor} that creates the WHERE part of the SELECT statements based on
 * {@link Filter} expressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class WhereBuilder implements SetExpressionVisitor<SQLExpression, Void> {

	private static final Void none = null;

	private ExpressionVisitor<SQLExpression, Void> _expressionEvaluator;

	public void initExpressionEvaluator(ExpressionVisitor<SQLExpression, Void> expressionEvaluator) {
		_expressionEvaluator = expressionEvaluator;
	}

	@Override
	public SQLExpression visitAllOf(AllOf expr, Void arg) {
		return SQLBoolean.TRUE;
	}

	@Override
	public SQLExpression visitAnyOf(AnyOf expr, Void arg) {
		throw new AssertionError("Polymorphic item lookup should be expanded before creating SQL.");
	}

	@Override
	public SQLExpression visitFilter(Filter expr, Void arg) {
		SQLExpression innerWhere = expr.getSource().visitSetExpr(this, arg);

		SQLExpression filterWhere = expr.getFilter().visit(_expressionEvaluator, none);
		return and(innerWhere, filterWhere);
	}

	@Override
	public SQLExpression visitMapTo(MapTo expr, Void arg) {
		SQLExpression innerWhere = expr.getSource().visitSetExpr(this, arg);
		return innerWhere;
	}

	@Override
	public SQLExpression visitSetLiteral(SetLiteral expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitSetParameter(SetParameter expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitCrossProduct(CrossProduct expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitIntersection(Intersection expr, Void arg) {
		throw new IllegalArgumentException("Intersection nodes must be transformed before SQL creation.");
	}

	@Override
	public SQLExpression visitNone(None expr, Void arg) {
		throw new IllegalArgumentException("None nodes must be transformed before SQL creation.");
	}

	@Override
	public SQLExpression visitPartition(Partition expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitSubstraction(Substraction expr, Void arg) {
		throw new IllegalArgumentException("Substraction nodes must be transformed before SQL creation.");
	}

	@Override
	public SQLExpression visitUnion(Union expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

}
