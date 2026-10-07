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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

/**
 * The developer articles shipped with the modules of the application.
 *
 * <p>
 * An article is a Markdown file {@value #DOCS_DIR}{@code /<name>.md} in the jar (or class output
 * directory) of a module; in the module sources it lives in {@code src/main/java/META-INF/tl-docs/}.
 * The file name without {@code .md} is the article name. The file starts with a front matter block
 * whose {@code description} says when to read the article:
 * </p>
 *
 * <pre>
 * ---
 * description: Read before writing or changing a .view.xml ...
 * ---
 *
 * # FAQ: React view layer
 * </pre>
 *
 * <p>
 * The first top-level heading is the title of the article. When several modules ship an article of
 * the same name, the first one on the class path wins.
 * </p>
 */
public final class DevDocs {

	/** Class path directory holding the articles. */
	public static final String DOCS_DIR = "META-INF/tl-docs";

	private static final String DOC_SUFFIX = ".md";

	private static final String FRONT_MATTER_DELIMITER = "---";

	private static final String DESCRIPTION_KEY = "description";

	private static final String TITLE_PREFIX = "# ";

	private static final String HREF = "href";

	private static final List<Extension> EXTENSIONS =
		List.of(TablesExtension.create(), HeadingAnchorExtension.create());

	private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

	private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
		.extensions(EXTENSIONS)
		.escapeHtml(true)
		.attributeProviderFactory(context -> (node, tagName, attributes) -> {
			if (node instanceof Link && !isResolvable(attributes.get(HREF))) {
				// A link to a file of the source tree leads nowhere in the application.
				attributes.remove(HREF);
			}
		})
		.build();

	/**
	 * A developer article.
	 *
	 * @param name
	 *        The article name (file name without {@code .md}).
	 * @param title
	 *        The text of the first top-level heading without its Markdown syntax, the name if there
	 *        is none.
	 * @param description
	 *        When to read the article, from its front matter.
	 * @param text
	 *        The Markdown body without front matter.
	 */
	public record Doc(String name, String title, String description, String text) {
		// record
	}

	private DevDocs() {
		// utility
	}

	/**
	 * All articles found by the class loader of the application, in class path order.
	 */
	public static List<Doc> load() {
		return load(DevDocs.class.getClassLoader());
	}

	/**
	 * All articles found by the given class loader, in class path order.
	 */
	public static List<Doc> load(ClassLoader loader) {
		Map<String, Doc> docs = new LinkedHashMap<>();
		try {
			Enumeration<URL> dirs = loader.getResources(DOCS_DIR);
			while (dirs.hasMoreElements()) {
				URL dir = dirs.nextElement();
				for (Map.Entry<String, String> file : files(dir).entrySet()) {
					String name = file.getKey().substring(0, file.getKey().length() - DOC_SUFFIX.length());
					docs.putIfAbsent(name, parse(name, file.getValue()));
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return new ArrayList<>(docs.values());
	}

	/**
	 * The article with the given name, or {@code null}.
	 */
	public static Doc get(String name) {
		for (Doc doc : load()) {
			if (doc.name().equals(name)) {
				return doc;
			}
		}
		return null;
	}

	/**
	 * The article files in the given directory, by file name, sorted by name.
	 */
	private static Map<String, String> files(URL dir) throws IOException {
		Map<String, String> result = new TreeMap<>();
		if ("file".equals(dir.getProtocol())) {
			Path path;
			try {
				path = Path.of(dir.toURI());
			} catch (URISyntaxException ex) {
				throw new IOException("Invalid directory URL: " + dir, ex);
			}
			try (Stream<Path> files = Files.list(path)) {
				for (Path file : (Iterable<Path>) files::iterator) {
					String fileName = file.getFileName().toString();
					if (fileName.endsWith(DOC_SUFFIX) && Files.isRegularFile(file)) {
						result.put(fileName, Files.readString(file, StandardCharsets.UTF_8));
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
						if (!entry.isDirectory() && entryName.startsWith(prefix) && entryName.endsWith(DOC_SUFFIX)
							&& entryName.indexOf('/', prefix.length()) < 0) {
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
	 * Parses the content of an article file.
	 */
	public static Doc parse(String name, String content) {
		String description = "";
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
				if (colon > 0 && line.substring(0, colon).strip().equals(DESCRIPTION_KEY)) {
					description = line.substring(colon + 1).strip();
				}
			}
		}
		String title = name;
		for (String line : body.lines().toList()) {
			if (line.startsWith(TITLE_PREFIX)) {
				// The heading text without its Markdown syntax, e.g. without the backticks of code.
				title = plainText(line.substring(TITLE_PREFIX.length()));
				break;
			}
		}
		return new Doc(name, title, description, body);
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
	 * carries an anchor, so a link to a section of the article ({@code #section}) works; a relative
	 * link to a file of the source tree is displayed as text, since it leads nowhere in the
	 * application.
	 * </p>
	 */
	public static String toHtml(String markdown) {
		return RENDERER.render(PARSER.parse(markdown));
	}

	private static boolean isResolvable(String href) {
		return href != null && (href.startsWith("#") || href.startsWith("http://") || href.startsWith("https://")
			|| href.startsWith("mailto:"));
	}

}
