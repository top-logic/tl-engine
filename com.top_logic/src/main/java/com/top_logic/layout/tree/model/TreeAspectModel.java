/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;


/**
 * Tree business model that adds a tree aspect to any set of business objects.
 * 
 * <p>
 * In a {@link TreeAspectModel}, the same business object may occur at several positions in the
 * tree. The business object is therefore not an identifier for the tree node. A tree node is only
 * identified by a path from the root business object to the business object of the node.
 * </p>
 * 
 * @param <N>
 *        The node type.
 * 
 * @see TLTreeModel for a tree model where each business object has only a single canonical position
 *      in the tree and is therefore an identifier for the tree node.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeAspectModel<N> extends TreeModelBase<N> {

	// TODO BHU: Decide about interface.	

}
