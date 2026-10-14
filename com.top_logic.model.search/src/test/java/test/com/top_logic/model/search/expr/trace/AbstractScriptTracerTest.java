/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.trace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.util.Pointer;
import com.top_logic.model.util.TLModelUtil;

/**
 * Base class for tests of {@link ScriptTracer} evaluations over objects of the test type
 * {@value #TYPE_F}, optionally with edited values provided by an {@link OverlayLookup}.
 */
public abstract class AbstractScriptTracerTest extends AbstractSearchExpressionTest {

	/**
	 * Name of the test type.
	 */
	protected static final String TYPE_F = "TestSearchExpression:F";

	/**
	 * Name of the name attribute of {@link #TYPE_F}.
	 */
	protected static final String NAME = "name";

	/**
	 * Name of the stored number attribute of {@link #TYPE_F}.
	 */
	protected static final String BASE = "base";

	/**
	 * Name of the stored reference of {@link #TYPE_F} to another {@link #TYPE_F}.
	 */
	protected static final String OTHER = "other";

	/**
	 * Name of the derived attribute of {@link #TYPE_F} doubling {@link #BASE}.
	 */
	protected static final String DOUBLED = "doubled";

	/**
	 * Name of the derived attribute of {@link #TYPE_F} incrementing {@link #DOUBLED}.
	 */
	protected static final String INCREMENTED = "incremented";

	/**
	 * Name of the derived attribute of {@link #TYPE_F} reading {@link #DOUBLED} of {@link #OTHER}.
	 */
	protected static final String OTHER_DOUBLED = "otherDoubled";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		becomeUser(PersonManager.getManager().getRoot());
	}

	/**
	 * Evaluates the given script with a {@link ScriptTracer}.
	 *
	 * @param script
	 *        The script to evaluate.
	 * @param trace
	 *        The list to add the reported accesses to.
	 * @param overlays
	 *        The edited values, <code>null</code> for none.
	 * @param args
	 *        The arguments to the script.
	 * @return The result of the script.
	 */
	protected Object trace(String script, List<Pointer> trace, OverlayLookup overlays, Object... args)
			throws ParseException {
		ScriptTracer tracer = ScriptTracer.compile(model(), parse(script));
		return tracer.execute(trace::add, overlays, args);
	}

	/**
	 * A script function reading the given attribute of its argument.
	 */
	protected static String access(String attribute) {
		return "x -> $x.get(`" + TYPE_F + "#" + attribute + "`)";
	}

	/**
	 * Creates a persistent {@link #TYPE_F} object.
	 */
	protected TLObject newF(String name, double base, TLObject other) {
		TLObject result = newObject(TYPE_F, name);
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			result.tUpdate(part(BASE), base);
			result.tUpdate(part(OTHER), other);
			tx.commit();
		}
		return result;
	}

	/**
	 * Creates a transient {@link #TYPE_F} object holding edited values.
	 */
	protected static TLObject overlay(double base, TLObject other) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type());
		result.tUpdate(part(NAME), "overlay");
		result.tUpdate(part(BASE), base);
		result.tUpdate(part(OTHER), other);
		return result;
	}

	/**
	 * An {@link OverlayLookup} with the given overlays.
	 */
	protected static OverlayLookup overlays(Map<TLObject, TLObject> overlayByBase) {
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

	/**
	 * The test type {@link #TYPE_F}.
	 */
	protected static TLClass type() {
		return (TLClass) TLModelUtil.findType(TYPE_F);
	}

	/**
	 * The attribute of {@link #TYPE_F} with the given name.
	 */
	protected static TLStructuredTypePart part(String name) {
		return type().getPartOrFail(name);
	}

	/**
	 * Asserts that the given trace consists of exactly the given pointers.
	 *
	 * @param expected
	 *        Alternating objects and attribute names.
	 */
	protected static void assertTrace(List<Pointer> trace, Object... expected) {
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

}
