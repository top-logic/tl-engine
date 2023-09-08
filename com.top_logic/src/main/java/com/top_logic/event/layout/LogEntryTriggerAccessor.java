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
 * Access to the {@link LogEntry#getTrigger() trigger} of a {@link LogEntry}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LogEntryTriggerAccessor extends ReadOnlyAccessor<LogEntry> {

	/** Accessing name for the triggering object. */
	public static final String TRIGGER = "trigger";

	/** Singleton {@link LogEntryTriggerAccessor} instance. */
	public static final LogEntryTriggerAccessor INSTANCE = new LogEntryTriggerAccessor();

	private LogEntryTriggerAccessor() {
		// singleton instance
	}

	@Override
	public Object getValue(LogEntry entry, String property) {
		Wrapper trigger = WrapperFactory.getWrapper(entry.getTrigger());

		if (trigger != null) {
			return trigger;
		}
		return entry.getTriggerName();
	}

}

