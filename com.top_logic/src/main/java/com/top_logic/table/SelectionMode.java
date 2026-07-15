/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Whether a table allows a single or multiple selected rows.
 */
public enum SelectionMode {

	/** At most one row may be selected. */
	SINGLE,

	/** Any number of rows may be selected. */
	MULTI;

}
