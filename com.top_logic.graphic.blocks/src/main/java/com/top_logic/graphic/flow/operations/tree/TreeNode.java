/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.Collections;
import java.util.List;

import com.top_logic.graphic.flow.data.Box;

/**
 * 
 */
public class TreeNode {

	private final Box _box;

	private Box _anchor;

	private TreeNode _parent;

	private List<TreeNode> _children = Collections.emptyList();

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

}
