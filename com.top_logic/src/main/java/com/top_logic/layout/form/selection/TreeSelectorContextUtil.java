/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import static com.top_logic.layout.tree.model.TreeUIModelUtil.*;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Utility for creating trees in popup select dialogs.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TreeSelectorContextUtil {

	/**
	 * Expands top level parent nodes, if they have one child only. Also expands all selected nodes.
	 */
	@SuppressWarnings("unchecked")
	public static void expandNodes(TreeUIModel<?> treeUIModel, Collection<?> selection) {
		TreeUIModel<Object> treeModel = (TreeUIModel<Object>) treeUIModel;

		expandRootNode(treeModel);
		expandSingleChildNodes(treeModel);
		expandSelectedNodes(treeModel, selection);
	}

	/**
	 * Expands tree to given tree level. Also expands all selected nodes.
	 */
	@SuppressWarnings("unchecked")
	public static void expandNodes(TreeUIModel<?> treeUIModel, int initialTreeExpansionDepth, Collection<?> selection) {
		TreeUIModel<Object> treeModel = (TreeUIModel<Object>) treeUIModel;

		if (SelectDialogConfig.SHOW_ALL_NODES != initialTreeExpansionDepth) {
			int expansionDepth = initialTreeExpansionDepth;
			if (!treeModel.isRootVisible()) {
				expansionDepth++;
			}
			setExpanded(treeModel, treeModel.getRoot(), true, expansionDepth);
			expandSelectedNodes(treeModel, selection);
		} else {
			setExpandedAll(treeModel, treeModel.getRoot(), true);
		}
	}

	private static void expandRootNode(TreeUIModel<Object> treeModel) {
		Object root = treeModel.getRoot();
		treeModel.setExpanded(root, true);
	}

	private static void expandSingleChildNodes(TreeUIModel<Object> treeModel) {
		// Expand child elements if there is exactly one child per level
		Object parent = treeModel.getRoot();
		List<?> children = treeModel.getChildren(parent);
		while (children.size() == 1) {
			parent = children.get(0);
			treeModel.setExpanded(parent, true);
			children = treeModel.getChildren(parent);
		}
	}

	private static void expandSelectedNodes(TreeUIModel<Object> treeModel, Collection<?> selection) {
		if (!CollectionUtil.isEmptyOrNull(selection)) {
			for (Object selectedNode : selection) {
				expandParents(treeModel, selectedNode);
			}
		}
	}

}
