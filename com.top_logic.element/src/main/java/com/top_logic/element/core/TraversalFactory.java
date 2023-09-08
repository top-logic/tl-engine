/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import com.top_logic.basic.graph.BFSTree;
import com.top_logic.basic.graph.DFSTree;
import com.top_logic.basic.graph.Traversal;
import com.top_logic.element.structured.StructuredElement;

/**
 * Factory for {@link Traversal}s on {@link StructuredElement}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TraversalFactory {

	/**
	 * Traversing a tree in DFS (depth first) order (reporting a parent before
	 * descending to its children).
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int)
	 * @see #DEPTH_FIRST_SORTED
	 */
	public final static int DEPTH_FIRST = 0;

	/** @see #DEPTH_FIRST */
	public final static int DEPTH_FIRST_SORTED = 4;

	/**
	 * Traversing a tree in DFS (depth first) order but reporting the children
	 * before its parent.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int)
	 * @see #DEPTH_FIRST_POSTORDER_SORTED
	 */
	public final static int DEPTH_FIRST_POSTORDER = 1;

	/** @see #DEPTH_FIRST_POSTORDER */
	public final static int DEPTH_FIRST_POSTORDER_SORTED = 5;

	/**
	 * Traversing a tree in BFS (breads first) order starting with the first
	 * level followed by its children levels.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int)
	 * @see #BREADTH_FIRST_SORTED
	 */
	public final static int BREADTH_FIRST = 2;

	/** @see #BREADTH_FIRST */
	public final static int BREADTH_FIRST_SORTED = 6;
	
	/**
	 * Traversing a tree from within a inner node to the root node.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int)
	 */
	public final static int ANCESTORS = 3;

	/**
	 * Traverse a structure starting from the given element.
	 * 
	 * @param element
	 *        The {@link StructuredElement} to start the traversal with.
	 * @param visitor
	 *        The callback to which each found node is reported during the
	 *        traversal.
	 * @param strategy
	 *        One of the constants defined above: {@link #DEPTH_FIRST},
	 *        {@link #DEPTH_FIRST_POSTORDER}, {@link #BREADTH_FIRST}, or
	 *        {@link #ANCESTORS}.
	 * @param excludeStart
	 *        Whether to exclude the given start node from the traversal (not
	 *        report the start node to the given visitor).
	 * @param maxDepth
	 *        The maximum length of a path from the given start node to any node
	 *        reported by the traversal.
	 * @return Whether the traversal has reported all specified nodes.
	 *         <code>false</code>, if the traversal was stopped by the given
	 *         visitor, see the result of
	 *         {@link TLElementVisitor#onVisit(StructuredElement, int)}.
	 */
	public static boolean traverse(StructuredElement element, TLElementVisitor visitor, int strategy, boolean excludeStart, int maxDepth) {
		return getTraversal(strategy, excludeStart, maxDepth).traverse(element, visitor);
	}

	/**
	 * Traverse a structure starting from the given element.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int, boolean, int)
	 */
	public static boolean traverse(StructuredElement element, TLElementVisitor aVisitor, int strategyId, boolean excludeStart) {
		return getTraversal(strategyId, excludeStart).traverse(element, aVisitor);
	}
	
	/**
	 * Traverse a structure starting from the given element.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int, boolean, int)
	 */
	public static boolean traverse(StructuredElement element, TLElementVisitor aVisitor, int strategyId) {
		return getTraversal(strategyId).traverse(element, aVisitor);
	}
	
	/**
	 * Create a structure {@link Traversal}.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int, boolean, int)
	 */
	public static Traversal<StructuredElement> getTraversal(int strategyId, boolean excludeStart, int aMaxDepth) {
		return getTraversal(strategyId, excludeStart).setMaxDepth(aMaxDepth);
	}

	/**
	 * Create a structure {@link Traversal}.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int, boolean, int)
	 */
	public static Traversal<StructuredElement> getTraversal(int strategyId, boolean excludeStart) {
		return getTraversal(strategyId).setExcludeStart(excludeStart);
	}

	/**
	 * Create a structure {@link Traversal}.
	 * 
	 * @see #traverse(StructuredElement, TLElementVisitor, int, boolean, int)
	 */
	public static Traversal<StructuredElement> getTraversal(int strategyId) {
		Traversal<StructuredElement> traversal;
        switch (strategyId) {
	        case TraversalFactory.DEPTH_FIRST:
	        	traversal = TraversalFactory.newDFSInstance();
	        	break;
	        case TraversalFactory.DEPTH_FIRST_SORTED:
				return TraversalFactory.newDFSInstanceSorted();
            case TraversalFactory.DEPTH_FIRST_POSTORDER:
            	traversal = TraversalFactory.newDFSInstance().setPostOrder(true);
                break;
            case TraversalFactory.DEPTH_FIRST_POSTORDER_SORTED:
				return TraversalFactory.newDFSInstanceSorted().setPostOrder(true);
            case TraversalFactory.BREADTH_FIRST: 
            	traversal = TraversalFactory.newBFSInstance();
                break;
            case TraversalFactory.BREADTH_FIRST_SORTED: 
				return TraversalFactory.newBFSInstanceSorted();
            case TraversalFactory.ANCESTORS:
            	traversal = new DFSTree<>(ElementParentAccess.INSTANCE);
            	break;
            default:
                throw new IllegalArgumentException("Unknown visitor strategy: " + strategyId);
        }
		return traversal;
	}

	/**
	 * Factory for creating {@link BFSTree} instances.
	 */
	static BFSTree<StructuredElement> newBFSInstance() {
		return new BFSTree<>(ElementChildrenAccess.INSTANCE);
	}

	static BFSTree<StructuredElement> newBFSInstanceSorted() {
		return new BFSTree<>(ElementChildrenAccess.INSTANCE_SORTED);
	}

	/**
	 * Factory for {@link DFSTree} instances.
	 */
	static DFSTree<StructuredElement> newDFSInstance() {
		return new DFSTree<>(ElementChildrenAccess.INSTANCE);
	}

	static DFSTree<StructuredElement> newDFSInstanceSorted() {
		return new DFSTree<>(ElementChildrenAccess.INSTANCE_SORTED);
	}

}
