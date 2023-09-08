/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import com.top_logic.event.logEntry.LogEntry;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Access to the {@link LogEntry#getSource() source} of a {@link LogEntry}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LogEntrySourceAccessor extends ReadOnlyAccessor<LogEntry> {

	/** Accessing name for the source object. */
	public static final String SOURCE = "source";

	/** Singleton {@link LogEntrySourceAccessor} instance. */
	public static final LogEntrySourceAccessor INSTANCE = new LogEntrySourceAccessor();

	private LogEntrySourceAccessor() {
		// singleton instance
	}

	@Override
	public Object getValue(LogEntry entry, String property) {
		Wrapper source = WrapperFactory.getWrapper(entry.getSource());

		if (source != null) {
			return source;
		}
		return entry.getSourceName();
	}

}

