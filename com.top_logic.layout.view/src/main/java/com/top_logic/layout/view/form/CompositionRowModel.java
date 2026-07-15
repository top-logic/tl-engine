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
 * Holds a reference to the row's overlay (for existing persistent objects) or transient object
 * (for new entries), plus the {@link AttributeFieldModel} instances for each editable column.
 * </p>
 */
public class CompositionRowModel {

	private final TLObjectOverlay _rowOverlay;

	private final TLObject _rowObject;

	private final boolean _isNew;

	private final Map<String, AttributeFieldModel> _columnModels = new LinkedHashMap<>();

	private CompositionRowModel(TLObjectOverlay rowOverlay, TLObject rowObject, boolean isNew) {
		_rowOverlay = rowOverlay;
		_rowObject = rowObject;
		_isNew = isNew;
	}

	/**
	 * Factory for existing persistent objects (wrapped in overlay).
	 */
	public static CompositionRowModel forExisting(TLObjectOverlay rowOverlay) {
		return new CompositionRowModel(rowOverlay, rowOverlay, false);
	}

	/**
	 * Factory for new transient objects.
	 */
	public static CompositionRowModel forNew(TLObject transientObject) {
		return new CompositionRowModel(null, transientObject, true);
	}

	/**
	 * The object to read/write attribute values from (overlay or transient).
	 */
	public TLObject getRowObject() {
		return _rowObject;
	}

	/**
	 * The overlay, or {@code null} for new transient objects.
	 */
	public TLObjectOverlay getRowOverlay() {
		return _rowOverlay;
	}

	/**
	 * Whether this is a newly created (transient) row.
	 */
	public boolean isNew() {
		return _isNew;
	}

	/**
	 * Registers an {@link AttributeFieldModel} for a column.
	 */
	public void putColumnModel(String columnName, AttributeFieldModel model) {
		_columnModels.put(columnName, model);
	}

	/**
	 * Gets the {@link AttributeFieldModel} for a column.
	 */
	public AttributeFieldModel getColumnModel(String columnName) {
		return _columnModels.get(columnName);
	}

	/**
	 * All column field models.
	 */
	public Map<String, AttributeFieldModel> getColumnModels() {
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
		for (AttributeFieldModel model : _columnModels.values()) {
			model.refreshFromObject();
		}
	}

}
