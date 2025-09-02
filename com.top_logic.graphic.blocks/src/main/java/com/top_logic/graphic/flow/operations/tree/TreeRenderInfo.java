/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Temporary information for tree layouts.
 */
public class TreeRenderInfo {

	static class Column {
		List<Box> _boxes = new ArrayList<>();

		double _width;

		/**
		 * The width of this column (maximum width of nodes placed in this column).
		 */
		double getWidth() {
			return _width;
		}

		/**
		 * All layouted boxes that are placed in this column.
		 */
		List<Box> getBoxes() {
			return _boxes;
		}

		/**
		 * TODO
		 *
		 * @param node
		 */
		void addBox(Box node) {
			_boxes.add(node);
		}

		/**
		 * TODO
		 *
		 * @return
		 */
		int size() {
			return _boxes.size();
		}

		/**
		 * TODO
		 *
		 * @param i
		 * @return
		 */
		Box getBox(int i) {
			return _boxes.get(i);
		}
	}

	private final List<Column> _columns = new ArrayList<>();

	private Map<Box, Box> _nodeForAnchor;

	private Map<Box, Box> _anchorForNode;

	private List<Box> _roots;

	private Map<Box, Box> _parentForChildNode;

	private Map<Box, List<Box>> _childrenForParentNode;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(List<Box> nodes, List<TreeConnection> connections) {
		Set<Box> nodeSet = new HashSet<>(nodes);

		_nodeForAnchor = new HashMap<>();
		_anchorForNode = new HashMap<>();
		for (TreeConnection connection : connections) {
			enterAnchor(_nodeForAnchor, _anchorForNode, nodeSet, connection.getParent());
			for (TreeConnector child : connection.getChildren()) {
				enterAnchor(_nodeForAnchor, _anchorForNode, nodeSet, child);
			}
		}

		_parentForChildNode = new HashMap<>();
		_childrenForParentNode = new HashMap<>();
		for (TreeConnection connection : connections) {
			Box parentNode = _nodeForAnchor.get(connection.getParent().getAnchor());
			List<Box> childNodes =
				connection.getChildren().stream()
					.map(TreeConnector::getAnchor)
					.map(_nodeForAnchor::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			for (Box childNode : childNodes) {
				_parentForChildNode.put(childNode, parentNode);
			}
			_childrenForParentNode.put(parentNode, childNodes);
		}

		_roots = new ArrayList<>();
		for (Box node : nodes) {
			if (_parentForChildNode.get(node) == null) {
				_roots.add(node);
			}
		}

		// Sort roots by their (last) Y coordinates to get a stable order of the layout.
		_roots.sort(Comparator.comparingDouble(b -> b.getY()));
	}

	/**
	 * Helper method to build the mapping from nodes (that are layouted) for anchor boxes (that are
	 * visually connected).
	 * 
	 * @param nodeForAnchor
	 *        The mapping from anchor to node that is built.
	 * @param anchorForNode
	 *        The mapping from node to its anchor that is built.
	 * @param nodes
	 *        all top-level tree nodes that are layouted.
	 * @param connector
	 *        The tree connector to analyze.
	 */
	private void enterAnchor(Map<Box, Box> nodeForAnchor, Map<Box, Box> anchorForNode, Set<Box> nodes,
			TreeConnector connector) {
		final Box anchor = connector.getAnchor();
		Box ancestor = anchor;
		while (ancestor != null) {
			if (nodes.contains(ancestor)) {
				nodeForAnchor.put(anchor, ancestor);
				anchorForNode.put(ancestor, anchor);
				break;
			}

			Widget parent = ancestor.getParent();
			if (parent instanceof Box) {
				ancestor = (Box) parent;
			} else {
				// Not found, most likely an error.
				break;
			}
		}
	}

	/**
	 * The organization of content boxes into columns.
	 */
	public List<Column> getColumns() {
		return _columns;
	}

	/**
	 * Layouted nodes without parents.
	 */
	public List<Box> getRoots() {
		return _roots;
	}

	/**
	 * Get or create a column for the given tree depth.
	 */
	public Column mkColumn(int level) {
		while (_columns.size() <= level) {
			_columns.add(new Column());
		}
		return _columns.get(level);
	}

	/**
	 * The children nodes for the given parent node.
	 */
	public List<Box> getChildren(Box node) {
		return _childrenForParentNode.getOrDefault(node, Collections.emptyList());
	}

	/**
	 * TODO
	 *
	 * @param node
	 * @return
	 */
	public Box getAnchor(Box node) {
		return _anchorForNode.getOrDefault(node, node);
	}

	/**
	 * Shifts the subtree rooted at the given node to the bottom.
	 */
	public void shiftY(Box node, double shiftY) {
		node.setY(node.getY() + shiftY);
		for (Box child : getChildren(node)) {
			shiftY(child, shiftY);
		}
	}

}
