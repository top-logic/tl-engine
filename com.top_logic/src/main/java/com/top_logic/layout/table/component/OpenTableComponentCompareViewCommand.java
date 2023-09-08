/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.compare.AbstractOpenCompareViewCommand;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareListsDialog;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractOpenCompareViewCommand} for {@link TableComponent}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenTableComponentCompareViewCommand extends AbstractOpenCompareViewCommand {

	/** Default command name for a {@link OpenTableComponentCompareViewCommand}. */
	public static final String COMMAND_NAME = "openTableComponentCompareView";

	/**
	 * Creates a new {@link OpenTableComponentCompareViewCommand}.
	 */
	public OpenTableComponentCompareViewCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult openCompareDialog(DisplayContext context, LayoutComponent component, Object businessModel,
			Object compareModel, CompareAlgorithm algorithm) {
		TableComponent table = (TableComponent) component;
		ListModelBuilder modelBuilder = table.getListBuilder();
		Collection<?> compareUIModel = modelBuilder.getModel(compareModel, table);
		Collection<?> origUIModel = modelBuilder.getModel(businessModel, table);
		List<Object> origList = toList(origUIModel);
		List<Object> compareList = toList(compareUIModel);
		CompareListsDialog dialog = new CompareListsDialog(origList, compareList, algorithm);
		dialog.setTableConfig(getTableConfiguration(table));

		ensureDialogCloseOnComponentInvisible(dialog.getDialogModel(), component);
		dialog.open(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	private List<Object> toList(Collection<?> uiModel) {
		@SuppressWarnings("unchecked")
		Collection<Object> uncheckedUIModel = (Collection<Object>) uiModel;
		return CollectionUtil.toList(uncheckedUIModel);
	}

	private TableConfigurationProvider getTableConfiguration(TableComponent table) {
		List<TableConfigurationProvider> tableConfigs = new ArrayList<>();
		tableConfigs.add(table.createTableConfigurationProvider());
		addExporter(table, tableConfigs);
		tableConfigs.add(getCompareTableProviderFor(table));
		return TableConfigurationFactory.combine(tableConfigs);
	}

	void addExporter(TableComponent table, List<TableConfigurationProvider> tableConfigs) {
		String exportId = "exportExcel";
		CommandHandler excelExportHandler = table.getCommandById(exportId);
		TableConfigurationProvider exportTableProviderFor = getExportTableProviderFor(table, excelExportHandler);
		tableConfigs.add(exportTableProviderFor);
	}

}

