/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;

/**
 * Parses raw data of {@link ControlCommand}s, that receive scrolling information of a table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class IndexViewportStateParser {
	
	private static final String COLUMN_ANCHOR = "columnAnchor";
	private static final String COLUMN_ANCHOR_OFFSET = "columnAnchorOffset";
	private static final String ROW_ANCHOR = "rowAnchor";
	private static final String ROW_ANCHOR_OFFSET = "rowAnchorOffset";
	
	/**
	 * {@link IndexViewportState} of table out of a suitable {@link ControlCommand}s
	 *         received raw data.
	 * 
	 * @throws ClassCastException
	 *         if raw data does not contain suitable information.
	 */
	public static final IndexViewportState getIndexViewportState(Map<String, Object> commandData) {
		IndexViewportState tableViewportState = new IndexViewportState();
		tableViewportState.setColumnAnchor(((Number) commandData.get(COLUMN_ANCHOR)).intValue());
		tableViewportState.setColumnOffset(((Number) commandData.get(COLUMN_ANCHOR_OFFSET)).intValue());
		tableViewportState.setRowAnchor(((Number) commandData.get(ROW_ANCHOR)).intValue());
		tableViewportState.setRowOffset(((Number) commandData.get(ROW_ANCHOR_OFFSET)).intValue());
		return tableViewportState;
	}

}
