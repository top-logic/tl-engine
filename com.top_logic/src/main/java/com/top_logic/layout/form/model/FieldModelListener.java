/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

/**
 * Listener for {@link FieldModel} state changes.
 */
public interface FieldModelListener {

	/**
	 * Called when the {@link FieldModel#getValue() value} changes.
	 *
	 * @param source
	 *        The model whose value changed.
	 * @param oldValue
	 *        The previous value.
	 * @param newValue
	 *        The new value.
	 */
	void onValueChanged(FieldModel source, Object oldValue, Object newValue);

	/**
	 * Called when the {@link FieldModel#isEditable() editability} changes.
	 *
	 * @param source
	 *        The model whose editability changed.
	 * @param editable
	 *        The new editability state.
	 */
	void onEditabilityChanged(FieldModel source, boolean editable);

	/**
	 * Called when validation state changes (error, warnings, or mandatory).
	 *
	 * @param source
	 *        The model whose validation state changed.
	 */
	void onValidationChanged(FieldModel source);
}
