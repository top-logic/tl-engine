/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.model.TLObject;

/**
 * Per-row model for a composition table.
 *
 * <p>
 * Holds the row's overlay - over an existing persistent object, or over the transient object of a
 * new entry - plus the {@link BoundFieldModel} holding the edited value of each editable column.
 * </p>
 */
public class CompositionRowModel {

	private final TLObjectOverlay _rowOverlay;

	private final TLObject _rowObject;

	private final boolean _new;

	private final Map<String, BoundFieldModel> _columnModels = new LinkedHashMap<>();

	private CompositionRowModel(TLObjectOverlay rowOverlay, boolean isNew) {
		_rowOverlay = rowOverlay;
		_rowObject = rowOverlay;
		_new = isNew;
	}

	/**
	 * Factory for existing persistent objects (wrapped in overlay).
	 */
	public static CompositionRowModel forExisting(TLObjectOverlay rowOverlay) {
		return new CompositionRowModel(rowOverlay, false);
	}

	/**
	 * Factory for new entries, edited in the overlay over their transient object, see
	 * {@link EditLevel#create(com.top_logic.model.TLClass, TLObject)}.
	 */
	public static CompositionRowModel forNew(TLObjectOverlay rowOverlay) {
		return new CompositionRowModel(rowOverlay, true);
	}

	/**
	 * Whether the row is a new entry, which is created when the form is saved.
	 */
	public boolean isNew() {
		return _new;
	}

	/**
	 * The object to read/write attribute values from, the {@link #getRowOverlay() overlay}.
	 */
	public TLObject getRowObject() {
		return _rowObject;
	}

	/**
	 * The overlay the row is edited in.
	 */
	public TLObjectOverlay getRowOverlay() {
		return _rowOverlay;
	}

	/**
	 * Registers the field model holding the edited value of a column.
	 */
	public void putColumnModel(String columnName, BoundFieldModel model) {
		_columnModels.put(columnName, model);
	}

	/**
	 * The field model holding the edited value of a column, or {@code null} if that cell was not
	 * edited yet.
	 */
	public BoundFieldModel getColumnModel(String columnName) {
		return _columnModels.get(columnName);
	}

	/**
	 * All column field models.
	 */
	public Map<String, BoundFieldModel> getColumnModels() {
		return _columnModels;
	}

	/**
	 * Re-reads all column model values from the underlying row object.
	 *
	 * <p>
	 * Call this after external code has modified the row overlay (e.g. a detail dialog applying its
	 * overlay) to synchronize the cached field model values with the live object state.
	 * </p>
	 */
	public void refreshColumnModels() {
		for (BoundFieldModel model : _columnModels.values()) {
			model.refreshFromObject();
		}
	}

}
