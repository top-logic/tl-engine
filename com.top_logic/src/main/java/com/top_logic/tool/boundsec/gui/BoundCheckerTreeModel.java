/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.top_logic.tool.boundsec.BoundChecker;

/**
 * Implementation of the {@link javax.swing.tree.TreeModel} for
 * {@link com.top_logic.tool.boundsec.BoundChecker}s.
 * 
 * @deprecated use new Tree aproach
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
@Deprecated
public class BoundCheckerTreeModel implements TreeModel {

    /** The root of the tree model */
    private BoundChecker root;

    /**
     * Constructor
     */
    public BoundCheckerTreeModel(BoundChecker aRoot) {
        root = aRoot;
    }

    /**
     * Get the root object of this tree model
     * 
     * @return the root bound checker of this tree model, never <code>null</code>
     */
    @Override
	public Object getRoot() {
        return this.root;
    }

    /**
     * Count the childs of the given bound checker.
     * 
     * @param   parent      The parent for the child bound checkers
     * @return the count of child bound checkers for the given object
     */
    @Override
	public int getChildCount(Object parent) {
        BoundChecker theParent = (BoundChecker)parent;
		Collection<?> theChilds = theParent.getChildCheckers();
        if (theChilds == null) {
            return 0;
        }
        return theChilds.size();        
    }

    /**
     * Is the given bound checker a leaf or not?
     * 
     * @param   node        The bound checker to check if leaf or not
     * @return true, if given bound checker is leaf 
     */
    @Override
	public boolean isLeaf(Object node) {
        BoundChecker theNode = (BoundChecker)node;
		Collection<?> theChilds = theNode.getChildCheckers();
        return (theChilds == null || theChilds.size()==0);
    }

    /**
     * Not implemented so far
     * 
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    @Override
	public void addTreeModelListener(TreeModelListener l) {
        // not needed so far
    }

    /**
     * Not implemented so far
     * 
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    @Override
	public void removeTreeModelListener(TreeModelListener l) {
        // not needed so far
    }

    /**
     * Get the child bound checker for the given parent bound checker.
     * 
     * @param   parent      The parent bound checker
     * @param   index       The index of the child checker to get
     * @return the child checker
     */
    @Override
	public Object getChild(Object parent, int index) {
		Collection<?> childs = ((BoundChecker) parent).getChildCheckers();
        if (childs != null) {
			if (childs instanceof RandomAccess) {
				return ((List<?>) childs).get(index);
            }
			Iterator<?> allChilds = childs.iterator();
            Object theRequestedChecker = null;
            for (int i = 0; i <= index; i++) {
                theRequestedChecker = allChilds.next();
            }
            return theRequestedChecker;
        }
        return null;   // oops, we have no childs
    }

    /**
     * Get the index of the child checker for the parent checker.
     * 
     * @param   parent      The parent
     * @param   child       The child
     * @return the index if the child
     */
    @Override
	public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return -1;
        }
        BoundChecker theParent = (BoundChecker) parent;
        BoundChecker theChild = (BoundChecker) child;
        
		Collection<?> childs = theParent.getChildCheckers();
        if (childs != null) {
			Iterator<?> allChilds = childs.iterator();
            int index = 0;
            while (allChilds.hasNext()) {
                BoundChecker currentChild = (BoundChecker) allChilds.next();
                if (currentChild.equals(theChild)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     * Not implemented so far
     *
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    @Override
	public void valueForPathChanged(TreePath path, Object newValue) {
        // not needed so far
    }

}
