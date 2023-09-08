/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.List;

import com.top_logic.layout.IdentityProvider;
import com.top_logic.layout.InvalidationListener;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Computes a set of non-overlapping UI updates from a number of
 * {@link TreeUIModel} changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeUIUpdateAccumulator<M extends TreeUIModel> extends TreeUpdateAccumulator<M> {

	private final IdentityProvider nodeIds;

	/**
	 * Callback which is triggered when this {@link TreeUpdateAccumulator} can not handle some
	 * update.
	 */
	private final InvalidationListener _invalidationCallback;
	
	public TreeUIUpdateAccumulator(IdentityProvider nodeIds, InvalidationListener invalidationCallback) {
		if (invalidationCallback == null) {
			throw new IllegalArgumentException("This " + TreeUpdateAccumulator.class.getName()
				+ " needs a callback for invalidation");
		}
		_invalidationCallback = invalidationCallback;
		
		assert nodeIds != null : "Identity provider must not be null.";
		this.nodeIds = nodeIds;
	}

	@Override
	public void invalidateSubtree(M model, Object subroot) {
		if (subroot.equals(model.getRoot()) && !model.isRootVisible()) {
			invalidateInvisibleRoot(model, subroot);
			return;
		}
		
		super.invalidateSubtree(model, subroot);
	}

	@Override
	protected String computeRightmostVisibleDescenantId(M model, Object subroot) {
        Object rightmostVisibleDescendant = subroot;
		String rightmostVisibleDescendantId;
        while (true) {
			if (! model.isExpanded(rightmostVisibleDescendant)) {
				rightmostVisibleDescendantId = getNodeId(rightmostVisibleDescendant);
				break;
			}
			
			List children = model.getChildren(rightmostVisibleDescendant);
			if (children.isEmpty()) {
				rightmostVisibleDescendantId = getNodeId(rightmostVisibleDescendant);
				break;
			}
			
			int lastDisplayedChildIndex = children.size() - 1;
			while (lastDisplayedChildIndex >= 0) {
			    Object child = children.get(lastDisplayedChildIndex);
				if (!isNewNode(child)) {
			        break;
			    }
			        
			    lastDisplayedChildIndex--;
			}
			
			if (lastDisplayedChildIndex < 0) {
                rightmostVisibleDescendantId = getNodeId(rightmostVisibleDescendant);
                break;
			}
			
			Object lastDisplayedChild = children.get(lastDisplayedChildIndex);
			
			Object update = getUpdateForNode(lastDisplayedChild);
		    if (update instanceof TreeUIUpdateAccumulator.SubtreeUpdate) {
		        rightmostVisibleDescendantId = ((TreeUIUpdateAccumulator.SubtreeUpdate) update).getRightmostVisibleDescendantId();
				removeUpdateForNode(lastDisplayedChild);
		        break;
		    }
			
			rightmostVisibleDescendant = lastDisplayedChild;
		}
        return rightmostVisibleDescendantId;
	}

	@Override
	protected String getNodeId(Object node) {
		return nodeIds.getObjectId(node);
	}

	@Override
	protected boolean isRelevantUpdate(M model, Object subroot) {
		if (! model.isDisplayed(subroot)) {
			return false;
		}
		if (model.getRoot() == subroot) {
			if (!model.isRootVisible()) {
				return false;
			}
		} else {
			if (!model.containsNode(subroot)) {
				return false;
			}
		}
		
		// Check, whether the changed node is a descendant of a currently
		// repainted subtree.
		if (hasAncestorOrSelfWithSubtreeUpdate(model, subroot)) {
			return false;
		}
		
		return true;
	}

	protected boolean hasAncestorOrSelfWithSubtreeUpdate(M model, Object node) {
		Object ancestor = node;
		
		while (true) {
			if (getUpdateForNode(ancestor) instanceof TreeUIUpdateAccumulator.SubtreeUpdate) {
				// Node currently being repainted.
				return true;
			}
			
			ancestor = model.getParent(ancestor);
			if (ancestor == null) {
				return false;
			}
		}
	}
	
	/**
	 * Adds an update for an invisible root node to the cache.
	 * 
	 * @param model
	 *        the {@link TreeUIModel} whose root is not {@link TreeUIModel#isRootVisible() visible}
	 * @param invisibleRoot
	 *        the invisible root for which an update must be created
	 */
	protected void invalidateInvisibleRoot(M model, Object invisibleRoot) {
		if (!model.childrenInitialized(invisibleRoot)) {
			/* There are no children on the client, because children are not yet initialized. No
			 * updates possible. */
			invalidate();
			return;
		}
		final List rootChildren = model.getChildren(invisibleRoot);
		int size = rootChildren.size();

		// find the first child which is already visible at the GUI to determine
		// a correct client side id
		String startID = null;
		for (int index = 0; index < size; index++) {
			Object firstVisibleChild = rootChildren.get(index);
			if (!isNewNode(firstVisibleChild)) {
				startID = getNodeId(firstVisibleChild);
				break;
			}
		}
		if (startID == null) {
			// There are no children on the client. No updates possible
			invalidate();
			return;
		}

		// find the last child which is already visible at the GUI to determine
		// a correct client side id
		String stopID = null;
		for (int index = size - 1; index >= 0; index--) {
			Object lastVisibleChild = rootChildren.get(index);
			if (!isNewNode(lastVisibleChild)) {
				stopID = getNodeId(lastVisibleChild);
				break;
			}
		}

		if (getUpdateForNode(invisibleRoot) != null) {
			invalidate();
		} else {
			// update for root. all other updates are unnecessary
			clearNodeUpdates();
			addUpdate(new SubtreeUpdate(invisibleRoot, startID, stopID));
		}
	}

	/**
	 * This method is called when an event can not be handled by this {@link TreeUpdateAccumulator}
	 */
	protected void invalidate() {
		_invalidationCallback.notifyInvalid(this);
	}

}