/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.util.Resources;

/**
 * Resolves localized config values to plain English text suitable for an LLM tool description.
 */
public final class ToolDescriptions {

	private ToolDescriptions() {
		// utility
	}

	/**
	 * Resolves the given key in English and strips HTML tags. Returns an empty string for null
	 * keys.
	 */
	public static String englishPlainText(HtmlResKey key) {
		if (key == null) {
			return "";
		}
		// DefaultHtmlResKey is the sole HtmlResKey implementation in the engine; it stores the
		// underlying ResKey (delivering HTML source) in its content() field.
		if (!(key instanceof DefaultHtmlResKey defaultKey)) {
			throw new IllegalArgumentException(
				"Unsupported HtmlResKey implementation: " + key.getClass().getName()
					+ " (expected DefaultHtmlResKey)");
		}
		ResKey underlying = defaultKey.content();
		String html = Resources.getInstance(Locale.ENGLISH).getString(underlying, "");
		return stripHtml(html);
	}

	/**
	 * Resolves the given key in English. Returns an empty string for null / empty keys.
	 */
	public static String englishPlainText(ResKey key) {
		if (key == null) {
			return "";
		}
		return Resources.getInstance(Locale.ENGLISH).getString(key, "");
	}

	private static String stripHtml(String html) {
		// Minimal tag stripper — tool descriptions are short, no need for a real parser.
		String noTags = html.replaceAll("<[^>]+>", " ");
		String decoded = noTags
			.replace("&nbsp;", " ")
			.replace("&amp;", "&")
			.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&quot;", "\"");
		return decoded.replaceAll("\\s+", " ").trim();
	}

}
