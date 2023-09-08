/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.top_logic.basic.util.ResKey.LangString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.AbstractMethod;
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
import com.top_logic.model.search.expr.config.dom.Expr.StaticMethod;
import com.top_logic.model.search.expr.config.dom.Expr.StringLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Sub;
import com.top_logic.model.search.expr.config.dom.Expr.TextContent;
import com.top_logic.model.search.expr.config.dom.Expr.True;
import com.top_logic.model.search.expr.config.dom.Expr.Tuple;
import com.top_logic.model.search.expr.config.dom.Expr.Tuple.Coord;
import com.top_logic.model.search.expr.config.dom.Expr.TypeLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.Var;
import com.top_logic.model.search.expr.config.dom.Expr.Wrapped;
import com.top_logic.model.search.expr.config.dom.ExprVisitor;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;

/**
 * {@link ExprVisitor} serializing an {@link Expr} in its compact form.
 * 
 * @see SearchExpressionParser
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExprPrinter implements ExprVisitor<Appendable, Appendable, IOException> {

	/**
	 * Singleton {@link ExprPrinter} instance.
	 */
	public static final ExprPrinter INSTANCE = new ExprPrinter();

	private ExprPrinter() {
		// Singleton constructor.
	}

	/**
	 * Converts the given {@link Expr} to its textual representation.
	 */
	public static String toString(Expr expr) {
		StringBuilder buffer = new StringBuilder();
		try {
			expr.visit(INSTANCE, buffer);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return buffer.toString();
	}

	private static final int NONE = -99;

	private static final int ASSIGN = -3;

	private static final int DEFINE = -1;

	private static final int OR = 0;

	private static final int AND = 1;

	private static final int EQ = 2;

	private static final int ADD = 3;

	private static final int MUL = 4;

	private static final int NOT = 5;

	private static final int APPLY = 6;

	private static final int ACCESS = 7;

	private static final int WRAPPED = 8;

	/**
	 * {@link ExprVisitor} deciding whether an {@link Expr expression} must be parenthesized, when
	 * it is used within an expression with binding strength given as argument.
	 */
	private static class NeedsParentesis implements ExprVisitor<Boolean, Integer, RuntimeException> {

		/**
		 * Singleton {@link ExprPrinter.NeedsParentesis} instance.
		 */
		@SuppressWarnings("hiding")
		public static final NeedsParentesis INSTANCE = new NeedsParentesis();

		private NeedsParentesis() {
			// Singleton constructor.
		}

			@Override
			public Boolean visit(Null expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(True expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(False expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(StringLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(Html expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(ResKeyReference expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(ResKeyLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(NumberLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(ModuleLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(SingletonLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(TypeLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(PartLiteral expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(Tuple expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(StartTag expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(EndTag expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(Attribute expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(AttributeContents expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(TextContent expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(EmbeddedExpression expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(And expr, Integer arg) throws RuntimeException {
				return arg > AND;
			}

			@Override
			public Boolean visit(Or expr, Integer arg) throws RuntimeException {
				return arg > OR;
			}

			@Override
			public Boolean visit(Not expr, Integer arg) throws RuntimeException {
				return arg > NOT;
			}

			@Override
			public Boolean visit(Add expr, Integer arg) throws RuntimeException {
				return arg > ADD;
			}

			@Override
			public Boolean visit(Sub expr, Integer arg) throws RuntimeException {
				return arg > ADD;
			}

			@Override
			public Boolean visit(Mul expr, Integer arg) throws RuntimeException {
				return arg > MUL;
			}

			@Override
			public Boolean visit(Div expr, Integer arg) throws RuntimeException {
				return arg > MUL;
			}

			@Override
			public Boolean visit(Mod expr, Integer arg) throws RuntimeException {
				return arg > MUL;
			}

			@Override
			public Boolean visit(Eq expr, Integer arg) throws RuntimeException {
				return arg >= EQ;
			}

			@Override
			public Boolean visit(Cmp expr, Integer arg) throws RuntimeException {
				return arg >= EQ;
			}

			@Override
			public Boolean visit(Define expr, Integer arg) throws RuntimeException {
				return arg > DEFINE;
			}

		@Override
		public Boolean visit(Apply expr, Integer arg) throws RuntimeException {
			return arg > APPLY;
		}

		@Override
		public Boolean visit(At expr, Integer arg) throws RuntimeException {
			return arg > APPLY;
		}

		@Override
		public Boolean visit(Assign expr, Integer arg) throws RuntimeException {
			return arg > ASSIGN;
		}

			@Override
			public Boolean visit(Block expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(Var expr, Integer arg) throws RuntimeException {
				return false;
			}

			@Override
			public Boolean visit(Method expr, Integer arg) throws RuntimeException {
				return arg > ACCESS;
			}

			@Override
			public Boolean visit(StaticMethod expr, Integer arg) throws RuntimeException {
				return false;
			}
			
			@Override
			public Boolean visit(Wrapped expr, Integer arg) throws RuntimeException {
				// Note: When using a wrapped expression within another expression, this is treated
				// like
				return arg > WRAPPED;
			}
	}

	@Override
	public Appendable visit(Null expr, Appendable arg) throws IOException {
		arg.append("null");
		return arg;
	}

	@Override
	public Appendable visit(True expr, Appendable arg) throws IOException {
		arg.append("true");
		return arg;
	}

	@Override
	public Appendable visit(False expr, Appendable arg) throws IOException {
		arg.append("false");
		return arg;
	}

	@Override
	public Appendable visit(StringLiteral expr, Appendable arg) throws IOException {
		arg.append('"');
		arg.append(quote(expr.getValue()));
		arg.append('"');
		return arg;
	}

	@Override
	public Appendable visit(Html expr, Appendable arg) throws IOException {
		arg.append("{{{");
		for (HtmlContent x : expr.getContents()) {
			x.visit(this, arg);
		}
		arg.append("}}}");
		return arg;
	}

	@Override
	public Appendable visit(StartTag node, Appendable arg) throws IOException {
		arg.append('<');
		arg.append(node.getTag());
		for (Attribute attr : node.getAttributes()) {
			attr.visit(this, arg);
		}
		if (node.isEmpty()) {
			arg.append("/>");
		} else {
			arg.append('>');
		}
		return arg;
	}

	@Override
	public Appendable visit(Attribute expr, Appendable arg) throws IOException {
		arg.append(' ');
		arg.append(expr.getName());
		arg.append('=');
		arg.append('"');
		expr.getValue().visit(this, arg);
		arg.append('"');
		return arg;
	}

	@Override
	public Appendable visit(AttributeContents value, Appendable arg) throws IOException {
		for (AttributeContent element : value.getValues()) {
			element.visit(this, arg);
		}
		return arg;
	}

	@Override
	public Appendable visit(TextContent node, Appendable arg) throws IOException {
		arg.append(node.getValue());
		return arg;
	}

	@Override
	public Appendable visit(EmbeddedExpression node, Appendable arg) throws IOException {
		arg.append('{');
		node.getExpr().visit(this, arg);
		arg.append('}');
		return arg;
	}

	@Override
	public Appendable visit(EndTag node, Appendable arg) throws IOException {
		arg.append("</");
		arg.append(node.getTag());
		arg.append('>');
		return null;
	}

	@Override
	public Appendable visit(ResKeyReference expr, Appendable arg) throws IOException {
		arg.append('#');
		arg.append('"');
		arg.append(quote(expr.getKey()));
		arg.append('"');
		return arg;
	}

	@Override
	public Appendable visit(ResKeyLiteral expr, Appendable arg) throws IOException {
		arg.append("#(");
		appendLiteralContents(arg, expr);
		arg.append(')');
		return arg;
	}

	private void appendLiteralContents(Appendable out, ResKeyLiteral expr) throws IOException {
		boolean first = true;
		for (LangString s : expr.getValues().values()) {
			if (first) {
				first = false;
			} else {
				out.append(", ");
			}
			out.append('"');
			out.append(quote(s.getText()));
			out.append('"');
			out.append('@');
			out.append(s.getLang().toLanguageTag());
		}
		for (Entry<String, ResKeyLiteral> entry : expr.getSuffixes().entrySet()) {
			if (first) {
				first = false;
			} else {
				out.append(", ");
			}
			entry.getKey();
			out.append(": {");
			appendLiteralContents(out, entry.getValue());
			out.append("}");

		}
	}

	private String quote(String value) {
		return value.replaceAll("([\"\\\\])", "\\\\$1");
	}

	@Override
	public Appendable visit(NumberLiteral expr, Appendable arg) throws IOException {
		arg.append(Double.toString(expr.getValue()));
		return arg;
	}

	@Override
	public Appendable visit(ModuleLiteral expr, Appendable arg) throws IOException {
		arg.append('`');
		arg.append(expr.getName());
		arg.append('`');
		return arg;
	}

	@Override
	public Appendable visit(SingletonLiteral expr, Appendable arg) throws IOException {
		arg.append('`');
		arg.append(expr.getModule());
		arg.append('#');
		arg.append(expr.getName());
		arg.append('`');
		return arg;
	}

	@Override
	public Appendable visit(TypeLiteral expr, Appendable arg) throws IOException {
		arg.append('`');
		arg.append(expr.getModule());
		arg.append(':');
		arg.append(expr.getName());
		arg.append('`');
		return arg;
	}

	@Override
	public Appendable visit(PartLiteral expr, Appendable arg) throws IOException {
		arg.append('`');
		arg.append(expr.getModule());
		arg.append(':');
		arg.append(expr.getType());
		arg.append('#');
		arg.append(expr.getName());
		arg.append('`');
		return arg;
	}

	@Override
	public Appendable visit(And expr, Appendable arg) throws IOException {
		boolean first = true;
		for (Expr part : expr.getOperands()) {
			if (first) {
				first = false;
			} else {
				arg.append(" and ");
			}
			descend(part, arg, AND);
		}
		if (first) {
			arg.append("true");
		}
		return arg;
	}

	@Override
	public Appendable visit(Or expr, Appendable arg) throws IOException {
		boolean first = true;
		for (Expr part : expr.getOperands()) {
			if (first) {
				first = false;
			} else {
				arg.append(" or ");
			}
			descend(part, arg, OR);
		}
		if (first) {
			arg.append("false");
		}
		return arg;
	}

	@Override
	public Appendable visit(Not expr, Appendable arg) throws IOException {
		arg.append("!");
		descend(expr.getExpr(), arg, NOT);
		return arg;
	}

	@Override
	public Appendable visit(Add expr, Appendable arg) throws IOException {
		return op(expr, arg, " + ", ADD);
	}

	@Override
	public Appendable visit(Sub expr, Appendable arg) throws IOException {
		return op(expr, arg, " - ", ADD);
	}

	@Override
	public Appendable visit(Mul expr, Appendable arg) throws IOException {
		return op(expr, arg, " * ", MUL);
	}

	@Override
	public Appendable visit(Div expr, Appendable arg) throws IOException {
		return op(expr, arg, " / ", MUL);
	}

	@Override
	public Appendable visit(Mod expr, Appendable arg) throws IOException {
		return op(expr, arg, " % ", MUL);
	}

	@Override
	public Appendable visit(Eq expr, Appendable arg) throws IOException {
		return op(expr, arg, " == ", EQ);
	}

	private Appendable op(Operation expr, Appendable arg, String op, int prio) throws IOException {
		List<Expr> operands = expr.getOperands();
		descend(operands.get(0), arg, prio);
		arg.append(op);
		descend(operands.get(1), arg, prio);
		return arg;
	}

	@Override
	public Appendable visit(Cmp expr, Appendable arg) throws IOException {
		List<Expr> operands = expr.getOperands();
		descend(operands.get(0), arg, EQ);
		arg.append(" ");
		arg.append(expr.getKind().getOperator());
		arg.append(" ");
		descend(operands.get(1), arg, EQ);
		return arg;
	}

	@Override
	public Appendable visit(Block expr, Appendable arg) throws IOException {
		arg.append("{");
		for (Expr content : expr.getContents()) {
			descendDirect(content, arg);
			arg.append("; ");
		}
		arg.append("}");
		return null;
	}

	@Override
	public Appendable visit(Define expr, Appendable arg) throws IOException {
		arg.append(expr.getName());
		arg.append(" -> ");
		descend(expr.getExpr(), arg, DEFINE);
		return arg;
	}

	@Override
	public Appendable visit(Apply expr, Appendable arg) throws IOException {
		descend(expr.getFun(), arg, APPLY);
		arg.append("(");
		descend(expr.getArg(), arg, NONE);
		arg.append(")");
		return null;
	}

	@Override
	public Appendable visit(Assign expr, Appendable arg) throws IOException {
		arg.append(expr.getName());
		arg.append(" = ");
		descend(expr.getExpr(), arg, ASSIGN);
		return null;
	}

	@Override
	public Appendable visit(At expr, Appendable arg) throws IOException {
		descend(expr.getSelf(), arg, APPLY);
		arg.append("[");
		descend(expr.getIndex(), arg, APPLY);
		arg.append("]");
		return null;
	}

	@Override
	public Appendable visit(Tuple expr, Appendable arg) throws IOException {
		arg.append("tuple(");
		boolean first = true;
		for (Coord coord : expr.getCoords()) {
			if (first) {
				first = false;
			} else {
				arg.append(", ");
			}
			arg.append(coord.getName());
			if (coord.isOptional()) {
				arg.append("?");
			}
			arg.append(" -> ");
			descend(coord.getExpr(), arg, DEFINE);
		}
		arg.append(")");
		return arg;
	}

	@Override
	public Appendable visit(Var expr, Appendable arg) throws IOException {
		arg.append("$");
		arg.append(expr.getName());
		return arg;
	}

	@Override
	public Appendable visit(Method expr, Appendable arg) throws IOException {
		descend(expr.getSelf(), arg, ACCESS);
		arg.append(".");
		printMethod(expr, arg);
		return arg;
	}

	@Override
	public Appendable visit(StaticMethod expr, Appendable arg) throws IOException {
		printMethod(expr, arg);
		return arg;
	}

	private void printMethod(AbstractMethod expr, Appendable arg) throws IOException {
		arg.append(expr.getName());
		arg.append("(");
		boolean first = true;
		for (Arg argument : expr.getArgs()) {
			if (first) {
				first = false;
			} else {
				arg.append(", ");
			}
			String name = argument.getName();
			if (name != null) {
				arg.append(name);
				arg.append(": ");
			}
			descendDirect(argument.getValue(), arg);
		}
		arg.append(")");
	}

	@Override
	public Appendable visit(Wrapped expr, Appendable arg) throws IOException {
		String originalSrc = expr.getSrc();
		if (originalSrc != null) {
			arg.append(originalSrc);
		} else {
			descend(expr, arg, WRAPPED);
		}
		return null;
	}

	private void descend(Expr expr, Appendable arg, int context) throws IOException {
		if (expr.visit(NeedsParentesis.INSTANCE, context)) {
			arg.append('(');
			descendDirect(expr, arg);
			arg.append(')');
		} else {
			descendDirect(expr, arg);
		}
	}

	private void descendDirect(Expr expr, Appendable arg) throws IOException {
		expr.visit(this, arg);
	}

}
