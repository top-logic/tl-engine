/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.dataset.SwingDatasetUtilities;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;

/**
 * The SwingPlot contains two dataset one manipulated dataset with additional control
 * points to synchronize the y-axes and the normal dataset without the control points.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingPlot extends CategoryPlot {

    /** A {@link SwingDataset} without additional control points. */
    private SwingDataset swingDataset;
    
    /**  
     * Creates a {@link SwingPlot} with the
     * given parameters.
     * 
     * @param aDataset      A {@link SwingDataset} with additional control points 
     *                      to synchronize the y-axes.
     * @param aSwingDataset A {@link SwingDataset} without additional control points.
     * @param aDomainAxis   A domain (category) axis.
     * @param aRangeAxis    A range axis.
     * @param aRenderer     A renderer.
     */
    public SwingPlot(CategoryDataset aDataset, SwingDataset aSwingDataset, CategoryAxis aDomainAxis,
                     ValueAxis aRangeAxis, CategoryItemRenderer aRenderer) {
        super(aDataset, aDomainAxis, aRangeAxis, aRenderer);
        this.swingDataset = aSwingDataset;
    }
    
    /**
     * Overriden to replace the manipulated {@link SwingDataset} with the additional 
     * control points to synchronize the y-axes through the normal {@link SwingDataset} 
     * without control points.
     * 
     * @param aG2          A graphics device.
     * @param aDataArea    A area within which the plot (including axes) should be drawn.
     * @param anAnchor     A anchor point (<code>null</code> permitted).
     * @param aParentState A state from the parent plot, if there is one.
     * @param aState       Collects info as the chart is drawn (<code>null</code> permitted).
     * @see org.jfree.chart.plot.CategoryPlot#draw(java.awt.Graphics2D, java.awt.geom.Rectangle2D, java.awt.geom.Point2D, org.jfree.chart.plot.PlotState, org.jfree.chart.plot.PlotRenderingInfo)     
     */
    @Override
	public void draw(Graphics2D aG2, Rectangle2D aDataArea, Point2D anAnchor, PlotState aParentState, PlotRenderingInfo aState) {
        setDataset(this.swingDataset);
        super.draw(aG2, aDataArea, anAnchor, aParentState, aState);
    }
    
	@Override
	public List getCategoriesForAxis(CategoryAxis axis) {
		List res = new ArrayList();
		for (int i = 0; i < swingDataset.getColumnCount(); i++) {
			Comparable category = swingDataset.getColumnKey(i);
			SwingRenderingInfo renderingInfo = swingDataset.getRenderingInfo(getCategoryColumnNameName(i));
			if (renderingInfo == null || renderingInfo.isVisible()) {
				res.add(category);
			}
		}
		return res;
	};

	/**
	 * the number of visible columns of this plot
	 */
	public int getVisibleColumnCount() {
		return getNumberOfVisibleColumnsUpToColumn(swingDataset.getColumnCount());
	}

	private String getCategoryColumnNameName(int column) {
		int theSeries = 0;
		List<?> theKeys = swingDataset.getRowKeys();

		if (theKeys.size() > 1) {
			theSeries = ((Integer) theKeys.get(0)).intValue();
		}

		return SwingDatasetUtilities.getKey(theSeries, column);
	}

	/**
	 * the number of visible columns with an index less than maxColumn
	 */
	public int getNumberOfVisibleColumnsUpToColumn(int maxColumn) {
		int visibleColumns = 0;
		for (int i = 0; i < maxColumn; i++) {
			SwingRenderingInfo renderingInfo = swingDataset.getRenderingInfo(getCategoryColumnNameName(i));
			if (renderingInfo == null || renderingInfo.isVisible()) {
				visibleColumns++;
			}
		}
		return visibleColumns;
	}

}

