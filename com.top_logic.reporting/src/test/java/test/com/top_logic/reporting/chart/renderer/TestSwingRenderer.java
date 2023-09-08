/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.title.TextTitle;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.demo.SwingMain;
import com.top_logic.reporting.chart.demo.generator.DemoURLGenerator;
import com.top_logic.reporting.chart.info.swing.NegativeBarInfo;
import com.top_logic.reporting.chart.info.swing.NormalBarInfo;
import com.top_logic.reporting.chart.info.swing.OverwriteBarInfo;
import com.top_logic.reporting.chart.info.swing.PositiveBarInfo;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;
import com.top_logic.reporting.chart.renderer.SwingRenderer;
import com.top_logic.reporting.chart.util.SwingToolTipGenerator;

/**
 * testcase for the {@link SwingRenderer}.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestSwingRenderer extends TestCase {

    /**
     * Default way of creating a SwingChart
     */
    public void testMain() throws IOException {
        String        title  = "testSwingRenderer";
        int           width  = 600;
        int           height = 400;
        SwingDataset  sds    = SwingMain.createDataset2();
        SwingRenderer swr    = new SwingRenderer();
                
        /* Create one value axes. */
        double    bottomMargin = 0.00;
        double    topMargin    = 0.05;
        ValueAxis rangeAxis1 = new NumberAxis("TEUR");
        SwingMain.customizeValueAxis(sds, 0, rangeAxis1, SwingMain.AXIS_FULL, bottomMargin, topMargin);
        
        /* Create the category axis and set the label position to 90°. */
        CategoryAxis categoryAxis = new CategoryAxis();
        // categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        
        /*
         * Create the swing renderer and the swing plot. 
         */
        CategoryPlot plot = new CategoryPlot(sds, categoryAxis, rangeAxis1, swr);
        
        Color          color   = new Color(  0, 255, 0, /* transparency */ 25);
        IntervalMarker marker2 = new IntervalMarker(0, 8,
                                                    color, new BasicStroke(1.0f),
                                                    color, new BasicStroke(1.0f),
                                                    1f);
        plot.addRangeMarker(marker2);
        
        JFreeChart theChart = new JFreeChart(title, TextTitle.DEFAULT_FONT, plot, /* legende */ false);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(ChartColor.lightGray);
        
		swr.setDefaultToolTipGenerator(new SwingToolTipGenerator(new DecimalFormat("#,##0 TEUR")));
		swr.setDefaultItemURLGenerator(new DemoURLGenerator("http://www.testing.local"));
        swr.setToolTipWithAxes(true);
        
        theChart.setBackgroundPaint(Color.WHITE); // default is grey
        
        ChartRenderingInfo renderingInfo = new ChartRenderingInfo();
        File             pngFile = BasicTestCase.createNamedTestFile("testSwingRenderer.png");
        FileOutputStream out     = new FileOutputStream(pngFile);
		ChartUtils.writeChartAsPNG(out, theChart, width, height, renderingInfo);
		String imageMap = ChartUtils.getImageMap(
                                            "imageMap", 
                                            renderingInfo,
                                            new StandardToolTipTagFragmentGenerator(), 
                                            new StandardURLTagFragmentGenerator());
        // System.out.println(imageMap);
        assertTrue("Ticket #5305: TEUR is not contained in imageMap", imageMap.indexOf(" TEUR\"") > 0);
        assertTrue(imageMap.indexOf("http://www.testing.local") > 0);
    }

    /**
     * Create a demo dataset for testShowAsIcon.
     */
    public static SwingDataset createIconDataset() {
        SwingDataset dataset = new SwingDataset();
        
        /*
         * - Note the series starts with '0' and then '1', '2', ...
         *   The series label are not displayed.
         * - The category labels must be unique!
         *   The category lables are displayed.
         * - Do not forget to set the values to the correct axis.
         */
		Comparable series0 = Integer.valueOf(0);
		Comparable series1 = Integer.valueOf(1);
        
        // Comparable series0 = "0";
        // Comparable series1 = "1";
        
        SwingRenderingInfo firstbar = new SwingRenderingInfo(10.0 , /* lineToPrev */ true , /* icon */ true , SwingRenderer.RIGHT);
        SwingRenderingInfo lasttbar = new SwingRenderingInfo(8.0  , /* lineToPrev */ true , /* icon */ true , SwingRenderer.LEFT);
        
        int i = 0; /* Unique category name which are not displayed. */
                      // value, series, "Unique Label" , BarInfos
        dataset.setValue( 10.0, series0, "Planwert 'alt'"             , firstbar);
        dataset.setValue(- 2.0, series0, StringServices.getSpaces(++i), new NegativeBarInfo (- 2.0));
        dataset.setValue(  4.0, series0, StringServices.getSpaces(++i), new PositiveBarInfo (  4.0));
        dataset.setValue( 12.0, series0, "Planwert 'akt'"             , new NormalBarInfo   ( 12.0));
        dataset.setValue(- 5.0, series0, StringServices.getSpaces(++i), new NegativeBarInfo (- 5.0));
        dataset.setValue(- 1.0, series1, StringServices.getSpaces(  i), new NegativeBarInfo (- 1.0));
        dataset.setValue(  3.0, series0, StringServices.getSpaces(++i), new PositiveBarInfo (  3.0));
        dataset.setValue(  3.0, series1, StringServices.getSpaces(  i), new PositiveBarInfo (  3.0));
        dataset.setValue( 10.0, series0, "Planwert 'Prog'"            , new NormalBarInfo   ( 10.0));
        dataset.setValue(  2.0, series1, "Planwert 'Prog'"            , new OverwriteBarInfo(- 2.0, Color.RED));
        dataset.setValue(  8.0, series0, "Zielwert"                   , lasttbar);
        
        return dataset;
    }

    /**
     * Test SwingChart using the showAsIcon flag.
     */
    public void testShowAsIcon() throws IOException {
        String        title  = "testShowAsIcon";
        int           width  = 577;
        int           height = 411;
        SwingDataset  sds    = createIconDataset();
        SwingRenderer swr    = new SwingRenderer();
        
        /* Create one value axes. */
        double    bottomMargin = 0.00;
        double    topMargin    = 0.05;
        ValueAxis rangeAxis1 = new NumberAxis("TEUR");
        SwingMain.customizeValueAxis(sds, 0, rangeAxis1, SwingMain.AXIS_FULL, bottomMargin, topMargin);
        
        /* Create the category axis and set the label position to 90°. */
        CategoryAxis categoryAxis = new CategoryAxis();
        // categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        
        /*
         * Create the swing renderer and the swing plot. 
         */
        CategoryPlot plot = new CategoryPlot(sds, categoryAxis, rangeAxis1, swr);
        
        Color          color   = new Color(  0, 255, 0, /* transparency */ 25);
        IntervalMarker marker2 = new IntervalMarker(0, 8,
                                                    color, new BasicStroke(1.0f),
                                                    color, new BasicStroke(1.0f),
                                                    1f);
        plot.addRangeMarker(marker2);
        
        JFreeChart theChart = new JFreeChart(title, TextTitle.DEFAULT_FONT, plot, /* legende */ false);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(ChartColor.lightGray);
        
		swr.setDefaultToolTipGenerator(new SwingToolTipGenerator(new DecimalFormat("#,##0 TEUR")));
		swr.setDefaultItemURLGenerator(new DemoURLGenerator("http://www.testing.local"));
        swr.setToolTipWithAxes(true);
        
        theChart.setBackgroundPaint(Color.WHITE); // default is grey
        
        ChartRenderingInfo renderingInfo = new ChartRenderingInfo();
        File             pngFile = BasicTestCase.createNamedTestFile("testShowAsIcon.png");
        FileOutputStream out     = new FileOutputStream(pngFile);
		ChartUtils.writeChartAsPNG(out, theChart, width, height, renderingInfo);
		String imageMap = ChartUtils.getImageMap(
                                            "imageMap", 
                                            renderingInfo,
                                            new StandardToolTipTagFragmentGenerator(), 
                                            new StandardURLTagFragmentGenerator());
        // System.out.println(imageMap);
        assertTrue("Ticket #5305: TEUR is not contained in imageMap", imageMap.indexOf(" TEUR\"") > 0);
        assertTrue(imageMap.indexOf("http://www.testing.local") > 0);
    }

    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestSwingRenderer.class);
    }
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite());
    }
    
}
