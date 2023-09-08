/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.renderer;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.ChartColor;
import org.jfree.chart.renderer.category.BarRenderer;

import com.top_logic.mig.util.ColorUtil;

/**
 * The {@link BarRenderer} draws the series with the same color through the categories.
 * The DifferentSeriesColorsBarRenderer can draw the series with different colors.  
 * E.g.             BarRenderer     DifferentSeriesColorsBarRenderer
 *  Category A
 *      Series 1 =      blue                blue
 *      Series 2 =      red                 yellow
 *  Category B
 *      Series 1 =      blue                red
 *      Series 2 =      red                 green
 *      
 * The {@link DifferentSeriesColorsBarRenderer} use the gradient paint 
 * with the intensity (50) as default.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DifferentSeriesColorsBarRenderer extends BarRenderer {

    private static int     DEFAULT_INTENSITY      = 50;
    private static boolean DEFAULT_GRADIENT_PAINT = true;

    private Paint[] colors;
    private int     intensity;
    private boolean gradientPaint;

    /**
     * Creates a {@link DifferentSeriesColorsBarRenderer}.
     */
    public DifferentSeriesColorsBarRenderer() {
        this.colors        = ChartColor.createDefaultPaintArray();
        this.intensity     = DEFAULT_INTENSITY;
        this.gradientPaint = DEFAULT_GRADIENT_PAINT;
    }
    
    /**
     * Creates a {@link DifferentSeriesColorsBarRenderer} with
     * the given parameters.
     * 
     * @param someColors
     *        An array of {@link Color}s.
     */
    public DifferentSeriesColorsBarRenderer(Color[] someColors) {
        this(someColors, DEFAULT_GRADIENT_PAINT);
    }
    
    /**
     * Creates a {@link DifferentSeriesColorsBarRenderer} with the
     * given parameters.
     * 
     * @param someColors
     *        An array of {@link Color}s.
     * @param isGradientPaint
     *        Indicates whether gradient paints instead of normal colors are
     *        used.
     */
    public DifferentSeriesColorsBarRenderer(Color[] someColors, boolean isGradientPaint) {
       this(someColors, DEFAULT_INTENSITY, isGradientPaint);
    }
    
    /**
     * Creates a {@link DifferentSeriesColorsBarRenderer} with the
     * given parameters.
     * 
     * @param someColors
     *        An array of {@link Color}s.
     * @param anIntensity
     *        An gradient paint intensity (0.255).
     * @param isGradientPaint
     *        Indicates whether gradient paints instead of normal colors are
     *        used.
     */
    public DifferentSeriesColorsBarRenderer(Color[] someColors, int anIntensity, boolean isGradientPaint) {
        this.colors        = someColors;
        this.intensity     = anIntensity;
        this.gradientPaint = isGradientPaint;
    }

    /**
     * This method returns for the given row and column a {@link Paint}.
     * 
     * @param aRow
     *        A row index (zero-based).
     * @param aColumn
     *        A column index (zero-based).
     * @return Returns for the given row and column a {@link Paint}.
     * @see org.jfree.chart.renderer.AbstractRenderer#getItemPaint(int, int)
     */
    @Override
	public Paint getItemPaint(int aRow, int aColumn) {
        Paint thePaint = this.colors[aColumn % colors.length];
        if (isGradientPaint()) {
            thePaint = ColorUtil.getGradientPaintFor((Color)thePaint, intensity);
        }
        
        return thePaint;
    }

    /**
     * This method returns the colors.
     * 
     * @return Returns the colors.
     */
    public Paint[] getColors() {
        return this.colors;
    }
    
    /**
     * This method sets the colors.
     *
     * @param aColors The colors to set.
     */
    public void setColors(Paint[] aColors) {
        this.colors = aColors;
    }
    
    /**
     * This method returns the gradientPaint.
     * 
     * @return Returns the gradientPaint.
     */
    public boolean isGradientPaint() {
        return this.gradientPaint;
    }

    /**
     * This method sets the gradientPaint.
     *
     * @param aGradientPaint The gradientPaint to set.
     */
    public void setGradientPaint(boolean aGradientPaint) {
        this.gradientPaint = aGradientPaint;
    }
    
    /**
     * This method returns the intensity.
     * 
     * @return Returns the intensity.
     */
    public int getIntensity() {
        return this.intensity;
    }
    
    /**
     * This method sets the intensity.
     *
     * @param aIntensity The intensity to set.
     */
    public void setIntensity(int aIntensity) {
        this.intensity = aIntensity;
    }
    
}

