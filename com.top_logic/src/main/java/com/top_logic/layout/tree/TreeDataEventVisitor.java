/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Visitor interface inspecting a specific {@link TreeDataEvent} send by a {@link TreeData}.
 * 
 * @see TreeDataEvent
 * @see TreeDataListener
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TreeDataEventVisitor {

	/**
	 * Informs this listener that the {@link TreeData#getTreeModel()} has changed.
	 * 
	 * @param source
	 *        The {@link TreeData} that was changed.
	 * @param newModel
	 *        The new model that was set.
	 * @param oldModel
	 *        The old model that was formerly set.
	 */
	void notifyTreeModelChange(TreeData source, TreeUIModel newModel, TreeUIModel oldModel);

	/**
	 * Informs this listener that the {@link TreeData#getSelectionModel()} has changed.
	 * 
	 * @param source
	 *        The {@link TreeData} that was changed.
	 * @param newSelection
	 *        The new model that was set.
	 * @param oldSelection
	 *        The old model that was formerly set.
	 */
	void notifySelectionModelChange(TreeData source, SelectionModel newSelection, SelectionModel oldSelection);

	/**
	 * Informs this listener that the {@link TreeData#getTreeRenderer()} has changed.
	 * 
	 * @param source
	 *        The {@link TreeData} that was changed.
	 * @param newRenderer
	 *        The new renderer that was set.
	 * @param oldRenderer
	 *        The old renderer that was formerly set.
	 */
	void notifyRendererChange(TreeData source, TreeRenderer newRenderer, TreeRenderer oldRenderer);

}

