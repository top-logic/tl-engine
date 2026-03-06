/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.model.TLObject;
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
	 * Whether any attribute has been changed.
	 */
	public boolean isDirty() {
		return !_changes.isEmpty();
	}

	/**
	 * Whether the given attribute has been changed in this overlay.
	 */
	public boolean isChanged(TLStructuredTypePart part) {
		return _changes.containsKey(part);
	}

	/**
	 * Transfers all accumulated changes to the given target object.
	 *
	 * @param target
	 *        The object to apply changes to. Typically the original base object within a KB
	 *        transaction.
	 */
	public void applyTo(TLObject target) {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			target.tUpdate(entry.getKey(), entry.getValue());
		}
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
