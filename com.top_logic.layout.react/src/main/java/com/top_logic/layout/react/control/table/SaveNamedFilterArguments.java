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
 * Typed arguments of the {@link TableViewControl} {@code saveNamedFilter} command: the name to keep
 * the table's current filter criteria under.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Save filter as '{filterName}'")
public interface SaveNamedFilterArguments extends ReactCommand {

	/** @see #getFilterName() */
	String FILTER_NAME = "filterName";

	/**
	 * The free-text name the user typed.
	 *
	 * @implNote Named {@link #FILTER_NAME} rather than {@code name} on the wire: the
	 *           {@link ReactCommand#getName() command name} already occupies that property.
	 */
	@Name(FILTER_NAME)
	@Mandatory
	String getFilterName();

}
