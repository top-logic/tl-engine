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
import com.top_logic.layout.view.element.ButtonElement;
import com.top_logic.layout.view.tiles.FrameRouteConfig;
import com.top_logic.layout.view.tiles.NavigatePushCommand;
import com.top_logic.layout.view.tiles.TileStackElement;

/**
 * Tests that {@link DesignTreeBuilder} shows the views a configuration refers to through its
 * {@link com.top_logic.layout.view.ViewRef} properties as links, without expanding them.
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
	 * A {@code <view-ref>} shows the referenced view as a link leaf, which cannot be edited
	 * structurally from the referencing node.
	 */
	public void testViewRefIsLink() throws Exception {
		view("a-root.view.xml", "<view-ref view='a-child.view.xml'/>");
		view("a-child.view.xml", "<panel title='Child'/>");

		DesignTreeNode root = build("a-root.view.xml");

		DesignTreeNode ref = single(root, node -> configOf(node) instanceof ReferenceElement.Config);
		assertEquals("The referenced view is the only child.", 1, ref.getChildren().size());
		ViewLinkDesignTreeNode link = link(ref.getChildren().get(0), "a-child.view.xml");
		assertEquals("The link belongs to the referring view.", path("a-root.view.xml"), link.getSourceFile());
		assertTrue("A link is a leaf.", link.getChildren().isEmpty());
		assertEquals("a-child.view.xml", link.getDisplayLabel());
		assertNull("A link carries no configuration.", DesignTreeNodes.configOf(link));
		assertEquals("The view of the root file is named by the file.", "a-root.view.xml", root.getLabel());
		assertNull("The reference is no child container.", ref.getChildContainer());
		assertFalse("Nothing can be added to a reference.", AddChildCommand.canExecute(ref));
		assertNull("The link is no element of the referencing node.", DesignTreeNodes.owningContainer(link));
		assertOnlyFile(root, "a-root.view.xml");
	}

	/**
	 * The initial view and the frame views of a tile stack are links, each under a group named by
	 * its property.
	 */
	public void testTileStackReferencesAreLinks() throws Exception {
		view("b-root.view.xml",
			"<channels><channel name='navPath'/></channels>"
				+ "<tile-stack initial='b-initial.view.xml' path='navPath'>"
				+ "<frame id='detail' route='detail' view='b-frame.view.xml'/>"
				+ "</tile-stack>");
		view("b-initial.view.xml", "<panel title='Initial'/>");
		view("b-frame.view.xml", "<panel title='Frame'/>");

		DesignTreeNode root = build("b-root.view.xml");
		DesignTreeNode stack = single(root, node -> configOf(node) instanceof TileStackElement.Config);

		DesignTreeNode initialGroup = group(stack, TileStackElement.Config.INITIAL);
		assertNull("A view reference group is no child container.", initialGroup.getChildContainer());
		assertEquals(1, initialGroup.getChildren().size());
		link(initialGroup.getChildren().get(0), "b-initial.view.xml");

		DesignTreeNode framesGroup = group(stack, TileStackElement.Config.FRAMES);
		assertNotNull("The frames can be edited.", framesGroup.getChildContainer());
		assertEquals(1, framesGroup.getChildren().size());
		DesignTreeNode frame = framesGroup.getChildren().get(0);
		assertTrue(configOf(frame) instanceof FrameRouteConfig);
		DesignTreeNode viewGroup = group(frame, FrameRouteConfig.VIEW);
		assertEquals(1, viewGroup.getChildren().size());
		link(viewGroup.getChildren().get(0), "b-frame.view.xml");
		assertNotNull("The entries of a frame can be edited.",
			group(frame, FrameRouteConfig.CONTENTS).getChildContainer());

		assertNotNull("A frame can be added.", AddChildCommand.execute(framesGroup, null));
		assertEquals(2, ((TileStackElement.Config) configOf(stack)).getFrames().size());
		assertOnlyFile(root, "b-root.view.xml");
	}

	/**
	 * The view of a frame declaration nested in another one is a link below the nested declaration,
	 * which is an entry of the enclosing declaration.
	 */
	public void testNestedFrameIsLink() throws Exception {
		view("c-root.view.xml",
			"<channels><channel name='navPath'/></channels>"
				+ "<tile-stack initial='c-initial.view.xml' path='navPath'>"
				+ "<frame id='outer' route='outer' view='c-outer.view.xml'>"
				+ "<frame id='inner' route='inner' view='c-inner.view.xml'/>"
				+ "</frame>"
				+ "</tile-stack>");
		view("c-initial.view.xml", "<panel title='Initial'/>");
		view("c-outer.view.xml", "<panel title='Outer'/>");
		view("c-inner.view.xml", "<panel title='Inner'/>");

		DesignTreeNode root = build("c-root.view.xml");

		DesignTreeNode inner = linkTo(root, "c-inner.view.xml");
		DesignTreeNode declaration = inner.getParent().getParent();
		assertTrue("The nested frame view is shown below its declaration.",
			configOf(declaration) instanceof FrameRouteConfig);
		assertEquals("inner", ((FrameRouteConfig) configOf(declaration)).getId());
		DesignTreeNode contents = declaration.getParent();
		assertEquals(FrameRouteConfig.CONTENTS, contents.getProperty().getPropertyName());
		assertEquals("outer", ((FrameRouteConfig) configOf(contents.getParent())).getId());
	}

	/**
	 * A dialog opened by an action of a command is a link below that action.
	 */
	public void testOpenDialogInCommandIsLink() throws Exception {
		view("d-root.view.xml",
			"<panel title='Panel'><commands>"
				+ "<generic-command name='open'><open-dialog dialog-view='d-dialog.view.xml'/></generic-command>"
				+ "</commands></panel>");
		view("d-dialog.view.xml", "<panel title='Dialog'/>");

		DesignTreeNode root = build("d-root.view.xml");

		DesignTreeNode dialog = linkTo(root, "d-dialog.view.xml");
		assertTrue("The dialog is shown below the action opening it.",
			configOf(dialog.getParent()) instanceof OpenDialogAction.Config);
	}

	/**
	 * Every reference to a view is a link, a repeated one and one back to the referring view
	 * included.
	 */
	public void testEveryReferenceIsLink() throws Exception {
		view("e-root.view.xml",
			"<stack><view-ref view='e-shared.view.xml'/><view-ref view='e-shared.view.xml'/>"
				+ "<view-ref view='e-root.view.xml'/></stack>");
		view("e-shared.view.xml", "<panel title='Shared'/>");

		DesignTreeNode root = build("e-root.view.xml");

		List<DesignTreeNode> refs = all(root, node -> configOf(node) instanceof ReferenceElement.Config);
		assertEquals(3, refs.size());
		link(refs.get(0).getChildren().get(0), "e-shared.view.xml");
		link(refs.get(1).getChildren().get(0), "e-shared.view.xml");
		link(refs.get(2).getChildren().get(0), "e-root.view.xml");
		assertOnlyFile(root, "e-root.view.xml");
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
	 * The given node as link to the given file.
	 */
	private static ViewLinkDesignTreeNode link(DesignTreeNode node, String name) {
		assertTrue("Not a link: " + node, node instanceof ViewLinkDesignTreeNode);
		ViewLinkDesignTreeNode link = (ViewLinkDesignTreeNode) node;
		assertEquals(path(name), link.getTargetFile());
		return link;
	}

	/**
	 * The single link to the given view file.
	 */
	private static DesignTreeNode linkTo(DesignTreeNode root, String name) {
		return single(root,
			node -> node instanceof ViewLinkDesignTreeNode link && path(name).equals(link.getTargetFile()));
	}

	/**
	 * Checks that the tree shows the content of the given file only.
	 */
	private static void assertOnlyFile(DesignTreeNode root, String name) {
		assertEquals("No other file is expanded into the tree.", List.of(),
			all(root, node -> !path(name).equals(node.getSourceFile())));
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
