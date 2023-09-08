/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


/**
 * UI model encapsulating the display-related aspects of a {@link TreeModelBase}.
 * 
 * <p>
 * A tree UI model associates each displayed node with an identifier, and expansion state.
 * Additionally, it decides about whether a node is selectable.
 * </p>
 * 
 * <p>
 * Note: A {@link TreeUIModel} is always a {@link TLTreeModel} where each node object is unique
 * within the tree. Displaying a {@link TreeAspectModel} therefore requires additional intermediate
 * node objects.
 * </p>
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeUIModel<N> extends TLTreeModel<N> {

    /**
	 * Decides, whether the root node of this tree should be displayed.
	 * 
	 * <p>
	 * It is useful to suppress the rendering of the root node for tree models with synthetic roots.
	 * </p>
	 * 
	 * <p>
	 * If the root node is not visible, it is always {@link #isExpanded(Object) expanded}. This is
	 * because a tree with collapsed root node which is not visible is not really useful.
	 * </p>
	 * 
	 * TODO BHU: Move to general node filter.
	 */
    boolean isRootVisible();

	/**
	 * Checks, whether the given node is currently expanded or not.
	 * 
	 * <p>
	 * If the root node is not visible, it is always {@link #isExpanded(Object) expanded}. This is
	 * because a tree with collapsed root node which is not visible is not really useful.
	 * </p>
	 */
	boolean isExpanded(N node);

	/**
	 * Sets the expansion state of the given node to the given value. 
	 * 
	 * @return <code>true</code>, if the expansion state of the given
	 *         node has changed, <code>false</code> otherwise.
	 */
	boolean setExpanded(N node, boolean expanded);

	/**
	 * Whether all parent nodes of the given node are {@link #isExpanded(Object)}, e.g. the root
	 * node, which has no parent, is always displayed, also if it is {@link #isRootVisible() not
	 * visible}.
	 */
	boolean isDisplayed(N node);
	
	/**
	 * The business object of this node.
	 */
	Object getUserObject(N node);

}