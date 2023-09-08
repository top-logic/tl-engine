/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Expression;
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
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.visit.ObjectExpressionEvaluator.ExpressionContext;

/**
 * Visitor that checks whether the context object is contained in the visited {@link SetExpression}.
 * 
 * <p>
 * Result of the visit for {@link SetExpression}s: Whether the
 * {@link ExpressionContext#getContextObject() context object} is contained in the visited set.
 * </p>
 * 
 * @see SimpleExpressionEvaluator
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CheckSetContains implements SetExpressionVisitor<Boolean, ObjectExpressionEvaluator.ExpressionContext> {

	/** Singleton instance of {@link CheckSetContains} */
	public static final CheckSetContains IS_CONTAINED_INSTANCE = new CheckSetContains();

	private CheckSetContains() {
		// singleton instance
	}

	@Override
	public Boolean visitNone(None expr, ExpressionContext arg) {
		return Boolean.FALSE;
	}

	@Override
	public Boolean visitSetLiteral(SetLiteral expr, ExpressionContext arg) {
		return expr.getValues().contains(arg.getContextObject());
	}

	@Override
	public Boolean visitSetParameter(SetParameter expr, ExpressionContext arg) {
		Collection values = (Collection) arg.getParameterValue(expr.getName());
		return values.contains(arg.getContextObject());
	}

	@Override
	public Boolean visitAllOf(AllOf expr, ExpressionContext arg) {
		DataObject contextObject = (DataObject) arg.getContextObject();
		if (contextObject == null) {
			return false;
		}
		return contextObject.tTable().equals(expr.getDeclaredType());
	}

	@Override
	public Boolean visitAnyOf(AnyOf expr, ExpressionContext arg) {
		DataObject contextObject = (DataObject) arg.getContextObject();
		if (contextObject == null) {
			return false;
		}
		return contextObject.tTable().isSubtypeOf(expr.getDeclaredType());
	}

	@Override
	public Boolean visitSubstraction(Substraction expr, ExpressionContext arg) {
		Boolean inLeftSet = expr.getLeftExpr().visitSetExpr(this, arg);
		if (!inLeftSet) {
			return Boolean.FALSE;
		}
		return !expr.getRightExpr().visitSetExpr(this, arg);
	}

	@Override
	public Boolean visitIntersection(Intersection expr, ExpressionContext arg) {
		Boolean inLeftSet = expr.getLeftExpr().visitSetExpr(this, arg);
		if (!inLeftSet) {
			return Boolean.FALSE;
		}
		return expr.getRightExpr().visitSetExpr(this, arg);
	}

	@Override
	public Boolean visitUnion(Union expr, ExpressionContext arg) {
		Boolean inLeftSet = expr.getLeftExpr().visitSetExpr(this, arg);
		if (inLeftSet) {
			return Boolean.TRUE;
		}
		return expr.getRightExpr().visitSetExpr(this, arg);
	}

	@Override
	public Boolean visitCrossProduct(CrossProduct expr, ExpressionContext arg) {
		Tuple contextObject = (Tuple) arg.getContextObject();
		List<SetExpression> expressions = expr.getExpressions();
		if (expressions.size() != contextObject.size()) {
			String error =
				"Expression '" + expr + "' and context object '" + contextObject + "' have different sizes.";
			throw new IllegalStateException(error);
		}
		for (int i = 0; i < expressions.size(); i++) {
			SimpleExpressionEvaluator contextFactory = SimpleExpressionEvaluator.INSTANCE;
			ExpressionContext newContext = contextFactory.createContext(arg, contextObject.get(i));
			Boolean contains = expressions.get(i).visitSetExpr(this, newContext);
			if (!contains) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitFilter(Filter expr, ExpressionContext arg) {
		Boolean inLeftSet = expr.getSource().visitSetExpr(this, arg);
		if (!inLeftSet) {
			return Boolean.FALSE;
		}
		Map<String, ?> arguments = arg.getParameterValues();
		DataObject contextObject = (DataObject) arg.getContextObject();
		Expression filterExpression = expr.getFilter();
		return SimpleExpressionEvaluator.matches(filterExpression, contextObject, arguments);
	}

	@Override
	public Boolean visitMapTo(MapTo expr, ExpressionContext arg) {
		throw new UnsupportedOperationException("Can not find sources which maps to the given context object.");
	}

	@Override
	public Boolean visitPartition(Partition expr, ExpressionContext arg) {
		throw new UnsupportedOperationException("To be implemented.");
	}

}
