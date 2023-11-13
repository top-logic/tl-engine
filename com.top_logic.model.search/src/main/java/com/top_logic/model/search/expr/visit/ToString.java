/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.visit;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.Collection;

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
import com.top_logic.model.search.expr.TupleExpression.Coord;
import com.top_logic.model.search.expr.UnaryOperation;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.html.AttributeMacro;
import com.top_logic.model.search.expr.html.HtmlMacro;
import com.top_logic.model.search.expr.html.TagMacro;
import com.top_logic.model.util.TLModelUtil;

/**
 * Visitor creating a human-readable representation of a {@link SearchExpression} model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToString implements Visitor<Void, StringBuilder> {

	/**
	 * Singleton {@link ToString} instance.
	 */
	public static final ToString INSTANCE = new ToString();

	private ToString() {
		// Singleton constructor.
	}

	@Override
	public Void visitLambda(Lambda expr, StringBuilder out) {
		out.append("(");
		out.append(expr.getName());
		out.append(" -> ");
		expr.getBody().visit(this, out);
		out.append(")");

		return none();
	}

	@Override
	public Void visitCall(Call expr, StringBuilder out) {
		SearchExpression function = expr.getFunction();
		if (function instanceof Lambda) {
			Lambda lambda = (Lambda) function;
			out.append("{");
			out.append(lambda.getName());
			out.append(" = ");
			expr.getArgument().visit(this, out);
			out.append("; ");
			lambda.getBody().visit(this, out);
			out.append("; ");
			out.append("}");
		} else {
			function.visit(this, out);
			out.append("(");
			expr.getArgument().visit(this, out);
			out.append(")");
		}

		return none();
	}

	@Override
	public Void visitRecursion(Recursion recursion, StringBuilder out) {
		out.append(SearchBuilder.RECURSION_FUN);
		out.append("(");
		recursion.getStart().visit(this, out);
		out.append(", ");
		recursion.getFunction().visit(this, out);
		out.append(", ");
		out.append(recursion.getMinDepth());
		out.append(", ");
		out.append(recursion.getMaxDepth());
		out.append(")");

		return none();
	}

	@Override
	public Void visitVar(Var expr, StringBuilder out) {
		out.append('$');
		out.append(expr.getName());

		return none();
	}

	@Override
	public Void visitAll(All expr, StringBuilder out) {
		out.append("all(");
		out.append(TLModelUtil.qualifiedName(expr.getInstanceType()));
		out.append(")");
		return none();
	}

	@Override
	public Void visitTuple(TupleExpression expr, StringBuilder out) {
		out.append("tuple(");
		Coord[] coords = expr.getCoords();
		for (int cnt = coords.length, n = 0; n < cnt; n++) {
			if (n > 0) {
				out.append(", ");
			}
			Coord coord = coords[n];
			out.append(coord.getName());
			if (coord.isOptional()) {
				out.append("?");
			}
			out.append(" -> ");
			coord.getExpr().visit(this, out);
		}
		out.append(")");
		return none();
	}

	@Override
	public Void visitKBQuery(KBQuery expr, StringBuilder out) {
		out.append("query(");
		out.append(expr.getQuery());
		out.append(" : ");
		out.append(expr.getClassType());
		out.append(")");
		return none();
	}

	@Override
	public Void visitInstanceOf(InstanceOf expr, StringBuilder out) {
		out.append('(');
		expr.getValue().visit(this, out);
		out.append(" instanceof ");
		out.append(TLModelUtil.qualifiedName(expr.getCheckType()));
		out.append(')');
		return none();
	}

	@Override
	public Void visitUnion(Union expr, StringBuilder out) {
		binaryOperation(out, "union", expr);
		return none();
	}

	@Override
	public Void visitIntersection(Intersection expr, StringBuilder out) {
		binaryOperation(out, "intersection", expr);
		return none();
	}

	@Override
	public Void visitForeach(Foreach expr, StringBuilder out) {
		operation(out, "foreach", expr.getBase(), expr.getFunction());
		return none();
	}

	@Override
	public Void visitFlatten(Flatten expr, StringBuilder out) {
		unaryOperation(out, "flatten", expr);
		return none();
	}

	@Override
	public Void visitSingleton(Singleton expr, StringBuilder out) {
		unaryOperation(out, "singleton", expr);
		return none();
	}

	@Override
	public Void visitSingleElement(SingleElement expr, StringBuilder out) {
		unaryOperation(out, "singleElement", expr);
		return none();
	}

	@Override
	public Void visitFilter(Filter expr, StringBuilder out) {
		operation(out, "filter", expr.getBase(), expr.getFunction());
		return none();
	}

	@Override
	public Void visitGenericMethod(GenericMethod expr, StringBuilder out) {
		operation(out, expr.getName(), expr.getArguments());
		return none();
	}

	@Override
	public Void visitAccess(Access expr, StringBuilder out) {
		expr.getSelf().visit(this, out);
		out.append(".");
		out.append(expr.getPart().getName());

		return none();
	}

	@Override
	public Void visitAt(At expr, StringBuilder out) {
		expr.getSelf().visit(this, out);
		out.append("[");
		expr.getIndex().visit(this, out);
		out.append("]");

		return none();
	}

	@Override
	public Void visitUpdate(Update expr, StringBuilder out) {
		expr.getSelf().visit(this, out);
		out.append(".update(");
		out.append("`");
		out.append(expr.getPart());
		out.append("`");
		out.append(", ");
		expr.getValue().visit(this, out);
		out.append(")");

		return none();
	}

	@Override
	public Void visitBlock(Block expr, StringBuilder out) {
		out.append("{");
		for (SearchExpression content : expr.getContents()) {
			content.visit(this, out);
			out.append("; ");
		}
		out.append("}");

		return none();
	}

	@Override
	public Void visitReferers(Referers expr, StringBuilder out) {
		expr.getTarget().visit(this, out);
		out.append(".");
		out.append("referers(`");
		out.append(TLModelUtil.qualifiedName(expr.getReference()));
		out.append("`)");

		return none();
	}

	@Override
	public Void visitAssociationNavigation(AssociationNavigation expr, StringBuilder out) {
		out.append(expr.getDestinationEnd().getOwner().getName());
		out.append(".");
		out.append(expr.getDestinationEnd().getName());
		out.append(" for ");
		out.append(expr.getSourceEnd().getOwner().getName());
		out.append(".");
		out.append(expr.getSourceEnd().getName());
		out.append(" = ");
		expr.getSource().visit(this, out);

		return none();
	}

	@Override
	public Void visitLiteral(Literal expr, StringBuilder out) {
		Object value = expr.getValue();
		if (value instanceof String) {
			out.append('"');
			out.append(value);
			out.append('"');
		} else if (value instanceof Collection<?> && ((Collection<?>) value).isEmpty()) {
			out.append("none()");
		} else {
			out.append(value);
		}
		return none();
	}

	@Override
	public Void visitIsEmpty(IsEmpty expr, StringBuilder out) {
		unaryOperation(out, "isEmpty", expr);
		return none();
	}

	@Override
	public Void visitNot(Not expr, StringBuilder out) {
		unaryOperation(out, "not", expr);
		return none();
	}
	
	@Override
	public Void visitArithmetic(ArithmeticExpr expr, StringBuilder arg) {
		operation(arg, expr.getOp().name().toLowerCase(), expr.getLeft(), expr.getRight());
		return none();
	}

	@Override
	public Void visitAnd(And expr, StringBuilder out) {
		binaryOperation(out, "and", expr);
		return none();
	}

	@Override
	public Void visitOr(Or expr, StringBuilder out) {
		binaryOperation(out, "or", expr);
		return none();
	}

	@Override
	public Void visitIfElse(IfElse expr, StringBuilder out) {
		out.append('(');
		expr.getCondition().visit(this, out);
		out.append(" ? ");
		expr.getIfClause().visit(this, out);
		out.append(" : ");
		expr.getElseClause().visit(this, out);
		out.append(')');
		return none();
	}

	@Override
	public Void visitEquals(IsEqual expr, StringBuilder out) {
		binaryOperation(out, "isEqual", expr);
		return none();
	}

	@Override
	public Void visitCompare(Compare expr, StringBuilder out) {
		operation(out, "compare", expr.getLeft(), expr.getRight());
		return none();
	}

	@Override
	public Void visitCompareOp(CompareOp expr, StringBuilder out) {
		expr.getLeft().visit(this, out);
		out.append(" ");
		out.append(expr.getKind().getOperator());
		out.append(" ");
		expr.getRight().visit(this, out);
		return none();
	}

	@Override
	public Void visitRound(Round expr, StringBuilder out) {
		operation(out, "round", expr.getLeft(), expr.getRight());
		return none();
	}

	@Override
	public Void visitGetDay(GetDay expr, StringBuilder out) {
		operation(out, "day", expr.getArgument());
		return none();
	}

	@Override
	public Void visitStringEquals(IsStringEqual expr, StringBuilder out) {
		operation(out, "isStringEqual", expr.getLeft(), expr.getRight(), literal(expr.isCaseSensitive()));
		return none();
	}

	@Override
	public Void visitStringGreater(IsStringGreater expr, StringBuilder out) {
		operation(out, "isStringGreater", expr.getLeft(), expr.getRight(), literal(expr.isCaseSensitive()));
		return none();
	}

	@Override
	public Void visitStringContains(StringContains expr, StringBuilder out) {
		operation(out, "stringContains", expr.getLeft(), expr.getRight(), literal(expr.isCaseSensitive()));
		return none();
	}

	@Override
	public Void visitStringStartsWith(StringStartsWith expr, StringBuilder out) {
		operation(out, "stringStartsWith", expr.getLeft(), expr.getRight(), literal(expr.isCaseSensitive()));
		return none();
	}

	@Override
	public Void visitStringEndsWith(StringEndsWith expr, StringBuilder out) {
		operation(out, "stringEndsWith", expr.getLeft(), expr.getRight(), literal(expr.isCaseSensitive()));
		return none();
	}

	@Override
	public Void visitDesc(Desc expr, StringBuilder out) {
		operation(out, "desc", expr.getArgument());
		return none();
	}

	@Override
	public Void visitLength(Length expr, StringBuilder arg) {
		operation(arg, "length", expr.getArgument());
		return none();
	}

	@Override
	public Void visitSize(Size expr, StringBuilder arg) {
		operation(arg, "size", expr.getArgument());
		return none();
	}

	@Override
	public Void visitList(ListExpr expr, StringBuilder arg) {
		operation(arg, "list", expr.getElements());
		return none();
	}

	@Override
	public Void visitSort(Sort expr, StringBuilder arg) {
		operation(arg, "sort", expr.getList(), expr.getComparator());
		return none();
	}

	@Override
	public Void visitContainsElement(ContainsElement expr, StringBuilder out) {
		binaryOperation(out, "containsElement", expr);
		return none();
	}

	@Override
	public Void visitContainsAll(ContainsAll expr, StringBuilder out) {
		binaryOperation(out, "containsAll", expr);
		return none();
	}

	@Override
	public Void visitContainsSome(ContainsSome expr, StringBuilder out) {
		binaryOperation(out, "containsSome", expr);
		return none();
	}

	@Override
	public Void visitHtml(HtmlMacro expr, StringBuilder arg) {
		arg.append("{{{");
		for (SearchExpression content : expr.getContents()) {
			content.visit(this, arg);
		}
		arg.append("}}}");
		return none();
	}

	@Override
	public Void visitTag(TagMacro expr, StringBuilder arg) {
		arg.append('<');
		arg.append(expr.getTag());
		for (SearchExpression attr : expr.getAttributes()) {
			arg.append(attr.visit(this, arg));
		}
		if (expr.isEmpty()) {
			arg.append("/>");
		} else {
			arg.append('>');
		}
		return none();
	}

	@Override
	public Void visitAttr(AttributeMacro expr, StringBuilder arg) {
		arg.append(' ');
		arg.append(expr.getName());
		arg.append('=');
		arg.append('"');
		expr.getValue().visit(this, arg);
		arg.append('"');
		return none();
	}

	private void unaryOperation(StringBuilder out, String name, UnaryOperation expr) {
		operation(out, name, expr.getArgument());
	}

	private void binaryOperation(StringBuilder out, String name, BinaryOperation expr) {
		operation(out, name, expr.getLeft(), expr.getRight());
	}

	private void operation(StringBuilder out, String name, SearchExpression... args) {
		out.append(name);
		out.append("(");
		boolean first = true;
		for (SearchExpression expr : args) {
			if (first) {
				first = false;
			} else {
				out.append(", ");
			}
			expr.visit(this, out);
		}
		out.append(")");
	}

	private Void none() {
		return null;
	}

}
