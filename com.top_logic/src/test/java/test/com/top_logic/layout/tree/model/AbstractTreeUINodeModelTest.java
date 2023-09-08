/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel;

/**
 * Abstract super class for tests of {@link AbstractTreeUINodeModel}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTreeUINodeModelTest<N extends AbstractTreeUINodeModel.TreeUINode<N>> extends
		AbstractMutableTLTreeModelTest<N> {

	@Override
	protected final AbstractMutableTLTreeModel<N> createTreeModel() {
		return createTreeUINodeModel();
	}

	/**
	 * Creates the {@link AbstractTreeUINodeModel} to test.
	 */
	protected abstract AbstractTreeUINodeModel<N> createTreeUINodeModel();

	public void testDisplayStateAddChild() {
		rootNode.setExpanded(true);
		N child = rootNode.createChild("testChild");
		assertTrue(child.isDisplayed());
	}
}
