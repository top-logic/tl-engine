/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * The class {@link AbstractTLTreeModelTest} creates a {@link TLTreeModel} and constructs some
 * nodes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTLTreeModelTest<N extends AbstractMutableTLTreeNode<N>> extends AbstractLayoutTest {

	public AbstractMutableTLTreeModel<N> treemodel;

	public N rootNode;

	public N child_1;

	public N child_1_1;

	public N child_2;

	public AbstractTLTreeModelTest() {
		super();
	}

	public AbstractTLTreeModelTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		treemodel = createTreeModel();
		rootNode = treemodel.getRoot();
		rootNode.setBusinessObject("root");
		child_1 = rootNode.createChild("1");
		child_1_1 = child_1.createChild("1_1");
		child_2 = rootNode.createChild("2");
	}

	protected abstract AbstractMutableTLTreeModel<N> createTreeModel();

	@Override
	protected void tearDown() throws Exception {
		treemodel = null;
		rootNode = null;
		child_1 = null;
		child_1_1 = null;
		child_2 = null;

		super.tearDown();
	}

}
