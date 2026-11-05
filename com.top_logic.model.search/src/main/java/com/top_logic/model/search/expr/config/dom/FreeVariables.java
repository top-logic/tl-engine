/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.dom;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr.Access;
import com.top_logic.model.search.expr.config.dom.Expr.AccessLike;
import com.top_logic.model.search.expr.config.dom.Expr.Add;
import com.top_logic.model.search.expr.config.dom.Expr.And;
import com.top_logic.model.search.expr.config.dom.Expr.Apply;
import com.top_logic.model.search.expr.config.dom.Expr.Arg;
import com.top_logic.model.search.expr.config.dom.Expr.Assign;
import com.top_logic.model.search.expr.config.dom.Expr.At;
import com.top_logic.model.search.expr.config.dom.Expr.Attribute;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContent;
import com.top_logic.model.search.expr.config.dom.Expr.AttributeContents;
import com.top_logic.model.search.expr.config.dom.Expr.Block;
import com.top_logic.model.search.expr.config.dom.Expr.Chain;
import com.top_logic.model.search.expr.config.dom.Expr.Cmp;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.config.dom.Expr.Div;
import com.top_logic.model.search.expr.config.dom.Expr.EmbeddedExpression;
import com.top_logic.model.search.expr.config.dom.Expr.EndTag;
import com.top_logic.model.search.expr.config.dom.Expr.Eq;
import com.top_logic.model.search.expr.config.dom.Expr.False;
import com.top_logic.model.search.expr.config.dom.Expr.Html;
import com.top_logic.model.search.expr.config.dom.Expr.HtmlContent;
import com.top_logic.model.search.expr.config.dom.Expr.Method;
import com.top_logic.model.search.expr.config.dom.Expr.Mod;
import com.top_logic.model.search.expr.config.dom.Expr.ModuleLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Mul;
import com.top_logic.model.search.expr.config.dom.Expr.Not;
import com.top_logic.model.search.expr.config.dom.Expr.Null;
import com.top_logic.model.search.expr.config.dom.Expr.NumberLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Operation;
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
import com.top_logic.model.search.expr.config.dom.Expr.Tuple.Coord;
import com.top_logic.model.search.expr.config.dom.Expr.TypeLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Var;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.interpreter.DefResolver;

/**
 * The free variables of a TL-Script syntax tree: the names of the variables it uses without
 * defining them.
 *
 * <p>
 * The analysis works on the parsed {@link Expr}, so it needs neither the application model nor the
 * registry of script functions, unlike a compilation. A variable is free when compiling the script
 * fails because the variable is not defined. The reference for these scoping rules is the
 * resolution of variables in the compiled expression, {@link DefResolver}, together with the
 * translation of a block by {@link SearchBuilder#visit(Block, com.top_logic.model.TLModel)}:
 * </p>
 *
 * <ul>
 * <li>A function <code>x -&gt; ...</code> ({@link Define}) defines its parameter for its
 * body.</li>
 * <li>A coordinate of a {@link Tuple} defines its name for the coordinates following it, not for
 * its own value.</li>
 * <li>An assignment <code>x = ...;</code> in a {@link Block} defines its name for the rest of the
 * block, not for its own value. An assignment outside a block defines nothing.</li>
 * </ul>
 */
public final class FreeVariables implements ExprVisitor<Void, Void, RuntimeException> {

	/**
	 * The number of enclosing definitions per variable name.
	 */
	private final Map<String, Integer> _defined = new HashMap<>();

	private final Set<String> _free = new LinkedHashSet<>();

	private FreeVariables() {
		// Created per analysis.
	}

	/**
	 * The names of the variables the given script uses without defining them, in the order of
	 * their first use.
	 *
	 * @param script
	 *        The script to analyze.
	 * @return The names of the free variables, empty if the script defines all variables it uses.
	 */
	public static Set<String> of(Expr script) {
		FreeVariables analysis = new FreeVariables();
		analysis.descend(script);
		return analysis._free;
	}

	/**
	 * The names of the variables the given script uses that are neither defined by the script
	 * itself nor given as defined around it.
	 *
	 * @param script
	 *        The script to analyze.
	 * @param defined
	 *        The names of the variables defined around the script.
	 * @return The names of the undefined variables, in the order of their first use.
	 */
	public static Set<String> undefined(Expr script, Collection<String> defined) {
		Set<String> result = of(script);
		result.removeAll(defined);
		return result;
	}

	private void descend(Expr expr) {
		if (expr != null) {
			expr.visit(this, null);
		}
	}

	private void descendAll(List<? extends Expr> exprs) {
		for (Expr expr : exprs) {
			descend(expr);
		}
	}

	private void enter(String name) {
		_defined.merge(name, 1, Integer::sum);
	}

	private void exit(String name) {
		_defined.computeIfPresent(name, (key, count) -> count == 1 ? null : count - 1);
	}

	private Void operation(Operation expr) {
		descendAll(expr.getOperands());
		return null;
	}

	private Void access(AccessLike expr) {
		descend(expr.getExpr());
		descend(expr.getMethod());
		return null;
	}

	@Override
	public Void visit(Var expr, Void arg) {
		String name = expr.getName();
		if (!_defined.containsKey(name)) {
			_free.add(name);
		}
		return null;
	}

	@Override
	public Void visit(Define expr, Void arg) {
		String name = expr.getName();
		enter(name);
		descend(expr.getExpr());
		exit(name);
		return null;
	}

	@Override
	public Void visit(Tuple expr, Void arg) {
		List<Coord> coords = expr.getCoords();
		for (Coord coord : coords) {
			descend(coord.getExpr());
			enter(coord.getName());
		}
		for (Coord coord : coords) {
			exit(coord.getName());
		}
		return null;
	}

	@Override
	public Void visit(Block expr, Void arg) {
		List<Expr> contents = expr.getContents();
		int assigned = 0;
		for (Expr content : contents) {
			if (content instanceof Assign assign) {
				descend(assign.getExpr());
				enter(assign.getName());
				assigned++;
			} else {
				descend(content);
			}
		}
		for (int n = contents.size() - 1; assigned > 0; n--) {
			if (contents.get(n) instanceof Assign assign) {
				exit(assign.getName());
				assigned--;
			}
		}
		return null;
	}

	@Override
	public Void visit(Assign expr, Void arg) {
		descend(expr.getExpr());
		return null;
	}

	@Override
	public Void visit(Wrapped expr, Void arg) {
		descend(expr.getExpr());
		return null;
	}

	@Override
	public Void visit(Apply expr, Void arg) {
		descend(expr.getFun());
		descend(expr.getArg());
		return null;
	}

	@Override
	public Void visit(At expr, Void arg) {
		descend(expr.getSelf());
		descend(expr.getIndex());
		return null;
	}

	@Override
	public Void visit(Method expr, Void arg) {
		for (Arg argument : expr.getArgs()) {
			descend(argument.getValue());
		}
		return null;
	}

	@Override
	public Void visit(Access expr, Void arg) {
		return access(expr);
	}

	@Override
	public Void visit(Chain expr, Void arg) {
		return access(expr);
	}

	@Override
	public Void visit(Not expr, Void arg) {
		descend(expr.getExpr());
		return null;
	}

	@Override
	public Void visit(Add expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Sub expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Mul expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Div expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Mod expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Cmp expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(And expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Or expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Eq expr, Void arg) {
		return operation(expr);
	}

	@Override
	public Void visit(Html expr, Void arg) {
		for (HtmlContent content : expr.getContents()) {
			descend(content);
		}
		return null;
	}

	@Override
	public Void visit(StartTag expr, Void arg) {
		for (Attribute attribute : expr.getAttributes()) {
			descend(attribute);
		}
		return null;
	}

	@Override
	public Void visit(Attribute expr, Void arg) {
		descend(expr.getValue());
		return null;
	}

	@Override
	public Void visit(AttributeContents expr, Void arg) {
		for (AttributeContent content : expr.getValues()) {
			descend(content);
		}
		return null;
	}

	@Override
	public Void visit(EmbeddedExpression expr, Void arg) {
		descend(expr.getExpr());
		return null;
	}

	@Override
	public Void visit(EndTag expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(TextContent expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(Null expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(True expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(False expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(StringLiteral expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(ResKeyReference expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(ResKeyLiteral expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(NumberLiteral expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(ModuleLiteral expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(SingletonLiteral expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(TypeLiteral expr, Void arg) {
		return null;
	}

	@Override
	public Void visit(PartLiteral expr, Void arg) {
		return null;
	}

}
