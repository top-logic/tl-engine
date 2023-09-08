/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.treexf;

import static com.top_logic.basic.treexf.TransformFactory.*;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.treexf.DefaultMatch;
import com.top_logic.basic.treexf.Expr;
import com.top_logic.basic.treexf.ExprCapture;
import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.TreeMaterializer;
import com.top_logic.basic.treexf.TreeTransformer;
import com.top_logic.basic.treexf.Value;
import com.top_logic.basic.treexf.ValueCapture;

/**
 * Test case for {@link TreeTransformer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTreeTransformer extends TestCase {

	public abstract static class ExprImpl {

		public abstract int eval(Map<String, Integer> bindings);

	}

	public static class Plus extends ExprImpl {

		private final ExprImpl[] _ops;

		public Plus(ExprImpl... ops) {
			_ops = ops;
		}

		@Override
		public int eval(Map<String, Integer> bindings) {
			int result = 0;
			for (int n = 0, cnt = _ops.length; n < cnt; n++) {
				result += _ops[n].eval(bindings);
			}
			return result;
		}
	}

	public static class Mul extends ExprImpl {
		private final ExprImpl _left;

		private final ExprImpl _right;

		public Mul(ExprImpl left, ExprImpl right) {
			_left = left;
			_right = right;
		}

		@Override
		public int eval(Map<String, Integer> bindings) {
			return _left.eval(bindings) * _right.eval(bindings);
		}
	}

	public static class Literal extends ExprImpl {
		private final int _value;

		public Literal(int value) {
			_value = value;
		}

		@Override
		public int eval(Map<String, Integer> bindings) {
			return _value;
		}
	}

	public static class Var extends ExprImpl {
		private final String _name;

		public Var(String name) {
			_name = name;
		}

		@Override
		public int eval(Map<String, Integer> bindings) {
			return bindings.get(_name);
		}
	}

	public static class Factory {
		public static Plus plus(ExprImpl... ops) {
			return new Plus(ops);
		}

		public static Mul mul(ExprImpl left, ExprImpl right) {
			return new Mul(left, right);
		}

		public static Literal literal(int value) {
			return new Literal(value);
		}

		public static Var var(String name) {
			return new Var(name);
		}
	}

	public void testMaterialize() {
		Expr expr = plus(val(3), val(4));
		ExprImpl impl = (ExprImpl) TreeMaterializer.createTreeMaterializer(Factory.class).materialize(expr);
		assertEquals(7, impl.eval(Collections.<String, Integer> emptyMap()));
	}

	public void testValueCapture() {
		ValueCapture v = valueCapture("v");
		TreeTransformer transformer = new TreeTransformer(
			transformation(
				plus(var(v), var(v)),
				mul(val(2), var(v))));

		Expr expr = plus(var("x"), var("x"));
		Node result = transformer.transform(new DefaultMatch(), expr);
		assertEqualsExpr(mul(val(2), var("x")), result);

		Expr expr2 = plus(var("x"), var("y"));
		Node result2 = transformer.transform(new DefaultMatch(), expr2);
		assertEqualsExpr(plus(var("x"), var("y")), result2);
	}

	public void testExprCapture1() {
		ExprCapture v = exprCapture("v");
		TreeTransformer transformer = new TreeTransformer(
			transformation(
				plus(v, v),
				mul(val(2), v)));

		Expr expr = plus(var("x"), var("x"));
		Node result = transformer.transform(new DefaultMatch(), expr);
		assertEqualsExpr(mul(val(2), var("x")), result);

		Expr expr2 = plus(var("x"), var("y"));
		Node result2 = transformer.transform(new DefaultMatch(), expr2);
		assertEqualsExpr(plus(var("x"), var("y")), result2);
	}

	public void testExprCapture2() {
		ExprCapture e1 = exprCapture("e1");
		ExprCapture e2 = exprCapture("e2");
		ExprCapture e3 = exprCapture("e3");
		TreeTransformer transformer = new TreeTransformer(
			transformation(
				mul(e1, plus(e2, e3)),
				plus(mul(e1, e2), mul(e1, e3))));

		Expr expr = mul(val(2), plus(var("x"), var("y")));
		Node result = transformer.transform(new DefaultMatch(), expr);
		
		assertEqualsExpr(plus(mul(val(2), var("x")), mul(val(2), var("y"))), result);
	}

	private void assertEqualsExpr(Node expected, Node actual) {
		assertEquals(expected.toString(), actual.toString());
	}

	private static Expr plus(Node... ops) {
		return expr(Plus.class, ops);
	}

	private static Expr mul(Node left, Node right) {
		return expr(Mul.class, left, right);
	}

	private static Expr val(int value) {
		return expr(Literal.class, value(value));
	}

	private static Expr var(String name) {
		return var(value(name));
	}

	private static Expr var(Value name) {
		return expr(Var.class, name);
	}

}
