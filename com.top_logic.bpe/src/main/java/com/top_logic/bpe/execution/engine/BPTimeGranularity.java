/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

/**
 * Unit of a time span in a process.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public enum BPTimeGranularity {

	/** Time span measured in days. */
	DAY(24 * 60 * 60 * 1000),

	/** Time span measured in hours. */
	HOUR(60 * 60 * 1000),

	/** Time span measured in minutes. */
	MINUTE(60 * 1000),

	/** Time span measured in seconds. */
	SECOND(1000);

	private long _milliFactor;

	BPTimeGranularity(long milliFactor) {
		_milliFactor = milliFactor;
	}

	/**
	 * Converts the given value in this unit to milliseconds.
	 * 
	 * @param val
	 *        a value in the granularity of this {@link BPTimeGranularity}
	 * @return the some value converted to milliseconds
	 */
	public long convertToMillis(int val) {
		return _milliFactor * val;
	}

}
