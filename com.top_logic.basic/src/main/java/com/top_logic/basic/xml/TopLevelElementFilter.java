/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.util.function.Predicate;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * {@link XMLStreamReader} that hides the top-level elements (the children of the document element)
 * of an underlying reader whose local name is not accepted by a filter.
 *
 * <p>
 * A rejected element is skipped together with its whole content. The document element itself and
 * all content of accepted top-level elements are passed through unchanged.
 * </p>
 */
public class TopLevelElementFilter extends StreamReaderDelegate {

	/**
	 * Depth of a top-level element: The document element has depth 1.
	 */
	private static final int TOP_LEVEL_DEPTH = 2;

	private final Predicate<String> _acceptedNames;

	/**
	 * Depth of the element of the current event, counting the document element as 1.
	 */
	private int _depth;

	/**
	 * Creates a {@link TopLevelElementFilter}.
	 *
	 * @param reader
	 *        The reader to filter. It must be positioned before the document element.
	 * @param acceptedNames
	 *        Filter for the local names of the top-level elements to pass through.
	 */
	public TopLevelElementFilter(XMLStreamReader reader, Predicate<String> acceptedNames) {
		super(reader);
		_acceptedNames = acceptedNames;
	}

	@Override
	public int next() throws XMLStreamException {
		while (true) {
			int event = super.next();
			switch (event) {
				case XMLStreamConstants.START_ELEMENT:
					_depth++;
					if (_depth == TOP_LEVEL_DEPTH && !_acceptedNames.test(getLocalName())) {
						XMLStreamUtil.skipUpToMatchingEndTag(getParent());
						_depth--;
						continue;
					}
					return event;
				case XMLStreamConstants.END_ELEMENT:
					_depth--;
					return event;
				default:
					return event;
			}
		}
	}

	@Override
	public int nextTag() throws XMLStreamException {
		// Must not delegate: The underlying reader would not skip rejected elements.
		int event = next();
		while (isIgnorable(event)) {
			event = next();
		}
		if (event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_ELEMENT) {
			throw new XMLStreamException("Expected start or end tag, found '" + XMLStreamUtil.getEventName(event)
				+ "'.", getLocation());
		}
		return event;
	}

	private boolean isIgnorable(int event) {
		switch (event) {
			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.CDATA:
				return isWhiteSpace();
			case XMLStreamConstants.SPACE:
			case XMLStreamConstants.COMMENT:
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				return true;
			default:
				return false;
		}
	}

}
