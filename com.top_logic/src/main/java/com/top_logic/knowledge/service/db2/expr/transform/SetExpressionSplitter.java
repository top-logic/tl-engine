/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.SetExpression;

/**
 * BHU: This class
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetExpressionSplitter extends ExpressionTransformer<MetaObject> {

	/**
	 * Singleton {@link SetExpressionSplitter} instance.
	 */
	public static final SetExpressionSplitter INSTANCE = new SetExpressionSplitter();

	private SetExpressionSplitter() {
		// Singleton constructor.
	}
	
	static final Void none = null;

	public static SetExpression makeMonomorphicCopy(SetExpression expr, MetaObject expectedConcreteResultType) {
		assert expectedConcreteResultType != null;
		return expr.visitSetExpr(INSTANCE, expectedConcreteResultType);
	}
	
	@Override
	public SetExpression visitAllOf(AllOf expr, MetaObject expectedConcreteResultType) {
		if (expr.getConcreteTypes().contains(expectedConcreteResultType)) {
			return super.visitAllOf(expr, expectedConcreteResultType);
		} else {
			return none();
		}
	}
	
	@Override
	public SetExpression visitAnyOf(AnyOf expr, MetaObject expectedConcreteResultType) {
		if (expr.getConcreteTypes().contains(expectedConcreteResultType)) {
			return allOf((MOClass) expectedConcreteResultType);
		} else {
			return none();
		}
	}
	
	@Override
	public SetExpression visitFilter(Filter expr, MetaObject expectedConcreteResultType) {
		if (expr.getConcreteTypes().size() > 1) {
			SetExpression source = descendSet(expr, expr.getSource(), expectedConcreteResultType);
			Expression filter = descendExpr(expr, expr.getFilter(), expectedConcreteResultType);
			return filter(source, and(hasType(expectedConcreteResultType.getName()), filter));
		} else {
			return super.visitFilter(expr, expectedConcreteResultType);
		}
	}
	
	@Override
	public SetExpression visitMapTo(MapTo expr, MetaObject expectedConcreteResultType) {
		if (expr.getConcreteTypes().size() > 1) {
			SetExpression source = descendSet(expr, expr.getSource(), expectedConcreteResultType);
			Expression mapping = descendExpr(expr, expr.getMapping(), expectedConcreteResultType);
			return filter(map(source, mapping), hasType(expectedConcreteResultType.getName()));
		} else {
			return visitMapTo(expr, expectedConcreteResultType);
		}
	}
	
	@Override
	public SetExpression visitPartition(Partition expr, MetaObject expectedConcreteResultType) {
		//  TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected Expression processHasType(HasType expr, Expression context, MetaObject concreteContextType) {
		if (concreteContextType == null) {
			return super.processHasType(expr, context, concreteContextType);
		}
		else if (expr.getConcreteTypes().contains(concreteContextType)) {
			return literal(true);
		} else {
			return literal(false);
		}
	}
	
	@Override
	protected Expression processInstanceOf(InstanceOf expr, Expression context, MetaObject concreteContextType) {
		if (concreteContextType == null) {
			return super.processInstanceOf(expr, context, concreteContextType);
		}
		else if (expr.getConcreteTypes().contains(concreteContextType)) {
			return literal(true);
		} else {
			return literal(false);
		}
	}
	
	@Override
	public Expression visitInSet(InSet expr, MetaObject concreteContextType) {
		// The set must only be evaluated in the context type, because for all
		// other types, no match can be found.
		MetaObject expectedResultType = concreteContextType;
		
		if (expr.getSetExpr().getSymbol() != null) {
			// Do not try to make test set monomorphic, because having a symbol
			// is sufficient to generate the SQL comparison.
			return super.visitInSet(expr, null);
		} else {
			return super.visitInSet(expr, expectedResultType);
		}
	}
	
}
