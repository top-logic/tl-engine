/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.data.category.CategoryDataset;

/**
 * The ProgressInfo contains the information for gradient paint and shape 
 * for the progress chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ProgressInfo implements TemplateInfo {

    /** 
     * This method returns a gradient paint from upper left to lower right 
     * with the color from the given painter.
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
     * This method returns a Ellipse2D as shape.
     * 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShape(double, double, double, double, int, int, org.jfree.data.category.CategoryDataset)
     */
    @Override
	public Object getShape(double aX, double aY, double aWidth, double aHeight, int aRow, int aColumn, CategoryDataset aDataset) {
        return new Ellipse2D.Double(aX, aY , aWidth, aHeight);
    }
    
    /** 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShapeColor(int, int, org.jfree.data.category.CategoryDataset)
     */
    @Override
	public Color getShapeColor(int aRow, int aColumn, CategoryDataset aDataset) {
        double value = ((Double)aDataset.getValue(aRow, aColumn)).doubleValue();
        if      (value == 0) {return Color.GREEN;}
        else if (value == 1) {return Color.YELLOW;}
        else if (value == 2) {return Color.RED;}
        else if (value == 3) {return Color.BLUE;}
        else if (value == 4) {return Color.ORANGE;}
        else if (value == 5) {return Color.GRAY;}
        else if (value == 6) {return Color.MAGENTA;}
        else if (value == 7) {return Color.CYAN;}
        else if (value == 8) {return Color.PINK;}
        else                 {return Color.GRAY;}
    }
    
    /** 
     * @see com.top_logic.reporting.chart.info.TemplateInfo#getShapeSize(org.jfree.data.category.CategoryDataset, java.awt.geom.Rectangle2D, double, double, int, int)
     */
    @Override
	public double getShapeSize(CategoryDataset aDataset, Rectangle2D aDataArea, double aMaxSize, double aMinSize, int aRow, int aColumn) {
        return aMaxSize;
    }

}

