/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.docs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions presenting the developer articles of the application ({@link DevDocs}) to a
 * human reader, e.g. in a documentation window of the development menu.
 */
@ScriptPrefix("devDocs")
public class DevDocsFunctions extends TLScriptFunctions {

	/** Key of the article name in an entry of {@link #list(String)}. */
	public static final String KEY_NAME = "name";

	/** Key of the article title in an entry of {@link #list(String)}. */
	public static final String KEY_TITLE = "title";

	/** Key of the article description in an entry of {@link #list(String)}. */
	public static final String KEY_DESCRIPTION = "description";

	/**
	 * The developer articles of the application, sorted by title.
	 *
	 * @param search
	 *        Words that an article must all contain in its title, its description or its text,
	 *        ignoring case. Empty for all articles.
	 * @return For each article a dictionary with its name ({@value #KEY_NAME}), its title
	 *         ({@value #KEY_TITLE}) and the description saying when to read it
	 *         ({@value #KEY_DESCRIPTION}).
	 */
	@Label("Developer articles")
	@SideEffectFree
	public static List<Map<String, Object>> list(String search) {
		List<String> words = new ArrayList<>();
		if (search != null) {
			for (String word : search.toLowerCase(Locale.ROOT).split("\\s+")) {
				if (!word.isEmpty()) {
					words.add(word);
				}
			}
		}
		List<DevDocs.Doc> docs = new ArrayList<>(DevDocs.load());
		docs.sort(Comparator.comparing(DevDocs.Doc::title, String.CASE_INSENSITIVE_ORDER));
		List<Map<String, Object>> result = new ArrayList<>();
		for (DevDocs.Doc doc : docs) {
			if (matches(doc, words)) {
				Map<String, Object> entry = new LinkedHashMap<>();
				entry.put(KEY_NAME, doc.name());
				entry.put(KEY_TITLE, doc.title());
				entry.put(KEY_DESCRIPTION, doc.description());
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * The developer article with the given name as HTML.
	 *
	 * @see DevDocs#toHtml(String)
	 *
	 * @param name
	 *        The article name, as in the {@value #KEY_NAME} entry of {@link #list(String)}.
	 * @return The HTML source, {@code null} if there is no such article.
	 */
	@Label("Developer article as HTML")
	@SideEffectFree
	public static String html(String name) {
		if (name == null) {
			return null;
		}
		DevDocs.Doc doc = DevDocs.get(name);
		if (doc == null) {
			return null;
		}
		return DevDocs.toHtml(doc.text());
	}

	private static boolean matches(DevDocs.Doc doc, List<String> words) {
		if (words.isEmpty()) {
			return true;
		}
		String content = (doc.title() + "\n" + doc.description() + "\n" + doc.text()).toLowerCase(Locale.ROOT);
		for (String word : words) {
			if (!content.contains(word)) {
				return false;
			}
		}
		return true;
	}

}
