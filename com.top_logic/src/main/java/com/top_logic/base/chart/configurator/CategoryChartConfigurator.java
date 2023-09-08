/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.configurator;

import java.awt.Font;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.renderer.BarLineRenderer;
import com.top_logic.util.FormFieldHelper;

/**
 * The CategoryChartConfigurator is an interface to configure category charts easier.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class CategoryChartConfigurator extends RangeAxisChartConfigurator {

    protected int configurationRenderer = 0;

    /**
     * Creates a {@link ChartConfigurator} with the given chart.
     *
     * @param aChart
     *        A {@link JFreeChart} to configure.
     */
    public CategoryChartConfigurator(JFreeChart aChart) {
        super(aChart);
    }

    @Override
	public void setDefaultValues() {
        super.setDefaultValues();

        setMaximumCategoryLabelLines(3);
    }

    /**
     * This method returns the {@link CategoryAxis} of the intern chart.
     */
    public CategoryAxis getDomainAxis() {
        return getCategoryPlot().getDomainAxis();
    }

    /**
     * This method sets the {@link CategoryAxis} of the intern chart.
     *
     * @param anAxis
     *        An {@link CategoryAxis}. Must not be <code>null</code>.
     */
    public void setDomainAxis(CategoryAxis anAxis) {
        getCategoryPlot().setDomainAxis(anAxis);
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
     * This method sets the visibility ot the domain grid lines.
     */
    public void setDomainGridlinesVisible(boolean aFlag) {
        getCategoryPlot().setDomainGridlinesVisible(aFlag);
    }

    /**
     * This method sets the maximum label lines on the domain axis.
     *
     * @param aNumberOfLines
     *        A number of lines (1..n).
     */
    public void setMaximumCategoryLabelLines(int aNumberOfLines) {
        if (aNumberOfLines <= 0) {
            throw new IllegalArgumentException("The number of lines must be greater than 0.");
        }

        getDomainAxis().setMaximumCategoryLabelLines(aNumberOfLines);
    }



    /**
     * Sets the current Renderer index, all successive operations will act on, inclusive the
     * {@link #getRenderer()} and {@link #setRenderer(CategoryItemRenderer)} methods.
     *
     * @param i
     *            the index of the renderer to use in successive operations
     */
    public void useConfigurationRenderer(int i) {
        configurationRenderer = i;
    }


    /**
     * This method returns the renderer selected by the
     * {@link #useConfigurationRenderer(int)} method.
     */
    public CategoryItemRenderer getRenderer() {
        return getRenderer(configurationRenderer);
    }

    /**
     * This method sets the renderer selected by the {@link #useConfigurationRenderer(int)}
     * method for the intern chart.
     *
     * @param aRenderer
     *            The {@link CategoryItemRenderer}. Must not be <code>null</code>.
     */
    public void setRenderer(CategoryItemRenderer aRenderer) {
        setRenderer(configurationRenderer, aRenderer);
    }

    /**
     * This method returns the renderer with the given index of the intern
     * chart.
     *
     * @param anIndex
     *        The index of the renderer (0..n).
     */
    public CategoryItemRenderer getRenderer(int anIndex) {
        return getCategoryPlot().getRenderer(anIndex);
    }

    /**
     * This method sets the renderer at the index position.
     *
     * @param anIndex
     *        The index of the renderer (0..n).
     * @param aRenderer
     *        The {@link CategoryItemRenderer}. Must not be <code>null</code>.
     */
    public void setRenderer(int anIndex, CategoryItemRenderer aRenderer) {
        if (aRenderer == null) {
            throw new IllegalArgumentException("The renderer must NOT be null!");
        }

        getCategoryPlot().setRenderer(anIndex, aRenderer);
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
     *        A {@link CategoryItemLabelGenerator}.
     */
    public void setItemLabelGenerator(CategoryItemLabelGenerator aGenerator) {
		getRenderer().setDefaultItemLabelGenerator(aGenerator);
		getRenderer().setDefaultItemLabelsVisible(true);
    }

    /**
     * This method sets the visibility of the item labels. If no item label
     * generator was registered, this method added a
     * {@link StandardCategoryItemLabelGenerator} to the chart.
     *
     * @param aFlag
     *        A boolean flag.
     */
    public void setItemLabelsVisible(boolean aFlag) {
		getRenderer().setDefaultItemLabelsVisible(aFlag);

		if (aFlag && getRenderer().getDefaultItemLabelGenerator() == null) {
			getRenderer().setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
//            setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        }
    }

    /**
     * This method sets the tooltip generator.
     *
     * @param aGenerator
     *        A {@link CategoryToolTipGenerator}.
     */
    public void setToolTipGenerator(CategoryToolTipGenerator aGenerator) {
		getRenderer().setDefaultToolTipGenerator(aGenerator);
    }

    /**
     * This method sets the url generator.
     *
     * @param aGenerator
     *        A {@link CategoryURLGenerator}.
     */
    public void setURLGenerator(CategoryURLGenerator aGenerator) {
		getRenderer().setDefaultItemURLGenerator(aGenerator);
    }



    /**
     * Overridden to support stacked bar renderer.
     *
     * @see com.top_logic.base.chart.configurator.RangeAxisChartConfigurator#computeRange(int)
     */
    @Override
	public Range computeRange(int aIndex) {
        double min = 0, max = 0;
        boolean stacked = (getRenderer(aIndex) instanceof StackedBarRenderer) && !(getRenderer(aIndex) instanceof BarLineRenderer);
        Dataset theDataSet = getDataset(aIndex);
        if (theDataSet instanceof CategoryDataset) {
			min = FormFieldHelper.getdoubleValue(stacked ? findMinimumStackedRangeValue((CategoryDataset) theDataSet)
				: DatasetUtils.findMinimumRangeValue((CategoryDataset) theDataSet));
			max = FormFieldHelper.getdoubleValue(stacked ? findMaximumStackedRangeValue((CategoryDataset) theDataSet)
				: DatasetUtils.findMaximumRangeValue((CategoryDataset) theDataSet));
        }
        else if (theDataSet instanceof XYDataset) {
			min = FormFieldHelper.getdoubleValue(DatasetUtils.findMinimumRangeValue((XYDataset) theDataSet));
			max = FormFieldHelper.getdoubleValue(DatasetUtils.findMaximumRangeValue((XYDataset) theDataSet));
        }
        else if (theDataSet != null) {
            throw new UnsupportedOperationException("Unsupported dataset.");
        }
        double diff = Math.abs(max - min);
        double buffer = diff * RANGE_LOWER_BUFFER;
        double buffer2 = diff * RANGE_UPPER_BUFFER;
        min = (min >= 0 && min < buffer) ? 0 : min - (min < 0 ? buffer2 : buffer);
        max = (max <=0 && -max < buffer2) ? 0 : max + (max < 0 ? buffer : buffer2);
        return new Range(min, max);
    }

    /**
	 * Replacement of {@link DatasetUtils#findMinimumStackedRangeValue(CategoryDataset)} Returns the
	 * minimum value in the dataset range, assuming that values in each category are "stacked".
	 *
	 * @param aDataSet
	 *        the dataset.
	 *
	 * @return The minimum value.
	 */
    public static Number findMinimumStackedRangeValue(CategoryDataset aDataSet) {
        Number theResult = null;
        if (aDataSet != null) {
            double min = Double.POSITIVE_INFINITY;
            int rowCount = aDataSet.getRowCount(), columnCount = aDataSet.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                double total = 0.0;
                for (int j = 0; j < rowCount; j++) {
                    total += FormFieldHelper.getdoubleValue(aDataSet.getValue(j, i));
                    min = Math.min(min, total);
                }
            }
            theResult = Double.valueOf(min == Double.POSITIVE_INFINITY ? 0.0 : min);
        }
        return theResult;
    }

    public static Number findMaximumStackedRangeValue(CategoryDataset aDataSet) {
        Number theResult = null;
        if (aDataSet != null) {
            double max = Double.NEGATIVE_INFINITY;
            int rowCount = aDataSet.getRowCount(), columnCount = aDataSet.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                double total = 0.0;
                for (int j = 0; j < rowCount; j++) {
                    total += FormFieldHelper.getdoubleValue(aDataSet.getValue(j, i));
                    max = Math.max(max, total);
                }
            }
            theResult = Double.valueOf(max == Double.NEGATIVE_INFINITY ? 0.0 : max);
        }
        return theResult;
    }

}
