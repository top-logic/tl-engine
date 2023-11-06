/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * {@link Accessor} for {@link LogLine}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLineAccessor extends ReadOnlyAccessor<LogLine> {

	/** The {@link LogLineAccessor} instance. */
	public static final LogLineAccessor INSTANCE = new LogLineAccessor();

	@Override
	public Object getValue(LogLine logLine, String property) {
		switch (property) {
			case LogLine.PROPERTY_TIME: {
				return logLine.getTime();
			}
			case LogLine.PROPERTY_SEVERITY: {
				return logLine.getSeverity();
			}
			case LogLine.PROPERTY_CATEGORY: {
				return logLine.getCategory();
			}
			case LogLine.PROPERTY_THREAD: {
				return logLine.getThread();
			}
			case LogLine.PROPERTY_MESSAGE: {
				return logLine.getMessage();
			}
			case LogLine.PROPERTY_FILE_CATEGORY: {
				return logLine.getFileCategory();
			}
			case LogLine.PROPERTY_FILE_NAME: {
				return logLine.getFileName();
			}
			case LogLine.PROPERTY_DETAILS: {
				return logLine.getDetails();
			}
			default: {
				throw new RuntimeException(
					LogLine.class.getSimpleName() + " has no property '" + property + "'.");
			}
		}
	}

}
