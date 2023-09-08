/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import test.com.top_logic.basic.ExpectedFailure;

import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.ExpansionState;

/**
 * Test case for {@link ExpansionState}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestExpansionState extends TestCase {

	public void testApply() {
		DefaultStructureTreeUIModel uiModel = createModel();
		Object node0 = uiModel.getRoot();

		uiModel.setExpanded(node0, true);
		assertExpansionState(uiModel);

		Object node01 = uiModel.getChildren(node0).get(1);
		uiModel.setExpanded(node01, true);
		assertExpansionState(uiModel);

		Object node010 = uiModel.getChildren(node01).get(0);
		Object node011 = uiModel.getChildren(node01).get(1);
		uiModel.setExpanded(node010, true);
		uiModel.setExpanded(node011, true);
		assertExpansionState(uiModel);
	}

	public void testExpansionCheck() {
		DefaultStructureTreeUIModel model1 = createModel();
		DefaultStructureTreeUIModel model2 = createModel();
		checkExpansion(model1, model2);

		Object root1 = model1.getRoot();
		model1.setExpanded(root1, true);

		Object root2 = model2.getRoot();
		model2.setExpanded(root2, true);

		checkExpansion(model1, model2);

		Object node1 = model1.getChildren(root1).get(0);
		model1.setExpanded(node1, true);

		Object node2 = model2.getChildren(root2).get(0);
		model2.setExpanded(node2, true);

		checkExpansion(model1, model2);

		Object otherNode2 = model2.getChildren(node2).get(2);
		model2.setExpanded(otherNode2, true);

		try {
			expectFailedExpansionCheck(model1, model2);
			fail("Missmatch not detected.");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
	}

	private void expectFailedExpansionCheck(DefaultStructureTreeUIModel model1, DefaultStructureTreeUIModel model2) {
		try {
			checkExpansion(model1, model2);
		} catch (AssertionFailedError ex) {
			throw new ExpectedFailure(ex);
		}
	}

	private void assertExpansionState(DefaultStructureTreeUIModel expandedModel) {
		ExpansionState state = ExpansionState.createExpansionState(expandedModel, expandedModel.getRoot());
		DefaultStructureTreeUIModel newModel = createModel();
		state.apply(newModel, newModel.getRoot());

		expectFailedExpansionCheck(expandedModel, newModel);
	}

	private void checkExpansion(DefaultStructureTreeUIModel model1, DefaultStructureTreeUIModel model2) {
		checkExpansion(model1, model1.getRoot(), model2, model2.getRoot());
	}

	private void checkExpansion(DefaultStructureTreeUIModel model1, Object node1, DefaultStructureTreeUIModel model2,
			Object node2) {
		boolean isExpanded = model1.isExpanded(node1);
		assertEquals(isExpanded, model2.isExpanded(node2));

		if (isExpanded) {
			List children2 = model2.getChildren(node2);
			int index = 0;
			for (Object child1 : model1.getChildren(node1)) {
				Object child2 = children2.get(index++);

				checkExpansion(model1, child1, model2, child2);
			}
		}
	}

	private DefaultStructureTreeUIModel createModel() {
		DefaultMutableTLTreeModel model =
			new DefaultMutableTLTreeModel(new AbstractTLTreeNodeBuilder() {
				@Override
				public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
					String businessObject = (String) node.getBusinessObject();
					int childCount = businessObject.length() + 1;
					ArrayList<DefaultMutableTLTreeNode> children = new ArrayList<>(childCount);
					for (int n = 0; n < childCount; n++) {
						children.add(createNode(node.getModel(), node, businessObject + n));
					}
					return children;
				}

				@Override
				public boolean isFinite() {
					return false;
				}

			}, "0");

		DefaultStructureTreeUIModel uiModel = new DefaultStructureTreeUIModel(model);
		return uiModel;
	}

}
