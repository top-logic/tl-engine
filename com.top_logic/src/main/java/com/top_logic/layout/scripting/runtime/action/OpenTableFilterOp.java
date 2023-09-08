/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import static com.top_logic.basic.StringServices.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultPopupHandler;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.WrappedCommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.action.OpenTableFilter;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.OpenFilterDialogAction;
import com.top_logic.layout.table.renderer.TableButtons.OpenGlobalFilterCommandModel;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link TableActionOp} that opens a table filter dialog for a certain column.
 * 
 * @see OpenTableFilter
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OpenTableFilterOp extends TableActionOp<OpenTableFilter> {

	/**
	 * Creates a {@link OpenTableFilterOp} from configuration.
	 */
	public OpenTableFilterOp(InstantiationContext context, OpenTableFilter config) {
		super(context, config);
		makeBackwardsCompatibleWithFilterColumnName();
	}

	/** Migrates the script before execution. */
	@SuppressWarnings("deprecation")
	private void makeBackwardsCompatibleWithFilterColumnName() {
		String filterColumnName = getConfig().getFilterColumnName();
		if (!isEmpty(filterColumnName)) {
			getConfig().setColumnName(filterColumnName);
			getConfig().setFilterColumnName("");
		}
	}

	@Override
	protected void internalProcess(ActionContext context, TableControl table, Object argument) {
		TableData tableData = table.getTableData();
		String filterPosition = getFilterPosition(tableData);
		if (!filterPosition.equals(TableViewModel.GLOBAL_TABLE_FILTER_ID)) {
			checkColumn(tableData, filterPosition);
		} else {
			checkGlobalFilter(tableData, filterPosition);
		}
		DisplayContext displayContext = context.getDisplayContext();
		PopupHandler handler = findHandler(context, table);
		OpenFilterDialogAction.openTableFilter(displayContext, handler, tableData, filterPosition);
	}

	private void checkColumn(TableData tableData, String columnName) {
		checkColumnExists(tableData, columnName);
		checkColumnIsVisible(tableData, columnName);
		checkColumnIsFilterable(tableData, columnName);
	}

	private void checkGlobalFilter(TableData tableData, String columnName) {
		if (!isPositionFilterable(tableData, columnName)) {
			fail("Global table filter is not existent.");
		}
	}

	private void checkColumnIsFilterable(TableData tableData, String columnName) {
		if (!isPositionFilterable(tableData, columnName)) {
			fail("Column '" + columnName + "' can not be filtered.");
		}
	}

	private boolean isPositionFilterable(TableData tableData, String columnName) {
		return tableData.getViewModel().getFilter(columnName) != null;
	}

	private PopupHandler findHandler(ActionContext context, TableControl table) {
		TableData tableData = table.getTableData();
		String columnName = getFilterPosition(tableData);
		if (TableViewModel.GLOBAL_TABLE_FILTER_ID.equals(columnName)) {
			return findToolbarHandler(context, tableData);
		} else {
			String placementID = table.getColumnActivateElementID(columnName);
			FrameScope frameScope = table.getFrameScope();
			return new DefaultPopupHandler(frameScope, placementID);
		}
	}

	private String getFilterPosition(TableData tableData) {
		return ScriptTableUtil.getColumnName(getConfig(), tableData);
	}

	private PopupHandler findToolbarHandler(ActionContext context, TableData tableData) {
		ToolBarGroup group = tableData.getToolBar().getGroup(CommandHandlerFactory.TABLE_BUTTONS_GROUP);
		ApplicationAssertions.assertNotNull(getConfig(), "Table buttons group not found.", group);
		
		for (HTMLFragment view : group.getViews()) {
			if (!(view instanceof ButtonControl)) {
				continue;
			}
			CommandModel button = ((ButtonControl) view).getModel();
			if (isOpener(button)) {
				AbstractControlBase control = findButton(rootScope(context), button);
				ApplicationAssertions.assertNotNull(getConfig(), "Opening button not found.", control);

				return new DefaultPopupHandler(control.getFrameScope(), control.getID());
			}
		}

		throw ApplicationAssertions.fail(getConfig(), "Global filter opening command not found.");
	}

	private boolean isOpener(Command button) {
		return button instanceof OpenGlobalFilterCommandModel ||
			(button instanceof WrappedCommandModel && isOpener(((WrappedCommandModel) button).unwrap()));
	}

}
