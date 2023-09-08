/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.tool.boundsec.BoundChecker;

/**
 * Reduced Version of BoundCheckerTreeModel.
 * 
 * This class is not used any more, only its subclass 
 * (<code>CompoundSecurityLayoutTreeModel</code> is used. So only the
 * subclass will be mrigrated to the new aproach
 * 
 * @deprecated use new Tree aproach.
 * 
 * Retunrs only the parts necessary to set the security settings
 * in the BoundRole administration.
 * 
 * @author    <a href="mailto:kbu@top-logic.com"></a>
 */
@Deprecated
public class BoundCheckerRoleProfileTreeModel extends BoundCheckerTreeModel {

	/**
	 * Constructor with the tree root node.
	 * 
	 * @param aRoot	the root BoundChecker
	 */
	public BoundCheckerRoleProfileTreeModel(BoundChecker aRoot) {
		super(aRoot);
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		return this.getChildren((BoundChecker) parent).get(index);
	}

	protected List getChildren (BoundChecker aChecker) {
		List theChildren = new ArrayList();
		List theRepresentedChildren = new ArrayList();
		this.getChildren(aChecker, theChildren, theRepresentedChildren);

		return theChildren;		
	}

    /** KHA this function is never used !?  */
	public List getRepresentedChildren (BoundChecker aChecker) {
		List theChildren = new ArrayList();		
		List theRepresentedChildren = new ArrayList();
		this.getChildren(aChecker, theChildren, theRepresentedChildren);

		return theRepresentedChildren;		
	}

	protected void getChildren (BoundChecker aChecker, List aChildList, List aRepresentedList) {
		if (aChecker != null) {
			Collection<? extends BoundChecker> theBoundChildren = aChecker.getChildCheckers();
			if (theBoundChildren != null) {
				Iterator<? extends BoundChecker> theBoundChildrenIt = theBoundChildren.iterator();
				while (theBoundChildrenIt.hasNext()) {
					BoundChecker theBoundChild = theBoundChildrenIt.next();
					
					aRepresentedList.add (theBoundChild);
					
					if (!(theBoundChild instanceof TableComponent)
						&& theBoundChild.getCommandGroups() != null && !theBoundChild.getCommandGroups().isEmpty()) {
						aChildList.add(theBoundChild); // Show layouts with command groups
					}
					else {
						this.getChildren(theBoundChild, aChildList, aRepresentedList);
					}
				}
			}
		}
	}

	/**
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		return this.getChildren((BoundChecker) node).isEmpty();
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		return this.getChildren((BoundChecker) parent).size();
	}

	/**
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == null || child == null) {
			return -1;
		}

		return this.getChildren((BoundChecker) parent).indexOf(child);
	}

}
