/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.service.db2.expr.visit.DescendingExpressionVisitor;

/**
 * BHU: This class
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionSplitter extends ExpressionTransformer<Void> {

	/**
	 * Singleton {@link ExpressionSplitter} instance.
	 */
	public static final ExpressionSplitter INSTANCE = new ExpressionSplitter();

	private ExpressionSplitter() {
		// Singleton constructor.
	}
	
	static final Void none = null;

	public static SetExpression makeTableAccessUnique(SetExpression expr) {
		return expr.visitSetExpr(INSTANCE, none);
	}
	
	@Override
	public SetExpression visitFilter(Filter expr, Void arg) {
		SetExpression source = expr.getSource();
		Expression filter = expr.getFilter();
		
		Set<? extends MetaObject> pendingSplits = ExpressionMonomorpher.monomorphAccesses(filter);
		
		SetExpression sourceCopy = descendSet(expr, source, arg);
		if (pendingSplits.size() > 1) {
			return process(expr, sourceCopy, ExpressionMonomorpher.split(pendingSplits, filter));
		} else {
			return process(expr, sourceCopy, filter);
		}
	}
	
	@Override
	public SetExpression visitMapTo(MapTo expr, Void arg) {
		Expression mapping = expr.getMapping();
		Set<? extends MetaObject> pendingSourceSplits = ExpressionMonomorpher.monomorphAccesses(mapping);
		
		SetExpression transformedMap;
		if (pendingSourceSplits.size() > 1) {
			Set<? extends MetaObject> concreteSourceTypes = expr.getSource().getConcreteTypes();
			
			SetExpression sourceCopy = expr.getSource().visitSetExpr(this, none);
			
			transformedMap = null;
			for (Iterator<? extends MetaObject> it = concreteSourceTypes.iterator(); it.hasNext();) {
				MetaObject concreteSourceType = it.next();
				
				Expression mappingCopy = ExpressionCopy.copy(mapping);
				SetExpression monomorphicSourceCopy = filter(ExpressionCopy.copy(sourceCopy), hasType(concreteSourceType.getName()));
				
				SetExpression mapCopy = map(monomorphicSourceCopy, mappingCopy);
				if (transformedMap == null) {
					transformedMap = mapCopy;
				} else {
					transformedMap = union(transformedMap, mapCopy);
				}
			}
		} else {
			transformedMap = super.visitMapTo(expr, arg);
		}
			
		return transformedMap;
	}
	
	@Override
	public SetExpression visitPartition(Partition expr, Void arg) {
		//  TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}
		
	private static final class ExpressionMonomorpher extends DescendingExpressionVisitor<Set<MetaObject>, Void, Void> {
		
		/**
		 * Singleton {@link ExpressionMonomorpher} instance.
		 */
		private static final ExpressionMonomorpher MONOMORPHER_INSTANCE = new ExpressionMonomorpher();

		private ExpressionMonomorpher() {
			// Singleton constructor.
		}

		/**
		 * Monomorphes the given {@link Expression} by inspecting the potential context types of the
		 * given expression and splits into many monomorphic expressions by setting
		 * <code>and(hasType(X), expr)</code> where <code>X</code> is a potential context type and
		 * <code>expr</code> is the expression with polymorphic context type.
		 * 
		 * @param expr
		 *        the expression to monomorph
		 * 
		 * @return the set context types which could not be eliminated. May contain only one element
		 *         in which case the expression is monomorph.
		 */
		public static Set<? extends MetaObject> monomorphAccesses(Expression expr) {
			return expr.visit(ExpressionMonomorpher.MONOMORPHER_INSTANCE, none);
		}

		@Override
		protected Set<MetaObject> processBinary(BinaryOperation expr, Set<MetaObject> left,
				Set<MetaObject> right, Void arg) {
			switch (expr.getOperator()) {
			case AND:
			case OR: {
				if (left.size() > 1) {
					expr.setLeft(split(left, expr.getLeft()));
				}
				if (right.size() > 1) {
					expr.setRight(split(right, expr.getRight()));
				}
				return Collections.emptySet();
			}
				
			default:
				return CollectionUtil.union2(left, right);
			}
		}

		@Override
		protected Set<MetaObject> process(UnaryOperation expr, Set<MetaObject> expression, Void arg) {
			switch (expr.getOperator()) {
				case IS_NULL:
				case NOT: {
					if (expression.size() > 1) {
						expr.setExpr(split(expression, expr.getExpr()));
					}
					return Collections.emptySet();
				}
				case BRANCH:
				case REVISION:
				case HISTORY_CONTEXT:
				case IDENTIFIER:
				case TYPE_NAME:
					return expression;
				default:
					throw Operator.noSuchOperator(expr.getOperator());
			}
		}

		/**
		 * Creates an expression that results in <code>true</code> iff there is a type in the given
		 * <code>concreteTypes</code> such that the given <code>pattern</code> evaluated in that
		 * type results in <code>true</code>.
		 * 
		 * 
		 * @param concreteTypes
		 *        the types in which the pattern may be evaluated. must not be <code>null</code>
		 * @param pattern
		 *        the expression to evaluate
		 * 
		 * @return the described expression
		 */
		static Expression split(Set<? extends MetaObject> concreteTypes, Expression pattern) {
			Iterator<? extends MetaObject> it = concreteTypes.iterator();
			Expression result = and(hasType(it.next().getName()), pattern);
			while (it.hasNext()) {
				result = or(result, and(hasType(it.next().getName()), ExpressionCopy.copy(pattern)));
			}
			return result;
		}
		

		@Override
		protected Set<MetaObject> process(Eval orig, Set<MetaObject> context, Set<MetaObject> expression) {
			return context;
		}

		@Override
		protected Set<MetaObject> process(Matches expr, Set<MetaObject> expression) {
			return expression;
		}

		@Override
		protected Set<MetaObject> process(ExpressionTuple orig, List<Set<MetaObject>> entries) {
			Set<MetaObject> result = entries.get(0);
			for (int n = 1, cnt = entries.size(); n < cnt; n++) {
				result = CollectionUtil.union2(result, entries.get(n));
			}
			return result;
		}

		@Override
		protected Set<MetaObject> processAttribute(Attribute attribute, Set<MetaObject> context, Void arg) {
			return attribute.getContext().getConcreteTypes();
		}

		@Override
		public Set<MetaObject> visitContext(ContextAccess expr, Void arg) {
			return Collections.emptySet();
		}

		@Override
		protected Set<MetaObject> processFlex(Flex expr, Set<MetaObject> context, Void arg) {
			return Collections.emptySet();
		}

		@Override
		protected Set<MetaObject> processIsCurrent(IsCurrent expr, Set<MetaObject> context, Void arg) {
			return context;
		}

		@Override
		protected Set<MetaObject> processGetEntry(GetEntry getEntry, Set<MetaObject> context, Void arg) {
			return Collections.emptySet();
		}

		@Override
		protected Set<MetaObject> processHasType(HasType expr, Set<MetaObject> context, Void arg) {
			return Collections.emptySet();
		}

		@Override
		protected Set<MetaObject> processInstanceOf(InstanceOf expr, Set<MetaObject> context, Void arg) {
			return Collections.emptySet();
		}

		@Override
		public Set<MetaObject> visitLiteral(Literal expr, Void arg) {
			return Collections.emptySet();
		}
		
		@Override
		public Set<MetaObject> visitParameter(Parameter expr, Void arg) {
			return Collections.emptySet();
		}

		@Override
		public Set<MetaObject> visitRequestedHistoryContext(RequestedHistoryContext expr, Void arg) {
			return Collections.emptySet();
		}

		@Override
		protected Set<MetaObject> processReference(Reference reference, Set<MetaObject> context, Void arg) {
			return reference.getContext().getConcreteTypes();
		}
		
		@Override
		public Set<MetaObject> visitInSet(InSet expr, Void arg) {
			expr.setSetExpr(makeTableAccessUnique(expr.getSetExpr()));
			return Collections.emptySet();
		}
		
		@Override
		protected Set<MetaObject> process(InSet orig, Set<MetaObject> expr, Void set) {
			throw new UnreachableAssertion("visitInSet does not descend.");
		}

		@Override
		protected Void descendSet(QueryPart parent, SetExpression expr, Void arg) {
			throw new UnreachableAssertion("visitInSet does not descend.");
		}
		
	}

}
