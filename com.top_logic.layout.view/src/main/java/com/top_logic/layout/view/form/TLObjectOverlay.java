/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.layout.form.FormMember;
import com.top_logic.model.TLFormObjectBase;
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
public class TLObjectOverlay extends TransientObject implements TLFormObjectBase {

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
	 */
	public boolean isDirty() {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			if (!Objects.equals(entry.getValue(), _base.tValue(entry.getKey()))) {
				return true;
			}
		}
		return false;
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

	@Override
	public boolean isCreate() {
		return _base.tTransient();
	}

	@Override
	public TLObject getEditedObject() {
		return _base.tTransient() ? null : _base;
	}

	@Override
	public String getDomain() {
		return null;
	}

	@Override
	public Object getFieldValue(TLStructuredTypePart attribute) {
		return tValue(attribute);
	}

	@Override
	public FormMember getField(TLStructuredTypePart attribute) {
		return null;
	}

	@Override
	public Object getBaseValue(TLStructuredTypePart attribute) {
		return _base.tValue(attribute);
	}

	@Override
	public Object defaultValue(TLStructuredTypePart part) {
		return _base.tValue(part);
	}
}
