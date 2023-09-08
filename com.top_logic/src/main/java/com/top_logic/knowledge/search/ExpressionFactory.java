/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery.LoadStrategy;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCopy;
import com.top_logic.knowledge.service.db2.expr.visit.LiteralValue;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLObject;

/**
 * Factory for {@link Expression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionFactory {

	/**
	 * constant to indicate that no parameter are declared in the corresponding search expression.
	 * 
	 * @see ExpressionFactory#queryUnresolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 * @see ExpressionFactory#queryResolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 */
	public static final List<ParameterDeclaration> NO_QUERY_PARAMETERS = Collections.emptyList();

	/**
	 * constant to indicate that no order is requested in the query.
	 * 
	 * @see ExpressionFactory#queryUnresolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 * @see ExpressionFactory#queryResolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 */
	public static final Order NO_ORDER = null;

	/**
	 * Constructs an expression that negates the given expression.
	 * 
	 * @param expr
	 *        The expression to negate.
	 * @return The negated expression.
	 */
	public static Expression not(Expression expr) {
		return new UnaryOperation(Operator.NOT, expr);
	}

	/**
	 * Constructs an expression that checks whether the given expression evaluates to
	 * <code>null</code>.
	 * 
	 * @param expr
	 *        The expression to check.
	 * @return The checking expression.
	 */
	public static Expression isNull(Expression expr) {
		return new UnaryOperation(Operator.IS_NULL, expr);
	}

	/**
	 * Constructs an <code>and</code> combination of the two given
	 * {@link Expression}s.
	 */
	public static Expression and(Expression left, Expression right) {
		if (isNullOrTrue(left)) {
			return nullAsTrue(right);
		}
		if (isNullOrTrue(right)) {
			return nullAsTrue(left);
		}
		if (isConstantValue(left, Boolean.FALSE) || isConstantValue(right, Boolean.FALSE)) {
			return literal(Boolean.FALSE);
		}

		return binaryOperation(Operator.AND, left, right);
	}
	
	/**
	 * Constructs an <code>or</code> combination of the two given
	 * {@link Expression}s.
	 */
	public static Expression or(Expression left, Expression right) {
		if (isNullOrFalse(left)) {
			return nullAsFalse(right);
		}
		if (isNullOrFalse(right)) {
			return nullAsFalse(left);
		}
		if (isConstantValue(left, Boolean.TRUE) || isConstantValue(right, Boolean.TRUE)) {
			return literal(Boolean.TRUE);
		}
		
		return binaryOperation(Operator.OR, left, right);
	}

	private static boolean isNullOrFalse(Expression expr) {
		return expr == null || isLiteralFalse(expr);
	}

	/**
	 * Checks whether the given expression is a {@link Literal} with value {@link Boolean#FALSE}
	 * 
	 * @param expr
	 *        the expression to check. Must not be <code>null</code>
	 */
	public static boolean isLiteralFalse(Expression expr) {
		return isConstantValue(expr, Boolean.FALSE);
	}

	private static boolean isNullOrTrue(Expression expr) {
		return expr == null || isLiteralTrue(expr);
	}

	/**
	 * Checks whether the given expression is a {@link Literal} with value {@link Boolean#TRUE}
	 * 
	 * @param expr
	 *        the expression to check. Must not be <code>null</code>
	 */
	public static boolean isLiteralTrue(Expression expr) {
		return isConstantValue(expr, Boolean.TRUE);
	}

	private static boolean isConstantValue(Expression expr, Object value) {
		return LiteralValue.isLiteral(expr) && LiteralValue.getLiteralValue(expr) == value;
	}

	private static Expression nullAsFalse(Expression expr) {
		return escapeNull(expr, Boolean.FALSE);
	}

	private static Expression nullAsTrue(Expression expr) {
		return escapeNull(expr, Boolean.TRUE);
	}

	private static Expression escapeNull(Expression expr, Boolean value) {
		if (expr != null) {
			return expr;
		} else {
			return literal(value);
		}
	}

	/**
	 * Constructs a {@link Literal} expression.
	 */
	public static Expression literal(Object value) {
		if (value == null) {
			// null-Literals are not allowed #9479
			throw new IllegalArgumentException("Literals with value 'null' are not allowed.");
		}
		return new Literal(value);
	}
	
	/**
	 * Constructs a {@link Parameter} expression. The parameter is later be filled from the
	 * {@link QueryArguments arguments}. The value of the created parameter must not be
	 * <code>null</code>.
	 * 
	 * @see QueryArguments#setArguments(Object...)
	 */
	public static Expression param(String name) {
		return new Parameter(name);
	}

	/**
	 * Constructs a {@link Operator#LE} comparison of the two given
	 * {@link Expression}s.
	 */
	public static Expression le(Expression expr1, Expression expr2) {
		return binaryOperation(Operator.LE, expr1, expr2);
	}

	/**
	 * Constructs a {@link Operator#GE} comparison of the two given
	 * {@link Expression}s.
	 */
	public static Expression ge(Expression expr1, Expression expr2) {
		return binaryOperation(Operator.GE, expr1, expr2);
	}
	
	/**
	 * Constructs a {@link Operator#LT} comparison of the two given
	 * {@link Expression}s.
	 */
	public static Expression lt(Expression expr1, Expression expr2) {
		return binaryOperation(Operator.LT, expr1, expr2);
	}
	
	/**
	 * Constructs a {@link Operator#GT} comparison of the two given
	 * {@link Expression}s.
	 */
	public static Expression gt(Expression expr1, Expression expr2) {
		return binaryOperation(Operator.GT, expr1, expr2);
	}

	/**
	 * Constructs test that checks that the given expression value is within the
	 * set of given literal values.
	 * 
	 * @param expr
	 *        The expression to test for being a member in the given values.
	 * @param values
	 *        The values the given expression is tested against.
	 * @return An expression that evaluates to <code>true</code> if and only if
	 *         the expression's value is among the given values.
	 */
	public static Expression inLiteralSet(Expression expr, Collection<?> values) {
		return inSet(expr, setLiteral(values));
	}

	/**
	 * Creates a test that checks that the result of the given
	 * {@link Expression} is contained within the resulting set of the given
	 * {@link SetExpression}.
	 * 
	 * @param expr
	 *        The {@link Expression} that delivers the value that is checked for
	 *        being member of the set.
	 * @param setExpr
	 *        The {@link SetExpression} that delivers the set.
	 * @return The test {@link Expression}.
	 */
	public static Expression inSet(Expression expr, SetExpression setExpr) {
		return new InSet(expr, setExpr);
	}

	/**
	 * Creates a test that checks that the context object is contained within
	 * the resulting set of the given {@link SetExpression}.
	 * 
	 * @param setExpr
	 *        The {@link SetExpression} that delivers the set.
	 * @return The test {@link Expression}.
	 */
	public static Expression inSet(SetExpression setExpr) {
		return inSet(context(), setExpr);
	}

	/**
	 * Constructs an expression that compares the value of the given expression with the given
	 * literal value which might be <code>null</code>.
	 */
	public static Expression eqBinaryLiteral(Expression expr, Object value) {
		Expression result;
		if (value == null) {
			result = isNull(expr);
		} else {
			result = eqBinary(expr, literal(value));
		}
		return result;
	}
	
	/**
	 * Constructs a {@link Operator#EQBINARY} comparison of the two given
	 * {@link Expression}s.
	 */
	public static Expression eqBinary(Expression expr1, Expression expr2) {
		return binaryOperation(Operator.EQBINARY, expr1, expr2);
	}
	
	/**
	 * Constructs a {@link Operator#EQCI} comparison of the two given
	 * {@link Expression}s.
	 */
	public static Expression eqCi(Expression expr1, Expression expr2) {
		return binaryOperation(Operator.EQCI, expr1, expr2);
	}

	/**
	 * Construct an expression that is <code>true</code> if and only if, the
	 * given attribute has values within the given range.
	 * 
	 * @param attrName
	 *        The attribute to test values of.
	 * @param startValue
	 *        The lower bound (inclusive).
	 * @param stopValue
	 *        The upper bound (exclusive).
	 * @return A boolean expression.
	 */
	public static Expression attributeRange(String typeName, String attrName, Comparable startValue, Comparable stopValue) {
		if (startValue.compareTo(stopValue) < 0) {
			return 
				and(
					ge(attribute(typeName, attrName), literal(startValue)), 
					lt(attribute(typeName, attrName), literal(stopValue)) 
				);
		}
		return literal(false);
	}
	
	/**
	 * This is a service method for
	 * {@link #attributeRange(String, String, Comparable, Comparable)} if there
	 * is already the concrete {@link MOAttribute}.
	 */
	public static Expression attributeRange(MOAttribute attr, Comparable minValue, Comparable maxValue) {
		return attributeRange(getOwnerTypeName(attr), attr.getName(), minValue, maxValue);
	}
	
	/**
	 * Constructs a {@link Operator#EQBINARY} comparison of the two given
	 * {@link Expression} with the given value.
	 */
	public static Expression attributeEqBinary(String typeName, String attrName, Object value) {
		return eqBinaryLiteral(attribute(typeName, attrName), value);
	}

	/**
	 * Constructs an expression that compares the given attribute for binary
	 * equality to the given value.
	 */
	public static Expression attributeEqBinary(MOAttribute attr, Object value) {
		return eqBinaryLiteral(attribute(attr), value);
	}
	
	/**
	 * Constructs a {@link Operator#EQCI} comparison of the two given
	 * {@link Expression} with the given value.
	 */
	public static Expression attributeEqCi(String typeName, String attrName, String value) {
		return attributeEqCi(attribute(typeName, attrName), value);
	}
	
	/**
	 * Constructs an expression that compares the given attribute for case insensitive 
	 * equality to the given value.
	 */
	public static Expression attributeEqCi(MOAttribute attr, String value) {
		return attributeEqCi(attribute(attr), value);
	}

	static Expression attributeEqCi(Expression attribute, Object value) {
		Expression result;
		if (value == null) {
			result = isNull(attribute);
		} else {
			result = eqCi(attribute, literal(value));
		}
		return result;
	}

	/**
	 * Create a comparison.
	 * 
	 * @param binary
	 *        Whether the comparison should be binary.
	 * @param expr1
	 *        The first expression to compare.
	 * @param expr2
	 *        The second expression to compare.
	 * @return An expression that compares the two given expressions for
	 *         equality.
	 */
	public static Expression eq(boolean binary, Expression expr1, Expression expr2) {
		if (binary) {
			return eqBinary(expr1, expr2);
		} else {
			return eqCi(expr1, expr2);
		}
	}

	/**
	 * Constructs an {@link Attribute} access expression on the current context.
	 */
	public static Expression attribute(String ownerTypeName, String attributeName) {
		return attribute(context(), ownerTypeName, attributeName);
	}

	/**
	 * Constructs an {@link Attribute} access expression on the given context.
	 * 
	 * @param context
	 *        The object on which the attribute is accessed.
	 */
	public static Expression attribute(Expression context, String ownerTypeName, String attributeName) {
		return new Attribute(context, ownerTypeName, attributeName);
	}
	
	/**
	 * Shortcut for creating a not type implementation bound {@link Attribute}
	 * from a given {@link MOAttribute}.
	 */
	public static Expression attribute(MOAttribute attr) {
		return attribute(getOwnerTypeName(attr), attr.getName());
	}

	/**
	 * Constructs an expression that accesses the flex attribute with the given name and type on the
	 * current context expresssion.
	 * 
	 * @param typeName
	 *        The name of the type of the accessed flex attribute.
	 * @param name
	 *        The name of the accessed flex attribute.
	 */
	public static Expression flex(String typeName, String name) {
		return flex(context(), typeName, name);
	}

	/**
	 * Constructs an expression that accesses the flex attribute with the given name and type on the
	 * given context expression.
	 * 
	 * @param type
	 *        The type of the accessed flex attribute.
	 * @param name
	 *        The name of the accessed flex attribute.
	 */
	public static Expression flex(Expression context, MOPrimitive type, String name) {
		return flex(context, type.getName(), name);
	}

	/**
	 * Constructs an expression that accesses the flex attribute with the given name and type on the
	 * given context expression.
	 * 
	 * @param context
	 *        The object on which the attribute is accessed.
	 * @param typeName
	 *        The name of the type of the accessed flex attribute.
	 * @param name
	 *        The name of the accessed flex attribute.
	 */
	public static Expression flex(Expression context, String typeName, String name) {
		return new Flex(context, typeName, name);
	}

	/**
	 * Constructs an expression that accesses the flex attribute with the given
	 * name and type.
	 * 
	 * @param type
	 *        The type of the accessed flex attribute.
	 * @param name
	 *        The name of the accessed flex attribute.
	 */
	public static Expression flex(MOPrimitive type, String name) {
		return flex(type.getName(), name);
	}
	
	/**
	 * Constructs a new {@link ExpressionTuple} from the given expressions.
	 */
	public static Expression tuple(Expression... expressions) {
		return tuple(Arrays.asList(expressions));
	}

	/**
	 * Constructs a {@link HasType} check the current context object for the given type.
	 */
	public static Expression hasType(String typeName) {
		return hasType(context(), typeName);
	}

	/**
	 * Constructs a {@link HasType} check the given context object for the given type.
	 * 
	 * @param context
	 *        The object that is checked for its type.
	 */
	public static HasType hasType(Expression context, String typeName) {
		return new HasType(context, typeName);
	}
	
	/**
	 * Constructs an {@link InstanceOf} check the current context for the given type.
	 */
	public static Expression instanceOf(String typeName) {
		return instanceOf(context(), typeName);
	}
	
	/**
	 * Constructs an {@link InstanceOf} check the given context for the given type.
	 * 
	 * @param context
	 *        The object that is checked for its type.
	 */
	public static Expression instanceOf(Expression context, String typeName) {
		return new InstanceOf(context, typeName);
	}

	/**
	 * Constructs an {@link Expression} that evaluates the second expression in
	 * the context of the result of the first one.
	 * 
	 * @param context
	 *        The {@link Expression} that delivers the context object.
	 * @param expr
	 *        The {@link Expression} that is evaluated in the context of the
	 *        result of the first one.
	 */
	public static Expression eval(Expression context, Expression expr) {
		return new Eval(context, expr);
	}

	/**
	 * Constructs an {@link Expression} that accesses the current context tuple at the given index.
	 * 
	 * @param index
	 *        The index at which to access the context tuple.
	 */
	public static Expression getEntry(int index) {
		return getEntry(context(), index);
	}

	/**
	 * Constructs an {@link Expression} that accesses the given context tuple at the given index.
	 * 
	 * @param context
	 *        The object that is accessed.
	 * @param index
	 *        The index at which to access the context tuple.
	 */
	public static Expression getEntry(Expression context, int index) {
		return new GetEntry(context, index);
	}

	/**
	 * Constructs an expression that accesses the source end of an association
	 * item.
	 */
	public static Expression source() {
		return reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, null);
	}

	/**
	 * Constructs an expression that accesses the destination end of an
	 * association item.
	 */
	public static Expression destination() {
		return reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME, null);
	}

	/**
	 * Constructs an expression that retrieves all items of the given type
	 * (excluding sub classes).
	 * 
	 * @param typeName
	 *        The name of the type to read all items of.
	 */
	public static SetExpression allOf(String typeName) {
		return new AllOf(typeName);
	}

	/**
	 * Constructs an expression that retrieves all items of the given type
	 * (excluding sub classes).
	 * 
	 * @param type
	 *        The type to read all items of.
	 */
	public static SetExpression allOf(MOClass type) {
		return allOf(type.getName());
	}
	
	/**
	 * Constructs an expression that retrieves all items of the given type
	 * (including sub classes).
	 * 
	 * @param typeName
	 *        The name of the type to read all items of.
	 */
	public static SetExpression anyOf(String typeName) {
		return new AnyOf(typeName);
	}
	
	/**
	 * Constructs an expression that retrieves all items of the given type
	 * (including sub classes).
	 * 
	 * @param type
	 *        The type to read all items of.
	 */
	public static SetExpression anyOf(MOClass type) {
		return anyOf(type.getName());
	}
	
	/**
	 * Constructs an expression that builds the {@link Intersection} of the given expressions.
	 */
	public static SetExpression intersection(SetExpression left, SetExpression right) {
		return new Intersection(left, right);
	}

	/**
	 * Constructs an expression that builds the {@link Substraction} of the given expressions.
	 */
	public static SetExpression substraction(SetExpression left, SetExpression right) {
		return new Substraction(left, right);
	}

	/**
	 * Constructs an expression that builds the {@link Union} of the given expressions.
	 */
	public static SetExpression union(SetExpression left, SetExpression right) {
		return new Union(left, right);
	}

	/**
	 * Constructs an expression that builds the {@link CrossProduct} of the given expressions.
	 */
	public static SetExpression crossProduct(SetExpression... expressions) {
		return crossProduct(Arrays.asList(expressions));
	}

	/**
	 * Creates a {@link CrossProduct} expression.
	 * 
	 * @param expressions
	 *        List of expressions whose results build up the cross product.
	 */
	public static SetExpression crossProduct(List<SetExpression> expressions) {
		if (expressions.isEmpty()) {
			throw new IllegalArgumentException("No empty cross products.");
		}
		return new CrossProduct(expressions);
	}

	/**
	 * Constructs an expression that filters the items described by the source
	 * expression with the given filter expression.
	 */
	public static SetExpression filter(SetExpression source, Expression filter) {
		if (isLiteralTrue(filter)) {
			return source;
		}
		return new Filter(source, filter);
	}

	public static None none() {
		return new None();
	}

	public static SetExpression navigateForwards(SetExpression source, MOClass association, MOClass expectedType) {
		return navigateForwards(source, association.getName(), expectedType.getName());
	}
	
	public static SetExpression navigateForwards(SetExpression source, String associationName, String expectedTypeName) {
		return navigate(false, source, anyOf(associationName), expectedTypeName);
	}
	
	public static SetExpression navigateForwards(SetExpression source, SetExpression links, MOClass expectedType) {
		return navigate(false, source, links, expectedType.getName());
	}
	
	public static SetExpression navigateBackwards(SetExpression source, MOClass association, MOClass expectedType) {
		return navigateBackwards(source, association.getName(), expectedType.getName());
	}
	
	public static SetExpression navigateBackwards(SetExpression source, String associationName, String expectedTypeName) {
		return navigate(true, source, anyOf(associationName), expectedTypeName);
	}
	
	public static SetExpression navigateBackwards(SetExpression source, SetExpression links, MOClass expectedType) {
		return navigate(true, source, links, expectedType.getName());
	}
	
	public static SetExpression navigate(boolean backwards, SetExpression source, SetExpression links, String expectedTypeName) {
		return navigate(backwards, source, links, instanceOf(expectedTypeName));
	}

	public static SetExpression navigateForwards(SetExpression source, SetExpression links, String expectedTypeName) {
		return navigate(false, source, links, instanceOf(expectedTypeName));
	}
	
	public static SetExpression navigate(boolean backwards, SetExpression from, SetExpression links, Expression filter) {
		return 
			filter(
				map(
					filter(links, inSet(backwards ? destination() : source(), from)), 
					backwards ? source() : destination()), 
				filter);
	}
		
	public static SetExpression partition(SetExpression source, Expression equivalence, Function representative) {
		return new Partition(source, equivalence, representative);
	}

	public static SetExpression map(SetExpression source, Expression projection) {
		return new MapTo(source, projection);
	}

	public static SetExpression setLiteral(Collection<? extends Object> values) {
		return new SetLiteral(values);
	}

	public static SetExpression setParam(String name) {
		return new SetParameter(name);
	}
	
	public static SetExpression setLiteralOfEntries(Object... values) {
		return setLiteral(Arrays.asList(values));
	}

	/**
	 * Creates a query that searches for {@link KnowledgeAssociation} that matches the given
	 * expression.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are {@link KnowledgeAssociation}s
	 * </p>
	 * 
	 * @param search
	 *        the expression to match to occur in result
	 */
	public static RevisionQuery<KnowledgeAssociation> associationQuery(SetExpression search) {
		BranchParam branch = BranchParam.single;
		RangeParam range = RangeParam.complete;
		return queryUnresolved(branch, range, NO_QUERY_PARAMETERS, search, NO_ORDER, KnowledgeAssociation.class);
	}

	/**
	 * Creates a query that searches for {@link KnowledgeObject} that matches the given expression.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are {@link KnowledgeObject}s
	 * </p>
	 * 
	 * @param search
	 *        the expression to match to occur in result
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryUnresolved(SetExpression, Class)
	 */
	public static RevisionQuery<KnowledgeObject> queryUnresolved(SetExpression search) {
		return queryUnresolved(search, KnowledgeObject.class);
	}

	/**
	 * Creates a query that searches for {@link KnowledgeObject} that matches the given expression.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are of expected type
	 * </p>
	 * 
	 * @param search
	 *        the expression to match to occur in result
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryResolved(SetExpression, Class)
	 */
	public static <T> RevisionQuery<T> queryUnresolved(SetExpression search, Class<T> expectedType) {
		return queryUnresolved(search, NO_ORDER, expectedType);
	}

	/**
	 * Creates an {@link RevisionQuery#getResolve() unresolved} query that searches for
	 * {@link KnowledgeObject} which matches the given expression. The results of the query are
	 * returned in the given {@link Order}.
	 * 
	 * <p>
	 * Search is executed on a {@link BranchParam#single single branch}, {@link RangeParam#complete
	 * all results are taken} and no parameters are used.
	 * </p>
	 * 
	 * <p>
	 * Note: It is expected that all found elements are {@link KnowledgeObject}s
	 * </p>
	 * 
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryUnresolved(SetExpression, Order, Class)
	 */
	public static RevisionQuery<KnowledgeObject> queryUnresolved(SetExpression search, Order order) {
		return queryUnresolved(search, order, KnowledgeObject.class);
    }

	/**
	 * Creates an {@link RevisionQuery#getResolve() unresolved} query that searches for
	 * {@link KnowledgeObject} which matches the given expression. The results of the query are
	 * returned in the given {@link Order}.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are of given type
	 * </p>
	 * 
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryResolved(SetExpression, Order, Class)
	 */
	public static <T> RevisionQuery<T> queryUnresolved(SetExpression search, Order order, Class<T> expectedType) {
		return queryUnresolved(NO_QUERY_PARAMETERS, search, order, expectedType);
	}

	/**
	 * Creates a query with the given {@link ParameterDeclaration parameters} that searches for
	 * {@link KnowledgeObject} that matches the given expression. The results of the query are
	 * returned in the given {@link Order}.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are {@link KnowledgeObject}s
	 * </p>
	 * 
	 * @param params
	 *        see {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 */
	public static RevisionQuery<KnowledgeObject> queryUnresolved(List<ParameterDeclaration> params,
			SetExpression search, Order order) {
		return queryUnresolved(params, search, order, KnowledgeObject.class);
	}

	/**
	 * Creates a query with the given {@link ParameterDeclaration parameters} that searches for
	 * {@link KnowledgeObject} that matches the given expression. The results of the query are
	 * returned in the given {@link Order}.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are of the given type.
	 * </p>
	 * 
	 * @param params
	 *        see {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryResolved(List, SetExpression, Order, Class)
	 */
	public static <T> RevisionQuery<T> queryUnresolved(List<ParameterDeclaration> params, SetExpression search,
			Order order, Class<T> expectedType) {
		return queryUnresolved(BranchParam.single, RangeParam.complete, params, search, order, expectedType);
	}

	/**
	 * Creates a query with the given {@link ParameterDeclaration parameters} that searches for
	 * {@link KnowledgeObject} that matches the given expression. The results of the query are
	 * returned in the given {@link Order}.
	 * 
	 * <p>
	 * Note: It is expected that all found elements are {@link KnowledgeObject}s
	 * </p>
	 * 
	 * @param requestedBranch
	 *        see {@link AbstractQuery#getBranchParam()}
	 * @param range
	 *        see {@link AbstractQuery#getRangeParam()}
	 * @param params
	 *        see {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 */
	public static RevisionQuery<KnowledgeObject> queryUnresolved(BranchParam requestedBranch, RangeParam range,
			List<ParameterDeclaration> params, SetExpression search, Order order) {
		return queryUnresolved(requestedBranch, range, params, search, order, KnowledgeObject.class);
	}

	/**
	 * Creates an {@link RevisionQuery#getResolve() unresolved} query with the given
	 * {@link ParameterDeclaration parameters} that searches for objects with the given type that
	 * matches the given expression. The results of the query are returned in the given
	 * {@link Order}.
	 * 
	 * @param requestedBranch
	 *        see {@link AbstractQuery#getBranchParam()}
	 * @param range
	 *        see {@link AbstractQuery#getRangeParam()}
	 * @param params
	 *        see {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryResolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 */
	public static <E> RevisionQuery<E> queryUnresolved(BranchParam requestedBranch, RangeParam range,
			List<ParameterDeclaration> params, SetExpression search, Order order, Class<E> expectedType) {
		return newRevisionQuery(requestedBranch, range, params, search, order, expectedType, false,
			LoadStrategy.DEFAULT);
	}

	/**
	 * Creates a {@link RevisionQuery} from the given parameters.
	 * 
	 * <p>
	 * Applications should create explicit
	 * {@link ExpressionFactory#queryResolved(BranchParam, RangeParam, List, SetExpression, Order, Class)
	 * resolving query} or
	 * {@link ExpressionFactory#queryUnresolved(BranchParam, RangeParam, List, SetExpression, Order, Class)
	 * non resolving query}.
	 * </p>
	 * 
	 * @param loadStrategy
	 *        See {@link RevisionQuery#getLoadStrategy()}.
	 * 
	 * @see RevisionQuery#RevisionQuery(BranchParam, RangeParam, List, SetExpression, Order, Class,
	 *      boolean)
	 * @see ExpressionFactory#queryResolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 * @see ExpressionFactory#queryUnresolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 */
	@FrameworkInternal
	public static <E> RevisionQuery<E> newRevisionQuery(BranchParam requestedBranch, RangeParam range,
			List<ParameterDeclaration> params, SetExpression search, Order order, Class<E> expectedType, boolean resolve, LoadStrategy loadStrategy) {
		return new RevisionQuery<>(requestedBranch, range, params, search, order, expectedType, resolve)
			.setLoadStrategy(loadStrategy);
	}
	
	/**
	 * Creates a {@link RevisionQuery#getResolve() resolving} that searches with the given type that
	 * matches the given expression.
	 * 
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryUnresolved(SetExpression, Class)
	 */
	public static <T extends TLObject> RevisionQuery<T> queryResolved(SetExpression search, Class<T> expectedType) {
		return queryResolved(search, NO_ORDER, expectedType);
	}

	/**
	 * Creates a {@link RevisionQuery#getResolve() resolving} that searches with the given type that
	 * matches the given expression. The results of the query are returned in the given
	 * {@link Order}.
	 * 
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryUnresolved(SetExpression, Order, Class)
	 */
	public static <T extends TLObject> RevisionQuery<T> queryResolved(SetExpression search, Order order,
			Class<T> expectedType) {
		return queryResolved(NO_QUERY_PARAMETERS, search, order, expectedType);
	}

	/**
	 * Creates a {@link RevisionQuery#getResolve() resolving} query searching on a single branch and
	 * returns all elements matching the search expression.
	 * 
	 * @param params
	 *        see {@link RevisionQuery#getSearchParams()}
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryUnresolved(List, SetExpression, Order, Class)
	 */
	public static <T extends TLObject> RevisionQuery<T> queryResolved(List<ParameterDeclaration> params,
			SetExpression search, Order order, Class<T> expectedType) {
		return queryResolved(BranchParam.single, RangeParam.complete, params, search, order, expectedType);
	}

	/**
	 * Creates a query with the given {@link ParameterDeclaration parameters} that searches for
	 * objects with the given type that matches the given expression. The results of the query are
	 * returned in the given {@link Order}.
	 * 
	 * @param requestedBranch
	 *        see {@link AbstractQuery#getBranchParam()}
	 * @param range
	 *        see {@link AbstractQuery#getRangeParam()}
	 * @param params
	 *        see {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        see {@link AbstractQuery#getSearch()}
	 * @param order
	 *        see {@link RevisionQuery#getOrder()}
	 * @param expectedType
	 *        see {@link RevisionQuery#getExpectedType()}
	 * 
	 * @see KnowledgeBase#searchStream(RevisionQuery)
	 * @see ExpressionFactory#queryUnresolved(BranchParam, RangeParam, List, SetExpression, Order,
	 *      Class)
	 */
	public static <E extends TLObject> RevisionQuery<E> queryResolved(BranchParam requestedBranch, RangeParam range,
			List<ParameterDeclaration> params, SetExpression search, Order order, Class<E> expectedType) {
		return newRevisionQuery(requestedBranch, range, params, search, order, expectedType, true, LoadStrategy.DEFAULT);
	}

	public static HistoryQuery historyQuery(SetExpression search) {
		return historyQuery(NO_QUERY_PARAMETERS, search);
	}
	
	public static HistoryQuery historyQuery(List<ParameterDeclaration> searchParams, SetExpression search) {
		return historyQuery(BranchParam.single, RevisionParam.all, RangeParam.complete, searchParams, search);
	}
	
	public static HistoryQuery historyQuery(BranchParam branchParam, RevisionParam revisionParam, RangeParam rangeParam, List<ParameterDeclaration> searchParams, SetExpression search) {
		return new HistoryQuery(branchParam, revisionParam, rangeParam, searchParams, search);
	}
	
	public static Expression binaryOperation(Operator symbol, Expression left, Expression right) {
		return new BinaryOperation(symbol, left, right);
	}
	
	public static Expression unaryOperation(Operator symbol, Expression expr) {
		return new UnaryOperation(symbol, expr);
	}

	public static Expression context() {
		return new ContextAccess();
	}

	public static Expression matches(String regex, Expression expr) {
		return new Matches(regex, expr);
	}

	/**
	 * Creates a new expression representing the access to an referenced attribute of the current
	 * context expression.
	 * 
	 * @param ownerTypeName
	 *        the type defining the reference attribute
	 * @param attributeName
	 *        the name of the reference attribute
	 * @param accessPart
	 *        the part of the reference to access. <code>null</code> means the whole reference is
	 *        desired
	 */
	public static Expression reference(String ownerTypeName, String attributeName, ReferencePart accessPart) {
		return reference(context(), ownerTypeName, attributeName, accessPart);
	}

	/**
	 * Creates a new expression representing the access to an referenced attribute of the given
	 * context expression.
	 * 
	 * @param context
	 *        The object on which the reference is accessed.
	 * @param ownerTypeName
	 *        the type defining the reference attribute
	 * @param attributeName
	 *        the name of the reference attribute
	 * @param accessPart
	 *        the part of the reference to access. <code>null</code> means the whole reference is
	 *        desired
	 */
	public static Reference reference(Expression context, String ownerTypeName, String attributeName,
			ReferencePart accessPart) {
		return new Reference(context, ownerTypeName, attributeName, accessPart);
	}

	/**
	 * Creates a new expression representing the access to the given {@link MOReference}.
	 * 
	 * @see #reference(String, String, ReferencePart)
	 */
	public static Expression reference(MOReference reference, ReferencePart type) {
		return reference(reference.getOwner().getName(), reference.getName(), type);
	}

	/**
	 * Creates a new expression representing an referenced attribute
	 * 
	 * @param ownerTypeName
	 *        the type defining the reference attribute
	 * @param attributeName
	 *        the name of the reference attribute
	 */
	public static Expression reference(String ownerTypeName, String attributeName) {
		return reference(context(), ownerTypeName, attributeName);
	}

	/**
	 * Creates a new expression representing an referenced attribute
	 * 
	 * @param context
	 *        The object on which the reference is accessed.
	 * @param ownerTypeName
	 *        the type defining the reference attribute
	 * @param attributeName
	 *        the name of the reference attribute
	 */
	public static Expression reference(Expression context, String ownerTypeName, String attributeName) {
		return reference(context, ownerTypeName, attributeName, null);
	}

	/**
	 * Creates a new expression representing the access to the given {@link MOReference}.
	 * 
	 * @see #reference(String, String)
	 */
	public static Expression reference(MOReference reference) {
		return reference(reference, null);
	}

	public static Expression tuple(List<Expression> expressions) {
		if (expressions.size() == 0) {
			throw new IllegalArgumentException("No empty tuples.");
		}
		return new ExpressionTuple(expressions);
	}

	/**
	 * Creates a {@link CountFunction}.
	 */
	public static Function count() {
		return new CountFunction();
	}

	public static Function max(Expression expr) {
		return new MaxFunction(expr);
	}

	public static Function min(Expression expr) {
		return new MinFunction(expr);
	}

	public static Function sum(Expression expr) {
		return new SumFunction(expr);
	}

	/**
	 * Create an OrderSpec for ascending order.
	 */
    public static OrderSpec order(Expression orderExpr) {
		return new OrderSpec(orderExpr, /* descending */false);
    }

	/**
	 * Create an OrderSpec for descending order.
	 */
	public static OrderSpec orderDesc(Expression orderExpr) {
		return new OrderSpec(orderExpr, /* descending */true);
	}

    public static OrderSpec order(Expression orderExpr, boolean descending) {
		return new OrderSpec(orderExpr, descending);
	}

	public static OrderTuple orders(List<OrderSpec> orderSpecs) {
		return new OrderTuple(orderSpecs);
	}

	/**
	 * Find the name of the owner type of a given {@link MOAttribute}.
	 */
	static String getOwnerTypeName(MOAttribute attribute) {
		return attribute.getOwner().getName();
	}

	public static RevisionQueryArguments revisionArgs() {
		return new RevisionQueryArguments();
	}
	
	public static HistoryQueryArguments historyArgs() {
		return new HistoryQueryArguments();
	}

	public static List<ParameterDeclaration> params(ParameterDeclaration... decls) {
		return Arrays.asList(decls);
	}

	/**
	 * @see #paramDecl(String, String)
	 */
	public static ParameterDeclaration paramDecl(MetaObject type, String name) {
		return paramDecl(type.getName(), name);
	}

	/**
	 * @param typeName
	 *        the name of the type of the value of the represented parameter
	 * @param name
	 *        the name of the parameter
	 */
	public static ParameterDeclaration paramDecl(String typeName, String name) {
		return new ParameterDeclaration(typeName, name);
	}

	/**
	 * creates a deep copy of the given expression.
	 */
	public static <T extends QueryPart> T deepClone(T expr) {
		return ExpressionCopy.copy(expr);
	}

	/**
	 * an expression that accesses the branch of the context object.
	 */
	public static Expression branch() {
		return branch(context());
	}

	/**
	 * Access to the branch of an object.
	 * 
	 * @param expr
	 *        The object whose branch is accessed.
	 */
	public static Expression branch(Expression expr) {
		return unaryOperation(Operator.BRANCH, expr);
	}

	/**
	 * Accesses to the revision of the context object.
	 */
	public static Expression revision() {
		return revision(context());
	}

	/**
	 * Accesses to the revision of an object.
	 * 
	 * <p>
	 * In contrast to {@link #historyContext(Expression)}, this expression represents the history
	 * context of the request, when the given {@link Expression} represents a
	 * {@link HistoryUtils#isCurrent(ObjectKey) current object}.
	 * </p>
	 * 
	 * @param expr
	 *        The object whose revision is accessed.
	 * 
	 * @see #historyContext(Expression)
	 */
	public static Expression revision(Expression expr) {
		return unaryOperation(Operator.REVISION, expr);
	}

	/**
	 * Accesses to the history context of an object.
	 * 
	 * <p>
	 * In contrast to {@link #revision(Expression)}, this expression always returns
	 * {@link ObjectKey#getHistoryContext() history context} of the given {@link Expression}.
	 * </p>
	 * 
	 * @param expr
	 *        The object whose history context is accessed.
	 * 
	 * @see #revision(Expression)
	 */
	public static Expression historyContext(Expression expr) {
		return unaryOperation(Operator.HISTORY_CONTEXT, expr);
	}

	/**
	 * Accesses to the identifier of the context object.
	 */
	public static Expression identifier() {
		return identifier(context());
	}

	/**
	 * Accesses to the identifier of an object
	 * 
	 * @param expr
	 *        The object whose identifier is accessed.
	 */
	public static Expression identifier(Expression expr) {
		return unaryOperation(Operator.IDENTIFIER, expr);
	}

	/**
	 * Accesses to the type name of the context object.
	 */
	public static Expression type() {
		return type(context());
	}

	/**
	 * Accesses to the type name of an object.
	 * 
	 * @param expr
	 *        The object whose type is accessed.
	 */
	public static Expression type(Expression expr) {
		return unaryOperation(Operator.TYPE_NAME, expr);
	}

	/** 
	 * An expression that checks whether the object defined by the given expression is a current object. 
	 */
	public static Expression isCurrent(Expression context) {
		return new IsCurrent(context);
	}

	/**
	 * Special expression determining the requested history context of the the request.
	 */
	public static Expression requestedHistoryContext() {
		return new RequestedHistoryContext();
	}

	/**
	 * Reference to the type of an {@link TLObject}.
	 */
	public static Expression typeRef() {
		return reference(PersistentObject.OBJECT_TYPE, PersistentObject.TYPE_REF);
	}

	/**
	 * Name of the type of an {@link TLObject}.
	 */
	public static Expression typeName() {
		return attribute(typeRef(), ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE,
			ApplicationObjectUtil.META_ELEMENT_ME_TYPE_ATTR);
	}

}
