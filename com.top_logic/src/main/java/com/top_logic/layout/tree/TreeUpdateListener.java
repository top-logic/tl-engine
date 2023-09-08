/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.Attachable;
import com.top_logic.layout.IdentityProvider;
import com.top_logic.layout.InvalidationListener;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.util.Utils;

/**
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeUpdateListener extends TreeUIUpdateAccumulator<TreeUIModel> implements Attachable, TreeModelListener, SelectionListener {
	
	private TreeUIModel    model;
	private boolean        attached;
	private SelectionModel selectionModel;

	public TreeUpdateListener(IdentityProvider nodeIds, InvalidationListener invalidationCallback) {
		super(nodeIds, invalidationCallback);
	}

	@Override
	public boolean attach() {
		if (attached) {
			return false;
		}
		if (getModel() == null) {
			StringBuilder message = new StringBuilder();
			message.append("The ");
			message.append(TreeUpdateListener.class.getSimpleName());
			message.append(" does not have a tree model so it can not be attached.");
			throw new IllegalStateException(message.toString());
		}
		attached = true;

		getModel().addTreeModelListener(this);

		if (selectionModel != null) {
			selectionModel.addSelectionListener(this);
		}

		return true;
	}

	@Override
	public boolean detach() {
		if (! attached) {
			return false;
		}
		attached = false;
		
		assert getModel() != null: "If this listener is attached it must have a tree model";
		getModel().removeTreeModelListener(this);
		
		if (selectionModel != null) {
			selectionModel.removeSelectionListener(this);
		}
		clear();
		
		return true;
	}

	@Override
	public boolean isAttached() {
		return attached;
	}

	public final void setModel(TreeUIModel model) {
		if (model == null) {
			throw new IllegalArgumentException("The given " + TreeUIModel.class.getSimpleName()+ " must not be null");
		}
		
		boolean wasAttached = detach();
		
		this.model = model;
		
		if (wasAttached) {
			attach();
		}
	}

	public void setSelectionModel(SelectionModel newSelectionModel) {
		boolean wasAttached = detach();

		SelectionModel oldSelectionModel = this.selectionModel;
		
		this.selectionModel = newSelectionModel;

		if (wasAttached) {
			// Clear old selection
			invalidateNodes(oldSelectionModel.getSelection());

			// Establish new selection
			invalidateNodes(newSelectionModel.getSelection());

			// Update nodes that have changed selectable state.
			checkSelectableChange(getModel(), newSelectionModel, oldSelectionModel, getModel().getRoot());
		}
		
		if (wasAttached) {
			attach();
		}
	}

	/**
	 * Update nodes, whose {@link SelectionModel#isSelectable(Object)} states have changed.
	 */
	private void checkSelectableChange(TreeUIModel aModel, SelectionModel newSelection, SelectionModel oldSelection, Object node) {
		if (newSelection.isSelectable(node) != oldSelection.isSelectable(node)) {
			invalidateNode(aModel, node);
		}
		
		if (aModel.isExpanded(node)) {
			for (Iterator it = aModel.getChildIterator(node); it.hasNext(); ) {
				checkSelectableChange(aModel, newSelection, oldSelection, it.next());
			}
		}
	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		if (!isAttached()) {
			// It is possible that during the event handling this listener is
			// informed whereas it is already detached
			return;
		}
		TLTreeModel eventSource = (TLTreeModel) evt.getModel();
		Object changedNode = evt.getNode();

		switch (evt.getType()) {
			case TreeModelEvent.NODE_CHANGED:
				invalidateNode(getModel(), changedNode);
				break;

			case TreeModelEvent.AFTER_STRUCTURE_CHANGE:
				if (selectionModel != null) {
					final Set selection = selectionModel.getSelection();
					final int selectionCount = selection.size();
					if (selectionCount > 0) {
						// must iterate over copy as selection is changed during iteration
						final Object[] copy = selection.toArray(new Object[selectionCount]);
						for (Object formerSelectedObject : copy) {
							final Object bestMatch =
								TLTreeModelUtil.findBestMatch(eventSource, formerSelectedObject, changedNode);
							// no event if best match is the original node
							if (!Utils.equals(bestMatch, formerSelectedObject)) {
								selectionModel.setSelected(formerSelectedObject, false);
								selectionModel.setSelected(bestMatch, true);
							}
						}
					}
				}
				break;

			case TreeModelEvent.BEFORE_STRUCTURE_CHANGE: {
				// nothing to do in case, all necessary informations are available after structure
				// has changed
			}
				//$FALL-THROUGH$
			case TreeModelEvent.BEFORE_COLLAPSE:
			case TreeModelEvent.BEFORE_EXPAND:
				invalidateSubtree(getModel(), changedNode);
				break;

			case TreeModelEvent.AFTER_NODE_ADD:
				notifyAdd(changedNode);
				invalidateParent(changedNode);
				break;

			case TreeModelEvent.BEFORE_NODE_REMOVE: {
				if (selectionModel != null) {
					/*
					 * deselect all nodes in the subtree starting with the removed node
					 */
					Set currentSelection = selectionModel.getSelection();
					switch (currentSelection.size()) {
						case 0:
							break;
						case 1: {
							Object selectedObject = currentSelection.iterator().next();
							if (TLTreeModelUtil.isAncestor(eventSource, changedNode, selectedObject)) {
								selectionModel.setSelected(selectedObject, false);
							}
							break;
						}
						default: {
							// copy selection to avoid concurrent modification.
							Object[] selectionCopy = currentSelection.toArray();
							for (Object selectedObject : selectionCopy) {
								if (TLTreeModelUtil.isAncestor(eventSource, changedNode, selectedObject)) {
									selectionModel.setSelected(selectedObject, false);
								}
							}
							break;
						}
					}
				}
				invalidateParent(changedNode);
				break;
			}
		}

	}

	private void invalidateParent(Object changedNode) {
		Object parent = getModel().getParent(changedNode);
		if (parent == null) {
			invalidateSubtree(getModel(), getModel().getRoot());
		} else {
			invalidateSubtree(getModel(), parent);
		}
	}

	@Override
	public void notifySelectionChanged(SelectionModel senderModel, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		invalidateNodes(CollectionUtil.symmetricDifference(formerlySelectedObjects, selectedObjects));
	}

	private void invalidateNodes(Collection<?> nodes) {
		for (Iterator<?> it = nodes.iterator(); it.hasNext();) {
			invalidateNode(getModel(), it.next());
		}
	}

    public TreeUIModel getModel() {
        return this.model;
    }

	/**
	 * Detaches this {@link TreeUpdateListener}.
	 */
	@Override
	protected void invalidate() {
		super.invalidate();
		// can not handle some update so no need for further listening to the model.
		detach();
	}
	
}