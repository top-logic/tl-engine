/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.visit;

import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.ArithmeticExpr;
import com.top_logic.model.search.expr.AssociationNavigation;
import com.top_logic.model.search.expr.At;
import com.top_logic.model.search.expr.Block;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.Compare;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.ContainsAll;
import com.top_logic.model.search.expr.ContainsElement;
import com.top_logic.model.search.expr.ContainsSome;
import com.top_logic.model.search.expr.Desc;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Flatten;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.GetDay;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.InstanceOf;
import com.top_logic.model.search.expr.Intersection;
import com.top_logic.model.search.expr.IsEmpty;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.IsStringEqual;
import com.top_logic.model.search.expr.IsStringGreater;
import com.top_logic.model.search.expr.KBQuery;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Length;
import com.top_logic.model.search.expr.ListExpr;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.Recursion;
import com.top_logic.model.search.expr.Referers;
import com.top_logic.model.search.expr.Round;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SingleElement;
import com.top_logic.model.search.expr.Singleton;
import com.top_logic.model.search.expr.Size;
import com.top_logic.model.search.expr.Sort;
import com.top_logic.model.search.expr.StringContains;
import com.top_logic.model.search.expr.StringEndsWith;
import com.top_logic.model.search.expr.StringStartsWith;
import com.top_logic.model.search.expr.TupleExpression;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;

/**
 * Visitor interface for {@link SearchExpression} models.
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The type of the arguemt to the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Visitor<R, A> {

	/**
	 * Visit case for {@link Lambda}.
	 */
	R visitLambda(Lambda expr, A arg);

	/**
	 * Visit case for {@link Call}.
	 */
	R visitCall(Call expr, A arg);

	/**
	 * Visit case for {@link Recursion}.
	 */
	R visitRecursion(Recursion expr, A arg);

	/**
	 * Visit case for {@link Var}.
	 */
	R visitVar(Var expr, A arg);

	/**
	 * Visit case for {@link All}.
	 */
	R visitAll(All expr, A arg);

	/**
	 * Visit case for {@link TupleExpression}.
	 */
	R visitTuple(TupleExpression expr, A arg);

	/**
	 * Visit case for {@link KBQuery}.
	 */
	R visitKBQuery(KBQuery expr, A arg);

	/**
	 * Visit case for {@link InstanceOf}.
	 */
	R visitInstanceOf(InstanceOf expr, A arg);

	/**
	 * Visit case for {@link Union}.
	 */
	R visitUnion(Union expr, A arg);

	/**
	 * Visit case for {@link Intersection}.
	 */
	R visitIntersection(Intersection expr, A arg);

	/**
	 * Visit case for {@link Foreach}.
	 */
	R visitForeach(Foreach expr, A arg);

	/**
	 * Visit case for {@link Flatten}.
	 */
	R visitFlatten(Flatten expr, A arg);

	/**
	 * Visit case for {@link Singleton}.
	 */
	R visitSingleton(Singleton expr, A arg);

	/**
	 * Visit case for {@link SingleElement}.
	 */
	R visitSingleElement(SingleElement expr, A arg);

	/**
	 * Visit case for {@link Filter}.
	 */
	R visitFilter(Filter expr, A arg);

	/**
	 * Visit case for {@link GenericMethod}.
	 */
	R visitGenericMethod(GenericMethod expr, A arg);

	/**
	 * Visit case for {@link Access}.
	 */
	R visitAccess(Access expr, A arg);

	/**
	 * Visit case for {@link At}.
	 */
	R visitAt(At expr, A arg);

	/**
	 * Visit case for {@link Update}.
	 */
	R visitUpdate(Update expr, A arg);

	/**
	 * Visit case for {@link Block}.
	 */
	R visitBlock(Block expr, A arg);

	/**
	 * Visit case for {@link Referers}.
	 */
	R visitReferers(Referers expr, A arg);

	/**
	 * Visit case for {@link AssociationNavigation}.
	 */
	R visitAssociationNavigation(AssociationNavigation expr, A arg);

	/**
	 * Visit case for {@link Literal}.
	 */
	R visitLiteral(Literal expr, A arg);

	/**
	 * Visit case for {@link IsEmpty}.
	 */
	R visitIsEmpty(IsEmpty expr, A arg);

	/**
	 * Visit case for {@link Not}.
	 */
	R visitNot(Not expr, A arg);

	/**
	 * Visit case for {@link And}.
	 */
	R visitAnd(And expr, A arg);

	/**
	 * Visit case for {@link Or}.
	 */
	R visitOr(Or expr, A arg);

	/**
	 * Visit case for {@link IfElse}.
	 */
	R visitIfElse(IfElse expr, A arg);

	/**
	 * Visit case for {@link IsEqual}.
	 */
	R visitEquals(IsEqual expr, A arg);

	/**
	 * Visit case for {@link CompareOp}.
	 */
	R visitCompareOp(CompareOp expr, A arg);

	/**
	 * Visit case for {@link Compare}.
	 */
	R visitCompare(Compare expr, A arg);

	/**
	 * Visit case for {@link Round}.
	 */
	R visitRound(Round expr, A arg);

	/**
	 * Visit case for {@link GetDay}.
	 */
	R visitGetDay(GetDay expr, A arg);

	/**
	 * Visit case for {@link IsStringEqual}.
	 */
	R visitStringEquals(IsStringEqual expr, A arg);

	/**
	 * Visit case for {@link IsStringGreater}.
	 */
	R visitStringGreater(IsStringGreater expr, A arg);

	/**
	 * Visit case for {@link StringContains}.
	 */
	R visitStringContains(StringContains expr, A arg);

	/**
	 * Visit case for {@link StringStartsWith}.
	 */
	R visitStringStartsWith(StringStartsWith expr, A arg);

	/**
	 * Visit case for {@link StringEndsWith}.
	 */
	R visitStringEndsWith(StringEndsWith expr, A arg);

	/**
	 * Visit case for {@link ContainsElement}.
	 */
	R visitContainsElement(ContainsElement expr, A arg);

	/**
	 * Visit case for {@link ContainsAll}.
	 */
	R visitContainsAll(ContainsAll expr, A arg);

	/**
	 * Visit case for {@link ContainsSome}.
	 */
	R visitContainsSome(ContainsSome expr, A arg);

	/**
	 * Visit case for {@link TagMacro}.
	 */
	R visitTag(TagMacro expr, A arg);

	/**
	 * Visit case for {@link HtmlMacro}.
	 */
	R visitHtml(HtmlMacro expr, A arg);

	/**
	 * Visit case for {@link AttributeMacro}.
	 */
	R visitAttr(AttributeMacro expr, A arg);

	/**
	 * Visit case for {@link Length}.
	 */
	R visitLength(Length expr, A arg);

	/**
	 * Visit case for {@link Size}.
	 */
	R visitSize(Size expr, A arg);

	/**
	 * Visit case for {@link ListExpr}.
	 */
	R visitList(ListExpr expr, A arg);

	/**
	 * Visit case for {@link Desc}.
	 */
	R visitDesc(Desc expr, A arg);

	/**
	 * Visit case for {@link Sort}.
	 */
	R visitSort(Sort expr, A arg);

	/**
	 * Visit case for {@link ArithmeticExpr}.
	 */
	R visitArithmetic(ArithmeticExpr expr, A arg);

}