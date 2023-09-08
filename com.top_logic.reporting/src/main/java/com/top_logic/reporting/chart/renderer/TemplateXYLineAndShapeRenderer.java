/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TemplateXYLineAndShapeRenderer extends XYLineAndShapeRenderer {

    /** The rendering info. */
    private ItemShapeProvider  shapeProvider;
    /** The minimum shape size. */
    private int           shapeMinSize;
    /** The shape margin. */
    private double        shapeMargin;
    /** The gradient indicates how strong the fill colour tone is.  */
    private int           shapeGradientValue;
    
    public TemplateXYLineAndShapeRenderer(ItemShapeProvider aShapeProvider) {
    	super();
    	this.shapeProvider      = aShapeProvider;
    	this.shapeMinSize       = 10;
        this.shapeMargin        = 15;
        this.shapeGradientValue = 30;
    }
    
    /**
     * Draws the item shapes and adds chart entities (second pass). This method 
     * draws the shapes which mark the item positions. If <code>entities</code> 
     * is not <code>null</code> it will be populated with entity information.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area within which the data is being drawn.
     * @param plot  the plot (can be used to obtain standard color 
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  the crosshair state.
     * @param entities the entity collection.
     */
	@Override
	protected void drawSecondaryPass(Graphics2D g2, XYPlot plot, XYDataset dataset, int pass, int series, int item, ValueAxis domainAxis,
	        Rectangle2D dataArea, ValueAxis rangeAxis, CrosshairState crosshairState, EntityCollection entities) {

		Shape entityArea = null;

		// get the data point...
		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);
		if (Double.isNaN(y1) || Double.isNaN(x1)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

		if (getItemShapeVisible(series, item)) {
			Object theItem = getShape(series, item);
			if(theItem instanceof Shape) {
				Shape shape = (Shape) theItem;
				if (orientation == PlotOrientation.HORIZONTAL) {
					shape = ShapeUtils.createTranslatedShape(shape, transY1, transX1);
				}
				else if (orientation == PlotOrientation.VERTICAL) {
					shape = ShapeUtils.createTranslatedShape(shape, transX1, transY1);
				}
				entityArea = shape;
				if (shape.intersects(dataArea)) {
					if (getItemShapeFilled(series, item)) {
						if (this.getUseFillPaint()) {
							g2.setPaint(getItemFillPaint(series, item));
						}
						else {
							g2.setPaint(getItemPaint(series, item));
						}
						g2.fill(shape);
					}
					if (this.getDrawOutlines()) {
						if (getUseOutlinePaint()) {
							g2.setPaint(getItemOutlinePaint(series, item));
						}
						else {
							g2.setPaint(getItemPaint(series, item));
						}
						g2.setStroke(getItemOutlineStroke(series, item));
						g2.draw(shape);
					}
				}
			}
			else if(theItem instanceof Image) {
				Image theImage = (Image) theItem;
				g2.drawImage(theImage, (int)transX1, (int)transY1,10,10, null);
			}
		}

		// draw the item label if there is one...
		if (isItemLabelVisible(series, item)) {
			double xx = transX1;
			double yy = transY1;
			if (orientation == PlotOrientation.HORIZONTAL) {
				xx = transY1;
				yy = transX1;
			}
			drawItemLabel(g2, orientation, dataset, series, item, xx, yy, (y1 < 0.0));
		}

		updateCrosshairValues(crosshairState, x1, y1, series, transX1, transY1, plot.getOrientation());

		// add an entity for the item...
		if (entities != null) {
			addEntity(entities, entityArea, dataset, series, item, transX1, transY1);
		}
	}

	public Object getShape(int row, int column) {
		if(this.shapeProvider.useImage(row, column)) {
			return this.shapeProvider.getShape(row, column);
		}
		else {
			return super.getItemShape(row, column);
		}
	}
    
    
    /**
     * This method returns the shapeGradientValue.
     * 
     * @return Returns the shapeGradientValue.
     */
    public int getShapeGradientValue() {
        return this.shapeGradientValue;
    }
    
    /**
     * This method sets the shapeGradientValue.
     *
     * @param aShapeGradientValue The shapeGradientValue to set.
     */
    public void setShapeGradientValue(int aShapeGradientValue) {
        this.shapeGradientValue = aShapeGradientValue;
    }

    /**
     * This method returns the shapeMargin.
     * 
     * @return Returns the shapeMargin.
     */
    public double getShapeMargin() {
        return this.shapeMargin;
    }

    /**
     * This method sets the shapeMargin.
     *
     * @param aShapeMargin The shapeMargin to set.
     */
    public void setShapeMargin(double aShapeMargin) {
        this.shapeMargin = aShapeMargin;
    }

    /**
     * This method returns the shapeMinSize.
     * 
     * @return Returns the shapeMinSize.
     */
    public int getShapeMinSize() {
        return this.shapeMinSize;
    }

    /**
     * This method sets the shapeMinSize.
     *
     * @param aShapeMinSize The shapeMinSize to set.
     */
    public void setShapeMinSize(int aShapeMinSize) {
        this.shapeMinSize = aShapeMinSize;
    }
}
