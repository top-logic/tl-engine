/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.SimpleJsonListBinding;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the group command of the {@link TableViewControl} and of its
 * {@link ReactColumnSelectControl column selection}: the columns the table groups its rows by.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Group by columns '{columns}'")
public interface GroupArguments extends ReactCommand {

	/** @see #getColumns() */
	String COLUMNS = "columns";

	/**
	 * The names of the columns to group the rows by, outermost group level first; an empty list
	 * shows the rows ungrouped.
	 */
	@Name(COLUMNS)
	@JsonBinding(SimpleJsonListBinding.class)
	List<String> getColumns();

}
