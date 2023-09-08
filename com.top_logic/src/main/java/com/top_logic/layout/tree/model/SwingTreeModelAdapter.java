/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeModel;

import com.top_logic.layout.WrappedModel;


/**
 * Adapter that wraps a {@link TreeModel} as {@link TLTreeModel}.
 * 
 * <p>
 * <b>Warning:</b> This adapter may only be used for <b>finite</b> tree models, because
 * whole-tree scans are performed.
 * </p>
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SwingTreeModelAdapter extends AbstractTreeModel<Object> implements WrappedModel {

    private final TreeModel impl;

    /**
     * Creates a new {@link TLTreeModel} adapter for the given {@link TreeModel}.
     * 
     * @param impl
     *        the {@link TreeModel} to wrap.
     */
    public SwingTreeModelAdapter(TreeModel impl) {
        this.impl = impl;
    }

    @Override
	public Object getWrappedModel() {
    	return impl;
    }
    
    /**
     * Optimized implementation of {@link AbstractTreeModel#getChildIterator(Object)}
     * dispatching to {@link TreeModel#getChild(Object, int)}.
     * 
     * @see AbstractTreeModel#getChildIterator(Object)
     */
    @Override
	public Iterator<?> getChildIterator(final Object parent) {
		return new Iterator<Object>() {
            int index = 0;
            int size = impl.getChildCount(parent);
            
            @Override
			public boolean hasNext() {
                return index < size;
            }

            @Override
			public Object next() {
                return impl.getChild(parent, index++);
            }

            @Override
			public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
	public Object getRoot() {
        return impl.getRoot();
    }

    /**
     * Optimized implementation of {@link AbstractTreeModel#hasChild(Object, Object)}
     * dispatching to {@link TreeModel#getIndexOfChild(Object, Object)}, because
     * {@link #getParent(Object)} is inefficient.
     * 
     * @see AbstractTreeModel#hasChild(Object, Object)
     */
    @Override
	public boolean hasChild(Object parent, Object child) {
        return impl.getIndexOfChild(parent, child) >= 0;
    }

    /**
     * Optimized implementation of {@link AbstractTreeModel#hasChildren(Object)}
     * dispatching to {@link TreeModel#getChildCount(Object)}.
     * 
     * @see AbstractTreeModel#hasChildren(Object)
     */
    @Override
	public boolean hasChildren(Object node) {
        return impl.getChildCount(node) > 0;
    }

    @Override
	public boolean isLeaf(Object node) {
        return impl.isLeaf(node);
    }

	/**
	 * Returns the given node.
	 * 
	 * @see com.top_logic.layout.tree.model.TLTreeModel#getBusinessObject(java.lang.Object)
	 */
	@Override
	public Object getBusinessObject(Object node) {
		return node;
	}

	@Override
	public List<?> getChildren(final Object node) {
		return new AbstractList<>() {
            @Override
			public Object get(int aIndex) {
                return impl.getChild(node, aIndex);
            }

            @Override
			public int size() {
                return impl.getChildCount(node);
            }
        };
    }

	@Override
	public boolean childrenInitialized(Object parent) {
		// Wrapped model has only initialised nodes.
		return true;
	}

	@Override
	public void resetChildren(Object parent) {
		// Ignore, not lazily initialized.
	}

    /**
     * Inefficient search for the parent of the given node using a complete scan over the
     * whole tree in the worst case.
     * 
     * @see TLTreeModel#getParent(Object)
     */
    @Override
	public Object getParent(Object node) {
        Object root = getRoot();
        if (node.equals(root)) {
            return null;
        }
        return searchParent(node, root);
    }

    private Object searchParent(Object targetNode, Object current) {
		for (Iterator<?> it = getChildIterator(current); it.hasNext();) {
            Object child = it.next();
            if (child.equals(targetNode)) {
                return current;
            }
            Object parent = searchParent(targetNode, child);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }
    
    /**
     * Optimized implementation of {@link AbstractTreeModel#createPathToRoot(Object)}
     * using at most one single scan over the complete tree, since
     * {@link #getParent(Object)} is not efficient.
     * 
     * @see AbstractTreeModel#createPathToRoot(Object)
     */
    @Override
	public List<Object> createPathToRoot(Object aNode) {
        ArrayList<Object> result = new ArrayList<>();
        boolean pathFound = fillPathToRoot(result, aNode, getRoot());
        if (pathFound) {
        	return result;
        } else {
        	return Collections.emptyList();
        }
    }

	/**
	 * Tries to create a path from the given <code>targetNode</code> to the
	 * given <code>subtreeRoot</code>. The path is stored into the given list.
	 * After completion, the first node in the resulting path is the given node,
	 * the last node is the given <code>subtreeRoot</code> of the tree model.
	 * 
	 * If there is no path in the tree model from the <code>targetNode</code> to
	 * the <code>subtreeRoot</code> the given list remains unchanged.
	 * 
	 * @param path
	 *        The list, in which the computed path should be stored.
	 * @param targetNode
	 *        The node from which the path should start.
	 * @param subtreeRoot
	 *        The node at which the path should end.
	 * 
	 * @return Whether target node has been found in the subtree rooted at the
	 *         <code>subtreeRoot</code> node.
	 */
    private boolean fillPathToRoot(ArrayList<Object> path, Object targetNode, Object subtreeRoot) {
        if (targetNode.equals(subtreeRoot)) {
            path.add(subtreeRoot);
            return true;
        } else {
            for (Iterator<?> it = getChildIterator(subtreeRoot); it.hasNext(); ) {
                Object child = it.next();
                boolean found = fillPathToRoot(path, targetNode, child);
                if (found) {
                    path.add(subtreeRoot);
                    return true;
                }
            }
            
            return false;
        }
    }

    /**
	 * This method returns the inner {@link TreeModel}. Don't change the inner
	 * {@link TreeModel} it should be used as read only model!!!
	 */
    public TreeModel getTreeModel() {
    	return this.impl;
    }

	@Override
	public boolean isFinite() {
		return true;
	}

}
