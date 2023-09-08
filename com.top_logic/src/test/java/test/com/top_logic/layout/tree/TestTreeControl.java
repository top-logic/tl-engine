/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.layout.TestControl;
import test.com.top_logic.layout.tree.model.TestTLTreeModelHelper;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.tree.DefaultTreeData;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeControl.TreeAction;
import com.top_logic.layout.tree.TreeDataOwner;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;
import com.top_logic.layout.tree.renderer.NoResourceProvider;
import com.top_logic.layout.tree.renderer.TreeContentRenderer;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Test case for {@link TreeControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTreeControl extends TestControl {

	public void testRemoveNode() {
		DefaultMutableTLTreeModel appModel = TestTLTreeModelHelper.createInfiniteTree(3, "root");
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, false);
		DefaultMultiSelectionModel newSelectionModel = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, newSelectionModel, DefaultTreeRenderer.INSTANCE));

		writeControl(treeControl);

		DefaultMutableTLTreeNode root = appModel.getRoot();
		DefaultMutableTLTreeNode child_1 = root.getChildAt(0);
		DefaultMutableTLTreeNode child_2 = root.getChildAt(1);
		DefaultMutableTLTreeNode child_1_1 = child_1.getChildAt(0);
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.getChildAt(0);

		selectNode(treeControl, child_2, true);
		assertEquals(set(child_2), treeControl.getData().getSelectionModel().getSelection());
		root.removeChild(1);
		assertEquals(set(), treeControl.getData().getSelectionModel().getSelection());

		selectNode(treeControl, child_1, true);
		selectNode(treeControl, child_1_1, true);
		selectNode(treeControl, child_1_1_1, true);
		child_1_1.getParent().removeChild(child_1_1.getParent().getIndex(child_1_1));
		// the complete subtree is removed, i.e. child_1_1 and child_1_1_1
		assertEquals(set(child_1), treeControl.getData().getSelectionModel().getSelection());

	}

	public void testMultiSelection() {
		DefaultMutableTLTreeModel appModel = TestTLTreeModelHelper.createInfiniteTree(3, "root");
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, false);
		DefaultMultiSelectionModel newSelectionModel = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, newSelectionModel, DefaultTreeRenderer.INSTANCE));

		writeControl(treeControl);

		DefaultMutableTLTreeNode root = appModel.getRoot();
		DefaultMutableTLTreeNode child_1 = root.getChildAt(0);
		DefaultMutableTLTreeNode child_2 = root.getChildAt(1);
		DefaultMutableTLTreeNode child_1_1 = child_1.getChildAt(0);
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.getChildAt(0);

		selectNode(treeControl, child_1, true);
		selectNode(treeControl, child_2, true);
		assertEquals(set(child_1, child_2), treeControl.getData().getSelectionModel().getSelection());

		selectNode(treeControl, child_2, true);
		assertEquals(set(child_1), treeControl.getData().getSelectionModel().getSelection());

		selectNode(treeControl, child_1_1, true);
		selectNode(treeControl, child_1_1_1, true);
		assertEquals(set(child_1, child_1_1, child_1_1_1), treeControl.getData().getSelectionModel().getSelection());

		selectNode(treeControl, child_2, false);
		assertEquals(set(child_2), treeControl.getData().getSelectionModel().getSelection());
	}

	/**
	 * Regression test for Ticket #3178.
	 * 
	 * @see "Ticket #3178"
	 */
	public void testInvalidateInvisibleRoot() throws IOException {
		DefaultMutableTLTreeModel appModel = new DefaultMutableTLTreeModel();
		appModel.getRoot().createChild("n1");
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, false);
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, selectionModel, DefaultTreeRenderer.INSTANCE));
		
		writeControl(treeControl);

		model.setExpanded(model.getRoot(), true);

		getControlDocument(treeControl);
	}

	/**
	 * Regression test for Ticket #20954.
	 * 
	 * @see "Ticket #20954"
	 */
	public void testUpdateInvisibleRootAfterMove() {
		Map<Object, Control> nodeControls = new HashMap<>();
		DefaultMutableTLTreeModel appModel = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode invisibleRoot = appModel.getRoot();
		DefaultMutableTLTreeNode node = invisibleRoot.createChild("n1");
		DefaultMutableTLTreeNode child1 = node.createChild("n1_1");
		DefaultMutableTLTreeNode child2 = node.createChild("n1_2");

		// Test invisible root
		boolean rootVisible = false;
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, rootVisible);
		TreeRenderer treeRenderer = createTreeRenderer(nodeControls);
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, selectionModel, treeRenderer));

		TreeUIModelUtil.setExpandedAll(model, model.getRoot(), true);
		writeControl(treeControl);

		{
			Control nodeControlBeforeMove = nodeControls.get(node);
			Control child1ControlBeforeMove = nodeControls.get(child1);
			Control child2ControlBeforeMove = nodeControls.get(child2);
			assertTrue("Node is displayed by control", nodeControlBeforeMove.isAttached());
			assertTrue("Node is displayed by control", child1ControlBeforeMove.isAttached());
			assertTrue("Node is displayed by control", child2ControlBeforeMove.isAttached());
			child2.moveTo(node, 0);
			assertEquals(list(child2, child1), node.getChildren());
			revalidate(treeControl, new StringWriter());
			assertFalse("Node is repainted with new control", nodeControlBeforeMove.isAttached());
			assertFalse("Node is repainted with new control", child1ControlBeforeMove.isAttached());
			assertFalse("Node is repainted with new control", child2ControlBeforeMove.isAttached());
		}
		{
			Control nodeControlBeforeMove = nodeControls.get(node);
			Control child1ControlBeforeMove = nodeControls.get(child1);
			Control child2ControlBeforeMove = nodeControls.get(child2);
			assertTrue("Node is displayed by control", nodeControlBeforeMove.isAttached());
			assertTrue("Node is displayed by control", child1ControlBeforeMove.isAttached());
			assertTrue("Node is displayed by control", child2ControlBeforeMove.isAttached());
			child1.moveTo(invisibleRoot, 0);
			assertEquals(list(child1, node), invisibleRoot.getChildren());
			assertEquals(list(child2), node.getChildren());
			revalidate(treeControl, new StringWriter());
			assertFalse("Node is repainted with new control", nodeControlBeforeMove.isAttached());
			assertFalse("Node is repainted with new control", child1ControlBeforeMove.isAttached());
			assertFalse("Node is repainted with new control", child2ControlBeforeMove.isAttached());
		}
	}

	/**
	 * Regression test for Ticket #14815.
	 * 
	 * @see "Ticket #14815"
	 */
	public void testUpdateInvisibleRoot() throws IOException {
		DefaultMutableTLTreeModel appModel = new DefaultMutableTLTreeModel();
		appModel.getRoot().createChild("n1");
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, false);
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, selectionModel, DefaultTreeRenderer.INSTANCE));
		model.setExpanded(model.getRoot(), true);

		writeControl(treeControl);
		model.getRoot().setBusinessObject("new business object");

		getControlDocument(treeControl);
	}

	public void testSaveSelection() {
		DefaultMutableTLTreeModel appModel = TestTLTreeModelHelper.createInfiniteTree(3, "root");
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, false);
		DefaultMultiSelectionModel newSelectionModel = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, newSelectionModel, DefaultTreeRenderer.INSTANCE));

		writeControl(treeControl);

		final DefaultMutableTLTreeNode root = appModel.getRoot();
		final DefaultMutableTLTreeNode child_1 = root.getChildAt(0);
		final DefaultMutableTLTreeNode child_2 = root.getChildAt(1);
		final DefaultMutableTLTreeNode child_1_1 = child_1.getChildAt(0);
		final DefaultMutableTLTreeNode child_1_1_1 = child_1_1.getChildAt(0);

		selectNode(treeControl, child_2, true);
		selectNode(treeControl, child_1_1_1, true);

		child_1.updateNodeStructure();

		final DefaultMutableTLTreeNode new_child_1_1 = child_1.getChildAt(0);
		final DefaultMutableTLTreeNode new_child_1_1_1 = new_child_1_1.getChildAt(0);

		final SelectionModel selectionModel = treeControl.getSelectionModel();
		assertEquals(set(new_child_1_1_1, child_2), selectionModel.getSelection());
	}

	public void testRemoveNodeScopesOfDeletedNodes() {
		Map<Object, Control> nodeControls = new HashMap<>();
		DefaultMutableTLTreeModel appModel = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode childNode = appModel.getRoot().createChild("n1");
		TreeUIModel<DefaultMutableTLTreeNode> model =
			new DefaultStructureTreeUIModel<>(appModel, true);
		TreeRenderer treeRenderer = createTreeRenderer(nodeControls);
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeControl treeControl = new TreeControl(new DefaultTreeData(
			Maybe.<TreeDataOwner> none(), model, selectionModel, treeRenderer));

		TreeUIModelUtil.setExpandedAll(model, model.getRoot(), true);
		writeControl(treeControl);

		Control childControl = nodeControls.get(childNode);
		assertNotNull("Child control must exist after corresponding node has been written!");
		assertTrue("Child control must be attached after writing!", childControl.isAttached());

		DefaultMutableTLTreeNode parent = childNode.getParent();
		parent.removeChild(parent.getIndex(childNode));
		assertTrue("Tree control must have updates, due to child has been removed!", hasUpdates(treeControl));

		revalidate(treeControl, new StringWriter());
		assertFalse("Child control must be detached, after corresponding child node" +
			"has been removed from tree model", childControl.isAttached());
	}

	private TreeRenderer createTreeRenderer(final Map<Object, Control> nodeControls) {
		return new TreeRenderer() {

			@Override
			protected String getControlTag(TreeControl control) {
				return HTMLConstants.DIV;
			}

			@Override
			public TreeContentRenderer getTreeContentRenderer() {
				return new TreeContentRenderer() {

					@Override
					public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext)
							throws IOException {
						Control nodeControl = new ConstantControl<>("") {

							@Override
							protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
								// Write nothing, due to attaching of control during rendering
								// process is of interest only.
							}
						};
						nodeControl.write(context, writer);
						nodeControls.put(nodeContext.currentNode(), nodeControl);
					}

					@Override
					public ResourceProvider getResourceProvider() {
						return NoResourceProvider.INSTANCE;
					}
				};
			}

			@Override
			protected String getNodeTag() {
				return HTMLConstants.DIV;
			}
		};
	}

	private HandlerResult selectNode(TreeControl treeControl, Object node, boolean ctrlModifier) {
		String selectCommand =
			(String) ReflectionUtils.getStaticValue("com.top_logic.layout.tree.TreeControl$NodeSelectAction",
				"COMMAND_NAME");
		String ctrlModifierParam =
			(String) ReflectionUtils.getStaticValue("com.top_logic.layout.tree.TreeControl$NodeSelectAction",
				"CTRL_PARAM");

		SelectionModel selectionModel = treeControl.getData().getSelectionModel();
		boolean currentlySelected = selectionModel.isSelected(node);
		final String nodeId = treeControl.getNodeId(node);
		HashMap<String, Object> arguments = new HashMap<>();
		arguments.put(ctrlModifierParam, ctrlModifier);
		arguments.put(TreeAction.ID_PARAM, nodeId);
		final HandlerResult result = executeControlCommand(treeControl, selectCommand, arguments);
		if (result.isSuccess()) {
			boolean nowSelected = treeControl.getSelectionModel().isSelected(node);
			if (ctrlModifier && currentlySelected) {
				assertFalse("No error but given node is already selected", nowSelected);
			} else {
				assertTrue("No error but given node was not selected", nowSelected);
			}
		}
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return TestControl.suite(TestTreeControl.class);
	}

}
