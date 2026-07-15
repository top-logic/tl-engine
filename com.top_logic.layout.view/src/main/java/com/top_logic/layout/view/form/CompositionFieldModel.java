/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLObject;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} for a composition reference as a whole.
 *
 * <p>
 * Value is the current list of composed objects (row overlays + transient new objects) stored in
 * the main overlay. Reference-level constraints (min/max count) apply here.
 * </p>
 *
 * <p>
 * Dirty tracking checks both the list membership (added/removed objects) and whether any row
 * overlay is dirty (attribute changes within composed objects).
 * </p>
 */
public class CompositionFieldModel extends AbstractFieldModel {

	// Note: AbstractFieldModel tracks _defaultValue and _value internally.
	// We use _value (via getValue/setValue) as the current list and _defaultValue as the
	// original list for dirty tracking. We override isDirty() to also check row overlays.
	// No separate _currentList/_originalList fields to avoid state duplication.

	private final List<TLObjectOverlay> _rowOverlays = new ArrayList<>();

	/**
	 * Creates a new {@link CompositionFieldModel} with the given initial list.
	 *
	 * @param initialList
	 *        The initial list of composed objects; used as default value for dirty tracking.
	 */
	public CompositionFieldModel(List<TLObject> initialList) {
		super(new ArrayList<>(initialList)); // defaultValue = initial snapshot
	}

	@Override
	public Object getValue() {
		return getCachedValue(); // Returns the internally stored list
	}

	@Override
	public void setValue(Object value) {
		Object oldValue = getCachedValue();
		setValueInternal(value);
		fireValueChanged(oldValue, value);
	}

	/**
	 * Registers a row overlay for dirty tracking.
	 */
	public void addRowOverlay(TLObjectOverlay overlay) {
		_rowOverlays.add(overlay);
	}

	/**
	 * Removes a row overlay from dirty tracking.
	 */
	public void removeRowOverlay(TLObjectOverlay overlay) {
		_rowOverlays.remove(overlay);
	}

	@Override
	public boolean isDirty() {
		// Check list membership changes (uses AbstractFieldModel's default comparison).
		if (super.isDirty()) {
			return true;
		}
		// Check row overlay changes.
		for (TLObjectOverlay overlay : _rowOverlays) {
			if (overlay.isDirty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The current transient list (typed accessor).
	 */
	@SuppressWarnings("unchecked")
	public List<TLObject> getCurrentList() {
		return (List<TLObject>) getCachedValue();
	}

}
