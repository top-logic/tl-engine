/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.logEntry.LogEntry;
import com.top_logic.util.Resources;

/**
 * {@link Mapping} that maps {@link LogEntry} message resource keys with optional encoded arguments
 * to internationalized messages.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class LogEntryMessageMapping implements Mapping<String, String> {

	/** Singleton {@link LogEntryMessageMapping} instance. */
	public static final LogEntryMessageMapping INSTANCE = new LogEntryMessageMapping();

	/**
	 * Creates a new {@link LogEntryMessageMapping}.
	 * 
	 */
	protected LogEntryMessageMapping() {
		// singleton instance
	}

	@Override
	public String map(String input) {
		return Resources.getInstance().getString(ResKey.decode(input));
	}

}
