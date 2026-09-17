/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactFormFieldControl#SUBMIT_COMMAND} command: the value the user
 * has finished entering in a field.
 *
 * <p>
 * The value travels with the submit so that one command both stores it and reports it as complete -
 * a recorded step or an agent therefore submits a value in a single step, without a preceding
 * {@link ReactFormFieldControl#CMD_VALUE_CHANGED}.
 * </p>
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template.
 * </p>
 */
@Label("Submit '{target}' with '{value}'")
public interface FieldSubmitArguments extends ReactCommand {

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * The value entered in the client, or {@code null}/empty for an empty field.
	 */
	@Name(VALUE)
	@Nullable
	String getValue();

	/** @see #getValue() */
	void setValue(String value);

}
