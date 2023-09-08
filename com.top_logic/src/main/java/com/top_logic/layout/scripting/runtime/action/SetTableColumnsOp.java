/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link TableActionOp} that sets the columns of a table.
 * 
 * @see SetTableColumns
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetTableColumnsOp extends TableActionOp<SetTableColumns> {

	public SetTableColumnsOp(InstantiationContext context, SetTableColumns config) {
		super(context, config);
	}

	@Override
	protected void internalProcess(ActionContext context, TableControl table, Object argument) {
		process(table);
	}

	void process(final TableControl table) {
		try {
			table.getViewModel().setColumns(getColumnNames(table));
		} catch (VetoException ex) {
			ex.setContinuationCommand(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					process(table);
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			ex.process(table.getWindowScope());
		}
	}

	private List<String> getColumnNames(TableControl table) {
		if (getConfig().isLabel()) {
			return ScriptTableUtil.toColumnNames(getConfig().getColumns(), table.getTableData(), false);
		}
		return getConfig().getColumns();
	}

}
