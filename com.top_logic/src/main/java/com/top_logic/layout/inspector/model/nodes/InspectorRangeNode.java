/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;

/**
 * Node representing a subsequence of an array.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InspectorRangeNode extends InspectorTreeNode {

	private final int _start;

	private final int _end;

	/**
	 * Creates a {@link InspectorRangeNode}.
	 */
	public InspectorRangeNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			Object[] array, int start, int end) {
		super(model, parent, array, null);
		_start = start;
		_end = end;
		AbstractTreeTableModel.markSynthetic(this);
	}

	@Override
	public boolean isStatic() {
		return getParent().isStatic();
	}

	/**
	 * Type-safe access to {@link #getBusinessObject()}.
	 */
	public Object[] array() {
		return (Object[]) getBusinessObject();
	}

	/**
	 * Returns the first index in the {@link #array() array} that is represented by this node.
	 */
	public int getStartIndex() {
		return _start;
	}

	/**
	 * Returns the first index &gt;= {@link #getStartIndex()} in the {@link #array() array} that
	 * is <b>not</b> represented by this node.
	 */
	public int getEndIndex() {
		return _end;
	}

	/**
	 * Creates the children of this node.
	 */
	@Override
	public List<InspectorTreeNode> makeChildren() {
		ArrayList<InspectorTreeNode> childList = new ArrayList<>();
		InspectorArrayNode.addChildren(childList, this, array(), getStartIndex(), getEndIndex());
		return childList;
	}

}