/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TreeView;

/**
 * The {@link ConstantTreeViewTreeModelAdapter} provides a {@link TLTreeModel} based on a
 * {@link TreeView} and a root object.
 * 
 * <p>
 * The {@link TreeView} must be constant, i.e. no nodes must be added or removed, in order to
 * guarantee the consistency of the parent relation. Adding and removing of
 * {@link TreeModelListener}s is not supported.
 * </p>
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ConstantTreeViewTreeModelAdapter<N> implements TLTreeModel<N> {

	TreeView<N> base;

	Map<N, N> parent;

	N root;

    /**
     * Creates a {@link ConstantTreeViewTreeModelAdapter}.
     *
     * @param aRoot The root node.
     * @param aBase The base model.
     */
	public ConstantTreeViewTreeModelAdapter(N aRoot, TreeView<N> aBase) {
        this.root   = aRoot;
        this.base   = aBase;
		this.parent = new HashMap<>();
        initParents(aRoot, aBase, this.parent);
    }

	private static <T> void initParents(T aParent, TreeView<T> aView, Map<T, T> someParents) {
		Iterator<? extends T> theChildren = aView.getChildIterator(aParent);
        if (theChildren != null) {
            while (theChildren.hasNext()) {
				T theChild = theChildren.next();
                someParents.put(theChild, aParent);
                initParents(theChild, aView, someParents);
            }
        }
    }


	@Override
	public List<N> createPathToRoot(N aNode) {
		List<N> theResult = new ArrayList<>();
        while (aNode != null) {
            theResult.add(aNode);
            aNode = this.getParent(aNode);
        }
        return theResult;
    }

	@Override
	public N getParent(N aNode) {
        return this.parent.get(aNode);
    }

    @Override
	public boolean addTreeModelListener(TreeModelListener aListener) {
        return false;
    }

    @Override
	public boolean containsNode(Object aNode) {
        return this.parent.containsKey(aNode);
    }

	@Override
	public List<? extends N> getChildren(N aParent) {
        return CollectionUtil.toList(this.base.getChildIterator(aParent));
    }

	@Override
	public boolean childrenInitialized(N parent) {
		return true;
	}

	@Override
	public void resetChildren(Object parent) {
		// Ignore, not lazily initialized.
	}

	@Override
	public N getRoot() {
        return this.root;
    }

	@Override
	public boolean hasChild(N aParent, Object aNode) {
        return this.getChildren(aParent).contains(aNode);
    }

	@Override
	public boolean hasChildren(N aNode) {
        return ! isLeaf(aNode);
    }

    @Override
	public boolean removeTreeModelListener(TreeModelListener aListener) {
        return false;
    }

	@Override
	public Iterator<? extends N> getChildIterator(N aNode) {
        return this.base.getChildIterator(aNode);
    }

	@Override
	public boolean isLeaf(N aNode) {
		Iterator<? extends N> theIt = this.getChildIterator(aNode);
        return (theIt == null) || ( ! theIt.hasNext());
    }

	/**
	 * Returns the given node
	 * 
	 * @see com.top_logic.layout.tree.model.TLTreeModel#getBusinessObject(java.lang.Object)
	 */
	@Override
	public Object getBusinessObject(Object node) {
		return node;
	}

	@Override
	public boolean isFinite() {
		return base.isFinite();
	}

}

