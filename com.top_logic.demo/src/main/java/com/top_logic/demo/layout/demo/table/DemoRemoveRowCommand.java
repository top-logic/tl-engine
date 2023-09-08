/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CommandHandler} that removes the selected row from a {@link TableComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DemoRemoveRowCommand extends AbstractCommandHandler {

	public interface Config extends AbstractCommandHandler.Config {
		@Override
		@FormattedDefault(SimpleBoundCommandGroup.DELETE_NAME)
		public CommandGroupReference getGroup();
	}

	/**
	 * Creates a {@link DemoRemoveRowCommand}.
	 */
	public DemoRemoveRowCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
		TableComponent tableComponent = (TableComponent) component;
		
		TableViewModel viewModel = tableComponent.getViewModel();
		int selectedApplicationRow =
			viewModel.getApplicationModelRow(TableUtil.getSingleSelectedRow(tableComponent.getTableData()));

		EditableRowTableModel tableModel = tableComponent.getTableModel();
		Object selectedRowObject = tableModel.getRowObject(selectedApplicationRow);

		// Directly remove the currently selected row.
		tableModel.removeRow(selectedApplicationRow);
		
		// Simulate a deletion event to notify others about the removal.
		tableComponent.fireModelDeletedEvent(selectedRowObject, tableComponent);
		
		return HandlerResult.DEFAULT_RESULT;
	}
}