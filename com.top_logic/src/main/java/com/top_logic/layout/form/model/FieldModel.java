/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * Lightweight model for a single form field value.
 *
 * <p>
 * Captures typed value, dirty tracking, and edit lifecycle.
 * </p>
 */
public interface FieldModel {

	/**
	 * The current typed value.
	 */
	Object getValue();

	/**
	 * Sets the typed value.
	 *
	 * <p>
	 * Fires {@link FieldModelListener#onValueChanged(FieldModel, Object, Object)} if the value
	 * changes.
	 * </p>
	 *
	 * @param value
	 *        The new value. May be {@code null}.
	 */
	void setValue(Object value);

	/**
	 * Whether the value has been modified from its default/original.
	 */
	boolean isDirty();

	/**
	 * Whether the value is currently editable.
	 *
	 * <p>
	 * Subsumes the legacy {@code immutable}, {@code disabled}, {@code frozen}, and {@code blocked}
	 * states. Controls render non-editable state uniformly.
	 * </p>
	 */
	boolean isEditable();

	/**
	 * Whether a value is required.
	 */
	boolean isMandatory();

	/**
	 * Whether the field currently has a validation error.
	 */
	boolean hasError();

	/**
	 * The current validation error, or {@code null} if no error.
	 */
	ResKey getError();

	/**
	 * Whether the field currently has validation warnings.
	 */
	boolean hasWarnings();

	/**
	 * The current validation warnings, or an empty list if none.
	 */
	List<ResKey> getWarnings();

	/**
	 * Sets a validation error from the {@code FormValidationModel}.
	 *
	 * <p>
	 * This is called by the view's bridging listener to propagate model-level
	 * validation results.
	 * </p>
	 *
	 * @param error
	 *        The error key, or {@code null} to clear.
	 */
	default void setModelValidationError(ResKey error) {
		// Default no-op, overridden in AbstractFieldModel.
	}

	/**
	 * Sets validation warnings from the {@code FormValidationModel}.
	 *
	 * @param warnings
	 *        The warning keys, or empty list to clear.
	 */
	default void setModelValidationWarnings(List<ResKey> warnings) {
		// Default no-op, overridden in AbstractFieldModel.
	}

	/**
	 * Adds a listener for value, editability, and validation changes.
	 */
	void addListener(FieldModelListener listener);

	/**
	 * Removes a previously added listener.
	 */
	void removeListener(FieldModelListener listener);
}
