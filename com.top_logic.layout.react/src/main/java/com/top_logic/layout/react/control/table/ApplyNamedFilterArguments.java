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
 * Typed arguments of the {@link TableViewControl} {@code applyNamedFilter} command: which of the
 * filters the table offers under a name to filter by.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Apply filter '{id}'")
public interface ApplyNamedFilterArguments extends ReactCommand {

	/** @see #getId() */
	String ID = "id";

	/**
	 * The {@link com.top_logic.table.NamedFilter#id() identifier} of the filter to apply.
	 */
	@Name(ID)
	@Mandatory
	String getId();

}
