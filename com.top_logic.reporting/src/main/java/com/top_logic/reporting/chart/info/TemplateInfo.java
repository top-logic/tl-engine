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

/**
 * The TemplateInfo contains needed information to generate a chart
 * with the {@link com.top_logic.reporting.chart.renderer.TemplateRenderer}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public interface TemplateInfo {
    
    /**
     * This method returns the gradient paint for the given shape and its
     * additional information.
     * 
     * @param  aDataset            A {@link CategoryDataset}.
     * @param  aShape              A {@link Shape}.
     * @param  aGradientColorValue A value between 0...n which indicates how
     *                             strong the gradient paint is.
     * @param  aRow                A row.
     * @param  aColumn             A column.
     * @return Returns the gradient paint for the given shape and its additional
     *         information.
     */
    public GradientPaint getGradientPaint(CategoryDataset aDataset, Shape aShape, int aGradientColorValue, int aRow, int aColumn);

    /**
     * This method returns the shape for the given parameters 
     * (e.g. Rectangle2D or Ellipse2D).
     * 
     * @param  aX       A x-coordinate.
     * @param  aY       A y-coordinate.
     * @param  aWidth   A width.
     * @param  aHeight  A height.
     * @param  aRow     A row.
     * @param  aColumn  A column.
     * @param  aDataset A Dataset.
     * @return Returns the shape for the given parameters.
     */
    public Object getShape(double aX, double aY, double aWidth, double aHeight, int aRow, int aColumn, CategoryDataset aDataset);
    
    /**
     * This method returns a color for the given row and column.
     * 
     * @param  aRow     A row.
     * @param  aColumn  A column.
     * @param  aDataset A {@link CategoryDataset}.
     * @return Returns a color for the given series and category.
     */
    public Color getShapeColor(int aRow, int aColumn, CategoryDataset aDataset);

    /**
     * This method returns the shape size.
     * 
     * @param  aDataset  A {@link CategoryDataset}.
     * @param  aDataArea A {@link Rectangle2D}.
     * @param  aMaxSize  A maximum size for the shape.
     * @param  aMinSize  A minimum size for the shape
     * @param  aRow      A row.
     * @param  aColumn   A column.
     * @return Returns the shape size.
     */
    public double getShapeSize(CategoryDataset aDataset, Rectangle2D aDataArea, double aMaxSize, double aMinSize, int aRow, int aColumn);

}

