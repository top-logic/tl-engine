/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.model.search.expr.visit.Copy;

/**
 * Utilities for conveniently constructing {@link SearchExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SearchExpressions extends SearchExpressionFactory {

	/**
	 * Creates a local variable binding.
	 * 
	 * @param name
	 *        The variable name to bind in the given context.
	 * @param init
	 *        The value to initialize the variable with.
	 * @param body
	 *        The context that is evaluated with the given variable binding in scope.
	 */
	public static Call let(Object name, SearchExpression init, SearchExpression body) {
		return call(lambda(name, body), init);
	}

	/**
	 * Creates an {@link Literal} expression for the empty set.
	 */
	public static Literal literalEmptySet() {
		return literal(Collections.emptySet());
	}

	/**
	 * Creates an {@link Literal} representing <code>true</code>.
	 */
	public static Literal literalTrue() {
		return literal(true);
	}

	/**
	 * Creates an {@link Literal} representing <code>false</code>.
	 */
	public static Literal literalFalse() {
		return literal(false);
	}

	/**
	 * Creates an addition.
	 */
	public static ArithmeticExpr add(SearchExpression left, SearchExpression right) {
		return arithmetic(ArithmeticExpr.Op.ADD, left, right);
	}

	/**
	 * Creates a subtraction.
	 */
	public static ArithmeticExpr sub(SearchExpression left, SearchExpression right) {
		return arithmetic(ArithmeticExpr.Op.SUB, left, right);
	}

	/**
	 * Creates a multiplication.
	 */
	public static ArithmeticExpr mul(SearchExpression left, SearchExpression right) {
		return arithmetic(ArithmeticExpr.Op.MUL, left, right);
	}

	/**
	 * Creates a division.
	 */
	public static ArithmeticExpr div(SearchExpression left, SearchExpression right) {
		return arithmetic(ArithmeticExpr.Op.DIV, left, right);
	}

	/**
	 * Creates a modulo operation.
	 */
	public static ArithmeticExpr mod(SearchExpression left, SearchExpression right) {
		return arithmetic(ArithmeticExpr.Op.MOD, left, right);
	}

	/**
	 * Creates an {@link SearchExpression} expression comparing two floating point numbers rounded
	 * to a certain precision for equality.
	 * 
	 * @param left
	 *        See {@link BinaryOperation#getLeft()}.
	 * @param right
	 *        See {@link BinaryOperation#getRight()}.
	 * @param precision
	 *        Number of significant digits after the comma.
	 */
	public static SearchExpression isFloatEqual(SearchExpression left, SearchExpression right,
			SearchExpression precision) {
		return isEqual(round(left, precision), round(right, Copy.copy(precision)));
	}

	/**
	 * Creates an {@link SearchExpression} expression comparing the days of the given dates for
	 * equality.
	 */
	public static SearchExpression isDateEqual(SearchExpression left, SearchExpression right) {
		return isEqual(day(left), day(right));
	}

	/**
	 * Creates an {@link SearchExpression} expression comparing the given two values.
	 */
	public static SearchExpression isGreater(SearchExpression left, SearchExpression right) {
		return compareOp(CompareKind.GT, left, right);
	}

	/**
	 * Creates an {@link SearchExpression} comparing the left expression with the right expression
	 * rounded to the given precision.
	 */
	public static SearchExpression isFloatGreater(SearchExpression left, SearchExpression right,
			SearchExpression precision) {
		return compareFloat(CompareKind.GT, left, right, precision);
	}

	/**
	 * Creates an {@link SearchExpression} comparing the left expression with the right expression
	 * rounded to the given precision.
	 */
	public static SearchExpression compareFloat(CompareKind kind, SearchExpression left,
			SearchExpression right, SearchExpression precision) {
		return compareOp(kind, round(left, precision), round(right, Copy.copy(precision)));
	}

	/**
	 * Creates an {@link SearchExpression} expression comparing the days of the given dates.
	 */
	public static SearchExpression isDateGreater(SearchExpression left, SearchExpression right) {
		return compareDate(CompareKind.GT, left, right);
	}

	/**
	 * Creates an {@link SearchExpression} expression comparing the days of the given dates.
	 */
	public static SearchExpression compareDate(CompareKind kind, SearchExpression left,
			SearchExpression right) {
		return compareOp(kind, day(left), day(right));
	}

	/**
	 * Creates a {@link Sort} expression.
	 */
	public static Sort sort(SearchExpression list) {
		NamedConstant x = new NamedConstant("x");
		NamedConstant y = new NamedConstant("x");
		return new Sort(list, lambda(x, lambda(y, compare(var(x), var(y)))));
	}

	/**
	 * Creates the identity function.
	 */
	public static Lambda identity() {
		NamedConstant x = new NamedConstant("x");
		return lambda(x, var(x));
	}

	/**
	 * Creates a {@link #literal(Object)} {@link HTMLFragment} printing the given text.
	 */
	public static SearchExpression text(String textWithEntities) {
		return SearchExpressionFactory.literal(Fragments.htmlSource(textWithEntities));
	}

	/**
	 * Creates a {@link #literal(Object)} {@link HTMLFragment} closing the given tag.
	 */
	public static SearchExpression endTag(String tag) {
		return SearchExpressionFactory.literal(new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.endTag(tag);
			}
		});
	}

}
