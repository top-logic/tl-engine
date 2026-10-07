/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.docs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.Link;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.top_logic.layout.react.control.html.ReactHtmlControl;

/**
 * The developer documentation shipped with the modules of the application.
 *
 * <p>
 * An article is a Markdown file below {@value #DOCS_DIR} in the jar (or class output directory) of
 * a module; in the module sources it lives below {@code src/main/java/META-INF/tl-docs/}. Its path
 * relative to that folder without {@code .md} is the article name, e.g. {@code view-layer/tables}
 * for {@code META-INF/tl-docs/view-layer/tables.md}. A folder is a chapter grouping the articles in
 * it; the folders of the same path in several modules form one chapter, so a module adds articles to
 * a chapter of another one. A chapter takes its title, description and introduction from an
 * {@value #CHAPTER_FILE} in its folder.
 * </p>
 *
 * <p>
 * A file starts with a front matter block whose {@code description} says when to read the article
 * and whose optional {@code order} places it among the other entries of its chapter (entries
 * without an order follow those with one, all others are sorted by title):
 * </p>
 *
 * <pre>
 * ---
 * description: Read before writing or changing a .view.xml ...
 * order: 10
 * ---
 *
 * # React view layer
 * </pre>
 *
 * <p>
 * The first top-level heading is the title. A link to another article names it with the
 * {@value #LINK_SCHEME} scheme, followed by the article name and optionally a section anchor:
 * {@code [spacing](doc:view-layer/basics#spacing-model)}. When several modules ship a file of the
 * same path, the first one on the class path wins.
 * </p>
 */
public final class DevDocs {

	/** Class path directory holding the documentation. */
	public static final String DOCS_DIR = "META-INF/tl-docs";

	/** Scheme of a link to an article, followed by the article name. */
	public static final String LINK_SCHEME = "doc:";

	/** File of a chapter folder describing the chapter. */
	public static final String CHAPTER_FILE = "index.md";

	private static final String DOC_SUFFIX = ".md";

	private static final String FRONT_MATTER_DELIMITER = "---";

	private static final String DESCRIPTION_KEY = "description";

	private static final String ORDER_KEY = "order";

	private static final String TITLE_PREFIX = "# ";

	private static final String HREF = "href";

	/** A rendered section or subsection heading: level, anchor and content. */
	private static final Pattern SECTION_HEADING = Pattern.compile("<h([23]) id=\"([^\"]*)\">(.*?)</h\\1>");

	private static final List<Extension> EXTENSIONS =
		List.of(TablesExtension.create(), HeadingAnchorExtension.create());

	private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

	private DevDocs() {
		// utility
	}

	/**
	 * The documentation found by the class loader of the application.
	 *
	 * @return The root chapter, whose name is empty.
	 */
	public static DevDoc load() {
		return load(DevDocs.class.getClassLoader());
	}

	/**
	 * The documentation found by the given class loader.
	 *
	 * @return The root chapter, whose name is empty.
	 */
	public static DevDoc load(ClassLoader loader) {
		Map<String, String> files = new LinkedHashMap<>();
		try {
			Enumeration<URL> dirs = loader.getResources(DOCS_DIR);
			while (dirs.hasMoreElements()) {
				for (Map.Entry<String, String> file : files(dirs.nextElement()).entrySet()) {
					files.putIfAbsent(file.getKey(), file.getValue());
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		DevDoc root = new DevDoc("", true);
		Map<String, DevDoc> chapters = new HashMap<>();
		chapters.put("", root);
		for (Map.Entry<String, String> file : files.entrySet()) {
			String path = file.getKey();
			int separator = path.lastIndexOf('/');
			String folder = separator < 0 ? "" : path.substring(0, separator);
			String fileName = path.substring(separator + 1);
			DevDoc chapter = chapter(chapters, folder);
			if (fileName.equals(CHAPTER_FILE)) {
				parse(chapter, file.getValue());
			} else {
				DevDoc article = new DevDoc(path.substring(0, path.length() - DOC_SUFFIX.length()), false);
				parse(article, file.getValue());
				chapter.addChild(article);
			}
		}
		root.sortChildren();
		return root;
	}

	private static DevDoc chapter(Map<String, DevDoc> chapters, String folder) {
		DevDoc chapter = chapters.get(folder);
		if (chapter == null) {
			int separator = folder.lastIndexOf('/');
			DevDoc parent = chapter(chapters, separator < 0 ? "" : folder.substring(0, separator));
			chapter = new DevDoc(folder, true);
			parent.addChild(chapter);
			chapters.put(folder, chapter);
		}
		return chapter;
	}

	/**
	 * The node of the given name below the given root.
	 *
	 * @param name
	 *        An article or chapter name, optionally with the {@value #LINK_SCHEME} scheme in front
	 *        and a section anchor behind ({@code doc:view-layer/basics#spacing-model}).
	 * @return The node, {@code null} if there is none of that name.
	 */
	public static DevDoc find(DevDoc root, String name) {
		String path = name;
		if (path.startsWith(LINK_SCHEME)) {
			path = path.substring(LINK_SCHEME.length());
		}
		int anchor = path.indexOf('#');
		if (anchor >= 0) {
			path = path.substring(0, anchor);
		}
		return findPath(root, path);
	}

	private static DevDoc findPath(DevDoc node, String path) {
		if (node.getName().equals(path)) {
			return node;
		}
		for (DevDoc child : node.getChildren()) {
			if (path.equals(child.getName()) || (child.isChapter() && path.startsWith(child.getName() + "/"))) {
				return findPath(child, path);
			}
		}
		return null;
	}

	/**
	 * The article files below the given directory, by path relative to it.
	 */
	private static Map<String, String> files(URL dir) throws IOException {
		Map<String, String> result = new TreeMap<>();
		if ("file".equals(dir.getProtocol())) {
			Path base;
			try {
				base = Path.of(dir.toURI());
			} catch (URISyntaxException ex) {
				throw new IOException("Invalid directory URL: " + dir, ex);
			}
			try (Stream<Path> files = Files.walk(base)) {
				for (Path file : (Iterable<Path>) files::iterator) {
					if (file.getFileName().toString().endsWith(DOC_SUFFIX) && Files.isRegularFile(file)) {
						String path = base.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
						result.put(path, Files.readString(file, StandardCharsets.UTF_8));
					}
				}
			}
		} else {
			URLConnection connection = dir.openConnection();
			if (connection instanceof JarURLConnection jarConnection) {
				jarConnection.setUseCaches(false);
				String prefix = DOCS_DIR + "/";
				try (JarFile jar = jarConnection.getJarFile()) {
					for (Enumeration<JarEntry> it = jar.entries(); it.hasMoreElements();) {
						JarEntry entry = it.nextElement();
						String entryName = entry.getName();
						if (!entry.isDirectory() && entryName.startsWith(prefix) && entryName.endsWith(DOC_SUFFIX)) {
							try (InputStream in = jar.getInputStream(entry)) {
								result.put(entryName.substring(prefix.length()),
									new String(in.readAllBytes(), StandardCharsets.UTF_8));
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Fills the given node from the content of its file.
	 */
	private static void parse(DevDoc node, String content) {
		String description = "";
		Integer order = null;
		String body = content;
		List<String> lines = content.lines().toList();
		if (!lines.isEmpty() && lines.get(0).strip().equals(FRONT_MATTER_DELIMITER)) {
			for (int n = 1; n < lines.size(); n++) {
				String line = lines.get(n);
				if (line.strip().equals(FRONT_MATTER_DELIMITER)) {
					body = String.join("\n", lines.subList(n + 1, lines.size())).stripLeading();
					break;
				}
				int colon = line.indexOf(':');
				if (colon > 0) {
					String key = line.substring(0, colon).strip();
					String value = line.substring(colon + 1).strip();
					if (key.equals(DESCRIPTION_KEY)) {
						description = value;
					} else if (key.equals(ORDER_KEY)) {
						try {
							order = Integer.valueOf(value);
						} catch (NumberFormatException ex) {
							// Not a number: the entry is ordered by its title.
						}
					}
				}
			}
		}
		String title = null;
		for (String line : body.lines().toList()) {
			if (line.startsWith(TITLE_PREFIX)) {
				title = plainText(line.substring(TITLE_PREFIX.length()));
				break;
			}
		}
		node.setContent(title, description, order, body);
	}

	/**
	 * The text of the given inline Markdown source without its syntax.
	 */
	private static String plainText(String markdown) {
		StringBuilder result = new StringBuilder();
		PARSER.parse(markdown).accept(new AbstractVisitor() {
			@Override
			public void visit(Text text) {
				result.append(text.getLiteral());
			}

			@Override
			public void visit(Code code) {
				result.append(code.getLiteral());
			}
		});
		return result.toString().strip();
	}

	/**
	 * Renders the Markdown source of an article as HTML.
	 *
	 * <p>
	 * Tables are rendered as tables. HTML written in the source is displayed as text. Each heading
	 * carries an anchor, so a link to a section of the article ({@code #section}) works. A link to
	 * an article of the given documentation ({@code doc:view-layer/basics#spacing-model}) carries
	 * the article name and section in its {@link ReactHtmlControl#LINK_ATTRIBUTE} and the section in its
	 * target. A link to an article the documentation does not contain and a relative link to a file
	 * of the source tree are displayed as text, since they lead nowhere in the application. An
	 * article with several sections gets a table of contents between its introduction and its first
	 * section.
	 * </p>
	 *
	 * @param markdown
	 *        The Markdown source.
	 * @param root
	 *        The documentation links to articles are resolved against.
	 */
	public static String toHtml(String markdown, DevDoc root) {
		HtmlRenderer renderer = HtmlRenderer.builder()
			.extensions(EXTENSIONS)
			.escapeHtml(true)
			.attributeProviderFactory(context -> (node, tagName, attributes) -> {
				if (node instanceof Link) {
					String href = attributes.get(HREF);
					if (href != null && href.startsWith(LINK_SCHEME)) {
						attributes.remove(HREF);
						if (find(root, href) != null) {
							String target = href.substring(LINK_SCHEME.length());
							int anchor = target.indexOf('#');
							attributes.put(HREF, anchor < 0 ? "#" : target.substring(anchor));
							attributes.put(ReactHtmlControl.LINK_ATTRIBUTE, target);
						}
					} else if (!isResolvable(href)) {
						attributes.remove(HREF);
					}
				}
			})
			.build();
		return withContents(renderer.render(PARSER.parse(markdown)));
	}

	/**
	 * Inserts a table of contents into the given rendered article, between its introduction and
	 * its first section: the sections, the subsections of each nested below it, each linking to its
	 * heading. An article with fewer than two sections gets none.
	 */
	private static String withContents(String html) {
		Matcher headings = SECTION_HEADING.matcher(html);
		StringBuilder contents = new StringBuilder();
		int firstSection = -1;
		int sections = 0;
		boolean inSubsections = false;
		while (headings.find()) {
			boolean section = headings.group(1).equals("2");
			if (section) {
				if (inSubsections) {
					contents.append("</ul>");
					inSubsections = false;
				}
				if (sections > 0) {
					contents.append("</li>");
				} else {
					firstSection = headings.start();
				}
				sections++;
			} else if (sections == 0) {
				// A subsection before the first section belongs to no entry.
				continue;
			} else if (!inSubsections) {
				contents.append("<ul>");
				inSubsections = true;
			}
			contents.append("<li><a href=\"#").append(headings.group(2)).append("\">")
				.append(headings.group(3)).append("</a>");
			if (!section) {
				contents.append("</li>");
			}
		}
		if (sections < 2) {
			return html;
		}
		if (inSubsections) {
			contents.append("</ul>");
		}
		contents.append("</li>");
		return html.substring(0, firstSection)
			+ "<ul>" + contents + "</ul>\n"
			+ html.substring(firstSection);
	}

	private static boolean isResolvable(String href) {
		return href != null && (href.startsWith("#") || href.startsWith("http://") || href.startsWith("https://")
			|| href.startsWith("mailto:"));
	}

}
