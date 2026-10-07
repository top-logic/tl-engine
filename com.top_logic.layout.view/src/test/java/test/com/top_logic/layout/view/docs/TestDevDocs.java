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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import com.top_logic.layout.view.docs.DevDocs;

/**
 * Tests for {@link DevDocs}: finding the articles on the class path, their front matter, and their
 * rendering as HTML.
 */
public class TestDevDocs extends TestCase {

	private Path _dir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = Files.createTempDirectory("dev-docs");
	}

	@Override
	protected void tearDown() throws Exception {
		try (var files = Files.walk(_dir)) {
			files.sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
		}
		super.tearDown();
	}

	/**
	 * Articles are found in a class output directory and in a jar; the first of a name wins.
	 */
	public void testLoad() throws IOException {
		Path classes = _dir.resolve("classes");
		write(classes.resolve(DevDocs.DOCS_DIR + "/views.md"),
			"---\ndescription: Read before writing a view.\n---\n\n# Views\n\nText.\n");

		Path jar = _dir.resolve("dep.jar");
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jar))) {
			entry(zip, "META-INF/", "");
			entry(zip, DevDocs.DOCS_DIR + "/", "");
			entry(zip, DevDocs.DOCS_DIR + "/views.md", "# Shadowed\n");
			entry(zip, DevDocs.DOCS_DIR + "/access.md", "# Access\n");
			entry(zip, DevDocs.DOCS_DIR + "/nested/ignored.md", "# Ignored\n");
		}

		try (URLClassLoader loader = new URLClassLoader(
			new URL[] { classes.toUri().toURL(), jar.toUri().toURL() }, null)) {
			List<DevDocs.Doc> docs = DevDocs.load(loader);
			assertEquals(List.of("views", "access"), docs.stream().map(DevDocs.Doc::name).toList());

			DevDocs.Doc views = docs.get(0);
			assertEquals("Views", views.title());
			assertEquals("Read before writing a view.", views.description());
			assertEquals("# Views\n\nText.", views.text());

			DevDocs.Doc access = docs.get(1);
			assertEquals("Access", access.title());
			assertEquals("", access.description());
		}
	}

	/**
	 * An article without a heading is titled by its name.
	 */
	public void testTitleFallback() {
		assertEquals("plain", DevDocs.parse("plain", "Just text.\n").title());
	}

	/**
	 * The title is the text of the heading, without its Markdown syntax.
	 */
	public void testTitleText() {
		assertEquals("FAQ: React view layer (com.top_logic.layout.view and the table)",
			DevDocs.parse("views", "# FAQ: React view layer (`com.top_logic.layout.view` and the *table*)\n").title());
	}

	/**
	 * Tables are rendered, headings carry anchors, source HTML is escaped, and links into the source
	 * tree lose their target.
	 */
	public void testToHtml() {
		String html = DevDocs.toHtml("""
			## Spacing model

			| A | B |
			|---|---|
			| 1 | 2 |

			A <panel> element, see [spacing](#spacing-model), [the guide](https://top-logic.com/) and
			[the other article](../../docs/faq/other.md).
			""");
		assertTrue(html, html.contains("<h2 id=\"spacing-model\">Spacing model</h2>"));
		assertTrue(html, html.contains("<table>"));
		assertTrue(html, html.contains("<td>2</td>"));
		assertTrue(html, html.contains("&lt;panel&gt;"));
		assertTrue(html, html.contains("<a href=\"#spacing-model\">spacing</a>"));
		assertTrue(html, html.contains("<a href=\"https://top-logic.com/\">the guide</a>"));
		assertTrue(html, html.contains("<a>the other article</a>"));
	}

	private static void write(Path file, String content) throws IOException {
		Files.createDirectories(file.getParent());
		Files.writeString(file, content, StandardCharsets.UTF_8);
	}

	private static void entry(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

}
