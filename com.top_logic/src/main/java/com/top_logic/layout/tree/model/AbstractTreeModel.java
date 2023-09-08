/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.TreeView;


/**
 * Common base class for {@link TLTreeModel} implementations.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public abstract class AbstractTreeModel<N> extends AbstractTreeModelBase<N> implements TLTreeModel<N> {

    /**
     * Default implementation for {@link TLTreeModel#createPathToRoot(Object)} dispatching
     * to {@link #getParent(Object)}.
     * 
     * <p>
     * Must be overridden with an optimized implementation, if {@link #getParent(Object)} is
     * not efficient.
     * </p>
     * 
     * @see TLTreeModel#createPathToRoot(Object)
     */
	@Override
	public List<N> createPathToRoot(N aNode) {
		if (aNode == null) {
			return Collections.emptyList();
		}
		ArrayList<N> result = new ArrayList<>();
		do {
			result.add(aNode);
			aNode = getParent(aNode);
		} while (aNode != null);
		
		// there is at least the given node in the result list.
		if (result.get(result.size() - 1) == getRoot()) {
			return result;
		} else {
			return Collections.emptyList();
		}
    }

    /**
     * Default implementation for {@link TLTreeModel#containsNode(Object)} dispatching to
     * {@link #createPathToRoot(Object)}.
     * 
     * <p>
     * Must be overridden with an optimized implementation, if
     * {@link #createPathToRoot(Object)} is not efficient.
     * </p>
     * 
     * @see TLTreeModel#containsNode(Object)
     */
	@Override
	public boolean containsNode(N aNode) {
        return ! createPathToRoot(aNode).isEmpty();
    }
    
    /**
     * Default implementation for {@link TreeView#getChildIterator(Object)}
     * dispatching to {@link #getChildren(Object)}.
     * 
     * <p>
     * Must be overridden with an optimized implementation, if {@link #getChildren(Object)}
     * is not efficient.
     * </p>
     * 
     * @see TreeView#getChildIterator(Object)
     */
	@Override
	public Iterator<? extends N> getChildIterator(N aNode) {
        return getChildren(aNode).iterator();
    }
    
    /**
     * Default implementation for {@link TLTreeModel#hasChild(Object, Object)}
     * dispatching to {@link #getChildren(Object)}.
     * <p>
     * Should be overridden with an optimized implementation.
     * </p>
     * 
     * @see TLTreeModel#hasChild(Object, Object)
     */
	@Override
	public boolean hasChild(N parent, Object node) {
		return getChildren(parent).contains(node);
    }

    /**
     * Default implementation for {@link TLTreeModel#hasChildren(Object)}
     * dispatching to {@link #getChildren(Object)}.
     * 
     * <p>
     * Must be overridden with an optimized implementation, if {@link #getChildren(Object)}
     * is not efficient.
     * </p>
     * 
     * @see TLTreeModel#hasChildren(Object)
     */
	@Override
	public boolean hasChildren(N node) {
        return getChildren(node).size() > 0;
    }

}
