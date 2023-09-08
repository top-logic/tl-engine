/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.configurator;

import java.awt.Font;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.urls.XYURLGenerator;

/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class XYChartConfigurator extends RangeAxisChartConfigurator {

    /**
     * Creates a {@link XYChartConfigurator} with the given chart.
     * 
     * @param aChart
     *        A {@link JFreeChart} to configure.
     */
    public XYChartConfigurator(JFreeChart aChart) {
        super(aChart);
    }

    /**
     * This method returns the {@link ValueAxis} of the intern chart.
     */
    public ValueAxis getDomainAxis() {
        return getXYPlot().getDomainAxis();
    }
    
    /**
     * This method sets the domain axis of the intern chart.
     * 
     * @param aValueAxis
     *        A {@link ValueAxis}. Must not be <code>null</code>.
     */
    public void setDomainAxis(ValueAxis aValueAxis) {
        getXYPlot().setDomainAxis(aValueAxis);
    }
    
    /**
     * This method sets the label angle of the domain axis.
     * 
     * @param aAngle
     *        A angle.
     */
    public void setDomainAxisLabelAngle(double aAngle) {
        getDomainAxis().setLabelAngle(aAngle);
    }
    
    /**
     * This method sets the vertical tick labels.
     * 
     * @param aFlag
     *        A boolean.
     */
    public void setVerticalTickLabels(boolean aFlag) {
        getDomainAxis().setVerticalTickLabels(aFlag);
    }
    
    /**
     * This method sets the visibility ot the domain grid lines.
     */
    public void setDomainGridlinesVisible(boolean aFlag) {
        getXYPlot().setDomainGridlinesVisible(aFlag);
    }
    
    /**
     * This method returns the renderer of the intern chart.
     */
    public XYItemRenderer getRenderer() {
        return getRenderer(0);
    }
    
    /**
     * This method returns the renderer with the given index of the intern
     * chart.
     * 
     * @param anIndex
     *        The index of the renderer (0..n).
     */
    public XYItemRenderer getRenderer(int anIndex) {
        return getXYPlot().getRenderer();
    }
    
    /**
     * This method sets the renderer for the intern chart.
     * 
     * @param aRenderer
     *        The {@link XYItemRenderer}. Must not be <code>null</code>.
     */
    public void setRenderer(XYItemRenderer aRenderer) {
        setRenderer(0, aRenderer);
    }
    
    /**
     * This method sets the renderer at the index position.
     * 
     * @param anIndex
     *        The index of the renderer (0..n).
     * @param aRenderer
     *        The {@link XYItemRenderer}. Must not be <code>null</code>.
     */
    public void setRenderer(int anIndex, XYItemRenderer aRenderer) {
        if (aRenderer == null) {
            throw new IllegalArgumentException("The renderer must NOT be null!");
        }
        
        getXYPlot().setRenderer(anIndex, aRenderer);
    }
    
    /**
     * This method sets the domain axis label.
     * 
     * @param aLabel
     *        A label. Must not be <code>null</code>.
     */
    public void setDomainAxisLabel(String aLabel) {
        getDomainAxis().setLabel(aLabel);
    }
    
    
    /**
     * This method sets the label font for the domain axis.
     * 
     * @param aFont
     *        A {@link Font}. Must not be <code>null</code>.
     */
    public void setDomainAxisLabelFont(Font aFont) {
        getDomainAxis().setLabelFont(aFont);
    }
    
    /**
     * This method sets the item label generator and sets the item labels
     * visible.
     * 
     * @param aGenerator
     *        A {@link XYItemLabelGenerator}. 
     */
    public void setItemLabelGenerator(XYItemLabelGenerator aGenerator) {
		getRenderer().setDefaultItemLabelGenerator(aGenerator);
		getRenderer().setDefaultItemLabelsVisible(true);
    }
    
    /**
     * This method sets the visibility of the item labels. If no item label
     * generator was registered, this method added a
     * {@link StandardXYItemLabelGenerator} to the chart.
     * 
     * @param aFlag
     *        A boolean flag.
     */
    public void setItemLabelsVisible(boolean aFlag) {
		getRenderer().setDefaultItemLabelsVisible(aFlag);
        
		if (aFlag && getRenderer().getDefaultItemLabelGenerator() == null) {
            setItemLabelGenerator(new StandardXYItemLabelGenerator());
        }
    }
    
    /**
     * This method sets the tooltip generator.
     * 
     * @param aGenerator
     *        A {@link XYToolTipGenerator}.
     */
    public void setToolTipGenerator(XYToolTipGenerator aGenerator) {
		getRenderer().setDefaultToolTipGenerator(aGenerator);
    }
    
    /**
     * This method sets the url generator.
     * 
     * @param aGenerator
     *        A {@link XYURLGenerator}.
     */
    public void setURLGenerator(XYURLGenerator aGenerator) {
        getRenderer().setURLGenerator(aGenerator);
    }
}

