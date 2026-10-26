/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * The side of its cell a {@link Column} puts what it shows on.
 *
 * <p>
 * The side is named by where reading starts and ends, not by left and right, so that it holds in
 * both reading directions.
 * </p>
 *
 * @see Column#alignment()
 */
public enum ColumnAlignment {

	/** Content starts at the beginning of the cell, as text does. */
	START,

	/** Content ends at the end of the cell, as numbers do, so that their digits line up. */
	END;

}
