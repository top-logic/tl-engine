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

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.designer.DesignSession;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.DesignTreeNodes;
import com.top_logic.layout.view.element.PanelElement;

/**
 * Tests the navigation and the per-file trees of a {@link DesignSession}.
 */
public class TestDesignSession extends TestCase {

	private File _webapp;

	private File _views;

	private FileManager _fileManagerBefore;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_webapp = Files.createTempDirectory("tl-design-session").toFile();
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
	 * Opening a linked file appends it to the path, and shows its tree.
	 */
	public void testOpenAppendsToPath() throws Exception {
		view("s-a-root.view.xml", ref("s-a-1.view.xml"));
		view("s-a-1.view.xml", ref("s-a-2.view.xml"));
		view("s-a-2.view.xml", "<panel title='2'/>");
		DesignSession session = new DesignSession(path("s-a-root.view.xml"));
		int[] notified = { 0 };
		session.addListener(() -> notified[0]++);

		session.open(path("s-a-1.view.xml"));
		session.open(path("s-a-2.view.xml"));

		assertEquals(paths("s-a-root.view.xml", "s-a-1.view.xml", "s-a-2.view.xml"), session.getPath());
		assertEquals(path("s-a-2.view.xml"), session.getCurrentFile());
		assertEquals(path("s-a-2.view.xml"), session.getCurrentTree().getSourceFile());
		assertEquals(2, notified[0]);

		session.open(path("s-a-2.view.xml"));
		assertEquals("Opening the file shown changes nothing.", 2, notified[0]);
	}

	/**
	 * Opening a file already on the path goes back to it, so that a reference cycle does not grow
	 * the path.
	 */
	public void testOpenFileOnPathTruncates() throws Exception {
		view("s-b-root.view.xml", ref("s-b-1.view.xml"));
		view("s-b-1.view.xml", ref("s-b-2.view.xml"));
		view("s-b-2.view.xml", ref("s-b-1.view.xml"));
		DesignSession session = new DesignSession(path("s-b-root.view.xml"));
		session.open(path("s-b-1.view.xml"));
		session.open(path("s-b-2.view.xml"));

		session.open(path("s-b-1.view.xml"));

		assertEquals(paths("s-b-root.view.xml", "s-b-1.view.xml"), session.getPath());
	}

	/**
	 * A prefix of the path written to the bound channel - a breadcrumb going back - shows the last
	 * file of that prefix, and the channel follows the session.
	 */
	public void testBackThroughPathChannel() throws Exception {
		view("s-c-root.view.xml", ref("s-c-1.view.xml"));
		view("s-c-1.view.xml", ref("s-c-2.view.xml"));
		view("s-c-2.view.xml", "<panel title='2'/>");
		DesignSession session = new DesignSession(path("s-c-root.view.xml"));
		DefaultViewChannel channel = new DefaultViewChannel("path");
		Registration binding = session.bindPath(channel);
		assertEquals(List.of(new ViewRef("s-c-root.view.xml")), channel.get());

		session.open(path("s-c-1.view.xml"));
		session.open(path("s-c-2.view.xml"));
		assertEquals(List.of(new ViewRef("s-c-root.view.xml"), new ViewRef("s-c-1.view.xml"),
			new ViewRef("s-c-2.view.xml")), channel.get());

		channel.set(List.of(new ViewRef("s-c-root.view.xml"), new ViewRef("s-c-1.view.xml")));

		assertEquals(paths("s-c-root.view.xml", "s-c-1.view.xml"), session.getPath());
		assertEquals(path("s-c-1.view.xml"), session.getCurrentFile());

		binding.dispose();
		session.open(path("s-c-2.view.xml"));
		assertEquals("A disposed binding is not updated.", 2, ((List<?>) channel.get()).size());
	}

	/**
	 * The tree of a file is built once and kept while navigating, together with its modifications.
	 */
	public void testTreeIsKeptWhileNavigating() throws Exception {
		view("s-d-root.view.xml", ref("s-d-1.view.xml"));
		view("s-d-1.view.xml", "<panel title='1'/>");
		DesignSession session = new DesignSession(path("s-d-root.view.xml"));
		DesignTreeNode root = session.getCurrentTree();

		session.open(path("s-d-1.view.xml"));
		DesignTreeNode tree = session.getCurrentTree();
		DesignTreeNode panel = tree.getChildren().get(0);
		panel.markDirty();
		session.openPath(paths("s-d-root.view.xml"));
		assertSame(root, session.getCurrentTree());
		session.open(path("s-d-1.view.xml"));

		assertSame(tree, session.getCurrentTree());
		assertSame(panel, session.getCurrentTree().getChildren().get(0));
		assertTrue("The modification survives navigating.", panel.isDirty());
	}

	/**
	 * Saving covers the modified files that are not shown, and only modified ones.
	 */
	public void testDirtyViewsOfFilesNotShown() throws Exception {
		view("s-e-root.view.xml", ref("s-e-1.view.xml"));
		view("s-e-1.view.xml", "<panel title='1'/>");
		DesignSession session = new DesignSession(path("s-e-root.view.xml"));
		session.open(path("s-e-1.view.xml"));
		session.getCurrentTree().getChildren().get(0).markDirty();
		session.openPath(paths("s-e-root.view.xml"));

		assertEquals(List.of(path("s-e-1.view.xml")), new ArrayList<>(session.getDirtyViews().keySet()));
		assertSame(DesignTreeNodes.configOf(session.tree(path("s-e-1.view.xml"))),
			session.getDirtyViews().get(path("s-e-1.view.xml")));

		session.markSaved(session.getDirtyViews().keySet());

		assertTrue(session.getDirtyViews().isEmpty());
		assertFalse(session.tree(path("s-e-1.view.xml")).getChildren().get(0).isDirty());
	}

	/**
	 * A picked file is opened on the shortest path of references from the root file.
	 */
	public void testSelectOpensShortestPath() throws Exception {
		view("s-f-root.view.xml",
			"<stack>" + ref("s-f-long1.view.xml") + ref("s-f-short.view.xml") + "</stack>");
		view("s-f-long1.view.xml", ref("s-f-long2.view.xml"));
		view("s-f-long2.view.xml", ref("s-f-target.view.xml"));
		view("s-f-short.view.xml", ref("s-f-target.view.xml"));
		view("s-f-target.view.xml", ref("s-f-root.view.xml"));
		DesignSession session = new DesignSession(path("s-f-root.view.xml"));

		session.select(path("s-f-target.view.xml"));

		assertEquals(paths("s-f-root.view.xml", "s-f-short.view.xml", "s-f-target.view.xml"),
			session.getPath());

		session.select(path("s-f-root.view.xml"));
		assertEquals(paths("s-f-root.view.xml"), session.getPath());
	}

	/**
	 * A picked file no reference leads to is shown directly behind the root file.
	 */
	public void testSelectUnreachableFile() throws Exception {
		view("s-g-root.view.xml", ref("s-g-1.view.xml"));
		view("s-g-1.view.xml", "<panel title='1'/>");
		view("s-g-computed.view.xml", "<panel title='Computed'/>");
		DesignSession session = new DesignSession(path("s-g-root.view.xml"));
		session.open(path("s-g-1.view.xml"));

		session.select(path("s-g-computed.view.xml"));

		assertEquals(paths("s-g-root.view.xml", "s-g-computed.view.xml"), session.getPath());
	}

	/**
	 * Reverting discards the trees of all files and shows the root file.
	 */
	public void testRevert() throws Exception {
		view("s-h-root.view.xml", ref("s-h-1.view.xml"));
		view("s-h-1.view.xml", "<panel title='1'/>");
		DesignSession session = new DesignSession(path("s-h-root.view.xml"));
		session.open(path("s-h-1.view.xml"));
		DesignTreeNode tree = session.getCurrentTree();
		tree.getChildren().get(0).markDirty();
		int[] notified = { 0 };
		session.addListener(() -> notified[0]++);

		session.revert();

		assertEquals(paths("s-h-root.view.xml"), session.getPath());
		assertEquals(1, notified[0]);
		assertNotSame(tree, session.tree(path("s-h-1.view.xml")));
		assertTrue(session.getDirtyViews().isEmpty());
	}

	/**
	 * Reverting discards a property value changed in place in the configuration of a tree: the tree
	 * built anew shows the value of the file, on a configuration read anew.
	 */
	public void testRevertDiscardsChangedConfiguration() throws Exception {
		view("s-i-root.view.xml", "<panel title='Original'/>");
		DesignSession session = new DesignSession(path("s-i-root.view.xml"));
		PanelElement.Config panel = panel(session.getCurrentTree());
		Object original = panel.getTitle();
		PropertyDescriptor title = panel.descriptor().getProperty(PanelElement.Config.TITLE);

		// What the form does when a property is edited.
		panel.update(title, ResKey.text("Changed"));
		assertEquals(ResKey.text("Changed"), panel(session.getCurrentTree()).getTitle());

		session.revert();

		PanelElement.Config reverted = panel(session.getCurrentTree());
		assertNotSame("The configuration is read anew.", panel, reverted);
		assertEquals("The value of the file is shown.", original, reverted.getTitle());
		assertEquals(original,
			((PanelElement.Config) ViewLoader.getOrLoadConfig(path("s-i-root.view.xml")).getContent()).getTitle());
	}

	private static PanelElement.Config panel(DesignTreeNode node) {
		if (DesignTreeNodes.configOf(node) instanceof PanelElement.Config panel) {
			return panel;
		}
		for (DesignTreeNode child : node.getChildren()) {
			PanelElement.Config result = panel(child);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private static String ref(String name) {
		return "<view-ref view='" + name + "'/>";
	}

	private void view(String name, String content) throws IOException {
		File file = new File(_views, name);
		Files.writeString(file.toPath(),
			"<?xml version='1.0' encoding='utf-8'?><view>" + content + "</view>", StandardCharsets.UTF_8);
	}

	private static String path(String name) {
		return ViewLoader.fullPath(name);
	}

	private static List<String> paths(String... names) {
		List<String> result = new ArrayList<>();
		for (String name : names) {
			result.add(path(name));
		}
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, used while parsing polymorphic properties.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDesignSession.class, TypeIndex.Module.INSTANCE);
	}
}
