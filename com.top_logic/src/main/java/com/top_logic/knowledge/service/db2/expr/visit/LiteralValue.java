/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.Literal;

/**
 * {@link ExpressionVisitor} that retrieves the literal value from a
 * {@link Literal} expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LiteralValue extends DefaultExpressionVisitor<Object, Void> {

	/**
	 * Singleton {@link LiteralValue} instance.
	 */
	public static final LiteralValue INSTANCE = new LiteralValue();

	private static Void none = null;

	private LiteralValue() {
		// Singleton constructor.
	}
	
	@Override
	public Object visitLiteral(Literal expr, Void arg) {
		return expr.getValue();
	}
	
	/**
	 * Whether the given expression is the <code>false</code> literal.
	 */
	public static boolean isLiteralFalse(Expression expr) {
		Object literalValue = getLiteralValue(expr);
		if (literalValue == null) {
			return false;
		} else {
			return ! ((Boolean) literalValue).booleanValue();
		}
	}

	/**
	 * Whether the given expression is a <code>true</code> literal.
	 */
	public static boolean isLiteralTrue(Expression expr) {
		Object literalValue = getLiteralValue(expr);
		if (literalValue == null) {
			return false;
		} else {
			return ((Boolean) literalValue).booleanValue();
		}
	}

	/**
	 * Whether the given expression represents a literal
	 */
	public static boolean isLiteral(Expression expression) {
		return expression instanceof Literal;
	}

	/**
	 * Whether the given literal has <code>null</code> value.
	 * 
	 * @param literal
	 *        expects that the given expression represents a literal.
	 */
	public static boolean isLiteralNull(Expression literal) {
		assert isLiteral(literal) : "Can only handle literal expressions";
		return getLiteralValue(literal) == null;
	}

	/**
	 * The literal value of the given {@link Literal} expression, or <code>null</code>, if the given
	 * expression is not a {@link Literal}.
	 */
	public static Object getLiteralValue(Expression expr) {
		return expr.visit(INSTANCE, none);
	}

}