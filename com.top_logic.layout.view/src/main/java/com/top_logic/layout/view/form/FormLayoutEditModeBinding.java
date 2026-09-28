/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;

/**
 * {@link FormModelListener} keeping a {@link ReactFormLayoutControl} read-only exactly while a
 * {@link FormModel} is not in edit mode.
 *
 * <p>
 * A form grid standing inside a form lays out a part of that form's fields. The chrome of its fields
 * (the required marker, the read-only appearance, the visibility of errors and help) follows the
 * read-only state of the grid nearest to them, so such a grid displays the edit mode of the form it
 * belongs to rather than a state of its own.
 * </p>
 *
 * <p>
 * The binding lasts as long as the grid: disposing the grid removes the listener from the form.
 * </p>
 *
 * @see #bind(ReactFormLayoutControl, FormModel)
 */
public final class FormLayoutEditModeBinding implements FormModelListener {

	private final ReactFormLayoutControl _layout;

	private FormLayoutEditModeBinding(ReactFormLayoutControl layout) {
		_layout = layout;
	}

	/**
	 * Makes the given grid follow the edit mode of the given form, from now on until the grid is
	 * disposed.
	 *
	 * @param layout
	 *        The grid laying out a part of the form's fields.
	 * @param form
	 *        The form whose edit mode the grid displays.
	 */
	public static void bind(ReactFormLayoutControl layout, FormModel form) {
		FormLayoutEditModeBinding binding = new FormLayoutEditModeBinding(layout);
		binding.onFormStateChanged(form);
		form.addFormModelListener(binding);
		layout.addCleanupAction(() -> form.removeFormModelListener(binding));
	}

	@Override
	public void onFormStateChanged(FormModel source) {
		_layout.setReadOnly(!source.isEditMode());
	}

}
