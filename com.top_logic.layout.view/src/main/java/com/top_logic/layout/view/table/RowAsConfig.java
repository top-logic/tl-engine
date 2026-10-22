/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration of a table naming the row its scripts and the scripts of its columns are evaluated
 * for.
 *
 * @implNote The scripts declare the row with
 *           {@link com.top_logic.layout.view.channel.Supplies} renamed by {@link #ROW_AS}.
 */
public interface RowAsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getRowAs()}. */
	String ROW_AS = "row-as";

	/** Name of the variable holding the row when no other name is given. */
	String DEFAULT_ROW_AS = "row";

	/**
	 * The name of the variable holding the row in the scripts evaluated for a row: the row class of
	 * the table, and the scripts of its columns - e.g. the value of a computed column.
	 *
	 * <p>
	 * Unset, the variable is named {@value #DEFAULT_ROW_AS}. With <code>row-as="ticket"</code>, a
	 * computed column reads <code>$ticket.get(`my:Ticket#state`)</code>. The scripts see the row
	 * behind the values of their inputs; an input of the same name is a configuration error and is
	 * renamed with <code>as</code>. For a column of an embedding, the row is the embedded object.
	 * </p>
	 */
	@Name(ROW_AS)
	@Nullable
	String getRowAs();

}
