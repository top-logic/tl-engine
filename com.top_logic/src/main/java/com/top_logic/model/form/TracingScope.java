/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.Sink;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.Pointer;

/**
 * Thread-bound scope in which every model read of an attribute value is traced and resolved
 * through form overlays.
 *
 * <p>
 * While a {@link TracingScope} is open on the current thread, each read of an attribute value of an
 * application object (a script access, a {@link TLObject#tValue(TLStructuredTypePart)} call from
 * Java code, a generated getter) is
 * </p>
 * <ul>
 * <li>reported as {@link Pointer} to the scope's {@link Sink}, and</li>
 * <li>answered from the overlay of the object, if the scope's {@link OverlayLookup} has an overlay
 * for it.</li>
 * </ul>
 *
 * <p>
 * This allows to evaluate a computation (e.g. the expression of a derived attribute) on the values
 * entered in a form, and to observe all values the computation depends on.
 * </p>
 *
 * <p>
 * A scope is opened with {@link #open(Sink, OverlayLookup)} in a try-with-resources statement:
 * </p>
 *
 * <pre>
 * try (TracingScope scope = TracingScope.open(trace, overlays)) {
 *     ...
 * }
 * </pre>
 *
 * <p>
 * Scopes nest: opening a scope while another one is open on the same thread makes the new scope
 * the current one, closing it restores the outer one.
 * </p>
 *
 * <p>
 * An implementation of a model read calls {@link #current()} and, if a scope is open,
 * {@link #read(TLObject, TLStructuredTypePart)} before performing its plain read. Outside of a scope
 * the check costs a single thread-local lookup.
 * </p>
 *
 * <p>
 * An overlay that reads the value of its base object must do this through
 * {@link #readBase(TLObject, TLStructuredTypePart)}. Otherwise, the base read would be
 * redirected to the overlay again. A read of an attribute of an object that is currently redirected
 * to its overlay is served plainly by the object itself (without being reported) for the same
 * reason.
 * </p>
 */
public final class TracingScope implements AutoCloseable {

	/**
	 * Result of {@link #read(TLObject, TLStructuredTypePart)} signaling that the caller must perform
	 * its plain read.
	 */
	public static final Object PLAIN = new Object() {
		@Override
		public String toString() {
			return "plain";
		}
	};

	private static final ThreadLocal<TracingScope> CURRENT = new ThreadLocal<>();

	private final TracingScope _outer;

	private final Sink<Pointer> _trace;

	private final OverlayLookup _overlays;

	/**
	 * The attribute reads that are currently redirected to an overlay, or served by an overlay from
	 * its base object.
	 *
	 * <p>
	 * Shared by all nested scopes of a thread: a read of one of these attributes is served plainly,
	 * no matter in which nested scope it happens.
	 * </p>
	 */
	private final Set<Pointer> _guard;

	private TracingScope(TracingScope outer, Sink<Pointer> trace, OverlayLookup overlays) {
		_outer = outer;
		_trace = trace;
		_overlays = overlays;
		_guard = outer == null ? new HashSet<>() : outer._guard;
	}

	/**
	 * Opens a {@link TracingScope} on the current thread.
	 *
	 * @param trace
	 *        The {@link Sink} to report each attribute read to, or <code>null</code> to not record
	 *        reads.
	 * @param overlays
	 *        The {@link OverlayLookup} to resolve the overlays of read objects from, or
	 *        <code>null</code> if reads are not redirected to overlays.
	 * @return The opened scope that must be {@link #close() closed} by the caller.
	 */
	public static TracingScope open(Sink<Pointer> trace, OverlayLookup overlays) {
		TracingScope scope = new TracingScope(CURRENT.get(), trace, overlays);
		CURRENT.set(scope);
		return scope;
	}

	/**
	 * The {@link TracingScope} currently open on this thread, or <code>null</code> if there is none.
	 */
	public static TracingScope current() {
		return CURRENT.get();
	}

	/**
	 * The {@link Sink} reads are reported to, or <code>null</code> if reads are not recorded.
	 */
	public Sink<Pointer> getTrace() {
		return _trace;
	}

	/**
	 * The {@link OverlayLookup} reads are resolved with, or <code>null</code> if reads are not
	 * redirected to overlays.
	 */
	public OverlayLookup getOverlays() {
		return _overlays;
	}

	/**
	 * Traces the read of an attribute value of an object in this scope.
	 *
	 * <p>
	 * Reports the read to the {@link #getTrace() trace}. If the {@link #getOverlays() overlays}
	 * contain an overlay for the given object that is different from the object itself, the value
	 * is read from the overlay. While the overlay is read, the read of the same attribute of the
	 * overlay itself is not reported again, and a read of the same attribute of the given object is
	 * served plainly.
	 * </p>
	 *
	 * @param object
	 *        The object whose attribute is read.
	 * @param part
	 *        The attribute that is read.
	 * @return The value read from the overlay, or {@link #PLAIN}, if the caller must perform its
	 *         plain read.
	 */
	public Object read(TLObject object, TLStructuredTypePart part) {
		Pointer pointer = Pointer.create(object, part);
		if (_guard.contains(pointer)) {
			return PLAIN;
		}
		if (_trace != null) {
			_trace.add(pointer);
		}
		if (_overlays == null) {
			return PLAIN;
		}
		TLObject overlay = _overlays.getExistingOverlay(object);
		if (overlay == null || overlay == object) {
			return PLAIN;
		}
		Pointer overlayPointer = Pointer.create(overlay, part);
		boolean overlayGuarded = _guard.add(overlayPointer);
		_guard.add(pointer);
		try {
			return overlay.tValue(part);
		} finally {
			_guard.remove(pointer);
			if (overlayGuarded) {
				_guard.remove(overlayPointer);
			}
		}
	}

	/**
	 * Reports the read of an attribute value of an object in this scope to the
	 * {@link #getTrace() trace} without redirecting it to an overlay.
	 *
	 * <p>
	 * Used by objects that are read directly, even if an overlay exists for them.
	 * </p>
	 *
	 * @param object
	 *        The object whose attribute is read.
	 * @param part
	 *        The attribute that is read.
	 */
	public void record(TLObject object, TLStructuredTypePart part) {
		if (_trace == null) {
			return;
		}
		Pointer pointer = Pointer.create(object, part);
		if (_guard.contains(pointer)) {
			return;
		}
		_trace.add(pointer);
	}

	/**
	 * Reads an attribute value of the base object of an overlay.
	 *
	 * <p>
	 * The {@link TLObject#tValue(TLStructuredTypePart) read} is performed plainly by the base object:
	 * neither reported, nor redirected to an overlay of the base object.
	 * </p>
	 *
	 * @param base
	 *        The object the overlay is based on.
	 * @param part
	 *        The attribute to read.
	 * @return The value of the given attribute of the base object.
	 */
	public static Object readBase(TLObject base, TLStructuredTypePart part) {
		TracingScope current = CURRENT.get();
		if (current == null) {
			return base.tValue(part);
		}
		Pointer pointer = Pointer.create(base, part);
		if (!current._guard.add(pointer)) {
			return base.tValue(part);
		}
		try {
			return base.tValue(part);
		} finally {
			current._guard.remove(pointer);
		}
	}

	/**
	 * Computes a (derived) attribute value of the base object of an overlay on the values of the
	 * overlay.
	 *
	 * <p>
	 * The computation runs in a nested scope that redirects reads of the base object to the given
	 * overlay and resolves all other objects through the overlays of the current scope. Reads of the
	 * computation are reported to the trace of the current scope. The read of the computed attribute
	 * of the base object itself is served plainly.
	 * </p>
	 *
	 * @param base
	 *        The object the overlay is based on.
	 * @param overlay
	 *        The overlay of the base object.
	 * @param part
	 *        The attribute to compute by {@link TLObject#tValue(TLStructuredTypePart) reading} it
	 *        from the base object.
	 * @return The attribute value computed on the values of the overlay.
	 */
	public static Object computeOnOverlay(TLObject base, TLObject overlay, TLStructuredTypePart part) {
		TracingScope outer = CURRENT.get();
		Sink<Pointer> trace = outer == null ? null : outer._trace;
		OverlayLookup outerOverlays = outer == null ? null : outer._overlays;
		try (TracingScope scope = open(trace, new BaseOverlay(base, overlay, outerOverlays))) {
			return readBase(base, part);
		}
	}

	/**
	 * Closes this scope and makes the scope current again that was current when this scope was
	 * opened.
	 */
	@Override
	public void close() {
		if (CURRENT.get() != this) {
			throw new IllegalStateException("Tracing scopes not closed in reverse opening order.");
		}
		if (_outer == null) {
			CURRENT.remove();
		} else {
			CURRENT.set(_outer);
		}
	}

	/**
	 * {@link OverlayLookup} resolving a single base object to its overlay and all other objects
	 * through an optional outer lookup.
	 */
	private static final class BaseOverlay implements OverlayLookup {

		private final TLObject _base;

		private final TLObject _overlay;

		private final OverlayLookup _outer;

		BaseOverlay(TLObject base, TLObject overlay, OverlayLookup outer) {
			_base = base;
			_overlay = overlay;
			_outer = outer;
		}

		@Override
		public TLObject getExistingOverlay(TLObject object) {
			if (object == _base) {
				return _overlay;
			}
			return _outer == null ? null : _outer.getExistingOverlay(object);
		}

		@Override
		public Iterable<? extends TLObject> getOverlays() {
			if (_outer == null) {
				return List.of(_overlay);
			}
			Set<TLObject> result = new LinkedHashSet<>();
			result.add(_overlay);
			for (TLObject overlay : _outer.getOverlays()) {
				result.add(overlay);
			}
			return result;
		}
	}

}
