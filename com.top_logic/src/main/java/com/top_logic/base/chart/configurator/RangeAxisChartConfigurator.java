/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.configurator;

import java.awt.Font;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.util.FormFieldHelper;

/**
 * The RangeAxisChartConfigurator is an interface to configure charts with range axes
 * easier.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class RangeAxisChartConfigurator extends ChartConfigurator {

    /** Automatic range type. */
    public static final String RANGE_AUTO = "auto";

    /** Clipped range type. */
    public static final String RANGE_CLIP = "clip";

    /** Fixed range type. */
    public static final String RANGE_FIXED = "fixed";

    /** The available range types. */
    public static final String[] RANGE_TYPES = new String[] {RANGE_AUTO, RANGE_CLIP, RANGE_FIXED};

    /** The default resource provider for the range types. */
    public static final I18NResourceProvider RANGE_TYPE_RESOURCE_PROVIDER = new I18NResourceProvider(I18NConstants.RANGE_TYPE);

    /** The percentage of range to differ from the min / max values of the data set. */
    public static final double RANGE_UPPER_BUFFER = 0.1;

    /** The percentage of range to differ from the min / max values of the data set. */
    public static final double RANGE_LOWER_BUFFER = 0.5;

    /**
     * Creates a {@link RangeAxisChartConfigurator} with the given chart.
     *
     * @param aChart
     *        A {@link JFreeChart} to configure.
     */
    public RangeAxisChartConfigurator(JFreeChart aChart) {
        super(aChart);
    }

    @Override
	public void setDefaultValues() {
        useIntegerValuesForRangeAxis();
        setUpperMarginToRangeAxis(0.15);
        setLowerMarginToRangeAxis(0.15);
    }

    /**
     * This method returns the range axis of the intern chart.
     */
    public ValueAxis getRangeAxis() {
        if (isCategoryPlot()) {
            return getCategoryPlot().getRangeAxis();
        } else {
            return getXYPlot().getRangeAxis();
        }
    }

    /**
     * This method sets the value axis to the intern chart.
     *
     * @param aValueAxis
     *        A {@link ValueAxis}. Must not be <code>null</code>.
     */
    public void setRangeAxis(ValueAxis aValueAxis) {
        if (isCategoryPlot()) {
            getCategoryPlot().setRangeAxis(aValueAxis);
        } else {
            getXYPlot().setRangeAxis(aValueAxis);
        }
    }

    /**
     * This method sets the upper margin for the intern chart (as a percentage
     * of the axis range).
     *
     * @param aValue
     *        A margin value.
     */
    public void setUpperMarginToRangeAxis(double aValue) {
        getRangeAxis().setUpperMargin(aValue);
    }

    /**
     * This method returns the upper margin of the intern chart (as a percentage
     * of the axis range).
     */
    public double getUpperMarginFromRangeAxis() {
        return getRangeAxis().getUpperMargin();
    }

    /**
     * This method sets the lower margin for the intern chart (as a percentage
     * of the axis range).
     *
     * @param aValue
     *        A margin value.
     */
    public void setLowerMarginToRangeAxis(double aValue) {
        getRangeAxis().setLowerMargin(aValue);
    }

    /**
     * This method returns the lower margin of the intern chart (as a percentage
     * of the axis range).
     */
    public double getLowerMarginFromRangeAxis() {
        return getRangeAxis().getLowerMargin();
    }

    /**
     * This method sets the visibility of the tick marks on the range axis.
     *
     * @param aFlag
     *        A boolean.
     */
    public void setRangeAxisTickMarksVisible(boolean aFlag) {
        getRangeAxis().setTickMarksVisible(aFlag);
    }

    /**
     * This method sets the visibility of the tick labels on the range axis.
     *
     * @param aFlag
     *        A boolean.
     */
    public void setRangeAxisTickLabelsVisible(boolean aFlag) {
        getRangeAxis().setTickLabelsVisible(aFlag);
    }

    /**
     * This method sets the tick units of the range axis to integer values (e.g.
     * 0, 1, 2, ...).
     */
    public void useIntegerValuesForRangeAxis() {
        getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    /**
     * This method sets the tick units of the range axis to double values (e.g.
     * 0.5, 5.5, 10.5, ...).
     */
    public void useDoubleValuesForRangeAxis() {
        getRangeAxis().setStandardTickUnits(NumberAxis.createStandardTickUnits());
    }

    /**
     * This method sets the range axis label.
     *
     * @param aLabel
     *        A label. Must not be <code>null</code>.
     */
    public void setRangeAxisLabel(String aLabel) {
        getRangeAxis().setLabel(aLabel);
    }

    /**
     * This method sets the label font for the range axis.
     *
     * @param aFont
     *        A {@link Font}. Must not be <code>null</code>.
     */
    public void setRangeAxisLabelFont(Font aFont) {
        getRangeAxis().setLabelFont(aFont);
    }



    /**
     * Sets the Range of the value axis according to the given range type without a default
     * range.
     *
     * @param aRangeType
     *            the range type to use; must be one of the types in {@link #RANGE_TYPES}
     */
    public void setRange(String aRangeType) {
        setRange(aRangeType, null);
    }

    /**
     * Sets a fixed Range to the value axis.
     *
     * @param aRange
     *            the range to set
     */
    public void setRange(Range aRange) {
        setRange(RANGE_FIXED, aRange);
    }

    /**
     * Sets the Range of the value axis according to the given range type.
     *
     * @param aRangeType
     *            the range type to use; must be one of the types in {@link #RANGE_TYPES}
     * @param aRange
     *            the range to set for an empty chart or for the fixed range type, may be
     *            <code>null</code>
     */
    public void setRange(String aRangeType, Range aRange) {
        if (RANGE_AUTO.equals(aRangeType)) {
            getRangeAxis().setAutoRange(true);
        }
        else if (RANGE_FIXED.equals(aRangeType)) {
			getRangeAxis().setRange(ChartUtil.normalizeRange(aRange));
        }
        else if (RANGE_CLIP.equals(aRangeType)) {
			getRangeAxis().setRange(ChartUtil.normalizeRange(computeRange()));
        }
        else {
            throw new UnsupportedOperationException("Unsupported range type.");
        }
    }

    /**
     * Computes the range for the {@link #RANGE_CLIP} range type, respecting all DataSets.
     *
     * @return the range for the {@link #RANGE_CLIP} range type
     */
    public Range computeRange() {
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        int count = 0;
        Plot thePlot = getPlot();
        if (thePlot instanceof CategoryPlot) {
            count = ((CategoryPlot)thePlot).getDatasetCount();
        }
        else if (thePlot instanceof XYPlot) {
            count = ((XYPlot)thePlot).getDatasetCount();
        }
        for (int i = 0; i < count; i++) {
            Range theRange = computeRange(i);
            min = Math.min(min, theRange.getLowerBound());
            max = Math.max(max, theRange.getUpperBound());
        }
        return new Range(min == Double.POSITIVE_INFINITY ? 0.0 : min, max == Double.NEGATIVE_INFINITY ? 0.0 : max);
    }

    /**
     * Computes the range for the {@link #RANGE_CLIP} range type for the given DataSet.
     *
     * @param aIndex
     *            the index of the DataSet to compute the range for
     * @return the range for the {@link #RANGE_CLIP} range type
     */
    public Range computeRange(int aIndex) {
        double min = 0, max = 0;
        Dataset theDataSet = getDataset(aIndex);
        if (theDataSet instanceof CategoryDataset) {
			min = FormFieldHelper.getdoubleValue(DatasetUtils.findMinimumRangeValue((CategoryDataset) theDataSet));
			max = FormFieldHelper.getdoubleValue(DatasetUtils.findMaximumRangeValue((CategoryDataset) theDataSet));
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

}
