/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.visit;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

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
import com.top_logic.model.search.expr.SearchExpressionFactory;
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
 * Copy {@link Visitor} for {@link SearchExpression} models.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Copy extends DescendingVisitor<SearchExpression, Void> {

	/**
	 * Singleton {@link Copy} instance.
	 */
	public static final Copy INSTANCE = new Copy();

	private Copy() {
		// Singleton constructor.
	}

	private static final SearchExpression[] SEARCH_EXPRESSIONS = new SearchExpression[0];

	@Override
	protected SearchExpression composeAccess(Access expr, Void arg, SearchExpression selfResult) {
		return access(selfResult, expr.getPart());
	}

	@Override
	protected SearchExpression composeAt(At expr, Void arg, SearchExpression self, SearchExpression index) {
		return SearchExpressionFactory.at(self, index);
	}

	@Override
	protected SearchExpression composeReferers(Referers expr, Void arg, SearchExpression targetResult) {
		return referers(targetResult, expr.getReference());
	}

	@Override
	protected SearchExpression composeAssociationNavigation(AssociationNavigation expr, Void arg, SearchExpression sourceResult) {
		return associationNavigation(sourceResult, expr.getSourceEnd(), expr.getDestinationEnd());
	}

	@Override
	protected SearchExpression composeAll(All expr, Void arg) {
		return all(expr.getInstanceType());
	}

	@Override
	protected SearchExpression composeTuple(TupleExpression expr, Void arg, List<SearchExpression> results) {
		return tuple(coords(expr, results));
	}

	private Coord[] coords(TupleExpression expr, List<SearchExpression> results) {
		Coord[] result = new Coord[expr.getCoords().length];
		for (int n = 0, cnt = expr.getCoords().length; n < cnt; n++) {
			Coord origCoord = expr.getCoords()[n];
			result[n] = composeCoord(origCoord, results.get(n));
		}
		return result;
	}

	private Coord composeCoord(Coord origCoord, SearchExpression result) {
		return coord(origCoord.isOptional(), origCoord.getName(), result);
	}

	@Override
	protected SearchExpression composeSource(KBQuery expr, Void arg) {
		return query(expr.getClassType(), expr.getQuery());
	}

	@Override
	protected SearchExpression composeInstanceOf(InstanceOf expr, Void arg, SearchExpression valueResult) {
		return instanceOf(valueResult, expr.getCheckType());
	}

	@Override
	protected SearchExpression composeContainsAll(ContainsAll expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return containsAll(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeContainsElement(ContainsElement expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return containsElement(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeContainsSome(ContainsSome expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return containsSome(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeEquals(IsEqual expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return isEqual(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeCompare(Compare expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return compare(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeCompareOp(CompareOp expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return compareOp(expr.getKind(), leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeRound(Round expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return round(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeGetDay(GetDay expr, Void arg, SearchExpression argumentResult) {
		return day(argumentResult);
	}

	@Override
	protected SearchExpression composeStringEquals(IsStringEqual expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return isStringEqual(leftResult, rightResult, expr.isCaseSensitive());
	}

	@Override
	protected SearchExpression composeStringGreater(IsStringGreater expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return isStringGreater(leftResult, rightResult, expr.isCaseSensitive());
	}
	
	@Override
	protected SearchExpression composeStringContains(StringContains expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return stringContains(leftResult, rightResult, expr.isCaseSensitive());
	}
	
	@Override
	protected SearchExpression composeStringStartsWith(StringStartsWith expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return stringStartsWith(leftResult, rightResult, expr.isCaseSensitive());
	}
	
	@Override
	protected SearchExpression composeStringEndsWith(StringEndsWith expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return stringEndsWith(leftResult, rightResult, expr.isCaseSensitive());
	}

	@Override
	protected SearchExpression composeUpdate(Update expr, Void arg, SearchExpression selfResult,
			SearchExpression valueResult) {
		return update(selfResult, expr.getPart(), valueResult);
	}
	
	@Override
	protected SearchExpression composeBlock(Block expr, Void arg, List<SearchExpression> results) {
		return block(toArray(results));
	}

	@Override
	protected SearchExpression composeLambda(Lambda expr, Void arg, SearchExpression bodyResult) {
		return lambda(expr.getName(), bodyResult);
	}

	@Override
	protected SearchExpression composeCall(Call expr, Void arg, SearchExpression functionResult, SearchExpression argumentResult) {
		return call(functionResult, argumentResult);
	}

	@Override
	protected SearchExpression composeRecursion(Recursion expr, Void arg, SearchExpression startResult,
			SearchExpression functionResult, SearchExpression minDepthResult, SearchExpression maxDepthResult) {
		return recursion(startResult, functionResult, minDepthResult, maxDepthResult);
	}

	@Override
	protected SearchExpression composeFilter(Filter expr, Void arg, SearchExpression baseResult, SearchExpression functionResult) {
		return filter(baseResult, functionResult);
	}

	@Override
	protected SearchExpression composeGenericMethod(GenericMethod expr, Void arg, SearchExpression baseResult,
			List<SearchExpression> argumentsResult) {
		return expr.copy(baseResult, toArray(argumentsResult));
	}

	@Override
	protected SearchExpression composeLiteral(Literal expr, Void arg) {
		return literal(expr.getValue());
	}

	@Override
	protected SearchExpression composeIsEmpty(IsEmpty expr, Void arg, SearchExpression argumentResult) {
		return isEmpty(argumentResult);
	}

	@Override
	protected SearchExpression composeNot(Not expr, Void arg, SearchExpression argumentResult) {
		return not(argumentResult);
	}

	@Override
	protected SearchExpression composeAnd(And expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return and(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeOr(Or expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return or(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeIfElse(IfElse expr, Void arg, SearchExpression conditionResult, SearchExpression ifResult,
			SearchExpression elseResult) {
		return ifElse(conditionResult, ifResult, elseResult);
	}

	@Override
	protected SearchExpression composeVar(Var expr, Void arg) {
		return var(expr.getName());
	}

	@Override
	protected SearchExpression composeUnion(Union expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return union(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeIntersection(Intersection expr, Void arg, SearchExpression leftResult, SearchExpression rightResult) {
		return union(leftResult, rightResult);
	}

	@Override
	protected SearchExpression composeForeach(Foreach expr, Void arg, SearchExpression baseResult, SearchExpression functionResult) {
		return foreach(baseResult, functionResult);
	}

	@Override
	protected SearchExpression composeFlatten(Flatten expr, Void arg, SearchExpression argumentResult) {
		return flatten(argumentResult);
	}

	@Override
	protected SearchExpression composeSingleton(Singleton expr, Void arg, SearchExpression argumentResult) {
		return singleton(argumentResult);
	}

	@Override
	protected SearchExpression composeSingleElement(SingleElement expr, Void arg, SearchExpression argumentResult) {
		return singleElement(argumentResult);
	}

	@Override
	protected SearchExpression composeHtml(HtmlMacro expr, Void arg, List<SearchExpression> contentsResult) {
		return html(toArray(contentsResult));
	}

	@Override
	protected SearchExpression composeTag(TagMacro expr, Void arg, List<SearchExpression> attributesResult) {
		return tag(expr.getTag(), expr.isEmpty(), toArray(attributesResult));
	}

	@Override
	protected SearchExpression composeAttr(AttributeMacro expr, Void arg, SearchExpression valueResult) {
		return attr(expr.getName(), valueResult);
	}

	@Override
	protected SearchExpression composeDesc(Desc expr, Void arg, SearchExpression keyResult) {
		return desc(keyResult);
	}

	@Override
	protected SearchExpression composeLength(Length expr, Void arg, SearchExpression stringResult) {
		return length(stringResult);
	}

	@Override
	protected SearchExpression composeSize(Size expr, Void arg, SearchExpression listResult) {
		return size(listResult);
	}

	@Override
	protected SearchExpression composeList(ListExpr expr, Void arg, List<SearchExpression> elementsResult) {
		return list(toArray(elementsResult));
	}

	@Override
	protected SearchExpression composeSort(Sort expr, Void arg, SearchExpression listResult,
			SearchExpression keyFunResult) {
		return sort(listResult, keyFunResult);
	}

	@Override
	protected SearchExpression composeArithmetic(ArithmeticExpr expr, Void arg, SearchExpression leftResult,
			SearchExpression rightResult) {
		return arithmetic(expr.getOp(), leftResult, rightResult);
	}

	/**
	 * Utility method for applying the {@link Copy} visitor to the given expression.
	 */
	public static SearchExpression copy(SearchExpression expr) {
		if (expr == null) {
			return null;
		}
		return expr.visit(Copy.INSTANCE, null);
	}

	private SearchExpression[] toArray(List<SearchExpression> results) {
		return results.toArray(SEARCH_EXPRESSIONS);
	}

}
