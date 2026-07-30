/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;

/**
 * Lean overlay over a persistent {@link TLObject} that intercepts writes.
 *
 * <p>
 * Unchanged attributes delegate to the base object. Changed attributes are stored in a local map.
 * This serves as the transient editing buffer for the form system.
 * </p>
 */
public class TLObjectOverlay extends TransientObject {

	private final TLObject _base;

	private final Map<TLStructuredTypePart, Object> _changes = new LinkedHashMap<>();

	/**
	 * Creates a new overlay over the given base object.
	 *
	 * @param base
	 *        The persistent object to overlay. Must not be {@code null}.
	 */
	public TLObjectOverlay(TLObject base) {
		_base = base;
	}

	@Override
	public TLStructuredType tType() {
		return _base.tType();
	}

	@Override
	public TLObject tContainer() {
		return _base.tContainer();
	}

	@Override
	public TLReference tContainerReference() {
		return _base.tContainerReference();
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		if (_changes.containsKey(part)) {
			return _changes.get(part);
		}
		return _base.tValue(part);
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		_changes.put(part, value);
	}

	/**
	 * Whether any attribute holds a value that differs from the base object's value.
	 *
	 * <p>
	 * A stored value equal to the base value does not count as a change: writing back an unchanged
	 * value (e.g. a composition table registering its unmodified row list on edit-mode entry, or an
	 * edit that is typed and reverted) leaves the overlay clean.
	 * </p>
	 *
	 * <p>
	 * An overlay stands for the object it wraps, so a reference value that only replaces objects by
	 * their overlays is no change either. Edits <em>within</em> a referenced object are tracked by
	 * that object's own overlay, not by the reference holding it.
	 * </p>
	 */
	public boolean isDirty() {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			if (isChange(entry.getValue(), _base.tValue(entry.getKey()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the stored value differs from the base value, comparing overlays by the objects they
	 * stand for.
	 */
	private static boolean isChange(Object stored, Object baseValue) {
		if (stored instanceof Collection<?> storedValues && baseValue instanceof Collection<?> baseValues) {
			return !resolveAll(storedValues).equals(resolveAll(baseValues));
		}
		return !Objects.equals(resolve(stored), resolve(baseValue));
	}

	private static List<Object> resolveAll(Collection<?> values) {
		List<Object> result = new ArrayList<>(values.size());
		for (Object value : values) {
			result.add(resolve(value));
		}
		return result;
	}

	/**
	 * The object an overlay stands for; any other value unchanged.
	 */
	private static Object resolve(Object value) {
		return value instanceof TLObjectOverlay overlay ? overlay.getBase() : value;
	}

	/**
	 * Whether the given attribute has been changed in this overlay.
	 */
	public boolean isChanged(TLStructuredTypePart part) {
		return _changes.containsKey(part);
	}

	/**
	 * Transfers all accumulated changes to the base object.
	 */
	public void apply() {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			_base.tUpdate(entry.getKey(), entry.getValue());
		}
		// The base now holds all values, so the overlay has no unsaved changes anymore. Reads
		// delegate to the base, and dirty tracking reports a clean state.
		_changes.clear();
	}

	/**
	 * Discards all accumulated changes. After reset, all reads delegate to the base object again.
	 */
	public void reset() {
		_changes.clear();
	}

	/**
	 * The base object this overlay wraps.
	 */
	public TLObject getBase() {
		return _base;
	}
}
