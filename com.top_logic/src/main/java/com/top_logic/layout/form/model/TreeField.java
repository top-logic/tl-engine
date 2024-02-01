/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.tree.DefaultTreeData;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeDataListener;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link FormField} for embedding a {@link TreeData} within a {@link FormContext}.
 * 
 * @see FormTree Tree with input fields.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class TreeField extends ConstantField implements TreeData, TreeDataOwner {

	@Inspectable
	private final DefaultTreeData data;

	/**
	 * Creates a new not {@link #isImmutable() immutable} {@link TreeField}.
	 * 
	 * @param name
	 *        see {@link #getName()}
	 * @param treeModel
	 *        see {@link #getTreeModel()}
	 * @param selectionModel
	 *        see {@link #getSelectionModel()}
	 * @param renderer
	 *        see {@link #getTreeRenderer()}
	 * 
	 * @see ConstantField#ConstantField(String, boolean)
	 */
	protected TreeField(String name, TreeUIModel treeModel, SelectionModel selectionModel, TreeRenderer renderer) {
		super(name, !IMMUTABLE);
		this.data = new DefaultTreeData(Maybe.some(this), treeModel, selectionModel, renderer);
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitTreeField(this, arg);
	}

	@Override
	public boolean addTreeDataListener(TreeDataListener listener) {
		return data.addTreeDataListener(listener);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return data.getSelectionModel();
	}

	@Override
	public TreeUIModel getTreeModel() {
		return data.getTreeModel();
	}

	@Override
	public TreeRenderer getTreeRenderer() {
		return data.getTreeRenderer();
	}

	@Override
	public boolean removeTreeDataListener(TreeDataListener listener) {
		return data.removeTreeDataListener(listener);
	}

	@Override
	public Maybe<TreeDataOwner> getOwner() {
		/* Not supported, as the TreeField does not need an owner. The owner is only needed to build
		 * a stable reference for the scripting framework. But the TreeField can already be
		 * referenced as a FormMember. */
		return Maybe.none();
	}

	@Override
	public TreeDragSource getDragSource() {
		return data.getDragSource();
	}

	@Override
	public List<TreeDropTarget> getDropTargets() {
		return data.getDropTargets();
	}

	@Override
	public TreeData getTreeData() {
		return data;
	}

}
