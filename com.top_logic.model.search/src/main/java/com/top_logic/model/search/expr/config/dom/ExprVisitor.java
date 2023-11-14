/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.dom;

import com.top_logic.model.search.expr.config.dom.Expr.Method;
import com.top_logic.model.search.expr.config.dom.Expr.Add;
import com.top_logic.model.search.expr.config.dom.Expr.And;
import com.top_logic.model.search.expr.config.dom.Expr.Apply;
import com.top_logic.model.search.expr.config.dom.Expr.Assign;
import com.top_logic.model.search.expr.config.dom.Expr.At;
import com.top_logic.model.search.expr.config.dom.Expr.Attribute;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContents;
import com.top_logic.model.search.expr.config.dom.Expr.Block;
import com.top_logic.model.search.expr.config.dom.Expr.Cmp;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.dom.Expr.Div;
import com.top_logic.model.search.expr.config.dom.Expr.EmbeddedExpression;
import com.top_logic.model.search.expr.config.dom.Expr.EndTag;
import com.top_logic.model.search.expr.config.dom.Expr.Eq;
import com.top_logic.model.search.expr.config.dom.Expr.False;
import com.top_logic.model.search.expr.config.dom.Expr.Html;
import com.top_logic.model.search.expr.config.dom.Expr.Mod;
import com.top_logic.model.search.expr.config.dom.Expr.ModuleLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Mul;
import com.top_logic.model.search.expr.config.dom.Expr.Not;
import com.top_logic.model.search.expr.config.dom.Expr.Null;
import com.top_logic.model.search.expr.config.dom.Expr.NumberLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Or;
import com.top_logic.model.search.expr.config.dom.Expr.PartLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.ResKeyReference;
import com.top_logic.model.search.expr.config.dom.Expr.SingletonLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.StartTag;
import com.top_logic.model.search.expr.config.dom.Expr.StringLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Sub;
import com.top_logic.model.search.expr.config.dom.Expr.TextContent;
import com.top_logic.model.search.expr.config.dom.Expr.True;
import com.top_logic.model.search.expr.config.dom.Expr.Tuple;
import com.top_logic.model.search.expr.config.dom.Expr.TypeLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Var;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;

/**
 * Visitor interface for the {@link Expr} hierarchy.
 * 
 * @param <R>
 *        The visit result type.
 * @param <A>
 *        The visit argument type.
 * @param <E>
 *        The visit exception type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExprVisitor<R, A, E extends Throwable> {

	/** Visits {@link Null}. */
	R visit(Null expr, A arg) throws E;

	/** Visits {@link True}. */
	R visit(True expr, A arg) throws E;

	/** Visits {@link False}. */
	R visit(False expr, A arg) throws E;

	/** Visits {@link StringLiteral}. */
	R visit(StringLiteral expr, A arg) throws E;

	/** Visits {@link Html}. */
	R visit(Html expr, A arg) throws E;

	/**
	 * Visit case for {@link TextContent}.
	 */
	R visit(TextContent expr, A arg) throws E;

	/**
	 * Visit case for {@link StartTag}.
	 */
	R visit(StartTag expr, A arg) throws E;

	/**
	 * Visit case for {@link StartTag}.
	 */
	R visit(Attribute expr, A arg) throws E;

	/**
	 * Visit case for {@link EndTag}.
	 */
	R visit(EndTag expr, A arg) throws E;

	/**
	 * Visit case for {@link AttributeContents}.
	 */
	R visit(AttributeContents expr, A arg) throws E;

	/**
	 * Visit case for {@link EmbeddedExpression}.
	 */
	R visit(EmbeddedExpression expr, A arg) throws E;

	/** Visits {@link ResKeyReference}. */
	R visit(ResKeyReference expr, A arg) throws E;

	/** Visits {@link ResKeyLiteral}. */
	R visit(ResKeyLiteral expr, A arg) throws E;

	/** Visits {@link NumberLiteral}. */
	R visit(NumberLiteral expr, A arg) throws E;

	/** Visits {@link ModuleLiteral}. */
	R visit(ModuleLiteral expr, A arg) throws E;

	/** Visits {@link SingletonLiteral}. */
	R visit(SingletonLiteral expr, A arg) throws E;

	/** Visits {@link TypeLiteral}. */
	R visit(TypeLiteral expr, A arg) throws E;

	/** Visits {@link PartLiteral}. */
	R visit(PartLiteral expr, A arg) throws E;

	/** Visits {@link Wrapped}. */
	R visit(Wrapped expr, A arg) throws E;

	/** Visits {@link Add}. */
	R visit(Add expr, A arg) throws E;

	/** Visits {@link Sub}. */
	R visit(Sub expr, A arg) throws E;

	/** Visits {@link Mul}. */
	R visit(Mul expr, A arg) throws E;

	/** Visits {@link Div}. */
	R visit(Div expr, A arg) throws E;

	/** Visits {@link Mod}. */
	R visit(Mod expr, A arg) throws E;

	/** Visits {@link Cmp}. */
	R visit(Cmp expr, A arg) throws E;

	/** Visits {@link And}. */
	R visit(And expr, A arg) throws E;

	/** Visits {@link Or}. */
	R visit(Or expr, A arg) throws E;

	/** Visits {@link Not}. */
	R visit(Not expr, A arg) throws E;

	/** Visits {@link Eq}. */
	R visit(Eq expr, A arg) throws E;

	/** Visits {@link Block}. */
	R visit(Block expr, A arg) throws E;

	/** Visits {@link Define}. */
	R visit(Define expr, A arg) throws E;

	/** Visits {@link Apply}. */
	R visit(Apply expr, A arg) throws E;

	/** Visits {@link Assign}. */
	R visit(At expr, A arg) throws E;

	/** Visits {@link Assign}. */
	R visit(Assign expr, A arg) throws E;

	/** Visits {@link Tuple}. */
	R visit(Tuple expr, A arg) throws E;

	/** Visits {@link Var}. */
	R visit(Var expr, A arg) throws E;

	/** Visits {@link Method}. */
	R visit(Method expr, A arg) throws E;

}
