/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

/**
 * The OverviewInfo contains the information for gradient paint and shape 
 * for the overview chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class OverviewInfo implements TemplateInfo {

    /** 
     * This method returns a gradient paint from upper left to lower right 
     * with the color from {@link #getShapeColor(int, int, CategoryDataset)}.
     * 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getGradientPaint(org.jfree.data.category.CategoryDataset, java.awt.Shape, int, int, int)
     */
    @Override
	public GradientPaint getGradientPaint(CategoryDataset aDataset, Shape aShape, int aGradientColorValue, int aRow, int aColumn) {
        Color itemColor  = getShapeColor(aRow, aColumn, aDataset);
        int   red        = itemColor.getRed()   - aGradientColorValue > 0 ? itemColor.getRed()   - aGradientColorValue : 0;
        int   green      = itemColor.getGreen() - aGradientColorValue > 0 ? itemColor.getGreen() - aGradientColorValue : 0;
        int   blue       = itemColor.getBlue()  - aGradientColorValue > 0 ? itemColor.getBlue()  - aGradientColorValue : 0;
        Color itemColor2 = new Color(red, green, blue);

        Rectangle2D   rec      = aShape.getBounds2D();
        return new GradientPaint((float)rec.getX(), (float)rec.getY(), itemColor, (float)(rec.getX() + rec.getWidth()), (float)(rec.getY() + rec.getHeight()), itemColor2);
    }

    /**
     * This method returns a Rectangle2D as shape.
     * 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShape(double, double, double, double, int, int, org.jfree.data.category.CategoryDataset)
     */
    @Override
	public Object getShape(double aX, double aY, double aWidth, double aHeight, int aRow, int aColumn, CategoryDataset aDataset) {
        return new Rectangle2D.Double(aX, aY , aWidth, aHeight);
    }
    
    /** 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShapeSize(org.jfree.data.category.CategoryDataset, java.awt.geom.Rectangle2D, double, double, int, int)
     */
    @Override
	public double getShapeSize(CategoryDataset aDataset, Rectangle2D aDataArea, double aMaxSize, double aMinSize, int aRow, int aColumn) {
        double value = ((Double)aDataset.getValue(aRow, aColumn)).doubleValue();
        
        double quadFactor  = (value / findMaxSize(aDataset));
        if (quadFactor < 0) {quadFactor = 0.0;}
        
        return aMaxSize * quadFactor;
    }

    /** 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShapeColor(int, int, org.jfree.data.category.CategoryDataset)
     */
    @Override
	public Color getShapeColor(int aSeries, int aCategory, CategoryDataset aDataset) {
        if      (aCategory == 0) {return Color.GREEN;}
        else if (aCategory == 1) {return Color.YELLOW;}
        else if (aCategory == 2) {return Color.RED;}
        else if (aCategory == 3) {return Color.BLUE;}
        else if (aCategory == 4) {return Color.ORANGE;}
        else if (aCategory == 5) {return Color.GRAY;}
        else if (aCategory == 6) {return Color.MAGENTA;}
        else if (aCategory == 7) {return Color.CYAN;}
        else if (aCategory == 8) {return Color.PINK;}
        else                     {return Color.GRAY;}
    }
    
    /**
     * This method returns the maximum value in the given dataset.
     * 
     * @param  aDataset {@link CategoryDataset}.
     * @return Returns the maximum value in the given dataset.
     */
    private double findMaxSize(CategoryDataset aDataset) {
		return ((Double) DatasetUtils.findMaximumRangeValue(aDataset)).doubleValue();
    }
    
}

