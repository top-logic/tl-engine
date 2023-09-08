/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.handler;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * The command-handler for opening a detail-table-dialog.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class OpenChartDetailsCommand extends OpenModalDialogCommandHandler {

	/**
	 * Command ID of this {@link CommandHandler}.
	 */
	public static String COMMAND_ID = "openChartDetails";

	/**
	 * <code>PARAMETER_ID</code>: Name of the parameter in the arguments
	 */
	public static final String PARAMETER_ID = "_id";

	/**
	 * Config-constructor for {@link OpenChartDetailsCommand}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public OpenChartDetailsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		boolean isSuccess = false;
		HandlerResult theResult = HandlerResult.DEFAULT_RESULT;

		if (aComponent instanceof AbstractChartComponent) {
			String id = (String) someArguments.get(OpenChartDetailsCommand.PARAMETER_ID);
			ChartTree tree = ((AbstractChartComponent) aComponent).getChartTree();
			ChartNode node = tree.getNode(id);
			List<?> values = node.getObjects();

			if (!CollectionUtil.isEmptyOrNull(values)) {

				isSuccess = AbstractChartComponent.showSingleObject(aComponent, values);

				if (!isSuccess) {
					theResult = super.handleCommand(aContext, aComponent, model, someArguments);
					LayoutComponent dialog = getDialog(aComponent, someArguments);
					FindTableVisitor visitor = new FindTableVisitor();
					dialog.acceptVisitorRecursively(visitor);
					visitor.getTable().setModel(values);
					isSuccess = true;
				}
			}
			else {
				return theResult;
			}
		}

		if (!isSuccess) {
			theResult = new HandlerResult();
			theResult.addErrorText("Failed to get objects for display.");
		}

		return theResult;
	}

	/**
	 * Descending visitor that searches a {@link TableComponent}. Used to find the layout for the
	 * chart-detail-table.
	 */
	public static class FindTableVisitor extends DefaultDescendingLayoutVisitor {

		private TableComponent _table;

		@Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
			if (aComponent instanceof TableComponent) {
				_table = (TableComponent) aComponent;
				return false;
			}
			return super.visitLayoutComponent(aComponent);
		}

		/**
		 * Getter for the {@link TableComponent} found during visit. Correct use assumed this is
		 * never null. Fix layout or initial component or don't use this visitor if it returns null.
		 * 
		 * @return the first table-component the visitor found.
		 */
		public TableComponent getTable() {
			return _table;
		}
	}

}