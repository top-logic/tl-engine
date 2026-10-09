/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.docs;

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

import com.top_logic.basic.docs.DevDoc;
import com.top_logic.basic.docs.DevDocs;

/**
 * Tests for {@link DevDocs}: finding the documentation on the class path, its chapters, the front
 * matter, and the rendering as HTML.
 */
public class TestDevDocs extends TestCase {

	private static final String LINK_ATTRIBUTE = "data-tl-link";

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
	 * Chapters are formed from the folders of a class output directory and of a jar together; the
	 * first file of a path wins; entries are ordered by their order, then by title.
	 */
	public void testLoad() throws IOException {
		Path classes = _dir.resolve("classes");
		write(classes, "views/index.md",
			"---\ndescription: Read before writing a view.\norder: 10\n---\n\n# Views\n\nIntro.\n");
		write(classes, "views/tables.md", "---\norder: 20\n---\n# Tables\n");
		write(classes, "views/basics.md", "---\norder: 10\n---\n# Basics\n");
		write(classes, "access.md", "# Access\n");

		Path jar = _dir.resolve("tl-dep-1.2.3-SNAPSHOT.jar");
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jar))) {
			entry(zip, "META-INF/", "");
			entry(zip, DevDocs.DOCS_DIR + "/", "");
			entry(zip, DevDocs.DOCS_DIR + "/views/tables.md", "# Shadowed\n");
			entry(zip, DevDocs.DOCS_DIR + "/views/extra.md", "# Extra\n");
			entry(zip, DevDocs.DOCS_DIR + "/views/deep/nested.md", "# Nested\n");
		}

		try (URLClassLoader loader = new URLClassLoader(
			new URL[] { classes.toUri().toURL(), jar.toUri().toURL() }, null)) {
			DevDoc root = DevDocs.load(loader);
			assertEquals("", root.getName());
			assertEquals(List.of("views", "access"), names(root.getChildren()));

			DevDoc views = root.getChildren().get(0);
			assertTrue(views.isChapter());
			assertEquals("Views", views.getTitle());
			assertEquals("Read before writing a view.", views.getDescription());
			assertEquals("# Views\n\nIntro.", views.getText());
			assertEquals(List.of("views/basics", "views/tables", "views/deep", "views/extra"),
				names(views.getChildren()));
			assertEquals("Tables", views.getChildren().get(1).getTitle());

			DevDoc deep = views.getChildren().get(2);
			assertTrue(deep.isChapter());
			assertEquals("deep", deep.getTitle());
			assertNull("A chapter without index has no text.", deep.getText());
			assertEquals(List.of("views/deep/nested"), names(deep.getChildren()));

			assertSame(deep.getChildren().get(0), DevDocs.find(root, "doc:views/deep/nested#section"));

			assertEquals("", root.getNumber());
			assertEquals("1", views.getNumber());
			assertEquals("1.2", views.getChildren().get(1).getNumber());
			assertEquals("1.3.1", deep.getChildren().get(0).getNumber());
			assertEquals("2", root.getChildren().get(1).getNumber());

			assertEquals("META-INF/tl-docs/views/index.md", views.getSource());
			assertEquals("tl-dep: META-INF/tl-docs/views/extra.md", views.getChildren().get(3).getSource());
			assertNull("A chapter without index has no file.", deep.getSource());
			assertSame(views, DevDocs.find(root, "views"));
			assertSame(views, views.getChildren().get(0).getParent());
			assertNull(DevDocs.find(root, "views/missing"));

		}
	}

	/**
	 * The title is the text of the heading, without its Markdown syntax.
	 */
	public void testTitleText() throws IOException {
		Path classes = _dir.resolve("classes");
		write(classes, "views.md", "# React view layer (`com.top_logic.layout.view` and the *table*)\n");
		write(classes, "plain.md", "Just text.\n");
		try (URLClassLoader loader = new URLClassLoader(new URL[] { classes.toUri().toURL() }, null)) {
			DevDoc root = DevDocs.load(loader);
			assertEquals("React view layer (com.top_logic.layout.view and the table)",
				DevDocs.find(root, "views").getTitle());
			assertEquals("An article without a heading is titled by its name.", "plain",
				DevDocs.find(root, "plain").getTitle());
		}
	}

	/**
	 * Tables are rendered, headings carry anchors, source HTML is escaped, a link to an article
	 * carries its target, and links leading nowhere lose their target.
	 */
	public void testToHtml() throws IOException {
		Path classes = _dir.resolve("classes");
		write(classes, "views/basics.md", "# Basics\n");
		DevDoc root;
		try (URLClassLoader loader = new URLClassLoader(new URL[] { classes.toUri().toURL() }, null)) {
			root = DevDocs.load(loader);
		}
		String html = DevDocs.toHtml("""
			## Spacing model

			| A | B |
			|---|---|
			| 1 | 2 |

			A <panel> element, see [spacing](#spacing-model), [the guide](https://top-logic.com/),
			[basics](doc:views/basics#fill), [all basics](doc:views/basics), [missing](doc:views/missing)
			and [the other article](../../docs/faq/other.md).
			""", root, LINK_ATTRIBUTE);
		assertTrue(html, html.contains("<h2 id=\"spacing-model\">Spacing model</h2>"));
		assertTrue(html, html.contains("<table>"));
		assertTrue(html, html.contains("<td>2</td>"));
		assertTrue(html, html.contains("&lt;panel&gt;"));
		assertTrue(html, html.contains("<a href=\"#spacing-model\">spacing</a>"));
		assertTrue(html, html.contains(
			"<a href=\"https://top-logic.com/\" target=\"_blank\" rel=\"noopener noreferrer\">the guide</a>"));
		assertTrue(html, html.contains("<a href=\"#fill\" data-tl-link=\"views/basics#fill\">basics</a>"));
		assertTrue(html, html.contains("<a href=\"#\" data-tl-link=\"views/basics\">all basics</a>"));
		assertTrue(html, html.contains("<a>missing</a>"));
		assertTrue(html, html.contains("<a>the other article</a>"));
	}

	/**
	 * An article with several sections gets a table of contents between its introduction and its
	 * first section, its subsections nested below their section.
	 */
	public void testContents() throws IOException {
		String html = DevDocs.toHtml("""
			# Title

			Intro.

			## First `one`

			### Detail

			## Second
			""", emptyRoot(), LINK_ATTRIBUTE);
		assertTrue(html, html.contains("<p>Intro.</p>\n<ul><li><a href=\"#first-one\">First <code>one</code></a>"
			+ "<ul><li><a href=\"#detail\">Detail</a></li></ul></li>"
			+ "<li><a href=\"#second\">Second</a></li></ul>\n<h2 id=\"first-one\">"));

		String single = DevDocs.toHtml("# Title\n\n## Only\n", emptyRoot(), LINK_ATTRIBUTE);
		assertFalse("A single section needs no contents.", single.contains("<ul>"));
	}


	/**
	 * A chapter lists its entries below its text; a chapter without text is introduced by its title.
	 */
	public void testChapterEntries() throws IOException {
		Path classes = _dir.resolve("classes");
		write(classes, "views/index.md", "---\ndescription: The views.\n---\n# Views\n\nIntro.\n");
		write(classes, "views/basics.md", "---\ndescription: Read <first>.\norder: 10\n---\n# Basics\n");
		write(classes, "views/tables.md", "---\norder: 20\n---\n# Tables\n");
		write(classes, "loose/one.md", "# One\n");
		try (URLClassLoader loader = new URLClassLoader(new URL[] { classes.toUri().toURL() }, null)) {
			DevDoc root = DevDocs.load(loader);
			String views = DevDocs.toHtml(DevDocs.find(root, "views"), root, LINK_ATTRIBUTE);
			assertTrue(views, views.startsWith("<h1 id=\"views\">Views</h1>\n<p>Intro.</p>\n<ul>"));
			assertTrue(views, views.contains(
				"<li><a href=\"#\" data-tl-link=\"views/basics\">2.1 Basics</a> - Read &lt;first&gt;.</li>"
					+ "<li><a href=\"#\" data-tl-link=\"views/tables\">2.2 Tables</a></li></ul>"));

			String loose = DevDocs.toHtml(DevDocs.find(root, "loose"), root, LINK_ATTRIBUTE);
			assertTrue(loose, loose.startsWith("<h1>loose</h1>\n<ul><li>"));

			String article = DevDocs.toHtml(DevDocs.find(root, "views/tables"), root, LINK_ATTRIBUTE);
			assertFalse("An article lists no entries.", article.contains("<ul>"));
		}
	}

	private static DevDoc emptyRoot() throws IOException {
		try (URLClassLoader loader = new URLClassLoader(new URL[0], null)) {
			return DevDocs.load(loader);
		}
	}

	private static List<String> names(List<DevDoc> docs) {
		return docs.stream().map(DevDoc::getName).toList();
	}

	private static void write(Path classes, String path, String content) throws IOException {
		Path file = classes.resolve(DevDocs.DOCS_DIR).resolve(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, content, StandardCharsets.UTF_8);
	}

	private static void entry(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

}
