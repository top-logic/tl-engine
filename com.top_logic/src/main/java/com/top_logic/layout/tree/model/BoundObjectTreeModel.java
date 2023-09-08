/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;

/**
 * Implementation of {@link TreeModel} for {@link com.top_logic.tool.boundsec.BoundObject}s.
 *
 * Implements ModelBuilder to suppoert a simple Demo, too
 *
 * @author    <a href="mailto:khaa@top-logic.com">Klaus Halfmann</a>
 * @author    Dieter Rothb&auml;cher
 */
public class BoundObjectTreeModel implements TreeModel  {

    /** The root of the tree model */
    private BoundObject root;

    /**
     * Constructor
     */
    public BoundObjectTreeModel(BoundObject aRoot) {
        root = aRoot;
    }

    /**
     * Get the root object of this tree model
     * 
     * @return the root bound object of this tree model, never <code>null</code>
     */
    @Override
	public Object getRoot() {
        return this.root;
    }

    /**
     * Count the childs of the given bound object.
     * 
     * @param   parent      The parent for the child bound objects
     * @return the count of child bound objects for the given object
     */
    @Override
	public int getChildCount(Object parent) {
        BoundObject theParent = (BoundObject)parent;
        Collection theChilds = theParent.getSecurityChildren();
        if (theChilds == null) {
            return 0;
        }
        return theChilds.size();        
    }

    /**
     * Is the given bound object a leaf or not?
     * 
     * @param   node        The bound object to check if leaf or not
     * @return true, if given bound object is leaf 
     */
    @Override
	public boolean isLeaf(Object node) {
        BoundObject theNode = (BoundObject)node;
        Collection theChilds = theNode.getSecurityChildren();
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
     * Get the child bound object for the given parent bound object.
     * 
     * @param   parent      The parent bound object
     * @param   index       The index of the child object to get
     * @return the child object
     */
    @Override
	public Object getChild(Object parent, int index) {
        Collection childs = ((BoundObject)parent).getSecurityChildren();
        if (childs != null) {
            if (childs instanceof List) {
                return ((List) childs).get(index);
            }
            // else do it the hard way ...
            Iterator allChilds = childs.iterator();
            Object theRequestedObject = null;
            for (int i = 0; i <= index; i++) {
                theRequestedObject = allChilds.next();
            }
            return theRequestedObject;
        }
        return null;   // oops, we have no childs
    }

    /**
     * Get the index of the child bound object for the parent bound object.
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
        BoundObject theParent = (BoundObject) parent;
        BoundObject theChild = (BoundObject) child;
        
        Collection childs = theParent.getSecurityChildren();
        if (childs != null) {
            Iterator allChilds = childs.iterator();
            int index = 0;
            while (allChilds.hasNext()) {
                BoundObject currentChild = (BoundObject) allChilds.next();
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

    /**
     * Create a simple DemoModel from some {@link SimpleBoundObject}s.
     */
    public static class DemoModelBuilder implements ModelBuilder {
        
		/**
		 * Singleton {@link DemoModelBuilder} instance.
		 */
		public static final DemoModelBuilder INSTANCE = new DemoModelBuilder();

		private DemoModelBuilder() {
			// Singleton constructor.
		}

        /**
         * Create a simple DemoModel from some {@link SimpleBoundObject}s.
         */
        @Override
		public Object getModel(Object businessModel, LayoutComponent aComponent) {
			SimpleBoundObject rootO = new SimpleBoundObject("root");
			SimpleBoundObject prjO = new SimpleBoundObject("Project");
			SimpleBoundObject subPrjO = new SimpleBoundObject("SubProject");
			SimpleBoundObject taskO = new SimpleBoundObject("Task");
            rootO.addChild(prjO);
            prjO.addChild (subPrjO);
            prjO.addChild (taskO);
            BoundObjectTreeModel objectModel = new BoundObjectTreeModel(rootO);
    
            return objectModel;
        }

        /** 
         * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, LayoutComponent)
         */
        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
            return (false);
        }
    }
}
