/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.criteria.interval;

import com.top_logic.reporting.report.model.partition.criteria.Criteria;

/**
 * This is the base class for all criterias that work on any kind of intervals.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public interface IntervalCriteria extends Criteria {

	/**
	 * Returns the begin of the interval.
	 */
	public Object getBegin();

	/**
	 * Returns the end of the interval.
	 */
	public Object getEnd();

	/**
	 * Returns the granularity of the criteria. The granularity describes the size of subintervals that
	 * might be created.
	 */
	public Object getGranularity();

	/**
	 * Returns any additional settings an interval criteria might need. Can be <code>null</code>.
	 */
	public Object getAdditionalSettings();

}
