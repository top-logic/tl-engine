/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.element.core.util.TLElementIDFilter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.layout.tree.model.TreeModelEvent;


/**
 * Tree model for a tree of {@link com.top_logic.element.structured.StructuredElement elements}.
 * 
 * The default tree model will not use any filter for display, one is free to
 * create a subclass, which will include such filter.
 * TODO so what does displayFilter do up to now? (dkh)
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredElementTreeModel extends AbstractTreeModel<StructuredElement> {
	
	private static final NamedConstant NOT_IN_TREE = new NamedConstant("notInTree");

    /** The root element of the model. */
    private StructuredElement root;

    /** The filter used for the tree. */
    private Filter<? super StructuredElement> displayFilter;

    /**
     * @param    aRoot    The root node of the model, must not be <code>null</code>.
     * @throws   IllegalArgumentException    If the given root node is null.
     */
    public StructuredElementTreeModel(StructuredElement aRoot) {
        this(aRoot, null);
    }

    /**
     * @param    aRoot      The root node of the model, must not be <code>null</code>.
     * @param    aFilter    The filter to be used when asking for children.
     * @throws   IllegalArgumentException    If the given root node is null.
     */
    public StructuredElementTreeModel(StructuredElement aRoot, Filter<? super StructuredElement> aFilter) {
        super();

        if (aRoot == null) {
            throw new IllegalArgumentException("Given root node is null");
        }

        this.root          = aRoot;
        this.displayFilter = aFilter;
    }

    @Override
	public StructuredElement getRoot() {
        return (this.root);
    }

    public int getChildCount(StructuredElement someParent) {
        return (this.getChildren(someParent).size()); 
    }

    /**
     * Check is leaf via StructuredElement.hasChildren.
     */
    @Override
	public boolean isLeaf(StructuredElement aParent) {
        return !aParent.hasChildren(this.getTreeFilter());
    }

	/**
	 * Returns the given node
	 * 
	 * @see com.top_logic.layout.tree.model.TLTreeModel#getBusinessObject(java.lang.Object)
	 */
	@Override
	public Object getBusinessObject(StructuredElement node) {
		return node;
	}


    /**
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(StructuredElement someParent, int someIndex) {
        return (this.getChildren(someParent).get(someIndex));
    }

    /**
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(StructuredElement someParent, StructuredElement someChild) {
        return (this.getChildren(someParent).indexOf(someChild));
    }

    /**
     * @param    anID    The ID the filter should use for look up, must not be <code>null</code>.
     * @return   The requested filter, must not be <code>null</code>.
     */
    protected Filter getStructuredElementIDFilter(TLID anID) {
        return (new TLElementIDFilter(anID));
    }

    /**
     * Return the filter to be used when working in this tree.
     * 
     * @return    The filter to be used for filtering this tree, may be <code>null</code>.
     */
    public Filter<? super StructuredElement> getTreeFilter() {
        return (this.displayFilter);
    }

    @Override
	public List<? extends StructuredElement> getChildren(StructuredElement aParent) {
        return aParent.getChildren(this.getTreeFilter());
    }

	@Override
	public boolean childrenInitialized(StructuredElement parent) {
		// Wrapped node is initialised completely.
		return true;
	}

	@Override
	public void resetChildren(StructuredElement parent) {
		// Ignore, not lazily initialized.
	}

	@Override
	public StructuredElement getParent(StructuredElement aChild) {
    	Object theResult = internalGetParent(aChild);
    	
    	if (NOT_IN_TREE == theResult){
    		throw new IllegalArgumentException("The given child is not part of this tree.");
    	}
    	else {
    		return (StructuredElement) theResult;
    	}
    }

	private Object internalGetParent(StructuredElement aChild) {
		if (isNotAccepted(aChild)) {
			return NOT_IN_TREE;
    	}
        if (aChild == root) {
            return null; // TreeModel may be a subtree of complete structure
        }
        return aChild.getParent();
	}

	private boolean isNotAccepted(StructuredElement aChild) {
		Filter<? super StructuredElement> treeFilter = getTreeFilter();
		return aChild != root && treeFilter != null && !treeFilter.accept(aChild);
	}
    
    @Override
	public boolean hasChild(StructuredElement parent, Object node) {
		if (!(node instanceof StructuredElement)) {
			return false;
		}
		Object parentOfNode = internalGetParent((StructuredElement) node);
		return parent.equals(parentOfNode);
    }
    
    @Override
	public List<StructuredElement> createPathToRoot(StructuredElement aNode) {
        ArrayList<StructuredElement> result = new ArrayList<>();
        StructuredElement element = aNode;
        while (element != null) {
            result.add(element);
			Object parentElement = internalGetParent(element);
			if (parentElement != NOT_IN_TREE) {
				element = (StructuredElement) parentElement;
			} else {
				break;
			}
        }
        if (!result.isEmpty() && result.get(result.size() - 1).equals(root)) {
        	return result;
        } else {
        	return Collections.emptyList();
        }
    }
    
    @Override
	public boolean containsNode(StructuredElement aNode) {
    	if(aNode == null) {
			throw new IllegalArgumentException("Cannot compute containment for node 'null'!");
    	}
    	
		StructuredElement element = aNode;
		Object parentElement = aNode;
		while (parentElement != null) {
			if (parentElement != NOT_IN_TREE) {
				element = (StructuredElement) parentElement;
			} else {
				break;
			}
			parentElement = internalGetParent(element);
		}
		return element == root;
    }

    @Override
	public boolean hasChildren(StructuredElement aNode) {
        return aNode.hasChildren(getTreeFilter());
    }

    /**
     * Call {@link StructuredElement#move(StructuredElement, StructuredElement)} and fire appropriate Event.
     * 
     * TODO can I use a better type of Event?
     */
    public boolean move(StructuredElement aParent, StructuredElement aChild, StructuredElement aBefore) {
        if (hasListeners()) {
			fireTreeModelEvent(TreeModelEvent.BEFORE_NODE_REMOVE, aChild);
            fireTreeModelEvent(TreeModelEvent.BEFORE_NODE_REMOVE, aBefore);
        }
        boolean result = aParent.move(aChild, aBefore);
        if (result && hasListeners()) {
			fireTreeModelEvent(TreeModelEvent.AFTER_NODE_ADD, aChild);
            fireTreeModelEvent(TreeModelEvent.AFTER_NODE_ADD, aBefore);
        }
        return result;
    }

	@Override
	public boolean isFinite() {
		return true;
	}

}
