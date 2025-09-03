/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.Collections;
import java.util.List;

import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.operations.tree.TreeRenderInfo.Column;

/**
 * Temporary object that contains information for computing a tree layout.
 */
class TreeNode {

	private final Box _box;

	private Box _anchor;

	private TreeNode _parent;

	private List<TreeNode> _children = Collections.emptyList();

	private Column _column;

	private double _offsetY;

	private int _index;

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
	public void setChildren(List<TreeNode> children) {
		_children = children;
	}

	/**
	 * The column of the tree layout, where this node has been placed.
	 */
	public Column getColumn() {
		return _column;
	}

	/**
	 * The index of this node within its {@link #getColumn()}.
	 */
	public int getIndex() {
		return _index;
	}

	/**
	 * @see #getColumn()
	 * @see #getIndex()
	 */
	public void setColumn(Column column, int index) {
		_column = column;
		_index = index;
	}

	/**
	 * The tree level of this node.
	 * 
	 * @see Column#getLevel()
	 */
	public int getLevel() {
		return _column.getLevel();
	}

	/**
	 * The X coordinate of this node (defined by the tree layout algorithm).
	 * 
	 * @see Column#getOffsetX()
	 */
	public double getX() {
		return getColumn().getOffsetX();
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
	 * The node that precedes this node in its {@link #getColumn()}.
	 */
	public TreeNode getColumnPredecessor() {
		return getIndex() == 0 ? null : getColumn().getNode(getIndex() - 1);
	}

}
