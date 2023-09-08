/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.BinarySetExpression;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.TypedSetExpression;
import com.top_logic.knowledge.search.Union;

/**
 * Base class for {@link SetExpressionVisitor}s.
 * 
 * All visit methods delegate to default implementations along the visited type hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSetExpressionVisitor<RS, RE, A> extends DefaultExpressionVisitor<RE, A> implements SetExpressionVisitor<RS, A> {

	@Override
	public RS visitAllOf(AllOf expr, A arg) {
		return visitTypedSetExpression(expr, arg);
	}

	@Override
	public RS visitAnyOf(AnyOf expr, A arg) {
		return visitTypedSetExpression(expr, arg);
	}

	@Override
	public RS visitCrossProduct(CrossProduct expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	@Override
	public RS visitFilter(Filter expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	@Override
	public RS visitIntersection(Intersection expr, A arg) {
		return visitBinarySetExpression(expr, arg);
	}

	@Override
	public RS visitMapTo(MapTo expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	@Override
	public RS visitNone(None expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	@Override
	public RS visitPartition(Partition expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	@Override
	public RS visitSetLiteral(SetLiteral expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	@Override
	public RS visitSetParameter(SetParameter expr, A arg) {
		return visitSetExpression(expr, arg);
	}
	
	@Override
	public RS visitSubstraction(Substraction expr, A arg) {
		return visitBinarySetExpression(expr, arg);
	}

	@Override
	public RS visitUnion(Union expr, A arg) {
		return visitBinarySetExpression(expr, arg);
	}

	/**
	 * Common action for visit of {@link TypedSetExpression}
	 * 
	 * @param expr
	 *        the visited {@link TypedSetExpression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected RS visitTypedSetExpression(TypedSetExpression expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	/**
	 * Common action for visit of {@link BinarySetExpression}
	 * 
	 * @param expr
	 *        the visited {@link BinarySetExpression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected RS visitBinarySetExpression(BinarySetExpression expr, A arg) {
		return visitSetExpression(expr, arg);
	}

	/**
	 * Common action for visit of {@link SetExpression}
	 * 
	 * @param expr
	 *        the visited {@link SetExpression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected RS visitSetExpression(SetExpression expr, A arg) {
		return null;
	}

}
