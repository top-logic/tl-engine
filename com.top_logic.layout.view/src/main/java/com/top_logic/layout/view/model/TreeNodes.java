/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;

/**
 * Operations on the nodes of a {@link DefaultTreeUINodeModel} that address a node by the business
 * object it stands for.
 *
 * <p>
 * A tree built anew has other nodes than the one the display was working with, so everything such a
 * rebuild carries over - which subtrees were open, which node to select - is expressed by the
 * business objects behind the nodes, which are the same objects in both trees.
 * </p>
 *
 * @see ObservableTreeModel
 */
public class TreeNodes {

	/**
	 * Not instantiated: a namespace for operations over the nodes of a tree.
	 */
	private TreeNodes() {
		// No instances.
	}

	/**
	 * The business objects of the nodes whose subtree is open.
	 *
	 * @param root
	 *        The node to start at, typically {@link DefaultTreeUINodeModel#getRoot()}.
	 * @return The objects a tree built anew opens again, see
	 *         {@link #restoreExpansion(DefaultTreeUINode, Set)}.
	 */
	public static Set<Object> collectExpanded(DefaultTreeUINode root) {
		Set<Object> expanded = new HashSet<>();
		collectExpanded(root, expanded);
		return expanded;
	}

	private static void collectExpanded(DefaultTreeUINode node, Set<Object> expanded) {
		if (!node.isExpanded()) {
			return;
		}
		expanded.add(node.getBusinessObject());

		// Only an open node has its children created, so the descent stops where the tree was
		// closed anyway.
		for (DefaultTreeUINode child : node.getChildren()) {
			collectExpanded(child, expanded);
		}
	}

	/**
	 * Opens the subtrees of the nodes standing for the given business objects.
	 *
	 * <p>
	 * The children of the given root are visited whether it is open or not: the root stands for the
	 * tree as a whole, whose top level is displayed even where the root node itself is not. Below
	 * that top level the descent follows the nodes that are opened, so a closed subtree is not
	 * computed.
	 * </p>
	 *
	 * @param root
	 *        The node to start at, typically {@link DefaultTreeUINodeModel#getRoot()}.
	 * @param expanded
	 *        The business objects whose nodes are to be open, see
	 *        {@link #collectExpanded(DefaultTreeUINode)}.
	 */
	public static void restoreExpansion(DefaultTreeUINode root, Set<Object> expanded) {
		restoreExpansion(root, expanded, true);
	}

	private static void restoreExpansion(DefaultTreeUINode node, Set<Object> expanded, boolean root) {
		boolean open = expanded.contains(node.getBusinessObject());
		if (open) {
			node.setExpanded(true);
		}
		if (!open && !root) {
			return;
		}

		// Opening a node creates its children, so they can be visited afterwards.
		for (DefaultTreeUINode child : node.getChildren()) {
			restoreExpansion(child, expanded, false);
		}
	}

	/**
	 * The node of the given subtree standing for the given business object.
	 *
	 * <p>
	 * The search computes the child list of every node it passes, as
	 * {@link DefaultTreeUINode#getChildren()} does regardless of whether the node is open, so an
	 * object below a closed subtree is found as well - at the price of building that subtree. Where
	 * only the part of the tree that was computed already is of interest, use
	 * {@link #findComputedNode(DefaultTreeUINode, Object)}.
	 * </p>
	 *
	 * @param root
	 *        The node to start at, typically {@link DefaultTreeUINodeModel#getRoot()}.
	 * @param businessObject
	 *        The object whose node is looked for.
	 * @return The node standing for the given object, {@code null} where the tree holds none.
	 */
	public static DefaultTreeUINode findNode(DefaultTreeUINode root, Object businessObject) {
		if (Objects.equals(root.getBusinessObject(), businessObject)) {
			return root;
		}
		for (DefaultTreeUINode child : root.getChildren()) {
			DefaultTreeUINode found = findNode(child, businessObject);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * The node of the given subtree standing for the given business object, looked for among the
	 * nodes whose children were computed already.
	 *
	 * <p>
	 * A node whose children nobody asked for keeps them uncomputed, so an object that would appear
	 * only there is not found. That is what an object change needs: a part of the tree nobody
	 * opened is computed when somebody does, and building it to answer a change nobody sees is
	 * wasted work.
	 * </p>
	 *
	 * @param root
	 *        The node to start at, typically {@link DefaultTreeUINodeModel#getRoot()}.
	 * @param businessObject
	 *        The object whose node is looked for.
	 * @return The node standing for the given object, {@code null} where the computed part of the
	 *         tree holds none.
	 */
	public static DefaultTreeUINode findComputedNode(DefaultTreeUINode root, Object businessObject) {
		if (Objects.equals(root.getBusinessObject(), businessObject)) {
			return root;
		}
		if (!root.isInitialized()) {
			return null;
		}
		for (DefaultTreeUINode child : root.getChildren()) {
			DefaultTreeUINode found = findComputedNode(child, businessObject);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Opens the ancestors of the given node, so that it is displayed, leaving the node itself as it
	 * is.
	 *
	 * @param node
	 *        The node to make visible.
	 */
	public static void revealNode(DefaultTreeUINode node) {
		for (DefaultTreeUINode parent = node.getParent(); parent != null; parent = parent.getParent()) {
			parent.setExpanded(true);
		}
	}

}
