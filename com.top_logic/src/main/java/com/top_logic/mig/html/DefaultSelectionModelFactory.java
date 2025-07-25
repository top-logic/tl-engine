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
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.TreeModelOwner;
import com.top_logic.tool.boundsec.CommandHandler;

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
		 * @see #withTreeSemantics()
		 */
		String TREE_SEMANTICS = "treeSemantics";

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
		 * Whether to create a selection model that provides enhanced information and operations for
		 * tree structures.
		 * 
		 * <p>
		 * A selection model with tree semantics must only be used, if the underlying model is a
		 * tree model.
		 * </p>
		 * 
		 * <p>
		 * This option is only relevant, for a multi-selection model.
		 * </p>
		 */
		@Name(TREE_SEMANTICS)
		@DynamicMode(fun = CommandHandler.ConfirmConfig.VisibleIf.class, args = @Ref(MULTIPLE_PROPERTY_NAME))
		boolean withTreeSemantics();

		/**
		 * Returns the selection filter for the created {@link SelectionModel}.
		 */
		@InstanceFormat
		@Name(FILTER_PROPERTY_NAME)
		Filter<?> getFilter();

		/**
		 * Setter for {@link #getFilter()}.
		 */
		void setFilter(Filter<?> filter);
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
	public SelectionModel<?> newSelectionModel(SelectionModelOwner owner) {
		Filter<?> selectionFilter = _config.getFilter();
		if (_config.isMultiple()) {
			if (_config.withTreeSemantics()) {
				TreeModelOwner treeModelOwner;
				if (owner instanceof TreeModelOwner treeOwner) {
					treeModelOwner = treeOwner;
				} else {
					// Try to extract from table data.
					TableDataOwner tableOwner = (TableDataOwner) owner;
					treeModelOwner = () -> ((TreeTableData) tableOwner.getTableData()).getTree();
				}
				return new DefaultTreeMultiSelectionModel<>(selectionFilter, owner, treeModelOwner);
			}
			return new DefaultMultiSelectionModel<>(selectionFilter, owner);
		} else {
			return new DefaultSingleSelectionModel<>(selectionFilter, owner);
		}
	}

	@Override
	public PolymorphicConfiguration<?> getConfig() {
		return _config;
	}

}

