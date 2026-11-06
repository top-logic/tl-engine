/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.model.TLObject;

/**
 * Read-only view of a form's editing state.
 *
 * <p>
 * Provides the current object (base or overlay) and the edit mode flag. Field controls use this
 * interface to pull their state instead of being pushed by the form.
 * </p>
 */
public interface FormModel {

	/**
	 * The current object being displayed or edited.
	 *
	 * <p>
	 * In edit mode, returns the {@link TLObjectOverlay}. In view mode, returns the base
	 * {@link TLObject}.
	 * </p>
	 *
	 * @return The current object, or {@code null} if no object is selected.
	 */
	TLObject getCurrentObject();

	/**
	 * Whether the form is currently in edit mode.
	 */
	boolean isEditMode();

	/**
	 * The form whose edit session this model takes part in.
	 *
	 * <p>
	 * For a {@link FormControl}, the form itself. For a model nested into a form, such as an
	 * {@link ObjectFieldsControl}, the form it forwards its changes, its validation and its save
	 * to.
	 * </p>
	 */
	FormControl getFormControl();

	/**
	 * Registers a listener that is notified when the form state changes.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	void addFormModelListener(FormModelListener listener);

	/**
	 * Removes a previously registered listener.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	void removeFormModelListener(FormModelListener listener);
}
