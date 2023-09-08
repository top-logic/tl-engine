/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.visit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.top_logic.model.search.expr.StringOperation;
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
 * {@link Visitor} that descends a {@link SearchExpression} tree.
 * 
 * <p>
 * The visit results of the descendant nodes are available as generic {@link List}.
 * </p>
 * 
 * @see DescendingVisitor
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericDescendingVisitor<R, A> extends AbstractDescendingVisitor<R, A> {

	@Override
	public R visitAll(All expr, A arg) {
		return compose(expr, arg, wrap(expr.getInstanceType()));
	}

	@Override
	public R visitTuple(TupleExpression expr, A arg) {
		Coord[] coords = expr.getCoords();
		int length = coords.length;
		List<R> coordNodes = new ArrayList<>(length);
		for (int n = 0; n < length; n++) {
			Coord coord = coords[n];
			coordNodes.add(
				compose(Coord.class, arg,
					Arrays.asList(
						wrap(coord.isOptional()),
						wrap(coord.getName()),
						descendPart(expr, arg, coord.getExpr()))));
		}
		return compose(expr, arg, coordNodes);
	}

	/**
	 * Combines a structured value that is not a {@link SearchExpression}, e.g. a {@link Coord}.
	 */
	protected abstract R compose(Class<?> type, A arg, List<R> contents);

	@Override
	public R visitKBQuery(KBQuery expr, A arg) {
		return compose(expr, arg, wrap(expr.getClassType()), wrap(expr.getQuery()));
	}

	@Override
	public R visitInstanceOf(InstanceOf expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getValue()), wrap(expr.getCheckType()));
	}

	@Override
	public R visitAccess(Access expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getSelf()), wrap(expr.getPart()));
	}

	@Override
	public R visitAt(At expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getSelf()), descendPart(expr, arg, expr.getIndex()));
	}

	@Override
	public R visitUpdate(Update expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getSelf()), wrap(expr.getPart()),
			descendPart(expr, arg, expr.getValue()));
	}

	@Override
	public R visitBlock(Block expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getContents()));
	}

	@Override
	public R visitReferers(Referers expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getTarget()), wrap(expr.getReference()));
	}

	@Override
	public R visitAssociationNavigation(AssociationNavigation expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getSource()), wrap(expr.getSourceEnd()),
			wrap(expr.getDestinationEnd()));
	}

	@Override
	public R visitLiteral(Literal expr, A arg) {
		return compose(expr, arg, wrap(expr.getValue()));
	}

	@Override
	public R visitIsEmpty(IsEmpty expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitArithmetic(ArithmeticExpr expr, A arg) {
		return compose(expr, arg, wrap(expr.getOp()), descendPart(expr, arg, expr.getLeft()),
			descendPart(expr, arg, expr.getRight()));
	}

	@Override
	public R visitNot(Not expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitVar(Var expr, A arg) {
		return compose(expr, arg, wrap(expr.getName()));
	}

	@Override
	public R visitFlatten(Flatten expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitSingleton(Singleton expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitSingleElement(SingleElement expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitLambda(Lambda expr, A arg) {
		return compose(expr, arg, wrap(expr.getName()), descendPart(expr, arg, expr.getBody()));
	}

	@Override
	public R visitCall(Call expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getFunction(), expr.getArgument()));
	}

	@Override
	public R visitRecursion(Recursion expr, A arg) {
		return compose(expr, arg,
			descendParts(expr, arg, expr.getStart(), expr.getFunction(), expr.getMinDepth(), expr.getMaxDepth()));
	}

	@Override
	public R visitUnion(Union expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitIntersection(Intersection expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitFilter(Filter expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getBase(), expr.getFunction()));
	}

	@Override
	public R visitGenericMethod(GenericMethod expr, A arg) {
		SearchExpression[] arguments = expr.getArguments();
		SearchExpression self = expr.getSelf();
		List<R> partResults = newResult((self == null ? 0 : 1) + arguments.length);
		if (self != null) {
			partResults.add(descendPart(expr, arg, self));
		}
		List<R> parts = descendParts(partResults, expr, arg, arguments);
		return compose(expr, arg, parts);
	}

	@Override
	public R visitAnd(And expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitOr(Or expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitIfElse(IfElse expr, A arg) {
		return compose(expr, arg,
			descendParts(expr, arg, expr.getCondition(), expr.getIfClause(), expr.getElseClause()));
	}

	@Override
	public R visitEquals(IsEqual expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitCompare(Compare expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitCompareOp(CompareOp expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getLeft()), descendPart(expr, arg, expr.getRight()));
	}

	@Override
	public R visitRound(Round expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitGetDay(GetDay expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitStringEquals(IsStringEqual expr, A arg) {
		return visitStringOperation(expr, arg);
	}

	@Override
	public R visitStringGreater(IsStringGreater expr, A arg) {
		return visitStringOperation(expr, arg);
	}

	@Override
	public R visitStringContains(StringContains expr, A arg) {
		return visitStringOperation(expr, arg);
	}

	@Override
	public R visitStringStartsWith(StringStartsWith expr, A arg) {
		return visitStringOperation(expr, arg);
	}

	@Override
	public R visitStringEndsWith(StringEndsWith expr, A arg) {
		return visitStringOperation(expr, arg);
	}

	/**
	 * Common visit case for all {@link StringOperation}s.
	 */
	private R visitStringOperation(StringOperation expr, A arg) {
		return compose(expr, arg,
			descendPart(expr, arg, expr.getLeft()), descendPart(expr, arg, expr.getRight()),
			wrap(expr.isCaseSensitive()));
	}

	@Override
	public R visitContainsElement(ContainsElement expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitContainsAll(ContainsAll expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitContainsSome(ContainsSome expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getLeft(), expr.getRight()));
	}

	@Override
	public R visitForeach(Foreach expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getBase(), expr.getFunction()));
	}

	@Override
	public R visitHtml(HtmlMacro expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getContents()));
	}

	@Override
	public R visitTag(TagMacro expr, A arg) {
		return compose(expr, arg, wrap(expr.getTag()), wrap(expr.isEmpty()),
			descendParts(expr, arg, expr.getAttributes()));
	}

	@Override
	public R visitAttr(AttributeMacro expr, A arg) {
		return compose(expr, arg, wrap(expr.getName()), descendPart(expr, arg, expr.getValue()));
	}

	@Override
	public R visitDesc(Desc expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitLength(Length expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitList(ListExpr expr, A arg) {
		return compose(expr, arg, descendParts(expr, arg, expr.getElements()));
	}

	@Override
	public R visitSize(Size expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getArgument()));
	}

	@Override
	public R visitSort(Sort expr, A arg) {
		return compose(expr, arg, descendPart(expr, arg, expr.getList()), descendPart(expr, arg, expr.getComparator()));
	}

	/**
	 * Final step of creating the visit result.
	 *
	 * @param expr
	 *        The visited expression.
	 * @param arg
	 *        The visit argument.
	 * @param descendResult
	 *        The result from {@link #descendPart(SearchExpression, Object, SearchExpression)}.
	 * @return The final visit result.
	 */
	protected final R compose(SearchExpression expr, A arg, R descendResult) {
		return compose(expr, arg, Collections.<R> singletonList(descendResult));
	}

	/**
	 * Final step of creating the visit result from multiple descend results.
	 *
	 * @param expr
	 *        The visited expression.
	 * @param arg
	 *        The visit argument.
	 * @param descendResult
	 *        The result from {@link #descendParts(SearchExpression, Object, SearchExpression...)}.
	 * @return The final visit result.
	 */
	@SafeVarargs
	protected final R compose(SearchExpression expr, A arg, R... descendResult) {
		return compose(expr, arg, Arrays.asList(descendResult));
	}

	/**
	 * Final step of creating the visit result from multiple descend results.
	 *
	 * @param expr
	 *        The visited expression.
	 * @param arg
	 *        The visit argument.
	 * @param descendResult
	 *        The result from
	 *        {@link #descendParts(List, SearchExpression, Object, SearchExpression...)}.
	 * @return The final visit result.
	 */
	protected final R compose(SearchExpression expr, A arg, R r1, R r2, List<R> descendResult) {
		List<R> concat = new ArrayList<>();
		concat.add(r1);
		concat.add(r2);
		concat.addAll(descendResult);
		return compose(expr, arg, concat);
	}

	/**
	 * Generic compose for all kinds of {@link SearchExpression} computing the visit result out of
	 * the visit results form all descendants.
	 * 
	 * @param expr
	 *        The expression that is being visited.
	 * @param arg
	 *        The argument to the visit.
	 * @param descendResult
	 *        The visit results of all of its descendants.
	 * @return The final result of the visit.
	 */
	protected R compose(SearchExpression expr, A arg, List<R> descendResult) {
		return null;
	}

	/**
	 * Hook being called for all non-expression nodes during transformation.
	 * 
	 * @param value
	 *        The literal value.
	 * @return A visit result.
	 * 
	 * @see Lambda#getName() An example for a literal value in an expression tree.
	 */
	protected abstract R wrap(Object value);

}
