/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.model;

/**
 * Possible time intervals.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public enum TimeGranularity {

	/**
	 * Show complete years on the x-axis.
	 */
	YEAR,

	/**
	 * Show quarters on the x-axis.
	 */
	QUARTER,

	/**
	 * Show quarters on the x-axis.
	 */
	MONTH,

	/**
	 * Show weeks on the x-axis.
	 */
	WEEK,

	/**
	 * Show each single day on the x-axis.
	 */
	DAY;
}