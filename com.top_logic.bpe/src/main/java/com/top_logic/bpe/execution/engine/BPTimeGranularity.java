/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public enum BPTimeGranularity {

	DAY(24 * 60 * 60 * 1000), HOUR(60 * 60 * 1000), MINUTE(60 * 1000), SECOND(1000);

	private long _milliFactor;

	BPTimeGranularity(long milliFactor) {
		_milliFactor = milliFactor;
	}

	/**
	 * @param val
	 *        a value in the granularity of this BPTimeGranularity
	 * @return the some value converted to milliseconds
	 */
	public long convertToMillis(int val) {
		return _milliFactor * val;
	}

}
