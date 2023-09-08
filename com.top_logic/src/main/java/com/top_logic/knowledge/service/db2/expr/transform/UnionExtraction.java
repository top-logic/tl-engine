/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.service.db2.expr.meta.MetaExpressionFactory.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.meta.MetaSet;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultSetExpressionVisitor;

/**
 * Pulls up all {@link Union} operations to the top of the {@link SetExpression}
 * tree.
 * 
 * <p>
 * Transform {@link InSet} operation with union sets to <code>or</code>
 * operations of {@link InSet}s.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnionExtraction extends InlineExpressionTransformer<SimpleSetTemplate> {

	/**
	 * {@link SetExpressionVisitor} that creates an {@link Expression} which expresses the
	 * containment of the argument {@link Expression} in the visited {@link SetExpression}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class InsetBuilder extends DefaultSetExpressionVisitor<Expression, Void, Expression> {
		
		/**
		 * Singleton {@link InsetBuilder} instance.
		 */
		public static final InsetBuilder INSET_INSTANCE = new InsetBuilder();

		private InsetBuilder() {
			// Singleton constructor.
		}
		
		@Override
		public Expression visitUnion(Union expr, Expression testExpr) {
			return or(expr.getLeftExpr().visitSetExpr(this, testExpr), expr.getRightExpr().visitSetExpr(this, testExpr));
		}
		
		@Override
		protected Expression visitSetExpression(SetExpression expr, Expression testExpr) {
			return inSet(testExpr, expr);
		}

	}

	/**
	 * Singleton {@link UnionExtraction} instance.
	 */
	public static final UnionExtraction INSTANCE = new UnionExtraction();
	
	private static final Void none = null;
	
	private UnionExtraction() {
		// Singleton constructor.
	}
	
	/**
	 * Pulls up all {@link Union} operations to the top of the {@link SetExpression}.
	 */
	public static SetExpression extractUnions(SetExpression expr) {
		UnionExtraction transformer = new UnionExtraction();
		return expr.visitSetExpr(transformer, new SimpleSetTemplate());
	}
	
	@Override
	public SetExpression visitFilter(Filter expr, SimpleSetTemplate currentVar) {
		SetExpression filterSource = expr.getSource();
		Expression filterExpr = descendExpr(expr, expr.getFilter());
		
		MetaSet newVar = newVar();
		currentVar.expandVar(filter(newVar, filterExpr), newVar);
		
		return descendSet(expr, filterSource, currentVar);
	}

	@Override
	public SetExpression visitMapTo(MapTo expr, SimpleSetTemplate currentVar) {
		Expression mappingExpr = descendExpr(expr, expr.getMapping());
		SetExpression mapSource = expr.getSource();
		
		MetaSet newVar = newVar();
		currentVar.expandVar(map(newVar, mappingExpr), newVar);
		
		return descendSet(expr, mapSource, currentVar);
	}
	
	@Override
	public Expression visitInSet(InSet expr, SimpleSetTemplate arg) {
		Expression transformedExpr = descendExpr(expr, expr.getContext());
		SetExpression set = descendSet(expr, expr.getSetExpr(), new SimpleSetTemplate());
		return process(expr, transformedExpr, set);
	}
	
	@Override
	protected Expression process(InSet orig, Expression expr, SetExpression set) {
		return set.visitSetExpr(InsetBuilder.INSET_INSTANCE, expr);
	}
	
	// Terminal symbols fill the template variable with a copy of themselves and return the expansion. 
	
	@Override
	public SetExpression visitAllOf(AllOf expr, SimpleSetTemplate currentVar) {
		currentVar.expandVar(super.visitAllOf(expr, currentVar), null);
		
		return currentVar.getExpansion();
	}
	
	@Override
	public SetExpression visitAnyOf(AnyOf expr, SimpleSetTemplate currentVar) {
		currentVar.expandVar(super.visitAnyOf(expr, currentVar), null);
		
		return currentVar.getExpansion();
	}
	
	@Override
	public SetExpression visitSetLiteral(SetLiteral expr, SimpleSetTemplate currentVar) {
		currentVar.expandVar(super.visitSetLiteral(expr, currentVar), null);
		return currentVar.getExpansion();
	}
	
	@Override
	public SetExpression visitNone(None expr, SimpleSetTemplate currentVar) {
		// Drop operation of the empty set, since the result is always empty.
		return expr;
	}
	
	// Descenders that require to clone the template.
	
	@Override
	public SetExpression visitIntersection(Intersection expr, SimpleSetTemplate arg) {
		// Create clone of template for all but the last descending.
		SetExpression left = descendSet(expr, expr.getLeftExpr(), arg.clone());
		SetExpression right = descendSet(expr, expr.getRightExpr(), arg);
		return process(expr, left, right);
	}
	
	@Override
	public SetExpression visitSubstraction(Substraction expr, SimpleSetTemplate arg) {
		// Create clone of template for all but the last descending.
		SetExpression left = descendSet(expr, expr.getLeftExpr(), arg.clone());
		SetExpression right = descendSet(expr, expr.getRightExpr(), arg);
		return process(expr, left, right);
	}
	
	@Override
	public SetExpression visitUnion(Union expr, SimpleSetTemplate arg) {
		// Create clone of template for all but the last descending.
		SetExpression left = descendSet(expr, expr.getLeftExpr(), arg.clone());
		SetExpression right = descendSet(expr, expr.getRightExpr(), arg);
		return process(expr, left, right);
	}
	
	@Override
	public List<SetExpression> descendSets(QueryPart parent, List<SetExpression> expressions, SimpleSetTemplate arg) {
		// Create clone of template for all but the last descending.
		ArrayList<SetExpression> result = new ArrayList<>(expressions.size());
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			SetExpression expr = expressions.get(n);
			result.add(descendSet(parent, expr, (n < (cnt - 1)) ? arg.clone() : arg));
		}
		return result;
	}
	
	
	// In all other cases, regular descending is sufficient.

	private Expression descendExpr(QueryPart parent, Expression expr) {
		return descendExpr(parent, expr, null);
	}
	
	@Override
	protected Expression descendExpr(QueryPart parent, Expression expr, SimpleSetTemplate arg) {
		return super.descendExpr(parent, expr, null);
	}

	private static MetaSet newVar() {
		return metaSet(null);
	}

}
