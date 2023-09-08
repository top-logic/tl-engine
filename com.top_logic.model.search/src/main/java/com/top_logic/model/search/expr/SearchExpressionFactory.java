/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;


import com.top_logic.basic.html.SafeHTML;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.layout.basic.fragments.RenderedFragment;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;

/**
 * Factory for {@link SearchExpression} models.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SearchExpressionFactory {

	private static final SearchExpression[] NO_ARGS = {};

	/**
	 * Creates an {@link All} expression.
	 * 
	 * @param type
	 *        See {@link All#getInstanceType()}.
	 */
	public static All all(TLStructuredType type) {
		return new All(type);
	}

	/**
	 * Creates a {@link TupleExpression}.
	 * 
	 * @param coords
	 *        See {@link TupleExpression#getCoords()}.
	 */
	public static TupleExpression tuple(TupleExpression.Coord... coords) {
		return new TupleExpression(coords);
	}

	/**
	 * Creates a {@link TupleExpression.Coord}.
	 *
	 * @param optional
	 *        See {@link TupleExpression.Coord#isOptional()}.
	 * @param name
	 *        See {@link TupleExpression.Coord#getName()}.
	 * @param expr
	 *        See {@link TupleExpression.Coord#getExpr()}.
	 */
	public static TupleExpression.Coord coord(boolean optional, Object name, SearchExpression expr) {
		return new TupleExpression.Coord(optional, name, expr);
	}

	/**
	 * Creates a pre-compiled expression that evaluates the given knowledge base query.
	 * 
	 * <p>
	 * The query must be formulated in a way to ensure only to retrieve instances of the given type
	 * or its subtypes.
	 * </p>
	 * 
	 * @param classType
	 *        The type of items retrieved.
	 * @param query
	 *        The knowledge base query to execute.
	 */
	public static KBQuery query(TLClass classType, SetExpression query) {
		return new KBQuery(classType, query);
	}

	/**
	 * Creates an {@link InstanceOf} expression.
	 * @param value
	 *        See {@link InstanceOf#getValue()}.
	 * @param type
	 *        See {@link InstanceOf#getCheckType()}.
	 */
	public static InstanceOf instanceOf(SearchExpression value, TLStructuredType type) {
		return new InstanceOf(value, type);
	}

	/**
	 * Creates an {@link Filter} expression.
	 * 
	 * @param base
	 *        See {@link Filter#getBase()}.
	 * @param function
	 *        See {@link Filter#getFunction()}.
	 */
	public static Filter filter(SearchExpression base, SearchExpression function) {
		return new Filter(base, function);
	}

	/**
	 * Creates an {@link Foreach} expression.
	 * 
	 * @param base
	 *        See {@link Foreach#getBase()}.
	 * @param function
	 *        See {@link Foreach#getFunction()}.
	 */
	public static Foreach foreach(SearchExpression base, SearchExpression function) {
		return new Foreach(base, function);
	}

	/**
	 * Creates an {@link Union} expression.
	 * 
	 * @param left
	 *        See {@link Union#getLeft()}.
	 * @param right
	 *        See {@link Union#getRight()}.
	 */
	public static Union union(SearchExpression left, SearchExpression right) {
		return new Union(left, right);
	}

	/**
	 * Creates an {@link Intersection} expression.
	 * 
	 * @param left
	 *        See {@link Intersection#getLeft()}.
	 * @param right
	 *        See {@link Intersection#getRight()}.
	 */
	public static Intersection intersection(SearchExpression left, SearchExpression right) {
		return new Intersection(left, right);
	}

	/**
	 * Creates an {@link Flatten} expression.
	 * 
	 * @param arg
	 *        See {@link Flatten#getArgument()}.
	 */
	public static Flatten flatten(SearchExpression arg) {
		return new Flatten(arg);
	}

	/**
	 * Creates a {@link Singleton} expression.
	 * 
	 * @param arg
	 *        See {@link Singleton#getArgument()}.
	 */
	public static Singleton singleton(SearchExpression arg) {
		return new Singleton(arg);
	}

	/**
	 * Creates a {@link SingleElement} expression.
	 * 
	 * @param arg
	 *        See {@link SingleElement#getArgument()}.
	 */
	public static SingleElement singleElement(SearchExpression arg) {
		return new SingleElement(arg);
	}

	/**
	 * Creates a collection access expression.
	 *
	 * @param self
	 *        The collection (list or map).
	 * @param index
	 *        The index value (index or key).
	 * @return The value of the given collection at the given index.
	 */
	public static At at(SearchExpression self, SearchExpression index) {
		return new At(self, index);
	}

	/**
	 * Creates an {@link Access} expression.
	 * 
	 * @param self
	 *        See {@link Access#getSelf()}.
	 * @param part
	 *        See {@link Access#getPart()}.
	 */
	public static Access access(SearchExpression self, TLStructuredTypePart part) {
		return new Access(self, part);
	}

	/**
	 * Creates an {@link Update} expression.
	 * 
	 * @param self
	 *        See {@link Update#getSelf()}.
	 * @param part
	 *        See {@link Update#getPart()}.
	 * @param value
	 *        See {@link Update#getValue()}.
	 */
	public static Update update(SearchExpression self, TLStructuredTypePart part, SearchExpression value) {
		return new Update(self, part, value);
	}

	/**
	 * Creates an {@link Block} expression.
	 * 
	 * @param contents
	 *        See {@link Block#getContents()}.
	 */
	public static Block block(SearchExpression... contents) {
		return new Block(contents);
	}

	/**
	 * Creates an {@link Referers} expression.
	 * @param target
	 *        See {@link Referers#getTarget()}.
	 * @param reference
	 *        See {@link Referers#getReference()}.
	 */
	public static Referers referers(SearchExpression target, TLReference reference) {
		return new Referers(target, reference);
	}

	/**
	 * Creates an {@link AssociationNavigation} expression.
	 * 
	 * @param source
	 *        See {@link AssociationNavigation#getSource()}.
	 * @param sourceEnd
	 *        See {@link AssociationNavigation#getSourceEnd()}.
	 * @param destinationEnd
	 *        See {@link AssociationNavigation#getDestinationEnd()}.
	 */
	public static AssociationNavigation associationNavigation(
			SearchExpression source, TLAssociationEnd sourceEnd, TLAssociationEnd destinationEnd) {
		return new AssociationNavigation(source, sourceEnd, destinationEnd);
	}

	/**
	 * Creates an {@link ContainsAll} expression.
	 * 
	 * @param left
	 *        See {@link ContainsAll#getLeft()}.
	 * @param right
	 *        See {@link ContainsAll#getRight()}.
	 */
	public static ContainsAll containsAll(SearchExpression left, SearchExpression right) {
		return new ContainsAll(left, right);
	}

	/**
	 * Creates an {@link ContainsElement} expression.
	 * 
	 * @param left
	 *        See {@link ContainsElement#getLeft()}.
	 * @param right
	 *        See {@link ContainsElement#getRight()}.
	 */
	public static ContainsElement containsElement(SearchExpression left, SearchExpression right) {
		return new ContainsElement(left, right);
	}

	/**
	 * Creates an {@link ContainsSome} expression.
	 * 
	 * @param left
	 *        See {@link ContainsSome#getLeft()}.
	 * @param right
	 *        See {@link ContainsSome#getRight()}.
	 */
	public static ContainsSome containsSome(SearchExpression left, SearchExpression right) {
		return new ContainsSome(left, right);
	}

	/**
	 * Creates a {@link Lambda} expression.
	 * 
	 * @param name
	 *        See {@link Lambda#getName()}.
	 * @param body
	 *        See {@link Lambda#getBody()}.
	 */
	public static Lambda lambda(Object name, SearchExpression body) {
		return new Lambda(name, body);
	}

	/**
	 * Creates a {@link Call} expression.
	 * 
	 * @param function
	 *        See {@link Call#getFunction()}.
	 * @param argument
	 *        See {@link Call#getArgument()}.
	 */
	public static Call call(SearchExpression function, SearchExpression argument) {
		return new Call(function, argument);
	}

	/**
	 * Short-cut for applying multiple arguments to a higher-order function.
	 * 
	 * @param function
	 *        The function expression.
	 * @param arguments
	 *        The arguments to apply.
	 * @return The resulting function call expression.
	 */
	public static SearchExpression call(SearchExpression function, SearchExpression... arguments) {
		SearchExpression result = function;
		for (SearchExpression argument : arguments) {
			result = new Call(result, argument);
		}
		return result;
	}

	/**
	 * The recursive application of a producer function to a start value.
	 *
	 * @param start
	 *        The expression providing the start value (either a single value or a collection of
	 *        values).
	 * @param fun
	 *        The function to apply to each start value and recursively to each result of a previous
	 *        application result.
	 * @param minDepth
	 *        Minimum function applications to perform for an intermediate result to be included in
	 *        the final result collection.
	 * @param maxDepth
	 *        Maximum function applications to perform for an intermediate result to be included in
	 *        the final result collection. The value <code>-1</code> means infinite depth.
	 * @return {@link SearchExpression} returning the collection of each (intermediate) result of
	 *         recursively applying the given function to the given start value(s). Intermediate
	 *         results are included in the result, if at least <code>minDepth</code> and at most
	 *         <code>maxDepth</code> function applications were performed to create a value.
	 */
	public static Recursion recursion(SearchExpression start, SearchExpression fun, SearchExpression minDepth,
			SearchExpression maxDepth) {
		return new Recursion(start, fun, minDepth, maxDepth);
	}

	/**
	 * Creates an {@link Var} expression.
	 * 
	 * @param name
	 *        See {@link Var#getName()}.
	 */
	public static Var var(Object name) {
		return new Var(name);
	}

	/**
	 * Creates an {@link IsEqual} expression.
	 * 
	 * @param left
	 *        See {@link IsEqual#getLeft()}.
	 * @param right
	 *        See {@link IsEqual#getRight()}.
	 */
	public static IsEqual isEqual(SearchExpression left, SearchExpression right) {
		return new IsEqual(left, right);
	}

	/**
	 * Creates a {@link CompareOp} expression.
	 *
	 * @param kind
	 *        See {@link CompareOp#getKind()}.
	 * @param left
	 *        See {@link CompareOp#getLeft()}.
	 * @param right
	 *        See {@link CompareOp#getRight()}.
	 */
	public static CompareOp compareOp(CompareKind kind, SearchExpression left, SearchExpression right) {
		return new CompareOp(kind, left, right);
	}

	/**
	 * Creates a {@link Compare} expression.
	 *
	 * @param left
	 *        See {@link Compare#getLeft()}.
	 * @param right
	 *        See {@link Compare#getRight()}.
	 */
	public static Compare compare(SearchExpression left, SearchExpression right) {
		return new Compare(left, right);
	}

	/**
	 * Creates a {@link Round} expression.
	 *
	 * @param left
	 *        See {@link Round#getLeft()}.
	 * @param right
	 *        See {@link Round#getRight()}.
	 */
	public static Round round(SearchExpression left, SearchExpression right) {
		return new Round(left, right);
	}

	/**
	 * Creates a {@link GetDay} expression.
	 *
	 * @param value
	 *        See {@link GetDay#getArgument()}.
	 */
	public static GetDay day(SearchExpression value) {
		return new GetDay(value);
	}

	/**
	 * Creates an {@link IsStringEqual} expression.
	 * 
	 * @param left
	 *        See {@link IsStringEqual#getLeft()}.
	 * @param right
	 *        See {@link IsStringEqual#getRight()}.
	 * @param caseSensitive
	 *        See {@link IsStringEqual#isCaseSensitive()}.
	 */
	public static IsStringEqual isStringEqual(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		return new IsStringEqual(left, right, caseSensitive);
	}

	/**
	 * Creates an {@link IsStringGreater} expression.
	 * 
	 * @param left
	 *        See {@link IsStringGreater#getLeft()}.
	 * @param right
	 *        See {@link IsStringGreater#getRight()}.
	 * @param caseSensitive
	 *        See {@link IsStringGreater#isCaseSensitive()}.
	 */
	public static IsStringGreater isStringGreater(SearchExpression left, SearchExpression right,
			boolean caseSensitive) {
		return new IsStringGreater(left, right, caseSensitive);
	}

	/**
	 * Creates an {@link StringContains} expression.
	 * 
	 * @param left
	 *        See {@link StringContains#getLeft()}.
	 * @param right
	 *        See {@link StringContains#getRight()}.
	 * @param caseSensitive
	 *        See {@link StringContains#isCaseSensitive()}.
	 */
	public static StringContains stringContains(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		return new StringContains(left, right, caseSensitive);
	}

	/**
	 * Creates an {@link StringStartsWith} expression.
	 * 
	 * @param left
	 *        See {@link StringStartsWith#getLeft()}.
	 * @param right
	 *        See {@link StringStartsWith#getRight()}.
	 * @param caseSensitive
	 *        See {@link StringStartsWith#isCaseSensitive()}.
	 */
	public static StringStartsWith stringStartsWith(SearchExpression left, SearchExpression right,
			boolean caseSensitive) {
		return new StringStartsWith(left, right, caseSensitive);
	}

	/**
	 * Creates an {@link StringEndsWith} expression.
	 * 
	 * @param left
	 *        See {@link StringEndsWith#getLeft()}.
	 * @param right
	 *        See {@link StringEndsWith#getRight()}.
	 * @param caseSensitive
	 *        See {@link StringEndsWith#isCaseSensitive()}.
	 */
	public static StringEndsWith stringEndsWith(SearchExpression left, SearchExpression right, boolean caseSensitive) {
		return new StringEndsWith(left, right, caseSensitive);
	}

	/**
	 * Creates an {@link IsEmpty} expression.
	 * 
	 * @param arg
	 *        See {@link IsEmpty#getArgument()}.
	 */
	public static IsEmpty isEmpty(SearchExpression arg) {
		return new IsEmpty(arg);
	}

	/**
	 * Creates an {@link Literal} expression.
	 * 
	 * @param value
	 *        See {@link Literal#getValue()}.
	 */
	public static Literal literal(Object value) {
		return new Literal(value);
	}

	/**
	 * Creates an {@link And} expression.
	 * 
	 * @param left
	 *        See {@link And#getLeft()}.
	 * @param right
	 *        See {@link And#getRight()}.
	 */
	public static And and(SearchExpression left, SearchExpression right) {
		return new And(left, right);
	}

	/**
	 * Creates an {@link Or} expression.
	 * 
	 * @param left
	 *        See {@link Or#getLeft()}.
	 * @param right
	 *        See {@link Or#getRight()}.
	 */
	public static Or or(SearchExpression left, SearchExpression right) {
		return new Or(left, right);
	}

	/**
	 * Creates an {@link Not} expression.
	 * 
	 * @param arg
	 *        See {@link Not#getArgument()}.
	 */
	public static Not not(SearchExpression arg) {
		return new Not(arg);
	}

	/**
	 * Creates an {@link ArithmeticExpr}.
	 */
	public static ArithmeticExpr arithmetic(ArithmeticExpr.Op op, SearchExpression left, SearchExpression right) {
		return new ArithmeticExpr(op, left, right);
	}

	/**
	 * Creates an {@link IfElse} expression.
	 * 
	 * @param condition
	 *        See {@link IfElse#getCondition()}.
	 * @param ifClause
	 *        See {@link IfElse#getIfClause()}.
	 * @param elseClause
	 *        See {@link IfElse#getElseClause()}.
	 */
	public static IfElse ifElse(SearchExpression condition, SearchExpression ifClause, SearchExpression elseClause) {
		return new IfElse(condition, ifClause, elseClause);
	}

	/**
	 * Creates a {@link Sort} expression.
	 */
	public static Sort sort(SearchExpression list, SearchExpression comparator) {
		return new Sort(list, comparator);
	}

	/**
	 * Creates a {@link Desc} expression.
	 */
	public static Desc desc(SearchExpression key) {
		return new Desc(key);
	}

	/**
	 * Creates a {@link ListExpr} expression.
	 */
	public static ListExpr list(SearchExpression... expressions) {
		return new ListExpr(expressions);
	}

	/**
	 * Creates a {@link Size} expression.
	 */
	public static Size size(SearchExpression list) {
		return new Size(list);
	}

	/**
	 * Creates a {@link Length} expression.
	 */
	public static Length length(SearchExpression string) {
		return new Length(string);
	}

	/**
	 * Creates a {@link FirstElement} expression.
	 */
	public static FirstElement firstElement(SearchExpression list) {
		return new FirstElement(list, NO_ARGS);
	}

	/**
	 * Creates a {@link LastElement} expression.
	 */
	public static LastElement lastElement(SearchExpression list) {
		return new LastElement(list, NO_ARGS);
	}

	/**
	 * Creates a {@link ElementAt} expression.
	 */
	public static ElementAt elementAt(SearchExpression list, SearchExpression index) {
		return new ElementAt(list, index);
	}

	/**
	 * Creates a {@link ElementIndex} expression.
	 */
	public static ElementIndex elementIndex(SearchExpression list, SearchExpression element) {
		return new ElementIndex(list, element);
	}

	/**
	 * Creates a {@link ResKeyArguments} expression.
	 *
	 * @param self
	 *        See {@link ResKeyArguments#getSelf()}.
	 * @param arguments
	 *        See {@link ResKeyArguments#getArguments()}.
	 */
	public static ResKeyArguments reskeyArguments(SearchExpression self, SearchExpression... arguments) {
		return new ResKeyArguments(self, arguments);
	}

	/**
	 * Creates a {@link HtmlMacro} expression.
	 */
	public static HtmlMacro html(SearchExpression... contents) {
		return new HtmlMacro(contents);
	}

	/**
	 * Creates a {@link TagMacro} expression.
	 */
	public static TagMacro tag(String tag, boolean emptyTag, SearchExpression... attributes) {
		return new TagMacro(tag, emptyTag, attributes);
	}

	/**
	 * Creates a {@link AttributeMacro} expression.
	 */
	public static AttributeMacro attr(String name, SearchExpression value) {
		boolean cssAttribute = HTMLConstants.CLASS_ATTR.equals(name);
		boolean dynamicSafety = dynamicSafety(name, value);
		return new AttributeMacro(cssAttribute, dynamicSafety, name, value);
	}

	private static boolean dynamicSafety(String name, SearchExpression value) {
		if (value instanceof Literal) {
			if (((Literal) value).getValue() instanceof RenderedFragment) {
				// This situation is statically checked.
				return false;
			}
		}

		// Dynamic safety is only required for protected attributes.
		return SafeHTML.getInstance().getAttributeChecker(name) != null;
	}

	/**
	 * Creates a {@link ReduceOperation} expression.
	 * 
	 * @see Expr
	 */
	public static ReduceOperation reduce(SearchExpression self, SearchExpression... arguments) {
		return new ReduceOperation(self, arguments);
	}

}
