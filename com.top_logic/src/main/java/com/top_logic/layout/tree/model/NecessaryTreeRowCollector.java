/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.table.TableModel;

/**
 * Encapsulates the algorithm for {@link AbstractTreeTableModel.TreeTable#getNecessaryRows(Object)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class NecessaryTreeRowCollector<N extends AbstractTreeTableModel.AbstractTreeTableNode<N>> {

	private final AbstractTreeTableModel<N> _tree;

	private final AbstractTreeTableModel<N>.TreeTable _table;

	public NecessaryTreeRowCollector(AbstractTreeTableModel<N> tree, AbstractTreeTableModel<N>.TreeTable table) {
		_tree = tree;
		_table = table;
	}

	public AbstractTreeTableModel<N> getTree() {
		return _tree;
	}

	public AbstractTreeTableModel<N>.TreeTable getTable() {
		return _table;
	}

	/**
	 * @see TableModel#getNecessaryRows(Object)
	 */
	public Collection<Object> getNecessaryRows(N node) {
		if (node.isDisplayed()) {
			return Collections.emptyList();
		}
		if (getTree().isRoot(node) && !getTree().isRootVisible()) {
			return Collections.emptyList();
		}
		if (isSynthetic(node)) {
			N nonSyntheticChild = findNonSyntheticChild(node);
			if (nonSyntheticChild == null) {
				return Collections.emptyList();
			}
			if (getTree().isRoot(node)) {
				return Collections.<Object> singleton(nonSyntheticChild);
			}
			node = nonSyntheticChild;
		}
		if (getTable().isFilterIncludeParents()) {
			return Collections.<Object> singleton(node);
		}
		if (getTable().isFilterIncludeChildren()) {
			return getUppermostInvisibleAncestor(node);
		} else {
			return getInvisibleAncestors(node);
		}
	}

	/**
	 * Returns the first non-synthetic child of the given synthetic node.
	 * 
	 * @param node
	 *        Has to be a synthetic node.
	 * @return null, if the node has no non-synthetic direct children.
	 */
	private N findNonSyntheticChild(N node) {
		List<N> children = new ArrayList<>(node.getChildren());
		getTree().sort(children);
		for (N child : children) {
			if (!isSynthetic(child)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * @param node
	 *        Must not be a synthetic node.
	 * @return The list of invisible ancestors, without synthetic nodes.
	 */
	private Collection<Object> getInvisibleAncestors(N node) {
		List<Object> ancestors = new ArrayList<>();
		N ancestor = node;
		boolean isRootVisible = getTree().isRootVisible();
		while ((ancestor != null) && !ancestor.isDisplayed()) {
			if (getTree().isRoot(ancestor) && !isRootVisible) {
				break;
			}
			if (isSynthetic(ancestor)) {
				/* Synthetic nodes must not be filtered. They are visible if they have at least one
				 * visible child. They are therefore skipped in this list. */
				ancestor = ancestor.getParent();
				continue;
			}
			ancestors.add(ancestor);
			ancestor = ancestor.getParent();
		}
		return ancestors;
	}

	/**
	 * @param node
	 *        Must not be a synthetic node.
	 * @return The uppermost invisible, non-synthetic ancestor.
	 */
	private Collection<Object> getUppermostInvisibleAncestor(N node) {
		N ancestor = node;
		N nonSyntheticAncestor = node;
		boolean isRootVisible = getTree().isRootVisible();
		while ((ancestor != null) && !ancestor.isDisplayed()) {
			if (getTree().isRoot(ancestor) && !isRootVisible) {
				break;
			}
			if (!isSynthetic(ancestor)) {
				nonSyntheticAncestor = ancestor;
			}
			ancestor = ancestor.getParent();
		}
		return Collections.<Object> singleton(nonSyntheticAncestor);
	}

	private boolean isSynthetic(N node) {
		return AbstractTreeTableModel.isSynthetic(node);
	}

}
