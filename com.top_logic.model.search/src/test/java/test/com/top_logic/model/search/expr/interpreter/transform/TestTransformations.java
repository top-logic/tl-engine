/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.interpreter.transform;

import static com.top_logic.basic.treexf.TransformFactory.*;
import static com.top_logic.model.search.expr.interpreter.transform.GenericSearchExprFactory.*;
import static test.com.top_logic.model.search.expr.interpreter.transform.GenericSearchExprTestFactory.*;
import junit.framework.TestCase;

import com.top_logic.basic.treexf.DefaultMatch;
import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.Transformation;
import com.top_logic.basic.treexf.TreeTransformer;
import com.top_logic.basic.treexf.Value;
import com.top_logic.model.search.expr.interpreter.transform.Transformations;

/**
 * Test case for {@link Transformations}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTransformations extends TestCase {

	public void testAndTrue() {
		Node expr = and(literal(true), var("x"));

		Node result = transform(expr, Transformations.andTrue1());

		assertExpression(var("x"), result);
	}

	public void testInline() {
		Value x = value("$x");
		Node expr =
			call(
				lambda(x,
					isEqual(var(x), var("y"))), var("z"));

		Node result = transform(expr, Transformations.inlineCalls());

		assertExpression(isEqual(var("z"), var("y")), result);
	}

	public void testNewId() {
		Node expr = filter(
			var("$s"),
			lambda("$x",
				and(
					isEqual(var("$x"), var("e1")),
					isEqual(var("$x"), var("e2")))));

		Node result = transform(expr, Transformations.splitAndFilter());

		assertExpression(
			filter(
				filter(
					var("$s"),
					lambda("$x",
						isEqual(var("$x"), var("e1")))),
				lambda("y0",
					isEqual(var("y0"), var("e2")))),
			result);
	}

	public void testFilterAnd() {
		Node expr =
			filter(
				all("tl5.structure.demoTypes.A"),
				lambda(
					value("T1"),
					and(containsElement(
						all("tl5.interface.xref.DemoTypes"), var("T1")),
						isEqual(access(var("T1"), "typedWrapper"),
							var("T1")))));

		Node result = transform(expr, Transformations.splitAndFilter());

		assertExpression(
			filter(
				filter(
					all("tl5.structure.demoTypes.A"),
					lambda(
						value("T1"),
						containsElement(all("tl5.interface.xref.DemoTypes"), var("T1"))
					)),
				lambda(
					value("y0"),
					isEqual(access(var("y0"), "typedWrapper"),
						var("y0")))
			),
			result);

	}

	public void testFilterWithEquals() {
		Node expr = filter(
			all(type("A")),
			lambda("x",
				isEqual(var("x"), access(var("y"), "ref"))));

		Node result = transform(expr, Transformations.filterWithIsEqual1());
		System.out.println(result);

		assertExpression(
			ifElse(
				containsElement(
					all(type("A")),
					access(var("y"), "ref")),
				singleton(access(var("y"), "ref")),
				literalEmptySet()),
			result);
	}

	public void testFilterWithEqualsNegative() {
		Node expr = filter(
			all(type("A")),
			lambda("x",
				isEqual(var("x"), access(var("x"), "ref"))));

		Node result = transform(expr, Transformations.filterWithIsEqual1());
		System.out.println(result);

		assertExpression(
			filter(
				all(type("A")),
				lambda("x",
					isEqual(var("x"), access(var("x"), "ref")))),
			result);
	}

	public void testSingleElement() {
		Node expr = access(singleElement(singleton(var("x"))), "ref");
		Node result = transform(expr, Transformations.TRANSFORMATIONS);

		assertExpression(
			access(var("x"), "ref"),
			result);
	}

	public void testTransform() {
		Node expr = filter(
			all(type("tl5.structure.demoTypes.A")),
				lambda("T1",
				and(
					literal(true),
						let(
						"$0", access(var("T1"), part("typedWrapper")),
						containsElement(
							filter(
								all(type("tl5.interface.xref.DemoTypes")), 
								lambda("D2", and(literal(true),
									isEqual(var("D2"), var("T1"))))),
								var("$0"))))));

		Node result = transform(expr, Transformations.TRANSFORMATIONS);
		System.out.println(result);

		assertExpression(
			filter(
				filter(
					all("tl5.structure.demoTypes.A"),
					lambda("T1",
						instanceOf("tl5.interface.xref.DemoTypes", var("T1")))),
				lambda("y0",
					isEqual(access(var("y0"), "typedWrapper"), var("y0")))),
			result);
	}

	private Node transform(Node expr, Transformation... transformations) {
		return new TreeTransformer(transformations).transform(matcher(), expr);
	}

	private DefaultMatch matcher() {
		return new DefaultMatch();
	}

	private void assertExpression(Node expected, Node actual) {
		assertEquals(expected.toString(), actual.toString());
	}

	private String part(String name) {
		return name;
	}

	private String type(String name) {
		return name;
	}

}
