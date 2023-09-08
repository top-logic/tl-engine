/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dnd;


import com.top_logic.layout.tree.component.TreeNodeBasedTreeModelBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} for the drag source tree of the DND demo.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoDndTreeModelBuilder extends TreeNodeBasedTreeModelBuilder<DefaultMutableTLTreeNode> {

	/**
	 * Singleton {@link DemoDndTreeModelBuilder} instance.
	 */
	public static final DemoDndTreeModelBuilder INSTANCE = new DemoDndTreeModelBuilder();

	private DemoDndTreeModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		DefaultMutableTLTreeModel model = new DefaultMutableTLTreeModel("R");

		model.getRoot().createChild("A1");
		DefaultMutableTLTreeNode a2 = model.getRoot().createChild("A2");

		a2.createChild("A21");

		DefaultMutableTLTreeNode a3 = model.getRoot().createChild("A3");

		a3.createChild("A31");
		a3.createChild("A32");
		a3.createChild("A33");

		return model.getRoot();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, DefaultMutableTLTreeNode node) {
		return null;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}
