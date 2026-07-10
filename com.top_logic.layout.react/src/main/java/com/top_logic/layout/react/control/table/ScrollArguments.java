/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link TableViewControl} {@code scroll} command: the row window the client
 * viewport now shows.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Scroll to row {start}")
public interface ScrollArguments extends ReactCommand {

	/** @see #getStart() */
	String START = "start";

	/** @see #getCount() */
	String COUNT = "count";

	/**
	 * Zero-based index of the first visible row.
	 */
	@Name(START)
	@Mandatory
	int getStart();

	/**
	 * Number of rows in the viewport.
	 */
	@Name(COUNT)
	@Mandatory
	int getCount();

}
