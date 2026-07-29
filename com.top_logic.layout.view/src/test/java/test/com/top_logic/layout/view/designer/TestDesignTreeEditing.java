/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.designer.AddChildCommand;
import com.top_logic.layout.view.designer.DesignTreeBuilder;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.ErrorDesignTreeNode;
import com.top_logic.layout.view.designer.MoveElementCommand;
import com.top_logic.layout.view.designer.MoveElementCommand.Direction;
import com.top_logic.layout.view.designer.RemoveElementCommand;
import com.top_logic.layout.view.element.AppShellElement;
import com.top_logic.layout.view.element.StackElement;

/**
 * Tests that the structural editing commands of the design tree change the configuration, not only
 * the tree.
 */
public class TestDesignTreeEditing extends TestCase {

	private static final String FILE = "/WEB-INF/views/test.view.xml";

	private DesignTreeNode build(ConfigurationItem config) throws Exception {
		return new DesignTreeBuilder().build(config, FILE);
	}

	private static List<PolymorphicConfiguration<?>> children(StackElement.Config config) {
		return List.copyOf(config.getChildren());
	}

	/**
	 * A configuration with a single tree property is bound to it directly, without a group node.
	 */
	public void testInlinedContainerIsBound() throws Exception {
		StackElement.Config stack = TypedConfiguration.newConfigItem(StackElement.Config.class);
		DesignTreeNode node = build(stack);

		assertNotNull("The node is bound to its only tree property", node.getChildContainer());
		assertEquals("children", node.getChildContainer().getProperty().getPropertyName());
		assertTrue("An element can be added", AddChildCommand.canExecute(node));
	}

	/**
	 * A configuration with several tree properties gets one group node per property, each bound to
	 * its own property.
	 */
	public void testGroupNodesAreBound() throws Exception {
		AppShellElement.Config shell = TypedConfiguration.newConfigItem(AppShellElement.Config.class);
		DesignTreeNode node = build(shell);

		assertNull("A node with several tree properties is not itself a container",
			node.getChildContainer());
		assertEquals("One group node per tree property", 3, node.getChildren().size());

		for (DesignTreeNode group : node.getChildren()) {
			assertNotNull("A group node is bound to its property", group.getChildContainer());
			String property = group.getChildContainer().getProperty().getPropertyName();
			assertTrue("An element can be added to group " + property, AddChildCommand.canExecute(group));
		}
	}

	/**
	 * Adding an element inserts it into the configuration, so that it is saved.
	 */
	public void testAddReachesConfiguration() throws Exception {
		StackElement.Config stack = TypedConfiguration.newConfigItem(StackElement.Config.class);
		DesignTreeNode node = build(stack);

		DesignTreeNode child = AddChildCommand.execute(node, null);

		assertNotNull("A child node is created", child);
		assertEquals("The element reached the configuration", 1, stack.getChildren().size());
		assertEquals("The tree shows the new element", 1, node.getChildren().size());
		assertTrue("The change is marked for saving", node.isDirty());
	}

	/**
	 * Adding to a group node inserts into that group's property only.
	 */
	public void testAddReachesGroupProperty() throws Exception {
		AppShellElement.Config shell = TypedConfiguration.newConfigItem(AppShellElement.Config.class);
		DesignTreeNode node = build(shell);

		DesignTreeNode contentGroup = groupFor(node, "content");
		assertNotNull("Content group is present", AddChildCommand.execute(contentGroup, null));

		assertEquals("The element reached the content property", 1, shell.getContent().size());
		assertEquals("The header property is untouched", 0, shell.getHeader().size());
		assertEquals("The footer property is untouched", 0, shell.getFooter().size());
	}

	/**
	 * Removing an element removes it from the configuration, so that it is saved.
	 */
	public void testRemoveReachesConfiguration() throws Exception {
		StackElement.Config stack = TypedConfiguration.newConfigItem(StackElement.Config.class);
		DesignTreeNode node = build(stack);
		AddChildCommand.execute(node, null);
		DesignTreeNode second = AddChildCommand.execute(node, null);
		PolymorphicConfiguration<?> remaining = stack.getChildren().get(0);

		assertTrue("The element can be removed", RemoveElementCommand.canExecute(second));
		assertTrue(RemoveElementCommand.execute(second));

		assertEquals("Only the removed element is gone", List.of(remaining), children(stack));
		assertEquals("The tree shows the remaining element", 1, node.getChildren().size());
	}

	/**
	 * Reordering elements reorders the configuration, so that it is saved.
	 */
	public void testMoveReachesConfiguration() throws Exception {
		StackElement.Config stack = TypedConfiguration.newConfigItem(StackElement.Config.class);
		DesignTreeNode node = build(stack);
		DesignTreeNode first = AddChildCommand.execute(node, null);
		DesignTreeNode second = AddChildCommand.execute(node, null);
		List<PolymorphicConfiguration<?>> initial = children(stack);

		assertFalse("The first element cannot move up", MoveElementCommand.canExecute(first, Direction.UP));
		assertTrue("The first element can move down", MoveElementCommand.canExecute(first, Direction.DOWN));
		assertTrue(MoveElementCommand.execute(first, Direction.DOWN));

		assertEquals("The configuration order is swapped",
			List.of(initial.get(1), initial.get(0)), children(stack));
		assertEquals("The tree order is swapped", List.of(second, first), node.getChildren());
	}

	/**
	 * The root of the design tree is not an element of any container, so it cannot be edited away.
	 */
	public void testRootIsNotEditable() throws Exception {
		StackElement.Config stack = TypedConfiguration.newConfigItem(StackElement.Config.class);
		DesignTreeNode node = build(stack);

		assertFalse("The root cannot be removed", RemoveElementCommand.canExecute(node));
		assertFalse("The root cannot be moved", MoveElementCommand.canExecute(node, Direction.UP));
		assertFalse("The root cannot be moved", MoveElementCommand.canExecute(node, Direction.DOWN));
	}

	/**
	 * A node that carries no configuration is not offered any structural operation.
	 */
	public void testErrorNodeIsNotEditable() throws Exception {
		StackElement.Config stack = TypedConfiguration.newConfigItem(StackElement.Config.class);
		DesignTreeNode node = build(stack);

		DesignTreeNode errorNode = new ErrorDesignTreeNode(FILE, "missing.view.xml", "not found");
		node.getChildren().add(errorNode);

		assertFalse("A node without configuration cannot be removed",
			RemoveElementCommand.canExecute(errorNode));
		assertFalse("A node without configuration cannot be moved",
			MoveElementCommand.canExecute(errorNode, Direction.UP));
		assertFalse("A node without configuration has nothing to add to",
			AddChildCommand.canExecute(errorNode));
	}

	private static DesignTreeNode groupFor(DesignTreeNode node, String propertyName) {
		for (DesignTreeNode group : node.getChildren()) {
			if (propertyName.equals(group.getChildContainer().getProperty().getPropertyName())) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, used to resolve property options.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDesignTreeEditing.class, TypeIndex.Module.INSTANCE);
	}
}
