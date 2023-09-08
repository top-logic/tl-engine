/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.AbstractList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import com.top_logic.layout.WrappedModel;

/**
 * Adapter to wrap a root {@link TreeNode} as {@link TLTreeModel}.
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SwingTreeNodeTreeModelAdapter extends AbstractTreeModel<Object> implements WrappedModel {

    private final TreeNode root;
    
    /** is needed for {@link #getWrappedModel()} only and may be null. */
	private final TreeModel	impl;

    /**
     * Construct a {@link TLTreeModel} view with the given {@link TreeNode} as root node.
     */
    public SwingTreeNodeTreeModelAdapter(TreeModel impl) {
        this.root = (TreeNode) impl.getRoot();
		this.impl = impl;
    }
    
    @Override
	public Object getWrappedModel() {
    	return impl;
    }
    
	@Override
	public List<?> getChildren(final Object aParent) {
		return new AbstractList<>() {
            private TreeNode node = (TreeNode) aParent;

            @Override
			public Object get(int aIndex) {
                return node.getChildAt(aIndex);
            }

            @Override
			public int size() {
                return node.getChildCount();
            }
        };
    }

	@Override
	public boolean childrenInitialized(Object parent) {
		// Wrapped node is initialised completely.
		return true;
	}

	@Override
	public void resetChildren(Object parent) {
		// Ignore, not lazily initialized.
	}

    @Override
	public Object getParent(Object aNode) {
        // Make sure, that any node of a tree node tree can be used as root node of this tree model.
        if (aNode.equals(root)) {
            return null;
        }
        return ((TreeNode) aNode).getParent();
    }

    /**
     * @param aNode must not be <code>null</code>.
     */
    @Override
	public boolean containsNode(Object aNode) {
		if (!(aNode instanceof TreeNode)) {
			return false;
		}
        do {
            // Make sure to throw a NullPointerException, if the initial argument was null.
            if (aNode.equals(root)) {
                return true;
            }
            aNode = getParent(aNode);
        } while (aNode != null);

        return false;
    }
    
    @Override
	public Object getRoot() {
        return root;
    }

    @Override
	public boolean isLeaf(Object aNode) {
		return !((TreeNode) aNode).getAllowsChildren();
    }
    
	@Override
	public Object getBusinessObject(Object node) {
		if (node instanceof DefaultMutableTreeNode) {
			return ((DefaultMutableTreeNode) node).getUserObject();
		}
		return node;
	}

    public void setBusinessObject(Object aNode, Object businessObject) {
        ((DefaultMutableTreeNode) aNode).setUserObject(businessObject);
        if (hasListeners()) {
			fireTreeModelEvent(TreeModelEvent.NODE_CHANGED, aNode);
        }
    }

    public void addChild(Object aNode, Object aChild) {
        ((DefaultMutableTreeNode) aNode).add((MutableTreeNode) aChild);
        if (hasListeners()) {
			fireTreeModelEvent(TreeModelEvent.AFTER_NODE_ADD, aChild);
        }
    }
    
    public void removeNode(Object aNode) {
        DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) aNode;
        if (hasListeners()) {
			fireTreeModelEvent(TreeModelEvent.BEFORE_NODE_REMOVE, aNode);
        }
        dmtn.removeFromParent();
    }

	@Override
	public boolean hasChild(Object parent, Object node) {
		if (!(node instanceof TreeNode)) {
			return false;
		}
		TreeNode parentOfNode = ((TreeNode) node).getParent();
		return parent == parentOfNode;
	}

	@Override
	public boolean isFinite() {
		return false;
	}

}

