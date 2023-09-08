/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Set;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.layout.tree.model.TLTreeModelUtil;

/**
 * Utilities for the {@link SelectionModel}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class SelectionUtil {

	/**
	 * Sets the given tree nodes as new selection into the given {@link SelectionModel}.
	 * 
	 * <p>
	 * To display all selected nodes to the user, the corresponding parent nodes are expanded, if
	 * necessary.
	 * </p>
	 * 
	 * @see #setSelection(SelectionModel, Set)
	 * 
	 * @param model
	 *        Model holding the selection.
	 * @param selection
	 *        New selected tree nodes.
	 */
	public static void setTreeSelection(SelectionModel model, Set<? extends TreeUINode<?>> selection) {
		setSelection(model, selection);

		Set<?> actualSelection = model.getSelection();
		for (TreeUINode<?> selectedNode : selection) {
			if (!actualSelection.contains(selectedNode)) {
				// selection of given node was actually declined.
				continue;
			}
			TLTreeModelUtil.expandParents(selectedNode);
		}
	}

	/**
	 * Sets a new selection into the given {@link SelectionModel}.
	 * 
	 * <p>
	 * If the new selection contains multiple items, but the selection model allows only a single
	 * selection, then only the first item is selected.
	 * </p>
	 * 
	 * @param model
	 *        Model holding the selection.
	 * @param selection
	 *        New selection.
	 */
	public static void setSelection(SelectionModel model, Set<?> selection) {
		if (selection.isEmpty()) {
			model.clear();
		} else {
			if (model.isMultiSelectionSupported()) {
				model.setSelection(selection);
			} else {
				Set<?> currentSelection = model.getSelection();
				switch (currentSelection.size()) {
					case 0: {
						// Currently nothing selected. Select first:
						model.setSelected(CollectionUtilShared.getFirst(selection), true);
						break;
					}
					case 1: {
						if (selection.contains(currentSelection.iterator().next())) {
							// Actually no change necessary;
						} else {
							// Currently selection invalid. Select first:
							model.setSelected(CollectionUtilShared.getFirst(selection), true);
						}
						break;
					}
					default: {
						// Strange situation: no multi selection not supported, but more than one
						// element selected. Select first:
						model.setSelected(CollectionUtilShared.getFirst(selection), true);
						break;
					}
				}
			}
		}
	}

}
