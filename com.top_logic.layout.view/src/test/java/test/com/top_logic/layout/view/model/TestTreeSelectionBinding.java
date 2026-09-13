/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.TreeSelectionBinding;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Tests for {@link TreeSelectionBinding}.
 *
 * <p>
 * What a display bound to the selection channel sees is the business object of the selected node,
 * the set of those objects while several nodes are selected, and nothing while none is - never the
 * tree nodes themselves.
 * </p>
 */
public class TestTreeSelectionBinding extends TestCase {

	/** The business object at the root of the test tree. */
	private static final String ROOT = "root";

	/** The business object of a node below the root. */
	private static final String A = "a";

	/** The business object of a node below the root. */
	private static final String B = "b";

	/** The business object of a node below the root. */
	private static final String C = "c";

	private ViewChannel _channel;

	private DefaultTreeUINodeModel _tree;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel("selection");
		_tree = tree();
	}

	/**
	 * Tests that the node a tree selecting a single node holds is written to the channel as its
	 * business object, and that giving the selection up clears the channel.
	 */
	public void testSingleSelection() {
		SelectionModel<Object> selection = new DefaultSingleSelectionModel<>(SelectionModelOwner.NO_OWNER);
		selection.addSelectionListener(new TreeSelectionBinding<>(_channel));

		selection.setSelected(node(A), true);
		assertEquals("The business object of the selected node is the selection.", A, _channel.get());

		selection.setSelected(node(B), true);
		assertEquals("Selecting another node replaces the value.", B, _channel.get());

		selection.clear();
		assertNull("Nothing is selected any more.", _channel.get());
	}

	/**
	 * Tests that a tree selecting any number of nodes writes one selected node as its business
	 * object, several as the set of their business objects, and none as nothing.
	 */
	public void testMultiSelection() {
		SelectionModel<Object> selection = new DefaultMultiSelectionModel<>(SelectionModelOwner.NO_OWNER);
		selection.addSelectionListener(new TreeSelectionBinding<>(_channel));

		selection.setSelected(node(A), true);
		assertEquals("A single selected node is its business object, as in a tree selecting one.",
			A, _channel.get());

		selection.setSelected(node(C), true);
		assertEquals("Several selected nodes are the set of their business objects.",
			Set.of(A, C), _channel.get());

		selection.setSelected(node(B), true);
		assertEquals(Set.of(A, B, C), _channel.get());

		selection.setSelected(node(A), false);
		assertEquals(Set.of(B, C), _channel.get());

		selection.setSelected(node(B), false);
		assertEquals("One node left of the selection is that object again.", C, _channel.get());

		selection.clear();
		assertNull("Nothing is selected any more.", _channel.get());
	}

	/**
	 * The node of the test tree standing for the given business object.
	 */
	private DefaultTreeUINode node(Object businessObject) {
		for (DefaultTreeUINode child : _tree.getChildren(_tree.getRoot())) {
			if (businessObject.equals(child.getBusinessObject())) {
				return child;
			}
		}
		throw new IllegalArgumentException("No node for '" + businessObject + "'.");
	}

	/**
	 * A tree whose root has a node for each of {@link #A}, {@link #B} and {@link #C}.
	 */
	private static DefaultTreeUINodeModel tree() {
		TreeBuilder<DefaultTreeUINode> builder = new TreeBuilder<>() {
			@Override
			public DefaultTreeUINode createNode(AbstractMutableTLTreeModel<DefaultTreeUINode> model,
					DefaultTreeUINode parent, Object userObject) {
				return new DefaultTreeUINode(model, parent, userObject);
			}

			@Override
			public List<DefaultTreeUINode> createChildList(DefaultTreeUINode node) {
				List<DefaultTreeUINode> children = new ArrayList<>();
				if (ROOT.equals(node.getBusinessObject())) {
					for (String businessObject : new String[] { A, B, C }) {
						children.add(createNode(node.getModel(), node, businessObject));
					}
				}
				return children;
			}

			@Override
			public boolean isFinite() {
				return true;
			}
		};
		return new DefaultTreeUINodeModel(builder, ROOT);
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTreeSelectionBinding.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
