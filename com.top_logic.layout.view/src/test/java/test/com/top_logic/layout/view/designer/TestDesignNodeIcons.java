/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.reflect.TypeDescriptor;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.NotifyAction;
import com.top_logic.layout.view.designer.ConfigDesignTreeNode;
import com.top_logic.layout.view.designer.DesignNodeKind;
import com.top_logic.layout.view.designer.DesignTreeBuilder;
import com.top_logic.layout.view.designer.DesignTreeDisplay;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.DesignTreeNodeResourceProvider;
import com.top_logic.layout.view.designer.ErrorDesignTreeNode;
import com.top_logic.layout.view.designer.ViewLinkDesignTreeNode;
import com.top_logic.layout.view.designer.VirtualDesignTreeNode;
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.AppShellElement;
import com.top_logic.layout.view.element.CommandScopeElement;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.element.StackElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TreeElement;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.element.WindowElement;

/**
 * Tests the icons and styles the {@link DesignTreeNodeResourceProvider} gives the rows of the design
 * tree: the {@link DesignNodeKind} of a node, and the {@link TypeIcon} of the configuration of an
 * element or command.
 */
public class TestDesignNodeIcons extends TestCase {

	private static final String FILE = "/WEB-INF/views/test.view.xml";

	private static final DesignTreeNodeResourceProvider PROVIDER = DesignTreeNodeResourceProvider.INSTANCE;

	/**
	 * Every UI element type has an icon: its own, an inherited one, or the one of its kind.
	 *
	 * <p>
	 * The element types are the configurations of the concrete {@link UIElement} implementations
	 * known to the {@link TypeIndex}. The descriptors are used, so that no implementation class is
	 * initialized (some have static initializers requiring a running application).
	 * </p>
	 */
	public void testEveryElementTypeHasIcon() {
		List<String> missing = new ArrayList<>();
		int checked = 0;
		for (TypeDescriptor implementation : concreteSpecializations(UIElement.class)) {
			TypeDescriptor configDescriptor = implementation.getConfiguration();
			if (configDescriptor == null) {
				continue;
			}
			Class<?> configType = load(configDescriptor.getClassName());
			if (configType == null || !UIElement.Config.class.isAssignableFrom(configType)) {
				continue;
			}
			@SuppressWarnings("unchecked")
			ConfigurationItem config =
				TypedConfiguration.newConfigItem((Class<? extends ConfigurationItem>) configType);
			ThemeImage icon = DesignTreeNodeResourceProvider.icon(config);
			if (icon == null || icon.resolve() == ThemeImage.none()) {
				missing.add(configType.getName());
			}
			checked++;
		}
		assertTrue("UI element types found: " + checked, checked > 50);
		assertEquals("UI element types without an icon.", List.of(), missing);

	}

	/**
	 * The concrete transitive specializations of the given type as known to the {@link TypeIndex}.
	 */
	private static Set<TypeDescriptor> concreteSpecializations(Class<?> type) {
		Set<TypeDescriptor> all = new HashSet<>();
		Deque<TypeDescriptor> todo = new ArrayDeque<>();
		todo.add(TypeIndex.getInstance().getDescriptor(type));
		while (!todo.isEmpty()) {
			TypeDescriptor descriptor = todo.removeFirst();
			if (all.add(descriptor)) {
				todo.addAll(descriptor.getSpecializations());
			}
		}
		all.removeIf(descriptor -> descriptor.isInterface() || descriptor.isAbstract());
		return all;
	}

	private static Class<?> load(String className) {
		try {
			return Class.forName(className, false, TestDesignNodeIcons.class.getClassLoader());
		} catch (ClassNotFoundException | LinkageError ex) {
			// Not on the class path of this module, cannot occur in its views.
			return null;
		}
	}

	/**
	 * An annotated type shows its own icon.
	 */
	public void testAnnotatedTypeHasOwnIcon() {
		assertIcon("css:bi bi-diagram-3", TreeElement.Config.class);
		assertIcon("css:bi bi-window-desktop", AppShellElement.Config.class);
		assertIcon("css:bi bi-layout-split", StackElement.Config.class);
	}

	/**
	 * A type without an annotation of its own shows the icon of the annotated type it extends.
	 */
	public void testUnannotatedTypeInherits() {
		assertNull(PanelElement.Config.class.getAnnotation(TypeIcon.class));
		assertNotNull(CommandScopeElement.Config.class.getAnnotation(TypeIcon.class));
		assertIcon("css:bi bi-window", PanelElement.Config.class);

		assertNull(TableElement.Config.class.getAnnotation(TypeIcon.class));
		assertNotNull(AbstractTableElement.Config.class.getAnnotation(TypeIcon.class));
		assertIcon("css:bi bi-table", TableElement.Config.class);
	}

	/**
	 * The most specific annotation wins over the ones of the types extended.
	 */
	public void testMostSpecificWins() {
		assertIcon("css:bi bi-list-nested", TreeTableElement.Config.class);
		assertIcon("css:bi bi-window-stack", WindowElement.Config.class);
	}

	/**
	 * The view a file starts with is a file, whatever its type.
	 */
	public void testViewFile() {
		DesignTreeNode root = new ConfigDesignTreeNode(TypedConfiguration.newConfigItem(ViewElement.Config.class), FILE);
		assertKind(DesignNodeKind.VIEW_FILE, "css:bi bi-file-earmark-code", root);
	}

	/**
	 * A UI element shows the icon of its type.
	 */
	public void testElement() throws Exception {
		DesignTreeNode shell = buildShell(TypedConfiguration.newConfigItem(AppShellElement.Config.class));
		assertKind(DesignNodeKind.ELEMENT, "css:bi bi-window-desktop", shell);
	}

	/**
	 * A command shows the icon of its type, an action without an icon of its type the icon of its
	 * kind.
	 */
	public void testCommand() {
		DesignTreeNode command =
			new ConfigDesignTreeNode(TypedConfiguration.newConfigItem(GenericViewCommand.Config.class), FILE);
		assertKind(DesignNodeKind.COMMAND, "css:bi bi-lightning", command);

		DesignTreeNode action =
			new ConfigDesignTreeNode(TypedConfiguration.newConfigItem(NotifyAction.Config.class), FILE);
		assertKind(DesignNodeKind.COMMAND, "css:bi bi-lightning", action);
	}

	/**
	 * Any other configuration is a part.
	 */
	public void testPart() {
		DesignTreeNode channel =
			new ConfigDesignTreeNode(TypedConfiguration.newConfigItem(ValueChannelConfig.class), FILE);
		assertKind(DesignNodeKind.PART, "css:bi bi-sliders", channel);
	}

	/**
	 * A property group is labelled by its property alone.
	 */
	public void testGroup() throws Exception {
		DesignTreeNode shell = buildShell(TypedConfiguration.newConfigItem(AppShellElement.Config.class));
		DesignTreeNode content = content(shell);
		assertTrue(content.getChildren().isEmpty());
		assertKind(DesignNodeKind.GROUP, "css:bi bi-diagram-2", content);
		assertEquals(content.getDisplayLabel(), PROVIDER.getLabel(content));
	}

	/**
	 * The row a group shares with its single entry shows the icon and style of the entry.
	 */
	public void testSharedRowShowsEntry() throws Exception {
		AppShellElement.Config shellConfig = TypedConfiguration.newConfigItem(AppShellElement.Config.class);
		shellConfig.getContent().add(TypedConfiguration.newConfigItem(StackElement.Config.class));
		DesignTreeNode shell = buildShell(shellConfig);
		DesignTreeNode content = content(shell);
		DesignTreeNode stack = content.getChildren().get(0);
		assertSame(stack, DesignTreeDisplay.displayedNode(content));

		assertKind(DesignNodeKind.ELEMENT, "css:bi bi-layout-split", stack);
		assertEquals(content.getDisplayLabel() + DesignTreeDisplay.GROUP_SEPARATOR + stack.getDisplayLabel(),
			PROVIDER.getLabel(stack));
	}

	/**
	 * A link to another view file is marked by its icon, not by its label.
	 */
	public void testLink() {
		DesignTreeNode link = new ViewLinkDesignTreeNode(FILE, new ViewRef("other.view.xml"));
		assertKind(DesignNodeKind.LINK, "css:bi bi-box-arrow-up-right", link);
		assertEquals("other.view.xml", PROVIDER.getLabel(link));
	}

	/**
	 * A view that cannot be loaded is marked as an error.
	 */
	public void testError() {
		DesignTreeNode error = new ErrorDesignTreeNode(FILE, "broken.view.xml", "Broken.");
		assertKind(DesignNodeKind.ERROR, "css:bi bi-exclamation-triangle", error);
		assertTrue("The label names the view.", PROVIDER.getLabel(error).contains("broken.view.xml"));
		assertTrue("The tooltip names the problem.", PROVIDER.getTooltip(error).contains("Broken."));
	}

	private static DesignTreeNode buildShell(AppShellElement.Config shell) throws Exception {
		return new DesignTreeBuilder().build(shell, FILE);
	}

	private static DesignTreeNode content(DesignTreeNode shell) {
		for (DesignTreeNode child : shell.getChildren()) {
			if (child instanceof VirtualDesignTreeNode
				&& AppShellElement.Config.CONTENT.equals(child.getProperty().getPropertyName())) {
				return child;
			}
		}
		throw new AssertionError("No content group.");
	}

	private static void assertKind(DesignNodeKind expectedKind, String expectedIcon, DesignTreeNode node) {
		assertEquals(expectedKind, DesignNodeKind.of(node));
		assertEquals(expectedKind.getCssClass(), PROVIDER.getCssClass(node));
		assertEquals(expectedIcon, encoded(PROVIDER.getImage(node, Flavor.DEFAULT)));
	}

	private static void assertIcon(String expected, Class<? extends ConfigurationItem> configType) {
		assertEquals(configType.getName(), expected,
			encoded(DesignTreeNodeResourceProvider.icon(TypedConfiguration.newConfigItem(configType))));
	}

	private static String encoded(ThemeImage icon) {
		assertNotNull("No icon.", icon);
		return icon.resolve().toEncodedForm();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the {@link ThemeFactory} the icons are
	 * resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDesignNodeIcons.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}
}
