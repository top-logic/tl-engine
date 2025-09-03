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
 * 
 */
public class TreeNode {

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
	 */
	public TreeNode(Box node) {
		_box = node;
		_anchor = node;
	}

	/**
	 * TODO
	 *
	 * @param anchor
	 */
	public void setAnchor(Box anchor) {
		_anchor = anchor;
	}

	/**
	 * TODO
	 */
	public Box getBox() {
		return _box;
	}

	/**
	 * TODO
	 */
	public Box getAnchor() {
		return _anchor;
	}

	/**
	 * TODO
	 *
	 * @param parentNode
	 */
	public void setParent(TreeNode parent) {
		_parent = parent;
	}

	/**
	 * TODO
	 */
	public TreeNode getParent() {
		return _parent;
	}

	/**
	 * TODO
	 *
	 * @param childNodes
	 */
	public void setChildren(List<TreeNode> children) {
		_children = children;
	}

	/**
	 * TODO
	 */
	public List<TreeNode> getChildren() {
		return _children;
	}

	/**
	 * TODO
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
	 * TODO
	 *
	 * @param column
	 * @param index
	 */
	public void setColumn(Column column, int index) {
		_column = column;
		_index = index;
	}

	public int getLevel() {
		return _column.getLevel();
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public double getX() {
		return getColumn().getOffsetX();
	}

	/**
	 * TODO
	 */
	public double getY() {
		return _offsetY;
	}

	/**
	 * TODO
	 *
	 * @param minY
	 */
	public void setY(double offsetY) {
		_offsetY = offsetY;
	}

	/**
	 * TODO
	 *
	 * @return
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

	/**
	 * TODO
	 *
	 * @return
	 */
	public TreeNode getLastChild() {
		return getChildren().isEmpty() ? null : getChildren().get(getChildren().size() - 1);
	}

}
