/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;


/**
 * Factory for {@link Node}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransformFactory {

	/**
	 * Creates an {@link Expr}.
	 * 
	 * @param type
	 *        See {@link Expr#getType()}
	 * @param children
	 *        See {@link Expr#getChild(int)}
	 */
	public static Expr expr(Object type, Node... children) {
		return new Expr(type, children);
	}

	/**
	 * Creates a {@link LiteralValue}.
	 * 
	 * @param value
	 *        See {@link LiteralValue#getValue()}.
	 */
	public static Value value(Object value) {
		return new LiteralValue(value);
	}

	/**
	 * Creates a {@link ExprCapture}.
	 * 
	 * @param name
	 *        See {@link Capture#getName()}.
	 */
	public static ExprCapture exprCapture(String name) {
		return new ExprCapture(name);
	}

	/**
	 * Creates a {@link TransformCapture}.
	 * 
	 * @param name
	 *        See {@link Capture#getName()}.
	 * @param transformations
	 *        {@link Transformation}s to be applied to the matched part during expansion.
	 */
	public static Node exprPattern(String name, Transformation... transformations) {
		return new TransformCapture(name, transformations);
	}

	/**
	 * Creates a {@link ExcludeCapture}.
	 * 
	 * @see ExcludeCapture#ExcludeCapture(String, Node...)
	 */
	public static ExprCapture excludeExpr(String name, Node... exclude) {
		return new ExcludeCapture(name, exclude);
	}

	/**
	 * Creates a {@link ValueCapture}.
	 * 
	 * @param name
	 *        See {@link Capture#getName()}.
	 */
	public static ValueCapture valueCapture(String name) {
		return new ValueCapture(name);
	}

	/**
	 * Creates a {@link NewIdReplacement}.
	 * 
	 * @param name
	 *        See {@link NewIdReplacement#getName()}.
	 */
	public static NewIdReplacement newIdReplacement(String name) {
		return new NewIdReplacement(name);
	}

	/**
	 * Creates a {@link Transformation}.
	 * 
	 * @param pattern
	 *        See {@link Transformation#getPattern()}.
	 * @param replacement
	 *        See {@link Transformation#getReplacement()}.
	 */
	public static Transformation transformation(Node pattern, Node replacement) {
		return new Transformation(pattern, replacement);
	}

}
