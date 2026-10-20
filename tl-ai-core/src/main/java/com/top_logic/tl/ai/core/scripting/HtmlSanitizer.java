/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * Allow-list filter for HTML that was produced from model output.
 *
 * <p>
 * Everything the chat renders is text a model wrote, and a model writes down what
 * {@code webFetch} and the tool results put in front of it. Such text is attacker-controlled, it is
 * persisted with the conversation and it is rendered again on every reopen, so markup in it must
 * never reach the browser as anything but the formatting a Markdown answer needs.
 * </p>
 *
 * <p>
 * The filter keeps the elements Markdown and the enabled flexmark extensions produce - headings,
 * lists, tables, code, block quotes, {@code <del>}, {@code <ins>}, {@code <sup>}, {@code <hr>} -
 * and drops everything else, every event-handler attribute, and every URL whose scheme is not
 * {@code http}, {@code https} or {@code mailto}.
 * </p>
 *
 * @see Converters#markdownToHtmlString(String)
 */
public class HtmlSanitizer {

	/**
	 * Base against which a relative URL is resolved for the scheme check.
	 *
	 * <p>
	 * jsoup validates a URL by resolving it and looking at the scheme of the result, so without a
	 * base every relative URL fails the check and loses its attribute. Relative URLs are normal
	 * here: {@code publishPage} hands the model a root-relative link whenever the deployment
	 * declares no {@code %HOST%}, and the model puts that link in its answer. Resolving against a
	 * fixed {@code https} origin makes such a URL pass, while
	 * {@link Safelist#preserveRelativeLinks(boolean)} keeps the original relative form in the
	 * output. A scheme jsoup cannot resolve - {@code javascript:}, {@code data:}, {@code vbscript:}
	 * - does not resolve against this base either and is still rejected.
	 * </p>
	 */
	private static final String RESOLVE_BASE = "https://localhost/";

	private static final Safelist SAFELIST = createSafelist();

	/**
	 * Keeps the renderer's own line breaks instead of re-indenting.
	 *
	 * <p>
	 * Pretty-printing would put {@code <br>} on a line of its own and add indentation between
	 * inline elements, which changes the spacing the reader sees.
	 * </p>
	 */
	private static final Document.OutputSettings OUTPUT = new Document.OutputSettings().prettyPrint(false);

	/**
	 * Drops everything from the given HTML that is not on the allow-list.
	 *
	 * @param html
	 *        Rendered HTML, may be <code>null</code>.
	 * @return The same document with unsafe elements, attributes and URLs removed;
	 *         <code>null</code> for a <code>null</code> input.
	 */
	public static String sanitize(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		return Jsoup.clean(html, RESOLVE_BASE, SAFELIST, OUTPUT);
	}

	private static Safelist createSafelist() {
		return Safelist.relaxed()
			// What the strikethrough, ins and table extensions emit, and the rule a thematic
			// break renders as. Safelist.relaxed() knows none of them.
			.addTags("del", "ins", "hr")

			// Written by Converters' attribute provider: the chat is a single-page application,
			// so a link that navigated the app window would discard the conversation.
			.addAttributes("a", "target", "rel")

			// The language marker on a fenced code block, which the syntax highlighting reads.
			.addAttributes("code", "class")
			.addAttributes("pre", "class")

			// Column alignment from a Markdown table header (|:---|---:|).
			.addAttributes("th", "align")
			.addAttributes("td", "align")

			// A link into the answer itself, e.g. [see below](#section).
			.addProtocols("a", "href", "#")

			// See RESOLVE_BASE: a root-relative link into this application stays relative.
			.preserveRelativeLinks(true);
	}

	private HtmlSanitizer() {
		// Static utility.
	}

}
