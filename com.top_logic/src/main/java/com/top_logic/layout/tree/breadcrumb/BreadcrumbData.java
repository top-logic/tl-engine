/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.SingleSelectionModelProvider;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * The {@link BreadcrumbData} acts as model for the {@link BreadcrumbControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BreadcrumbData extends NamedModel, SingleSelectionModelProvider {

	/**
	 * Returns the tree displayed by the {@link BreadcrumbControl bread crumb}
	 * this data belongs to.
	 */
	TLTreeModel getTree();

	/**
	 * The {@link SingleSelectionModel#getSingleSelection() single selection} is the currently
	 * selected node in the displayed {@link #getTree() tree}.
	 * 
	 * <p>
	 * While navigating downwards, the selection in {@link #getSelectionModel()} and
	 * {@link #getDisplayModel()} are the same. When navigating upwards the tree, the
	 * {@link #getDisplayModel()} may keep pointing to the deepest node visited.
	 * </p>
	 */
	SingleSelectionModel getSelectionModel();
	
	/**
	 * The {@link SingleSelectionModel#getSingleSelection() display selection} is the last visible
	 * node in the breadcrumb.
	 */
	SingleSelectionModel getDisplayModel();

	/**
	 * Adds a listener to inform about changes of {@link #getSelectionModel()},
	 * {@link #getDisplayModel()}, and {@link #getTree()}.
	 * 
	 * @param listener
	 *        the listener to inform. must not be <code>null</code>
	 */
	void addBreadcrumbDataListener(BreadcrumbDataListener listener);

	/**
	 * Removes a listener to be informed about changes of {@link #getSelectionModel()},
	 * {@link #getDisplayModel()}, and {@link #getTree()}.
	 * 
	 * @param listener
	 *        the listener to remove. must not be <code>null</code>
	 */
	void removeBreadcrumbDataListener(BreadcrumbDataListener listener);

	/** @see BreadcrumbDataOwner */
	BreadcrumbDataOwner getOwner();

}
