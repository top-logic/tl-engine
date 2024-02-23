/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.SelectionModelProvider;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Observable state of {@link TreeControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeData extends NamedModel, SelectionModelProvider, TypedAnnotatable {
	
	/**
	 * The currently displayed {@link TreeUIModel}.
	 */
	TreeUIModel getTreeModel();
	
	/**
	 * The current {@link SelectionModel} in use. 
	 */
	@Override
	SelectionModel getSelectionModel();
	
	/**
	 * The currently used {@link TreeRenderer}.
	 */
	TreeRenderer getTreeRenderer();
	
	
	/**
	 * Adds the given {@link TreeDataListener} to this {@link TreeData}
	 * implementation.
	 * 
	 * <p>
	 * The given listener is informed about changes to this {@link TreeData}.
	 * </p>
	 */
	boolean addTreeDataListener(TreeDataListener listener);
	
	/**
	 * Removes the given {@link TreeDataListener} from this {@link TreeData} implementation.
	 * 
	 * @see #addTreeDataListener(TreeDataListener)
	 */
	boolean removeTreeDataListener(TreeDataListener listener);

	/**
	 * Used by the scripting framework to retrieve the {@link TreeData} during script execution.
	 * <p>
	 * If the {@link TreeData} can be referenced without the {@link TreeDataOwner}, it should
	 * implement this method with "return {@link Maybe#none()}".
	 * </p>
	 * 
	 * @see TreeDataOwner
	 */
	Maybe<? extends TreeDataOwner> getOwner();

	/**
	 * The {@link TreeDropTarget}'s that controls drop operations in this tree.
	 */
	List<TreeDropTarget> getDropTargets();

	/**
	 * Defines the behavior if a drag-and-drop operation is started from this {@link TreeData}.
	 */
	TreeDragSource getDragSource();

}
