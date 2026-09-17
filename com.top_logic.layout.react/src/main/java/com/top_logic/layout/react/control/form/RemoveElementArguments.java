/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactValueListControl#CMD_REMOVE_ELEMENT} command: which of the
 * values to drop.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Remove value {index} of '{target}'")
public interface RemoveElementArguments extends ReactCommand {

	/** @see #getIndex() */
	String INDEX = "index";

	/**
	 * The position of the value to remove, counted from {@code 0}.
	 */
	@Name(INDEX)
	@Mandatory
	int getIndex();

}
