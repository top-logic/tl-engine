/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.demo;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.top_logic.reporting.chart.demo.info.ImageOverviewInfo;
import com.top_logic.reporting.chart.renderer.OverviewRenderer;
import com.top_logic.reporting.chart.renderer.TemplateRenderer;

/**
 * The OverviewMain generates a example overview chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class OverviewMain {

    /** Path to the demo directory. */
    public static final String PATH  = "src/com/top_logic/reporting/chart/demo/";
    
    private static JFreeChart createChart(String aString, boolean withImage) {
        CategoryDataset dataset = createDataset();
        
        CategoryAxis categoryAxis = new CategoryAxis();
        
        SymbolAxis rangeAxis = new SymbolAxis("", new String[] {"M", "Q", "T"});
        rangeAxis.setGridBandsVisible(false);
        
        TemplateRenderer renderer = new OverviewRenderer();
        
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, rangeAxis, renderer);
        plot.setRangeGridlinesVisible (true);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(ChartColor.lightGray);
        
        JFreeChart result = new JFreeChart(aString, TextTitle.DEFAULT_FONT, plot, false);
        customizeChart(result, withImage);
        return result;
    }

    private static void customizeChart(JFreeChart aChart, boolean withImage) {
        aChart.setBackgroundPaint(Color.WHITE);
        aChart.getPlot().setBackgroundPaint(Color.WHITE);
        
        CategoryPlot     plot     = (CategoryPlot)aChart.getPlot();
        TemplateRenderer renderer = (TemplateRenderer)plot.getRenderer();
        renderer.setShapeGradientValue(40);
        
        if (withImage) {
            renderer.setShapeMinSize(35);
            renderer.setRenderingInfo(new ImageOverviewInfo());
        }
    }
    
    private static CategoryDataset createDataset() {
        /*
         * The values are the size indicators for the quadrates.
         * 0 no quadrate is displayed
         * 1 a quadrat is displayed.
         * 2 a quadrat is displayed which is greater than 0 and 1  
         * 3 a quadrat is displayed which is greater than 0, 1 and 2
         * 4 a quadrat is displayed which is greater than 0, 1, 2 and 3  
         * and so on
         * 
         * The rows are particularly treated. For each symbol on the y-axis a 
         * corresponding value must exist. The series have to start with "0" and
         * then "1", "2", "3", ... 
         * 
         * E.g. 
         *  y-axis = T, Q, M       => "0", "1", "2"
         *  y-axis = T, Q, M, X, Z => "0", "1", "2", "3", "4"
         *  
         * The categories are not special handled.  
         */
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1, "0", " ");
        dataset.addValue(2, "1", " ");
        dataset.addValue(3, "2", " ");

        dataset.addValue(2, "0", "  ");
        dataset.addValue(3, "1", "  ");
        dataset.addValue(2, "2", "  ");
        
        dataset.addValue(1, "0", "   ");
        dataset.addValue(0, "1", "   ");
        dataset.addValue(0, "2", "   ");
        
        return dataset;
    }
    
    /** 
     * This method generates a example overview chart.
     * 
     * @param args An array with arguments.
     */
    public static void main(String[] args) {
        JFreeChart chart = createChart("Modul 01", /* with image */ false); 
        try {
			ChartUtils.writeChartAsPNG(new FileOutputStream(new File(PATH + "overview/overview.png")), chart, 200, 200);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        JFreeChart imageChart = createChart("Modul 01", /* with image */ true);
        try {
			ChartUtils.writeChartAsPNG(new FileOutputStream(new File(PATH + "overview/imageOverview.png")), imageChart,
				400, 400);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

