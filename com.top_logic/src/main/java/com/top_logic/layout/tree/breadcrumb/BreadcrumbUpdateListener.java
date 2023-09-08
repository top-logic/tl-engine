/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractAttachable;
import com.top_logic.layout.tree.TreeUpdateAccumulator.NodeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.SubtreeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.UpdateVisitor;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;

/**
 * The class {@link BreadcrumbUpdateListener} belongs to some
 * {@link BreadcrumbControl} and handles the updates to the displayed tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class BreadcrumbUpdateListener extends AbstractAttachable implements TreeModelListener {

	/**
	 * Map that holds updates for nodes
	 */
	private final Map<Object, NodeUpdate> updateByNode = new HashMap<>();

	/**
	 * the path which is currently visible at the GUI
	 */
	private List<?> currentPath;

	/**
	 * THe {@link BreadcrumbControl} this update listener belongs to.
	 */
	private final BreadcrumbControl control;

	public BreadcrumbUpdateListener(BreadcrumbControl control) {
		this.control = control;
	}

	/**
	 * recomputes the currently active path of the bread crumb.
	 */
	private void updateCurrentPath() {
		final Object singleSelection = control.getLastNode();
		currentPath = control.getTree().createPathToRoot(singleSelection);
	}

	/**
	 * determines whether updates are available
	 */
	boolean hasUpdates() {
		return !updateByNode.isEmpty();
	}

	@Override
	protected void internalAttach() {
		control.getTree().addTreeModelListener(this);
		updateCurrentPath();
	}

	@Override
	protected void internalDetach() {
		clear();
		control.getTree().removeTreeModelListener(this);
	}

	/**
	 * resets this listener
	 */
	private void clear() {
		updateByNode.clear();
		currentPath = null;
	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		checkAttached();
		
		TLTreeModel observedTree = control.getTree();
		if (evt.getModel() != observedTree) {
			return;
		}

		Object touchedNode = evt.getNode();
		switch (evt.getType()) {
			case TreeModelEvent.AFTER_NODE_ADD:
				Object changedParent = observedTree.getParent(touchedNode);
				if (currentPath.contains(changedParent)) {
					addNodeUpdate(changedParent);
				}
				break;
				
			case TreeModelEvent.BEFORE_NODE_REMOVE:
				Object parentStillInTree = observedTree.getParent(touchedNode);
				final boolean updateAdded = addSubtreeUpdate(parentStillInTree);
				if (updateAdded) {
					control.setLastNode(parentStillInTree);
				}
				break;
			case TreeModelEvent.BEFORE_STRUCTURE_CHANGE:
				// nothing to do in case, all necessary informations are available after structure
				// has changed
				break;
			case TreeModelEvent.AFTER_STRUCTURE_CHANGE:
				addSubtreeUpdate(touchedNode);
				final Object oldLeadingNode = currentPath.get(0);
				final Object bestMatch = TLTreeModelUtil.findBestMatch(observedTree, oldLeadingNode, touchedNode);
				control.setLastNode(bestMatch);
				final SingleSelectionModel selectionModel = control.getSelectionModel();
				if (selectionModel != null) {
					final Object currentSelection = selectionModel.getSingleSelection();
					if (currentSelection != null) {
						Object bestSelectionMatch =
							TLTreeModelUtil.findBestMatch(observedTree, currentSelection, touchedNode);
						selectionModel.setSingleSelection(bestSelectionMatch);
					}
				}
				break;
			default:
				return;
		}
	}

	/**
	 * checks whether there is a parent node of the given node for which an
	 * update was constructed.
	 */
	private boolean hasSuptreeUpdateForParent(Object node) {
		while (node != null) {
			final NodeUpdate potentialUpdate = updateByNode.get(node);
			if (potentialUpdate instanceof SubtreeUpdate) {
				return true;
			}
			node = control.getTree().getParent(node);
		}
		return false;
	}

	/**
	 * Adds an update for the given node
	 */
	private void addNodeUpdate(Object node) {
		if (hasSuptreeUpdateForParent(node)) {
			return;
		}
		updateByNode.put(node, new NodeUpdate(node, control.getNodeId(node)));
	}

	/**
	 * Adds an update for the whole subtree with given node as root.
	 * 
	 * If the given node is not contained in the displayed path, or a parent of the node was
	 * invalidated before, then nothing is done.
	 * 
	 * @return <code>true</code> iff an update for the given node was created.
	 */
	private boolean addSubtreeUpdate(Object node) {
		final int indexOfNode = currentPath.indexOf(node);
		if (indexOfNode < 0) {
			// node is not displayed by the control so no updates are build
			return false;
		}
		if (hasSuptreeUpdateForParent(node)) {
			// some parent node is already invalid
			return false;
		}
		final Object currentLeadingNode = currentPath.get(0);

		// remove all updates build before for nodes below the given node
		for (int index = indexOfNode; index >= 0; index--) {
			updateByNode.remove(currentPath.get(index));
		}

		updateByNode.put(node, new SubtreeUpdate(node, control.getNodeId(node), control.getNodeId(currentLeadingNode)));
		return true;
	}

	/**
	 * uses the renderer of its control to build incremental AJAX updates for
	 * the build {@link NodeUpdate}s and adds them to the given queue.
	 */
	void revalidate(UpdateQueue actions) {
		checkAttached();
		final UpdateVisitor<?, UpdateQueue, BreadcrumbControl> updater = control.getRenderer().getUpdater();
		for (Entry<Object, NodeUpdate> updateEntry : updateByNode.entrySet()) {
			NodeUpdate update = updateEntry.getValue();
			update.visit(updater, actions, control);
		}
		clear();
		updateCurrentPath();
	}

	/**
	 * handles a change of the selection in the bread crumb
	 */
	void handleSelectionChanged(Object formerlySelectedObject, Object selectedObject) {
		checkAttached();
		
		// the new selection is not in tree. might be a failure so a repaint is
		// needed
		final TLTreeModel tree = control.getTree();
		
		if (selectedObject != null) {
			// otherwise selection was cleared
			if (currentPath.contains(selectedObject)) {
				addNodeUpdate(selectedObject);
			} else {
				// if the selection was changed to a not displayed node that node
				// will also be the last node
				control.setLastNode(selectedObject);
			}
		}
		
		// If the formerly selected node is not longer in the tree, then not only
		// the selection has changed so some other method cares about.
		if (tree.containsNode(formerlySelectedObject) && currentPath.contains(formerlySelectedObject)) {
			addNodeUpdate(formerlySelectedObject);
		}
	}

	/**
	 * Handles a change of the last node of the bread crumb
	 * 
	 * @param designatedLeadingNode
	 *        the new last node
	 */
	void handleLastNodeChanged(Object designatedLeadingNode) {
		checkAttached();
		
		final TLTreeModel tree = control.getTree();
		if (!tree.containsNode(designatedLeadingNode)) {
			control.requestRepaint();
			return;
		}
		
		final Object currentLeadingNode = currentPath.get(0);
		/*
		 * The current last node could be removed from the tree before another
		 * node was selected. So it is necessary to check whether it is still in
		 * the tree.
		 */
		if (tree.containsNode(currentLeadingNode)) {
			final Object leastAncestor = TLTreeModelUtil.getCommonAncestor(tree, designatedLeadingNode, currentLeadingNode);
			addSubtreeUpdate(leastAncestor);
		} else {
			Object lastNodeInTree = null;
			// find last no in current path which is still in the tree
			for (int index = 1, size = currentPath.size(); index < size; index++) {
				final Object tmp = currentPath.get(index);
				if (tree.containsNode(tmp)) {
					lastNodeInTree = tmp;
					break;
				}
			}
			if (lastNodeInTree == null) {
				assert false : "Expected root is still in the tree";
				addSubtreeUpdate(tree.getRoot());
			} else {
				final Object leastAncestor = TLTreeModelUtil.getCommonAncestor(tree, designatedLeadingNode, lastNodeInTree);
				addSubtreeUpdate(leastAncestor);
			}
		}
	}
}
