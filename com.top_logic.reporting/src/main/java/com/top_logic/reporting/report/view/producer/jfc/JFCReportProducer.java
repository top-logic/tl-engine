/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer.jfc;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.report.exception.UnsupportedException;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.util.ReportUtilities;

/**
 * The JFCReportProducer renders the report as chart. The JFCReportProducer 
 * uses the JFreeChart library to render the charts..
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public abstract class JFCReportProducer implements ReportConstants{

    /** See {@link #getReportType()}. */
    private String reportType;

    /** 
     * Creates a {@link JFCReportProducer}.
     */
    protected JFCReportProducer(String anReportType) {
        if (anReportType == null || anReportType.length() == 0) {
			throw new IllegalArgumentException("The identifier must NOT be null or empty!");
        }
        
        this.reportType = anReportType;
    }

    /**
     * This method returns the {@link JFreeChart} (e.g. 'bar chart' or 
     * 'pie chart').
     * 
     * @param aReport
     *        The report.
     */
    public abstract JFreeChart getJFreeChart(Report aReport);
    
    /**
     * Produces the given {@link Report}.
     * 
     * @param aReport
     *        The value to render. 
     *        Must not be <code>null</code>.
     */
    public JFreeChart produce(Report aReport) {
        JFreeChart theChart = getJFreeChart(aReport);
        theChart.setAntiAlias(aReport.isAntiAlias());
        Color chartBackground = aReport.getChartBackground();
        theChart.setBackgroundPaint(chartBackground);
        theChart.getPlot().setBackgroundPaint(chartBackground);
        
        configureTitle (theChart, aReport);
        configureLegend(theChart, aReport);
        
        return theChart;
    }

    private void configureTitle(JFreeChart aChart, Report aReport) {
        if (!aReport.isTitleVisible()) {
            return; 
        }
        
        // Set text
        TextTitle theTitle = aChart.getTitle();
        theTitle.setText(aReport.getTitleMessage());
        theTitle.setHorizontalAlignment(getJFCHorizontalAlignment(aReport.getTitleAlign()));
        
        // Set font
        Font theFont = aReport.getTitleFont();
        if (theFont != null) {
            theTitle.setFont(theFont);
        }
        
    }
    
    private void configureLegend(JFreeChart aChart, Report aReport) {
        if (!aReport.isLegendVisible()) {
            return; 
        }
        
        LegendTitle theLegend = aChart.getLegend();
        
        // Set alignment
        String theLegendAlign = aReport.getLegendAlign();
        if(!StringServices.isEmpty(theLegendAlign)) {
            theLegend.setPosition(ReportUtilities.getLegendAlign(theLegendAlign));
        }
        
        // Set font
        Font theLegendFont = aReport.getLegendFont();
        if (theLegendFont != null) {
            theLegend.setItemFont(theLegendFont);
        }
    }

    /**
     * This method returns the JFreeChart plot orientation for the given
     * orientation string. 
     * 
     * See {@link ReportConstants}.
     */
    protected PlotOrientation getPlotOrientation(String anOrientation) {
        if (anOrientation.equalsIgnoreCase(ORIENTATION_HORIZONTAL)) {
            return PlotOrientation.HORIZONTAL;
        }
        if (anOrientation.equalsIgnoreCase(ORIENTATION_VERTICAL)) {
            return PlotOrientation.VERTICAL;
        }
        
        throw new UnsupportedException(this.getClass(), "The orientation '" + anOrientation + "' is not supported. Only '" + ORIENTATION_HORIZONTAL + "' and '" + ORIENTATION_VERTICAL + "' are supported.");
    }
    
    /**
     * This method returns the JFreeChart horizontal alignment for the given
     * alignment string. 
     * 
     * See {@link ReportConstants}.
     */
    protected HorizontalAlignment getJFCHorizontalAlignment(String anAlign) {
        if (anAlign.equalsIgnoreCase(ALIGN_LEFT)) {
            return HorizontalAlignment.LEFT;
        }
        if (anAlign.equalsIgnoreCase(ALIGN_CENTER)) {
            return HorizontalAlignment.CENTER;
        }
        if (anAlign.equalsIgnoreCase(ALIGN_RIGHT)) {
            return HorizontalAlignment.RIGHT;
        }
        
        throw new UnsupportedException(this.getClass(), "The alignment '" + anAlign + "' is not supported.Only '" + ALIGN_LEFT + ", '" + ALIGN_CENTER + "' and '" + ALIGN_RIGHT + "' are supported.");
    }
    
    /**
     * This method returns the report type. This renderer can be used for all
     * reports with this report type. The report type must NOT be null or empty.
     */
    public String getReportType() {
        return this.reportType;
    }
    
    @Override
	public String toString() {
        return this.getClass() + "[identifier=" + this.reportType + "]";
    }
    
}

