/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * External index of {@link TLTreeNode}s by their user objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UserObjectIndex<N extends AbstractMutableTLTreeNode<N>> {

	/**
	 * Space optimized variant of <code>HashMap<Object, List<N>></code>.
	 * 
	 * <p>
	 * As long as a user object is represented by exactly one {@link DefaultMutableTLTreeNode}, the
	 * value of the {@link #nodesByUserObject} map is this unique node. Only if the same object
	 * occurs more than once in this {@link TLTreeModel}, the object is mapped to a
	 * {@link LocalList} of {@link DefaultMutableTLTreeNode}s.
	 * </p>
	 */
	private final Map<Object, Object> nodesByUserObject = new HashMap<>();

	/**
	 * Update nodes representing the given user object.
	 * 
	 * After calling this methods all changes manually done with the child list of the corresponding
	 * node (e.g. removing child or adding one) are skipped.
	 * 
	 * @param userObject
	 *        The user object that should be re-mapped to tree nodes.
	 * @return Whether potential changes have occurred to this model.
	 */
	public boolean updateUserObject(Object userObject) {
		Object nodeOrNodes = nodesByUserObject.get(userObject);
		if (nodeOrNodes == null) {
			// Not cached.
			return false;
		}

		if (nodeOrNodes instanceof LocalList) {
			@SuppressWarnings("unchecked")
			LocalList<N> nodeList = (LocalList<N>) nodeOrNodes;
			final List<N> copy = new ArrayList<>(nodeList);
			for (N node : copy) {
				if (!node.isAlive()) {
					// Might happen, if the given user object is mapped to
					// different nodes and update occurs top-down.
					continue;
				}
				node.updateNodeStructure();
			}
		} else {
			@SuppressWarnings("unchecked")
			N node = (N) nodeOrNodes;
			node.updateNodeStructure();
		}

		return true;
	}

	/**
	 * Deletes all nodes in the indexed model that have the given object as
	 * {@link TLTreeNode#getBusinessObject()}.
	 * 
	 * @param userObject
	 *        The deleted business object.
	 * @return Whether some node was deleted.
	 */
	public boolean deleteNodesWithUserObject(Object userObject) {
		Object nodeOrNodes = nodesByUserObject.get(userObject);
		if (nodeOrNodes == null) {
			// Not cached.
			return false;
		}

		if (nodeOrNodes instanceof LocalList) {
			@SuppressWarnings("unchecked")
			LocalList<N> nodeList = (LocalList<N>) nodeOrNodes;
			final List<N> copy = new ArrayList<>(nodeList);
			for (N node : copy) {
				if (!node.isAlive()) {
					// Might happen, if the given user object is mapped to
					// different nodes and update occurs top-down.
					continue;
				}
				delete(node);
			}
		} else {
			@SuppressWarnings("unchecked")
			N node = (N) nodeOrNodes;
			delete(node);
		}

		return true;
	}

	private void delete(N node) {
		N parent = node.getParent();
		parent.removeChild(parent.getIndex(node));
	}

	/**
	 * All initialized nodes of the model that have the given user object.
	 * 
	 * @see TLTreeNode#getBusinessObject()
	 */
	public List<N> getNodes(Object userObject) {
		Object nodeOrNodes = nodesByUserObject.get(userObject);
		if (nodeOrNodes == null) {
			// Not cached.
			return Collections.emptyList();
		} else if (nodeOrNodes instanceof LocalList) {
			@SuppressWarnings("unchecked")
			LocalList<N> nodeList = (LocalList<N>) nodeOrNodes;
			return new ArrayList<>(nodeList);
		} else {
			@SuppressWarnings("unchecked")
			N node = (N) nodeOrNodes;
			return Collections.singletonList(node);
		}
	}

	/**
	 * Must be called from
	 * {@link AbstractMutableTLTreeModel#handleRemoveNode(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)}
	 * .
	 * 
	 * @param subtreeRootParent
	 *        See
	 *        {@link AbstractMutableTLTreeModel#handleRemoveNode(AbstractMutableTLTreeNode, AbstractMutableTLTreeNode)}
	 *        .
	 */
	public void handleRemoveNode(N subtreeRootParent, N node) {
		Object userObject = node.getBusinessObject();

		Object nodes = nodesByUserObject.get(userObject);
		if (nodes == null) {
			return;
		}
		if (nodes == node) {
			// Unique binding: Remove.
			nodesByUserObject.remove(userObject);
			return;
		}

		LocalList<?> nodeList;
		if (nodes instanceof LocalList) {
			nodeList = (LocalList<?>) nodes;
			if (nodeList.remove(node)) {
				if (nodeList.isEmpty()) {
					// All nodes of given user object were removed.
					nodesByUserObject.remove(userObject);
					return;
				}
			}
		} else {
			// Other binding still there.
		}
	}

	/**
	 * Must be called from
	 * {@link AbstractMutableTLTreeModel#handleInitNode(AbstractMutableTLTreeNode)}
	 */
	public void handleInitNode(N node) {
		Object clash = nodesByUserObject.put(node.getBusinessObject(), node);
		if (clash == null) {
			return;
		}
		if (clash == node) {
			return;
		}

		LocalList<N> clashList;
		if (clash instanceof LocalList<?>) {
			@SuppressWarnings("unchecked")
			LocalList<N> oldList = (LocalList<N>) clash;
			clashList = oldList;
			if (clashList.contains(node)) {
				// reinstall clash list
				nodesByUserObject.put(node.getBusinessObject(), clashList);
				return;
			}
		} else {
			clashList = new LocalList<>();
			@SuppressWarnings("unchecked")
			N oldNode = (N) clash;
			clashList.add(oldNode);
		}
		clashList.add(node);
		nodesByUserObject.put(node.getBusinessObject(), clashList);

	}

	/**
	 * Own list type that is never handed out.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static class LocalList<T> extends ArrayList<T> {
		public LocalList() {
			super(2);
		}
	}

}
