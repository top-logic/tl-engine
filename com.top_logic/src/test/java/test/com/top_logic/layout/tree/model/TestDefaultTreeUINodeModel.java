/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.List;

import junit.framework.Test;

import com.top_logic.layout.tree.model.AbstractTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUIBuilder;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;

/**
 * Default test class for {@link DefaultTreeUINodeModel}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultTreeUINodeModel extends AbstractTreeUINodeModelTest<DefaultTreeUINode> {

	@Override
	protected AbstractTreeUINodeModel<DefaultTreeUINode> createTreeUINodeModel() {
		return new DefaultTreeUINodeModel(new DefaultTreeUIBuilder(), "0");
	}

	private DefaultTreeUINodeModel getModel() {
		return (DefaultTreeUINodeModel) treemodel;
	}

	public void testDisplayOnCollapsedNodes() {
		DefaultTreeUINodeModel model = getModel();
		model.setRootVisible(true);
		DefaultTreeUINode root = model.getRoot();
		model.setExpanded(root, true);
		model.setExpanded(child_1, true);
		model.setExpanded(child_1_1, true);

		assertTrue(model.isDisplayed(root));
		assertTrue(model.isDisplayed(child_1));
		assertTrue(model.isDisplayed(child_1_1));

		model.setExpanded(root, false);

		assertTrue("Node not expanded but displayed", model.isDisplayed(root));
		assertTrue(model.isExpanded(child_1));
		assertFalse("Parent node is not expanded", model.isDisplayed(child_1));
		assertTrue(model.isExpanded(child_1_1));
		assertFalse("Parent node is not expanded", model.isDisplayed(child_1_1));
	}

	public void testUpdateNodeStructure() {
		DefaultTreeUIBuilder builder = new DefaultTreeUIBuilder() {

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				List<DefaultTreeUINode> childList = super.createChildList(node);
				for (int index = 0; index < 5; index++) {
					childList.add(createNode(node.getModel(), node, node.getBusinessObject().toString() + "." + index));
				}
				return childList;
			}
		};
		DefaultTreeUINodeModel treeModel = new DefaultTreeUINodeModel(builder, "0");
		DefaultTreeUINode root = treeModel.getRoot();
		root.setExpanded(true);
		List<DefaultTreeUINode> children = root.getChildren();
		int numberChildren = children.size();
		for (int i = 0; i < numberChildren; i ++) {
			assertTrue(children.get(i).isDisplayed());
		}
		root.removeChild(0);
		assertEquals(numberChildren - 1, root.getChildCount());

		root.updateNodeStructure();
		List<DefaultTreeUINode> resurrectedChildren = root.getChildren();
		assertSame(numberChildren, resurrectedChildren.size());
		for (int i = 0; i < resurrectedChildren.size(); i++) {
			assertTrue(resurrectedChildren.get(i).isDisplayed());
		}
		
	}

	public static Test suite() {
		return suite(TestDefaultTreeUINodeModel.class);
	}

}
