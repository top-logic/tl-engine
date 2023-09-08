/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.breadcrumb;

import static com.top_logic.basic.util.Utils.*;

import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.model.SingleSelectionListener;

/**
 * Combines the selection of the individual nodes into the selection of the tree.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class TileBreadcrumbNodeToTreeSelectionUpdater implements SingleSelectionListener {

	private final TileBreadcrumbTreeModel _tree;

	/**
	 * Creates a {@link TileBreadcrumbNodeToTreeSelectionUpdater}.
	 * 
	 * @param tree
	 *        Is not allowed to be null. The {@link TileBreadcrumbTreeModel} whose selection
	 *        should be updated based on the selection of its nodes.
	 */
	public TileBreadcrumbNodeToTreeSelectionUpdater(TileBreadcrumbTreeModel tree) {
		_tree = requireNonNull(tree);
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object oldSelection, Object newSelection) {
		_tree.processIncomingEvent(this::onSelectionChange);
	}

	private void onSelectionChange() {
		removeListenerRecursively(_tree.getRoot());
		updateTreeSelectionRecursively(_tree.getRoot());
		addListenerRecursively(_tree.getRoot());
		logDebug(() -> "The selection changed to: " + _tree.getSelectionLabelPath());
	}

	private void updateTreeSelectionRecursively(TileBreadcrumbTreeNode newSelection) {
		/* The selection might have changed while this node was not on the selection path. And
		 * only the selection path is updated via listeners. The selection of newly selected
		 * nodes therefore needs to be updated. */
		newSelection.updateChildSelection();
		TileBreadcrumbTreeNode selectedChild = newSelection.getSelectedChild();
		if (selectedChild == null) {
			setTreeSelection(newSelection);
		} else {
			updateTreeSelectionRecursively(selectedChild);
		}
	}

	void addListenerRecursively(TileBreadcrumbTreeNode node) {
		addListener(node);
		if (node.getSelectedChild() != null) {
			addListenerRecursively(node.getSelectedChild());
		}
	}

	void removeListenerRecursively(TileBreadcrumbTreeNode node) {
		removeListener(node);
		if (node.getSelectedChild() != null) {
			removeListenerRecursively(node.getSelectedChild());
		}
	}

	private void addListener(TileBreadcrumbTreeNode node) {
		node.getChildSelection().addSingleSelectionListener(this);
	}

	private void removeListener(TileBreadcrumbTreeNode node) {
		node.getChildSelection().removeSingleSelectionListener(this);
	}

	private void setTreeSelection(TileBreadcrumbTreeNode newSelection) {
		_tree.getSingleSelectionModel().setSingleSelection(newSelection);
	}

	private static void logDebug(Supplier<String> message) {
		Logger.debug(message, TileBreadcrumbNodeToTreeSelectionUpdater.class);
	}

}
