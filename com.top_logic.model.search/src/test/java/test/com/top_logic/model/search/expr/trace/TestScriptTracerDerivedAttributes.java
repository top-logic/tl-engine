/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracedValueComputation;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.search.expr.trace.TracingAccessRewriter;
import com.top_logic.model.util.Pointer;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link ScriptTracer} reading attributes whose storage is a
 * {@link TracedValueComputation}.
 *
 * @see TracingAccessRewriter#lookupTraced(com.top_logic.model.search.expr.EvalContext, TLObject,
 *      TLStructuredTypePart, boolean)
 */
@SuppressWarnings("javadoc")
public class TestScriptTracerDerivedAttributes extends AbstractSearchExpressionTest {

	private static final String TYPE_F = "TestSearchExpression:F";

	private static final String NAME = "name";

	private static final String BASE = "base";

	private static final String OTHER = "other";

	private static final String DOUBLED = "doubled";

	private static final String INCREMENTED = "incremented";

	private static final String OTHER_DOUBLED = "otherDoubled";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		becomeUser(PersonManager.getManager().getRoot());
	}

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

	private Object trace(String script, List<Pointer> trace, OverlayLookup overlays, Object... args)
			throws ParseException {
		ScriptTracer tracer = ScriptTracer.compile(model(), parse(script));
		return tracer.execute(trace::add, overlays, args);
	}

	private static String access(String attribute) {
		return "x -> $x.get(`" + TYPE_F + "#" + attribute + "`)";
	}

	private TLObject newF(String name, double base, TLObject other) {
		TLObject result = newObject(TYPE_F, name);
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			result.tUpdate(part(BASE), base);
			result.tUpdate(part(OTHER), other);
			tx.commit();
		}
		return result;
	}

	private static TLObject overlay(double base, TLObject other) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type());
		result.tUpdate(part(NAME), "overlay");
		result.tUpdate(part(BASE), base);
		result.tUpdate(part(OTHER), other);
		return result;
	}

	private static OverlayLookup overlays(Map<TLObject, TLObject> overlayByBase) {
		return new OverlayLookup() {
			@Override
			public TLObject getExistingOverlay(TLObject object) {
				if (overlayByBase.containsValue(object)) {
					return object;
				}
				return overlayByBase.get(object);
			}

			@Override
			public Iterable<? extends TLObject> getOverlays() {
				return overlayByBase.values();
			}
		};
	}

	private static TLClass type() {
		return (TLClass) TLModelUtil.findType(TYPE_F);
	}

	private static TLStructuredTypePart part(String name) {
		return type().getPartOrFail(name);
	}

	/**
	 * Asserts that the given trace consists of exactly the given pointers.
	 *
	 * @param expected
	 *        Alternating objects and attribute names.
	 */
	private static void assertTrace(List<Pointer> trace, Object... expected) {
		Set<List<Object>> expectedPointers = new HashSet<>();
		for (int n = 0; n < expected.length; n += 2) {
			expectedPointers.add(Arrays.asList(expected[n], part((String) expected[n + 1])));
		}
		Set<List<Object>> actualPointers = new HashSet<>();
		for (Pointer pointer : trace) {
			actualPointers.add(Arrays.asList(pointer.object(), pointer.attribute()));
		}
		assertEquals(expectedPointers, actualPointers);
	}

	public static Test suite() {
		return suite(TestScriptTracerDerivedAttributes.class, PersonManager.Module.INSTANCE);
	}

}
