/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.command;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.table.TableData;
import com.top_logic.tool.boundsec.conditional.CommandStep;

/**
 * A command being executed in a table context.
 * 
 * @see AbstractTableCommandProvider#getTableCommand()
 * @see Command
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableCommand {

	/**
	 * Performs an action in the context of the given {@link TableData table}.
	 *
	 * @param context
	 *        The current command context, see {@link Command#executeCommand(DisplayContext)}.
	 * @param table
	 *        The context table.
	 * @param button
	 *        The button that was pressed.
	 * @return The result of the command execution, see
	 *         {@link Command#executeCommand(DisplayContext)}.
	 */
	CommandStep prepareCommand(DisplayContext context, TableData table, CommandModel button);

}
