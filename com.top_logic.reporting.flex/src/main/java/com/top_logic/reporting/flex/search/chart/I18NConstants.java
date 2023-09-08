/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.chart;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey DATE_PARTITION_FIRST = legacyKey("timeseries.chart.constraint.datePartitionFirst");

	public static ResKey EXCEEDS_MAX_DIMENSIONS__MIN_MIN1_MAX_MAX1_PARTITIONS_AGGREGATIONS;

	public static ResKey UNDERCUTS_MIN_DIMENSIONS__MIN_MIN1_MAX_MAX1_PARTITIONS_AGGREGATIONS;

	public static ResPrefix COLOR_TABLE;

	static {
		initConstants(I18NConstants.class);
	}
}
