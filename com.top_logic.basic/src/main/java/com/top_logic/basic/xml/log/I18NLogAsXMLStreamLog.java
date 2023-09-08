/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.log;

import javax.xml.stream.Location;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;

/**
 * Internationalizable error reporting during XML stream parsing (and writing).
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NLogAsXMLStreamLog implements XMLStreamLog {

	private I18NLog _log;

	/**
	 * Creates a {@link I18NLogAsXMLStreamLog}.
	 */
	protected I18NLogAsXMLStreamLog(I18NLog log) {
		_log = log;
	}

	@Override
	public void log(Level level, Location location, ResKey message, Throwable ex) {
		_log.log(level, withLocation(location, message), ex);
	}

	/**
	 * Add location information to the given error key.
	 */
	public static ResKey withLocation(Location location, ResKey errorKey) {
		if (location == null) {
			return errorKey;
		}
		String file = location.getSystemId();
		ResKey msg;
		if (StringServices.isEmpty(file)) {
			msg = I18NConstants.AT_LOCATION__LINE_COL_DETAIL.fill(location.getLineNumber(), location.getColumnNumber(),
				errorKey);
		} else {
			msg = I18NConstants.AT_LOCATION__FILE_LINE_COL_DETAIL.fill(file, location.getLineNumber(),
				location.getColumnNumber(), errorKey);
		}
		return msg;
	}

}
