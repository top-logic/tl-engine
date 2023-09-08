/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.ImageIcon;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.Layer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.top_logic.reporting.chart.demo.generator.DemoURLGenerator;
import com.top_logic.reporting.chart.renderer.ProgressRenderer;
import com.top_logic.reporting.chart.renderer.TemplateRenderer;
import com.top_logic.reporting.common.tree.TreeAxis;
import com.top_logic.reporting.common.tree.TreeInfo;
import com.top_logic.reporting.common.tree.TreeInfoNode;

/**
 * The ProgressMain generates a example progress chart with a tree axis.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ProgressMain {

    /** Constant for the project image index. */
    private static final int PROJECT      = 0;
    /** Constant for the order image index. */
    private static final int ORDER        = 1;
    /** Constant for the work package image index. */
    private static final int WORK_PACKAGE = 2;
    
    
    /** Path to the demo directory. */
    public static final String PATH  = "src/com/top_logic/reporting/chart/demo/";
    
    private static JFreeChart createChart() {
        CategoryDataset dataset = createDataset();
        
        CategoryAxis categoryAxis = new CategoryAxis();
        
        Image   icon1 = new ImageIcon(PATH + "img/p.png") .  getImage();
        Image   icon2 = new ImageIcon(PATH + "img/a.png") .  getImage();
        Image   icon3 = new ImageIcon(PATH + "img/ap.png").  getImage();
        Image[] icons = {icon1, icon2, icon3};
        
        TreeInfo[] infos     = getTreeInfos();
        TreeAxis   rangeAxis = new TreeAxis(infos, icons);
        rangeAxis.setIconLabelDistance(3);
        rangeAxis.setTreeIconsDistance(15);
        rangeAxis.setGridBandsVisible(false);
        rangeAxis.setTickMarksVisible(false);
        
        TemplateRenderer renderer = new ProgressRenderer();
        renderer.setShapeMargin(20);
		renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
		renderer.setDefaultItemURLGenerator(new DemoURLGenerator());
        
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, rangeAxis, renderer);
        plot.setRangeGridlinesVisible (true);
        plot.setDomainGridlinesVisible(false);
        plot.setDomainGridlinePaint(ChartColor.lightGray);
        
        JFreeChart result = new JFreeChart("", TextTitle.DEFAULT_FONT, plot, false);
        customizeChart(result);
        return result;
    }

    private static void customizeChart(JFreeChart aChart) {
        aChart.setBackgroundPaint(Color.WHITE);
        aChart.getPlot().setBackgroundPaint(Color.WHITE);

        CategoryPlot   plot   = (CategoryPlot)aChart.getPlot();
        CategoryMarker marker = new CategoryMarker("11", new Color(0, 0, 255, 35), new BasicStroke(1.0f));
        marker.setDrawAsLine(false);
        plot.addDomainMarker(marker, Layer.BACKGROUND);

        TemplateRenderer renderer = (TemplateRenderer)plot.getRenderer();
        renderer.setShapeGradientValue(100);
    }
    
    private static TreeInfo[] getTreeInfos() {
        TreeInfoNode compas2       = new TreeInfoNode(
                                      new TreeInfo("COMPAS PHASE 2", PROJECT,
                                                   "Ich bin ein Projektknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "COMPAS PHASE 2 Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));

        TreeInfoNode specification = new TreeInfoNode(
                                      new TreeInfo("Spezifikation", ORDER,
                                                   "Ich bin ein Auftragsknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "Spezifikation Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));

        TreeInfoNode realization   = new TreeInfoNode(
                                      new TreeInfo("Realisierung", ORDER,
                                                   "Ich bin ein Auftragsknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "LHTPA Integration Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));

        TreeInfoNode lhtpa         = new TreeInfoNode(
                                      new TreeInfo("LHTPA Integration", WORK_PACKAGE,
                                                   "Ich bin ein Arbeitspaketknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "LHTPA Integration Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));

        TreeInfoNode news          = new TreeInfoNode(
                                      new TreeInfo("News", WORK_PACKAGE,
                                                   "Ich bin ein Arbeitspaketknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "News Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));

        TreeInfoNode documentation = new TreeInfoNode(
                                      new TreeInfo("Dokumentation", ORDER,
                                                   "Ich bin ein Auftragsknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "Dokumentation Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));
        
        TreeInfoNode guide         = new TreeInfoNode(
                                      new TreeInfo("Benutzerhandbuch", WORK_PACKAGE,
                                                   "Ich bin ein Arbeitspaketknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "Benutzerhandbuch Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));
        
        TreeInfoNode introduction  = new TreeInfoNode(
                                      new TreeInfo("Einleitung", WORK_PACKAGE,
                                                   "Ich bin ein Arbeitspaketknoten!",
                                                   "http://www.top-logic.de/pos",
                                                   "Einleitung Tooltip ...",
                                                   "https://www.top-logic.com/exchange"));
        
        compas2.addChild(specification);
        compas2.addChild(realization);
           realization.addChild(lhtpa);
           realization.addChild(news);
        compas2.addChild(documentation);
           documentation.addChild(guide);
              guide.addChild(introduction);
        
        return compas2.getReverseArray();
    }
    
    private static CategoryDataset createDataset() {
        /*
         * The values are the color indicators for the shape.
         * 0 green
         * 1 yellow
         * 2 red  
         * 
         * The rows are particularly treated. The rows have to start with "0" and
         * then "1", "2", "3", ... This is needed for the TemplateRenderer and the
         * symbolic axis.
         * 
         * The categories are not special handled.  
         */
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int c1 = 0;
        dataset.addValue(1, String.valueOf(c1++), "10");
        dataset.addValue(0, String.valueOf(c1++), "10");
        dataset.addValue(2, String.valueOf(c1++), "10");
        dataset.addValue(1, String.valueOf(c1++), "10");
        dataset.addValue(0, String.valueOf(c1++), "10");
        dataset.addValue(2, String.valueOf(c1++), "10");
        dataset.addValue(2, String.valueOf(c1++), "10");
        dataset.addValue(2, String.valueOf(c1++), "10");

        int c2 = 0;
        dataset.addValue(0, String.valueOf(c2++), "11");
        dataset.addValue(1, String.valueOf(c2++), "11");
        dataset.addValue(2, String.valueOf(c2++), "11");
        dataset.addValue(1, String.valueOf(c2++), "11");
        dataset.addValue(0, String.valueOf(c2++), "11");
        dataset.addValue(1, String.valueOf(c2++), "11");
        dataset.addValue(1, String.valueOf(c2++), "11");
        dataset.addValue(1, String.valueOf(c2++), "11");

        int c3 = 0;
        dataset.addValue(0, String.valueOf(c3++), "12");
        dataset.addValue(0, String.valueOf(c3++), "12");
        dataset.addValue(1, String.valueOf(c3++), "12");
        dataset.addValue(0, String.valueOf(c3++), "12");
        dataset.addValue(0, String.valueOf(c3++), "12");
        dataset.addValue(0, String.valueOf(c3++), "12");
        dataset.addValue(0, String.valueOf(c3++), "12");
        dataset.addValue(0, String.valueOf(c3++), "12");

        return dataset;
    }
    
    /**
     * This method generates a example progress chart with a tree axis.
     * 
     * @param args An array with arguments.
     */
    public static void main(String[] args) {
        JFreeChart chart = createChart(); 
        try {
            writeChart(chart, "progressMap", PATH + "progress/", "progress.png", PATH + "progress/" + "progress.html", "Progress chart with tree, tooltips and urls", 450, 300);
        }
        catch (Exception e) {
            e.printStackTrace();
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
    private static void writeChart(JFreeChart aChart, String aMapName, String anImagePath,
                                  String anImageName, String aFileName,
                                  String aHtmlTitle, int aWidth, int aHeight)
                                                                             throws Exception {
        ChartRenderingInfo renderingInfo = new ChartRenderingInfo();
        FileOutputStream   out           = new FileOutputStream(new File(anImagePath + 
                                                                         anImageName));
		ChartUtils.writeChartAsPNG(out, aChart, aWidth, aHeight, renderingInfo);
        
        /* Add the additional entries to data area entries. */
        ValueAxis axis = aChart.getCategoryPlot().getRangeAxis();
        if (axis instanceof TreeAxis) {
            TreeAxis         treeAxis = (TreeAxis)axis;
            EntityCollection entities = treeAxis.getEntities();
            if (entities != null) {
                EntityCollection dataAreaCol = renderingInfo.getEntityCollection();
                dataAreaCol.addAll(entities);
                renderingInfo.setEntityCollection(dataAreaCol);            
            }
        }
		String imageMap = ChartUtils.getImageMap(
                                            aMapName, 
                                            renderingInfo,
                                            new StandardToolTipTagFragmentGenerator(), 
                                            new StandardURLTagFragmentGenerator());
        
        SwingMain.writeHTMLPage(aFileName, anImageName, aMapName, imageMap, aHtmlTitle);
    }
    
}

