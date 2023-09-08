/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

/**
 * A dedicated root node for Trees build of MergeTreeNodes.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class RootMergeNode extends MergeTreeNode {

    /**
     * Create an empty root Node.
     */
    public RootMergeNode() {
        super();
    }

    /**
     * Create a new RootMergeNode with a source Object.
     * 
     * @param   aSource arbitraty source Object.
     */
    public RootMergeNode(Object aSource) {
        source = aSource;
    }
    
    /**
     * Create a new RootMergeNode with a source Object.
     * 
     * @param   aSource arbitraty source Object.
     */
    public RootMergeNode(Object aSource, Object aDest) {
        source = aSource;
        dest   = aDest;
    }
    
    /** 
     * A root node cannot propagate messages, well.
     * 
     * @return always false
     */
    @Override
	protected boolean toBePropagated(int aLevel) {
        return false;
    }
}
