/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.function.Supplier;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.tree.TreeTableDataOwner;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;

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

		/** Name of the {@link #isMultiple()} property. */
		String TREE_SELECTION_PROPERTY_NAME = "tree-selection";

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
		 * Whether the created {@link SelectionModel} supports tree selection, i.e. when a node in a
		 * tree is selected, that means that all children are selected.
		 */
		@Name(TREE_SELECTION_PROPERTY_NAME)
		boolean isTreeSelection();

		/**
		 * Setter for {@link #isTreeSelection()}.
		 */
		void setTreeSelection(boolean value);

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
	public SelectionModel newSelectionModel(SelectionModelOwner owner) {
		Filter<Object> selectionFilter = getSelectionFilter();
		if (_config.isMultiple()) {
			if (_config.isTreeSelection()) {
				TreeTableDataOwner treeOwner = (TreeTableDataOwner) owner;
				Supplier<TLTreeModel<?>> treeSupplier = () -> {
					/* Table data may change during lifetime of selection model , e.g. when the
					 * owner is a GridComponent. */
					return treeOwner.getTableData().getTree();
				};
				SubtreeSelectionModel<?> selectionModel = new LazyTreeSelectionModel(owner,
					AbstractTreeTableModel.AbstractTreeTableNode.class, treeSupplier);
				selectionModel.setSelectionFilter(selectionFilter);
				return selectionModel;
					
			} else {
				return new DefaultMultiSelectionModel(selectionFilter, owner);
			}
		} else {
			return new DefaultSingleSelectionModel(selectionFilter, owner);
		}
	}

	/**
	 * Optional filter to restrict selection events.
	 */
	protected Filter<Object> getSelectionFilter() {
		return _config.getFilter();
	}

	@Override
	public PolymorphicConfiguration<?> getConfig() {
		return _config;
	}

}

