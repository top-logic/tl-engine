/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.Log;
import com.top_logic.basic.MessageEnhancingLog;
import com.top_logic.basic.Protocol;

/**
 * {@link MessageEnhancingLog} that appends the current location in a {@link XMLStreamReader}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogWithStreamLocation extends MessageEnhancingLog {

	private final Log _log;

	private XMLStreamReader _stream;

	/**
	 * Creates a {@link LogWithStreamLocation}.
	 * 
	 * @param impl
	 *        The target {@link Protocol}.
	 */
	public LogWithStreamLocation(Log impl) {
		_log = impl;
	}

	@Override
	protected Log impl() {
		return _log;
	}

	/**
	 * Updates the {@link XMLStreamReader} to take the location from.
	 * 
	 * @param value
	 *        The new stream.
	 * @return The stream before.
	 */
	public XMLStreamReader setStream(XMLStreamReader value) {
		XMLStreamReader before = _stream;
		_stream = value;
		return before;
	}

	@Override
	protected String enhance(String message) {
		if (_stream == null) {
			return message;
		}

		Location location = _stream.getLocation();
		return strip(message) + " at " + location.getSystemId() + " line " + location.getLineNumber() + " column "
			+ location.getColumnNumber() + ".";
	}

	private String strip(String message) {
		if (message.endsWith(".")) {
			return message.substring(0, message.length() - 1);
		} else {
			return message;
		}
	}

}