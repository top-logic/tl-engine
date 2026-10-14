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

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.util.Pointer;

/**
 * Test for {@link ScriptTracer} evaluating functions implemented in Java that read attributes
 * through {@link TLObject#tValue(TLStructuredTypePart)} and {@link TLObject#tValueByName(String)}.
 *
 * @see OtherValueMethod
 * @see OtherValueFunctions
 */
@SuppressWarnings("javadoc")
public class TestScriptTracerJavaFunctions extends AbstractScriptTracerTest {

	private static final String GENERIC = "testOtherValueMethod";

	private static final String STATIC = OtherValueFunctions.PREFIX + "Value";

	private static final String STATIC_BY_NAME = OtherValueFunctions.PREFIX + "ValueByName";

	private TLObject _f1;

	private TLObject _f2;

	private TLObject _f3;

	private TLObject _overlay3;

	private OverlayLookup _overlays;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_f2 = newF("f2", 5.0, null);
		_f3 = newF("f3", 7.0, null);
		_f1 = newF("f1", 3.0, _f2);

		// The edited reference of f1 points to f3, whose edited base value is 8.
		TLObject overlay1 = overlay(3.0, _f3);
		_overlay3 = overlay(8.0, null);
		Map<TLObject, TLObject> overlayByBase = new HashMap<>();
		overlayByBase.put(_f1, overlay1);
		overlayByBase.put(_f3, _overlay3);
		_overlays = overlays(overlayByBase);
	}

	public void testGenericMethod() throws ParseException {
		assertTraced(GENERIC);
	}

	public void testStaticFunction() throws ParseException {
		assertTraced(STATIC);
	}

	public void testStaticFunctionByName() throws ParseException {
		assertTraced(STATIC_BY_NAME);
	}

	public void testStaticFunctionWithoutTracing() throws ParseException {
		assertEquals(5.0, eval(call(STATIC, BASE), _f1));
		assertEquals(10.0, eval(call(STATIC_BY_NAME, DOUBLED), _f1));
		assertEquals(5.0, eval("x -> $x." + STATIC + "('" + BASE + "')", _f1));
		assertEquals(5.0, eval("x -> " + STATIC + "(attribute: '" + BASE + "', self: $x)", _f1));
	}

	public void testGenericMethodWithoutTracing() throws ParseException {
		assertEquals(5.0, eval(call(GENERIC, BASE), _f1));
		assertEquals(10.0, eval(call(GENERIC, DOUBLED), _f1));
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

	private static String call(String function, String attribute) {
		return "x -> " + function + "($x, '" + attribute + "')";
	}

	public static Test suite() {
		return suite(TestScriptTracerJavaFunctions.class);
	}

}
