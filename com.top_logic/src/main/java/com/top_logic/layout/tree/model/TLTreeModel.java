/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

import com.top_logic.basic.col.StructureView;

/**
 * Tree business model where the node objects are arranged in a canonical tree structure.
 * 
 * @param <N>
 *        The node type.
 * 
 * @see TreeAspectModel for a tree business model that adds a tree aspect to any set of business
 *      objects and a singel business object may have several positions in the tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLTreeModel<N> extends TreeModelBase<N>, StructureView<N> {

    /**
     * Create a path from the given node to the root node.
     * 
     * @param aNode
     *        The node from which the path should start. Must not be <code>null</code>.
     * @return A path from the given node to the {@link #getRoot() root} of this tree mode.
     *         The first node in the resulting path is the given node, the last node is the
     *         root node of this tree model. If the given node is not part of this tree
     *         model, an empty list is returned.
     * 
     */
	List<N> createPathToRoot(N aNode);

	/**
	 * Returns the business object of the given node
	 * 
	 * @param node
	 *        the node to get the business object of. Must not be <code>null</code>.
	 * 
	 * @return the user object of the node, i.e. the object which is represented by the given node.
	 */
	Object getBusinessObject(N node);

}
