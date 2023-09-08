/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.xml.XMLStreamReaderAdapter;

/**
 * An {@link XMLStreamReaderAdapter} that filters whitespaces next to tags and comments.
 * <p>
 * <b>The implementation currently does not support all methods in the {@link XMLStreamReader}
 * interface.</b>
 * </p>
 * <p>
 * The following methods are not supported:
 * </p>
 * <ul>
 * <li>{@link #getElementText()}</li>
 * <li>{@link #nextTag()}</li>
 * <li>{@link #getTextCharacters()}</li>
 * <li>{@link #getTextCharacters(int, char[], int, int)}</li>
 * <li>{@link #getTextStart()}</li>
 * <li>{@link #getTextLength()}</li>
 * </ul>
 * <p>
 * If you need any of these methods, just implement it.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class WhitespaceFilteringXMLStreamReader extends XMLStreamReaderAdapter {

	private static final Pattern ALL_WHITESPACE_REGEXP = Pattern.compile("\\A[ \t\r\n]+\\z");

	private static final Pattern SURROUNDING_WHITESPACE_REGEXP = Pattern
		.compile("(?s)\\A[ \\t\\r\\n]*([^ \\t\\r\\n](.*[^ \\t\\r\\n])?)?[ \\t\\r\\n]*\\z");

	private static final int TEXT_GROUP = 1;

	private String _currentText;

	private boolean _filterActive;

	/**
	 * Creates a new {@link WhitespaceFilteringXMLStreamReader} with the given inner
	 * {@link XMLStreamReader}.
	 * <p>
	 * The filter is activated in the constructor, but can be controlled via
	 * {@link #activateFilter()}, {@link #deactivateFilter()} and {@link #isFilterActive()}.
	 * </p>
	 */
	public WhitespaceFilteringXMLStreamReader(XMLStreamReader innerReader) {
		super(innerReader);
		activateFilter();
	}

	/** Filter out whitespaces. */
	public void activateFilter() {
		_filterActive = true;
	}

	/** Don't filter out whitespaces. */
	public void deactivateFilter() {
		_filterActive = false;
	}

	/** Are whitespaces filtered out? */
	public boolean isFilterActive() {
		return _filterActive;
	}

	@Override
	public int next() throws XMLStreamException {
		if (!_filterActive) {
			return super.next();
		}
		super.next();
		while (isWhitespaceEvent()) {
			super.next();
		}
		if (isCharacterEvent(super.getEventType())) {
			Matcher matcher = SURROUNDING_WHITESPACE_REGEXP.matcher(super.getText());
			if (matcher.find()) {
				_currentText = matcher.group(TEXT_GROUP);
			} else {
				_currentText = null;
			}
		} else {
			_currentText = null;
		}
		return super.getEventType();
	}

	private boolean isWhitespaceEvent() {
		return isCharacterEvent(super.getEventType()) && isWhitespaceOnly(super.getText());
	}

	private boolean isCharacterEvent(int eventType) {
		return (eventType == XMLStreamConstants.SPACE) || (eventType == XMLStreamConstants.CHARACTERS);
	}

	private boolean isWhitespaceOnly(String text) {
		return ALL_WHITESPACE_REGEXP.matcher(text).matches();
	}

	@Override
	public boolean isWhiteSpace() {
		if (!_filterActive) {
			super.isWhiteSpace();
		}
		return false;
	}

	@Override
	public String getText() {
		if (!_filterActive) {
			return super.getText();
		}
		if (isCharacterEvent(super.getEventType())) {
			return _currentText;
		}
		return super.getText();
	}

	@Override
	public boolean hasText() {
		if (!_filterActive) {
			return super.hasText();
		}
		if (isCharacterEvent(super.getEventType())) {
			return !_currentText.isEmpty();
		}
		return super.hasText();
	}

	/** @deprecated This method is not yet implemented and therefore not supported. */
	@Deprecated
	@Override
	public String getElementText() throws UnsupportedOperationException {
		throw createUnsupportedOperationException();
	}

	/** @deprecated This method is not yet implemented and therefore not supported. */
	@Deprecated
	@Override
	public int nextTag() throws UnsupportedOperationException {
		throw createUnsupportedOperationException();
	}

	/** @deprecated This method is not yet implemented and therefore not supported. */
	@Deprecated
	@Override
	public char[] getTextCharacters() throws UnsupportedOperationException {
		throw createUnsupportedOperationException();
	}

	/** @deprecated This method is not yet implemented and therefore not supported. */
	@Deprecated
	@Override
	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
			throws UnsupportedOperationException {
		throw createUnsupportedOperationException();
	}

	/** @deprecated This method is not yet implemented and therefore not supported. */
	@Deprecated
	@Override
	public int getTextStart() throws UnsupportedOperationException {
		throw createUnsupportedOperationException();
	}

	/** @deprecated This method is not yet implemented and therefore not supported. */
	@Deprecated
	@Override
	public int getTextLength() throws UnsupportedOperationException {
		throw createUnsupportedOperationException();
	}

	private UnsupportedOperationException createUnsupportedOperationException() {
		return new UnsupportedOperationException("This method is not supported by the " + getClass().getSimpleName()
			+ ". If you need it, implement it.");
	}

}
