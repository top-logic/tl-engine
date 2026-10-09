/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.docs.DevDoc;
import com.top_logic.basic.docs.DevDocs;
import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions presenting the developer documentation of the application ({@link DevDocs})
 * to a human reader, e.g. as a tree of chapters and articles beside the selected article.
 */
@ScriptPrefix("devDocs")
public class DevDocsFunctions extends TLScriptFunctions {

	/** Character standing for the {@code /} of an entry name in its {@link #routeKey(Object) URL key}. */
	public static final char ROUTE_SEPARATOR = '~';

	/**
	 * The root of the developer documentation.
	 *
	 * @return The root chapter, whose entries are the top-level chapters and articles.
	 */
	@Label("Root of the developer documentation")
	@SideEffectFree
	public static DevDoc root() {
		return DevDocs.load();
	}

	/**
	 * The chapters and articles of a chapter of the developer documentation.
	 *
	 * @param node
	 *        A chapter, e.g. the root of the documentation.
	 * @param search
	 *        Words that an article must all contain in its title, its description or its text,
	 *        ignoring case; a chapter is included if its introduction or one of its articles is.
	 *        Empty for all chapters and articles.
	 * @return The chapters and articles of the given chapter, empty for an article.
	 */
	@Label("Entries of a chapter of the developer documentation")
	@SideEffectFree
	public static List<DevDoc> children(Object node, String search) {
		if (!(node instanceof DevDoc doc)) {
			return List.of();
		}
		List<String> words = words(search);
		List<DevDoc> result = new ArrayList<>();
		for (DevDoc child : doc.getChildren()) {
			if (matches(child, words)) {
				result.add(child);
			}
		}
		return result;
	}

	/**
	 * The chapter a chapter or an article of the developer documentation belongs to.
	 *
	 * @param node
	 *        A chapter or an article.
	 * @return The chapter, {@code null} for the root of the documentation.
	 */
	@Label("Chapter of an entry of the developer documentation")
	@SideEffectFree
	public static DevDoc parent(Object node) {
		return node instanceof DevDoc doc ? doc.getParent() : null;
	}

	/**
	 * The chapter or article of the developer documentation a link names.
	 *
	 * @param link
	 *        The name of a chapter or an article, optionally with the scheme {@code doc:} in front
	 *        and a section anchor behind, e.g. {@code view-layer/basics#spacing-model}.
	 * @return The chapter or article, {@code null} if the documentation has none of that name.
	 */
	@Label("Entry of the developer documentation")
	@SideEffectFree
	public static DevDoc get(String link) {
		return link == null ? null : DevDocs.find(DevDocs.load(), link);
	}

	/**
	 * Where the file of a chapter or an article of the developer documentation comes from, e.g. to
	 * find it in the sources.
	 *
	 * @param node
	 *        A chapter or an article.
	 * @return The module, if it can be told, and the path of the file on the class path, e.g.
	 *         {@code tl-layout-view: META-INF/tl-docs/view-layer/tables.md}; {@code null} for a
	 *         chapter without introduction.
	 */
	@Label("Source of an entry of the developer documentation")
	@SideEffectFree
	public static String source(Object node) {
		return node instanceof DevDoc doc ? doc.getSource() : null;
	}

	/**
	 * The key of a chapter or an article of the developer documentation in a URL.
	 *
	 * <p>
	 * The key is the name with {@value #ROUTE_SEPARATOR} in place of each {@code /}, so that it is a
	 * single segment of a URL path; an encoded slash is refused by many servlet containers.
	 * </p>
	 *
	 * @param node
	 *        A chapter or an article.
	 * @return The key, {@code null} for no entry.
	 *
	 * @see #byRouteKey(String)
	 */
	@Label("URL key of an entry of the developer documentation")
	@SideEffectFree
	public static String routeKey(Object node) {
		return node instanceof DevDoc doc ? doc.getName().replace('/', ROUTE_SEPARATOR) : null;
	}

	/**
	 * The chapter or article of the developer documentation a URL key names.
	 *
	 * @param key
	 *        A key as computed by {@link #routeKey(Object)}.
	 * @return The chapter or article, {@code null} if the documentation has none of that key.
	 */
	@Label("Entry of the developer documentation by its URL key")
	@SideEffectFree
	public static DevDoc byRouteKey(String key) {
		return key == null || key.isEmpty() ? null : DevDocs.find(DevDocs.load(), key.replace(ROUTE_SEPARATOR, '/'));
	}

	/**
	 * A chapter or an article of the developer documentation as HTML.
	 *
	 * @param node
	 *        A chapter or an article.
	 * @return The HTML source: the text and, for a chapter, the list of its entries; {@code null}
	 *         for no entry.
	 *
	 * @see DevDocs#toHtml(DevDoc, DevDoc, String)
	 */
	@Label("Developer documentation as HTML")
	@SideEffectFree
	public static String html(Object node) {
		if (!(node instanceof DevDoc doc)) {
			return null;
		}
		DevDoc root = doc;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		return DevDocs.toHtml(doc, root, ReactHtmlControl.LINK_ATTRIBUTE);
	}

	private static List<String> words(String search) {
		List<String> result = new ArrayList<>();
		if (search != null) {
			for (String word : search.toLowerCase(Locale.ROOT).split("\\s+")) {
				if (!word.isEmpty()) {
					result.add(word);
				}
			}
		}
		return result;
	}

	private static boolean matches(DevDoc node, List<String> words) {
		if (words.isEmpty()) {
			return true;
		}
		if (node.getText() != null) {
			String content =
				(node.getTitle() + "\n" + node.getDescription() + "\n" + node.getText()).toLowerCase(Locale.ROOT);
			boolean all = true;
			for (String word : words) {
				if (!content.contains(word)) {
					all = false;
					break;
				}
			}
			if (all) {
				return true;
			}
		}
		for (DevDoc child : node.getChildren()) {
			if (matches(child, words)) {
				return true;
			}
		}
		return false;
	}

}
