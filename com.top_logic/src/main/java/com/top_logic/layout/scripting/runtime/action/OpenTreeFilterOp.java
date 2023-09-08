/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultPopupHandler;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.WrappedCommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.scripting.action.OpenTreeFilter;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.FilterOptionsUtil.OpenFilterOptionsCommand;
import com.top_logic.layout.table.renderer.FilterOptionsUtil.OpenFilterOptionsCommandModel;
import com.top_logic.layout.toolbar.ToolBarGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link TableActionOp} that opens the tree filter options popup.
 * 
 * @see OpenTreeFilter
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OpenTreeFilterOp extends TableActionOp<OpenTreeFilter> {

	/**
	 * Creates a {@link OpenTreeFilterOp} from a configuration.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} for instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link OpenTreeFilterOp}.
	 */
	public OpenTreeFilterOp(InstantiationContext context, OpenTreeFilter config) {
		super(context, config);
	}

	@Override
	protected void internalProcess(ActionContext context, TableControl table, Object argument) {
		TableData tableData = table.getTableData();
		OpenFilterOptionsCommand command = new OpenFilterOptionsCommand(tableData);
		command.showPopup(context.getDisplayContext(), findToolbarHandler(context, tableData));
	}

	private PopupHandler findToolbarHandler(ActionContext context, TableData tableData) {
		ToolBarGroup group = tableData.getToolBar().getGroup(CommandHandlerFactory.TABLE_CONFIG_GROUP);
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
		return button instanceof OpenFilterOptionsCommandModel ||
			(button instanceof WrappedCommandModel && isOpener(((WrappedCommandModel) button).unwrap()));
	}

}
