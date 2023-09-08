/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.SetDefaultAccessor;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * Extension of {@link GridComponentTableConfigProvider} to set default column accessor.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultAccessorTableConfigProvider extends GridComponentTableConfigProvider
		implements ConfiguredInstance<DefaultAccessorTableConfigProvider.Config> {

	/**
	 * Configuration of the {@link DefaultAccessorTableConfigProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<GridComponentTableConfigProvider> {

		/** Configuration name of {@link #getDefaultAccessorClass()}. */
		String ACCESSOR_CLASS_NAME = "accessorClass";

		/**
		 * Class of the default {@link Accessor} depending on the concrete {@link GridComponent}.
		 */
		@Mandatory
		@Name(ACCESSOR_CLASS_NAME)
		Class<? extends Accessor<?>> getDefaultAccessorClass();

	}

	private final Config _config;

	/**
	 * Creates a new {@link DefaultAccessorTableConfigProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultAccessorTableConfigProvider}.
	 */
	public DefaultAccessorTableConfigProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	protected void addTableConfigs(List<TableConfigurationProvider> configs, GridComponent grid) {
		addDefaultAccessor(configs, grid);
		super.addTableConfigs(configs, grid);
	}

	/**
	 * Sets an instance of the configured {@link Accessor} class as default
	 * {@link ColumnConfiguration#getAccessor() column accessor}.
	 */
	protected void addDefaultAccessor(List<TableConfigurationProvider> configs, GridComponent grid) {
		Accessor<?> defaultAccessor;
		try {
			defaultAccessor = ConfigUtil.getInstance(Accessor.class, Config.ACCESSOR_CLASS_NAME,
				getConfig().getDefaultAccessorClass(), new Class[] { GridComponent.class }, grid);
		} catch (ConfigurationException ex) {
			Logger.error("Unable to get default accessor for grid '" + grid.getName() + "'.", ex,
				DefaultAccessorTableConfigProvider.class);
			return;
		}
		configs.add(new SetDefaultAccessor(defaultAccessor));
	}

}

