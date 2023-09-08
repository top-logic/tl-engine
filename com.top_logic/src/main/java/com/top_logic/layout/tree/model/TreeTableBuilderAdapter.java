/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * Adapter for using a {@link TreeView} as {@link DefaultTreeTableBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeTableBuilderAdapter<N> extends DefaultTreeTableBuilder {

	/**
	 * Configuration options for {@link TreeTableBuilderAdapter}.
	 */
	public interface Config extends PolymorphicConfiguration<TreeTableBuilderAdapter<?>> {

		/**
		 * The {@link TreeView} for resolving child objects.
		 */
		@Name("treeView")
		PolymorphicConfiguration<TreeView<?>> getTreeView();

	}

	private final TreeView<N> _builder;

	/**
	 * Creates a {@link TreeTableBuilderAdapter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TreeTableBuilderAdapter(InstantiationContext context, Config config) {
		@SuppressWarnings("unchecked")
		TreeView<N> builder = (TreeView<N>) context.getInstance(config.getTreeView());
		_builder = builder;
	}
	
	/**
	 * Create a new {@link TreeTableBuilderAdapter}.
	 * 
	 * @param sourceTree
	 *        - {@link TreeView}, that holds the nodes which shall be created by this adapter.
	 */
	@SuppressWarnings("unchecked")
	public TreeTableBuilderAdapter(TreeView<?> sourceTree) {
		_builder = (TreeView<N>) sourceTree;
	}
	
	/**
	 * The internal {@link TreeView} providing the tree structure.
	 */
	public TreeView<N> getBuilder() {
		return _builder;
	}

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		@SuppressWarnings("unchecked")
		N nodeModel = (N) node.getBusinessObject();
		Iterator<? extends N> childIt = getBuilder().getChildIterator(nodeModel);
		ArrayList<DefaultTreeTableNode> result = new ArrayList<>();
		while (childIt.hasNext()) {
			N childModel = childIt.next();
			result.add(createNode(node.getModel(), node, childModel));
		}
		return result;
	}

	@Override
	public boolean isFinite() {
		return getBuilder().isFinite();
	}

}
