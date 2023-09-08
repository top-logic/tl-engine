/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultDescendingQueryVisitor;

/**
 * Checks that a given {@link SetExpression} did not contain any
 * {@link MOReference#getHistoryType() mixed references}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class NoMixedReferences {

	private static final SetExpressionVisitor<Void, Void> CHECK_NO_MIXED =
		new DefaultDescendingQueryVisitor<>() {

			/**
			 * @see com.top_logic.knowledge.service.db2.expr.transform.sql.HistoricSymbol#isHistoricSymbol(com.top_logic.knowledge.service.db2.expr.sym.Symbol)
			 */
			@Override
			public Void visitReference(Reference expr, Void arg) {

				if (expr.getAttribute().getHistoryType() == HistoryType.MIXED) {
					throw new IllegalArgumentException("Can not process queries that contain mixed references.");
				}
				return super.visitReference(expr, arg);
			}
		};

	/**
	 * Checks that no mixed references are contained..
	 */
	public static void checkNoMixedReferences(SetExpression expr) {
		expr.visitSetExpr(CHECK_NO_MIXED, null);
	}

}
