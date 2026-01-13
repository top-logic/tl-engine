/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.scripting;

import com.vladsch.flexmark.html.HtmlRenderer;
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

}
