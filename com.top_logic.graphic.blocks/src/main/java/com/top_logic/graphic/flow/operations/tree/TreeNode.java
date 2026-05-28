/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.graphic.flow.data.Box;

/**
 * Temporary object that contains information for computing a tree layout.
 */
class TreeNode {

	private final Box _box;

	private Box _anchor;

	private TreeNode _parent;

	private final List<TreeNode> _children = new ArrayList<>();

	private double _offsetX;

	private double _offsetY;

	/**
	 * X coordinate of an explicit bus override for this node's outgoing connections, or
	 * {@link Double#NaN} if no override is set.
	 *
	 * <p>Used by the row-wise sub-grid: a sub-grid child whose own children are rendered to the
	 * right of the surrounding sub-grid (at the parent's {@code postGridX}) has its bus routed at
	 * the parent's {@code childBusX} rather than the natural mid-x between this node and its
	 * children. Without the override the bus would land in the middle of the surrounding sub-grid
	 * and clash with sibling sub-grid nodes.
	 */
	private double _busXOverride = Double.NaN;

	/**
	 * Creates a {@link TreeNode}.
	 *
	 * @param node
	 *        The box that represents one node in a tree layout.
	 */
	public TreeNode(Box node) {
		_box = node;
		_anchor = node;
	}

	/**
	 * The top-level box that represents a node in a tree layout.
	 */
	public Box getBox() {
		return _box;
	}

	/**
	 * A box that is contained in the top-level node's {@link #getBox()}
	 */
	public Box getAnchor() {
		return _anchor;
	}

	/**
	 * @see #getAnchor()
	 */
	public void setAnchor(Box anchor) {
		_anchor = anchor;
	}

	/**
	 * The parent tree node.
	 */
	public TreeNode getParent() {
		return _parent;
	}

	/**
	 * @see #getParent()
	 */
	public void setParent(TreeNode parent) {
		if (_parent != null) {
			throw new IllegalStateException("Node '" + _box + "' must not have more than one parent: "
				+ _parent.getBox() + " and " + parent.getBox());
		}
		_parent = parent;
	}

	/**
	 * The children nodes of this node.
	 */
	public List<TreeNode> getChildren() {
		return _children;
	}

	/**
	 * @see #getChildren()
	 */
	public void addChild(TreeNode child) {
		_children.add(child);
	}

	/**
	 * The X coordinate of this node (defined by the tree layout algorithm).
	 */
	public double getX() {
		return _offsetX;
	}

	/**
	 * @see #getX()
	 */
	public void setX(double offsetX) {
		_offsetX = offsetX;
	}

	/**
	 * The Y coordinate of this node (defined by the tree layout algorithm).
	 */
	public double getY() {
		return _offsetY;
	}

	/**
	 * @see #getY()
	 */
	public void setY(double offsetY) {
		_offsetY = offsetY;
	}

	/**
	 * The Y coordinate of the bottom border of this node.
	 */
	public double getBottom() {
		return getY() + getBox().getHeight();
	}

	/**
	 * @see #getBusXOverride()
	 */
	public boolean hasBusXOverride() {
		return !Double.isNaN(_busXOverride);
	}

	/**
	 * X coordinate of an explicit bus override for this node's outgoing connections, or
	 * {@link Double#NaN} if no override is set.
	 *
	 * @see #hasBusXOverride()
	 */
	public double getBusXOverride() {
		return _busXOverride;
	}

	/**
	 * @see #getBusXOverride()
	 */
	public void setBusXOverride(double busXOverride) {
		_busXOverride = busXOverride;
	}

}
