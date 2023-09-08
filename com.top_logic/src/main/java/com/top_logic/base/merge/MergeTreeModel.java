/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.layout.tree.model.AbstractTLTreeNodeModel;

/**
 * A TLTreeModel based one {@link MergeTreeNode}s.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class MergeTreeModel extends AbstractTLTreeNodeModel<AbstractMergeNode> {

    /** The Root defining this Tree */
    protected RootMergeNode rootNode;
    
    /** 
     * Create a new MergeTreeModelbade on given root node.
     */
    public MergeTreeModel(RootMergeNode aRootNode) {
        this.rootNode = aRootNode;
    }

    /**
     * @see com.top_logic.layout.tree.model.TLTreeModel#getRoot()
     */
	@Override
	public AbstractMergeNode getRoot() {
        return rootNode;
    }

	@Override
	public boolean childrenInitialized(AbstractMergeNode parent) {
		return parent.isInitialized();
	}

	@Override
	public void resetChildren(AbstractMergeNode parent) {
		// Ignore, not lazily initialized.
	}

    @Override
	public boolean hasChildren(AbstractMergeNode aNode) {
		return aNode.getChildCount() > 0;
    }

    @Override
	public boolean containsNode(AbstractMergeNode aNode) {
        if (! (aNode instanceof MergeTreeNode)) {
            return false; 
        }
        MergeTreeNode theNode = (MergeTreeNode) aNode;
        while (theNode != rootNode && theNode != null) {
            theNode = theNode.getMergeParent();
        }

        return theNode == rootNode;
    }

	@Override
	public boolean isFinite() {
		return false;
	}

}

