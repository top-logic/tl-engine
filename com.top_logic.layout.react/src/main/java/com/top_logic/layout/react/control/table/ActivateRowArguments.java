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
 * Typed arguments of the {@link TableViewControl} activate command: which row the user opened by
 * double-clicking it, or by pressing {@code Enter} while it carried the keyboard cursor.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. The row is given by the id the table displays
 * it under, which a recorded step carries as the business key of the row's object, see
 * {@link DisplayedObjectId}.
 * </p>
 */
@Label("Activate row '{rowId}'")
public interface ActivateRowArguments extends ReactCommand {

	/** @see #getRowId() */
	String ROW_ID = SelectRowArguments.ROW_ID;

	/**
	 * The id of the row to activate, as the table published it with the row.
	 *
	 * @see TableViewControl#objectIdentities()
	 */
	@Name(ROW_ID)
	@Mandatory
	@DisplayedObjectId
	String getRowId();

}
