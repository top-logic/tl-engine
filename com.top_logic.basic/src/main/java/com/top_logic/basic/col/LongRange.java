/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Range of long values.
 * 
 * @see LongRangeSet#range(long, long) Constructing ranges.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LongRange {

	/**
	 * The start of this range.
	 */
	long getStartValue();

	/**
	 * The end of this range (inclusive).
	 */
	long getEndValue();

}
