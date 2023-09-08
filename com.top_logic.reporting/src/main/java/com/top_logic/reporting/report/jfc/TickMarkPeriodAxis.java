/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.jfc;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;

import com.top_logic.layout.basic.DefaultDisplayContext;

/**
 * The TickMarkPeriodAxis is a normal {@link PeriodAxis} but it fixed 
 * the always visible tick marks problem.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class TickMarkPeriodAxis extends PeriodAxis {

    public TickMarkPeriodAxis(String aLabel) {
		this(aLabel, new Day(), new Day());
    }

	public TickMarkPeriodAxis(String aLabel, RegularTimePeriod aFirst, RegularTimePeriod aLast, TimeZone aTimeZone,
			Locale locale) {
		super(aLabel, aFirst, aLast, aTimeZone, locale);
    }

    public TickMarkPeriodAxis(String aLabel, RegularTimePeriod aFirst, RegularTimePeriod aLast) {
		this(aLabel, aFirst, aLast, userTimeZone(), userLocale());
    }

	private static Locale userLocale() {
		return DefaultDisplayContext.getDisplayContext().getSubSessionContext().getCurrentLocale();
	}

	private static TimeZone userTimeZone() {
		return DefaultDisplayContext.getDisplayContext().getSubSessionContext().getCurrentTimeZone();
	}

	@Override
	protected void drawTickMarks(Graphics2D aG2, AxisState aState, Rectangle2D aDataArea, RectangleEdge aEdge) {
     if (isTickMarksVisible()) {
         super.drawTickMarks(aG2, aState, aDataArea, aEdge);
     }
    }
    
}

