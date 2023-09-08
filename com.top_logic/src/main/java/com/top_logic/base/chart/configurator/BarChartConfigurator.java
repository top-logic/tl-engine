/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.configurator;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.Collection;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.StandardGradientPaintTransformer;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.mig.util.ColorUtil;

/**
 * The BarChartConfigurator is an interface to configure bar charts easier.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class BarChartConfigurator extends CategoryChartConfigurator {

    /**
     * Creates a {@link BarChartConfigurator} with the given chart.
     *
     * @param aChart
     *        A {@link JFreeChart} to configure.
     */
    public BarChartConfigurator(JFreeChart aChart) {
        super(aChart);
    }

    /**
     * Creates a {@link BarChartConfigurator}. The constructor
     * creates intern a new bar chart with the given dataset.
     *
     * @param legend
     *        The flag indicates whether a legend is shown.
     * @param aDataset
     *        A {@link CategoryDataset}. Must not be <code>null</code>.
     */
    public BarChartConfigurator(boolean legend, CategoryDataset aDataset) {
        this("", "", "", legend, aDataset);
    }

    /**
     * Creates a {@link BarChartConfigurator}. The constructor
     * creates intern a new bar chart with the given values.
     *
     * @param aTitle
     *        A chart title. Must not be <code>null</code>.
     * @param aXAxisLabel
     *        A x-axis label. Must not be <code>null</code>.
     * @param aYAxisLabel
     *        A y-axis label. Must not be <code>null</code>.
     * @param legend
     *        The flag indicates whether a legend is shown.
     * @param aDataset
     *        A {@link CategoryDataset}. Must not be <code>null</code>.
     */
    public BarChartConfigurator(String aTitle, String aXAxisLabel, String aYAxisLabel, boolean legend, CategoryDataset aDataset) {
        super(ChartFactory.createBarChart(  aTitle,
                                            aXAxisLabel,
                                            aYAxisLabel,
                                            aDataset,
                                            PlotOrientation.VERTICAL,
                                            legend,
                                            !TOOLTIPS,
                                            !URLS));
    }

    @Override
	public void setDefaultValues() {
        super.setDefaultValues();

        adjustRangeAxisBounds(0.33);
        setMaximumBarWidth(0.15);
        setMinimumBarLength(2.0);
    }

    /**
     * This method returns the bar renderer of the intern chart.
     */
    public BarRenderer getBarRenderer() {
        return (BarRenderer)super.getRenderer();
    }

    /**
     * This method returns the maximum bar width (as percentage).
     */
    public double getMaximumBarWidth() {
        return getBarRenderer().getMaximumBarWidth();
    }

    /**
	 * Sets the maximum bar witdh
	 */
    public void setMaximumBarWidth(double aValueAsPercentage) {
        getBarRenderer().setMaximumBarWidth(aValueAsPercentage);
    }

    /**
     * This method sets the gradient paint to the series colors.
     */
    public void useGradientPaint() {
        getBarRenderer().setGradientPaintTransformer(new StandardGradientPaintTransformer());
        setGradientPaint(getBarRenderer(), (CategoryDataset)getDataset());
    }

    /**
     * This method sets the gradient paint to the series colors.
     *
     * @param aIntensity
     *            A intensive value (0..255).
     */
    public void useGradientPaint(int aIntensity) {
        getBarRenderer().setGradientPaintTransformer(new StandardGradientPaintTransformer());
        setGradientPaint(getBarRenderer(), (CategoryDataset)getDataset(), aIntensity);
    }

    /**
     * This method returns the minimum bar length.
     */
    public double getMinimumBarLength() {
        return getBarRenderer().getMinimumBarLength();
    }

    /**
     * This method sets the minimum bar length.
     *
     * @param aLength
     *        A bar length (0..n);
     */
    public void setMinimumBarLength(double aLength) {
        getBarRenderer().setMinimumBarLength(aLength);
    }

    /**
     * This method replaced the JFreeChart series colors through the given
     * colors.
     *
     * @param someColors
     *        A collection of {@link Color}s. Must not be <code>null</code>.
     */
    public void setSeriesColors (Collection someColors) {
        Iterator   theIter   = someColors.iterator();
        for (int i = 0; i < ((CategoryDataset)getDataset()).getRowCount(); i++) {
            if (theIter.hasNext()) {
                getBarRenderer().setSeriesPaint(i, (Paint)theIter.next());
            } else {
                return;
            }
        }
    }

    /**
     * This method adjusts the range (upper and lower bound) of the range
     * axis. The bound factor is multiplicated to the maximum of the upper and
     * lower bound.
     *
     * @param aBoundFactor
     *        The bound faktor adjusts the range of the upper and lower bound of
     *        the range axis.
     *
     * E.g. JFreeChart calculate the range to [-10, 1]
     *      bound factor = 0.5
     *      The new range is [-10, 5]
     *      or
     *      bound factor = 1.0
     *      The new range is [-10, 10]
     *
     */
    public void adjustRangeAxisBounds(double aBoundFactor) {
        double theLowerBoundAbs = Math.abs(getRangeAxis().getLowerBound());
        double theUpperBoundAbs = Math.abs(getRangeAxis().getUpperBound());
        double theMax           = Math.max(theLowerBoundAbs, theUpperBoundAbs);
        if (theMax == 0) {
            return;
        } else {
            if (theLowerBoundAbs / theMax <= aBoundFactor) {
                getRangeAxis().setLowerBound(theMax * aBoundFactor * -1);
            }
            if (theUpperBoundAbs / theMax <= aBoundFactor) {
                getRangeAxis().setUpperBound(theMax * aBoundFactor);
            }
        }
    }

    /**
     * This method replaced the JFreeChart series colors through
     * {@link GradientPaint}s with an intensity of 50.
     *
     * @param aRenderer
     *        A {@link BarRenderer}. Must not be <code>null</code>.
     * @param aDataSet
     *        A {@link CategoryDataset}. Must not be <code>null</code>.
     */
    private void setGradientPaint(BarRenderer aRenderer, CategoryDataset aDataSet) {
        setGradientPaint(aRenderer, aDataSet, 50);
    }

    /**
     * This method replaced the JFreeChart series colors through
     * {@link GradientPaint}s with the given intensity.
     *
     * @param aRenderer
     *        A {@link BarRenderer}. Must not be <code>null</code>.
     * @param aDataSet
     *        A {@link CategoryDataset}. Must not be <code>null</code>.
     * @param aIntensity
     *            A intensive value (0..255).
     */
    private void setGradientPaint(BarRenderer aRenderer, CategoryDataset aDataSet, int aIntensity) {
        int theRowCount = aDataSet.getRowCount();
        for (int i = 0; i < theRowCount; i++) {
            Paint theSeriesPaint = aRenderer.getSeriesPaint(i);
            if (theSeriesPaint instanceof Color) {
                Color theSeriesColor = (Color)theSeriesPaint;
                aRenderer.setSeriesPaint(i, ColorUtil.getGradientPaintFor(theSeriesColor, aIntensity));
            }
        }
    }

}
