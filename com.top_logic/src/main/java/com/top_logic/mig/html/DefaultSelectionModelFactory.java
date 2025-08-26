/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.TreeModelOwner;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
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
	 * Configuration of {@link DefaultSelectionModelFactory}.
	 */
	public interface Config extends PolymorphicConfiguration<DefaultSelectionModelFactory> {

		/** Name of the {@link #isMultiple()} property. */
		String MULTIPLE_PROPERTY_NAME = "multiple";

		/** Name of the {@link #isSubtreeSelection()} property. */
		String SUBTREE_SELECTION_PROPERTY_NAME = "subtree-selection";

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
		 * Whether the selection represents whole subtrees. When a node is selected, that means that
		 * all of its children are (implicitly) selected.
		 */
		@Name(SUBTREE_SELECTION_PROPERTY_NAME)
		@DynamicMode(fun = CommandHandler.ConfirmConfig.VisibleIf.class, args = @Ref(TREE_SEMANTICS))
		boolean isSubtreeSelection();

		/**
		 * Setter for {@link #isSubtreeSelection()}.
		 */
		void setSubtreeSelection(boolean value);

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

	/**
	 * Options of {@link DefaultSelectionModelFactory} that can be configured in the layout editor.
	 * 
	 * @implNote This will replace properties in {@link Config}, but requires migration. Will be
	 *           further refactored in a future change. Currently, this interface is only used as
	 *           super interface for typed layout templates.
	 */
	@CalledByReflection
	public interface UIOptions extends ConfigurationItem {
		/** Name of the {@link #isMultiSelection()} property. */
		String MULTI_SELECTION_PROPERTY_NAME = "multiSelection";

		/** Name of the {@link #getSelectionFilter()} property. */
		String SELECTION_FILTER_PROPERTY_NAME = "selectionFilter";

		/**
		 * Whether multiple value can be selected.
		 */
		@Name(MULTI_SELECTION_PROPERTY_NAME)
		boolean isMultiSelection();

		/**
		 * @see #isMultiSelection()
		 */
		void setMultiSelection(boolean multiple);

		/**
		 * Filter that decides, whether some value can be selected.
		 * 
		 * <p>
		 * Only values, that are accepted by the filter can be selected.
		 * </p>
		 */
		@InstanceFormat
		@Name(SELECTION_FILTER_PROPERTY_NAME)
		@Options(fun = AllInAppImplementations.class)
		Filter<Object> getSelectionFilter();

		/**
		 * Setter for {@link #getSelectionFilter()}.
		 */
		void setSelectionFilter(Filter<Object> filter);
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
		Filter<Object> selectionFilter = getSelectionFilter();
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

				if (_config.isSubtreeSelection()) {
					SubtreeSelectionModel<?> selectionModel = new LazyTreeSelectionModel(owner,
						AbstractTreeTableModel.AbstractTreeTableNode.class, treeModelOwner);
					selectionModel.setSelectionFilter(selectionFilter);
					return selectionModel;
				} else {
					return new DefaultTreeMultiSelectionModel<>(selectionFilter, owner, treeModelOwner);
				}
			} else {
				return new DefaultMultiSelectionModel<>(selectionFilter, owner);
			}
		} else {
			return new DefaultSingleSelectionModel<>(selectionFilter, owner);
		}
	}

	/**
	 * Optional filter to restrict selection events.
	 */
	@SuppressWarnings("unchecked")
	protected Filter<Object> getSelectionFilter() {
		return (Filter<Object>) _config.getFilter();
	}

	@Override
	public PolymorphicConfiguration<?> getConfig() {
		return _config;
	}

}

