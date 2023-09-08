/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.List;

import com.top_logic.layout.basic.ScrollContainerControl.ScrollRange;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * {@link ScrollRange} that defines the neighbourhood of a node as scroll range.
 * 
 * <p>
 * The range is the subtree of a given node including the node rendered directly before the given
 * node. (It is expected that the tree has "default" GUI representation.) Therefore the nodes are
 * defined as follows:
 * <ol>
 * <li>The main element is a given tree node.</li>
 * <li>The minimum element is the element directly displayed before the the given node.</li>
 * <li>The maximum element is the rightmost displayed descendant of the the given node.</li>
 * </ol>
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeNodeRange<N> implements ScrollRange {

	private final TreeControl _tree;

	private final N _node;

	/**
	 * Creates a new {@link TreeNodeRange}.
	 * 
	 * @param control
	 *        The control displaying the given node.
	 * @param node
	 *        The "main" node. It must be displayed by the given {@link TreeControl}.
	 */
	public TreeNodeRange(TreeControl control, N node) {
		_tree = control;
		_node = node;
	}

	@Override
	public String getMinElementId() {
		N previousNode = getPreviousNode(treeModel(), _node);
		if (previousNode == null) {
			return null;
		}
		return _tree.getNodeId(previousNode);
	}

	private TreeUIModel<N> treeModel() {
		return _tree.getData().getTreeModel();
	}

	private N getPreviousNode(TreeUIModel<N> model, N node) {
		N parent = model.getParent(node);
		if (parent == null) {
			// Node is root node
			return null;
		}
		List<? extends N> children = model.getChildren(parent);
		int childIndex = children.indexOf(node);
		if (childIndex == 0) {
			if (parent == model.getRoot() && !model.isRootVisible()) {
				// node is first child of invisible root.
				return null;
			}
			return parent;
		} else {
			return getLastVisibleChild(model, children.get(childIndex - 1));
		}
	}

	private N getLastVisibleChild(TreeUIModel<N> model, N node) {
		if (!model.isExpanded(node)) {
			return node;
		}
		List<? extends N> children = model.getChildren(node);
		if (children.isEmpty()) {
			return node;
		}
		return getLastVisibleChild(model, children.get(children.size() - 1));
	}


	@Override
	public String getMainElementId() {
		return _tree.getNodeId(_node);
	}

	@Override
	public String getMaxElementId() {
		N lastChild = getLastChild(treeModel(), _node);
		if (lastChild == null) {
			return null;
		}
		return _tree.getNodeId(lastChild);
	}

	private N getLastChild(TreeUIModel<N> treeModel, N node) {
		N lastChild;
		if (treeModel.isExpanded(node)) {
			List<? extends N> children = treeModel.getChildren(node);
			if (children.isEmpty()) {
				lastChild = null;
			} else {
				lastChild = children.get(children.size() - 1);
			}
		} else {
			lastChild = null;
		}
		return lastChild;
	}
}

