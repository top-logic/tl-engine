/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MOTupleImpl;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeItem;
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
 * Computation of {@link QueryPart#getPolymorphicType()}.
 * 
 * <p>
 * Argument of the visit: The type of the context object, in which an expression
 * may be evaluated.
 * </p>
 * 
 * <p>
 * Result of the visit for {@link SetExpression}s: The common type of all
 * elements in the set.
 * </p>
 * 
 * <p>
 * Result of the visit for {@link Expression}s: For boolean expressions, the
 * context type, in which the expression may be evaluated and return true. For
 * non-boolean expressions, the context type.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PolymorphicTypeComputation implements QueryVisitor<MetaObject, MetaObject, MetaObject, MetaObject, MetaObject, MetaObject> {

	private final TypeSystem typeSystem;
	private final ExpressionCompileProtocol log;

	private PolymorphicTypeComputation(TypeSystem typeSystem, ExpressionCompileProtocol log) {
		this.typeSystem = typeSystem;
		this.log = log;
	}
	
	private MetaObject getItemType() {
		return typeSystem.getItemType();
	}

	/**
	 * Computes the {@link QueryPart#getPolymorphicType() polymorphic type} of the given query.
	 */
	public static void computeTypes(TypeSystem typeSystem, ExpressionCompileProtocol log, QueryPart query) {
		PolymorphicTypeComputation visitor = new PolymorphicTypeComputation(typeSystem, log);
		query.visitQuery(visitor, visitor.getItemType());
	}
	
	@Override
	public MetaObject visitHistoryQuery(HistoryQuery expr, MetaObject arg) {
		for (ParameterDeclaration decl : expr.getSearchParams()) {
			decl.visitQuery(this, arg);
		}
		MetaObject resultType = expr.getSearch().visitSetExpr(this, arg);
		return resultType;
	}
	
	@Override
	public MetaObject visitRevisionQuery(RevisionQuery<?> expr, MetaObject arg) {
		for (ParameterDeclaration decl : expr.getSearchParams()) {
			decl.visitQuery(this, arg);
		}
		MetaObject resultType = expr.getSearch().visitSetExpr(this, arg);
		
		Order order = expr.getOrder();
		if (order != null) {
			order.visitOrder(this, resultType);
		}
		return resultType;
	}
	
	@Override
	public MetaObject visitParameterDeclaration(ParameterDeclaration expr, MetaObject arg) {
		MetaObject resultType = expr.getDeclaredType();
		assign(expr, resultType); 
		return resultType;
	}
	
	@Override
	public MetaObject visitAttribute(Attribute expr, MetaObject arg) {
		MetaObject contextType = descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		if (!ensureItemContext(expr, accessedType)) {
			assign(expr, MetaObject.INVALID_TYPE); 
			return accessedType;
		}
		
		MOAttribute attributeImpl = expr.getAttribute();
		
		MOClass ownerType = (MOClass) attributeImpl.getOwner();
		if (!(accessedType.isSubtypeOf(ownerType))) {
			error(expr, "Access to attribute ''{0}'' that is not defined in context type ''{1}''.",
				expr.getAttributeName(), accessedType.getName());
		}
		
		MetaObject attributeType = attributeImpl.getMetaObject();
		assign(expr, attributeType); 
		return contextType;
	}

	private MetaObject descendContext(ContextExpression expr, MetaObject arg) {
		Expression context = expr.getContext();
		return context.visit(this, arg);
	}

	@Override
	public MetaObject visitIsCurrent(IsCurrent expr, MetaObject arg) {
		MetaObject contextType = descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		ensureItemContext(expr, accessedType);
		assign(expr, MOPrimitive.BOOLEAN);
		return contextType;
	}
	
	@Override
	public MetaObject visitRequestedHistoryContext(RequestedHistoryContext expr, MetaObject contextType) {
		assign(expr, MOPrimitive.LONG);
		return contextType;
	}

	@Override
	public MetaObject visitGetEntry(GetEntry expr, MetaObject arg) {
		MetaObject contextType = descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		MetaObject result;
		if (accessedType instanceof MOTupleImpl) {
			List<MetaObject> entryTypes = ((MOTupleImpl) accessedType).getEntryTypes();
			int index = expr.getIndex();
			if (index < 0 || index >= entryTypes.size()) {
				error(expr, "Invalid tuple index '" + index + "'.");
				result = MetaObject.INVALID_TYPE;
			} else {
				result = entryTypes.get(index);
			}
		} else {
			error(expr, "Context is not of tuple type.");
			result = MetaObject.INVALID_TYPE;
		}
		
		assign(expr, result); 
		return contextType;
	}

	@Override
	public MetaObject visitBinaryOperation(BinaryOperation expr, MetaObject contextType) {
		Expression left = expr.getLeft();
		Expression right = expr.getRight();
		switch (expr.getOperator()) {
		case AND: {
			MetaObject rightContext = left.visit(this, contextType); 
			MetaObject leftType = left.getPolymorphicType();
			check(left, MOPrimitive.BOOLEAN, leftType);
			
			MetaObject resultContext = right.visit(this, rightContext);
			MetaObject rightType = right.getPolymorphicType();
			check(right, MOPrimitive.BOOLEAN, rightType);
			
			assign(expr, MOPrimitive.BOOLEAN); 
			return resultContext;
		}
			
		case OR: {
			MetaObject leftNonFalseContext = left.visit(this, contextType);
			MetaObject leftType = left.getPolymorphicType();
			check(left, MOPrimitive.BOOLEAN, leftType);
			
			MetaObject rightNonFalseContext = right.visit(this, contextType);
			MetaObject rightType = right.getPolymorphicType();
			check(right, MOPrimitive.BOOLEAN, rightType);
			
			assign(expr, MOPrimitive.BOOLEAN);
			
			return typeSystem.getUnionType(leftNonFalseContext, rightNonFalseContext);
		}
			
		case EQBINARY: {
			MetaObject leftType = descend(left, contextType);
			MetaObject rightType = descend(right, contextType);
			if (! typeSystem.hasCommonInstances(leftType, rightType)) {
				error(expr, "Comparision of incompatible types ''{0}'' and ''{1}''.", leftType, rightType);
			}
			assign(expr, MOPrimitive.BOOLEAN); 
			return contextType;
		}
		
		case EQCI: {
			MetaObject leftType = descend(left, contextType);
			check(left, MOPrimitive.STRING, leftType);
			MetaObject rightType = descend(right, contextType);
			check(right, MOPrimitive.STRING, rightType);
			assign(expr, MOPrimitive.BOOLEAN); 
			return contextType;
		}
			
		case GE: 
		case GT: 
		case LE: 
		case LT: {
			MetaObject leftType = descend(left, contextType);
			MetaObject rightType = descend(right, contextType);
			if (! typeSystem.isComparableTo(leftType, rightType)) {
				error(expr, "Uncomparable types.");
			}
			assign(expr, MOPrimitive.BOOLEAN); 
			return contextType;
		}
			
		default: 
			throw new AssertionError("Unexpected symbol: " + expr.getOperator());
		}
	}

	@Override
	public MetaObject visitUnaryOperation(UnaryOperation expr, MetaObject contextType) {
		switch (expr.getOperator()) {
			case IS_NULL:
			case NOT: {
				expr.getExpr().visit(this, contextType);
				assign(expr, MOPrimitive.BOOLEAN);
				return contextType;
			}
			case BRANCH: {
				expr.getExpr().visit(this, contextType);
				assign(expr, IdentifierTypes.BRANCH_REFERENCE_MO_TYPE);
				return contextType;
			}
			case REVISION:
			case HISTORY_CONTEXT: {
				expr.getExpr().visit(this, contextType);
				assign(expr, IdentifierTypes.REVISION_REFERENCE_MO_TYPE);
				return contextType;
			}
			case IDENTIFIER: {
				expr.getExpr().visit(this, contextType);
				assign(expr, IdentifierTypes.REFERENCE_MO_TYPE);
				return contextType;
			}
			case TYPE_NAME: {
				expr.getExpr().visit(this, contextType);
				assign(expr, IdentifierTypes.TYPE_REFERENCE_MO_TYPE);
				return contextType;
			}
			default:
				throw new AssertionError("Unexpected symbol: " + expr.getOperator());
		}
	}

	@Override
	public MetaObject visitEval(Eval expr, MetaObject arg) {
		MetaObject contextType = descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		switch (accessedType.getKind()) {
		case item:
		case alternative:
		case primitive:
		case struct:
		case tuple: {
			// Ok as evaluation context.
			break;
		}
		
		case collection:
		case function:
		case ANY:
		case INVALID:
		case NULL:
			error(expr, "Evaluation context cannot be of type '" + accessedType.getKind() + "'.");
			break;
		}
		
		assign(expr, descend(expr.getExpr(), accessedType));
		return contextType;
	}
	
	@Override
	public MetaObject visitContext(ContextAccess expr, MetaObject contextType) {
		assign(expr, contextType); 
		return contextType;
	}

	@Override
	public MetaObject visitFlex(Flex expr, MetaObject arg) {
		MetaObject contextType = descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		ensureItemContext(expr, accessedType);
		assign(expr, expr.getDeclaredType()); 
		return contextType;
	}

	@Override
	public MetaObject visitHasType(HasType expr, MetaObject arg) {
		descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		ensureItemContext(expr, accessedType);
		assign(expr, MOPrimitive.BOOLEAN); 
		return expr.getDeclaredType();
	}

	@Override
	public MetaObject visitInstanceOf(InstanceOf expr, MetaObject arg) {
		descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		ensureItemContext(expr, accessedType);
		assign(expr, MOPrimitive.BOOLEAN); 
		return expr.getDeclaredType();
	}

	private boolean ensureItemContext(QueryPart expr, MetaObject contextType) {
		boolean itemType = MetaObjectUtils.isItemType(contextType);
		if (!itemType) {
			error(expr, "Context of type ''{0}'' is not of item type. Type: ''{1}''.", contextType.getName(),
				contextType.getKind());
		}
		return itemType;
	}

	@Override
	public MetaObject visitLiteral(Literal expr, MetaObject contextType) {
		Object literalValue = expr.getValue();

		MetaObject result = getLiteralType(literalValue);
		if (result == MetaObject.INVALID_TYPE) {
			error(expr, "Literal of unsupported type.");
		}
		
		assign(expr, result); 
		return contextType;
	}
	
	@Override
	public MetaObject visitParameter(Parameter expr, MetaObject contextType) {
		assign(expr, expr.getDeclaredType()); 
		return contextType;
	}

	/**
	 * Computes the type of the given object
	 */
	public static MetaObject getLiteralType(Object literalValue) {
		MetaObject result;
		if (literalValue == null) {
			result = MetaObject.NULL_TYPE;
		} else if (literalValue instanceof KnowledgeItem) {
			result = ((KnowledgeItem) literalValue).tTable();
		} else if (literalValue instanceof ObjectKey) {
			result = ((ObjectKey) literalValue).getObjectType();
		} else {
			MOPrimitive primitive = MOPrimitive.getPrimitive(literalValue.getClass());
			if (primitive != null) {
				result = primitive;
			} else {
				result = MetaObject.INVALID_TYPE;
			}
		}
		return result;
	}

	@Override
	public MetaObject visitMatches(Matches expr, MetaObject contextType) {
		expr.visit(this, contextType);
		assign(expr, MOPrimitive.BOOLEAN); 
		return contextType;
	}

	@Override
	public MetaObject visitReference(Reference expr, MetaObject arg) {
		MetaObject contextType = descendContext(expr, arg);
		MetaObject accessedType = expr.getContext().getPolymorphicType();
		if (!ensureItemContext(expr, accessedType)) {
			assign(expr, MetaObject.INVALID_TYPE);
			return contextType;
		}

		MOAttribute attribute = expr.getAttribute();

		MOClass ownerType = (MOClass) attribute.getOwner();
		if (!(accessedType.isSubtypeOf(ownerType))) {
			error(expr, "Access to attribute ''{0}'' that is not defined in context type ''{1}''.",
				expr.getAttributeName(), accessedType.getName());
		}

		MetaObject attributeType;
		final ReferencePart accessType = expr.getAccessType();
		if (accessType == null) {
			attributeType = attribute.getMetaObject();
		} else {
			attributeType = expr.getColumnType(accessType);
		}
		assign(expr, attributeType);
		return contextType;

	}

	@Override
	public MetaObject visitTuple(ExpressionTuple expr, MetaObject contextType) {
		List<Expression> expressions = expr.getExpressions();
		List<MetaObject> entryTypes = new ArrayList<>(expressions.size());
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			MetaObject entryType = descend(expressions.get(n), contextType);
			entryTypes.add(entryType);
		}
		assign(expr, new MOTupleImpl(entryTypes)); 
		return contextType;
	}
	
	@Override
	public MetaObject visitInSet(InSet expr, MetaObject contextType) {
		descendContext(expr, contextType);
		MetaObject exprType = expr.getContext().getPolymorphicType();
		// the context type must not be used in an SetExpression.
		MetaObject invalidType = MetaObject.INVALID_TYPE;
		MetaObject setType = expr.getSetExpr().visitSetExpr(this, invalidType);
		if (!typeSystem.hasCommonInstances(exprType, setType)) {
			error(expr, "Contains test with incompatible types ({0} vs. {1}).", exprType, setType);
		}
		assign(expr, MOPrimitive.BOOLEAN); 
		return contextType;
	}

	@Override
	public MetaObject visitNone(None expr, MetaObject noContextType) {
		return assign(expr, typeSystem.getItemType());
	}

	@Override
	public MetaObject visitSetLiteral(SetLiteral expr, MetaObject noContextType) {
		Collection<? extends Object> values = expr.getValues();
		
		Iterator<? extends Object> it = values.iterator();
		if (! it.hasNext()) {
			// Set literals must contain at least one value.
			return assign(expr, MetaObject.INVALID_TYPE);
		}
		
		Object firstValue = it.next();
		MetaObject elementType = getLiteralType(firstValue);
		while (it.hasNext()) {
			Object value = it.next();
			MetaObject literalType = getLiteralType(value);
			elementType = typeSystem.getUnionType(elementType, literalType);
		}
		return assign(expr, elementType);
	}
	
	@Override
	public MetaObject visitSetParameter(SetParameter expr, MetaObject noContextType) {
		MOCollection declaredType = (MOCollection) expr.getDeclaredType();
		return assign(expr, declaredType.getElementType());
	}
	
	@Override
	public MetaObject visitAllOf(AllOf expr, MetaObject noContextType) {
		return assign(expr, expr.getDeclaredType());
	}

	@Override
	public MetaObject visitAnyOf(AnyOf expr, MetaObject noContextType) {
		return assign(expr, expr.getDeclaredType());
	}

	@Override
	public MetaObject visitCrossProduct(CrossProduct expr, MetaObject noContextType) {
		List<SetExpression> entries = expr.getExpressions();
		if (entries.size() == 0) {
			error(expr, "Empty cross product.");
			List<MetaObject> entryTypes = Collections.emptyList();
			return assign(expr, new MOTupleImpl(entryTypes));
		} else if (entries.size() == 1) {
			// Trivial cross product does not create tuples.
			return assign(expr, entries.get(0).visitSetExpr(this, null));
		} else {
			ArrayList<MetaObject> entryTypes = new ArrayList<>(entries.size());
			for (SetExpression entry : entries) {
				MetaObject elementType = entry.visitSetExpr(this, null);
				entryTypes.add(elementType);
			}
			return assign(expr, new MOTupleImpl(entryTypes));
		}
	}

	@Override
	public MetaObject visitFilter(Filter expr, MetaObject noContextType) {
		MetaObject sourceExprType = expr.getSource().visitSetExpr(this, null);
		MetaObject filterExpressionContextType = expr.getFilter().visit(this, sourceExprType);
		return assign(expr, filterExpressionContextType);
	}
	
	@Override
	public MetaObject visitPartition(Partition expr, MetaObject noContextType) {
		MetaObject contextType = expr.getSource().visitSetExpr(this, null);
		expr.getEquivalence().visit(this, contextType);
		MetaObject representativeType = expr.getRepresentative().visitFunction(this, contextType);
		return assign(expr, representativeType);
	}

	@Override
	public MetaObject visitMapTo(MapTo expr, MetaObject noContextType) {
		MetaObject contextType = expr.getSource().visitSetExpr(this, null);
		MetaObject projectionType = descend(expr.getMapping(), contextType);
		return assign(expr, projectionType);
	}

	@Override
	public MetaObject visitSubstraction(Substraction expr, MetaObject noContextType) {
		MetaObject sourceType = expr.getLeftExpr().visitSetExpr(this, null);
		MetaObject substractedType = expr.getRightExpr().visitSetExpr(this, null);
		if (! typeSystem.hasCommonInstances(substractedType, sourceType)) {
			// TODO: Not an error, but... ?
			error(expr, "Substraction cannot remove any items.");
		}
		return assign(expr, sourceType);
	}

	@Override
	public MetaObject visitUnion(Union expr, MetaObject noContextType) {
		MetaObject leftType = expr.getLeftExpr().visitSetExpr(this, noContextType);
		MetaObject rigthType = expr.getRightExpr().visitSetExpr(this, noContextType);
		
		MetaObject commonEntryType = typeSystem.getUnionType(leftType, rigthType);
		return assign(expr, commonEntryType);
	}

	@Override
	public MetaObject visitIntersection(Intersection expr, MetaObject noContextType) {
		MetaObject leftType = expr.getLeftExpr().visitSetExpr(this, noContextType);
		MetaObject rigthType = expr.getRightExpr().visitSetExpr(this, noContextType);
		
		if (! typeSystem.hasCommonInstances(leftType, rigthType)) {
			// TODO: Not an error, but... ?
			error(expr, "Intersection of incompatible types.");
		}
		
		MetaObject result = typeSystem.getIntersectionType(leftType, rigthType);
		return assign(expr, result);
	}

	@Override
	public MetaObject visitOrderSpec(OrderSpec expr, MetaObject contextType) {
		// Descend to assign types to inner expressions.
		expr.getOrderExpr().visit(this, contextType);

		// Order expressions have no types themselves.
		return null;
	}

	@Override
	public MetaObject visitOrderTuple(OrderTuple expr, MetaObject contextType) {
		// Descend to assign types to inner expressions.
		for (OrderSpec orderSpec : expr.getOrderSpecs()) {
			orderSpec.visitOrder(this, contextType);
		}
		
		// Order expressions have no types themselves.
		return null;
	}
	
	
	@Override
	public MetaObject visitCount(CountFunction fun, MetaObject arg) {
		return MOPrimitive.LONG;
	}
	
	@Override
	public MetaObject visitSum(SumFunction fun, MetaObject arg) {
		MetaObject exprType = descend(fun.getExpr(), arg);
		return assign(fun, exprType);
	}

	@Override
	public MetaObject visitMax(MaxFunction fun, MetaObject arg) {
		MetaObject exprType = descend(fun.getExpr(), arg);
		return assign(fun, exprType);
	}
	
	@Override
	public MetaObject visitMin(MinFunction fun, MetaObject arg) {
		MetaObject exprType = descend(fun.getExpr(), arg);
		return assign(fun, exprType);
	}
	
	private void check(QueryPart expr, MetaObject expectedType, MetaObject computedType) {
		if (! typeSystem.isAssignmentCompatible(expectedType, computedType)) {
			error(expr, "MetaObject mismatch, expected ''{0}'', got ''{1}''.", expectedType, computedType);
		}
	}

	private <T extends MetaObject> T assign(QueryPart expr, T queryType) {
		assert queryType != null : "Type must not be null.";
		
		expr.setPolymorphicType(queryType);
		return queryType;
	}

	private void error(QueryPart expr, String message, Object... args) {
		log.error(expr, message, args);
	}
	
	private MetaObject descend(Expression left, MetaObject contextType) {
		left.visit(this, contextType);
		return left.getPolymorphicType();
	}
	

}
