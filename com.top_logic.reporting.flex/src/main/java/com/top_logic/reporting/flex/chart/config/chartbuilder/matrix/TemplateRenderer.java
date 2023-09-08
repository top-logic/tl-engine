/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.matrix;

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;


/**
 * The TemplateRenderer is an abstract Renderer to draw diagrams. The
 * TemplateRenderer draws for each entry from a dataset an item into the diagram.
 * The color, size and form gets the renderer by delegation of its TemplateInfo.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class TemplateRenderer extends BarRenderer {

    /** The minimum shape size. */
    private int           shapeMinSize;
    /** The shape margin. */
    private double        shapeMargin;
    /** The gradient indicates how strong the fill colour tone is.  */
    private int           shapeGradientValue;
    
    /** The rendering info. */
    private TemplateInfo renderingInfo;
    
    /** 
     * Creates a {@link TemplateRenderer}.
     */
    public TemplateRenderer(TemplateInfo aInfo) {
        this.shapeMinSize       = 10;
        this.shapeMargin        = 15;
        this.shapeGradientValue = 30;
        this.renderingInfo      = aInfo;
    }
    
    @Override
	public void drawItem(Graphics2D aG2, CategoryItemRendererState aState, Rectangle2D aDataArea, CategoryPlot aPlot, CategoryAxis aCategoryAxis, ValueAxis aValueAxis, CategoryDataset aDataset, int aRow, int aColumn, int aPass) {
        double shapeSize = getShapeSize(aDataset, aDataArea, findMaxShapeSize(aDataArea, aDataset), this.shapeMinSize, aRow, aColumn) - getShapeMargin();
        if (shapeSize < this.shapeMinSize && shapeSize > 0) {shapeSize = this.shapeMinSize;}
        double shapeHalfSize = shapeSize / 2;
        
        RectangleEdge theLocation = aPlot.getRangeAxisEdge();
		double        shapeY      = aValueAxis.valueToJava2D(aRow, aDataArea, theLocation) - shapeHalfSize;
        double        shapeX      = aCategoryAxis.getCategoryMiddle(aColumn, getColumnCount(), aDataArea, aPlot.getDomainAxisEdge()) - shapeHalfSize;
        
        Object        item        = getShape(shapeX, shapeY, shapeSize, shapeSize, aRow, aColumn, aDataset);
        Shape         shape       = null;
        Image         image       = null;
        if (item instanceof Shape) {
            shape = (Shape)item;
            GradientPaint gradient = getGradientPaint(shape, aRow, aColumn, aDataset);
            aG2.setPaint(gradient);
            aG2.fill(shape);
            if(isDrawBarOutline()) {
                Stroke stroke = getItemOutlineStroke(aRow, aColumn);
                Paint  paint  = getItemOutlinePaint (aRow, aColumn);
                if(stroke != null && paint != null) {
                    aG2.setPaint (paint);
                    aG2.setStroke(stroke);
                }
            }
            aG2.draw(shape);            
        }
        else if (item instanceof Image) {
            image = (Image)item;
            if (shapeSize > 0) {
                aG2.drawImage(image, (int)shapeX, (int)shapeY, (int)shapeSize, (int)shapeSize, null);
            }
        }

        CategoryItemLabelGenerator labelGenerator = getItemLabelGenerator(aRow, aColumn);
        if (labelGenerator != null && isItemLabelVisible(aRow, aColumn) && shape != null) {
        	drawItemLabel(aG2, aDataset, aRow, aColumn, shapeX + shapeHalfSize, shapeY + shapeHalfSize, aPlot.getOrientation(), labelGenerator, false);
        }

        // Collect the tooltips and URLs for the image map.
        if (aState.getInfo() != null && shape != null) {
            ChartRenderingInfo renderingInfo = aState.getInfo().getOwner();
            if (renderingInfo == null) {
                // If the rendering info is null no tooltips and urls are needed.
                return;
            }
            EntityCollection   entities = renderingInfo.getEntityCollection();
            if (entities != null) {
            	String tooltip = null;
                CategoryToolTipGenerator tooltipGenerator = getToolTipGenerator(aRow, aColumn);
                if (tooltipGenerator != null) {
                    tooltip = tooltipGenerator.generateToolTip(aDataset, aRow, aColumn);
                }
                String url = null;
                CategoryURLGenerator urlGenerator = getItemURLGenerator(aRow, aColumn);
                if (urlGenerator != null) {
                    url = urlGenerator.generateURL(aDataset, aRow, aColumn);
                }
                CategoryItemEntity entity = new CategoryItemEntity(shape, tooltip, url,
					aDataset, aDataset.getRowKey(aRow),
					aDataset.getColumnKey(aColumn));
                entities.add(entity);
            }
        }
    }

    /**
     * Draws an item label.
     *
     * @param g2  the graphics device.
     * @param dataset  the dataset.
     * @param row  the series index (zero-based).
     * @param column  the item index (zero-based).
     * @param x  the x coordinate (in Java2D space).
     * @param y  the y coordinate (in Java2D space).
     * @param orientation  the orientation.
     * @param negative  indicates a negative value (which affects the item label position).
     */
    protected void drawItemLabel(Graphics2D g2, CategoryDataset dataset, int row, int column, double x, double y, PlotOrientation orientation, CategoryItemLabelGenerator generator, boolean negative) {
    	Font labelFont = getItemLabelFont(row, column);
    	Paint paint = getItemLabelPaint(row, column);
    	g2.setFont(labelFont);
    	g2.setPaint(paint);
        String label = generator.generateLabel(dataset, row, column);
        if (label == null) {
            return;  // nothing to do
        }

    	// get the label position..
    	ItemLabelPosition position = null;
    	if (!negative) {
    		position = getPositiveItemLabelPosition(row, column);
    	}
    	else {
    		position = getNegativeItemLabelPosition(row, column);
    	}

    	// work out the label anchor point...
    	Point2D anchorPoint = calculateLabelAnchorPoint(position.getItemLabelAnchor(), x, y, orientation);
		TextUtils.drawRotatedString(label, g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(),
    		position.getTextAnchor(), position.getAngle(), position.getRotationAnchor());
    }

    /**
     * This method returns the gradient paint for the given shape and its
     * additional information.
     */
    protected GradientPaint getGradientPaint(Shape aShape, int aRow, int aColumn, CategoryDataset aDataset) {
        return renderingInfo.getGradientPaint(aDataset, aShape, this.shapeGradientValue, aRow, aColumn);
    }
    
    /**
     * This method returns the shape for the given parameters.
     */
    protected Object getShape(double aX, double aY, double aWidth, double aHeight, int aRow, int aColumn, CategoryDataset aDataset) {
        return renderingInfo.getShape(aX, aY, aWidth, aHeight, aRow, aColumn, aDataset);
    }
   
    /**
     * This method returns the shape size.
     */
    protected double getShapeSize(CategoryDataset aDataset, Rectangle2D aDataArea, double aMaxSize, double aMinSize, int aRow, int aColumn) {
        return renderingInfo.getShapeSize(aDataset, aDataArea, aMaxSize, aMinSize, aRow, aColumn);
    }
    
    /**
     * This method returns the maximum shape size for the given data area and
     * dataset.
     * 
     * @param  aDataArea A {@link Rectangle2D}.
     * @param  aDataset  A {@link CategoryDataset}.
     * @return Returns the maximum shape size.
     */
    private double findMaxShapeSize(Rectangle2D aDataArea, CategoryDataset aDataset) {
        double xDistance = (aDataArea.getMaxX() - aDataArea.getMinX()) / aDataset.getColumnCount();
        double yDistance = (aDataArea.getMaxY() - aDataArea.getMinY()) / aDataset.getRowCount();
        
        return Math.min(xDistance, yDistance);
    }
    
    /**
     * This method returns the renderingInfo.
     * 
     * @return Returns the renderingInfo.
     */
    public TemplateInfo getRenderingInfo() {
        return this.renderingInfo;
    }

    /**
     * This method sets the renderingInfo.
     *
     * @param aRenderingInfo The renderingInfo to set.
     */
    public void setRenderingInfo(TemplateInfo aRenderingInfo) {
        this.renderingInfo = aRenderingInfo;
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

