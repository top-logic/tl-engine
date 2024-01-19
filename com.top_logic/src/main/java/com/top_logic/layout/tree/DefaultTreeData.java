/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.dnd.DefaultTreeDrag;
import com.top_logic.layout.tree.dnd.NoTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Simple {@link TreeData} implementation for directly instantiating {@link TreeControl}s.
 * 
 * Immutable, except for the properties stored via the {@link TypedAnnotatable} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTreeData extends LazyTypedAnnotatable implements TreeData {

	private final Maybe<? extends TreeDataOwner> _owner;

	@Inspectable
	private final TreeUIModel treeModel;

	@Inspectable
	private final SelectionModel selectionModel;

	@Inspectable
	private final TreeRenderer renderer;

	private TreeDragSource _dragSource;

	private List<TreeDropTarget> _dropTargets;

	public DefaultTreeData(Maybe<? extends TreeDataOwner> owner, TreeUIModel treeModel, SelectionModel selectionModel,
			TreeRenderer renderer) {
		this(owner, treeModel, selectionModel, renderer, DefaultTreeDrag.INSTANCE, Arrays.asList(NoTreeDrop.INSTANCE));
	}

	public DefaultTreeData(Maybe<? extends TreeDataOwner> owner, TreeUIModel treeModel, SelectionModel selectionModel,
			TreeRenderer renderer, TreeDragSource dragSource, List<TreeDropTarget> dropTargets) {
		_owner = owner;
		this.treeModel = treeModel;
		this.selectionModel = selectionModel;
		this.renderer = renderer;
		_dragSource = dragSource;
		_dropTargets = dropTargets;
	}
	

	@Override
	public SelectionModel getSelectionModel() {
		return selectionModel;
	}

	@Override
	public TreeUIModel getTreeModel() {
		return treeModel;
	}

	@Override
	public TreeRenderer getTreeRenderer() {
		return renderer;
	}

	@Override
	public boolean removeTreeDataListener(TreeDataListener listener) {
		return false; // Immutable, ignore.
	}

	@Override
	public boolean addTreeDataListener(TreeDataListener listener) {
	    return false; // Immutable, ignore.
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public Maybe<? extends TreeDataOwner> getOwner() {
		return _owner;
	}

	@Override
	public TreeDragSource getDragSource() {
		return _dragSource;
	}

	@Override
	public List<TreeDropTarget> getDropTargets() {
		return _dropTargets;
	}

}
