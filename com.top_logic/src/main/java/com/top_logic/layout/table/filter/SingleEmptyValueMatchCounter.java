/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link SingleOptionMatchCounter}, that does not really count, but returning always the empty value.
 * 
 * @see #EMPTY_VALUE
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SingleEmptyValueMatchCounter implements SingleOptionMatchCounter {

	/** Singleton instance of {@link SingleEmptyValueMatchCounter} */
	public static final SingleOptionMatchCounter INSTANCE = new SingleEmptyValueMatchCounter();

	/** Constant, that indicates that filter matches has not been counted until yet */
	public static final int EMPTY_VALUE = -1;

	@Override
	public void increaseCounter() {
		// Nothing to do
	}

	@Override
	public int getMatchCount() {
		return EMPTY_VALUE;
	}
}
