/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;

/**
 * An {@link XMLStreamReaderAdapter} that logs all events.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class LoggingXMLStreamReader extends XMLStreamReaderAdapter {

	/**
	 * Creates a new {@link LoggingXMLStreamReader} with the given {@link XMLStreamReader} as inner
	 * reader.
	 * 
	 * @see #wrap(XMLStreamReader)
	 */
	protected LoggingXMLStreamReader(XMLStreamReader innerReader) {
		super(innerReader);
	}

	@Override
	public int next() throws XMLStreamException {
		int result = super.next();
		if (Logger.isDebugEnabled(LoggingXMLStreamReader.class)) {
			log("next()    -> " + describeState());
		}
		return result;
	}

	@Override
	public int nextTag() throws XMLStreamException {
		int result = super.nextTag();
		if (Logger.isDebugEnabled(LoggingXMLStreamReader.class)) {
			log("nextTag() -> " + describeState());
		}
		return result;
	}

	/**
	 * Describes the state of the {@link XMLStreamReader}.
	 * <p>
	 * Is used to create the log messages that are passed to {@link #log(String)}.
	 * </p>
	 */
	protected String describeState() {
		if (!Logger.isDebugEnabled(LoggingXMLStreamReader.class)) {
			return null;
		}
		if (XMLStreamUtil.isAtStartTag(this)) {
			return buildStartTagDescription();
		}
		if (XMLStreamUtil.isAtEndTag(this)) {
			return buildEndTagDescription();
		}
		return XMLStreamUtil.getEventName(super.getEventType());
	}

	private String buildEndTagDescription() {
		return "</" + super.getLocalName() + ">";
	}

	private String buildStartTagDescription() {
		String namespacePrefix = StringServices.isEmpty(super.getPrefix()) ? "" : (super.getPrefix() + ":");
		String result = "<" + namespacePrefix + super.getLocalName();
		for (int i = 0; i < super.getAttributeCount(); i++) {
			result += buildAttributeDescription(i);
		}
		return result + " >";
	}

	private String buildAttributeDescription(int i) {
		String namespacePrefix =
			StringServices.isEmpty(super.getAttributePrefix(i)) ? "" : (super.getAttributePrefix(i) + ":");
		return " " + namespacePrefix + super.getAttributeLocalName(i) + "='" + super.getAttributeValue(i) + "'";
	}

	/**
	 * Logs the given message to {@link Logger#debug(String, Object)}.
	 */
	protected void log(String message) {
		Logger.debug(message, LoggingXMLStreamReader.class);
	}

	/**
	 * Wraps the given {@link XMLStreamReader} with a proxy that logs all read events in debug mode
	 * (if debugging is enabled for {@link LoggingXMLStreamReader}).
	 * 
	 * @param parser
	 *        The parser to wrap.
	 * @return The wrapped parser that logs events, if debug logging is enabled. Otherwise, the
	 *         given parser is returned unchanged.
	 */
	public static XMLStreamReader wrap(XMLStreamReader parser) {
		XMLStreamReader logger;
		if (Logger.isDebugEnabled(LoggingXMLStreamReader.class)) {
			logger = new LoggingXMLStreamReader(parser);
		} else {
			logger = parser;
		}
		return logger;
	}

}
