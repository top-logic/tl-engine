/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.layout.form.model.FieldModel;

/**
 * The inline-editing capability of a {@link Column}: produce a {@link FieldModel} for a
 * cell and write an edited value back to the row object.
 *
 * <p>
 * Uses {@link FieldModel} (the field abstraction the React form controls bind to) rather
 * than a legacy per-row {@code FormGroup}. The {@link #commit} method assumes an ambient
 * transaction owned by the caller.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 * @param <V>
 *        The cell value type.
 */
public interface CellEditor<R, V> {

	/**
	 * Creates an editable field model seeded with the cell's current value.
	 *
	 * @param row
	 *        The row being edited.
	 * @param current
	 *        The current cell value.
	 */
	FieldModel newField(R row, V current);

	/**
	 * Writes an edited value back to the row object. The caller owns the transaction.
	 *
	 * @param row
	 *        The row being edited.
	 * @param edited
	 *        The new cell value.
	 */
	void commit(R row, V edited);

}
