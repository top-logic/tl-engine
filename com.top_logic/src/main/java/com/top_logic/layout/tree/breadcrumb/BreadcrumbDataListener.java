/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * The class {@link BreadcrumbDataListener} handles changes in
 * {@link BreadcrumbData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BreadcrumbDataListener {

	/**
	 * This method handles a change of the selection model in the given
	 * {@link BreadcrumbData}.
	 * 
	 * @param source
	 *        the model whose {@link BreadcrumbData#getSelectionModel()
	 *        selection model} has changed.
	 * @param oldModel
	 *        the model before the change
	 * @param newModel
	 *        the new model of <code>source</code>
	 */
	void notifySelectionModelChanged(BreadcrumbData source, SingleSelectionModel oldModel, SingleSelectionModel newModel);
	
	/**
	 * This method handles a change of the display model in the given
	 * {@link BreadcrumbData}.
	 * 
	 * @param source
	 *        the model whose {@link BreadcrumbData#getSelectionModel()
	 *        selection model} has changed.
	 * @param oldModel
	 *        the model before the change
	 * @param newModel
	 *        the new model of <code>source</code>
	 */
	void notifyDisplayModelChanged(BreadcrumbData source, SingleSelectionModel oldModel, SingleSelectionModel newModel);

	/**
	 * This method handles a change of the tree in the given
	 * {@link BreadcrumbData}.
	 * 
	 * @param source
	 *        the model whose {@link BreadcrumbData#getTree() tree} has changed.
	 * @param oldTree
	 *        the tree before the change
	 * @param newTree
	 *        the new tree of <code>source</code>
	 */
	void notifyTreeChanged(BreadcrumbData source, TLTreeModel<?> oldTree, TLTreeModel<?> newTree);
}
