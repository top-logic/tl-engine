/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.editor.TLLayoutFormBuilder.EditModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DeclarativeApplyHandler;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * {@link DeclarativeApplyHandler} for declarative {@link TLLayout} forms that are connected to a
 * tree table providing navigation through the application layout.
 * 
 * <p>
 * After this handler has made the changes to the layout persistent, it resets the selected and
 * currently edited node in the connected tree table.
 * </p>
 * 
 * @see TreeTableComponent
 * @see TLLayoutFormBuilder
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class TLLayoutApplyHandler extends DeclarativeApplyHandler<EditModel, TLLayout> {

	/**
	 * Creates a {@link TLLayoutApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLLayoutApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void storeValues(FormComponent component, EditModel editModel, TLLayout layout) {
		storeLayout(component, editModel, layout);

		resetSelectedMasterTreeNode(component);

		validateTLLayoutForm(component);
	}

	@SuppressWarnings("unchecked")
	private void resetSelectedMasterTreeNode(FormComponent component) {
		TreeTableComponent treeTable = (TreeTableComponent) component.getSelectableMaster();
		AbstractTreeTableModel<?> tree = treeTable.getTableData().getTree();
		Object selected = treeTable.getSelected();
		IndexedTreeTableModel<DefaultTreeTableNode> indexedTree = (IndexedTreeTableModel<DefaultTreeTableNode>) tree;
		Object treeNode = CollectionUtil.getSingleValueFrom(indexedTree.getIndex().getNodes(selected));
		indexedTree.resetChildren((DefaultTreeTableNode) treeNode);
	}

	private void validateTLLayoutForm(FormComponent component) {
		ModelSpec modelSpec = component.getConfig().getModelSpec();

		if (modelSpec != null) {
			component.setModel(TypedConfigUtil.createInstance(modelSpec).eval(component));
		}
	}

	/**
	 * Stores the layout configuration given by the {@link EditModel}.
	 */
	protected abstract void storeLayout(FormComponent component, EditModel editModel, TLLayout layout);
}
