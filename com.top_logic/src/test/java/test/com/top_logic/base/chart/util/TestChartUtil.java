/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.chart.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.textui.TestRunner;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DefaultDisplayContext;

/**
 * The TestChartUtil tests the class {@link ChartUtil}
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestChartUtil extends BasicTestCase implements ChartConstants{

    private static final int DELAY = 10 * 1000;

	private static final int WIDTH = 800;

	private static final int HEIGHT = 600;

    public TestChartUtil(String name) {
	    super(name);
    }

	/**
	 * Tests {@link ChartUtil#normalizeRange(Range)}.
	 */
	public void testNormalize() {
		Range range1 = ChartUtil.normalizeRange(new Range(15, 15));
		assertTrue(range1.getUpperBound() > range1.getLowerBound());
		Range range2 = ChartUtil.normalizeRange(new Range(0, 0));
		assertTrue(range2.getUpperBound() > range2.getLowerBound());
		try {
			Range range3 = ChartUtil.normalizeRange(new Range(-6, -6));
			assertTrue(range3.getUpperBound() > range3.getLowerBound());
		} catch (IllegalArgumentException ex) {
			throw fail("Ticket #18137: ", ex);
		}
	}

	/** 
     * No comment for test methods.
     */
    public void testPng() throws Exception {
        File theFile  = BasicTestCase.createTestFile("testChart", ".png");

        assertTrue(theFile.exists());
        
        long                     theEmptyLength = theFile.length();
        final JFreeChart         theChart       = createChart();
        final ChartRenderingInfo theInfo        = new ChartRenderingInfo();
        final FileOutputStream   theOut         = new FileOutputStream(theFile);
        try {
        	Thread theThread = new Thread(new Runnable() {
                @Override
				public void run() {
                    try {
						ChartUtil.writeChartAsPng(theOut, theChart, new Dimension(WIDTH, HEIGHT), theInfo);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        	theThread.start();  
        	theThread.join(DELAY);
        	assertFalse(theThread.isAlive());
        } finally {
            StreamUtilities.close(theOut);
        }

		long theNewLen = theFile.length();
		assertTrue("File " + theFile + " has no content?", theEmptyLength < theNewLen);
        
        HTMLFragment imageMap = ChartUtil.getImageMap("TestMap", theInfo);
		TagWriter out = new TagWriter();
		imageMap.write(DefaultDisplayContext.getDisplayContext(), out);
		assertTrue(out.toString().length() > 41 /* empty map */);
        

		theFile.delete();
    }
    
   /** 
    * No comment for test methods.
    */
    public void testIndirectTmpFilePng() throws Exception {
        
        final JFreeChart theChart = createChart();
        
        MyRunnable theRunnable = new MyRunnable(theChart);
        Thread theThread = new Thread(theRunnable);
        theThread.run();
        theThread.join(DELAY);
        
        File theFile = FileManager.getInstance().getIDEFile(theRunnable.path);
        
        assertTrue(theFile.exists());
        assertTrue(theFile.length() > 0);
    }
    
    /** 
     * No comment for test methods.
     */
    private JFreeChart createChart() {
        DefaultCategoryDataset theDataset = new DefaultCategoryDataset();
        theDataset.addValue(10, "Series 1", "Category A");
        theDataset.addValue(10, "Series 2", "Category A");
        theDataset.addValue(10, "Series 1", "Category B");
        theDataset.addValue(10, "Series 2", "Category B");
        
        return ChartFactory.createBarChart("Title", "X-Label", "Y-Label", theDataset, PlotOrientation.VERTICAL, !LEGEND, TOOLTIPS, URLS);
    }

    private class MyRunnable implements Runnable {

        /** Image path. */
        public String     path;
        /** Chart. */
        public JFreeChart chart;
        
        /** 
         * Creates a {@link MyRunnable} with the
         * given parameter.
         */
        public MyRunnable(JFreeChart aChart) {
            this.chart = aChart;
        }
        
        @Override
		public void run() {
            try {
				this.path = ChartUtil.writeChartAsPng(chart, new Dimension(WIDTH, HEIGHT), null);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
		return TLTestSetup
			.createTLTestSetup(ServiceTestSetup.createSetup(TestChartUtil.class, SafeHTML.Module.INSTANCE));
//        return new TLTestSetup(new TestChartUtil("testPng"));
    }
    
    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        
        TestRunner.run(suite());
    }
    
}

