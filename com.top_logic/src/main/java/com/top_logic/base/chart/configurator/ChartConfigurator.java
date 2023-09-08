/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.configurator;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.util.ChartConstants;

/**
 * The ChartConfigurator is an interface to configure charts easier.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ChartConfigurator implements ChartConstants{

    private JFreeChart chart;

    private boolean    initPlotKind = false;
    private boolean    categoryPlot = false;

    protected int configurationDataset = 0;

    /**
     * Creates a {@link ChartConfigurator} with the given chart.
     *
     * @param aChart
     *        A {@link JFreeChart} to configure.
     */
    public ChartConfigurator(JFreeChart aChart) {
        this.chart = aChart;
    }

    /**
     * This method sets useful default values for the chart.
     */
    public abstract void setDefaultValues();

    /**
     * This method returns the wrapped {@link JFreeChart}.
     */
    public JFreeChart getChart() {
        return this.chart;
    }

    /**
     * This method sets the intern chart.
     */
    public void setChart(JFreeChart aChart) {
        this.chart = aChart;
    }

    /**
     * This method returns the title of the intern chart.
     */
    public String getTitle() {
        return this.chart.getTitle().getText();
    }

    /**
     * This method sets the title of the intern chart.
     */
    public void setTitle(String aTitle) {
        this.chart.setTitle(aTitle);
    }

    /**
     * This method returns if the anti alias is activated.
     */
    public boolean getAntiAlias() {
        return this.chart.getAntiAlias();
    }

    /**
     * This method sets the anti alias.
     */
    public void setAntiAlias(boolean aFlag) {
        this.chart.setAntiAlias(aFlag);
    }

    /**
     * This method returns the legend of the intern chart.
     */
    public LegendTitle getLegend() {
        return this.chart.getLegend();
    }

    /**
     * This method returns the legend position.
     */
    public RectangleEdge getLegendPosition() {
        return this.chart.getLegend().getPosition();
    }

    /**
     * This method sets the legendPosition. Please use the chart constants
     * ({@link ChartConstants#LEGEND_TOP},
     *  {@link ChartConstants#LEGEND_BOTTOM},
     *  {@link ChartConstants#LEGEND_LEFT} and
     *  {@link ChartConstants#LEGEND_RIGHT}).
     *
     * @param aLegendPosition
     *        The legendPosition to set. Must not be <code>null</code>.
     */
    public void setLegendPosition(RectangleEdge aLegendPosition) {
        this.chart.getLegend().setPosition(aLegendPosition);
    }

    /**
     * This method returns the {@link Plot} of the intern chart.
     */
    public Plot getPlot() {
        return this.chart.getPlot();
    }

    /**
     * This method returns the {@link XYPlot} of the intern chart.
     */
    public XYPlot getXYPlot() {
        return this.chart.getXYPlot();
    }

    /**
     * This method returns the {@link CategoryPlot} of the intern chart.
     */
    public CategoryPlot getCategoryPlot() {
        return this.chart.getCategoryPlot();
    }



    /**
     * Sets the current Dataset index, all successive operations will act on, inclusive the
     * {@link #getDataset()} and {@link #setDataset(Dataset)} methods.
     *
     * @param i
     *            the index of the dataset to use in successive operations
     */
    public void useConfigurationDataset(int i) {
        configurationDataset = i;
    }


    /**
     * This method returns the dataset at the index selected by the
     * {@link #useConfigurationDataset(int)} method.
     */
    public Dataset getDataset() {
        return getDataset(configurationDataset);
    }

    /**
     * This method returns the dataset for the given index.
     *
     * @param anIndex
     *        An index (0..n).
     */
    public Dataset getDataset(int anIndex) {
        if (getPlot() instanceof XYPlot) {
            return getXYPlot().getDataset(anIndex);
        } else if (getPlot() instanceof CategoryPlot) {
            return getCategoryPlot().getDataset(anIndex);
        } else {
            throw new IllegalArgumentException("The plot is neither a category plot nor xy plot.");
        }
    }

    /**
     * This method is a convenience method and works like the method
     * {@link #setDataset(int, Dataset)} with the index selected by the
     * {@link #useConfigurationDataset(int)} method..
     *
     * @param aDataset
     *            A {@link Dataset} which fits to the intern chat. Must not be
     *            <code>null</code>.
     */
    public void setDataset(Dataset aDataset) {
        setDataset(configurationDataset, aDataset);
    }

    /**
     * This method sets the dataset at the index position.
     *
     * @param anIndex
     *        The index of the dataset (0..n).
     * @param aDataset
     *        A {@link Dataset} which fits to the intern chat.
     *        Must not be <code>null</code>.
     */
    public void setDataset(int anIndex, Dataset aDataset) {
        if (aDataset == null) {
            throw new IllegalArgumentException("The dataset must NOT be null!");
        }

        if (aDataset instanceof XYDataset) {
            getXYPlot().setDataset(anIndex, (XYDataset)aDataset);
        } else if (aDataset instanceof CategoryDataset) {
            getCategoryPlot().setDataset(anIndex, (CategoryDataset)aDataset);
        } else {
            throw new IllegalArgumentException("The given dataset does not fit to the plot of the intern chart.");
        }
    }

    /**
     * This method returns <code>true</code> if the plot is an instance of
     * {@link CategoryPlot}, <code>false</code> otherwise.
     */
    protected boolean isCategoryPlot() {
        if (!this.initPlotKind) {
            if (getPlot() instanceof CategoryPlot) {
                this.categoryPlot = true;
            }
            this.initPlotKind = true;
        }

        return this.categoryPlot;
    }

}
