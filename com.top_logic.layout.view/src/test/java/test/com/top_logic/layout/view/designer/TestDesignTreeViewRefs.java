/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.OpenDialogAction;
import com.top_logic.layout.view.designer.AddChildCommand;
import com.top_logic.layout.view.designer.ConfigDesignTreeNode;
import com.top_logic.layout.view.designer.DesignTreeBuilder;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.DesignTreeNodes;
import com.top_logic.layout.view.designer.ErrorDesignTreeNode;
import com.top_logic.layout.view.designer.ViewLinkDesignTreeNode;
import com.top_logic.layout.view.designer.VirtualDesignTreeNode;
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.ButtonElement;
import com.top_logic.layout.view.tiles.FrameRouteConfig;
import com.top_logic.layout.view.tiles.NavigatePushCommand;
import com.top_logic.layout.view.tiles.TileStackElement;

/**
 * Tests that {@link DesignTreeBuilder} shows the views a configuration refers to through its
 * {@link com.top_logic.layout.view.ViewRef} properties.
 */
public class TestDesignTreeViewRefs extends TestCase {

	private File _webapp;

	private File _views;

	private FileManager _fileManagerBefore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_webapp = Files.createTempDirectory("tl-design-tree-view-refs").toFile();
		_views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		_views.mkdirs();
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));
	}

	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);
		super.tearDown();
	}

	/**
	 * A {@code <view-ref>} shows the referenced view as its only child, which cannot be edited
	 * structurally from the referencing node.
	 */
	public void testViewRefIsResolved() throws Exception {
		view("a-root.view.xml", "<view-ref view='a-child.view.xml'/>");
		view("a-child.view.xml", "<panel title='Child'/>");

		DesignTreeNode root = build("a-root.view.xml");

		DesignTreeNode ref = single(root, node -> configOf(node) instanceof ReferenceElement.Config);
		assertEquals("The referenced view is the only child.", 1, ref.getChildren().size());
		assertEquals(path("a-child.view.xml"), ref.getChildren().get(0).getSourceFile());
		assertEquals("A view of a file of its own is named by the file.", "a-child.view.xml",
			ref.getChildren().get(0).getLabel());
		assertEquals("The view of the root file is named by the file.", "a-root.view.xml", root.getLabel());
		assertNull("The reference is no child container.", ref.getChildContainer());
		assertFalse("Nothing can be added to a reference.", AddChildCommand.canExecute(ref));
		assertNull("The referenced view is no element of the referencing node.",
			DesignTreeNodes.owningContainer(ref.getChildren().get(0)));
	}

	/**
	 * The initial view and the frame views of a tile stack are resolved, each under a group named
	 * by its property.
	 */
	public void testTileStackFramesAreResolved() throws Exception {
		view("b-root.view.xml",
			"<channels><channel name='navPath'/></channels>"
				+ "<tile-stack initial='b-initial.view.xml' path='navPath'>"
				+ "<frame route='detail' view='b-frame.view.xml'/>"
				+ "</tile-stack>");
		view("b-initial.view.xml", "<panel title='Initial'/>");
		view("b-frame.view.xml", "<panel title='Frame'/>");

		DesignTreeNode root = build("b-root.view.xml");
		DesignTreeNode stack = single(root, node -> configOf(node) instanceof TileStackElement.Config);

		DesignTreeNode initialGroup = group(stack, TileStackElement.Config.INITIAL);
		assertNull("A view reference group is no child container.", initialGroup.getChildContainer());
		assertEquals(1, initialGroup.getChildren().size());
		assertEquals(path("b-initial.view.xml"), initialGroup.getChildren().get(0).getSourceFile());

		DesignTreeNode framesGroup = group(stack, TileStackElement.Config.FRAMES);
		assertNotNull("The frames can be edited.", framesGroup.getChildContainer());
		assertEquals(1, framesGroup.getChildren().size());
		DesignTreeNode frame = framesGroup.getChildren().get(0);
		assertTrue(configOf(frame) instanceof FrameRouteConfig);
		assertEquals("The frame view is the frame's only child.", 1, frame.getChildren().size());
		assertEquals(path("b-frame.view.xml"), frame.getChildren().get(0).getSourceFile());

		assertNotNull("A frame can be added.", AddChildCommand.execute(framesGroup, null));
		assertEquals(2, ((TileStackElement.Config) configOf(stack)).getFrames().size());
	}

	/**
	 * A view pushed from the activation command of a table is resolved below that command.
	 */
	public void testNavigatePushInTableIsResolved() throws Exception {
		view("c-root.view.xml",
			"<table><rows>list()</rows>"
				+ "<on-activate class='" + NavigatePushCommand.class.getName() + "' view='c-pushed.view.xml'/>"
				+ "</table>");
		view("c-pushed.view.xml", "<panel title='Pushed'/>");

		DesignTreeNode root = build("c-root.view.xml");

		DesignTreeNode pushed = viewRoot(root, "c-pushed.view.xml");
		assertTrue("The pushed view is shown below its command.",
			configOf(pushed.getParent()) instanceof NavigatePushCommand.Config);
		DesignTreeNode group = pushed.getParent().getParent();
		assertTrue(group instanceof VirtualDesignTreeNode);
		assertEquals(AbstractTableElement.Config.ON_ACTIVATE, group.getProperty().getPropertyName());
	}

	/**
	 * A dialog opened by an action of a command is resolved below that action.
	 */
	public void testOpenDialogInCommandIsResolved() throws Exception {
		view("d-root.view.xml",
			"<panel title='Panel'><commands>"
				+ "<generic-command name='open'><open-dialog dialog-view='d-dialog.view.xml'/></generic-command>"
				+ "</commands></panel>");
		view("d-dialog.view.xml", "<panel title='Dialog'/>");

		DesignTreeNode root = build("d-root.view.xml");

		DesignTreeNode dialog = viewRoot(root, "d-dialog.view.xml");
		assertTrue("The dialog is shown below the action opening it.",
			configOf(dialog.getParent()) instanceof OpenDialogAction.Config);
	}

	/**
	 * A view referenced twice is expanded at its first occurrence only, the second occurrence links
	 * to it.
	 */
	public void testSecondReferenceIsLink() throws Exception {
		view("e-root.view.xml",
			"<stack><view-ref view='e-shared.view.xml'/><view-ref view='e-shared.view.xml'/></stack>");
		view("e-shared.view.xml", "<panel title='Shared'/>");

		DesignTreeNode root = build("e-root.view.xml");

		List<DesignTreeNode> refs = all(root, node -> configOf(node) instanceof ReferenceElement.Config);
		assertEquals(2, refs.size());
		assertEquals(path("e-shared.view.xml"), refs.get(0).getChildren().get(0).getSourceFile());

		DesignTreeNode link = refs.get(1).getChildren().get(0);
		assertTrue("The second reference is a link: " + link, link instanceof ViewLinkDesignTreeNode);
		assertEquals(path("e-shared.view.xml"), ((ViewLinkDesignTreeNode) link).getTargetFile());
		assertTrue("A link is a leaf.", link.getChildren().isEmpty());
		assertEquals("→ e-shared.view.xml", link.getDisplayLabel());
		assertNull("A link carries no configuration.", DesignTreeNodes.configOf(link));
		assertEquals("The shared view is expanded once.", 1,
			all(root, node -> path("e-shared.view.xml").equals(node.getSourceFile())
				&& !path("e-shared.view.xml").equals(node.getParent().getSourceFile())).size());
	}

	/**
	 * A view referring back to a view containing it ends in a link.
	 */
	public void testCycleTerminates() throws Exception {
		view("f-a.view.xml", "<view-ref view='f-b.view.xml'/>");
		view("f-b.view.xml", "<view-ref view='f-a.view.xml'/>");

		DesignTreeNode root = build("f-a.view.xml");

		List<DesignTreeNode> links = all(root, node -> node instanceof ViewLinkDesignTreeNode);
		assertEquals(1, links.size());
		ViewLinkDesignTreeNode link = (ViewLinkDesignTreeNode) links.get(0);
		assertEquals("The cycle ends at the root view.", path("f-a.view.xml"), link.getTargetFile());
		assertEquals("The link belongs to the referring view.", path("f-b.view.xml"), link.getSourceFile());
	}

	/**
	 * A reference to a view that cannot be loaded shows an error node.
	 */
	public void testBrokenReferenceIsErrorNode() throws Exception {
		view("g-root.view.xml", "<view-ref view='g-missing.view.xml'/>");

		DesignTreeNode root = build("g-root.view.xml");

		DesignTreeNode ref = single(root, node -> configOf(node) instanceof ReferenceElement.Config);
		assertEquals(1, ref.getChildren().size());
		assertTrue(ref.getChildren().get(0) instanceof ErrorDesignTreeNode);
	}

	/**
	 * An unset view reference adds no child.
	 */
	public void testUnsetReferenceAddsNoChild() throws Exception {
		TileStackElement.Config stack = TypedConfiguration.newConfigItem(TileStackElement.Config.class);

		DesignTreeNode node = new DesignTreeBuilder().build(stack, path("h-root.view.xml"));

		assertTrue("Without an initial view, only the frames are shown.", node.getChildren().isEmpty());
		assertEquals("The frames are the only child source, so they are inlined.",
			TileStackElement.Config.FRAMES, node.getChildContainer().getProperty().getPropertyName());
	}

	/**
	 * The action of a button is a single-valued polymorphic tree property, which is filled by
	 * choosing one of the command types.
	 */
	public void testButtonActionCanBeAdded() throws Exception {
		ButtonElement.Config button = TypedConfiguration.newConfigItem(ButtonElement.Config.class);
		DesignTreeNode node = new DesignTreeBuilder().build(button, path("i-root.view.xml"));

		assertTrue("An action can be added.", AddChildCommand.canExecute(node));
		List<?> types = node.getChildContainer().allowedTypes().options();
		Object push = types.stream()
			.filter(option -> implementationOf(option) == NavigatePushCommand.class).findFirst().orElse(null);
		assertNotNull("The command types are offered: " + types, push);

		DesignTreeNode action = AddChildCommand.execute(node, push);

		assertTrue(configOf(action) instanceof NavigatePushCommand.Config);
		assertSame(configOf(action), button.getAction());
		assertFalse("A button has a single action.", AddChildCommand.canExecute(node));
	}

	private static Object implementationOf(Object typeOption) {
		return typeOption instanceof PolymorphicConfiguration<?> config ? config.getImplementationClass() : typeOption;
	}

	private void view(String name, String content) throws IOException {
		File file = new File(_views, name);
		Files.writeString(file.toPath(),
			"<?xml version='1.0' encoding='utf-8'?><view>" + content + "</view>", StandardCharsets.UTF_8);
	}

	private static String path(String name) {
		return ViewLoader.fullPath(name);
	}

	private static DesignTreeNode build(String name) throws Exception {
		return new DesignTreeBuilder().build(path(name));
	}

	private static Object configOf(DesignTreeNode node) {
		return node instanceof ConfigDesignTreeNode configNode ? configNode.getConfig() : null;
	}

	/**
	 * The node the given view file is expanded to.
	 */
	private static DesignTreeNode viewRoot(DesignTreeNode root, String name) {
		return single(root, node -> path(name).equals(node.getSourceFile()) && node.getParent() != null
			&& !path(name).equals(node.getParent().getSourceFile()));
	}

	private static DesignTreeNode group(DesignTreeNode node, String propertyName) {
		return single(node, child -> child instanceof VirtualDesignTreeNode
			&& child.getParent() == node && propertyName.equals(child.getProperty().getPropertyName()));
	}

	private static DesignTreeNode single(DesignTreeNode root, Predicate<DesignTreeNode> test) {
		List<DesignTreeNode> result = all(root, test);
		assertEquals("Exactly one matching node.", 1, result.size());
		return result.get(0);
	}

	private static List<DesignTreeNode> all(DesignTreeNode root, Predicate<DesignTreeNode> test) {
		List<DesignTreeNode> result = new ArrayList<>();
		collect(root, test, result);
		return result;
	}

	private static void collect(DesignTreeNode node, Predicate<DesignTreeNode> test, List<DesignTreeNode> result) {
		if (test.test(node)) {
			result.add(node);
		}
		for (DesignTreeNode child : node.getChildren()) {
			collect(child, test, result);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, used while parsing polymorphic properties.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDesignTreeViewRefs.class, TypeIndex.Module.INSTANCE);
	}
}
