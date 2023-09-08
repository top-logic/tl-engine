/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer.jfc;

import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.base.chart.configurator.BarChartConfigurator;
import com.top_logic.base.chart.renderer.DifferentSeriesColorsBarRenderer;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.util.ReportUtilities;

/**
 * The JFCBarChartProducer renders a report as a bar chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class JFCBarChartProducer extends JFCReportProducer {

    /** The single instance of this class. */
    public static final JFCBarChartProducer INSTANCE = new JFCBarChartProducer();

    /** 
     * Creates a {@link JFCPieChartProducer}.
     */
    private JFCBarChartProducer() {
        super(ReportUtilities.REPORT_TYPE_CHART);
    }

    @Override
	public JFreeChart getJFreeChart(Report aReport) {
        PlotOrientation theOrientation   = getPlotOrientation(aReport.getOrientation());
        boolean         isLegendVisible  = aReport.isLegendVisible();
        String          theTitleMessage  = aReport.isTitleVisible()            ? aReport.getTitleMessage()  : "";
        String          theRangeMessage  = aReport.isRangeAxisMessageVisible() ? aReport.getRangeMessage()  : "";
        String          theDomainMessage = aReport.isDomainAxisMessageVisible()? aReport.getDomainMessage() : "";

        CategoryDataset theDataset = ReportUtilities.generateCategoryDatasetFor(aReport);
        JFreeChart      theChart   = ChartFactory.createBarChart(theTitleMessage, theDomainMessage, theRangeMessage, theDataset, theOrientation, /* Legend */ isLegendVisible, /* Tooltips */ false, /* Urls */ false);
        
        configureGridLines(theChart, aReport);
        configureRangeAxisFonts(theChart, aReport);
        configureDomainAxisFonts(theChart, aReport);
        
        BarChartConfigurator theConfigurator = new BarChartConfigurator(theChart);
        if (aReport.isUseGradientPaint()) {
            theConfigurator.useGradientPaint();
        }

        if (!aReport.isUseSameCategoryColor()) {
            DifferentSeriesColorsBarRenderer theDiffSeriesColorsRenderer = new DifferentSeriesColorsBarRenderer();
            if (!aReport.isUseGradientPaint()) {
                theDiffSeriesColorsRenderer.setGradientPaint(false);
            }
            
            theChart.getCategoryPlot().setRenderer(theDiffSeriesColorsRenderer);
        }
        
        if (aReport.isUseIntValuesForRangeAxis()) {
            theConfigurator.useIntegerValuesForRangeAxis();
        }
        
        theConfigurator.setItemLabelGenerator(aReport.getCategoryItemLabelGenerator());
        theConfigurator.setItemLabelsVisible(aReport.isShowItemLabels());
        
        return theChart;
    }

    private void configureGridLines(JFreeChart aChart, Report aReport) {
        CategoryPlot theCategoryPlot = aChart.getCategoryPlot();
        theCategoryPlot.setRangeGridlinesVisible(aReport.isRangeAxisGridLineVisible());
        theCategoryPlot.setDomainGridlinesVisible(aReport.isDomainAxisGridLineVisible());
    }
    
    private void configureRangeAxisFonts(JFreeChart aChart, Report aReport) {
        CategoryPlot theCategoryPlot = aChart.getCategoryPlot();
        
        Font theFont = aReport.getRangeAxisFont();
        if (theFont != null) {
            theCategoryPlot.getRangeAxis().setLabelFont(theFont);
        }

        Font theValueFont = aReport.getRangeAxisValueFont();
        if(theValueFont != null) {
            theCategoryPlot.getRangeAxis().setTickLabelFont(theValueFont);
        }
    }
    
    private void configureDomainAxisFonts(JFreeChart aChart, Report aReport) {
        CategoryPlot theCategoryPlot = aChart.getCategoryPlot();
        
        Font theFont = aReport.getDomainAxisFont();
        if (theFont != null) {
            theCategoryPlot.getDomainAxis().setLabelFont(theFont);
        }
        
        Font theValueFont = aReport.getDomainAxisValueFont();
        if (theValueFont != null) {
            theCategoryPlot.getDomainAxis().setTickLabelFont(theValueFont);
        }
    }

}

