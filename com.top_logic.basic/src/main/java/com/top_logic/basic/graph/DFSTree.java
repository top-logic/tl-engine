/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

/**
 * Utility class for depth first search {@link Traversal} of trees.
 * 
 * <p>
 * Warning: On cyclic graphs, this algorithm will not crash with stack overflow.
 * </p>
 * 
 * <p>
 * Warning: On DAGs (directed acyclic graphs) this algorithm will report nodes
 * more than once.
 * </p>
 * 
 * @param <T> The node type of the traversed tree.
 * 
 * @see BFSTree
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DFSTree<T> extends Traversal<T> {
	
	private boolean postOrder = false;

	/**
	 * Creates a {@link DFSTree}.
	 *
	 * @param access See {@link Traversal#Traversal(GraphAccess)}.
	 */
    public DFSTree(GraphAccess<T> access) {
        super(access);
    }

	/**
	 * Sets whether an element should only be reported after descending to
	 * its children (instead of before descending to its children).
	 * 
	 * @return This strategy for call chaining.
	 */
	public DFSTree<T> setPostOrder(boolean value) {
    	checkModify();
		this.postOrder = value;
		return this;
	}
	
    @Override
    public boolean traverse(T start, TraversalListener<? super T> visitor) {
    	if (maxDepth < 0) {
    		return true;
    	}

    	return traverseDFS(start, visitor);
    }
    
	/**
	 * Traverse the structure in depth first search order starting with the
	 * given element.
	 * 
	 * @return Whether traversal finished after reporting all requested elements.
	 *         <code>false</code> is returned, if traversal has been stopped by
	 *         the given visitor.
	 */
    private boolean traverseDFS(T start, TraversalListener<? super T> visitor) {
    	if (excludeStart) {
    		return dfsDescend(visitor, start, 1);
    	} else {
    		return dfs(visitor, start, 0);
    	}
    }
    
    /**
     * Visit the given element and recursively descent through its children.
     * 
     * @return Whether traversing should unconditionally stop.
     */
    private boolean dfs(TraversalListener<? super T> visitor, T element, int depth) {
    	boolean preOrder = ! postOrder;
    	
		if (preOrder) {
    		if (! visit(visitor, element, depth)) {
    			return false;
    		}
    	}
    	
    	int childDepth = depth + 1;
    	if (childDepth <= maxDepth) {
    		if (! dfsDescend(visitor, element, childDepth)) {
    			return false;
    		}
    	}

    	if (postOrder) {
    		if (! visit(visitor, element, depth)) {
    			return false;
    		}
    	}
    	
		return true;
	}

    /**
     * Recursively descent through children of the given element.
     * 
     * @return Whether traversing should unconditionally stop.
     */
    private boolean dfsDescend(TraversalListener<? super T> visitor, T element, int childDepth) {
    	for (T child : access.next(element, filter)) {
    		if (! dfs(visitor, child, childDepth)) {
    			return false;
    		}
    	}
    	return true;
	}

}
