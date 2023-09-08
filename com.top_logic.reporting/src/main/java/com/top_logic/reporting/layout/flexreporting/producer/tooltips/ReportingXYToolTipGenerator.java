/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.producer.tooltips;

import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.top_logic.reporting.report.util.DateConstants;

/**
 * ToolTipGenerator for Charts in Reporting.
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ReportingXYToolTipGenerator extends ReportingTooltipGenerator implements XYToolTipGenerator {

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		String rowKey   = (String) dataset.getSeriesKey(series);
		Number cKey     = dataset.getX(series, item);
		Number theVal   = dataset.getYValue(series, item);
		String theRange = cKey.toString();
		
		if(dataset instanceof TimeSeriesCollection) {
			Date              theDate   = new Date(cKey.longValue());
			RegularTimePeriod thePeriod = ((TimeSeriesCollection)dataset).getSeries(series).getDataItem(item).getPeriod();
			DateFormat        theFormat = DateConstants.getDateFormatForTooltip(thePeriod.getClass());
			theRange = theFormat.format(theDate);
		}
		
		return buildTooltip(rowKey, theRange, theVal);
	}
}
