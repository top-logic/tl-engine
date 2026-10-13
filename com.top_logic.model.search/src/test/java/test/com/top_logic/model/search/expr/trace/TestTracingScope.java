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
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.util.Pointer;

/**
 * Test for {@link TracingScope} with overlays that read unchanged values from their base object and
 * for nested scopes.
 */
@SuppressWarnings("javadoc")
public class TestTracingScope extends AbstractScriptTracerTest {

	public void testOverlayReadingBaseDirectly() {
		TLObject f1 = newF("f1", 3.0, null);
		TLObject overlay = new BaseOverlay(f1, false);

		List<Pointer> trace = new ArrayList<>();
		try (TracingScope scope = TracingScope.open(trace::add, overlays(Map.of(f1, overlay)))) {
			assertEquals(3.0, f1.tValue(part(BASE)));
		}
		assertEquals(1, trace.size());
		assertTrace(trace, f1, BASE);
	}

	public void testOverlayReadingBaseGuarded() {
		TLObject f1 = newF("f1", 3.0, null);
		BaseOverlay overlay = new BaseOverlay(f1, true);

		List<Pointer> trace = new ArrayList<>();
		try (TracingScope scope = TracingScope.open(trace::add, overlays(Map.of(f1, overlay)))) {
			assertEquals("Unchanged value read from the base object.", 3.0, f1.tValue(part(BASE)));
			assertEquals("Direct read of the overlay.", 3.0, overlay.tValue(part(BASE)));
		}
		assertEquals(1, trace.size());
		assertTrace(trace, f1, BASE);
	}

	public void testOverlayComputingDerivedOnBase() {
		TLObject f1 = newF("f1", 3.0, null);
		BaseOverlay overlay = new BaseOverlay(f1, true);
		overlay.change(part(BASE), 10.0);

		List<Pointer> trace = new ArrayList<>();
		try (TracingScope scope = TracingScope.open(trace::add, overlays(Map.of(f1, overlay)))) {
			assertEquals(10.0, f1.tValue(part(BASE)));
			assertEquals(20.0, f1.tValue(part(DOUBLED)));
			assertEquals(21.0, f1.tValue(part(INCREMENTED)));
		}
		assertTrace(trace, f1, BASE, f1, DOUBLED, f1, INCREMENTED);

		trace.clear();
		try (TracingScope scope = TracingScope.open(trace::add, overlays(Map.of(f1, overlay)))) {
			assertEquals("Direct read of the overlay.", 20.0, overlay.tValue(part(DOUBLED)));
		}
		assertTrace(trace, f1, BASE);

		assertEquals("Outside of a scope, the base object is read plainly.", 6.0, f1.tValue(part(DOUBLED)));
		assertEquals("Outside of a scope, the overlay computes on its own values.", 20.0,
			overlay.tValue(part(DOUBLED)));
		assertNull(TracingScope.current());
	}

	public void testNestedScopes() {
		TLObject f1 = newF("f1", 3.0, null);
		TLObject overlay = overlay(10.0, null);

		assertNull(TracingScope.current());

		List<Pointer> outerTrace = new ArrayList<>();
		List<Pointer> innerTrace = new ArrayList<>();
		try (TracingScope outer = TracingScope.open(outerTrace::add, overlays(Map.of(f1, overlay)))) {
			assertSame(outer, TracingScope.current());
			assertEquals(10.0, f1.tValue(part(BASE)));

			try (TracingScope inner = TracingScope.open(innerTrace::add, null)) {
				assertSame(inner, TracingScope.current());
				assertEquals("Inner scope has no overlays.", 3.0, f1.tValue(part(BASE)));
				assertEquals(6.0, f1.tValue(part(DOUBLED)));
			}

			assertSame(outer, TracingScope.current());
			assertEquals(20.0, f1.tValue(part(DOUBLED)));
		}
		assertNull(TracingScope.current());

		assertTrace(innerTrace, f1, BASE, f1, DOUBLED);
		assertTrace(outerTrace, f1, BASE, f1, DOUBLED, overlay, BASE);
	}

	public void testNoRecordingScope() {
		TLObject f1 = newF("f1", 3.0, null);
		TLObject overlay = overlay(10.0, null);

		List<Pointer> trace = new ArrayList<>();
		try (TracingScope outer = TracingScope.open(trace::add, null)) {
			try (TracingScope inner = TracingScope.open(null, overlays(Map.of(f1, overlay)))) {
				assertEquals(20.0, f1.tValue(part(DOUBLED)));
			}
		}
		assertTrace(trace);
	}

	/**
	 * Overlay of a base object holding changed values and reading all other values from its base
	 * object.
	 */
	private static class BaseOverlay extends TransientObject {

		private final TLObject _base;

		private final boolean _guarded;

		private final Map<TLStructuredTypePart, Object> _changes = new HashMap<>();

		/**
		 * Creates a {@link BaseOverlay}.
		 *
		 * @param guarded
		 *        Whether the base object is read through
		 *        {@link TracingScope#readBase(TLObject, TLStructuredTypePart, java.util.function.BiFunction)}.
		 */
		BaseOverlay(TLObject base, boolean guarded) {
			_base = base;
			_guarded = guarded;
		}

		void change(TLStructuredTypePart part, Object value) {
			_changes.put(part, value);
		}

		@Override
		public TLStructuredType tType() {
			return _base.tType();
		}

		@Override
		public Object tValue(TLStructuredTypePart part) {
			if (_changes.containsKey(part)) {
				return _changes.get(part);
			}
			if (!_guarded) {
				return _base.tValue(part);
			}
			if (part.isDerived()) {
				return TracingScope.computeOnOverlay(_base, this, part, TLObject::tValue);
			}
			return TracingScope.readBase(_base, part, TLObject::tValue);
		}
	}

	public static Test suite() {
		return suite(TestTracingScope.class, PersonManager.Module.INSTANCE);
	}

}
