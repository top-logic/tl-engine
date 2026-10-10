/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The framework's citation syntax: a Markdown link to the source.
 *
 * <ul>
 * <li>{@code [Sommerfest](tl:GenericObject:11047)} - an object of the application, by the identifier
 * a tool showed for it.</li>
 * <li>{@code [laut Tagesschau](https://www.tagesschau.de/...)} - a web page, by its address.</li>
 * <li>{@code [...](https://... "the sentence the statement rests on")} - the link title names the
 * passage, which the popover highlights when the page's text contains it.</li>
 * </ul>
 *
 * <p>
 * An object link without its scheme, {@code [Sommerfest](GenericObject:11047)}, is understood as
 * well: a smaller model drops the {@code tl:} now and then, and the identifier alone - a capitalised
 * type name, a colon, an id - is no address a browser could open anyway. A lower-case scheme such as
 * {@code mailto:} or {@code tel:} is not taken for one.
 * </p>
 *
 * <p>
 * A link is the shape a model writes without being taught it, and it reads as the claim it supports:
 * the link text is the part of the sentence the source backs. An image ({@code ![...](...)}), a
 * relative link and a link of any other scheme is not a marker and is left alone.
 * </p>
 */
public final class LinkMarkers implements CitationMarkerSyntax {

	/** The scheme of a link to an object of the application. */
	public static final String OBJECT_SCHEME = "tl:";

	/**
	 * {@code [text](target "title")}. The text may hold one level of nested brackets; the target is
	 * {@code tl:…} or an http(s) address, which may hold one level of balanced parentheses as
	 * Wikipedia addresses do, or a bare object identifier.
	 */
	private static final Pattern LINK = Pattern.compile(
		"(?<!!)\\[((?:[^\\[\\]\\n]|\\[[^\\[\\]\\n]*\\])*)\\]"
			+ "\\(\\s*((?:[tT][lL]:|[hH][tT][tT][pP][sS]?://)(?:[^\\s()]|\\([^\\s()]*\\))+"
			+ "|[A-Z][A-Za-z0-9_]*:[A-Za-z0-9_.-]+)\\s*(?:\"([^\"\\n]*)\")?\\s*\\)");

	/** The one instance: the syntax has no configuration. */
	public static final LinkMarkers INSTANCE = new LinkMarkers();

	private LinkMarkers() {
		// Singleton.
	}

	@Override
	public List<Marker> find(String text) {
		List<Marker> result = new ArrayList<>();
		Matcher link = LINK.matcher(text);
		while (link.find()) {
			String target = link.group(2);
			String reference = target.regionMatches(true, 0, OBJECT_SCHEME, 0, OBJECT_SCHEME.length())
				? target.substring(OBJECT_SCHEME.length())
				: target;
			result.add(new Marker(link.start(), link.end(), link.group(1), List.of(reference),
				link.group(3)));
		}
		return result;
	}
}
