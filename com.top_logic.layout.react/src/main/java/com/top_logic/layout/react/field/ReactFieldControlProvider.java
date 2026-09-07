/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Creates the control that edits a value of a certain type.
 *
 * <p>
 * A provider is registered for the value types it handles, see {@link FieldControlRegistry}. It
 * receives the value through a {@link FieldModel} and its display hints through a {@link FieldSpec},
 * and thus serves both model attributes and configuration properties.
 * </p>
 */
@FunctionalInterface
public interface ReactFieldControlProvider {

	/**
	 * Creates the control editing the given value.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param field
	 *        What is being edited.
	 * @param model
	 *        Holds the edited value.
	 * @return The control to display.
	 */
	ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model);

}
