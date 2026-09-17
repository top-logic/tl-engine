/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link TableViewControl} {@code search} command: the text to look for in
 * the table's displayed columns.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Search for '{term}'")
public interface SearchArguments extends ReactCommand {

	/** @see #getTerm() */
	String TERM = "term";

	/**
	 * The text to look for, or empty to search for nothing.
	 */
	@Name(TERM)
	String getTerm();

}
