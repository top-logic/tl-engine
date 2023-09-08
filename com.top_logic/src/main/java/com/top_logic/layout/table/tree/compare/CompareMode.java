/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

/**
 * How to display the comparison between two table models.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public enum CompareMode {

	/** Comparison result will be displayed in a table with common columns */
	OVERLAY,

	/** Comparison result will be displayed in two separate table parts */
	SIDE_BY_SIDE

}
