/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.matrix;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.data.category.CategoryDataset;

import com.top_logic.basic.col.ComparableComparator;

/**
 * Template information for matrix charts.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MatrixChartInfo implements TemplateInfo {

    public static final MatrixChartInfo RED_YELLOW_GREEN = new MatrixChartInfo(0.0f);

    public static final MatrixChartInfo GREEN_YELLOW_RED = new MatrixChartInfo(1.0f / 3.0f);

    private final float factor;

    /** 
     * Creates a {@link MatrixChartInfo}.
     * 
     * @param    aFactor    The inverting factor for the HSB colors.
     */
    protected MatrixChartInfo(float aFactor) {
        this.factor = aFactor;
    }

    @Override
	public GradientPaint getGradientPaint(CategoryDataset aDataset, Shape aShape, int aGradientColorValue, int aRow, int aColumn) {
        Color       theColor  = this.getShapeColor(aRow, aColumn, aDataset);
        int         theRed    = theColor.getRed()   - aGradientColorValue > 0 ? theColor.getRed()   - aGradientColorValue : 0;
        int         theGreen  = theColor.getGreen() - aGradientColorValue > 0 ? theColor.getGreen() - aGradientColorValue : 0;
        int         theBlue   = theColor.getBlue()  - aGradientColorValue > 0 ? theColor.getBlue()  - aGradientColorValue : 0;
        Color       theColor2 = new Color(theRed, theGreen, theBlue);
        Rectangle2D theRec    = aShape.getBounds2D();

        return new GradientPaint((float)theRec.getX(), (float)theRec.getY(), theColor, (float)(theRec.getX() + theRec.getWidth()), (float)(theRec.getY() + theRec.getHeight()), theColor2);
    }

    @Override
	public Object getShape(double aX, double aY, double aWidth, double aHeight, int aRow, int aColumn, CategoryDataset aDataset) {
        return new Ellipse2D.Double(aX, aY , aWidth, aHeight);
    }

    @Override
	public Color getShapeColor(int aRow, int aColumn, CategoryDataset aDataset) {
        int   theColumnCount = aDataset.getColumnCount();
        int   theRowCount    = aDataset.getRowCount();
        float theSaturation  = 0.1f + ((float) (aRow * 9) / (float) (theRowCount * 10));
        float theHue         = (float) aColumn / (float) (theColumnCount * 2);

        if (this.factor != 0.0f) {
            theHue = this.factor - theHue;
        }

        return Color.getHSBColor(theHue, theSaturation, 1.0f);
    }

    @Override
	public double getShapeSize(CategoryDataset aDataset, Rectangle2D aDataArea, double aMaxSize, double aMinSize, int aRow, int aColumn) {
        Number theValue = aDataset.getValue(aRow, aColumn);
        
        if (theValue.doubleValue() == 0.0f) { 
            return (2 * aMinSize);
        }
        else {
            List<Number> theDistinctSizes = this.findDistinctSizes(aDataset);
            double       theStep          = (aMaxSize - (2 * aMinSize)) / theDistinctSizes.size();
            double       theShapeSize     = ((theDistinctSizes.indexOf(theValue) + 1) * theStep) + (2 * aMinSize);

            return theShapeSize;
        }
    }
    
    private List<Number> findDistinctSizes(CategoryDataset aDataset) {
        List<Number> theResultList = new ArrayList<>();
        int          theColumns    = aDataset.getColumnCount();
        int          theRows       = aDataset.getRowCount();

		theResultList.add(Double.valueOf(0.0f));

        for (int i = 0; i < theRows; i++) {
            for (int j = 0; j < theColumns; j++) {
                Number theCurrent = aDataset.getValue(i,j);

                if (!theResultList.contains(theCurrent))
                    theResultList.add(theCurrent);
            }
        }

        theResultList.remove(0);

        Collections.sort(theResultList, ComparableComparator.INSTANCE);
        
        return theResultList;
    }
}

