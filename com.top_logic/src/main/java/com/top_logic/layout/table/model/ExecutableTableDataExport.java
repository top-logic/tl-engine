/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.table.TableData;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link TableDataExport} that is always executable.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ExecutableTableDataExport implements TableDataExport {

	@Override
	public ExecutableState getExecutability(TableData table) {
		return ExecutableState.EXECUTABLE;
	}

}

