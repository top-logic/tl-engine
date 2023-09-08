/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import junit.framework.Test;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Test case for {@link DefaultMutableTLTreeModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDefaultMutableTLTreeModel extends AbstractMutableTLTreeModelTest<DefaultMutableTLTreeNode> {

	@Override
	protected AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> createTreeModel() {
		return new DefaultMutableTLTreeModel();
	}

	public static Test suite() {
		return suite(TestDefaultMutableTLTreeModel.class);
	}

}
