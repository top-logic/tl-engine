/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultSetExpressionVisitor;

/**
 * {@link SetExpressionVisitor} that decomposes all top-level {@link Union}
 * expressions into a list of {@link SetExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnionDecomposition extends DefaultSetExpressionVisitor<Void, Void, List<SetExpression>> {
	
	private static Void none = null;

	/**
	 * Singleton {@link UnionDecomposition} instance.
	 */
	private static final UnionDecomposition INSTANCE = new UnionDecomposition();

	private UnionDecomposition() {
		// Singleton constructor.
	}
	
	public static List<SetExpression> decomposeUnions(SetExpression expr) {
		ArrayList<SetExpression> result = new ArrayList<>();
		expr.visitSetExpr(INSTANCE, result);
		return result;
	}
	
	@Override
	public Void visitUnion(Union expr, List<SetExpression> arg) {
		expr.getLeftExpr().visitSetExpr(this, arg);
		expr.getRightExpr().visitSetExpr(this, arg);
		return none;
	}
	
	@Override
	protected Void visitSetExpression(SetExpression expr, List<SetExpression> arg) {
		arg.add(expr);
		return none;
	}
	
}