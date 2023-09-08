/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter.transform;

import static com.top_logic.basic.treexf.TransformFactory.*;
import static com.top_logic.model.search.expr.interpreter.transform.GenericSearchExprFactory.*;

import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.Transformation;
import com.top_logic.basic.treexf.Value;
import com.top_logic.basic.treexf.ValueCapture;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.ContainsElement;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.interpreter.SubExpressionPullOut;

/**
 * Static factory for {@link Transformation}s used for structurally simplifying search expressions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Transformations {

	/**
	 * The set of transformations.
	 */
	public static Transformation[] TRANSFORMATIONS = {
		joinUnionsOfSameBase(),
		splitAndFilter(),
		inlineCalls(),
		filterWithContainsElement(),

		filterWithIsEqual1(),
		filterWithIsEqual2(),

		distributeContainsElement(),
		allContainsElement(),
		intersectionWithAll1(),
		intersectionWithAll2(),
		emptySetContains(),
		singletonContains(),
		singletonLiteralContains(),
		singleElementFromEmptySet(),
		singleElementFromSingleton(),

		emptyUnion1(),
		emptyUnion2(),
		emptyIntersection1(),
		emptyIntersection2(),
		trueFilter(),
		falseFilter(),

		trueIfElse(),
		falseIfElse(),
		booleanIfElse(),
		andFalse1(),
		andFalse2(),
		andTrue1(),
		andTrue2(),
		orFalse1(),
		orFalse2(),
		orTrue1(),
		orTrue2(),

		literalNot(),
		duplicateNot(),

		distributeNotAnd(),
		distributeNotOr(),
		distributeOrAnd1(),
		distributeOrAnd2(),

		pulloutUnionFromFilter(),
		distributeIntersection1(),
		distributeIntersection2(),
	};

	/**
	 * Filter of union is union of filters (pull up union).
	 */
	public static Transformation pulloutUnionFromFilter() {
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");
		Node f = exprCapture("f");

		return transformation(
			filter(union(s1, s2), f),
			union(filter(s1, f), filter(s2, f)));
	}

//	private static TreePattern pulloutFilterFromIntersectionWithSameFunction() {
//		Tree s1 = expr("s1");
//		Tree s2 = expr("s2");
//		ValueVar x = valueVar("x");
//		Tree b1 = expr("b1");
//		
//		ValueVar y = valueVar("y");
//		Tree b2 = exprReplace(b1, pattern(var(x), var(y)));
//
//		return pattern(
//			intersection(filter(s1, lambda(x, b1)), filter(s2, lambda(y, b2))),
//			filter(intersection(s1, s2), lambda(x, b1)));
//	}

	/**
	 * Intersection of union is union of intersection (pull up union).
	 */
	private static Transformation distributeIntersection1() {
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");
		Node s3 = exprCapture("s3");

		return transformation(
			intersection(union(s1, s2), s3),
			union(intersection(s1, s3), intersection(s2, s3)));
	}

	/**
	 * Symmetric version of {@link #distributeIntersection1()}
	 */
	private static Transformation distributeIntersection2() {
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");
		Node s3 = exprCapture("s3");

		return transformation(
			intersection(s3, union(s1, s2)),
			union(intersection(s3, s1), intersection(s3, s2)));
	}

	/**
	 * {@link Union} of {@link Filter}s with the same {@link Filter#getBase() base} is filter of
	 * that set with {@link Or}ed filter expression.
	 */
	private static Transformation joinUnionsOfSameBase() {
		Node s1 = exprCapture("s1");
		Node f1 = exprCapture("f1");
		Node f2 = exprCapture("f2");

		return transformation(
			union(filter(s1, f1), filter(s1, f2)),
			filter(s1, or(f1, f2)));
	}

	/**
	 * {@link Filter} with an {@link And} condition is equivalent to to two nested {@link Filter}
	 * expressions.
	 */
	public static Transformation splitAndFilter() {
		Node s = exprCapture("s");
		Value x = valueCapture("x");
		Value y = newIdReplacement("y");
		Node b1 = exprCapture("b1");
		Node b2 = exprPattern("b2", transformation(var(x), var(y)));

		return transformation(
			filter(s, lambda(x, and(b1, b2))),
			filter(filter(s, lambda(x, b1)), lambda(y, b2)));
	}

	/**
	 * Evaluate {@link Lambda} expressions, where the argument is given.
	 * 
	 * <p>
	 * Note: This transformation reverts {@link SubExpressionPullOut}. Since the variable bound in
	 * the lambda expression may be used more than once, this transformation may result in duplicate
	 * work during interpretation.
	 * </p>
	 */
	public static Transformation inlineCalls() {
		ValueCapture x = valueCapture("x");
		Node argument = exprCapture("argument");

		Node body = exprPattern("body", transformation(var(x), argument));

		return transformation(
			call(lambda(x, body), argument),
			body);
	}

	private static Transformation filterWithContainsElement() {
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");
		ValueCapture x = valueCapture("x");

		return transformation(
			filter(s1, lambda(x, containsElement(s2, var(x)))),
			intersection(s1, s2));
	}

	/**
	 * Transforms a {@link Filter} with a constant {@link IsEqual} filter expression into a
	 * {@link ContainsElement} test.
	 */
	public static Transformation filterWithIsEqual1() {
		Node s = exprCapture("s1");
		ValueCapture x = valueCapture("x");
		Node y = excludeExpr("y", var(x));

		return transformation(
			filter(s, lambda(x, isEqual(var(x), y))),
			ifElse(containsElement(s, y), singleton(y), literalEmptySet()));
	}

	private static Transformation filterWithIsEqual2() {
		Node s = exprCapture("s1");
		ValueCapture x = valueCapture("x");
		Node y = excludeExpr("y", var(x));

		return transformation(
			filter(s, lambda(x, isEqual(y, var(x)))),
			ifElse(containsElement(s, y), singleton(y), literalEmptySet()));
	}

	private static Transformation distributeContainsElement() {
		Node c = exprCapture("c");
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");
		Node x = exprCapture("x");

		return transformation(
			containsElement(ifElse(c, s1, s2), x),
			ifElse(c, containsElement(s1, x), containsElement(s2, x)));
	}

	private static Transformation allContainsElement() {
		Value t = valueCapture("T");
		Node x = exprCapture("x");

		return transformation(
			containsElement(all(t), x),
			instanceOf(t, x));
	}

	private static Transformation intersectionWithAll1() {
		Value t = valueCapture("T");
		Node s = exprCapture("s");
		Value x = newIdReplacement("x");

		return transformation(
			intersection(s, all(t)),
			filter(s, lambda(x, instanceOf(t, var(x)))));
	}

	private static Transformation intersectionWithAll2() {
		Value t = valueCapture("T");
		Node s = exprCapture("s");
		Value x = newIdReplacement("x");

		return transformation(
			intersection(all(t), s),
			filter(s, lambda(x, instanceOf(t, var(x)))));
	}

	/**
	 * {@link Or} of {@link And} is {@link And} of {@link Or} (and pullout).
	 */
	public static Transformation distributeOrAnd1() {
		Node b1 = exprCapture("b1");
		Node b2 = exprCapture("b2");
		Node b3 = exprCapture("b3");

		return transformation(
			or(and(b1, b2), b3),
			and(or(b1, b3), or(b2, b3)));
	}

	/**
	 * Symmetric case to {@link #distributeOrAnd1()} (and pullout).
	 */
	public static Transformation distributeOrAnd2() {
		Node b1 = exprCapture("b1");
		Node b2 = exprCapture("b2");
		Node b3 = exprCapture("b3");

		return transformation(
			or(b1, and(b2, b3)),
			and(or(b1, b2), or(b1, b3)));
	}

	/**
	 * {@link Not} of {@link Or} is {@link And} of {@link Not} (and pullout).
	 */
	public static Transformation distributeNotOr() {
		Node b1 = exprCapture("b1");
		Node b2 = exprCapture("b2");

		return transformation(
			not(or(b1, b2)),
			and(not(b1), not(b2)));
	}

	/**
	 * {@link Not} of {@link And} is {@link Or} of {@link Not} (not pushdown).
	 */
	public static Transformation distributeNotAnd() {
		Node b1 = exprCapture("b1");
		Node b2 = exprCapture("b2");

		return transformation(
			not(and(b1, b2)),
			or(not(b1), not(b2)));
	}

	/**
	 * Contains in empty set is <code>false</code>.
	 */
	private static Transformation emptySetContains() {
		Node x = exprCapture("x");

		return transformation(
			containsElement(literalEmptySet(), x),
			literal(false));
	}

	/**
	 * Contains in singleton set is equals.
	 */
	private static Transformation singletonContains() {
		Node x = exprCapture("x");
		Node y = exprCapture("y");

		return transformation(
			containsElement(singleton(y), x),
			isEqual(x, y));
	}

	private static Transformation singletonLiteralContains() {
		Node x = exprCapture("x");
		Node singleton, element;
		singleton = element = elementFromSingletonCapture("y");

		return transformation(
			containsElement(literal(singleton), x),
			isEqual(x, literal(element)));
	}

	private static Transformation singleElementFromEmptySet() {
		return transformation(
			singleElement(literalEmptySet()),
			literal(null));
	}

	private static Transformation singleElementFromSingleton() {
		Node x = exprCapture("x");

		return transformation(
			singleElement(singleton(x)),
			x);
	}

	/**
	 * Eliminates if-else with boolean branches.
	 */
	public static Transformation booleanIfElse() {
		Node c = exprCapture("c");
		Node b1 = booleanExprCapture("b1");
		Node b2 = booleanExprCapture("b2");

		return transformation(
			ifElse(c, b1, b2),
			or(and(c, b1), and(not(c), b2)));
	}

	/**
	 * Remove empty sets from unions.
	 */
	public static Transformation emptyUnion1() {
		Node s = exprCapture("s");
		Node empty = literalEmptySet();

		return transformation(
			union(s, empty),
			s);
	}

	/**
	 * Symmetric case to {@link #emptyUnion1()}.
	 */
	public static Transformation emptyUnion2() {
		Node s = exprCapture("s");
		Node empty = literalEmptySet();

		return transformation(
			union(empty, s),
			s);
	}

	/**
	 * Intersection with empty set is empty.
	 */
	public static Transformation emptyIntersection1() {
		Node s = exprCapture("s");
		Node empty = literalEmptySet();

		return transformation(
			intersection(s, empty),
			empty);
	}

	/**
	 * Symmetric case to {@link #emptyIntersection1()}.
	 */
	public static Transformation emptyIntersection2() {
		Node s = exprCapture("s");
		Node empty = literalEmptySet();

		return transformation(
			intersection(empty, s),
			empty);
	}

	/**
	 * Filter with <code>true</code> condition can be dropped.
	 */
	public static Transformation trueFilter() {
		Node s = exprCapture("s");
		Value x = valueCapture("x");

		return transformation(
			filter(s, lambda(x, literal(true))),
			s);
	}

	/**
	 * Filter with <code>false</code> condition is empty set.
	 */
	public static Transformation falseFilter() {
		Node s = exprCapture("s");
		Value x = valueCapture("x");

		return transformation(
			filter(s, lambda(x, literal(false))),
			literalEmptySet());
	}

	/**
	 * Filter of the empty set is the empty set.
	 */
	public static Transformation emptyFilter() {
		Node f = exprCapture("f");

		return transformation(
			filter(literalEmptySet(), f),
			literalEmptySet());
	}

	/**
	 * If-else with <code>true</code> condition is the first branch.
	 */
	public static Transformation trueIfElse() {
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");

		return transformation(
			ifElse(literal(true), s1, s2),
			s1);
	}

	/**
	 * If-else with <code>false</code> condition is the second branch.
	 */
	public static Transformation falseIfElse() {
		Node s1 = exprCapture("s1");
		Node s2 = exprCapture("s2");

		return transformation(
			ifElse(literal(false), s1, s2),
			s2);
	}

	/**
	 * And of <code>false</code> is <code>false</code>.
	 */
	public static Transformation andFalse1() {
		Node b = exprCapture("b");

		return transformation(
			and(
				literal(false),
				b),
			literal(false));
	}

	/**
	 * Symmetric case of {@link #andFalse1()}.
	 */
	public static Transformation andFalse2() {
		Node b = exprCapture("b");

		return transformation(
			and(
				b,
				literal(false)),
			literal(false));
	}

	/**
	 * {@link And} of <code>true</code> and some condition is the condition.
	 */
	public static Transformation andTrue1() {
		Node b = exprCapture("b");

		return transformation(
			and(
				literal(true),
				b),
			b);
	}

	/**
	 * Symmetric case of {@link #andTrue1()}.
	 */
	public static Transformation andTrue2() {
		Node b = exprCapture("b");

		return transformation(
			and(
				b,
				literal(true)),
			b);
	}

	/**
	 * {@link Or} of <code>false</code> and some condition is the condition.
	 */
	public static Transformation orFalse1() {
		Node b = exprCapture("b");

		return transformation(
			or(
				literal(false),
				b),
			b);
	}

	/**
	 * Symmetric case of {@link #orFalse1()}.
	 */
	public static Transformation orFalse2() {
		Node b = exprCapture("b");

		return transformation(
			or(
				b,
				literal(false)),
			b);
	}

	/**
	 * {@link Or} with <code>true</code> is <code>true</code>.
	 */
	public static Transformation orTrue1() {
		Node b = exprCapture("b");

		return transformation(
			or(
				literal(true),
				b),
			literal(true));
	}

	/**
	 * Symmetric case of {@link #orTrue1()}.
	 */
	public static Transformation orTrue2() {
		Node b = exprCapture("b");

		return transformation(
			or(
				b,
				literal(true)),
			literal(true));
	}

	/**
	 * Evaluates {@link Not} on a literal boolean.
	 */
	public static Transformation literalNot() {
		Node b, notB;
		b = notB = invertedBooleanCapture("b");

		return transformation(
			not(b),
			notB);
	}

	/**
	 * Removes duplicate {@link Not}s.
	 */
	public static Transformation duplicateNot() {
		Node b = exprCapture("b");

		return transformation(
			not(not(b)),
			b);
	}

}
