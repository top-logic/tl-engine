/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import com.top_logic.mig.html.HTMLFormatter;

/**
 * This class wraps a number interval
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class NumberInterval implements Interval{
	private double start;
	private double end;

	public NumberInterval(double aStart, double aEnd) {
		if (aEnd < aStart) {
			throw new IllegalArgumentException("Interval has invalid length < 0!");
		}
		this.start = aStart;
		this.end = aEnd;
	}

	@Override
	public Object getBegin() {
		return Double.valueOf(this.start);
	}

	@Override
	public Object getEnd() {
		return Double.valueOf(this.end);
	}

	@Override
	public String toString() {
		return HTMLFormatter.getInstance().formatNumber((Number)getBegin()) + " - "
				+ HTMLFormatter.getInstance().formatNumber((Number)getEnd());
	}
}
