/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.UnaryOperation;

/**
 * The class {@link NullReferenceSimplifier} transforms a given {@link SetExpression} by simplifying
 * references which are checked to be <code>null</code>.
 * 
 * <p>
 * <ol>
 * <li>If an expression is of form <tt>isNull(item)</tt> where <tt>item</tt> is an expression that
 * represents an item, then the expression is transformed to <tt>isNull(identifier(item))</tt> to
 * check whether the identifier of the item is <tt>null</tt>.</li>
 * </ol>
 * </p>
 * 
 * @see NullReferenceSimplifier#simplifyNullReferences(SetExpression)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NullReferenceSimplifier extends InlineExpressionTransformer<Void> {

	/**
	 * Singleton {@link NullReferenceSimplifier} instance.
	 */
	public static final NullReferenceSimplifier INSTANCE = new NullReferenceSimplifier();

	private NullReferenceSimplifier() {
		// singleton instance
	}

	/**
	 * Main entry point to simplify {@link SetExpression} using {@link NullReferenceSimplifier}.
	 * 
	 * @param search
	 *        the set expression to transform.
	 * 
	 * @return The given {@link SetExpression} transformed by the {@link NullReferenceSimplifier}.
	 */
	public static SetExpression simplifyNullReferences(SetExpression search) {
		return search.visitSetExpr(INSTANCE, null);
	}

	@Override
	protected Expression process(UnaryOperation expr, Expression expression, Void arg) {
		switch (expr.getOperator()) {
			case IS_NULL:
				if (expr.getExpr().getPolymorphicType().getKind() == Kind.item) {
					/* A null reference is stored with 0 identifier. */
					return eqBinary(identifier(expression),
						literal(IdentifierUtil.nullIdForMandatoryDatabaseColumns()));
				} else {
					return super.process(expr, expression, arg);
				}
			default:
				return super.process(expr, expression, arg);
		}
	}

}

