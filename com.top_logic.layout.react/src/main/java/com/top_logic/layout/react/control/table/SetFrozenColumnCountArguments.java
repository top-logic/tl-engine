/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link TableViewControl} {@code setFrozenColumnCount} command: how many
 * leading columns stay frozen.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Freeze {count} columns")
public interface SetFrozenColumnCountArguments extends ReactCommandArguments {

	/** @see #getCount() */
	String COUNT = "count";

	/**
	 * Number of leading columns to keep frozen (clamped to at least zero).
	 */
	@Name(COUNT)
	@Mandatory
	int getCount();

}
