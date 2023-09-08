/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.SimpleTableDataExport;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * {@link TableConfigurationProvider} for tables to use in tile environment as context table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ContextTableConfiguration extends NoDefaultColumnAdaption
		implements ConfiguredInstance<ContextTableConfiguration.Config> {

	/**
	 * Typed configuration interface definition for {@link ContextTableConfiguration}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ContextTableConfiguration> {

		/** Configuration name of {@link #getNameOfExportFile()}. */
		String NAME_OF_EXPORT_FILE = "name-of-export-file";

		/** Configuration name of {@link #getRowStyle()}. */
		String ROW_STYLE = "row-style";

		/**
		 * Style that is applied to the rows (header and content) of the table.
		 */
		@StringDefault("height: 65px;")
		@Nullable
		@Name(ROW_STYLE)
		String getRowStyle();

		/**
		 * Name of the excel export file. If not set, no export is offered.
		 */
		@Name(NAME_OF_EXPORT_FILE)
		ResKey getNameOfExportFile();

	}

	private final Config _config;

	/**
	 * Create a {@link ContextTableConfiguration}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ContextTableConfiguration(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setRowClassProvider(TileTableRowClassProvider.INSTANCE);

		String rowStyle = getConfig().getRowStyle();
		if (rowStyle != null) {
			table.setHeaderStyle(rowStyle);
			table.setRowStyle(rowStyle);
		}
		table.setTableRenderer(TypedConfigUtil.newConfiguredInstance(TileCockpitTableRenderer.class));

		ResKey downloadNameKey = getConfig().getNameOfExportFile();
		if (downloadNameKey != null) {
			SimpleTableDataExport.Config exporter;
			try {
				exporter = (SimpleTableDataExport.Config) TypedConfiguration
					.createConfigItemForImplementationClass(SimpleTableDataExport.class);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
			exporter.setTemplateName("defaultTemplate.xlsx");
			exporter.setDownloadNameKey(downloadNameKey);
			table.setExporter(TypedConfigUtil.createInstance(exporter));
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}

