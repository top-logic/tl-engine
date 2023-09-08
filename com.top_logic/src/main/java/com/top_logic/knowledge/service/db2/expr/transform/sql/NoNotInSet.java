/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultDescendingQueryVisitor;

/**
 * Checks that no search for a thing that is not {@link InSet} of another set is given.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class NoNotInSet {

	private static final SetExpressionVisitor<Void, Boolean> CHECK_NOT_INSET =
		new DefaultDescendingQueryVisitor<>() {

			@Override
			public Void visitSubstraction(Substraction expr, Boolean arg) {
				Void left = descendSet(expr, expr.getLeftExpr(), arg);
				Void right = descendSet(expr, expr.getRightExpr(), !arg);
				return process(expr, left, right);
			}

			@Override
			public Void visitUnaryOperation(UnaryOperation expr, Boolean arg) {
				if (expr.getOperator() == Operator.NOT) {
					return super.visitUnaryOperation(expr, !arg);
				} else {
					return super.visitUnaryOperation(expr, arg);
				}
			}

			@Override
			public Void visitInSet(InSet expr, Boolean arg) {
				if (!arg) {
					throw new IllegalArgumentException(
						"Can not process queries which searches for things that are not in an inSet expression.");
				}
				return super.visitInSet(expr, arg);
			}

		};

	/**
	 * Checks that no combination of {@link Operator#NOT not} and {@link InSet} is contained in the
	 * Query.
	 */
	public static void checkNoNotInSet(SetExpression expr) {
		expr.visitSetExpr(CHECK_NOT_INSET, Boolean.TRUE);
	}

}
