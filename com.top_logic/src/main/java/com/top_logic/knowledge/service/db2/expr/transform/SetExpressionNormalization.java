/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.SetExpression;

/**
 * Transform {@link SetExpression}s using canonical identities.
 * 
 * <ul>
 * <li>filter(filter(s, e1), e2) to filter(s, e1 and e2)</li>
 * <li>map(map(s, e1), e2) to map(s, eval(e1, e2))</li>
 * <li>in(all(T)) to hasType(T)</li>
 * <li>in(any(T)) to instanceOf(T)</li>
 * </ul>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetExpressionNormalization extends InlineExpressionTransformer<Void> {
	
	private static final Void none = null;

	/**
	 * Normalizes the given {@link SetExpression} using the
	 * {@link SetExpressionNormalization}.
	 * 
	 * @param expr
	 *        The Expression to normalize.
	 * @return A normalized copy of the given expression.
	 */
	public static SetExpression normalizeSets(SetExpression expr) {
		return expr.visitSetExpr(new SetExpressionNormalization(), none);
	}
	
	@Override
	public Expression visitInSet(InSet expr, Void arg) {
		SetExpression setExpr = expr.getSetExpr();
		if (setExpr instanceof AnyOf) {
			AnyOf any = (AnyOf) setExpr;
			Expression instanceOf = eval(expr.getContext(), instanceOf(any.getTypeName()));
			return instanceOf;
		}
		else if (setExpr instanceof AllOf) {
			AllOf all = (AllOf) setExpr;
			return eval(expr.getContext(), hasType(all.getTypeName()));
		}
		else {
			return super.visitInSet(expr, arg);
		}
	}
	
	@Override
	protected SetExpression process(Filter expr, SetExpression source, Expression filter) {
		if (source instanceof Filter) {
			Filter filterResult = (Filter) source;
			source = filterResult.getSource();
			filter = and(filterResult.getFilter(), filter);
		}
		else if (source instanceof MapTo) {
			if (false) {
				// filter(map(s1, e1), e2)) to map(filter(s1, eval(e1, e2)), e1)
				//
				// This transformation extracts maps from inside filters. This
				// does not work, because it prevents the concrete type
				// computation from finding good estimations for the map result
				// (only the filter expression is considered when pruning the
				// potential types of the result).
				MapTo sourceMap = (MapTo) source;
				Filter transformedFilter = (Filter) 
				filter(
					sourceMap.getSource(), 
					eval(ExpressionCopy.copy(sourceMap.getMapping()), filter));
				MapTo transformedMap = (MapTo)
				map(
					process(transformedFilter, transformedFilter.getSource(), transformedFilter.getFilter()), 
					sourceMap.getMapping());
				
				return process(transformedMap, transformedMap.getSource(), transformedMap.getMapping());
			}
		}
		return super.process(expr, source, filter);
	}
	
	@Override
	protected SetExpression process(MapTo expr, SetExpression source, Expression mapping) {
		if (source instanceof MapTo) {
			MapTo mapResult = (MapTo) source;
			source = mapResult.getSource();
			mapping = eval(mapResult.getMapping(), mapping);
		}
		return super.process(expr, source, mapping);
	}
	
}
