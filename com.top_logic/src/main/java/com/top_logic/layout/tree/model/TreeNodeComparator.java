/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Comparator;
import java.util.HashMap;

/**
 * {@link Comparator} that compares nodes of a {@link TLTreeModel} using a
 * delegate node {@link Comparator} but preserving the hierarchy defined by the
 * {@link TLTreeModel}.
 * 
 * <p>
 * Two nodes are compared using the delegate node {@link Comparator}, if they
 * are children of the same parent node. Otherwise, the comparison result
 * reflects the tree hierarchy: A descendant node is always sorted after all of
 * its ancestor nodes. Nodes <code>a</code> and <code>b</code>, where
 * neither one is an ancestor of the other, are ordered according to their
 * respective ancestors that are direct children of the next common ancestor of
 * <code>a</code> and <code>b</code>.
 * </p>
 * 
 * <p>
 * Sorting an unstructured list of nodes from a {@link TLTreeModel} using this
 * comparator results in a depth first search pre-order traversal of the tree,
 * where children of the same parent are visited according to the order defined
 * by the delegate node {@link Comparator}.
 * </p>
 * 
 * <p>
 * <b>Note:</b> An instance of this class <b>must not</b> used from multiple
 * threads concurrently. Therefore it is not advisable to create a singleton
 * instance of this class.
 * </p>
 * 
 * <p>
 * <b>Note:</b> For efficiency, this comparator should only be used, if the
 * underlying tree structure is extremely flat (two or three levels except the
 * root node). For displaying (and sorting) deep tree models, the sort operation
 * should be applied to the children lists of each node separately.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeNodeComparator implements Comparator {

	private final TLTreeModel treeModel;
    private final Comparator nodeComparator;
    
    /**
     * Helper for constructing a path during a compare operation. 
     */
	private final HashMap path2 = new HashMap();
    
	/**
	 * Creates a {@link TreeNodeComparator} for nodes of the given
	 * {@link TLTreeModel}.
	 * 
	 * @param treeModel
	 *        The tree model of nodes to compare.
	 * @param nodeComparator
	 *        The {@link Comparator} to use for nodes with the same parent node.
	 */
    public TreeNodeComparator(TLTreeModel treeModel, Comparator nodeComparator) {
		this.treeModel = treeModel;
		this.nodeComparator = nodeComparator;
	}
    
    @Override
	public int compare(Object node1, Object node2) {
    	// Check for equality and comparison with null.
    	if (node1 == node2) {
    		return 0;
    	}
    	else if (node1 == null) {
    		return nodeComparator.compare(null, node2);
    	} 
    	else if (node2 == null) {
    		return nodeComparator.compare(node1, null);
    	}
    	else if (node1.equals(node2)) {
    		return 0;
    	}
    	
    	Object parent1 = treeModel.getParent(node1);
    	Object parent2 = treeModel.getParent(node2);

    	if (parent1 == null) {
    		if (parent2 == null) {
    			// Node 1 and node 2 are both root nodes, but they are not
				// equal. Compare the nodes directly.
    			return nodeComparator.compare(node1, node2);
    		} else {
    			// Node 1 is a root node, but node 2 is not. Check, whether node
				// 2 is a child of node 1.
    	    	if (parent2.equals(node1)) {
    	    		// Node 2 must appear after node 1, because node 2 is a child of
    	    		// node 1. Therefore, node 1 < node 2.
    	    		return -1;
    	    	} else {
    	    		// Search up the tree to decide, whether node 1 is an
					// ancestor of node 2.
    	    		while (true) {
    	    			Object ancestor2 = treeModel.getParent(parent2);
    	    			if (ancestor2 == null) {
    	    				// Parent 2 is a root node. If parent 2 equals node
							// 1, then node 2 is a descendant of node 1.
    	    				if (parent2.equals(node1)) {
    	    					return -1;
    	    				} else {
    	    					return nodeComparator.compare(node1, parent2);
    	    				}
    	    			}
						parent2 = ancestor2;
    	    		}
    	    	}
    		}
    	}
    	else if (parent2 == null) {
    		// Node 2 is a root node, but node 1 is not. Check, whether node 1
			// is a direct child of node 2.
    		if (parent1.equals(node2)) {
        		// Node 2 is the parent of node 1. Therefore, node 2 must appear
    			// before node 1. Therefore, node 1 > node 2.
        		return 1;
        	} else {
        		// Search up the tree to decide, whether node 2 is an ancestor
				// of node 1.
	    		while (true) {
	    			Object ancestor1 = treeModel.getParent(parent1);
	    			if (ancestor1 == null) {
	    				// Parent 1 is a root node. If parent 1 equals node
						// 2, then node 1 is a descendant of node 2.
	    				if (parent1.equals(node2)) {
	    					return 1;
	    				} else {
	    					return nodeComparator.compare(parent1, node2);
	    				}
	    			}
					parent1 = ancestor1;
	    		}
        	}
    	}
    	else if (parent1.equals(parent2)) {
    		// Both nodes are children of the same parent. Directly compare the
			// nodes.
    		return nodeComparator.compare(node1, node2);
    	}
    	else if (parent1.equals(node2)) {
    		// Node 2 is the parent of node 1. Therefore, node 2 must appear
			// before node 1. Therefore, node 1 > node 2.
    		return 1;
    	}
    	else if (parent2.equals(node1)) {
    		// Node 2 must appear after node 1, because node 2 is a child of
    		// node 1. Therefore, node 1 < node 2.
    		return -1;
    	}
    	else {
    		// Find ancestors of parent 2 and check, whether node 1 is among them.
    		try {
    			Object ancestor2 = parent2;
    			Object child2 = node2;
    			while (ancestor2 != null) {
    				if (ancestor2.equals(node1)) {
    					// Node 2 must appear after node 1, because node 2 is a child of
    					// node 1. Therefore, node 1 < node 2.
    					return -1;
    				}
    				path2.put(ancestor2, child2);
    				
    				child2 = ancestor2;
    				ancestor2 = treeModel.getParent(ancestor2);
    			}
    			
    			// Node 1 is not an ancestor of node 2. 
    			
    			Object ancestor1 = parent1;
    			Object child1 = node1;
    			while (ancestor1 != null) {
    				if (ancestor1.equals(node2)) {
    					// Node 2 is an ancestor of node 1. Therefore, node 1 > node 2.
    					return 1;
    				}
    				
    				Object subroot2 = path2.get(ancestor1);
    				if (subroot2 != null) {
    					// Ancestor 1 is a common ancestor of node 1 and node 2.
    					return nodeComparator.compare(child1, subroot2);
    				}
    				
    				child1 = ancestor1;
    				ancestor1 = treeModel.getParent(ancestor1);
    			}
    			
    			// Both nodes are in different trees without a common root node.
				// Compare the tree roots.
    			return nodeComparator.compare(child1, child2);
    		} finally {
    			path2.clear();
    		}
    	}
    }
}
