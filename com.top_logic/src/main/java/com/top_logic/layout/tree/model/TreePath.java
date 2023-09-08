/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.util.Utils;

/**
 * A path in a {@link TLTreeModel} expressed as list of
 * {@link TLTreeModel#getBusinessObject(Object) user objects} of the path nodes.
 * 
 * <p>
 * This class assumes that user objects of all sibling nodes are unique. This means that the same
 * user object may occur multiple times in the tree, but only once within a the children nodes of
 * each parent node.
 * </p>
 * 
 * <p>
 * A {@link TreePath} can be used as stable identifier for a node in a {@link TLTreeModel} that
 * survives technically re-building parts of the model (creating new technical node objects) but
 * keeping the same user objects.
 * </p>
 * 
 * @see #fromNode(TLTreeModel, Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TreePath {

	private static final TreePath ROOT = new Path(Collections.emptyList());

	/**
	 * Converts this {@link TreePath} back to the corresponding node in the given
	 * {@link TLTreeModel}.
	 * 
	 * @param tree
	 *        The {@link TLTreeModel} to resolve this path in.
	 * @return The unique node in the given {@link TLTreeModel} that has the user objects of this
	 *         {@link TreePath} as user objects in the nodes of its path from the root node.
	 *         <code>null</code>, if no such node is found in the given {@link TLTreeModel}.
	 */
	public abstract <N> N toNode(TLTreeModel<N> tree);

	/**
	 * A {@link TreePath} identifying the parent node of the node identified by this
	 * {@link TreePath}.
	 * 
	 * <p>
	 * Note: If this {@link TreePath} identifies the root node, the resulting path identifies no
	 * node (the {@link #toNode(TLTreeModel)} method of the resulting path retuns always
	 * <code>null</code>).
	 * </p>
	 */
	public abstract TreePath getParent();

	/**
	 * Whether this path identifies the root node of any {@link TLTreeModel}.
	 */
	public abstract boolean isRoot();

	/**
	 * Whether this path may identify some node.
	 * 
	 * @return Whether {@link #toNode(TLTreeModel)} has a chance to return a non-<code>null</code>
	 *         value.
	 */
	public abstract boolean isValid();

	/**
	 * Creates a {@link TreePath} identifying the given node.
	 * 
	 * @param tree
	 *        The {@link TLTreeModel} to identify the node in.
	 * @param node
	 *        A node of the given {@link TLTreeModel}.
	 * @return A {@link TreePath} {@link #toNode(TLTreeModel) resolving} to the given node, if the
	 *         given {@link TLTreeModel} is not structurally changed in between.
	 */
	public static <N> TreePath fromNode(TLTreeModel<N> tree, N node) {
		if (!tree.containsNode(node)) {
			return None.INSTANCE;
		}
		N root = tree.getRoot();
		if (node == root) {
			return ROOT;
		}
		ArrayList<Object> path = new ArrayList<>();
		while (node != root) {
			path.add(tree.getBusinessObject(node));
			node = tree.getParent(node);
		}
		Collections.reverse(path);
		return new Path(path);
	}

	private static class None extends TreePath {

		/**
		 * Singleton {@link TreePath.None} instance.
		 */
		public static final TreePath.None INSTANCE = new TreePath.None();

		private None() {
			// Singleton constructor.
		}

		@Override
		public <N> N toNode(TLTreeModel<N> tree) {
			return null;
		}

		@Override
		public TreePath getParent() {
			return this;
		}

		@Override
		public boolean isRoot() {
			return false;
		}

		@Override
		public boolean isValid() {
			return false;
		}
	}

	private static class Path extends TreePath {

		private final List<Object> _userObjects;

		private final int _size;

		public Path(List<Object> path) {
			this(path, path.size());
		}

		private Path(List<Object> path, int size) {
			_userObjects = path;
			_size = size;
		}

		@Override
		public <N> N toNode(TLTreeModel<N> tree) {
			N node = tree.getRoot();
			for (int n = 0, cnt = _size; n < cnt; n++) {
				Object userObject = _userObjects.get(n);
				node = findNode(tree, tree.getChildren(node), userObject);
				if (node == null) {
					return null;
				}
			}
			return node;
		}

		private <N> N findNode(TLTreeModel<N> tree, List<? extends N> nodes, Object userObject) {
			for (N node : nodes) {
				if (Utils.equals(tree.getBusinessObject(node), userObject)) {
					return node;
				}
			}
			return null;
		}

		@Override
		public TreePath getParent() {
			if (_size == 0) {
				return None.INSTANCE;
			}
			return new Path(_userObjects, _size - 1);
		}

		@Override
		public boolean isRoot() {
			return _size == 0;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public int hashCode() {
			return toList().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Path other = (Path) obj;
			return toList().equals(other.toList());
		}

		private List<Object> toList() {
			return _userObjects.subList(0, _size);
		}

	}

}
