/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TreeBuilder} dispatching to the wrapped {@link TreeModelBuilder}.
 * 
 * @see TreeBuilder
 * @see TreeModelBuilder
 * @see TreeTableComponent
 * 
 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
 */
public final class TreeBuilderAdaptor
		implements TreeBuilder<DefaultTreeTableNode>, ConfiguredInstance<TreeBuilderAdaptor.Config> {

	private LayoutComponent _contextComponent;

	private final TreeModelBuilder<Object> _treeModelBuilder;

	private final Config _config;

	/**
	 * Configuration of the {@link TreeBuilderAdaptor}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends PolymorphicConfiguration<TreeBuilderAdaptor> {

		/**
		 * Configuration attribute name for {@link #getTreeModelBuilder()}.
		 */
		String TREE_MODEL_BUILDER = "treeModelBuilder";

		/**
		 * {@link ModelBuilder} providing a tree.
		 */
		@Name(TREE_MODEL_BUILDER)
		PolymorphicConfiguration<? extends TreeModelBuilder<Object>> getTreeModelBuilder();
	}

	/**
	 * Creates a {@link TreeBuilderAdaptor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TreeBuilderAdaptor(InstantiationContext context, Config config) {
		_config = config;
		_treeModelBuilder = context.getInstance(config.getTreeModelBuilder());

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, component -> _contextComponent = component);
	}

	@Override
	public DefaultTreeTableNode createNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
			DefaultTreeTableNode parent, Object userObject) {
		return new DefaultTreeTableNode(model, parent, userObject);
	}

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		if (isLeaf(node)) {
			return new ArrayList<>(0);
		} else {
			return getChildren(node);
		}
	}

	private List<DefaultTreeTableNode> getChildren(DefaultTreeTableNode node) {
		ArrayList<DefaultTreeTableNode> childNodes = new ArrayList<>();

		Iterator<? extends Object> childIterator = getChildIterator(node.getBusinessObject());

		while (childIterator.hasNext()) {
			childNodes.add(createNode(node.getModel(), node, childIterator.next()));
		}

		return childNodes;
	}

	private boolean isLeaf(DefaultTreeTableNode node) {
		return _treeModelBuilder.isLeaf(_contextComponent, node.getBusinessObject());
	}

	private Iterator<? extends Object> getChildIterator(Object businessObject) {
		return _treeModelBuilder.getChildIterator(_contextComponent, businessObject);
	}

	@Override
	public boolean isFinite() {
		return _treeModelBuilder.canExpandAll();
	}

	/**
	 * Wrapped {@link TreeModelBuilder}.
	 */
	public TreeModelBuilder<Object> getTreeModelBuilder() {
		return _treeModelBuilder;
	}

	@Override
	public Config getConfig() {
		return _config;
	}
}
