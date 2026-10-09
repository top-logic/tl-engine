/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.docs;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.docs.DevDoc;
import com.top_logic.basic.docs.DevDocs;
import com.top_logic.layout.view.docs.DevDocsFunctions;

/**
 * Tests for {@link DevDocsFunctions}: the search filter of the tree, the chapter of an entry and the
 * URL key.
 */
public class TestDevDocsFunctions extends TestCase {

	private Path _dir;

	private DevDoc _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = Files.createTempDirectory("dev-docs");
		Path classes = _dir.resolve("classes");
		write(classes, "views/index.md", "# Views\n");
		write(classes, "views/basics.md", "# Basics\n\nThe fill contract.\n");
		write(classes, "views/deep/nested.md", "# Nested\n\nPinned columns.\n");
		write(classes, "access.md", "# Access\n");
		try (URLClassLoader loader = new URLClassLoader(new URL[] { classes.toUri().toURL() }, null)) {
			_root = DevDocs.load(loader);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (var files = Files.walk(_dir)) {
			files.sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
		}
		super.tearDown();
	}

	/**
	 * The search keeps the articles containing all words and the chapters above them.
	 */
	public void testChildren() {
		DevDoc views = DevDocs.find(_root, "views");
		assertEquals(List.of("access", "views"), names(DevDocsFunctions.children(_root, "")));
		assertEquals(List.of("views"), names(DevDocsFunctions.children(_root, "PINNED columns")));
		assertEquals(List.of("views/deep"), names(DevDocsFunctions.children(views, "pinned")));
		assertEquals(List.of(), names(DevDocsFunctions.children(_root, "pinned nothing")));
		assertEquals(List.of(), names(DevDocsFunctions.children("no entry", "")));
	}

	/**
	 * The chapter of an entry.
	 */
	public void testParent() {
		DevDoc basics = DevDocs.find(_root, "views/basics");
		assertEquals("views", DevDocsFunctions.parent(basics).getName());
		assertNull(DevDocsFunctions.parent(_root));
	}

	/**
	 * The URL key of an entry is a single path segment.
	 */
	public void testRouteKey() {
		DevDoc nested = DevDocs.find(_root, "views/deep/nested");
		assertEquals("views~deep~nested", DevDocsFunctions.routeKey(nested));
		assertNull(DevDocsFunctions.routeKey(null));
	}

	private static List<String> names(List<DevDoc> docs) {
		return docs.stream().map(DevDoc::getName).toList();
	}

	private static void write(Path classes, String path, String content) throws IOException {
		Path file = classes.resolve(DevDocs.DOCS_DIR).resolve(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, content, StandardCharsets.UTF_8);
	}

}
