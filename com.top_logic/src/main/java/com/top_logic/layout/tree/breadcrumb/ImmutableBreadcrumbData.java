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
 * {@link BreadcrumbData} without setters.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ImmutableBreadcrumbData implements BreadcrumbData {

	private final TLTreeModel<?> tree;
	private final SingleSelectionModel selectionModel;
	private final SingleSelectionModel displayModel;
	private final BreadcrumbDataOwner owner;

	/**
	 * Create a new ImmutableBreadcrumbData with the same model for node selection and node display.
	 * 
	 * @param tree
	 *        The tree to display a single path of.
	 * @param displayModel
	 *        See {@link #getDisplayModel()}.
	 * @param selectionModel
	 *        See {@link #getSelectionModel()}.
	 */
	public ImmutableBreadcrumbData(
			TLTreeModel<?> tree,
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

	@Override
	public SingleSelectionModel getDisplayModel() {
		return displayModel;
	}
	
	@Override
	public TLTreeModel<?> getTree() {
		return tree;
	}

	@Override
	public void removeBreadcrumbDataListener(BreadcrumbDataListener listener) {
		// immutable
	}

	@Override
	public void addBreadcrumbDataListener(BreadcrumbDataListener listener) {
		// immutable
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
