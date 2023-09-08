/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.dataset;

import java.util.Date;
import java.util.Random;

import org.jfree.data.Range;

import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;

/**
 * The SwingDatasetUtilities contains useful static methods for the 
 * {@link com.top_logic.reporting.chart.dataset.SwingDataset}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingDatasetUtilities {
    
    /**
     * The private constructor avoids that an instance can be created.
     */
    private SwingDatasetUtilities() {/* empty block */}
    
    /**
     * This method returns for the given row and column an unique string.
     * 
     * @param  aRow    A row.
     * @param  aColumn A column.
     * @return Returns for the given row and column an unique string.
     */
    public static String getKey (int aRow, int aColumn) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(aRow);
        buffer.append(',');
        buffer.append(aColumn);
        return buffer.toString();
    }
    
    /** 
     * This method returns for the given {@link SwingDataset} the maximum and 
     * minimum value as range with additional margin for the given axis index. 
     * 
     * @param  aDataset      A {@link SwingDataset}.
     * @param  aAxis         An axis index.
     * @param  aMargin       A positive value 0 <= aMargin <= 1.
     * @return Returns the maximum and minimum value as range with margin.
     */
    public static Range findRange(SwingDataset aDataset, int aAxis, double aMargin) {
        return SwingDatasetUtilities.findRange(aDataset, aAxis, aMargin, aMargin);
    }

    /** 
     * This method returns for the given {@link SwingDataset} the maximum and 
     * minimum value as range with additional margin for the given axis index. 
     * 
     * @param  aDataset      A {@link SwingDataset}.
     * @param  aAxis         An axis index.
     * @param  aBottomMargin A positive value 0 <= aMinMargin <= 1.
     * @param  aTopMargin    A positive value 0 <= aMinMargin <= 1.
     * @return Returns the maximum and minimum value as range with margin.
     */
    public static Range findRange(SwingDataset aDataset, int aAxis, double aBottomMargin, double aTopMargin) {
        double max         = Double.NEGATIVE_INFINITY;
        double min         = Double.POSITIVE_INFINITY;
        double value       = 0.0;
        double globalValue = 0.0;
        double colSum      = 0.0;
        int    columnCount = aDataset.getColumnCount();

		if (columnCount > 0) {
        	for (int i = 0; i < columnCount; i++) {
        		SwingRenderingInfo info = aDataset.getRenderingInfo(0, i);
        		if (info == null || info.getValueAxisIndex() != aAxis) {continue;}
        		
        		value  = ((Double)aDataset.getValue(0, i)).doubleValue();
        		colSum = getSum(aDataset, i);
        		if (info.isNormalBar()) {
        			max         = Math.max(value, max);
        			min         = Math.min(value, min);
        			globalValue = value;
        		}
        		else {
        		    int theDiff = info.getDrawLineToPrev();

        		    if (theDiff < 0) {
        		        double thePrev = ((Double) aDataset.getValue(0, i + theDiff)).doubleValue();

        		        max = Math.max(globalValue + colSum, max);
        		        min = Math.min(thePrev + colSum, min);
                        globalValue += value;
        		    }
        		    else {
            			max = Math.max(globalValue + colSum, max);
            			min = Math.min(globalValue + colSum, min);
            			globalValue += value;
        		    }
        		}
        	}
        } 
		else {
        	max = 0;
        	min = 0;
        }

		double theDiff  = Math.abs(min) + Math.abs(max);
        double minRange = min - (aBottomMargin * theDiff);
		double maxRange = max + (aTopMargin * theDiff);

		return ChartUtil.normalizeRange(minRange < maxRange ? new Range(minRange, maxRange) : new Range(maxRange, minRange));
    }

    /**
     * This method returns the sum over the series of the given category.
     * 
     * @param  aDataset  A {@link SwingDataset}. Must NOT be <code>null</code>.
     * @param  aCategory A category index. Must be greater or equals than 0 and
     *                   smaller than the dataset column count.
     * @return Returns the sum over the series of the given category.
     */
    private static double getSum(SwingDataset aDataset, int aCategory) {
        if (aCategory < 0) {
            throw new IllegalArgumentException("The categroy index must be greater than 0.");
        }
        if (aCategory >= aDataset.getColumnCount()) {
            throw new IllegalArgumentException("The categroy index must be smaller than the dataset column count.");
        }
        
        double value = 0.0;
        for (int j = 0; j < aDataset.getRowCount(); j++) {
            Object number = aDataset.getValue(j, aCategory);
            if (number != null) {
                value += ((Double)number).doubleValue();
            }
        }
        return value;
    }
    
    /**
     * This method adds additional control points to the given dataset to 
     * synchronize the y-axes to the same y-value. The space from 0 to maximum
     * and the space from 0 to minimum has the same length.
     * 
     * @param aDataset    A {@link SwingDataset}.
     * @param anAxesCount A axes count for the given dataset.
     */
    public static void synchronizeYAxes(SwingDataset aDataset, int anAxesCount) {
        for (int i = 0; i < anAxesCount; i++) {
            Range  range         = SwingDatasetUtilities.findRange(aDataset, i, 0, 0);
            double upperBound    = range.getUpperBound();
            double lowerBound    = range.getLowerBound();
            double lowerBoundAbs = Math.abs(lowerBound);
            double pseudo        = upperBound >= lowerBoundAbs ? upperBound : lowerBound;
            pseudo *= -1;
            
            StringBuffer uniqueName = new StringBuffer();
            uniqueName.append(new Date().getTime());
            uniqueName.append(new Random().nextInt(999999999));
            uniqueName.append(i);
            
            /*
             * Do not remove the following lines! All dataset entries needed an
             * info object therefore the additional control points need an info
             * object, too. For the findRange method it is necessary that the
             * info object is a normal bar with the right axis.
             */
            SwingRenderingInfo info = new SwingRenderingInfo();
            info.setNormalBar(true);
            info.setValueAxisIndex(i);
            
            aDataset.setValue(pseudo, aDataset.getRowKey(0), uniqueName.toString(), info);
        }
    }
}

