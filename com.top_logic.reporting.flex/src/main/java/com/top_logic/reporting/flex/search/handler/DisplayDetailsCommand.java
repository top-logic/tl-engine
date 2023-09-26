/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;
import com.top_logic.reporting.flex.chart.component.ChartDescription;
import com.top_logic.reporting.flex.chart.component.ChartModel;
import com.top_logic.reporting.flex.chart.component.DetailsTableDialog;
import com.top_logic.reporting.flex.chart.component.handler.OpenChartDetailsCommand;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.search.chart.SearchResultChartConfig;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Dialog-hanlder that works with {@link AttributedSearchResultSet} to pass information about type
 * and selected columns.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DisplayDetailsCommand extends OpenModalDialogCommandHandler {

	/**
	 * Configuration options for {@link DisplayDetailsCommand}.
	 */
	public interface Config extends OpenModalDialogCommandHandler.Config {
		// No separate access rights for displaying details.
		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		public CommandGroupReference getGroup();
	}

	/**
	 * <code>COMMAND_ID</code>: The ID used to register this command in
	 * {@link CommandHandlerFactory}
	 */
	public static String COMMAND_ID = "displaySearchResultDetails";

	/**
	 * Creates a new {@link DisplayDetailsCommand}
	 */
	public DisplayDetailsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		AbstractChartComponent chartComponent = (AbstractChartComponent) aComponent;
		if (model instanceof ChartModel) {
			ChartConfig config = ((ChartModel) model).getConfig();
			String id = (String) someArguments.get(OpenChartDetailsCommand.PARAMETER_ID);
			ChartTree tree = chartComponent.getChartTree();
			ChartNode node = tree.getNode(id);
			if (config instanceof SearchResultChartConfig) {
				Set<? extends TLClass> types = ((SearchResultChartConfig) config).getType().get();
				DataKey dataKey = tree.getDataKey(id);
				List<?> objects = node.getObjects();
				DetailsTableDialog dialog = DetailsTableDialog.getDialog(aComponent);
				List<String> searchColumns = ((SearchResultChartConfig) config).getColumns();
				List<String> chartColumns = new ChartDescription(tree).getColumns();
				chartColumns.removeAll(searchColumns);
				chartColumns.addAll(0, searchColumns);
				AttributedSearchResultSet result = toSearchResult(objects, types, chartColumns);
				dialog.setSearchResult(result);
				LayoutComponent topLayout = dialog.getDialogTopLayout();
				OpenModalDialogCommandHandler.openDialog(topLayout, new ConstantDisplayValue(String.valueOf(dataKey)));
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ComponentName getOpenToDialogName() {
		return DetailsTableDialog.GLOBAL_DIALOG_NAME;
	}

	private AttributedSearchResultSet toSearchResult(List<?> objects, Set<? extends TLClass> types,
			List<String> cols) {
		Collection<TLObject> wrappers = CollectionUtil.dynamicCastView(TLObject.class, (Collection<?>) objects);
		AttributedSearchResultSet result = new AttributedSearchResultSet(wrappers, types, cols, null);
		return result;
	}

	@Override
	protected ResKey getDefaultI18NKey() {
		return I18NConstants.OPEN_CHART_DETAIL;
	}
}