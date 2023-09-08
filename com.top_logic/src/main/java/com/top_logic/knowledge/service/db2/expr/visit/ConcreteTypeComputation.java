/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOTuple;
import com.top_logic.dob.meta.MOTupleImpl;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.CountFunction;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.MaxFunction;
import com.top_logic.knowledge.search.MinFunction;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.QueryVisitor;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.SumFunction;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;

/**
 * Compute the concrete return/content types of expressions.
 * 
 * <p>
 * Argument of the visit: Set of concrete context types in which the expression will potentially be
 * evaluated.
 * </p>
 * 
 * <p>
 * Result of the visit for {@link SetExpression}: Set of concrete types of elements of the set.
 * </p>
 * 
 * <p>
 * Result of the visit for boolean {@link Expression}s: Set of concrete context types for which the
 * expression may be evaluated and return <code>true</code>.
 * </p>
 * 
 * <p>
 * Result of the visit for non-boolean {@link Expression}s: Set of concrete context types for which
 * the expression may be evaluated (equal to the argument of the visit).
 * </p>
 * 
 * <p>
 * After the visit, {@link QueryPart#getConcreteTypes()} is assigned to the set of concrete types to
 * witch the {@link Expression} may be evaluated or the {@link SetExpression} may contain elements.
 * </p>
 * 
 * @see QueryPart#getConcreteTypes()
 * @see PolymorphicTypeComputation
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConcreteTypeComputation
		implements
		QueryVisitor<Set<MetaObject>, Set<MetaObject>, Set<MetaObject>, Set<MetaObject>, Set<MetaObject>, Set<MetaObject>> {

	private static final Set<MetaObject> BOOLEAN = Collections.<MetaObject> singleton(MOPrimitive.BOOLEAN);

	private static final Set<MetaObject> LONG = Collections.<MetaObject> singleton(MOPrimitive.LONG);

	private static final Set<MetaObject> none = null;

	private final ExpressionCompileProtocol log;

	private final TypeSystem typeSystem;

	/**
	 * Applies a {@link ConcreteTypeComputation} to the given expression.
	 * 
	 * @param typeSystem
	 *        the {@link TypeSystem} to resolve subtype information from.
	 * @param log
	 *        The {@link Protocol} to report errors to.
	 * @param expr
	 *        The expression to compute concrete type information for.
	 */
	public static void computeTypes(TypeSystem typeSystem, ExpressionCompileProtocol log, QueryPart expr) {
		Set<MetaObject> item = Collections.<MetaObject> singleton(typeSystem.getItemType());
		expr.visitQuery(new ConcreteTypeComputation(typeSystem, log), item);
	}

	private ConcreteTypeComputation(TypeSystem typeSystem, ExpressionCompileProtocol log) {
		this.typeSystem = typeSystem;
		this.log = log;
	}

	@Override
	public Set<MetaObject> visitInstanceOf(InstanceOf expr, Set<MetaObject> evaluationTypes) {
		Set<MetaObject> contextTypes = descendContext(expr, evaluationTypes);
		Set<MetaObject> concreteTrueTypes = getConcreteSubtypes(expr, expr.getDeclaredType());
		expr.setConcreteTrueTypes(concreteTrueTypes);
		Set<MetaObject> concreteMatchTypes = CollectionUtil.intersection(contextTypes, concreteTrueTypes);
		expr.setConcreteTypes(BOOLEAN);
		return concreteMatchTypes;
	}

	@Override
	public Set<MetaObject> visitHasType(HasType expr, Set<MetaObject> evaluationTypes) {
		Set<MetaObject> contextTypes = descendContext(expr, evaluationTypes);
		MetaObject declaredType = expr.getDeclaredType();
		Set<MetaObject> concreteTrueType = Collections.singleton(declaredType);
		expr.setConcreteTrueTypes(concreteTrueType);
		Set<MetaObject> concreteMatchTypes;
		if (contextTypes.contains(declaredType)) {
			concreteMatchTypes = concreteTrueType;
		} else {
			concreteMatchTypes = Collections.emptySet();
		}
		expr.setConcreteTypes(BOOLEAN);
		return concreteMatchTypes;
	}

	@Override
	public Set<MetaObject> visitBinaryOperation(BinaryOperation expr, Set<MetaObject> evaluationTypes) {
		switch (expr.getOperator()) {
		case OR: {
				Set<MetaObject> leftContextType = expr.getLeft().visit(this, evaluationTypes);
				Set<MetaObject> rightContextType = expr.getRight().visit(this, evaluationTypes);

			Set<MetaObject> potentialTrueTypes = CollectionUtil.union(leftContextType, rightContextType);
			expr.setConcreteTypes(BOOLEAN);
				return potentialTrueTypes;
		}
		case AND: {
				Set<MetaObject> leftContextType = expr.getLeft().visit(this, evaluationTypes);
				Set<MetaObject> rightContextType = expr.getRight().visit(this, leftContextType);
				Set<MetaObject> potentialTrueTypes = CollectionUtil.intersection(leftContextType, rightContextType);
			expr.setConcreteTypes(BOOLEAN);
				return potentialTrueTypes;
		}
		
		case EQBINARY:
		case EQCI:
		case GE: 
		case GT: 
		case LE: 
		case LT: {
			expr.getLeft().visit(this, evaluationTypes);
			expr.getRight().visit(this, evaluationTypes);
			expr.setConcreteTypes(BOOLEAN);
				return evaluationTypes;
		}

		default: 
			throw new AssertionError("Unexpected symbol: " + expr.getOperator());
		}
	}

	@Override
	public Set<MetaObject> visitUnaryOperation(UnaryOperation expr, Set<MetaObject> evaluationTypes) {
		switch (expr.getOperator()) {
			case NOT: {
				Expression negatedExpr = expr.getExpr();
				Set<MetaObject> excludedTypes = negatedExpr.visit(this, evaluationTypes);
				Set<MetaObject> potentialTrueTypes =
					(negatedExpr instanceof InstanceOf || negatedExpr instanceof HasType) ?
						CollectionUtil.difference(evaluationTypes, excludedTypes) :
						evaluationTypes;
				expr.setConcreteTypes(BOOLEAN);
				return potentialTrueTypes;
			}
			case IS_NULL: {
				expr.getExpr().visit(this, evaluationTypes);
				expr.setConcreteTypes(BOOLEAN);
				return evaluationTypes;
			}
			case BRANCH: {
				expr.getExpr().visit(this, evaluationTypes);
				expr.setConcreteTypes(Collections.<MetaObject> singleton(IdentifierTypes.BRANCH_REFERENCE_MO_TYPE));
				return evaluationTypes;
			}
			case REVISION:
			case HISTORY_CONTEXT: {
				expr.getExpr().visit(this, evaluationTypes);
				expr.setConcreteTypes(Collections.<MetaObject> singleton(IdentifierTypes.REVISION_REFERENCE_MO_TYPE));
				return evaluationTypes;
			}
			case IDENTIFIER: {
				expr.getExpr().visit(this, evaluationTypes);
				expr.setConcreteTypes(Collections.<MetaObject> singleton(IdentifierTypes.REFERENCE_MO_TYPE));
				return evaluationTypes;
			}
			case TYPE_NAME: {
				expr.getExpr().visit(this, evaluationTypes);
				expr.setConcreteTypes(Collections.<MetaObject> singleton(IdentifierTypes.TYPE_REFERENCE_MO_TYPE));
				return evaluationTypes;
			}
			default: {
				throw Operator.noSuchOperator(expr.getOperator());
			}
		}
	}

	@Override
	public Set<MetaObject> visitAttribute(Attribute expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		expr.setConcreteTypes(getConcreteSubtypes(expr, expr.getAttribute().getMetaObject()));
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitReference(Reference expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		final ReferencePart accessPart = expr.getAccessType();
		final MOReference attribute = expr.getAttribute();
		final MetaObject type;
		Set<MetaObject> concreteSubtypes;
		if (accessPart == null) {
			type = attribute.getMetaObject();
			if (attribute.isMonomorphic()) {
				concreteSubtypes = Collections.singleton(type);
			} else {
				concreteSubtypes = getConcreteSubtypes(expr, type);
			}
		} else {
			type = attribute.getType(accessPart);
			concreteSubtypes = getConcreteSubtypes(expr, type);
		}
		expr.setConcreteTypes(concreteSubtypes);
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitEval(Eval expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		expr.getExpr().visit(this, expr.getContext().getConcreteTypes());
		expr.setConcreteTypes(expr.getExpr().getConcreteTypes());
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitContext(ContextAccess expr, Set<MetaObject> evaluationTypes) {
		expr.setConcreteTypes(evaluationTypes);
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitFlex(Flex expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		expr.setConcreteTypes(getConcreteSubtypes(expr, expr.getDeclaredType()));
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitGetEntry(GetEntry expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		int index = expr.getIndex();
		
		Set<MetaObject> entryTypes = new HashSet<>();
		for (MetaObject evaluationType : evaluationTypes) {
			entryTypes.add(((MOTuple) evaluationType).getEntryTypes().get(index));
		}
		expr.setConcreteTypes(entryTypes);
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitInSet(InSet expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		expr.getSetExpr().visitSetExpr(this, none);
		expr.setConcreteTypes(BOOLEAN);
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitLiteral(Literal expr, Set<MetaObject> evaluationTypes) {
		expr.setConcreteTypes(getConcreteSubtypes(expr, expr.getPolymorphicType()));
		return evaluationTypes;
	}
	
	@Override
	public Set<MetaObject> visitParameter(Parameter expr, Set<MetaObject> evaluationTypes) {
		expr.setConcreteTypes(getConcreteSubtypes(expr, expr.getPolymorphicType()));
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitMatches(Matches expr, Set<MetaObject> evaluationTypes) {
		expr.getExpr().visit(this, evaluationTypes);
		expr.setConcreteTypes(BOOLEAN);
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitTuple(ExpressionTuple expr, Set<MetaObject> evaluationTypes) {
		List<Expression> expressions = expr.getExpressions();
		for (Expression entryExpr : expressions) {
			entryExpr.visit(this, evaluationTypes);
		}
		
		computeConcretTypes: 
		{
			List<MetaObject> concreteEntryTypes = new ArrayList<>();
			for (Expression entryExpr : expressions) {
				Set<MetaObject> entryTypes = entryExpr.getConcreteTypes();
				if (entryTypes.size() > 1) {
					error(entryExpr, "Polymorphic tuple types are not supported.");
				}
				
				if (entryTypes.size() == 0) {
					// No concrete types of a certain entry mean that there is
					// no concrete tuple type for this expression.
					Set<MetaObject> emptyType = Collections.emptySet();
					expr.setConcreteTypes(emptyType);
					break computeConcretTypes;
				}
				
				concreteEntryTypes.add(entryTypes.iterator().next());
			}
			expr.setConcreteTypes(Collections.<MetaObject> singleton(new MOTupleImpl(concreteEntryTypes)));
		}
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitIsCurrent(IsCurrent expr, Set<MetaObject> evaluationTypes) {
		descendContext(expr, evaluationTypes);
		expr.setConcreteTypes(BOOLEAN);
		return evaluationTypes;
	}

	@Override
	public Set<MetaObject> visitRequestedHistoryContext(RequestedHistoryContext expr, Set<MetaObject> evaluationTypes) {
		expr.setConcreteTypes(LONG);
		return evaluationTypes;
	}

	private Set<MetaObject> descendContext(ContextExpression expr, Set<MetaObject> evaluationTypes) {
		return expr.getContext().visit(this, evaluationTypes);
	}

	// SetExpressionVisitor implementation.

	@Override
	public Set<MetaObject> visitAllOf(AllOf expr, Set<MetaObject> arg) {
		return assignSet(expr, Collections.<MetaObject> singleton(expr.getPolymorphicType()));
	}

	@Override
	public Set<MetaObject> visitAnyOf(AnyOf expr, Set<MetaObject> arg) {
		return assignSet(expr, new HashSet<>(typeSystem.getConcreteSubtypes(expr.getPolymorphicType())));
	}

	@Override
	public Set<MetaObject> visitCrossProduct(CrossProduct expr, Set<MetaObject> arg) {
		List<SetExpression> expressions = expr.getExpressions();
		ArrayList<MetaObject> entryTypes = new ArrayList<>();

		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			Set<MetaObject> entryResult = expressions.get(n).visitSetExpr(this, arg);

			if (entryResult.size() > 1) {
				error(expr, "No polymorphic cross product expressions supported: ");
			}

			if (entryResult.size() == 0) {
				// Some error happened before.
				Set<MetaObject> result = Collections.emptySet();
				return assignSet(expr, result);
			}

			entryTypes.add(entryResult.iterator().next());
		}

		return assignSet(expr, Collections.<MetaObject> singleton(new MOTupleImpl(entryTypes)));
	}

	@Override
	public Set<MetaObject> visitFilter(Filter expr, Set<MetaObject> arg) {
		Set<MetaObject> exprContextTypes = expr.getSource().visitSetExpr(this, arg);
		Set<MetaObject> filteredTypes = expr.getFilter().visit(this, exprContextTypes);

		return assignSet(expr, CollectionUtil.intersection(exprContextTypes, filteredTypes));
	}

	@Override
	public Set<MetaObject> visitMapTo(MapTo expr, Set<MetaObject> arg) {
		Set<MetaObject> functionContextTypes = expr.getSource().visitSetExpr(this, arg);
		Expression mapping = expr.getMapping();
		mapping.visit(this, functionContextTypes);
		return assignSet(expr, mapping.getConcreteTypes());
	}

	/**
	 * TODO: Move handling of tuple types to the {@link TypeSystem}.
	 * 
	 * @see TypeSystem#getConcreteSubtypes(MetaObject)
	 */
	private Set<MetaObject> getConcreteSubtypes(QueryPart expr, MetaObject polymorphicType) {
		if (MetaObjectUtils.isItemType(polymorphicType)) {
			return new HashSet<>(typeSystem.getConcreteSubtypes(polymorphicType));
		} else if (polymorphicType instanceof MOTuple) {
			List<MetaObject> entryTypes = ((MOTuple) polymorphicType).getEntryTypes();
			for (int n = 0, cnt = entryTypes.size(); n < cnt; n++) {

				Set<MetaObject> entryResult = getConcreteSubtypes(expr, entryTypes.get(n));

				if (entryResult.size() > 1) {
					error(expr, "No polymorphic cross product expressions supported: ");
				}

				if (entryResult.size() == 0) {
					// Some error happened before.
					return Collections.emptySet();
				}

				entryTypes.add(entryResult.iterator().next());
			}

			return Collections.<MetaObject> singleton(new MOTupleImpl(entryTypes));
		} else {
			return Collections.singleton(polymorphicType);
		}
	}

	@Override
	public Set<MetaObject> visitIntersection(Intersection expr, Set<MetaObject> arg) {
		Set<MetaObject> leftTypes = expr.getLeftExpr().visitSetExpr(this, arg);
		Set<MetaObject> rightTypes = expr.getRightExpr().visitSetExpr(this, arg);

		return assignSet(expr, CollectionUtil.intersection(leftTypes, rightTypes));
	}

	@Override
	public Set<MetaObject> visitSubstraction(Substraction expr, Set<MetaObject> arg) {
		Set<MetaObject> leftTypes = expr.getLeftExpr().visitSetExpr(this, arg);
		expr.getRightExpr().visitSetExpr(this, arg);

		return assignSet(expr, leftTypes);
	}

	@Override
	public Set<MetaObject> visitUnion(Union expr, Set<MetaObject> arg) {
		Set<MetaObject> leftTypes = expr.getLeftExpr().visitSetExpr(this, arg);
		Set<MetaObject> rightTypes = expr.getRightExpr().visitSetExpr(this, arg);

		return assignSet(expr, CollectionUtil.union2(leftTypes, rightTypes));
	}

	@Override
	public Set<MetaObject> visitNone(None expr, Set<MetaObject> arg) {
		return assignSet(expr, Collections.<MetaObject> emptySet());
	}

	@Override
	public Set<MetaObject> visitPartition(Partition expr, Set<MetaObject> arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MetaObject> visitSetLiteral(SetLiteral expr, Set<MetaObject> arg) {
		Collection<? extends Object> values = expr.getValues();
		Set<MetaObject> result = new HashSet<>();
		for (Object value : values) {
			result.add(PolymorphicTypeComputation.getLiteralType(value));
		}
		return assignSet(expr, result);
	}

	@Override
	public Set<MetaObject> visitSetParameter(SetParameter expr, Set<MetaObject> arg) {
		return assignSet(expr, getConcreteSubtypes(expr, expr.getPolymorphicType()));
	}

	private Set<MetaObject> assignSet(SetExpression expr, Set<MetaObject> concreteTypes) {
		expr.setConcreteTypes(concreteTypes);
		return concreteTypes;
	}
	
	private void error(QueryPart expr, String message, Object... args) {
		log.error(expr, message, args);
	}

	@Override
	public Set<MetaObject> visitHistoryQuery(HistoryQuery expr, Set<MetaObject> arg) {
		return visitAbstractQuery(expr, arg);
	}

	@Override
	public Set<MetaObject> visitRevisionQuery(RevisionQuery<?> expr, Set<MetaObject> arg) {
		Set<MetaObject> concreteTypes = visitAbstractQuery(expr, arg);
		Order order = expr.getOrder();
		if (order != null) {
			order.visitQuery(this, concreteTypes);
		}
		return concreteTypes;
	}

	private Set<MetaObject> visitAbstractQuery(AbstractQuery<?> expr, Set<MetaObject> arg) {
		Set<MetaObject> concreteTypes = expr.getSearch().visitSetExpr(this, arg);
		for (ParameterDeclaration decl : expr.getSearchParams()) {
			decl.visitQuery(this, none);
		}
		return concreteTypes;
	}

	@Override
	public Set<MetaObject> visitParameterDeclaration(ParameterDeclaration expr, Set<MetaObject> arg) {
		// ParameterDeclaration have no types
		return none;
	}

	@Override
	public Set<MetaObject> visitOrderSpec(OrderSpec expr, Set<MetaObject> arg) {
		expr.getOrderExpr().visitQuery(this, arg);
		return none;
	}

	@Override
	public Set<MetaObject> visitOrderTuple(OrderTuple expr, Set<MetaObject> arg) {
		for (OrderSpec order : expr.getOrderSpecs()) {
			order.visitQuery(this, arg);
		}
		return none;
	}

	@Override
	public Set<MetaObject> visitCount(CountFunction fun, Set<MetaObject> arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MetaObject> visitSum(SumFunction fun, Set<MetaObject> arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MetaObject> visitMin(MinFunction fun, Set<MetaObject> arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MetaObject> visitMax(MaxFunction fun, Set<MetaObject> arg) {
		throw new UnsupportedOperationException();
	}

}
