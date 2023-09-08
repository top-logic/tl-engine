/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * Default implementation of {@link SingleOptionMatchCounter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultSingleOptionMatchCounter implements SingleOptionMatchCounter {

	private int matchCount;

	/**
	 * Create a new {@link DefaultSingleOptionMatchCounter}
	 */
	public DefaultSingleOptionMatchCounter() {
		matchCount = 0;
	}

	@Override
	public void increaseCounter() {
		matchCount++;
	}

	@Override
	public int getMatchCount() {
		return matchCount;
	}
}
