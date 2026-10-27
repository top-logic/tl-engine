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
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.designer.ConfigDesignTreeNode;
import com.top_logic.layout.view.designer.DesignTreeBuilder;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.SelectViewCommand;
import com.top_logic.layout.view.designer.ViewLinkDesignTreeNode;
import com.top_logic.layout.view.designer.VirtualDesignTreeNode;

/**
 * Tests {@link SelectViewCommand#findOrAddViewRoot(DesignTreeNode, String)} and the selection
 * target of a {@link ViewLinkDesignTreeNode}.
 */
public class TestSelectViewAddsView extends TestCase {

	private File _webapp;

	private File _views;

	private FileManager _fileManagerBefore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_webapp = Files.createTempDirectory("tl-select-view-adds-view").toFile();
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
	 * A view the tree does not show is added to the group of further views, which is created once
	 * and reused for the next view.
	 */
	public void testAddsViewToFurtherViews() throws Exception {
		view("a-root.view.xml", "<panel title='Root'/>");
		view("a-opened.view.xml", "<panel title='Opened'/>");
		view("a-other.view.xml", "<panel title='Other'/>");
		DesignTreeNode root = build("a-root.view.xml");
		int[] notified = { 0 };
		root.addStructureListener(() -> notified[0]++);

		DesignTreeNode opened = SelectViewCommand.findOrAddViewRoot(root, path("a-opened.view.xml"));

		assertNotNull(opened);
		assertEquals(path("a-opened.view.xml"), opened.getSourceFile());
		assertTrue(opened instanceof ConfigDesignTreeNode);
		DesignTreeNode group = opened.getParent();
		assertTrue(group instanceof VirtualDesignTreeNode);
		assertEquals(I18NConstants.DESIGNER_FURTHER_VIEWS, ((VirtualDesignTreeNode) group).getLabelKey());
		assertNull("The group is no configuration property.", group.getProperty());
		assertNull("Nothing can be added to the group.", group.getChildContainer());
		assertSame(root, group.getParent());
		assertEquals("The tree display is told about the added view.", 1, notified[0]);

		DesignTreeNode other = SelectViewCommand.findOrAddViewRoot(root, path("a-other.view.xml"));

		assertSame("The group is reused.", group, other.getParent());
		assertEquals(1, all(root, TestSelectViewAddsView::isFurtherViews).size());
		assertEquals(List.of(opened, other), group.getChildren());
		assertEquals(2, notified[0]);

		assertSame("A view added once is found afterwards.", opened,
			SelectViewCommand.findOrAddViewRoot(root, path("a-opened.view.xml")));
		assertEquals(2, notified[0]);
	}

	/**
	 * A view the tree shows is not added a second time.
	 */
	public void testShownViewIsNotAdded() throws Exception {
		view("b-root.view.xml", "<view-ref view='b-child.view.xml'/>");
		view("b-child.view.xml", "<panel title='Child'/>");
		DesignTreeNode root = build("b-root.view.xml");

		DesignTreeNode child = SelectViewCommand.findOrAddViewRoot(root, path("b-child.view.xml"));

		assertSame(SelectViewCommand.findViewRoot(root, path("b-child.view.xml")), child);
		assertTrue("No group of further views is created.",
			all(root, TestSelectViewAddsView::isFurtherViews).isEmpty());
	}

	/**
	 * A view referenced by the added view that the tree shows already is linked, not expanded a
	 * second time.
	 */
	public void testShownReferenceOfAddedViewIsLink() throws Exception {
		view("c-root.view.xml", "<view-ref view='c-shared.view.xml'/>");
		view("c-shared.view.xml", "<panel title='Shared'/>");
		view("c-opened.view.xml", "<view-ref view='c-shared.view.xml'/>");
		DesignTreeNode root = build("c-root.view.xml");

		DesignTreeNode opened = SelectViewCommand.findOrAddViewRoot(root, path("c-opened.view.xml"));

		List<DesignTreeNode> links = all(opened, node -> node instanceof ViewLinkDesignTreeNode);
		assertEquals(1, links.size());
		ViewLinkDesignTreeNode link = (ViewLinkDesignTreeNode) links.get(0);
		assertEquals(path("c-shared.view.xml"), link.getTargetFile());
		assertSame("Selecting the link selects the expanded view.",
			SelectViewCommand.findViewRoot(root, path("c-shared.view.xml")), link.getSelectionTarget());
	}

	/**
	 * A view that cannot be loaded is not added.
	 */
	public void testMissingViewIsNotAdded() throws Exception {
		view("d-root.view.xml", "<panel title='Root'/>");
		DesignTreeNode root = build("d-root.view.xml");

		assertNull(SelectViewCommand.findOrAddViewRoot(root, path("d-missing.view.xml")));
		assertTrue(all(root, TestSelectViewAddsView::isFurtherViews).isEmpty());
	}

	/**
	 * A link to a view expanded elsewhere selects that view, a link back to the root selects the
	 * root, and every other node selects itself.
	 */
	public void testLinkSelectsExpandedView() throws Exception {
		view("e-root.view.xml",
			"<stack><view-ref view='e-shared.view.xml'/><view-ref view='e-shared.view.xml'/>"
				+ "<view-ref view='e-back.view.xml'/></stack>");
		view("e-shared.view.xml", "<panel title='Shared'/>");
		view("e-back.view.xml", "<view-ref view='e-root.view.xml'/>");
		DesignTreeNode root = build("e-root.view.xml");

		List<DesignTreeNode> links = all(root, node -> node instanceof ViewLinkDesignTreeNode);
		assertEquals(2, links.size());
		assertSame(SelectViewCommand.findViewRoot(root, path("e-shared.view.xml")),
			links.get(0).getSelectionTarget());
		assertSame("The cycle back selects the root.", root, links.get(1).getSelectionTarget());
		assertSame(root, root.getSelectionTarget());
	}

	private static boolean isFurtherViews(DesignTreeNode node) {
		return node instanceof VirtualDesignTreeNode group
			&& I18NConstants.DESIGNER_FURTHER_VIEWS.equals(group.getLabelKey());
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
		return ServiceTestSetup.createSetup(TestSelectViewAddsView.class, TypeIndex.Module.INSTANCE);
	}
}
