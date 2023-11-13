/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import java.util.List;

import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.ArithmeticExpr;
import com.top_logic.model.search.expr.AssociationNavigation;
import com.top_logic.model.search.expr.At;
import com.top_logic.model.search.expr.BinaryOperation;
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
import com.top_logic.model.search.expr.UnaryOperation;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;
import com.top_logic.model.search.expr.visit.DescendingVisitor;

/**
 * {@link DescendingVisitor} rewriting parts of a {@link SearchExpression} model.
 * 
 * <p>
 * To actually perform a transformation, some visit method must be overridden.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Rewriter<A> extends DescendingVisitor<SearchExpression, A> {

	private static final SearchExpression[] SEARCH_EXPRESSIONS = {};

	private static final AttributeMacro[] ATTRIBUTE_MACROS = {};

	@Override
	protected SearchExpression composeUpdate(Update expr, A arg, SearchExpression selfResult,
			SearchExpression valueResult) {
		expr.setSelf(selfResult);
		expr.setValue(valueResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeBlock(Block expr, A arg, List<SearchExpression> results) {
		expr.setContents(toArray(results));
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeLambda(Lambda expr, A arg, SearchExpression bodyResult) {
		expr.setBody(bodyResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeCall(Call expr, A arg, SearchExpression functionResult,
			SearchExpression argumentResult) {
		expr.setFunction(functionResult);
		expr.setArgument(argumentResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeRecursion(Recursion expr, A arg, SearchExpression startResult,
			SearchExpression functionResult, SearchExpression minDepthResult, SearchExpression maxDepthResult) {
		expr.setStart(startResult);
		expr.setFunction(functionResult);
		expr.setMinDepth(minDepthResult);
		expr.setMaxDepth(maxDepthResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeUnion(Union expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeIntersection(Intersection expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeAll(All expr, A arg) {
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeTuple(TupleExpression expr, A arg, List<SearchExpression> results) {
		for (int cnt = expr.getCoords().length, n = 0; n < cnt; n++) {
			expr.getCoords()[n].setExpr(results.get(n));
		}
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeSource(KBQuery expr, A arg) {
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeInstanceOf(InstanceOf expr, A arg, SearchExpression valueResult) {
		expr.setValue(valueResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeFilter(Filter expr, A arg, SearchExpression baseResult,
			SearchExpression functionResult) {
		expr.setBase(baseResult);
		expr.setFunction(functionResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeGenericMethod(GenericMethod expr, A arg, List<SearchExpression> argumentsResult) {
		expr.setArguments(toArray(argumentsResult));
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeAccess(Access expr, A arg, SearchExpression selfResult) {
		expr.setSelf(selfResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeAt(At expr, A arg, SearchExpression self, SearchExpression index) {
		expr.setSelf(self);
		expr.setIndex(index);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeReferers(Referers expr, A arg, SearchExpression targetResult) {
		expr.setTarget(targetResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeAssociationNavigation(AssociationNavigation expr, A arg,
			SearchExpression sourceResult) {
		expr.setSource(sourceResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeLiteral(Literal expr, A arg) {
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeIsEmpty(IsEmpty expr, A arg, SearchExpression argumentResult) {
		return composeUnaryOperation(expr, arg, argumentResult);
	}

	@Override
	protected SearchExpression composeNot(Not expr, A arg, SearchExpression argumentResult) {
		return composeUnaryOperation(expr, arg, argumentResult);
	}

	@Override
	protected SearchExpression composeAnd(And expr, A arg, SearchExpression leftResult, SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeOr(Or expr, A arg, SearchExpression leftResult, SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeIfElse(IfElse expr, A arg, SearchExpression conditionResult,
			SearchExpression ifResult,
			SearchExpression elseResult) {
		expr.setCondition(conditionResult);
		expr.setIfClause(ifResult);
		expr.setElseClause(elseResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeEquals(IsEqual expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeCompareOp(CompareOp expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeCompare(Compare expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeRound(Round expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeGetDay(GetDay expr, A arg, SearchExpression argumentResult) {
		expr.setArgument(argumentResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeStringEquals(IsStringEqual expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		expr.setCaseSensitive(expr.isCaseSensitive());
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeStringGreater(IsStringGreater expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		expr.setCaseSensitive(expr.isCaseSensitive());
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeStringContains(StringContains expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		expr.setCaseSensitive(expr.isCaseSensitive());
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeStringStartsWith(StringStartsWith expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		expr.setCaseSensitive(expr.isCaseSensitive());
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeStringEndsWith(StringEndsWith expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		expr.setCaseSensitive(expr.isCaseSensitive());
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeContainsElement(ContainsElement expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeContainsAll(ContainsAll expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeContainsSome(ContainsSome expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return composeBinaryOperation(expr, arg, leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeVar(Var expr, A arg) {
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeForeach(Foreach expr, A arg, SearchExpression baseResult,
			SearchExpression functionResult) {
		expr.setBase(baseResult);
		expr.setFunction(functionResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeFlatten(Flatten expr, A arg, SearchExpression argumentResult) {
		return composeUnaryOperation(expr, arg, argumentResult);
	}

	@Override
	protected SearchExpression composeSingleton(Singleton expr, A arg, SearchExpression argumentResult) {
		return composeUnaryOperation(expr, arg, argumentResult);
	}

	@Override
	protected SearchExpression composeSingleElement(SingleElement expr, A arg, SearchExpression argumentResult) {
		return composeUnaryOperation(expr, arg, argumentResult);
	}

	@Override
	protected SearchExpression composeHtml(HtmlMacro expr, A arg, List<SearchExpression> contentsResult) {
		expr.setContents(toArray(contentsResult));
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeTag(TagMacro expr, A arg, List<SearchExpression> attributesResult) {
		expr.setAttributes(toAttributesArray(attributesResult));
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeAttr(AttributeMacro expr, A arg, SearchExpression valueResult) {
		expr.setValue(valueResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeDesc(Desc expr, A arg, SearchExpression keyResult) {
		expr.setArgument(keyResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeLength(Length expr, A arg, SearchExpression stringResult) {
		expr.setArgument(stringResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeSize(Size expr, A arg, SearchExpression listResult) {
		expr.setArgument(listResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeList(ListExpr expr, A arg, List<SearchExpression> elementsResult) {
		expr.setElements(toArray(elementsResult));
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeSort(Sort expr, A arg, SearchExpression listResult,
			SearchExpression keyFunResult) {
		expr.setList(listResult);
		expr.setComparator(keyFunResult);
		return compose(expr, arg);
	}

	private SearchExpression composeBinaryOperation(BinaryOperation expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		return compose(expr, arg);
	}

	private SearchExpression composeUnaryOperation(UnaryOperation expr, A arg, SearchExpression argumentResult) {
		expr.setArgument(argumentResult);
		return compose(expr, arg);
	}

	@Override
	protected SearchExpression composeArithmetic(ArithmeticExpr expr, A arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		expr.setLeft(leftResult);
		expr.setRight(rightResult);
		return compose(expr, arg);
	}

	/**
	 * Hook called after has completed transformation.
	 * 
	 * @param expr
	 *        The transformed expression.
	 * @param arg
	 *        The argument to the visit.
	 * @return The visit result for the transformation.
	 */
	protected SearchExpression compose(SearchExpression expr, A arg) {
		return expr;
	}

	private static SearchExpression[] toArray(List<SearchExpression> argumentsResult) {
		return argumentsResult.toArray(SEARCH_EXPRESSIONS);
	}

	private static AttributeMacro[] toAttributesArray(List<SearchExpression> argumentsResult) {
		return argumentsResult.toArray(ATTRIBUTE_MACROS);
	}

}
