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
import com.top_logic.layout.react.control.identity.DisplayedObjectId;

/**
 * Typed arguments of the {@link TableViewControl} {@code expand} command: which tree/group row to
 * expand or collapse, and the target state.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. The row is given by the id the table displays
 * it under, which a recorded step carries as the business key of the row's object, see
 * {@link DisplayedObjectId}.
 * </p>
 */
@Label("Set row '{rowId}' expanded to {expanded}")
public interface ExpandArguments extends ReactCommand {

	/** @see #getRowId() */
	String ROW_ID = SelectRowArguments.ROW_ID;

	/** @see #isExpanded() */
	String EXPANDED = "expanded";

	/**
	 * The id of the tree or group row to expand or collapse, as the table published it with the
	 * row.
	 *
	 * @see TableViewControl#objectIdentities()
	 */
	@Name(ROW_ID)
	@Mandatory
	@DisplayedObjectId
	String getRowId();

	/**
	 * {@code true} to expand the row, {@code false} to collapse it.
	 */
	@Name(EXPANDED)
	@Mandatory
	boolean isExpanded();

}
