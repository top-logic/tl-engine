/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.visit;

import java.util.List;

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
import com.top_logic.model.search.expr.TupleExpression.Coord;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;

/**
 * {@link Visitor} that descends a {@link SearchExpression} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DescendingVisitor<R, A> extends AbstractDescendingVisitor<R, A> {

	@Override
	public R visitUpdate(Update expr, A arg) {
		return composeUpdate(expr, arg, descendPart(expr, arg, expr.getSelf()),
			descendPart(expr, arg, expr.getValue()));
	}

	/**
	 * Combines the visit results of {@link Update} contents.
	 */
	protected abstract R composeUpdate(Update expr, A arg, R selfResult, R valueResult);

	@Override
	public R visitCall(Call expr, A arg) {
		return composeCall(expr, arg, descendPart(expr, arg, expr.getFunction()),
			descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Call} contents.
	 */
	protected abstract R composeCall(Call expr, A arg, R functionResult, R argumentResult);

	@Override
	public R visitRecursion(Recursion expr, A arg) {
		return composeRecursion(expr, arg, descendPart(expr, arg, expr.getStart()),
			descendPart(expr, arg, expr.getFunction()), descendPart(expr, arg, expr.getMinDepth()),
			descendPart(expr, arg, expr.getMaxDepth()));
	}

	/**
	 * Combines the visit results of {@link Recursion} contents.
	 */
	protected abstract R composeRecursion(Recursion expr, A arg, R startResult, R functionResult, R minDepthResult,
			R maxDepthResult);

	@Override
	public R visitUnion(Union expr, A arg) {
		return composeUnion(expr, arg, descendPart(expr, arg, expr.getLeft()), descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link Union} contents.
	 */
	protected abstract R composeUnion(Union expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitIntersection(Intersection expr, A arg) {
		return composeIntersection(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link Intersection} contents.
	 */
	protected abstract R composeIntersection(Intersection expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitFilter(Filter expr, A arg) {
		return composeFilter(expr, arg, descendPart(expr, arg, expr.getBase()),
			descendPart(expr, arg, expr.getFunction()));
	}

	/**
	 * Combines the visit results of {@link Filter} contents.
	 */
	protected abstract R composeFilter(Filter expr, A arg, R baseResult, R functionResult);

	@Override
	public R visitGenericMethod(GenericMethod expr, A arg) {
		return composeGenericMethod(expr, arg, descendParts(expr, arg, expr.getArguments()));
	}

	/**
	 * Combines the visit results of {@link GenericMethod} contents.
	 */
	protected abstract R composeGenericMethod(GenericMethod expr, A arg, List<R> argumentsResult);

	@Override
	public R visitAnd(And expr, A arg) {
		return composeAnd(expr, arg, descendPart(expr, arg, expr.getLeft()), descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link And} contents.
	 */
	protected abstract R composeAnd(And expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitOr(Or expr, A arg) {
		return composeOr(expr, arg, descendPart(expr, arg, expr.getLeft()), descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link Or} contents.
	 */
	protected abstract R composeOr(Or expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitIfElse(IfElse expr, A arg) {
		return composeIfElse(expr, arg, descendPart(expr, arg, expr.getCondition()),
			descendPart(expr, arg, expr.getIfClause()), descendPart(expr, arg, expr.getElseClause()));
	}

	/**
	 * Combines the visit results of {@link IfElse} contents.
	 */
	protected abstract R composeIfElse(IfElse expr, A arg, R conditionResult, R ifResult, R elseResult);

	@Override
	public R visitEquals(IsEqual expr, A arg) {
		return composeEquals(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link IsEqual} contents.
	 */
	protected abstract R composeEquals(IsEqual expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitCompare(Compare expr, A arg) {
		return composeCompare(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link Compare} contents.
	 */
	protected abstract R composeCompare(Compare expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitCompareOp(CompareOp expr, A arg) {
		return composeCompareOp(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link CompareOp} contents.
	 */
	protected abstract R composeCompareOp(CompareOp expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitRound(Round expr, A arg) {
		return composeRound(expr, arg, descendPart(expr, arg, expr.getLeft()), descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link Round} contents.
	 */
	protected abstract R composeRound(Round expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitGetDay(GetDay expr, A arg) {
		return composeGetDay(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link GetDay} contents.
	 */
	protected abstract R composeGetDay(GetDay expr, A arg, R argumentResult);

	@Override
	public R visitStringEquals(IsStringEqual expr, A arg) {
		return composeStringEquals(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link IsStringEqual} contents.
	 */
	protected abstract R composeStringEquals(IsStringEqual expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitStringGreater(IsStringGreater expr, A arg) {
		return composeStringGreater(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link IsStringGreater} contents.
	 */
	protected abstract R composeStringGreater(IsStringGreater expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitStringContains(StringContains expr, A arg) {
		return composeStringContains(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link StringContains} contents.
	 */
	protected abstract R composeStringContains(StringContains expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitStringStartsWith(StringStartsWith expr, A arg) {
		return composeStringStartsWith(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link StringStartsWith} contents.
	 */
	protected abstract R composeStringStartsWith(StringStartsWith expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitStringEndsWith(StringEndsWith expr, A arg) {
		return composeStringEndsWith(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link StringEndsWith} contents.
	 */
	protected abstract R composeStringEndsWith(StringEndsWith expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitContainsElement(ContainsElement expr, A arg) {
		return composeContainsElement(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link ContainsElement} contents.
	 */
	protected abstract R composeContainsElement(ContainsElement expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitContainsAll(ContainsAll expr, A arg) {
		return composeContainsAll(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link ContainsAll} contents.
	 */
	protected abstract R composeContainsAll(ContainsAll expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitContainsSome(ContainsSome expr, A arg) {
		return composeContainsSome(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link ContainsSome} contents.
	 */
	protected abstract R composeContainsSome(ContainsSome expr, A arg, R leftResult, R rightResult);

	@Override
	public R visitForeach(Foreach expr, A arg) {
		return composeForeach(expr, arg, descendPart(expr, arg, expr.getBase()),
			descendPart(expr, arg, expr.getFunction()));
	}

	/**
	 * Combines the visit results of {@link Foreach} contents.
	 */
	protected abstract R composeForeach(Foreach expr, A arg, R baseResult, R functionResult);

	@Override
	public R visitBlock(Block expr, A arg) {
		return composeBlock(expr, arg, descendParts(expr, arg, expr.getContents()));
	}

	/**
	 * Combines the visit results of {@link Block} contents.
	 */
	protected abstract R composeBlock(Block expr, A arg, List<R> results);

	@Override
	public R visitLambda(Lambda expr, A arg) {
		return composeLambda(expr, arg, descendPart(expr, arg, expr.getBody()));
	}

	/**
	 * Combines the visit results of {@link Lambda} contents.
	 */
	protected abstract R composeLambda(Lambda expr, A arg, R bodyResult);

	@Override
	public R visitAll(All expr, A arg) {
		return composeAll(expr, arg);
	}

	/**
	 * Combines the visit results of {@link All} contents.
	 */
	protected abstract R composeAll(All expr, A arg);

	@Override
	public R visitTuple(TupleExpression expr, A arg) {
		return composeTuple(expr, arg, descendParts(expr, arg, extract(expr.getCoords())));
	}

	private SearchExpression[] extract(Coord[] coords) {
		SearchExpression[] result = new SearchExpression[coords.length];
		for (int n = 0, cnt = coords.length; n < cnt; n++) {
			result[n] = coords[n].getExpr();
		}
		return result;
	}

	/**
	 * Combines the visit results of {@link TupleExpression} contents.
	 */
	protected abstract R composeTuple(TupleExpression expr, A arg, List<R> results);

	@Override
	public R visitKBQuery(KBQuery expr, A arg) {
		return composeSource(expr, arg);
	}

	/**
	 * Combines the visit results of {@link KBQuery} contents.
	 */
	protected abstract R composeSource(KBQuery expr, A arg);

	@Override
	public R visitInstanceOf(InstanceOf expr, A arg) {
		return composeInstanceOf(expr, arg, descendPart(expr, arg, expr.getValue()));
	}

	/**
	 * Combines the visit results of {@link InstanceOf} contents.
	 */
	protected abstract R composeInstanceOf(InstanceOf expr, A arg, R valueResult);

	@Override
	public R visitAccess(Access expr, A arg) {
		return composeAccess(expr, arg, descendPart(expr, arg, expr.getSelf()));
	}

	/**
	 * Combines the visit results of {@link Access} contents.
	 */
	protected abstract R composeAccess(Access expr, A arg, R selfResult);

	@Override
	public R visitAt(At expr, A arg) {
		return composeAt(expr, arg, descendPart(expr, arg, expr.getSelf()), descendPart(expr, arg, expr.getIndex()));
	}

	/**
	 * Combines the visit results of {@link Access} contents.
	 */
	protected abstract R composeAt(At expr, A arg, R self, R index);

	@Override
	public R visitReferers(Referers expr, A arg) {
		return composeReferers(expr, arg, descendPart(expr, arg, expr.getTarget()));
	}

	/**
	 * Combines the visit results of {@link Referers} contents.
	 */
	protected abstract R composeReferers(Referers expr, A arg, R targetResult);

	@Override
	public R visitAssociationNavigation(AssociationNavigation expr, A arg) {
		return composeAssociationNavigation(expr, arg, descendPart(expr, arg, expr.getSource()));
	}

	/**
	 * Combines the visit results of {@link AssociationNavigation} contents.
	 */
	protected abstract R composeAssociationNavigation(AssociationNavigation expr, A arg, R sourceResult);

	@Override
	public R visitLiteral(Literal expr, A arg) {
		return composeLiteral(expr, arg);
	}

	/**
	 * Combines the visit results of {@link Literal} contents.
	 */
	protected abstract R composeLiteral(Literal expr, A arg);

	@Override
	public R visitIsEmpty(IsEmpty expr, A arg) {
		return composeIsEmpty(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link IsEmpty} contents.
	 */
	protected abstract R composeIsEmpty(IsEmpty expr, A arg, R argumentResult);

	@Override
	public R visitNot(Not expr, A arg) {
		return composeNot(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Not} contents.
	 */
	protected abstract R composeNot(Not expr, A arg, R argumentResult);

	@Override
	public R visitVar(Var expr, A arg) {
		return composeVar(expr, arg);
	}

	/**
	 * Combines the visit results of {@link Var} contents.
	 */
	protected abstract R composeVar(Var expr, A arg);

	@Override
	public R visitFlatten(Flatten expr, A arg) {
		return composeFlatten(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Flatten} contents.
	 */
	protected abstract R composeFlatten(Flatten expr, A arg, R argumentResult);

	@Override
	public R visitSingleton(Singleton expr, A arg) {
		return composeSingleton(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Singleton} contents.
	 */
	protected abstract R composeSingleton(Singleton expr, A arg, R argumentResult);

	@Override
	public R visitSingleElement(SingleElement expr, A arg) {
		return composeSingleElement(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link SingleElement} contents.
	 */
	protected abstract R composeSingleElement(SingleElement expr, A arg, R argumentResult);

	@Override
	public R visitTag(TagMacro expr, A arg) {
		return composeTag(expr, arg, descendParts(expr, arg, expr.getAttributes()));
	}

	/**
	 * Combines the visit results of {@link TagMacro} contents.
	 */
	protected abstract R composeTag(TagMacro expr, A arg, List<R> attributesResult);

	@Override
	public R visitHtml(HtmlMacro expr, A arg) {
		return composeHtml(expr, arg, descendParts(expr, arg, expr.getContents()));
	}

	/**
	 * Combines the visit results of {@link HtmlMacro} contents.
	 */
	protected abstract R composeHtml(HtmlMacro expr, A arg, List<R> contentsResult);

	@Override
	public R visitAttr(AttributeMacro expr, A arg) {
		return composeAttr(expr, arg, descendPart(expr, arg, expr.getValue()));
	}

	/**
	 * Combines the visit results of {@link AttributeMacro} contents.
	 */
	protected abstract R composeAttr(AttributeMacro expr, A arg, R valueResult);

	@Override
	public R visitSort(Sort expr, A arg) {
		return composeSort(expr, arg, descendPart(expr, arg, expr.getList()), descendPart(expr, arg, expr.getComparator()));
	}

	/**
	 * Combines the visit results of {@link Sort} contents.
	 */
	protected abstract R composeSort(Sort expr, A arg, R listResult, R keyFunResult);

	@Override
	public R visitDesc(Desc expr, A arg) {
		return composeDesc(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Desc} contents.
	 */
	protected abstract R composeDesc(Desc expr, A arg, R keyResult);

	@Override
	public R visitLength(Length expr, A arg) {
		return composeLength(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Length} contents.
	 */
	protected abstract R composeLength(Length expr, A arg, R stringResult);

	@Override
	public R visitSize(Size expr, A arg) {
		return composeSize(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	/**
	 * Combines the visit results of {@link Size} contents.
	 */
	protected abstract R composeSize(Size expr, A arg, R listResult);

	@Override
	public R visitList(ListExpr expr, A arg) {
		return composeList(expr, arg, descendParts(expr, arg, expr.getElements()));
	}

	/**
	 * Combines the visit results of {@link ListExpr} contents.
	 */
	protected abstract R composeList(ListExpr expr, A arg, List<R> elementsResult);

	@Override
	public R visitArithmetic(ArithmeticExpr expr, A arg) {
		return composeArithmetic(expr, arg, descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	/**
	 * Combines the visit results of {@link ArithmeticExpr} contents.
	 */
	protected abstract R composeArithmetic(ArithmeticExpr expr, A arg, R leftResult, R rightResult);

}
