/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.compare.AbstractOpenCompareViewCommand;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareListsDialog;
import com.top_logic.layout.compare.CompareTreesDialog;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractOpenCompareViewCommand} for {@link GridComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenGridCompareViewCommand extends AbstractOpenCompareViewCommand {

	/** Default command name for a {@link OpenGridCompareViewCommand}. */
	public static final String COMMAND_NAME = "openGridCompareView";

	/**
	 * Creates a new {@link OpenGridCompareViewCommand}.
	 */
	public OpenGridCompareViewCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected HandlerResult openCompareDialog(DisplayContext context, LayoutComponent component, Object newBusinessModel,
			Object oldBusinessModel, CompareAlgorithm algorithm) {
		GridComponent grid = (GridComponent) component;
		ModelBuilder modelBuilder = grid.getBuilder();
		Object baseModel = modelBuilder.getModel(oldBusinessModel, grid);
		Object changeModel = modelBuilder.getModel(newBusinessModel, grid);
		AbstractFormDialogBase dialog;
		if (modelBuilder instanceof TableGridBuilder) {
			List<Object> origList = toList(changeModel);
			List<Object> compareList = toList(baseModel);

			CompareListsDialog compareListsDialog = new CompareListsDialog(origList, compareList, algorithm);
			compareListsDialog.setTableConfig(getTableConfiguration(grid));
			dialog = compareListsDialog;
		} else {
			TreeView tree = ((AbstractTreeGridBuilder) modelBuilder).asTreeView(grid);
			List<String> columns = getDefaultColumns(grid);
			CompareTreesDialog compareTreesDialog =
				CompareTreesDialog.newCompareTreesDialog(tree, baseModel, changeModel, columns, algorithm);
			if (compareTreesDialog == null) {
				return openNotComparableDialog(context, newBusinessModel);
			}
			compareTreesDialog.setTableConfig(getTableConfiguration(grid));
			compareTreesDialog.setSelectable(true);
			compareTreesDialog.setRootVisible(isRootVisible(grid));
			compareTreesDialog.setConfigKey(ConfigKey.part(grid, "compareTable"));
			dialog = compareTreesDialog;
		}
		ensureDialogCloseOnComponentInvisible(dialog.getDialogModel(), component);
		dialog.open(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	private boolean isRootVisible(GridComponent grid) {
		boolean rootVisible;
		if (grid.hasFormContext()) {
			TableField tableField = grid.getTableField(grid.getFormContext());
			TreeTableModel tableModel = (TreeTableModel) tableField.getTableModel();
			TreeUIModel<?> treeModel = tableModel.getTreeModel();
			rootVisible = treeModel.isRootVisible();
		} else {
			rootVisible = true;
		}
		return rootVisible;
	}

	private List<Object> toList(Object collectionUIModel) {
		return CollectionUtil.toList((Collection<Object>) collectionUIModel);
	}

	private List<String> getDefaultColumns(GridComponent grid) {
		List<String> defaultColumnNames = grid.getDefaultColumnNames();
		List<String> columns = new ArrayList<>();
		columns.add(CompareService.DECORATION_COLUMN);
		for (String column : defaultColumnNames) {
			if (GridComponent.COLUMN_TECHNICAL.equals(column)) {
				// exclude technical column from default columns.
				continue;
			}
			columns.add(column);
		}
		return columns;
	}

	private TableConfigurationProvider getTableConfiguration(GridComponent grid) {
		List<TableConfigurationProvider> tableConfigs = new ArrayList<>();
		grid.addTableConfigs(tableConfigs);
		tableConfigs.add(removeTechnicalColumn());
		tableConfigs.add(getExportTableProviderFor(grid, exportHandler(grid)));
		tableConfigs.add(getCompareTableProviderFor(grid));
		TableConfigurationProvider tableConfigurationProvider = TableConfigurationFactory.combine(tableConfigs);
		return tableConfigurationProvider;
	}

	private CommandHandler exportHandler(GridComponent grid) {
		String exportCommandHandlerName = "exportExcelGrid";
		return grid.getCommandById(exportCommandHandlerName);
	}

	private TableConfigurationProvider removeTechnicalColumn() {
		return new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration technicalColumn = table.getDeclaredColumn(GridComponent.COLUMN_TECHNICAL);
				if (technicalColumn != null) {
					table.removeColumn(GridComponent.COLUMN_TECHNICAL);
				}
			}
		};
	}

}

