/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.command;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.table.TableData;

/**
 * Factory for a {@link CommandModel toolbar-button} in the context of a {@link TableData table}.
 * 
 * @see TableCommandConfig
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableCommandProvider {

	/**
	 * Creates a toolbar-button for the given {@link TableData table}.
	 * 
	 * @param table
	 *        The table context in which the new button operates. The resulting command can access
	 *        the table contents and a potential selection by storing a reference to this argument.
	 * 
	 * @return The new button for the given table.
	 */
	CommandModel createCommand(TableData table);

}
