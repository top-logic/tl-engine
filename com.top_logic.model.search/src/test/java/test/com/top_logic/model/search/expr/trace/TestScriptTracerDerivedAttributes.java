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

import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.util.Pointer;

/**
 * Test for {@link ScriptTracer} reading derived attributes computed by a script.
 *
 * <p>
 * The reads of the derived attribute's computation are traced in the {@link TracingScope} of the
 * evaluation and resolved through its overlays.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestScriptTracerDerivedAttributes extends AbstractScriptTracerTest {

	public void testStoredAttributeReportsOwnPointer() throws ParseException {
		TLObject f1 = newF("f1", 3.0, null);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace(access(BASE), trace, null, f1);

		assertEquals(3.0, result);
		assertTrace(trace, f1, BASE);
	}

	public void testDerivedChain() throws ParseException {
		TLObject f1 = newF("f1", 3.0, null);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace(access(INCREMENTED), trace, null, f1);

		assertEquals(7.0, result);
		assertTrace(trace, f1, INCREMENTED, f1, DOUBLED, f1, BASE);
	}

	public void testDerivedChainDynamicGet() throws ParseException {
		TLObject f1 = newF("f1", 3.0, null);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace("x -> p -> $x.get($p)", trace, null, f1, part(INCREMENTED));

		assertEquals(7.0, result);
		assertTrace(trace, f1, INCREMENTED, f1, DOUBLED, f1, BASE);
	}

	public void testDerivedNavigation() throws ParseException {
		TLObject f2 = newF("f2", 5.0, null);
		TLObject f1 = newF("f1", 3.0, f2);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace(access(OTHER_DOUBLED), trace, null, f1);

		assertEquals(10.0, result);
		assertTrace(trace, f1, OTHER_DOUBLED, f1, OTHER, f2, DOUBLED, f2, BASE);
	}

	public void testDerivedChainOverOverlay() throws ParseException {
		TLObject f1 = newF("f1", 3.0, null);
		TLObject overlay = overlay(10.0, null);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace(access(INCREMENTED), trace, overlays(Map.of(f1, overlay)), f1);

		assertEquals(21.0, result);
		assertTrace(trace, f1, INCREMENTED, overlay, DOUBLED, overlay, BASE);
	}

	public void testDerivedNavigationOverOverlay() throws ParseException {
		TLObject f2 = newF("f2", 5.0, null);
		TLObject f3 = newF("f3", 7.0, null);
		TLObject f1 = newF("f1", 3.0, f2);

		// The edited reference of f1 points to f3, whose edited base value is 8.
		TLObject overlay1 = overlay(3.0, f3);
		TLObject overlay3 = overlay(8.0, null);
		Map<TLObject, TLObject> overlayByBase = new HashMap<>();
		overlayByBase.put(f1, overlay1);
		overlayByBase.put(f3, overlay3);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace(access(OTHER_DOUBLED), trace, overlays(overlayByBase), f1);

		assertEquals(16.0, result);
		assertTrace(trace, f1, OTHER_DOUBLED, overlay1, OTHER, f3, DOUBLED, overlay3, BASE);
	}

	public void testStoredAttributeOverOverlay() throws ParseException {
		TLObject f1 = newF("f1", 3.0, null);
		TLObject overlay = overlay(10.0, null);

		List<Pointer> trace = new ArrayList<>();
		Object result = trace(access(BASE), trace, overlays(Map.of(f1, overlay)), f1);

		assertEquals(10.0, result);
		assertTrace(trace, f1, BASE);
	}

	public static Test suite() {
		return suite(TestScriptTracerDerivedAttributes.class, PersonManager.Module.INSTANCE);
	}

}
