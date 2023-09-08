/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;


/**
 * {@link TreeView} where each node has a unique canonical parent.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface StructureView<N> extends TreeView<N> {

    /**
     * The parent of the given node, or <code>null</code>, if the given node is the
     * root node.
     * 
     * @param node
     *        The node to find the parent node for. Must not be <code>null</code>.
     * @return The parent node of the given node, or <code>null</code>, if the given node
     *         is the root node.
     */
	N getParent(N node);
	
}
