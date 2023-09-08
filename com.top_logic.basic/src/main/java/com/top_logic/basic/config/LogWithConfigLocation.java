/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.Log;
import com.top_logic.basic.MessageEnhancingLog;
import com.top_logic.basic.Protocol;

/**
 * {@link MessageEnhancingLog} that appends the location within a {@link ConfigurationItem}.
 * 
 * @see #setLocation(ConfigurationItem)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogWithConfigLocation extends MessageEnhancingLog {

	private final Log _log;

	private ConfigurationItem _context;

	/**
	 * Creates a {@link LogWithConfigLocation}.
	 * 
	 * @param impl
	 *        The target {@link Protocol}.
	 */
	public LogWithConfigLocation(Log impl) {
		_log = impl;
	}

	@Override
	protected Log impl() {
		return _log;
	}

	/**
	 * Sets the {@link ConfigurationItem} to take the location from.
	 * 
	 * @param value
	 *        The {@link ConfigurationItem} currently processed.
	 * @return The location before.
	 * 
	 * @see ConfigurationItem#location()
	 */
	public ConfigurationItem setLocation(ConfigurationItem value) {
		ConfigurationItem before = _context;
		_context = value;
		return before;
	}

	@Override
	protected String enhance(String message) {
		if (_context == null) {
			return message;
		}

		Location location = _context.location();
		return strip(message) + " at " + location.getResource() + " line " + location.getLine() + " column "
			+ location.getColumn() + ".";
	}

	private String strip(String message) {
		if (message.endsWith(".")) {
			return message.substring(0, message.length() - 1);
		} else {
			return message;
		}
	}

}
