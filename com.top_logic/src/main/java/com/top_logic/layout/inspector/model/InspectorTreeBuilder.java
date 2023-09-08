/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.dob.DataObject;
import com.top_logic.layout.inspector.model.nodes.InspectProperty;
import com.top_logic.layout.inspector.model.nodes.InspectorArrayNode;
import com.top_logic.layout.inspector.model.nodes.InspectorCollectionNode;
import com.top_logic.layout.inspector.model.nodes.InspectorConfigNode;
import com.top_logic.layout.inspector.model.nodes.InspectorDataObjectNode;
import com.top_logic.layout.inspector.model.nodes.InspectorMapNode;
import com.top_logic.layout.inspector.model.nodes.InspectorReflectionNode;
import com.top_logic.layout.inspector.model.nodes.InspectorTLObjectNode;
import com.top_logic.layout.inspector.model.nodes.InspectorTreeNode;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.model.TLObject;

/**
 * Build up the tree of {@link InspectorTreeNode}s.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorTreeBuilder implements TreeBuilder<InspectorTreeNode> {

	/**
	 * Creates a {@link InspectorTreeBuilder}.
	 */
	public InspectorTreeBuilder() {
	}

	@Override
	public InspectorTreeNode createNode(AbstractMutableTLTreeModel<InspectorTreeNode> treeModel,
			InspectorTreeNode parent, Object userObject) {
		return makeNode(treeModel, parent, RootProperty.INSTANCE, userObject);
	}

	@Override
	public List<InspectorTreeNode> createChildList(InspectorTreeNode node) {
		return node.makeChildren();
	}

	/**
	 * Creates an {@link InspectorTreeNode}.
	 * 
	 * @param parent
	 *        The node this method has been called for to get some children.
	 * @param property
	 *        See {@link InspectorTreeNode#getProperty()}.
	 * @param userObject
	 *        The value represented by the resulting {@link InspectorTreeNode}.
	 */
	public InspectorTreeNode makeNode(AbstractMutableTLTreeModel<InspectorTreeNode> treeModel,
			InspectorTreeNode parent, InspectProperty property, Object userObject) {
		if (userObject instanceof TLObject && hasType(userObject)) {
			return new InspectorTLObjectNode(treeModel, parent, property, (TLObject) userObject);
		}
		else if (userObject instanceof DataObject) {
			return new InspectorDataObjectNode(treeModel, parent, property, (DataObject) userObject);
		}
		else if (userObject instanceof Collection<?>) {
			return new InspectorCollectionNode(treeModel, parent, property, (Collection<?>) userObject);
		}
		else if (userObject instanceof Map<?, ?>) {
			return new InspectorMapNode(treeModel, parent, property, (Map<?, ?>) userObject);
		}
		else if (userObject instanceof ConfigurationItem) {
			return new InspectorConfigNode(treeModel, parent, property, (ConfigurationItem) userObject);
		}
		else if (userObject instanceof Object[]) {
			return new InspectorArrayNode(treeModel, parent, property, (Object[]) userObject);
		}
		return new InspectorReflectionNode(treeModel, parent, property, userObject);
	}

	private boolean hasType(Object object) {
		try {
			return ((TLObject) object).tType() != null;
		} catch (Exception ex) {
			// Workaround for not being able to access tType() for all kinds of objects (e.g.
			// Branches).
			return false;
		}
	}

	@Override
	public boolean isFinite() {
		return false;
	}

	private static final class RootProperty extends InspectProperty {

		/**
		 * Singleton {@link RootProperty} instance.
		 */
		public static final RootProperty INSTANCE = new RootProperty();

		private RootProperty() {
			// Singleton constructor.
		}

		@Override
		public Object staticType() {
			return null;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public Object getValue() {
			return null;
		}
	}

}
