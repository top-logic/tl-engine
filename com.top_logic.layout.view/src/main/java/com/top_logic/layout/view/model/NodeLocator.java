/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;

/**
 * Finds the node of a tree standing for a business object.
 *
 * <p>
 * A display addresses a node by the object behind it - the object a channel holds, the object a
 * command just created - and the way from that object to its node depends on what is known about
 * the tree: a tree that can say what holds an object walks from the object up, a tree that cannot
 * is searched.
 * </p>
 *
 * @see TreeNodes
 */
public interface NodeLocator {

	/**
	 * The node of the given tree standing for the given business object.
	 *
	 * @param root
	 *        The root of the tree to look in, typically {@link DefaultTreeUINodeModel#getRoot()}.
	 * @param businessObject
	 *        The object whose node is looked for.
	 * @return The node standing for the given object, {@code null} where the tree holds none.
	 */
	DefaultTreeUINode locate(DefaultTreeUINode root, Object businessObject);

	/**
	 * Locator searching the tree for the node of an object.
	 *
	 * <p>
	 * The part of the tree that was computed already is looked through first, and only an object
	 * that is not in it makes the search compute the child lists it passes - which is affordable for
	 * a tree whose extent is small, and not for a large or an unbounded one. Such a tree is located
	 * in by {@link #byParents(Function)}.
	 * </p>
	 */
	NodeLocator SEARCHING = (root, businessObject) -> {
		DefaultTreeUINode computed = TreeNodes.findComputedNode(root, businessObject);
		return computed != null ? computed : TreeNodes.findNode(root, businessObject);
	};

	/**
	 * Locator walking from an object up to the root of the tree, and descending along that chain.
	 *
	 * <p>
	 * Only the nodes on the way from the root to the object are computed, so the cost of finding a
	 * node is the depth of the tree instead of its extent.
	 * </p>
	 *
	 * <p>
	 * An object whose chain does not reach the object the tree is built from is not in this tree,
	 * and so is one whose chain reaches it but which no child list on the way holds.
	 * </p>
	 *
	 * @param parentFunction
	 *        What holds the given object in the tree, {@code null} for the object the tree is built
	 *        from and for an object belonging to no tree at all.
	 * @return The locator over that function.
	 */
	static NodeLocator byParents(Function<Object, Object> parentFunction) {
		return (root, businessObject) -> {
			Object rootObject = root.getBusinessObject();

			// The objects from the given one up to, but not including, the one the tree is built
			// from.
			List<Object> path = new ArrayList<>();
			Set<Object> seen = new HashSet<>();
			Object current = businessObject;
			while (current != null && !Objects.equals(current, rootObject)) {
				if (!seen.add(current)) {
					// A chain biting its own tail reaches no root.
					return null;
				}
				path.add(current);
				current = parentFunction.apply(current);
			}
			if (current == null) {
				return null;
			}

			DefaultTreeUINode node = root;
			for (int n = path.size() - 1; n >= 0; n--) {
				node = childFor(node, path.get(n));
				if (node == null) {
					// The child list does not hold what the chain says it holds.
					return null;
				}
			}
			return node;
		};
	}

	/**
	 * The child of the given node standing for the given business object, {@code null} where the
	 * child list computed for the node holds no such object.
	 */
	private static DefaultTreeUINode childFor(DefaultTreeUINode node, Object businessObject) {
		for (DefaultTreeUINode child : node.getChildren()) {
			if (Objects.equals(child.getBusinessObject(), businessObject)) {
				return child;
			}
		}
		return null;
	}

}
