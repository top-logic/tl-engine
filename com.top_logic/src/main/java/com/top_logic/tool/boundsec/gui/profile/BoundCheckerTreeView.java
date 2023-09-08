/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TreeView;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * {@link TreeView} of {@link LayoutComponent}s.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class BoundCheckerTreeView implements TreeView {

    Filter childChecker;

    /**
     * Creates a {@link BoundCheckerTreeView}.
     *
     * @param achildChecker A filter that selects children.
     */
    public BoundCheckerTreeView(Filter achildChecker) {
        this.childChecker = achildChecker;
    }

    /**
     * @see com.top_logic.basic.col.TreeView#getChildIterator(java.lang.Object)
     */
    @Override
	public Iterator<?> getChildIterator(Object aNode) {
        if (aNode instanceof LayoutComponent) {
            Collection theChildren = new ArrayList();
            this.fillChildren((LayoutComponent) aNode, theChildren);
            return theChildren.iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * @see com.top_logic.basic.col.TreeView#isLeaf(java.lang.Object)
     */
    @Override
	public boolean isLeaf(Object aNode) {
		if (aNode instanceof LayoutComponent) {
			return !hasChildren((LayoutComponent) aNode);
		}
		return true;
    }

	/**
	 * Method filling the children of the given component.
	 * 
	 * <p>
	 * Must be synchron to {@link #hasChildren(LayoutComponent)}
	 * </p>
	 */
    private void fillChildren(LayoutComponent aNode, Collection someChildren) {
        fillChildren(someChildren, aNode.getDialogs());
        if (aNode instanceof LayoutContainer) {
            fillChildren(someChildren, ((LayoutContainer)aNode).getChildList());
        }
    }

	private void fillChildren(Collection someChildren, Collection<? extends LayoutComponent> theChildList) {
		for (Object theChild : theChildList) {
            if (this.childChecker.accept(theChild)) {
                someChildren.add(theChild);
            } else if (theChild instanceof LayoutComponent) {
                this.fillChildren((LayoutComponent) theChild, someChildren);
            }
        }
    }

	/**
	 * Method checks whether the given component has children.
	 * 
	 * <p>
	 * Must be synchron to {@link #fillChildren(LayoutComponent, Collection)}
	 * </p>
	 */
	private boolean hasChildren(LayoutComponent aNode) {
		boolean hasChildren = hasChildren(aNode.getDialogs());
		if (!hasChildren && aNode instanceof LayoutContainer) {
			hasChildren = hasChildren(((LayoutContainer) aNode).getChildList());
		}
		return hasChildren;
	}

	private boolean hasChildren(Collection<? extends LayoutComponent> theChildList) {
		for (Object theChild : theChildList) {
			if (this.childChecker.accept(theChild)) {
				return true;
			} else if (theChild instanceof LayoutComponent) {
				boolean hasChildren = this.hasChildren((LayoutComponent) theChild);
				if (hasChildren) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

