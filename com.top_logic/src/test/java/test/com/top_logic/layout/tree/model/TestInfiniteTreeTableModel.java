/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeTableModel;

/**
 * Test for {@link TreeTableModel} with inifinit {@link TreeBuilder}
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestInfiniteTreeTableModel extends BasicTestCase {

	static class InfiniteTreebuilder implements TreeBuilder<DefaultTreeTableNode> {

		private final Random _rand = new Random(4711);

		@Override
		public DefaultTreeTableNode createNode(AbstractMutableTLTreeModel<DefaultTreeTableNode> model,
				DefaultTreeTableNode parent, Object userObject) {
			return new DefaultTreeTableNode(model, parent, userObject);
		}

		@Override
		public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
			ArrayList<DefaultTreeTableNode> children = new ArrayList<>();
			int size = _rand.nextInt(8) + 2;
			for (int i = 0; i < size; i++) {
				children.add(createNode(node.getModel(), node, node.getBusinessObject() + "_" + i));
			}
			return children;
		}

		@Override
		public boolean isFinite() {
			return false;
		}

	}

	protected AbstractTreeTableModel<DefaultTreeTableNode> createTreeTableModel() {
		TableConfiguration tableConfig = TableConfigurationFactory.table();
		tableConfig.setFilterDisplayChildren(false);
		tableConfig.setFilterDisplayParents(false);

		DefaultTreeTableModel result =
			new DefaultTreeTableModel(new InfiniteTreebuilder(), getName(), list("A", "B", "C"),
				tableConfig);
		return result;
	}

	public void testExpandRoot() {
		AbstractTreeTableModel<DefaultTreeTableNode> model = createTreeTableModel();
		model.setRootVisible(true);
		model.getTable();
		DefaultTreeTableNode root = model.getRoot();
		root.setExpanded(true);
		assertTrue(root.isDisplayed());
		List<DefaultTreeTableNode> children = root.getChildren();
		assertTrue(children.size() > 0);
		for (DefaultTreeTableNode child : children) {
			assertTrue("Ticket #17861: Child of expanded visible root not displayed.", child.isDisplayed());
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestInfiniteTreeTableModel}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestInfiniteTreeTableModel.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
