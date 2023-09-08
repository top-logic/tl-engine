/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link SelectionModelFactory} that creates "default" {@link SelectionModel}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultSelectionModelFactory extends SelectionModelFactory implements
		ConfiguredInstance<PolymorphicConfiguration<?>> {
	
	/**
	 * Configuration of this factory.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DefaultSelectionModelFactory> {

		/** Name of the {@link #isMultiple()} property. */
		String MULTIPLE_PROPERTY_NAME = "multiple";

		/** Name of the {@link #getFilter()} property. */
		String FILTER_PROPERTY_NAME = "filter";

		/**
		 * Whether the created {@link SelectionModel} must support multiple selection.
		 */
		@Name(MULTIPLE_PROPERTY_NAME)
		boolean isMultiple();

		/**
		 * Setter for {@link #isMultiple()}.
		 */
		void setMultiple(boolean multiple);

		/**
		 * Returns the selection filter for the created {@link SelectionModel}.
		 */
		@InstanceFormat
		@Name(FILTER_PROPERTY_NAME)
		Filter getFilter();

		/**
		 * Setter for {@link #getFilter()}.
		 */
		void setFilter(Filter filter);
	}

	private final Config _config;

	/**
	 * Creates a new {@link DefaultSelectionModelFactory} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultSelectionModelFactory}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public DefaultSelectionModelFactory(InstantiationContext context, Config config) throws ConfigurationException {
		_config = config;
	}

	@Override
	public SelectionModel newSelectionModel(SelectionModelOwner owner) {
		Filter selectionFilter = _config.getFilter();
		if (_config.isMultiple()) {
			return new DefaultMultiSelectionModel(selectionFilter, owner);
		} else {
			return new DefaultSingleSelectionModel(selectionFilter, owner);
		}
	}

	@Override
	public PolymorphicConfiguration<?> getConfig() {
		return _config;
	}

}

