/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.List;

/**
 * The basic class for interval providers. An interval is constructed from a start, an end
 * and a granularity.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public abstract class IntervalProvider {
	
    /**
	 * Maximum number of possible intervals. 
	 * 1) makes no sense to have more than 800 intervals, its not possible to display 
	 *    them on normal resulution 
	 * 2) too many intervals can cause an {@link OutOfMemoryError}
	 */
	public static final double MAX_INTERVALS = 100.0;
	
	private final Object granularity;
	
	public IntervalProvider(Object aGranularity) {
	    this.granularity = aGranularity;
	}
	
	public abstract List getIntervals(Object aStart, Object anEnd);
	
	public Object getGranularity() {
		return this.granularity;
	}
}
