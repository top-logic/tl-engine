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
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;

/**
 * {@link DescendingVisitor} that has default behavior for all concrete model types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDescendingVisitor<R, A> extends DescendingVisitor<R, A> {

	@Override
	protected R composeUpdate(Update expr, A arg, R selfResult, R valueResult) {
		return compose(expr, arg, combine(selfResult, valueResult));
	}

	@Override
	protected R composeBlock(Block expr, A arg, List<R> results) {
		return compose(expr, arg, combine(results));
	}

	@Override
	protected R composeLambda(Lambda expr, A arg, R bodyResult) {
		return compose(expr, arg, bodyResult);
	}

	@Override
	protected R composeCall(Call expr, A arg, R functionResult, R argumentResult) {
		return compose(expr, arg, combine(functionResult, argumentResult));
	}

	@Override
	protected R composeRecursion(Recursion expr, A arg, R startResult, R functionResult, R minDepthResult,
			R maxDepthResult) {
		return compose(expr, arg, combine(startResult, functionResult, minDepthResult, maxDepthResult));
	}

	@Override
	protected R composeUnion(Union expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeIntersection(Intersection expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeAll(All expr, A arg) {
		return compose(expr, arg, none());
	}

	@Override
	protected R composeTuple(TupleExpression expr, A arg, List<R> results) {
		return compose(expr, arg, combine(results));
	}

	@Override
	protected R composeSource(KBQuery expr, A arg) {
		return compose(expr, arg, none());
	}

	@Override
	protected R composeInstanceOf(InstanceOf expr, A arg, R valueResult) {
		return compose(expr, arg, valueResult);
	}

	@Override
	protected R composeFilter(Filter expr, A arg, R baseResult, R functionResult) {
		return compose(expr, arg, combine(baseResult, functionResult));
	}

	@Override
	protected R composeGenericMethod(GenericMethod expr, A arg, List<R> argumentsResult) {
		return compose(expr, arg, combine(argumentsResult));
	}

	@Override
	protected R composeAccess(Access expr, A arg, R selfResult) {
		return compose(expr, arg, selfResult);
	}

	@Override
	protected R composeAt(At expr, A arg, R self, R index) {
		return compose(expr, arg, combine(self, index));
	}

	@Override
	protected R composeReferers(Referers expr, A arg, R targetResult) {
		return compose(expr, arg, targetResult);
	}

	@Override
	protected R composeAssociationNavigation(AssociationNavigation expr, A arg, R sourceResult) {
		return compose(expr, arg, sourceResult);
	}

	@Override
	protected R composeLiteral(Literal expr, A arg) {
		return compose(expr, arg, none());
	}

	@Override
	protected R composeIsEmpty(IsEmpty expr, A arg, R argumentResult) {
		return compose(expr, arg, argumentResult);
	}

	@Override
	protected R composeAnd(And expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeOr(Or expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeNot(Not expr, A arg, R argumentResult) {
		return compose(expr, arg, argumentResult);
	}

	@Override
	protected R composeIfElse(IfElse expr, A arg, R conditionResult, R ifResult, R elseResult) {
		return compose(expr, arg, combine(conditionResult, ifResult, elseResult));
	}

	@Override
	protected R composeEquals(IsEqual expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeCompareOp(CompareOp expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeCompare(Compare expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeRound(Round expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeGetDay(GetDay expr, A arg, R argumentResult) {
		return compose(expr, arg, argumentResult);
	}

	@Override
	protected R composeStringEquals(IsStringEqual expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeStringGreater(IsStringGreater expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeStringContains(StringContains expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeStringStartsWith(StringStartsWith expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeStringEndsWith(StringEndsWith expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeContainsElement(ContainsElement expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeContainsAll(ContainsAll expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeContainsSome(ContainsSome expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	@Override
	protected R composeVar(Var expr, A arg) {
		return compose(expr, arg, none());
	}

	@Override
	protected R composeForeach(Foreach expr, A arg, R baseResult, R functionResult) {
		return compose(expr, arg, combine(baseResult, functionResult));
	}

	@Override
	protected R composeFlatten(Flatten expr, A arg, R argumentResult) {
		return compose(expr, arg, argumentResult);
	}

	@Override
	protected R composeSingleton(Singleton expr, A arg, R argumentResult) {
		return compose(expr, arg, argumentResult);
	}

	@Override
	protected R composeSingleElement(SingleElement expr, A arg, R argumentResult) {
		return compose(expr, arg, argumentResult);
	}

	@Override
	protected R composeHtml(HtmlMacro expr, A arg, List<R> contentsResult) {
		return compose(expr, arg, combine(contentsResult));
	}

	@Override
	protected R composeTag(TagMacro expr, A arg, List<R> attributesResult) {
		return compose(expr, arg, combine(attributesResult));
	}

	@Override
	protected R composeAttr(AttributeMacro expr, A arg, R valueResult) {
		return compose(expr, arg, valueResult);
	}

	@Override
	protected R composeDesc(Desc expr, A arg, R keyResult) {
		return compose(expr, arg, keyResult);
	}

	@Override
	protected R composeLength(Length expr, A arg, R stringResult) {
		return compose(expr, arg, stringResult);
	}

	@Override
	protected R composeSize(Size expr, A arg, R listResult) {
		return compose(expr, arg, listResult);
	}

	@Override
	protected R composeSort(Sort expr, A arg, R listResult, R keyFunResult) {
		return compose(expr, arg, combine(listResult, keyFunResult));
	}

	@Override
	protected R composeList(ListExpr expr, A arg, List<R> elementsResult) {
		return compose(expr, arg, combine(elementsResult));
	}

	@Override
	protected R composeArithmetic(ArithmeticExpr expr, A arg, R leftResult, R rightResult) {
		return compose(expr, arg, combine(leftResult, rightResult));
	}

	/**
	 * Combines two results to a single result.
	 * 
	 * @param result1
	 *        The result from the visit of one model part.
	 * @param result2
	 *        The result from the visit of another model part.
	 * @return The combined result to pass to the default case
	 *         {@link #compose(SearchExpression, Object, Object)}.
	 */
	protected R combine(R result1, R result2) {
		return result1;
	}

	/**
	 * Combines three results to a single result.
	 * 
	 * @param result1
	 *        The result from the visit of one model part.
	 * @param result2
	 *        The result from the visit of another model part.
	 * @param result3
	 *        The result from the visit of another model part.
	 * @return The combined result to pass to the default case
	 *         {@link #compose(SearchExpression, Object, Object)}.
	 */
	protected R combine(R result1, R result2, R result3) {
		return combine(combine(result1, result2), result3);
	}

	/**
	 * Combines three results to a single result.
	 * 
	 * @param result1
	 *        The result from the visit of one model part.
	 * @param result2
	 *        The result from the visit of another model part.
	 * @param result3
	 *        The result from the visit of another model part.
	 * @param result4
	 *        The result from the visit of another model part.
	 * @return The combined result to pass to the default case
	 *         {@link #compose(SearchExpression, Object, Object)}.
	 */
	protected R combine(R result1, R result2, R result3, R result4) {
		return combine(combine(result1, result2, result3), result4);
	}

	/**
	 * Combines the variable number of results to a single result.
	 * 
	 * @param results
	 *        The results from the visit of one model part.
	 * @return The combined result to pass to the default case
	 *         {@link #compose(SearchExpression, Object, Object)}.
	 */
	protected R combine(List<R> results) {
		if (results.isEmpty()) {
			return none();
		}
		R result = results.get(0);
		for (int n = 1, size = results.size(); n < size; n++) {
			result = combine(result, results.get(n));
		}
		return result;
	}

	/**
	 * The default visit case for all model elements that do not define a specialized handling.
	 * 
	 * @param expr
	 *        The visited expression.
	 * @param arg
	 *        The argument to the visit.
	 * @param result
	 *        The {@link #combine(Object, Object) combined} result of all contents.
	 * @return The final result of the visit.
	 */
	protected R compose(SearchExpression expr, A arg, R result) {
		return result;
	}

	/**
	 * The default result value to use for all model elements that have no contents.
	 * 
	 * @return <code>null</code> by default.
	 */
	protected R none() {
		return null;
	}

}
