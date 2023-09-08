/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Adapt a {@link TLTreeModel} with display related aspects to a {@link TreeUIModel}.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStructureTreeUIModel<N> extends TreeStructureModelAdaptor<N> implements TreeUIModel<N> {
	
	/**
	 * {@link UINodeProperties} by application model node.
	 */
	private HashMap<Object, UINodeProperties> uiNodeByNode = new HashMap<>();
	
	/**
	 * Creates a {@link AbstractStructureTreeUIModel}.
	 *
	 * @param applicationModel See {@link TreeModelAdapter#TreeModelAdapter(TreeModelBase)}.
	 */
	public AbstractStructureTreeUIModel(TLTreeModel<N> applicationModel) {
		super(applicationModel);
	}
	
	@Override
	public boolean isDisplayed(N node) {
		UINodeProperties entry = getEntryByNode(node);
		if (entry == null) return false;

		return entry.isDisplayed();
	}
	
	@Override
	public boolean setExpanded(N node, boolean expanded) {
		UINodeProperties entry = getEntryByNode(node);

		if (entry.isExpanded() == expanded) {
			return false;
		}
		if (!expanded && node == getRoot() && !isRootVisible()) {
			// Do not collapse invisible root node.
			return false;
		}
		if (hasListeners()) {
			fireTreeModelEvent(expanded ? TreeModelEvent.BEFORE_EXPAND : TreeModelEvent.BEFORE_COLLAPSE, node);
		}

		entry.setExpanded(expanded);

		// The visibility state of children of the current node changes, if
		// the current node is displayed. If the current node is indisplayed,
		// nothing changes, because an indisplayed node may only have
		// indisplayed childs.
		if (entry.isDisplayed()) {
			boolean newChildVisibility = expanded;
			setChildrenDisplayed(node, newChildVisibility);
		}

		if (hasListeners()) {
			fireTreeModelEvent(expanded ? TreeModelEvent.AFTER_EXPAND : TreeModelEvent.AFTER_COLLAPSE, node);
		}

		return true;
	}

	private void setChildrenDisplayed(N node, boolean newVisibility) {
		for (Iterator<? extends N> it = getChildIterator(node); it.hasNext();) {
    	    setDisplayed(it.next(), newVisibility);
    	}
	}

	private void setDisplayed(N node, boolean newVisibility) {
        UINodeProperties entry = getEntryByNode(node);
        
		boolean visibilityChanged = newVisibility != entry.isDisplayed();
		if (visibilityChanged) {
			entry.setDisplayed(newVisibility);
			
			if (entry.isExpanded()) {
				setChildrenDisplayed(node, newVisibility);
			}
		}
    }
    
	@Override
	public boolean isExpanded(N node) {
		UINodeProperties entry = getEntryByNode(node);
		if (entry == null) return false;

		return entry.isExpanded();
	}

	@Override
	public Object getUserObject(N node) {
		return getBusinessObject(node);
	}

	/**
	 * Checks, whether the given node is currently represented in this tree
	 * state.
	 */
	/* private */boolean containsUINode(N node) {
		return uiNodeByNode.containsKey(node);
	}

	/**
	 * Removes the given node from this tree state object.
	 * 
	 * @return <code>true</code>, if the object way formerly part of this
	 *         tree state object, <code>false</code> otherwise.
	 */
	/* private */boolean removeUINode(N node) {
	    
	    // TODO avoid unneeded creation of UINodeProperty
		UINodeProperties entry = getEntryByNode(node);
		if (entry == null) {
		    return false; // Unreacheable ?
		}
		
		uiNodeByNode.remove(node);
		
		return true;
	}
	
	private UINodeProperties getEntryByNode(N node) {
		Object result = uiNodeByNode.get(node);
		
		if (result == null) {
			result = enter(node);
		}
		
		return (UINodeProperties) result;
	}
	
	/**
	 * Enters the given node linked to the parent node identified by the
	 * given parent identifier.
	 */
	private UINodeProperties enter(N node) {
		UINodeProperties entry = new UINodeProperties(node);
		
		initEntry(node, entry);
		
		uiNodeByNode.put(node, entry);
		
		return entry;
	}

	private void initEntry(N node, UINodeProperties entry) {
		N parent = getParent(node);
		if (parent == null) {
			entry.setDisplayed(true);
			if (!isRootVisible()) {
				// expand root automatically if not visible.
				entry.setExpanded(true);
			}
		} else {
			entry.setDisplayed(isDisplayed(parent) && isExpanded(parent));
		}
	}
	
	final static class UINodeProperties {
	    
	    /**
	     * @see #getUserObject()
	     */
	    private final Object userObject;
	    
	    /**
	     * @see #isExpanded()
	     */
	    private boolean expanded;
	    
	    private boolean displayed;
	    
	    public UINodeProperties(Object aNode) {
	        this.userObject = aNode;
	    }

		public Object getUserObject() {
			return userObject;
		}

		public boolean isExpanded() {
			return expanded;
		}
		
		public void setExpanded(boolean expanded) {
			this.expanded = expanded;
		}
		
		public boolean isDisplayed() {
			return displayed;
		}
		
		public void setDisplayed(boolean displayed) {
			this.displayed = displayed;
		}
		
	}

}