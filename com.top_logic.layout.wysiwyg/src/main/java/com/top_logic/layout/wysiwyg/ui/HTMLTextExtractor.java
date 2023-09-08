/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link LabelProvider} extrating the visible text from a {@link StructuredText} object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HTMLTextExtractor implements LabelProvider {

	/**
	 * Singleton {@link HTMLTextExtractor} instance.
	 */
	public static final HTMLTextExtractor INSTANCE = new HTMLTextExtractor();

	/**
	 * Creates a {@link HTMLTextExtractor}.
	 */
	protected HTMLTextExtractor() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof StructuredText) {
			return extractText((StructuredText) object);
		}
		return null;
	}

	/**
	 * Extracts the text contents from a {@link StructuredText} value.
	 */
	protected final String extractText(StructuredText object) {
		return toText(object.getSourceCode());
	}

	/**
	 * Extracts the text contents from the given HTML source code.
	 *
	 * @param html
	 *        The HTML source code.
	 * @return A plain text value without formatting.
	 */
	protected String toText(String html) {
		Document document = Jsoup.parse(html);
		Node root = document.body();
		StringBuilder result = new StringBuilder();
		render(result, root);
		return result.toString();
	}

	private void render(StringBuilder result, Node current) {
		if (current instanceof TextNode) {
			result.append(((TextNode) current).text());
		} else if (current instanceof Element) {
			for (Node child : current.childNodes()) {
				render(result, child);
			}
			switch (((Element) current).tagName()) {
				case HTMLConstants.BR:
				case HTMLConstants.LI:
				case HTMLConstants.UL:
				case HTMLConstants.OL:
				case HTMLConstants.DD:
				case HTMLConstants.DT:
				case HTMLConstants.DIV: {
					result.append('\n');
					break;
				}
				case HTMLConstants.H1:
				case HTMLConstants.H2:
				case HTMLConstants.H3:
				case HTMLConstants.H4:
				case HTMLConstants.H5:
				case HTMLConstants.H6:
				case HTMLConstants.PARAGRAPH: {
					result.append('\n');
					result.append('\n');
					break;
				}
			}
		}
	}

}