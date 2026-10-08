/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.function.Consumer;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;

/**
 * {@link FormModelListener} keeping a form grid read-only exactly while a {@link FormModel} is not
 * in edit mode.
 *
 * <p>
 * The chrome of the fields of a form grid (the required marker, the read-only appearance, the
 * visibility of errors and help) follows the read-only state of the grid nearest to them
 * ({@link ReactFormLayoutControl#READ_ONLY}). A grid displays the edit mode of the form it belongs
 * to rather than a state of its own: the grid of the form itself as well as a grid standing inside a
 * form, laying out a part of that form's fields.
 * </p>
 *
 * <p>
 * The binding lasts as long as the grid: disposing the grid removes the listener from the form.
 * </p>
 *
 * @see #bind(ReactFormLayoutControl, FormModel)
 * @see #bind(ReactControl, Consumer, FormModel)
 */
public final class FormLayoutEditModeBinding implements FormModelListener {

	private final Consumer<Boolean> _setReadOnly;

	private FormLayoutEditModeBinding(Consumer<Boolean> setReadOnly) {
		_setReadOnly = setReadOnly;
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
		bind(layout, layout::setReadOnly, form);
	}

	/**
	 * Makes the given grid follow the edit mode of the given form, from now on until the grid is
	 * disposed.
	 *
	 * @param grid
	 *        The control rendering the grid.
	 * @param setReadOnly
	 *        Publishes the read-only state of the grid as {@link ReactFormLayoutControl#READ_ONLY}.
	 * @param form
	 *        The form whose edit mode the grid displays; may be the grid itself.
	 */
	public static void bind(ReactControl grid, Consumer<Boolean> setReadOnly, FormModel form) {
		FormLayoutEditModeBinding binding = new FormLayoutEditModeBinding(setReadOnly);
		binding.onFormStateChanged(form);
		form.addFormModelListener(binding);
		grid.addCleanupAction(() -> form.removeFormModelListener(binding));
	}

	@Override
	public void onFormStateChanged(FormModel source) {
		_setReadOnly.accept(Boolean.valueOf(!source.isEditMode()));
	}

}
