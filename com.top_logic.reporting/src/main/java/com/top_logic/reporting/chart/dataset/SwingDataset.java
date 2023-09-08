/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.dataset;

import java.util.HashMap;

import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;

/**
 * The SwingDataset is the corresponding dataset for the 
 * {@link com.top_logic.reporting.chart.renderer.SwingRenderer}. The SwingDataset contains 
 * the normal JFreeChart dataset information like value, series and category and 
 * additionally a {@link com.top_logic.reporting.chart.info.swing.SwingRenderingInfo} 
 * for each series and category.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingDataset extends DefaultCategoryDataset {

    /**
     * This map contains the rendering infos which are needed for the
     * {@link com.top_logic.reporting.chart.renderer.SwingRenderer}.
     * The keys are the series and category as a csv string (e.g.
     * series = 2, category = 3 => key = '2,3'.
     */
    private HashMap<String,SwingRenderingInfo> infos;
    
	/** The values for the different columns, needed for range check. */
	private double[] colValues;

    /**
	 * Creates a new {@link SwingDataset}.
	 */
    public SwingDataset() {
		this(8);
	}

	/**
	 * Creates a new {@link SwingDataset} with given number of slots to set
	 * {@link #setMaxMinValue(double, int) values}.
	 */
	public SwingDataset(int slots) {
		this.infos = new HashMap<>();
		this.colValues = new double[slots];
    }
    
    /**
     * This method sets a new entry into the SwingDataset.
     * 
     * @param aValue    A value.
     * @param aSeries   A series index as String. The index starts with '0', '1', ...
     * @param aCategory A unique category label. Note that the unique category
     *                  labels are displayed. If this is not desired, blanks can be used.
     * @param anInfo    A {@link SwingRenderingInfo}. Must NOT be
     *                  <code>null</code>.
     */
	public void setValue(double aValue, Comparable<?> aSeries, Comparable<?> aCategory, SwingRenderingInfo anInfo) {
        super.setValue(aValue, aSeries, aCategory);

        int theSeriesIndex   = this.getRowIndex(aSeries);
        int theCategoryIndex = this.getColumnIndex(aCategory);

        this.infos.put(SwingDatasetUtilities.getKey(theSeriesIndex, theCategoryIndex), anInfo);
    }
    
    /**
     * This method calls only the 
     * {@link #setValue(double, Comparable, Comparable, SwingRenderingInfo)} method.
     * 
     * @param aValue    A value.
     * @param aSeries   A series index as String. The index starts with '0', '1', ...
     * @param aCategory A unique category label. Note that the unique category
     *                  labels are displayed. If this is not desired, blanks can be used.
     * @param anInfo    A {@link SwingRenderingInfo}. Must NOT be
     *                  <code>null</code>.
     */
	public void addValue(double aValue, Comparable<?> aSeries, Comparable<?> aCategory, SwingRenderingInfo anInfo) {
        setValue(aValue, aSeries, aCategory, anInfo);
    }
    
    /**
     * This method returns the {@link SwingRenderingInfo} for the given key or
     * <code>null</code> if no info could be found.
     * 
     * @param  aKey A key (@link #infos}.
     * @return Returns the {@link SwingRenderingInfo}  or <code>null</code>.
     */
    public SwingRenderingInfo getRenderingInfo(String aKey) {
        SwingRenderingInfo theInfo = this.infos.get(aKey);

        return (theInfo == null) ? null : theInfo;
    }
    
    /**
     * This method returns the {@link SwingRenderingInfo} for the given row and
     * column or <code>null</code> if no info could be found.
     * 
     * @param  aRow    A row.
     * @param  aColumn A column.
     * @return Returns the {@link SwingRenderingInfo} or <code>null</code>.
     */
    public SwingRenderingInfo getRenderingInfo(int aRow, int aColumn) {
        return this.getRenderingInfo(SwingDatasetUtilities.getKey(aRow, aColumn));
    }

    /**
	 * Set the value for maximum / minimum calculation.
	 * 
	 * @param aValue
	 *        The value to be set.
	 * @param aPos
	 *        The position in the {@link #colValues}.
	 * @see #getRange()
	 */
    public void setMaxMinValue(double aValue, int aPos) {
        this.colValues[aPos] = aValue;
    }

	/**
	 * Gets the value for maximum / minimum calculation.
	 * 
	 * @param slot
	 *        The position in the {@link #colValues}.
	 * @see #getRange()
	 */
	public double getMaxMinValue(int slot) {
		return colValues[slot];
	}

    /** 
     * Return the range the swing chart has to be rendered.
     * 
     * @return    The range the chart has to be rendered, never <code>null</code>.
     */
    public Range getRange() {
        double thePlanOld = this.colValues[0];
        double theDecNeg  = this.colValues[1];
        double theDecPos  = this.colValues[2];
        double thePlanAct = this.colValues[3];
        double thePlanNeg = this.colValues[4];
        double thePlanPos = this.colValues[5];
        double theGoal    = this.colValues[7];
        double theMinDiff = thePlanOld + theDecNeg;
        double theMaxDiff = thePlanOld + theDecPos;

        theMinDiff = Math.min(theMinDiff, thePlanAct + thePlanNeg);
        theMinDiff = Math.min(theMinDiff, theGoal);

        theMaxDiff = Math.max(theMaxDiff, thePlanAct + thePlanPos);
        theMaxDiff = Math.max(theMaxDiff, theGoal);

        double lower = theMinDiff - (theMinDiff * 0.1);
		double upper = theMaxDiff + (theMaxDiff * 0.05);

		// Range must not be of length 0, because this could result in server freeze
		// in cause of a division by zero in JFreeChart components.
		if (lower == 0.0 && upper == 0.0) {
			upper = 100.0;
		}
		// IGNORE FindBugs(FE_FLOATING_POINT_EQUALITY): Workaround for a bug in JFreeChart that
		// happens when the lower and the upper value of a range are exactly equal.
		else if (lower == upper) {
			lower = lower - 20.0;
			upper = upper + 20.0;
		}

		return new Range(lower, upper);
    }
}
