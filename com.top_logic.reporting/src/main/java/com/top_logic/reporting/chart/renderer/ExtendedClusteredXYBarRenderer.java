/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * This class is mainly copied from {@link ClusteredXYBarRenderer}. The only
 * change is a possibility to set a maximum bar width.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ExtendedClusteredXYBarRenderer extends ClusteredXYBarRenderer {
	
	/** maximum bar width in percent of the drawing area */
	private double maxBarWidth;
	
    /** The minimum bar length (in Java2D units). */
    private double minimumBarLength;
	
    /** Determines whether bar center should be interval start. */
    private boolean centerBarAtStartValue;
    
    public ExtendedClusteredXYBarRenderer() {
    	super(0.0, false);
    }
    
    /**
     * Constructs a new XY clustered bar renderer.
     *
     * @param margin  the percentage amount to trim from the width of each bar.
     * @param centerBarAtStartValue  if true, bars will be centered on the start 
     *                               of the time period.
     */
    public ExtendedClusteredXYBarRenderer(double margin, boolean centerBarAtStartValue) {
    	super(margin, centerBarAtStartValue);
    	this.centerBarAtStartValue = centerBarAtStartValue;
    	this.minimumBarLength = 0.0;
    }
	
    /**
     * @author JFreeChart : a free chart library for the Java(tm) platform
     * 
     * Draws the visual representation of a single data item. This method
     * is mostly copied from the superclass, the change is that in the
     * calculated space for a singe bar we draw bars for each series next to
     * each other. The width of each bar is the available width divided by
     * the number of series. Bars for each series are drawn in order left to
     * right.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color 
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param item  the item index.
     * @param crosshairState  crosshair information for the plot 
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
	@Override
	public void drawItem(Graphics2D g2, 
						 XYItemRendererState state, 
						 Rectangle2D dataArea, 
						 PlotRenderingInfo info, 
						 XYPlot plot, 
						 ValueAxis domainAxis,
						 ValueAxis rangeAxis, 
						 XYDataset dataset, 
						 int series, 
						 int item, 
						 CrosshairState crosshairState, 
						 int pass) {
        IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

        double value0;
        double value1;
        if (getUseYInterval()) {
            value0 = intervalDataset.getStartYValue(series, item);
            value1 = intervalDataset.getEndYValue(series, item);
        }
        else {
            value0 = getBase();
            value1 = intervalDataset.getYValue(series, item);
        }
        if (Double.isNaN(value0) || Double.isNaN(value1)) {
            return;
        }

        double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea, 
                plot.getRangeAxisEdge());
        double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea, 
                plot.getRangeAxisEdge());

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        double x1 = intervalDataset.getStartXValue(series, item);
        double translatedX1 = domainAxis.valueToJava2D(x1, dataArea, 
                xAxisLocation);

        double x2 = intervalDataset.getEndXValue(series, item);
        double translatedX2 = domainAxis.valueToJava2D(x2, dataArea, 
                xAxisLocation);

        double translatedWidth = Math.max(1, Math.abs(translatedX2 
                - translatedX1));
        double translatedHeight = Math.abs(translatedValue0 - translatedValue1);

        if (this.centerBarAtStartValue) {
            translatedX1 -= translatedWidth / 2;
        }

        PlotOrientation orientation = plot.getOrientation();        
        double m = getMargin();
        if (m > 0.0) {
            double cut = translatedWidth * getMargin();
            translatedWidth = translatedWidth - cut;
            if (orientation == PlotOrientation.HORIZONTAL)
              translatedX1 = translatedX1 - cut / 2;                
            else
              translatedX1 = translatedX1 + cut / 2;
        }

        int numSeries = dataset.getSeriesCount();
        
        /**
         * Added to calculate a maximum bar width in percent of the drawing area.
         */
        double dataAreaWidth = dataArea.getWidth();
        double maximumWidth = dataAreaWidth * this.maxBarWidth;
        
        double barMiddle = translatedWidth/2 + translatedX1;
        double seriesBarWidth = translatedWidth / numSeries;
        
        seriesBarWidth = maximumWidth < seriesBarWidth ? maximumWidth : seriesBarWidth;
        barMiddle -= (numSeries * seriesBarWidth/2);
        
        translatedHeight = translatedHeight < this.minimumBarLength ? this.minimumBarLength : translatedHeight;

        Rectangle2D bar = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(Math.min(translatedValue0, 
                    translatedValue1), translatedX1 - seriesBarWidth 
                    * (numSeries - series), translatedHeight, seriesBarWidth);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            bar = new Rectangle2D.Double(barMiddle + seriesBarWidth * series,
                    Math.min(translatedValue0, translatedValue1),
                    seriesBarWidth, translatedHeight);
        }
        Paint itemPaint = getItemPaint(series, item);
        if (getGradientPaintTransformer() 
                != null && itemPaint instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint) itemPaint;
            itemPaint = getGradientPaintTransformer().transform(gp, bar);
        }
        g2.setPaint(itemPaint);

        g2.fill(bar);
        if (isDrawBarOutline() && Math.abs(translatedX2 - translatedX1) > 3) {
            g2.setStroke(getItemOutlineStroke(series, item));
            g2.setPaint(getItemOutlinePaint(series, item));
            g2.draw(bar);
        }

        if (isItemLabelVisible(series, item)) {
            XYItemLabelGenerator generator = getItemLabelGenerator(series, 
                    item);
            drawItemLabel(g2, dataset, series, item, plot, generator, bar, 
                    value1 < 0.0);
        }

        // add an entity for the item...
        if (info != null) {
            EntityCollection entities = info.getOwner().getEntityCollection();
            if (entities != null) {
                String tip = null;
                XYToolTipGenerator generator 
                    = getToolTipGenerator(series, item);
                if (generator != null) {
                    tip = generator.generateToolTip(dataset, series, item);
                }
                String url = null;
                if (getURLGenerator() != null) {
                    url = getURLGenerator().generateURL(dataset, series, item);
                }
                XYItemEntity entity = new XYItemEntity(bar, dataset, series, 
                        item, tip, url);
                entities.add(entity);
            }
        }
	}

	/**
     * Returns the maxBarWidth.
     */
    public double getMaxBarWidth() {
    	return (maxBarWidth);
    }

	/**
     * @param    maxBarWidth    The maximum bar width in percent of the drawing area.
     */
    public void setMaxBarWidth(double maxBarWidth) {
    	this.maxBarWidth = maxBarWidth;
    }

	/**
     * Returns the minimumBarLength.
     */
    public double getMinimumBarLength() {
    	return (minimumBarLength);
    }

    /**
     * @author JFreeChart : a free chart library for the Java(tm) platform
     * 
     * Sets the minimum bar length and sends a {@link RendererChangeEvent} to 
     * all registered listeners.  The minimum bar length is specified in Java2D
     * units, and can be used to prevent bars that represent very small data 
     * values from disappearing when drawn on the screen.
     * 
     * @param minimumBarLength  the minimum bar length (in Java2D units).
     * 
     * @see #getMinimumBarLength()
     */
    public void setMinimumBarLength(double minimumBarLength) {
    	this.minimumBarLength = minimumBarLength;
    }
}
