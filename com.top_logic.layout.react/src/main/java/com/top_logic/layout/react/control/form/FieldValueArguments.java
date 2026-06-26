/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactFormFieldControl#CMD_VALUE_CHANGED} command of a single-valued
 * field: the new value the client entered (as text; empty/{@code null} clears the field).
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template.
 * </p>
 */
@Label("Set the field value to '{value}'")
public interface FieldValueArguments extends ReactCommandArguments {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * The new field value entered in the client, or {@code null}/empty to clear the field.
	 */
	@Name(VALUE)
	@Nullable
	String getValue();

}
