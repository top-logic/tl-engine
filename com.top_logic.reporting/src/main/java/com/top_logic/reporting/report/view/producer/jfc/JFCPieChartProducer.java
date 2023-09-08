/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer.jfc;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.PieDataset;

import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.util.ReportUtilities;

/**
 * The JFCPieChartProducer renders a report as a pie chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class JFCPieChartProducer extends JFCReportProducer {

    /** The single instance of this class. */
    public static final JFCPieChartProducer INSTANCE = new JFCPieChartProducer();

    /** 
     * Creates a {@link JFCPieChartProducer}.
     */
    private JFCPieChartProducer() {
        super(ReportUtilities.REPORT_TYPE_CHART);
    }

    @Override
	public JFreeChart getJFreeChart(Report aReport) {
        PieDataset      theDataset      = ReportUtilities.generatePieDatasetFor(aReport);
        boolean         isLegendVisible = aReport.isLegendVisible();
        String          theTitleMessage = aReport.isTitleVisible() ? aReport.getTitleMessage() : "";
        
        JFreeChart      theChart        = ChartFactory.createPieChart(theTitleMessage, theDataset, /* Legend */ isLegendVisible, /* Tooltips */ false, /* Urls */ false);
        
        return theChart;
    }

}

