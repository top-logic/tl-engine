/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.interpreter.transform;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.SearchExpressions.*;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.TreeMaterializer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.interpreter.transform.TreeBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test case for {@link TreeBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTreeBuilder extends TestCase {

	public void testRoundTrip1() {
		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "module");
		TLClass A = TLModelUtil.addClass(module, "A");
		TLStructuredTypePart foo = TLModelUtil.addProperty(A, "foo", TLCore.getPrimitiveType(model, Kind.INT));
		TLStructuredTypePart bar = TLModelUtil.addProperty(A, "bar", TLCore.getPrimitiveType(model, Kind.STRING));

		SearchExpression expr = foreach(filter(all(A), lambda("x", isEqual(literal(42), access(var("x"), foo)))),
			lambda("y", access(var("y"), bar)));

		assertRoundTrip(expr);
	}

	public void testRoundTrip2() {
		SearchExpression expr = union(
			intersection(
				flatten(singleton(singleton(literal("Hello")))),
				singleton(literal("world"))),
			singleton(literal("!")));

		assertRoundTrip(expr);
	}

	public void testRoundTrip3() {
		SearchExpression expr = not(
			and(
				or(
					containsAll(var("x1"), var("x2")),
					containsElement(var("y1"), var("y2"))),
				containsSome(var("z1"), var("z2"))));

		assertRoundTrip(expr);
	}

	public void testRoundTrip4() {
		assertRoundTrip(let("x", var("x1"), ifElse(isEmpty(var("x")), var("y"), var("z"))));
	}

	public void testRoundTrip5() {
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "module");
		TLClass A = TLModelUtil.addClass(module, "A");
		TLClass B = TLModelUtil.addClass(module, "B");

		SearchExpression expr = filter(all(A), lambda("x", instanceOf(var("x"), B)));

		assertRoundTrip(expr);
	}

	public void testBlock() {
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "module");
		TLPrimitive T = TLModelUtil.addDatatype(module, module, "T", Kind.STRING);
		TLClass A = TLModelUtil.addClass(module, "A");
		TLClassProperty foo = TLModelUtil.addProperty(A, "foo", T);
		TLClassProperty bar = TLModelUtil.addProperty(A, "bar", T);
		TLClass B = TLModelUtil.addClass(module, "B");

		SearchExpression expr = foreach(
			filter(all(A), lambda("x", instanceOf(var("x"), B))),
			lambda("y",
				let("foovalue",
					access(var("y"), foo),
					block(
						update(var("y"), foo, access(var("y"), bar)),
						update(var("y"), bar, var("foovalue"))))));

		assertRoundTrip(expr);
	}

	private void assertRoundTrip(SearchExpression expr) {
		Node node = expr.visit(TreeBuilder.INSTANCE, null);

		TreeMaterializer treeMaterializer;
		try {
			treeMaterializer = TreeMaterializer.createTreeMaterializer(SearchExpressionFactory.class);
		} catch (IllegalArgumentException ex) {
			throw BasicTestCase
				.fail(
					"Ticket #11489: TreeMaterializer can not use factory classes with different methods with same return type.",
					ex);
		}
		Object result = treeMaterializer.materialize(node);

		assertEquals(expr.toString(), result.toString());
	}

}
