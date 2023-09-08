/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter.transform;

import java.util.Collections;

import com.top_logic.basic.treexf.Capture;
import com.top_logic.basic.treexf.Expr;
import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.TransformFactory;
import com.top_logic.basic.treexf.Value;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.ContainsElement;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.InstanceOf;
import com.top_logic.model.search.expr.Intersection;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SingleElement;
import com.top_logic.model.search.expr.Singleton;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Var;

/**
 * Factory for creating {@link Node}-wrapped {@link SearchExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericSearchExprFactory extends TransformFactory {

	/**
	 * @see IfElse
	 */
	public static Expr ifElse(Node condition, Node ifClause, Node elseClause) {
		return expr(IfElse.class, condition, ifClause, elseClause);
	}

	/**
	 * @see All
	 */
	public static Expr all(Value type) {
		return expr(All.class, type);
	}

	/**
	 * @see InstanceOf
	 */
	public static Expr instanceOf(Value type, Node value) {
		return expr(InstanceOf.class, type, value);
	}

	/**
	 * The {@link Literal} empty set.
	 */
	public static Expr literalEmptySet() {
		return literal(Collections.emptySet());
	}

	/**
	 * @see Literal
	 */
	public static Expr literal(Object value) {
		return expr(Literal.class, value(value));
	}

	/**
	 * @see Singleton
	 */
	public static Expr singleton(Node y) {
		return expr(Singleton.class, y);
	}

	/**
	 * @see SingleElement
	 */
	public static Expr singleElement(Node s) {
		return expr(SingleElement.class, s);
	}

	/**
	 * @see IsEqual
	 */
	public static Expr isEqual(Node x, Node y) {
		return expr(IsEqual.class, x, y);
	}

	/**
	 * @see Intersection
	 */
	public static Expr intersection(Node s1, Node s2) {
		return expr(Intersection.class, s1, s2);
	}

	/**
	 * @see Filter
	 */
	public static Expr filter(Node set, Node function) {
		return expr(Filter.class, set, function);
	}

	/**
	 * @see Union
	 */
	public static Expr union(Node set1, Node set2) {
		return expr(Union.class, set1, set2);
	}

	/**
	 * @see Lambda
	 */
	public static Expr lambda(Value var, Node body) {
		return expr(Lambda.class, var, body);
	}

	/**
	 * @see Var
	 */
	public static Expr var(Value name) {
		return expr(Var.class, name);
	}

	/**
	 * @see ContainsElement
	 */
	public static Expr containsElement(Node set, Node value) {
		return expr(ContainsElement.class, set, value);
	}

	/**
	 * @see Not
	 */
	public static Expr not(Node argument) {
		return expr(Not.class, argument);
	}

	/**
	 * @see Or
	 */
	public static Expr or(Node left, Node right) {
		return expr(Or.class, left, right);
	}

	/**
	 * @see And
	 */
	public static Expr and(Node left, Node right) {
		return expr(And.class, left, right);
	}

	/**
	 * Bound local variable within a body expression.
	 * 
	 * @param var
	 *        The variable to bind.
	 * @param init
	 *        The value to initialize the variable with.
	 * @param body
	 *        The body using the variable.
	 */
	public static Expr let(Value var, Node init, Node body) {
		return call(lambda(var, body), init);
	}

	/**
	 * @see Call
	 */
	public static Expr call(Node function, Node value) {
		return expr(Call.class, function, value);
	}

	/**
	 * @see Access
	 */
	public static Expr access(Node self, Value part) {
		return expr(Access.class, self, part);
	}

	/**
	 * Creates a {@link BooleanExprCapture}.
	 * 
	 * @param name
	 *        See {@link Capture#getName()}.
	 */
	public static BooleanExprCapture booleanExprCapture(String name) {
		return new BooleanExprCapture(name);
	}

	/**
	 * Creates a {@link ElementFromSingletonCapture}.
	 * 
	 * @param name
	 *        See {@link Capture#getName()}.
	 */
	public static ElementFromSingletonCapture elementFromSingletonCapture(String name) {
		return new ElementFromSingletonCapture(name);
	}

	/**
	 * Creates a {@link InvertedBooleanValueCapture}.
	 * 
	 * @param name
	 *        See {@link Capture#getName()}.
	 */
	public static InvertedBooleanValueCapture invertedBooleanCapture(String name) {
		return new InvertedBooleanValueCapture(name);
	}

}
