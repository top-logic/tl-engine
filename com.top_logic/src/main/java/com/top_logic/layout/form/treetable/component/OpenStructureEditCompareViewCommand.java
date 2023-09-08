/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.compare.AbstractOpenCompareViewCommand;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareTreesDialog;
import com.top_logic.layout.compare.InCompareModeExecutability;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.tree.component.TreeBuilderTreeView;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLNamed;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * {@link AbstractOpenCompareViewCommand} for a {@link StructureEditComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenStructureEditCompareViewCommand extends AbstractOpenCompareViewCommand {

	/** Default command name for a {@link OpenStructureEditCompareViewCommand}. */
	public static final String COMMAND_NAME = "openStructureEditCompareView";

	/**
	 * Configuration of a {@link OpenStructureEditCompareViewCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractOpenCompareViewCommand.Config {

		@Override
		@ListDefault({
			BusinessTreeBuilderAvailable.class,
			InViewModeExecutable.class,
			InCompareModeExecutability.class
		})
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();
	}

	/**
	 * Creates a new {@link OpenStructureEditCompareViewCommand}.
	 */
	public OpenStructureEditCompareViewCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult openCompareDialog(DisplayContext context, LayoutComponent component,
			Object newBusinessModel,
			Object oldBusinessModel, CompareAlgorithm algorithm) {
		StructureEditComponent<?> structureEditor = (StructureEditComponent<?>) component;
		List<String> columnNames = getVisibleColumns(structureEditor);
		TreeModelBuilder<?> compareModelBuilder = structureEditor._compareModelBuilder;
		Object baseModel = compareModelBuilder.getModel(oldBusinessModel, structureEditor);
		Object changeModel = compareModelBuilder.getModel(newBusinessModel, structureEditor);
		TreeBuilderTreeView<Object> tree =
			(TreeBuilderTreeView<Object>) new TreeBuilderTreeView<>(structureEditor, compareModelBuilder);
		CompareTreesDialog<Object> dialog =
			CompareTreesDialog.newCompareTreesDialog(tree, baseModel, changeModel, columnNames, algorithm);
		if (dialog == null) {
			return openNotComparableDialog(context, newBusinessModel);
		}

		dialog.setTableConfig(newTableConfig(structureEditor, columnNames));
		dialog.setRootVisible(structureEditor.rootVisible);
		ensureDialogCloseOnComponentInvisible(dialog.getDialogModel(), component);
		dialog.setConfigKey(ConfigKey.part(structureEditor, "compareTable"));
		dialog.open(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	private TableConfigurationProvider newTableConfig(StructureEditComponent<?> structureEditor,
			List<String> columnNames) {
		TableConfigurationProvider configuredTableConfiguration = structureEditor._tableConfiguration;
		TableConfigurationProvider exportTableConfiguration =
			getExportTableProviderFor(structureEditor, exportHandler(structureEditor));
		TableConfigurationProvider compareTableConfiguration = getCompareTableProviderFor(structureEditor);
		/* When the columns are not declared explicit, the settings of the default column are not
		 * applied to theses columns */
		TableConfigurationProvider declareVisibleColumns = declareColumns(columnNames);
		TableConfigurationProvider ensureColumnsVisible = GenericTableConfigurationProvider.showColumns(columnNames);
		return TableConfigurationFactory.combine(configuredTableConfiguration, exportTableConfiguration,
			compareTableConfiguration, declareVisibleColumns, ensureColumnsVisible);
	}

	private CommandHandler exportHandler(StructureEditComponent<?> structureEditor) {
		String exportCommandName = ((StructureEditComponent.Config) structureEditor.getConfig()).getExportCommand();
		if (exportCommandName == null) {
			return null;
		}
		return structureEditor.getCommandById(exportCommandName);
	}

	private TableConfigurationProvider declareColumns(final List<String> columnNames) {
		return new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				for (String columnName : columnNames) {
					if (CompareService.DECORATION_COLUMN.equals(columnName)) {
						/* Do not declare decoration column here, as otherwise special settings for
						 * the column are not applied. */
						continue;
					}
					table.declareColumn(columnName);
				}
			}
		};
	}

	private List<String> getVisibleColumns(StructureEditComponent<?> structureEditor) {
		List<String> columnNames = structureEditor.getTableDeclaration().getColumnNames();
		List<String> defaultColumns = new ArrayList<>();
		defaultColumns.add(CompareService.DECORATION_COLUMN);
		for (String colName : columnNames) {
			/* Default column is the tree column. It is supposed that the name column is the
			 * corresponding tree column. */
			if (DefaultTableDeclaration.DEFAULT_COLUMN_NAME.equals(colName)) {
				defaultColumns.add(TLNamed.NAME_ATTRIBUTE);
			} else {
				defaultColumns.add(colName);
			}
		}
		return defaultColumns;
	}

}

