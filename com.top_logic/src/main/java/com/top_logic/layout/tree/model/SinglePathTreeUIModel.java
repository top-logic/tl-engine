/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Iterator;
import java.util.List;

/**
 * The class {@link SinglePathTreeUIModel} is a {@link TreeUIModel} where at most one path from the
 * root node is expanded. Moreover if a node is expanded each node on the path to the root node is
 * expanded.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SinglePathTreeUIModel<N> extends AbstractStructureTreeUIModel<N> {

	private final boolean rootVisible;

	public SinglePathTreeUIModel(TLTreeModel<N> applicationModel) {
		this(applicationModel, /* rootVisible */true);
	}

	public SinglePathTreeUIModel(TLTreeModel<N> applicationModel, boolean rootVisible) {
		super(applicationModel);

		this.rootVisible = rootVisible;
	}

	@Override
	public boolean isRootVisible() {
		return rootVisible;
	}

	/**
	 * This method sets the given node to the given expand state. If the new state is
	 * <code>false</code> each node in the subtree beginning with the given node will be
	 * collapsed. If the state is <code>true</code>, each node which is not contained in the path
	 * to the root node will be collapsed.
	 * 
	 * @see com.top_logic.layout.tree.model.AbstractStructureTreeUIModel#setExpanded(java.lang.Object,
	 *      boolean)
	 */
	@Override
	public boolean setExpanded(N node, boolean expanded) {
		if ((isExpanded(node) && expanded) || (!isExpanded(node) && !expanded)) {
			return false;
		}
		if (!expanded) {
			Iterator<? extends N> childIterator = getChildIterator(node);
			while (childIterator.hasNext()) {
				N next = childIterator.next();
				if (isExpanded(next)) {
					setExpanded(next, false);
				}
			}
		} else {
			List<? extends N> thePathToRoot = createPathToRoot(node);
			for (int index = 0, size = thePathToRoot.size(); index < size-1; index++) {
				N currentParent = thePathToRoot.get(index + 1);
				if (!isExpanded(currentParent)) {
					super.setExpanded(currentParent, true);
				} else {
					collapseSiblings(thePathToRoot.get(index));
					break;
				}
			}
		}
		return super.setExpanded(node, expanded);
	}

	/**
	 * This method collapses all siblings of the given node.
	 * 
	 * @param node
	 *            a node in the tree.
	 */
	private void collapseSiblings(N node) {
		N parent = getParent(node);
		if (parent != null) {
			Iterator<? extends N> childIterator = getChildIterator(parent);
			while (childIterator.hasNext()) {
				N currentChild = childIterator.next();
				if (currentChild != node && isExpanded(currentChild)) {
					setExpanded(currentChild, false);
				}
			}
		}
	}

}
