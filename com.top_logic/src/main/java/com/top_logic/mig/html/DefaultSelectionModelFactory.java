/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.function.Supplier;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
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
	 * Configuration of this factory.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
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
			if (_config.isTreeSelection()) {
				TreeTableDataOwner treeOwner = (TreeTableDataOwner) owner;
				Supplier<TLTreeModel<?>> treeSupplier = () -> {
					/* Table data may change during lifetime of selection model , e.g. when the
					 * owner is a GridComponent. */
					return treeOwner.getTableData().getTree();
				};
				TreeSelectionModel<?> selectionModel = new LazyTreeSelectionModel(owner,
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

	@Override
	public PolymorphicConfiguration<?> getConfig() {
		return _config;
	}

}

