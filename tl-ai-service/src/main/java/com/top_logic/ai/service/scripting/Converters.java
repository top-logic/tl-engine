/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.scripting;

import java.util.Arrays;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * Data conversion utilities for TL-Script.
 */
@ScriptPrefix("convert")
public class Converters extends TLScriptFunctions {

	private static final MutableDataSet DEFAULT_OPTIONS = createDefaultOptions();

	/**
	 * Converts <i>Markdown</i> to HTML.
	 *
	 * @param markdown
	 *        Text in <i>Markdown</i> format.
	 * @return HTML fragment that can be rendered.
	 */
	public static HTMLFragment markdownToHtml(@Mandatory String markdown) {
		MutableDataSet options = new MutableDataSet();

		// uncomment to set optional extensions
		// options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(),
		// StrikethroughExtension.create()));

		// uncomment to convert soft-breaks to hard breaks
		// options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).build();

		// You can re-use parser and renderer instances
		Node document = parser.parse(markdown);
		String html = renderer.render(document);

		return Fragments.htmlSource(html);
	}

	/**
	 * Converts HTML to <i>Markdown</i>.
	 *
	 * @param html
	 *        Text in HTML format.
	 * @return Markdown formatted text.
	 */
	public static String htmlToMarkdown(@Mandatory String html) {
		return FlexmarkHtmlConverter.builder(DEFAULT_OPTIONS).build().convert(html);
	}

	private static MutableDataSet createDefaultOptions() {
		MutableDataSet options = new MutableDataSet();

		// Enable extensions for better HTML conversion
		options.set(Parser.EXTENSIONS, Arrays.asList(
			TablesExtension.create(),
			StrikethroughExtension.create(),
			EmojiExtension.create(),
			InsExtension.create(),
			SuperscriptExtension.create()
		));

		// Configure the HTML to Markdown converter for cleaner output
		options.set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false); // Use ATX headings (# style) instead of setext (underlined)
		options.set(FlexmarkHtmlConverter.OUTPUT_ATTRIBUTES_ID, false); // Don't output id attributes
		options.set(FlexmarkHtmlConverter.WRAP_AUTO_LINKS, false); // Don't wrap auto-links in angle brackets
		options.set(FlexmarkHtmlConverter.EXTRACT_AUTO_LINKS, true); // Extract links from anchor tags
		options.set(FlexmarkHtmlConverter.DOT_ONLY_NUMERIC_LISTS, true); // Use . for numeric lists
		options.set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false); // <br> as space, not blank line
		options.set(FlexmarkHtmlConverter.LISTS_END_ON_DOUBLE_BLANK, false); // End lists on single blank
		options.set(FlexmarkHtmlConverter.DIV_AS_PARAGRAPH, true); // Treat divs as paragraphs
		options.set(FlexmarkHtmlConverter.SKIP_HEADING_1, false); // Include H1 headings
		options.set(FlexmarkHtmlConverter.SKIP_HEADING_2, false); // Include H2 headings
		options.set(FlexmarkHtmlConverter.SKIP_ATTRIBUTES, true); // Skip HTML attributes
		return options;
	}

}
