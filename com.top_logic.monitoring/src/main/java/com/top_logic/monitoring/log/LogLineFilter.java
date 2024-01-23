/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import java.util.Map;

/**
 * Filters the unparsed log lines in the parser, before they are being parsed and objectified.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public interface LogLineFilter {

	/**
	 * Whether the log line should be accepted.
	 * <p>
	 * The {@link Map} entries are the unparsed properties of the {@link LogLine}. For example:
	 * {@link LogLine#PROPERTY_TIME}
	 * </p>
	 * 
	 * @implNote The filter works on the unparsed log lines, not the {@link LogLine} objects, to
	 *           optimize performance: Logs can be huge. Parsing the properties of a line and
	 *           creating an object out of them is a relevant part of the consumed time.
	 */
	boolean accept(Map<String, Object> unparsedLine);

}
