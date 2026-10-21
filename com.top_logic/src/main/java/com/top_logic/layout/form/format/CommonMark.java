/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Objects;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.top_logic.basic.xml.TagWriter;

/**
 * Service class to handle String values in <i>CommonMark</i> syntax.
 * 
 * @see "https://commonmark.org/"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommonMark {

	/**
	 * Renders a {@link String} in common mark syntax to an HTML fragment.
	 * 
	 * <p>
	 * The fragment is the one {@link #writeCommonMark(TagWriter, String)} writes: HTML contained
	 * in the text is escaped, and link and image targets with a scheme that can run a script (such
	 * as <code>javascript:</code>) are dropped. A text that cannot be parsed is rendered as plain
	 * text.
	 * </p>
	 * 
	 * @param value
	 *        The value in <i>CommonMark</i> syntax, not <code>null</code>.
	 * @return The HTML fragment.
	 */
	public static String toHtml(String value) {
		TagWriter out = new TagWriter();
		try {
			writeCommonMark(out, value);
		} catch (IOException ex) {
			// Writing to a string does not fail.
			throw new UncheckedIOException(ex);
		}
		return out.toString();
	}

	/**
	 * Writes a {@link String} in common mark syntax to the given {@link TagWriter}. Contained HTML
	 * is escaped.
	 * 
	 * @see #writeCommonMark(TagWriter, String, boolean)
	 */
	public static void writeCommonMark(TagWriter out, String value) throws IOException {
		writeCommonMark(out, value, true);
	}

	/**
	 * Writes a {@link String} in common mark syntax to the given {@link TagWriter}
	 * 
	 * <p>
	 * Link and image targets with a scheme that can run a script (such as
	 * <code>javascript:</code>) are dropped.
	 * </p>
	 * 
	 * @param out
	 *        {@link TagWriter} to write to.
	 * @param value
	 *        The value in <i>CommonMark</i> syntax to write.
	 * @param escapeHTML
	 *        Whether HTML should be escaped. Note that HTML is only a tag itself, not the text
	 *        between an opening tag and a closing tag. So markup in the text will be parsed as
	 *        normal and is not affected by this option.
	 */
	public static void writeCommonMark(TagWriter out, String value, boolean escapeHTML) throws IOException {
		Objects.requireNonNull(value, "Given value must not be null.");

		Parser parser = Parser
			.builder()
			.build();
		Node node;
		try {
			node = parser.parse(value);
		} catch (RuntimeException ex) {
			// Value could not be parsed
			out.writeText(value);
			return;
		}
		HtmlRenderer renderer = HtmlRenderer
			.builder()
			.escapeHtml(escapeHTML)
			.sanitizeUrls(true)
			.build();
		// Ensure that all content is delivered to the internal Writer
		out.flushBuffer();
		Writer internalWriter = out.internalTargetWriter();
		// Render to the internal Writer to avoid quoting of HTML structure.
		renderer.render(node, internalWriter);
	}

}

