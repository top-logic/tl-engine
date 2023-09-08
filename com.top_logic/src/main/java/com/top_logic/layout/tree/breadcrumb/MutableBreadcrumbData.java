/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * The class {@link MutableBreadcrumbData} is a standard mutable
 * {@link BreadcrumbData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MutableBreadcrumbData extends AbstractBreadcrumbData {

	private TLTreeModel tree;
	private SingleSelectionModel selectionModel;
	private SingleSelectionModel displayModel;
	private final BreadcrumbDataOwner owner;
	
	/**
	 * Create a {@link MutableBreadcrumbData}.
	 * 
	 * @param tree
	 *        See {@link #getTree()}.
	 * @param selectionModel
	 *        See {@link #getSelectionModel()}.
	 * @param displayModel
	 *        See {@link #getDisplayModel()}.
	 * @param owner
	 *        See {@link #getOwner()}.
	 */
	public MutableBreadcrumbData(
			TLTreeModel tree,
			SingleSelectionModel selectionModel,
			SingleSelectionModel displayModel,
			BreadcrumbDataOwner owner) {

		this.tree = tree;
		this.selectionModel = selectionModel;
		this.displayModel = displayModel;
		this.owner = owner;
	}

	@Override
	public SingleSelectionModel getSelectionModel() {
		return selectionModel;
	}

	@Override
	public SingleSelectionModel getSingleSelectionModel() {
		return selectionModel;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public SingleSelectionModel getDisplayModel() {
		return displayModel;
	}

	/**
	 * Sets a new {@link SingleSelectionModel selection model} and informs all added listeners
	 * about the change.
	 * 
	 * @param selectionModel
	 *        the new selection model. must not be <code>null</code>
	 */
	public void setSelectionModel(SingleSelectionModel selectionModel) {
		SingleSelectionModel oldModel = this.selectionModel;
		this.selectionModel = selectionModel;
		
		notifySelectionModelChange(selectionModel, oldModel);
	}
	
	/**
	 * Sets a new {@link SingleSelectionModel display model} and informs all added listeners
	 * about the change.
	 * 
	 * @param displayModel
	 *        the new selection model. must not be <code>null</code>
	 */
	public void setDisplayModel(SingleSelectionModel displayModel) {
		SingleSelectionModel oldModel = this.displayModel;
		this.displayModel = displayModel;

		notifyDisplayModelChange(displayModel, oldModel);
	}

	@Override
	public TLTreeModel getTree() {
		return tree;
	}

	/**
	 * Sets a new {@link TLTreeModel} and informs all listeners about the change
	 * 
	 * @param tree
	 *        the new tree. must not be null
	 */
	public void setTree(TLTreeModel tree) {
		TLTreeModel oldTree = this.tree;
		this.tree = tree;

		notifyTreeChange(tree, oldTree);
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public BreadcrumbDataOwner getOwner() {
		return owner;
	}

}
