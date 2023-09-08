/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;

/**
 * {@link Comparator} comparing {@link DateInterval}s by their begin.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
@Deprecated
public class DateIntervalComparator implements Comparator<DateInterval> {

	@Override
	public int compare(DateInterval o1, DateInterval o2) {
		if (o1 == null && o2 == null) return 0;
		if (o1 == null) return -1;
		if (o2 == null) return +1;
		Object b1 = o1.getBegin();
		Object b2 = o2.getBegin();
		if (b1 == null && b2 == null) return 0;
		if (b1 == null) return -1;
		if (b2 == null) return +1;
		return ComparableComparator.INSTANCE.compare(b1, b2);
	}

}
