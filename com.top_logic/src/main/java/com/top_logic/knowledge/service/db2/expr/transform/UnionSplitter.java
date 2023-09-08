/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultSetExpressionVisitor;

/**
 * BHU: This class
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UnionSplitter extends DefaultSetExpressionVisitor<Void, Void, Map<MetaObject, SetExpression>> {

	public static class FlatQueryCopy extends ExpressionTransformer<SetExpression> {

		/**
		 * Singleton {@link FlatQueryCopy} instance.
		 */
		private static final FlatQueryCopy INSTANCE = new FlatQueryCopy();

		private FlatQueryCopy() {
			// Singleton constructor.
		}
		
		@SuppressWarnings("unchecked")
		public static <Q extends AbstractQuery> Q copyQuery(Q query, SetExpression search) {
			return (Q) query.visitQuery(FlatQueryCopy.INSTANCE, search);
		}
		
		@Override
		protected SetExpression descendSet(QueryPart parent, SetExpression expr, SetExpression arg) {
			return arg;
		}
	}

	private static final Void none = null;
	private final ExpressionCompileProtocol log;

	private UnionSplitter(ExpressionCompileProtocol log) {
		this.log = log;
	}
	
	public static <Q extends AbstractQuery> Map<MetaObject, Q> splitUnions(ExpressionCompileProtocol log, Q query) {
		HashMap<MetaObject, SetExpression> setExprs = new HashMap<>();
		query.getSearch().visitSetExpr(new UnionSplitter(log), setExprs);
		
		HashMap<MetaObject, Q> result = new HashMap<>();
		for (Entry<MetaObject, SetExpression> entry : setExprs.entrySet()) {
			result.put(entry.getKey(), FlatQueryCopy.copyQuery(query, entry.getValue()));
		}
		
		return result;
	}

	@Override
	public Void visitUnion(Union expr, Map<MetaObject, SetExpression> arg) {
		expr.getLeftExpr().visitSetExpr(this, arg);
		expr.getRightExpr().visitSetExpr(this, arg);
		return none;
	}
	
	@Override
	protected Void visitSetExpression(SetExpression expr, Map<MetaObject, SetExpression> arg) {
		Set<? extends MetaObject> concreteTypes = expr.getConcreteTypes();
		
		if (concreteTypes.size() > 1) {
			for (MetaObject concreteType : concreteTypes) {
				SetExpression monomrphicExpr = filter(ExpressionCopy.copy(expr), hasType(concreteType.getName()));
				add(arg, concreteType, monomrphicExpr);
			}
		} else if (concreteTypes.size() == 1) {
			add(arg, concreteTypes.iterator().next(), expr);
		}
		
		return none;
	}

	private void add(Map<MetaObject, SetExpression> arg, MetaObject type, SetExpression expr) {
		SetExpression unionExpr = arg.get(type);
		if (unionExpr == null) {
			arg.put(type, expr);
		} else {
			SetExpression newUnionExpr = union(unionExpr, expr);

			// Preserve type information.
			newUnionExpr.setPolymorphicType(unionExpr.getPolymorphicType());
			newUnionExpr.setConcreteTypes(unionExpr.getConcreteTypes());
			
			arg.put(type, newUnionExpr);
		}
	}

}
