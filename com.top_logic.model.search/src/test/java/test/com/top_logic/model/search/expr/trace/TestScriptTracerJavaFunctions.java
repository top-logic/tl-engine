/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.search.ui.TLScriptDocumentation;
import com.top_logic.model.util.Pointer;

/**
 * Test for {@link ScriptTracer} evaluating functions implemented in Java that read attributes
 * through {@link EvalContext#access(TLObject, TLStructuredTypePart, boolean)}.
 *
 * @see OtherValueMethod
 * @see OtherValueFunctions
 */
@SuppressWarnings("javadoc")
public class TestScriptTracerJavaFunctions extends AbstractScriptTracerTest {

	private static final String GENERIC_TRACED = "testTracedOtherValue";

	private static final String GENERIC_PLAIN = "testPlainOtherValue";

	private static final String STATIC_TRACED = OtherValueFunctions.PREFIX + "Value";

	private static final String STATIC_TRACED_SECURED = OtherValueFunctions.PREFIX + "ValueSecured";

	private static final String STATIC_PLAIN = OtherValueFunctions.PREFIX + "ValuePlain";

	private TLObject _f1;

	private TLObject _f2;

	private TLObject _f3;

	private TLObject _overlay1;

	private TLObject _overlay3;

	private OverlayLookup _overlays;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_f2 = newF("f2", 5.0, null);
		_f3 = newF("f3", 7.0, null);
		_f1 = newF("f1", 3.0, _f2);

		// The edited reference of f1 points to f3, whose edited base value is 8.
		_overlay1 = overlay(3.0, _f3);
		_overlay3 = overlay(8.0, null);
		Map<TLObject, TLObject> overlayByBase = new HashMap<>();
		overlayByBase.put(_f1, _overlay1);
		overlayByBase.put(_f3, _overlay3);
		_overlays = overlays(overlayByBase);
	}

	public void testGenericMethodTraced() throws ParseException {
		assertTraced(GENERIC_TRACED);
	}

	public void testGenericMethodPlain() throws ParseException {
		assertNotTraced(GENERIC_PLAIN);
	}

	public void testStaticFunctionTraced() throws ParseException {
		assertTraced(STATIC_TRACED);
	}

	public void testStaticFunctionTracedContextLast() throws ParseException {
		assertTraced(STATIC_TRACED_SECURED);
	}

	public void testStaticFunctionPlain() throws ParseException {
		assertNotTraced(STATIC_PLAIN);
	}

	public void testStaticFunctionWithoutTracing() throws ParseException {
		assertEquals(5.0, eval(call(STATIC_TRACED, BASE), _f1));
		assertEquals(10.0, eval(call(STATIC_TRACED_SECURED, DOUBLED), _f1));
		assertEquals(5.0, eval("x -> $x." + STATIC_TRACED + "('" + BASE + "')", _f1));
		assertEquals(5.0, eval("x -> " + STATIC_TRACED + "(attribute: '" + BASE + "', self: $x)", _f1));
	}

	public void testGenericMethodWithoutTracing() throws ParseException {
		assertEquals(5.0, eval(call(GENERIC_TRACED, BASE), _f1));
		assertEquals(10.0, eval(call(GENERIC_TRACED, DOUBLED), _f1));
	}

	public void testContextIsNoScriptArgument() throws ParseException {
		for (String function : List.of(STATIC_TRACED, STATIC_TRACED_SECURED)) {
			MethodBuilder<?> builder = SearchBuilder.getInstance().getBuilder(function);
			assertNotNull("Function '" + function + "' not registered.", builder);
			ArgumentDescriptor descriptor = builder.descriptor();
			assertEquals(List.of("self", "attribute"), List.copyOf(descriptor.getArgumentNames()));
			assertEquals(2, descriptor.getMaxArgCnt());

			try {
				search("x -> " + function + "($x, '" + BASE + "', 'more')");
				fail("The evaluation context must not be passable as script argument.");
			} catch (Exception ex) {
				// Expected: too many arguments.
			}

			String doc =
				TLScriptDocumentation.documentation(DummyDisplayContext.newInstance(), function).orElse(null);
			assertNotNull("No documentation for '" + function + "'.", doc);
			assertTrue(doc, doc.contains("attribute"));
			assertFalse(doc, doc.contains(OtherValueFunctions.CONTEXT_PARAM));
		}
	}

	public void testNoCompileTimeEvaluation() throws ConfigurationException {
		TLScriptMethod.Builder builder = (TLScriptMethod.Builder) SearchBuilder.getInstance().getBuilder(STATIC_TRACED);
		TLScriptMethod call = builder.build(null, new SearchExpression[] {
			SearchExpressionFactory.literal(_f1),
			SearchExpressionFactory.literal(BASE) });
		assertFalse(call.canEvaluateAtCompileTime(new Object[] { _f1, BASE }));
	}

	private void assertTraced(String function) throws ParseException {
		List<Pointer> trace = new ArrayList<>();
		assertEquals(5.0, trace(call(function, BASE), trace, null, _f1));
		assertTrace(trace, _f1, OTHER, _f2, BASE);

		trace.clear();
		assertEquals("The edited values must be read.", 8.0, trace(call(function, BASE), trace, _overlays, _f1));
		assertTrace(trace, _f1, OTHER, _f3, BASE);

		trace.clear();
		assertEquals("A derived attribute must be computed over the edited values.", 16.0,
			trace(call(function, DOUBLED), trace, _overlays, _f1));
		assertTrace(trace, _f1, OTHER, _f3, DOUBLED, _overlay3, BASE);
	}

	private void assertNotTraced(String function) throws ParseException {
		List<Pointer> trace = new ArrayList<>();
		assertEquals(5.0, trace(call(function, BASE), trace, null, _f1));
		assertTrace(trace);

		trace.clear();
		assertEquals("A plain read ignores the edited values.", 5.0,
			trace(call(function, BASE), trace, _overlays, _f1));
		assertTrace(trace);
	}

	private static String call(String function, String attribute) {
		return "x -> " + function + "($x, '" + attribute + "')";
	}

	public static Test suite() {
		return suite(TestScriptTracerJavaFunctions.class, PersonManager.Module.INSTANCE);
	}

}
