/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.migrate.ticket27604;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.element.layout.create.ConstantCreateTypeOptions;
import com.top_logic.layout.tools.rewrite.DescendingDocumentRewrite;
import com.top_logic.layout.tools.rewrite.DocumentRewrite;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.model.search.providers.GridCreateHandlerByExpression;

/**
 * {@link DocumentRewrite} migrating the configuration of {@link GridCreateHandlerByExpression}
 * <code>createType</code> into {@link ConstantCreateTypeOptions}.
 * <p>
 * The migration is the following:
 * </p>
 * 
 * <pre>
 * <code>
 * - 	createType="TYPE"
 * + 	&lt;type-options class="com.top_logic.element.layout.create.ConstantCreateTypeOptions"
 * + 		include-subtypes="false"
 * + 		type="TYPE"
 * + 	/&gt;
 * </code>
 * </pre>
 *
 * @implNote The string literals are copied here and not references into the actual code, as that
 *           might change, which would break this migration.
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class UpgradeGridCreateHandlerByExpression27604 extends DescendingDocumentRewrite {

	private static final String PROPERTY_ROOT_PATH = "root_path";

	private static final String PROPERTY_MAX_DEPTH = "max_depth";

	private static final String PROPERTY_FOLLOW_LINKS = "follow_links";

	private static final String SEARCHED_TYPE = "com.top_logic.model.search.providers.GridCreateHandlerByExpression";

	private static final String SEARCHED_ATTRIBUTE = "createType";

	/** Creates a {@link UpgradeGridCreateHandlerByExpression27604}. */
	public UpgradeGridCreateHandlerByExpression27604() {
		super();
	}

	@Override
	public void init(Log log) {
		// The log is not used.
	}

	@Override
	protected boolean rewriteElement(Element layout) {
		if (!Objects.equals(layout.getAttribute("class"), SEARCHED_TYPE)) {
			return false;
		}
		String createType = layout.getAttribute(SEARCHED_ATTRIBUTE).strip();
		if (StringServices.isEmpty(createType)) {
			return false;
		}
		layout.removeAttribute(SEARCHED_ATTRIBUTE);
		addTypeOptionsTag(layout, createType);
		return true;
	}

	private void addTypeOptionsTag(Element layout, String type) {
		Document document = layout.getOwnerDocument();
		Element typeOptions = document.createElement("type-options");
		typeOptions.setAttribute("class", "com.top_logic.element.layout.create.ConstantCreateTypeOptions");
		typeOptions.setAttribute("include-subtypes", "false");
		typeOptions.setAttribute("type", type);
		layout.appendChild(typeOptions);
	}

	/**
	 * Migrates the layout XML files in the working directory, recursively.
	 * <p>
	 * Can be called without parameters or with exactly three:
	 * </p>
	 * <ol>
	 * <li>The path to the workspace.</li>
	 * <li>The max search depth.</li>
	 * <li>Whether links should be followed.</li>
	 * </ol>
	 */
	public static void main(String[] arguments) {
		Path rootPath = getRootPath();
		int maxDepth = getMaxDepth();
		boolean followLinks = getFollowLinks();

		System.out.println("Migration for ticket #27604:");
		System.out.println("\tPath: '" + rootPath.toAbsolutePath().normalize() + "'");
		System.out.println("\tMax Depth: " + maxDepth);
		System.out.println("\tFollow Links: " + followLinks);
		new UpgradeGridCreateHandlerByExpression27604().rewriteWorkspaceFiles(rootPath, maxDepth, followLinks);
		System.out.println("Migration completed.");
	}

	private static Path getRootPath() {
		String property = getPropertyElseEnv(PROPERTY_ROOT_PATH);
		if (StringServices.isEmpty(property)) {
			return Path.of(".");
		}
		return Path.of(property);
	}

	private static int getMaxDepth() {
		String property = getPropertyElseEnv(PROPERTY_MAX_DEPTH);
		if (StringServices.isEmpty(property)) {
			return 20;
		}
		return Integer.parseInt(property);
	}

	private static boolean getFollowLinks() {
		String property = getPropertyElseEnv(PROPERTY_FOLLOW_LINKS);
		if (StringServices.isEmpty(property)) {
			return true;
		}
		return Boolean.parseBoolean(property);
	}

	private static String getPropertyElseEnv(String name) {
		return System.getProperty(name, System.getenv(name));
	}

	private void rewriteWorkspaceFiles(Path rootPath, int maxDepth, boolean followLinks) {
		FileVisitOption[] visitOptions = toFileVisitOptions(followLinks);
		try (Stream<Path> files = Files.find(rootPath, maxDepth, this::isXmlFile, visitOptions)) {
			files.parallel().forEach(this::rewriteFile);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private FileVisitOption[] toFileVisitOptions(boolean followLinks) {
		return followLinks ? new FileVisitOption[] { FileVisitOption.FOLLOW_LINKS } : new FileVisitOption[0];
	}

	private boolean isXmlFile(Path path, BasicFileAttributes attributes) {
		return attributes.isRegularFile() && isRelevant(path.getFileName().toString());
	}

	private final boolean isRelevant(String fileName) {
		return LayoutUtils.isLayout(fileName)
			|| LayoutUtils.isLayoutOverlay(fileName)
			|| LayoutUtils.isTemplate(fileName);
	}

	private void rewriteFile(Path path) {
		Document document = toDocument(path);
		boolean changes = rewrite(document);
		if (!changes) {
			return;
		}
		String xml = DOMUtil.toString(document);
		writeBack(path, xml);
	}

	private Document toDocument(Path path) {
		try {
			return DOMUtil.getDocumentBuilder().parse(path.toFile());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (SAXException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void writeBack(Path path, String xml) {
		System.out.println("Updating file: " + path);
		try {
			Files.writeString(path, xml, WRITE, TRUNCATE_EXISTING);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
