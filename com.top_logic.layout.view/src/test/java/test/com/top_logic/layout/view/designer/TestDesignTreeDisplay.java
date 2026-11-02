/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.view.designer.AddChildCommand;
import com.top_logic.layout.view.designer.DesignTreeBuilder;
import com.top_logic.layout.view.designer.DesignTreeDisplay;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.RemoveElementCommand;
import com.top_logic.layout.view.designer.VirtualDesignTreeNode;
import com.top_logic.layout.view.element.AppShellElement;
import com.top_logic.layout.view.element.StackElement;

/**
 * Tests {@link DesignTreeDisplay}: a property group with a single entry shares the row of that
 * entry, every other node is displayed in a row of its own.
 */
public class TestDesignTreeDisplay extends TestCase {

	private static final String FILE = "/WEB-INF/views/test.view.xml";

	private DesignTreeNode _shell;

	private DesignTreeNode _content;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		buildShell(TypedConfiguration.newConfigItem(AppShellElement.Config.class));
	}

	/**
	 * Builds the tree of an app shell whose content holds a stack, which is no element of the
	 * content's default type.
	 *
	 * <p>
	 * The elements of the content are keyed by their type, so a further element added with the
	 * default type is distinct from the stack.
	 * </p>
	 */
	private DesignTreeNode buildShellWithStack() throws Exception {
		AppShellElement.Config shell = TypedConfiguration.newConfigItem(AppShellElement.Config.class);
		shell.getContent().add(TypedConfiguration.newConfigItem(StackElement.Config.class));
		buildShell(shell);
		return _content.getChildren().get(0);
	}

	private void buildShell(AppShellElement.Config shell) throws Exception {
		_shell = new DesignTreeBuilder().build(shell, FILE);
		_content = groupFor(_shell, "content");
		assertTrue("The content property is a group.", _content instanceof VirtualDesignTreeNode);
	}

	/**
	 * A group with a single entry is displayed in the row of that entry, which is labelled with both.
	 */
	public void testSingleEntryGroupIsCompacted() {
		DesignTreeNode entry = AddChildCommand.execute(_content, null);

		List<DesignTreeNode> rows = DesignTreeDisplay.displayedChildren(_shell);
		assertTrue("The entry takes the place of its group.", rows.contains(entry));
		assertFalse("The group has no row of its own.", rows.contains(_content));
		assertEquals("Groups without the single-entry group are displayed as they are.",
			_shell.getChildren().size(), rows.size());

		assertSame("The row of the group is the row of its entry.", entry, DesignTreeDisplay.displayedNode(_content));
		assertSame("The entry shares its row with the group.", _content, DesignTreeDisplay.sharedGroup(entry));
		assertEquals(_content.getDisplayLabel() + DesignTreeDisplay.GROUP_SEPARATOR + entry.getDisplayLabel(),
			DesignTreeDisplay.displayLabel(entry));
		assertEquals("The entry's children are displayed below the shared row.",
			entry.getChildren().size(), DesignTreeDisplay.displayedChildren(entry).size());
	}

	/**
	 * A group without an entry is displayed as a row of its own.
	 */
	public void testEmptyGroupIsNotCompacted() {
		assertTrue("The empty group has a row of its own.",
			DesignTreeDisplay.displayedChildren(_shell).contains(_content));
		assertSame(_content, DesignTreeDisplay.displayedNode(_content));
		assertEquals(_content.getDisplayLabel(), DesignTreeDisplay.displayLabel(_content));
	}

	/**
	 * A group with two entries is displayed as a row of its own, the entries below it.
	 */
	public void testTwoEntryGroupIsNotCompacted() throws Exception {
		DesignTreeNode first = buildShellWithStack();
		DesignTreeNode second = AddChildCommand.execute(_content, null);

		assertTrue("The group has a row of its own.", DesignTreeDisplay.displayedChildren(_shell).contains(_content));
		assertEquals(List.of(first, second), DesignTreeDisplay.displayedChildren(_content));
		assertNull("An entry of a group with several shares no row.", DesignTreeDisplay.sharedGroup(first));
		assertEquals(first.getDisplayLabel(), DesignTreeDisplay.displayLabel(first));
	}

	/**
	 * Adding a second entry through the row of the first one displays the group in a row of its own
	 * again, removing an entry leaves the group's single remaining entry sharing its row.
	 */
	public void testEditsChangeCompaction() throws Exception {
		DesignTreeNode first = buildShellWithStack();
		DesignTreeNode group = DesignTreeDisplay.sharedGroup(first);
		assertSame(_content, group);

		DesignTreeNode second = AddChildCommand.execute(group, null);
		assertNotNull("A second entry is added to the group.", second);
		assertTrue("The group has a row of its own.", DesignTreeDisplay.displayedChildren(_shell).contains(_content));
		assertNull(DesignTreeDisplay.sharedGroup(first));

		assertTrue(RemoveElementCommand.execute(second));
		assertSame("The remaining entry shares the row of the group.", first,
			DesignTreeDisplay.displayedNode(_content));

		assertTrue(RemoveElementCommand.execute(first));
		assertSame("The empty group has a row of its own.", _content, DesignTreeDisplay.displayedNode(_content));
		assertTrue("An entry can be added to the empty group.", AddChildCommand.canExecute(_content));
	}

	/**
	 * The commands of a shared row act on the entry as well as on its group, so that a further
	 * element can be added to the group.
	 */
	public void testCommandTargetsOfSharedRow() {
		DesignTreeNode entry = AddChildCommand.execute(_content, null);

		List<DesignTreeNode> targets = DesignTreeDisplay.commandTargets(entry);
		assertEquals(List.of(entry, _content), targets);
		assertTrue("The entry can be removed.", RemoveElementCommand.canExecute(targets.get(0)));
		assertTrue("A further element can be added to the group.", AddChildCommand.canExecute(targets.get(1)));
	}

	/**
	 * A node displayed in a row of its own is the only target of the commands of its row.
	 */
	public void testCommandTargetsOfOwnRow() throws Exception {
		DesignTreeNode stack = new DesignTreeBuilder().build(
			TypedConfiguration.newConfigItem(StackElement.Config.class), FILE);
		DesignTreeNode child = AddChildCommand.execute(stack, null);

		assertEquals("An inlined property has no group.", List.of(child), DesignTreeDisplay.commandTargets(child));
		assertEquals(List.of(_content), DesignTreeDisplay.commandTargets(_content));
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
	 * Test suite requiring the {@link TypeIndex} module, used to resolve property options, and the
	 * resources the labels are resolved from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDesignTreeDisplay.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
