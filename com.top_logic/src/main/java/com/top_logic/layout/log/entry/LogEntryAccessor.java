/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.entry;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * {@link Accessor} for {@link ParsedLogEntry}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogEntryAccessor extends ReadOnlyAccessor<ParsedLogEntry> {

	/** The {@link LogEntryAccessor} instance. */
	public static final LogEntryAccessor INSTANCE = new LogEntryAccessor();

	@Override
	public Object getValue(ParsedLogEntry logEntry, String property) {
		switch (property) {
			case ParsedLogEntry.PROPERTY_TIME: {
				return logEntry.getTime();
			}
			case ParsedLogEntry.PROPERTY_SEVERITY: {
				return logEntry.getSeverity();
			}
			case ParsedLogEntry.PROPERTY_CATEGORY: {
				return logEntry.getCategory();
			}
			case ParsedLogEntry.PROPERTY_THREAD: {
				return logEntry.getThread();
			}
			case ParsedLogEntry.PROPERTY_MESSAGE: {
				return logEntry.getMessage();
			}
			case ParsedLogEntry.PROPERTY_FILE_CATEGORY: {
				return logEntry.getFileCategory();
			}
			case ParsedLogEntry.PROPERTY_FILE_NAME: {
				return logEntry.getFileName();
			}
			case ParsedLogEntry.PROPERTY_DETAILS: {
				return logEntry.getDetails();
			}
			default: {
				throw new RuntimeException(
					ParsedLogEntry.class.getSimpleName() + " has no property '" + property + "'.");
			}
		}
	}

}
