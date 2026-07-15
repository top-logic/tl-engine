/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactDeckPaneControl#SELECT_CHILD_COMMAND} command: which child pane
 * to show.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Select pane {index}")
public interface SelectChildArguments extends ReactCommand {

	/** @see #getIndex() */
	String INDEX = "index";

	/**
	 * The zero-based index of the child pane to show.
	 */
	@Name(INDEX)
	@Mandatory
	int getIndex();

}
