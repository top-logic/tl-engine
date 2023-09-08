/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.partition.criteria.interval;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * The NumberIntervalCriteria that works on numbers.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class NumberIntervalCriteria implements IntervalCriteria {
	private Number	begin;
	private Number	end;
	private Number	granularity;
	private Map		additionalSettings;

	/**
	 * Creates a {@link NumberIntervalCriteria}.
	 * 
	 */
	public NumberIntervalCriteria( Number aBegin, Number anEnd ) {
		this(aBegin, anEnd, Integer.valueOf(24), new HashMap());
	}

	public NumberIntervalCriteria( Number aBegin, Number anEnd, Number aGranularity ) {
		this(aBegin, anEnd, aGranularity, new HashMap());
	}

	public NumberIntervalCriteria( Number aBegin, Number anEnd, Number aGranularity, Map someSettings ) {
		this.begin = aBegin;
		this.end = anEnd;
		this.granularity = aGranularity;
		this.additionalSettings = someSettings;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getBegin()
	 */
	@Override
	public Object getBegin() {
		return this.begin;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getEnd()
	 */
	@Override
	public Object getEnd() {
		return this.end;
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.Criteria#getCriteriaTyp()
	 */
	@Override
	public String getCriteriaTyp() {
		return "number-interval-criteria";
	}

	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getGranularity()
	 */
	@Override
	public Object getGranularity() {
		return this.granularity;
	}
	
	
	/**
	 * @see com.top_logic.reporting.report.model.partition.criteria.interval.IntervalCriteria#getAdditionalSettings()
	 */
	@Override
	public Object getAdditionalSettings() {
		return this.additionalSettings;
	}
	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + NumberFormat.getInstance().format(this.begin) + ";" + NumberFormat.getInstance().format(this.end) + "]";
	}

}
