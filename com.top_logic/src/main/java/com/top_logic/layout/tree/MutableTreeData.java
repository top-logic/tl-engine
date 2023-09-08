/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.util.AbstractObservable;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.dnd.DefaultTreeDrag;
import com.top_logic.layout.tree.dnd.NoTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Mutable {@link TreeData}.
 * 
 * <p>
 * {@link TreeData} whose {@link TreeData#getTreeModel()}, {@link TreeData#getTreeRenderer()}, and
 * {@link TreeData#getSelectionModel()} can be changed after construction.
 * </p>
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MutableTreeData extends AbstractObservable<TreeDataListener, TreeDataEvent>
		implements TreeData, LazyTypedAnnotatableMixin {

	private final Maybe<? extends TreeDataOwner> _owner;

	@Inspectable
	private TreeUIModel _model;

	@Inspectable
	private SelectionModel _selection;

	@Inspectable
	private TreeRenderer _renderer;

	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	private TreeDropTarget _dropTarget = NoTreeDrop.INSTANCE;

	private TreeDragSource _dragSource = DefaultTreeDrag.INSTANCE;

	/**
	 * Creates a new {@link MutableTreeData}.
	 * 
	 * @param model
	 *        Value of {@link #getTreeModel()}.
	 * @param selection
	 *        Value of {@link #getSelectionModel()}.
	 * @param renderer
	 *        Value of {@link #getTreeRenderer()}.
	 */
	public MutableTreeData(
			Maybe<? extends TreeDataOwner> owner, TreeUIModel model, SelectionModel selection, TreeRenderer renderer) {
		_owner = owner;
		_model = model;
		_selection = selection;
		_renderer = renderer;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public boolean addTreeDataListener(TreeDataListener listener) {
		return addListener(listener);
	}

	@Override
	public boolean removeTreeDataListener(TreeDataListener listener) {
		return removeListener(listener);
	}

	@Override
	protected void sendEvent(TreeDataListener listener, TreeDataEvent event) {
		listener.handleTreeDataChange(event);
	}

	@Override
	public TreeUIModel getTreeModel() {
		return _model;
	}

	/**
	 * Setter of {@link TreeData#getTreeModel()}.
	 * 
	 * @param newModel
	 *        The new value of {@link TreeData#getTreeModel()}.
	 */
	public void setTreeModel(TreeUIModel newModel) {
		TreeUIModel oldModel = _model;
		if (oldModel == newModel) {
			return;
		}
		_model = newModel;
		notifyListeners(new TreeDataEvent.TreeUIModelChange(this, oldModel, _model));
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selection;
	}

	/**
	 * Setter of {@link TreeData#getSelectionModel()}.
	 * 
	 * @param newSelection
	 *        The new value of {@link TreeData#getSelectionModel()}.
	 */
	public void setSelectionModel(SelectionModel newSelection) {
		SelectionModel oldSelection = _selection;
		if (oldSelection == newSelection) {
			return;
		}
		_selection = newSelection;
		notifyListeners(new TreeDataEvent.SelectionModelChange(this, oldSelection, _selection));
	}

	@Override
	public TreeRenderer getTreeRenderer() {
		return _renderer;
	}

	/**
	 * Setter of {@link TreeData#getTreeRenderer()}.
	 * 
	 * @param newRenderer
	 *        The new value of {@link TreeData#getTreeRenderer()}.
	 */
	public void setTreeRenderer(TreeRenderer newRenderer) {
		TreeRenderer oldRenderer = _renderer;
		if (oldRenderer == newRenderer) {
			return;
		}
		_renderer = newRenderer;
		notifyListeners(new TreeDataEvent.RendererChange(this, oldRenderer, _renderer));
	}

	@Override
	public Maybe<? extends TreeDataOwner> getOwner() {
		return _owner;
	}

	@Override
	public TreeDragSource getDragSource() {
		return _dragSource;
	}

	/**
	 * @see #getDragSource()
	 */
	public void setDragSource(TreeDragSource dragSource) {
		_dragSource = dragSource;
	}

	@Override
	public TreeDropTarget getDropTarget() {
		return _dropTarget;
	}

	/**
	 * @see #getDropTarget()
	 */
	public void setDropTarget(TreeDropTarget dropTarget) {
		_dropTarget = dropTarget;
	}

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

}
