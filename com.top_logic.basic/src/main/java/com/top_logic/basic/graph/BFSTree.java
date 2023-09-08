/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for breadth first search {@link Traversal} of trees.
 * 
 * <p>
 * Warning: On cyclic graphs, this algorithm will not terminate.
 * </p>
 * 
 * <p>
 * Warning: On DAGs (directed acyclic graphs) this algorithm will report nodes
 * more than once.
 * </p>
 * 
 * @param <T> The node type of the traversed tree.
 * 
 * @see DFSTree
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BFSTree<T> extends Traversal<T> {

	/**
	 * Creates a {@link BFSTree}.
	 *
	 * @param access The accessor of the traversed graph.
	 */
	public BFSTree(GraphAccess<T> access) {
		super(access);
    }

    @Override
    public boolean traverse(T start, TraversalListener<? super T> visitor) {
    	if (maxDepth < 0) {
    		return true;
    	}

		return traverseBFS(start, visitor, new ArrayList<>(), new ArrayList<>());
    }
    
	/**
	 * Traverse the structure in depth first search order starting with the
	 * given element.
	 * 
	 * @return Whether traversal finished after reporting all requested elements.
	 *         <code>false</code> is returned, if traversal has been stopped by
	 *         the given visitor.
	 */
    protected boolean traverseBFS(T start, TraversalListener<? super T> visitor, List<T> currentLevel, List<T> nextLevel) {
    	int initialDepth;
    	if (excludeStart) {
    		initialDepth = 1;
    		currentLevel.addAll(access.next(start, filter));
    	} else {
    		initialDepth = 0;
    		currentLevel.add(start);
    	}
		return bfs(visitor, currentLevel, nextLevel, initialDepth);
    }

	/**
	 * Visit all elements in current level and descend to all children of
	 * elements in the current level.
	 * 
	 * @param visitor
	 *        The visitor used as callback.
	 * @param currentLevel
	 *        All elements of the current level.
	 * @param nextLevel
	 *        Empty buffer to build the next level of elements in.
	 * @param depth
	 *        The current depth for the visit of the elements of the current
	 *        level.
	 * @return Whether traversal finished after reporting all requested
	 *         elements. <code>false</code> is returned, if traversal has been
	 *         stopped by the given visitor.
	 */
	private boolean bfs(TraversalListener<? super T> visitor, List<T> currentLevel, List<T> nextLevel, int depth) {
		while (! currentLevel.isEmpty()) {
			for (T current : currentLevel) {
				if (! visit(visitor, current, depth)) {
					return false;
				}
				nextLevel.addAll(access.next(current, filter));
			}
			
			depth++;
	    	if (depth > maxDepth) {
	    		return true;
	    	}
			
	    	// Re-use current level buffer as next level buffer.
	    	List<T> reusedBuffer = currentLevel;
	    	reusedBuffer.clear();
	    	
	    	currentLevel = nextLevel;
	    	nextLevel = reusedBuffer;
		}
		
		return true;
	}

}
