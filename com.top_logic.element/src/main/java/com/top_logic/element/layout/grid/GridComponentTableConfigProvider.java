/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.ResourceView;
import com.top_logic.layout.table.component.ComponentTableConfigProvider;
import com.top_logic.layout.table.model.SetTableResPrefix;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;

/**
 * {@link ComponentTableConfigProvider} that allows programmatic modification of
 * {@link TableConfiguration} for a {@link GridComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GridComponentTableConfigProvider implements ComponentTableConfigProvider {

	/** Singleton {@link GridComponentTableConfigProvider} instance. */
	public static final GridComponentTableConfigProvider INSTANCE = new GridComponentTableConfigProvider();

	/**
	 * Creates a new {@link GridComponentTableConfigProvider}.
	 */
	protected GridComponentTableConfigProvider() {
		// singleton instance
	}

	@Override
	public final TableConfigurationProvider getTableConfigProvider(LayoutComponent context, String tableName) {
		List<TableConfigurationProvider> configs = new ArrayList<>();
		addTableConfigs(configs, (GridComponent) context);
		if (configs.isEmpty()) {
			return TableConfigurationFactory.emptyProvider();
		}
		return TableConfigurationFactory.combine(configs.toArray(new TableConfigurationProvider[configs.size()]));
	}

	/**
	 * Fills the given list of {@link TableConfigurationProvider} to build a single
	 * {@link TableConfigurationProvider} from.
	 */
	protected void addTableConfigs(List<TableConfigurationProvider> configs, GridComponent grid) {
		setTableResPrefix(configs, grid);
		addProgrammaticConfig(configs, grid);
		setColumnVisibility(configs, grid);
	}

	/**
	 * Sets the visibility of the configured columns in the {@link GridComponent}.
	 */
	protected void setColumnVisibility(List<TableConfigurationProvider> configs, GridComponent grid) {
		configs.add(GenericTableConfigurationProvider.excludeColumns(grid.excludeColumns));
	}

	/**
	 * Sets the {@link TableConfigurationProvider} of an overridden {@link GridComponent}.
	 */
	protected void addProgrammaticConfig(List<TableConfigurationProvider> configs, GridComponent grid) {
		Set<TLClass> configuredTypes = grid.getConfiguredTypes();
		if (!configuredTypes.isEmpty()) {
			configs.add(new GenericTableConfigurationProvider(configuredTypes));
		}
	}

	/**
	 * Ensures the correct {@link TableConfiguration#getResPrefix()} of the result table.
	 */
	protected void setTableResPrefix(List<TableConfigurationProvider> configs, GridComponent grid) {
		ResourceView resources = grid.getResourceView();
		if (resources != null) {
			configs.add(new SetTableResPrefix(resources));
		}
	}

}

