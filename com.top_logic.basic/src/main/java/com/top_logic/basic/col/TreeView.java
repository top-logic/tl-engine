/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * A view of a generic object as tree of nodes.
 * 
 * <p>
 * An instance of {@link TreeView} encapsulates the access to the children of a
 * generic tree node.
 * </p>
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeView<N> {
    
	/**
     * Report, whether the given node cannot have child nodes.
     * 
     * @param node
     *            The node in question.
     * @return <code>true</code>, if {@link #getChildIterator(Object)} would always
     *         return an empty iterator. <code>false</code>, if
     *         {@link #getChildIterator(Object)} could return a non-empty iterator.
     */
    boolean isLeaf(N node);
	
	/**
	 * An {@link Iterator} over the children nodes of the given node.
	 * 
	 * @param node
	 *     The node of which the children are requested.
	 * @return An iterator of the children of the given node.
	 */
	Iterator<? extends N> getChildIterator(N node);

	/**
	 * Whether the tree is guaranteed to be finite.
	 */
	boolean isFinite();

}
