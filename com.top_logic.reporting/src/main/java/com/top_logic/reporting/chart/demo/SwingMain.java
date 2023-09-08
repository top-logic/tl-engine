/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;

import com.top_logic.basic.StringServices;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.dataset.SwingDatasetUtilities;
import com.top_logic.reporting.chart.demo.generator.DemoURLGenerator;
import com.top_logic.reporting.chart.info.swing.NegativeBarInfo;
import com.top_logic.reporting.chart.info.swing.NormalBarIconInfo;
import com.top_logic.reporting.chart.info.swing.NormalBarInfo;
import com.top_logic.reporting.chart.info.swing.OverwriteBarInfo;
import com.top_logic.reporting.chart.info.swing.PositiveBarInfo;
import com.top_logic.reporting.chart.plot.SwingPlot;
import com.top_logic.reporting.chart.renderer.SwingRenderer;
import com.top_logic.reporting.chart.util.SwingToolTipGenerator;

/**
 * The SwingMain shows with simple examples how the 
 * {@link com.top_logic.reporting.chart.renderer.SwingRenderer} and its 
 * {@link com.top_logic.reporting.chart.dataset.SwingDataset} works. 
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingMain {

    /** Path to the demo directory. */
    public static final String PATH  = "src/com/top_logic/reporting/chart/demo/swing/";
    /** Constant for right. > */
    public static final int    RIGHT = SwingRenderer.RIGHT; 
    /** Constant for left.  < */
    public static final int    LEFT  = SwingRenderer.LEFT;
    
    /** Indicate that the axis is completely drawn with line and ticks. */
    public static final boolean AXIS_FULL  = true;
    /** Indicate that only the axis label is drawn. */
    public static final boolean AXIS_LABEL = false;
    
    
    /**
     * This method returns a swing chart with three axes.
     * 
     * @param  aTitle A chart title.
     * @return Returns a swing chart with three axes.
     */
    private static JFreeChart createChart1(String aTitle) {
        SwingDataset dataset = createDataset1();
        SwingDatasetUtilities.synchronizeYAxes(dataset, 3);
        SwingDataset dataset2 = createDataset1();
        
        /* Create three value axes. */
        double bottomMargin = 0.05;
        double topMargin    = 0.05;
        ValueAxis rangeAxis1 = new NumberAxis("TEUR");
        customizeValueAxis(dataset, 0, rangeAxis1, AXIS_FULL, bottomMargin, topMargin);
        ValueAxis rangeAxis2 = new NumberAxis("Kilo");
        customizeValueAxis(dataset, 1, rangeAxis2, AXIS_FULL, bottomMargin, topMargin);
        ValueAxis rangeAxis3 = new NumberAxis("Hotdogs");
        customizeValueAxis(dataset, 2, rangeAxis3, AXIS_FULL, bottomMargin, topMargin);
        
        /* Create the category axis and set the label position to 45°. */
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        
        /*
         * Create the swing renderer and the swing plot. Set the axes to the
         * plot. The first axis is completely drawn. The both others are drawn
         * only as label.
         */
        SwingRenderer renderer = new SwingRenderer();
        SwingPlot plot = new SwingPlot(dataset, dataset2, categoryAxis, rangeAxis1, renderer);
        plot.setRangeAxis(1, rangeAxis2);
        plot.setRangeAxis(2, rangeAxis3);

        String goalLabel      = "Zielvorgabe";
        Color  overGoalColor  = new Color(255,   0, 0, 20);
        Color  underGoalColor = new Color(  0, 255, 0, 20);
        Range  range          = SwingDatasetUtilities.findRange(dataset, 0, bottomMargin, 
                                                                topMargin);
        configureMarker(dataset, range, bottomMargin, topMargin, plot, goalLabel, 
                        underGoalColor);
        
        IntervalMarker marker2 = new IntervalMarker(0, range.getUpperBound(),
                                                    overGoalColor, new BasicStroke(1.0f),
                                                    overGoalColor, new BasicStroke(1.0f),
                                                    1f);
        plot.addRangeMarker(marker2);
        
        JFreeChart aChart = new JFreeChart(aTitle, TextTitle.DEFAULT_FONT, plot, false);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(ChartColor.lightGray);
        
		renderer.setDefaultToolTipGenerator(new SwingToolTipGenerator());
		renderer.setDefaultItemURLGenerator(new DemoURLGenerator());
        renderer.setToolTipWithAxes(true);
        
        aChart.setBackgroundPaint(Color.WHITE);
        return aChart;
    }
    
    /**
     * This method returns for the first swing chart a swing dataset with three
     * units. 
     * 
     * @return Returns for the first swing chart a swing dataset with three
     *         units.
     */
    private static SwingDataset createDataset1() {
        SwingDataset dataset = new SwingDataset();
        
        /*
         * - Note the series starts with '0' and then '1', '2', ...
         *   The series label are not displayed.
         * - The category labels must be unique!
         *   The category lables are displayed.
         * - Do not forget to set the values to the correct axis.
         */
        int i = 0; /* Unique category name which are not displayed. */
        dataset.setValue(    10.0, "0", getSpaces(++i), new NormalBarIconInfo(    10.0, 0, RIGHT));
        dataset.setValue(     5.0, "0", getSpaces(++i), new PositiveBarInfo  (     5.0, 0));
        dataset.setValue(     5.0, "0", "HKprop",       new PositiveBarInfo  (     5.0, 0));
        dataset.setValue(    10.0, "1", "HKprop",       new PositiveBarInfo  (    10.0, 0));
        dataset.setValue(-   30.0, "0", getSpaces(++i), new NegativeBarInfo  (-   30.0, 0));
        dataset.setValue(-   10.0, "1", getSpaces(  i), new NegativeBarInfo  (-   10.0, 0));
        dataset.setValue(-   10.0, "0", getSpaces(++i), new NormalBarIconInfo(-   10.0, 0, LEFT));
        
        dataset.setValue(-  100.0, "0", getSpaces(++i), new NormalBarIconInfo(-  100.0, 1, RIGHT));
        dataset.setValue(-   50.0, "0", getSpaces(++i), new NegativeBarInfo  (-   50.0, 1));
        dataset.setValue(    50.0, "0", "Invest",       new PositiveBarInfo  (    50.0, 1));
        dataset.setValue(   100.0, "1", "Invest",       new PositiveBarInfo  (   100.0, 1));
        dataset.setValue(-  300.0, "0", getSpaces(++i), new NegativeBarInfo  (-  300.0, 1));
        dataset.setValue(-  100.0, "1", getSpaces(  i), new NegativeBarInfo  (-  100.0, 1));
        dataset.setValue(-  400.0, "0", getSpaces(++i), new NormalBarIconInfo(-  400.0, 1, LEFT));
        
        dataset.setValue( 10000.0, "0", getSpaces(++i), new NormalBarIconInfo( 10000.0, 2, RIGHT));
        dataset.setValue(  5000.0, "0", getSpaces(++i), new PositiveBarInfo  (  5000.0, 2));
        dataset.setValue(  5000.0, "0", "Entwicklung" , new PositiveBarInfo  (  5000.0, 2));
        dataset.setValue( 10000.0, "1", "Entwicklung",  new PositiveBarInfo  ( 10000.0, 2));
        dataset.setValue(- 5000.0, "0", getSpaces(++i), new NegativeBarInfo  ( -5000.0, 2));
        dataset.setValue(-10000.0, "1", getSpaces(  i), new NegativeBarInfo  (-10000.0, 2));
        dataset.setValue( 15000.0, "0", getSpaces(++i), new NormalBarIconInfo( 15000.0, 2, LEFT));
        
        return dataset;
    }
    
    /**
     * This method returns a swing chart with one axes.
     * 
     * @param  aString A chart title.
     * @return Returns a swing chart with one axes.
     */
    private static JFreeChart createChart2(String aString) {
        SwingDataset dataset = createDataset2();
        
        /* Create one value axes. */
        double bottomMargin = 0.00;
        double topMargin    = 0.05;
        ValueAxis rangeAxis1 = new NumberAxis("TEUR");
        customizeValueAxis(dataset, 0, rangeAxis1, AXIS_FULL, bottomMargin, topMargin);
        
        /* Create the category axis and set the label position to 45°. */
        CategoryAxis categoryAxis = new CategoryAxis();
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        
        /*
         * Create the swing renderer and the swing plot. 
         */
        SwingRenderer renderer = new SwingRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, rangeAxis1, renderer);
        
        Color          color   = new Color(  0, 255, 0, 15);
        IntervalMarker marker2 = new IntervalMarker(0, 8,
                                                    color, new BasicStroke(1.0f),
                                                    color, new BasicStroke(1.0f),
                                                    1f);
        plot.addRangeMarker(marker2);
        
        JFreeChart aChart = new JFreeChart(aString, TextTitle.DEFAULT_FONT, plot, false);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(ChartColor.lightGray);
        
		renderer.setDefaultToolTipGenerator(new SwingToolTipGenerator());
		renderer.setDefaultItemURLGenerator(new DemoURLGenerator());
        renderer.setToolTipWithAxes(true);
        
        aChart.setBackgroundPaint(Color.WHITE);
        return aChart;
    }

    /**
     * This method returns for the second swing chart a swing dataset with one
     * big units.
     * 
     * @return Returns for the second swing chart a swing dataset with one big
     *         units.
     */
    public static SwingDataset createDataset2() {
        SwingDataset dataset = new SwingDataset();
        
        /*
         * - Note the series starts with '0' and then '1', '2', ...
         *   The series label are not displayed.
         * - The category labels must be unique!
         *   The category lables are displayed.
         * - Do not forget to set the values to the correct axis.
         */
        int i = 0; /* Unique category name which are not displayed. */
                      // value, series, "Unique Label" , BarInfos
        dataset.setValue( 10.0, "0", "Planwert 'alt'",  new NormalBarInfo   ( 10.0));
        dataset.setValue(- 2.0, "0", getSpaces(++i),    new NegativeBarInfo (- 2.0));
        dataset.setValue(  4.0, "0", getSpaces(++i),    new PositiveBarInfo (  4.0));
        dataset.setValue( 12.0, "0", "Planwert 'akt'",  new NormalBarInfo   ( 12.0));
        dataset.setValue(- 5.0, "0", getSpaces(++i),    new NegativeBarInfo (- 5.0));
        dataset.setValue(- 1.0, "1", getSpaces(  i),    new NegativeBarInfo (- 1.0));
        dataset.setValue(  3.0, "0", getSpaces(++i),    new PositiveBarInfo (  3.0));
        dataset.setValue(  3.0, "1", getSpaces(  i),    new PositiveBarInfo (  3.0));
        dataset.setValue( 10.0, "0", "Planwert 'Prog'", new NormalBarInfo   ( 10.0));
        dataset.setValue(  2.0, "1", "Planwert 'Prog'", new OverwriteBarInfo(- 2.0, Color.RED));
        dataset.setValue(  8.0, "0", "Zielwert",        new NormalBarInfo   (  8.0, false));
        
        // KHA series must be Comparable only, so Integer.valueOf(0) should do as well
        return dataset;
    }
    
    /** 
     * This method sets a range marker with the given parameters.
     * 
     * @param aDataset      A {@link SwingDataset}.
     * @param aRange        A {@link Range}.
     * @param aBottomMargin A bottom margin.
     * @param aTopMargin    A top margin.
     * @param aPlot         A {@link CategoryPlot}.
     * @param aLabel        A label.
     * @param aColor        A color.
     */
    private static void configureMarker(SwingDataset aDataset, Range aRange,
                                        double aBottomMargin, double aTopMargin,
                                        CategoryPlot aPlot, String aLabel, Color aColor) {

        IntervalMarker marker = new IntervalMarker(aRange.getLowerBound(), 0, aColor,
                                                   new BasicStroke(1.0f), aColor,
                                                   new BasicStroke(1.0f), 1f);
        marker.setLabel(aLabel);
        marker.setLabelTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        marker.setLabelOffset(new RectangleInsets(/* y */5, /* x */5, 0, 0));
        aPlot.addRangeMarker(marker);
    }
    
    /** 
     * This method customize the value axis with the correct range and 
     * the integer tick units. It is possible to show the complete axis with tick marks
     * and the axis line or only the axis label. 
     * 
     * TODO TDI/KHA move into SwingDataset
     * 
     * @param aDataset      A {@link SwingDataset}.
     * @param aRangeAxis    A {@link ValueAxis}.
     * @param isShown       Indicates whether the complete axis is shown or only the label.
     * @param aBottomMargin Margin at the bottom (0..1).
     * @param aTopMargin    Margin at the top    (0..1).
     */
    public static void customizeValueAxis(SwingDataset aDataset, int aAxisIndex,
                                           ValueAxis aRangeAxis, boolean isShown,
                                           double aBottomMargin, double aTopMargin) {
        aRangeAxis.setRange(SwingDatasetUtilities.findRange(aDataset, aAxisIndex, 
                                                            aBottomMargin, aTopMargin));
		TickUnitSource units = NumberAxis.createIntegerTickUnits();
        aRangeAxis.setStandardTickUnits(units);
        if (!isShown) {
            aRangeAxis.setTickLabelsVisible(false);
            aRangeAxis.setTickMarksVisible (false);
            aRangeAxis.setAxisLineVisible  (false);
        }
    }


    /** 
     * This method write the chart completely with image (png) and image map into a
     * simple html page.
     * 
     * @param aChart      A {@link JFreeChart}.
     * @param aMapName    A image map name.
     * @param anImagePath A image name.
     * @param aFileName   A file name for the image.
     * @param aHtmlTitle  A html title.
     */
    public static void writeChart(JFreeChart aChart, String aMapName, String anImagePath,
                                  String anImageName, String aFileName,
                                  String aHtmlTitle, int aWidth, int aHeight)
                                                                        throws Exception {
        ChartRenderingInfo renderingInfo = new ChartRenderingInfo();
        FileOutputStream   out           = new FileOutputStream(new File(anImagePath + 
                                                                         anImageName));
		ChartUtils.writeChartAsPNG(out, aChart, aWidth, aHeight, renderingInfo);
		String imageMap = ChartUtils.getImageMap(
                                            aMapName, 
                                            renderingInfo,
                                            new StandardToolTipTagFragmentGenerator(), 
                                            new StandardURLTagFragmentGenerator());
        
        writeHTMLPage(aFileName, anImageName, aMapName, imageMap, aHtmlTitle);
    }

    /**
     * This method generates a simple HTML page with the image, image map and the html
     * title.
     * 
     * @param aFileName  A file name.
     * @param aImageName A image name.
     * @param aMapName   A image map name.
     * @param aImageMap  A image map.
     * @param aHtmlTitle A HTML title.
     */
    public static void writeHTMLPage(String aFileName, String aImageName, 
                                      String aMapName, String aImageMap, 
                                      String aHtmlTitle) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(aFileName));
		out.print(
		 HTMLConstants.DOCTYPE_HTML + "\n" +
         "<html>"                                                                         + "\n" +
         " <head>"                                                                        + "\n" +
         "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">" + "\n" +
         "  <title>" + aHtmlTitle + "</title>"                                            + "\n" +
         " </head>"                                                                       + "\n" +
         " <body>"                                                                        + "\n" +
         "  <br /><br />"                                                                 + "\n" +
         "  <div style=\"" + HTMLConstants.TEXT_ALIGN_CENTER + "\">"                      + "\n" +
         "  <img src=\"" + aImageName + "\" style=\"border: 0px\" alt=\"SwingChart\" "    +
                "usemap=\"#" + aMapName + "\">"                                           + "\n"
                );
        out.print(aImageMap);
        out.print( "   \n" +
         "  <div />"                                                                      + "\n" +
         " </body>" + "\n" +
         "</html>"  + "\n"
                );
        out.close();
    }

    /**
     * This method returns a string with the given count on spaces.
     * 
     * @param aCount A count.
     * @return Returns a string with the given count on spaces.
     */
    private static final String getSpaces(int aCount) {
        return StringServices.getSpaces(aCount);
    }
    
    /**
     * This method generates simple example swing charts.
     * 
     * @param args An array with arguments.
     */
    public static void main(String[] args) throws Exception {
        JFreeChart chart1 = createChart1("Generische Überleitungsschaukel"); 
        JFreeChart chart2 = createChart2("Generische Überleitungsschaukel"); 
        writeChart(chart1, "swingMap", PATH, "swing1.png", PATH + "swing1.html", 
                   "SwingChart with tooltips and urls", 600, 400);
        writeChart(chart2, "swingMap", PATH, "swing2.png", PATH + "swing2.html", 
                   "SwingChart with tooltips and urls", 600, 400);
    }

}

