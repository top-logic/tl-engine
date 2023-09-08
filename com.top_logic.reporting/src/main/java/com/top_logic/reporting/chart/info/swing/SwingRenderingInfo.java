/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

/**
 * The SwingRenderingInfo contains information for the 
 * {@link com.top_logic.reporting.chart.renderer.SwingRenderer} to draw an item 
 * which is related to this info object.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingRenderingInfo {

    /**
     * The swing info stores all data which are necessary to represent an
     * element in a swing chart. If the data in a special case are not enough,
     * the {@link #information} object can be used to store arbitrary additional
     * information.
     */
    private Object information;
    
    /** The tooltip value. */
    private double value;
    
    /**
     * Indicate whether the rendering object is a normal or special bar.
     */
    private boolean normalBar;
    /**
     * Indicate whether the previous bar is overwritten. 
     */
    private boolean overwritePrev;
    /**
     * Indicate whether the rendering object and the previous object are
     * associated with a line.
     */
    private int drawLineToPrev;
    
    /**
     * Indicate whether the rendering object shown as an any icon (e.g.
     * triangle) and NOT as a bar.
     */
    private boolean showAsIcon;

    /** Indicates whether the bar is drawn. */
    private boolean visible;

    /**
     * Indicate the icon orientation (e.g. LEFT or RIGHT).
     * {@link com.top_logic.reporting.chart.renderer.SwingRenderer#LEFT}
     * {@link com.top_logic.reporting.chart.renderer.SwingRenderer#RIGHT}
     */
    private int     iconOrientation;
    
    /** The corresponding axis index for the value of the rendering object. */
    private int     valueAxisIndex;
    
    /** The color of the rendering object. */
    private Color   color;
    
    /**
	 * Creates a {@link SwingRenderingInfo}.
	 */
    public SwingRenderingInfo() {
        this.visible = true;
    }

    /** 
     * Create a new SwingRenderingInfo with all (but visible) options.
     * 
     * @param aValue            The tooltip value.
     * @param lineToPrev        Draw a line to previous Bar.
     * @param aIconOrientation  either SwingRenderer#LEFT or SwingRenderer#LEFT
     * @param aColor             Color of the rendering object. 
     */
    public SwingRenderingInfo(double aValue, boolean aNormalBar, boolean aOverwritePrev, boolean lineToPrev, boolean aShowAsIcon, int aIconOrientation, int aValueAxisIndex, Color aColor) {
        this(aValue, aNormalBar, aOverwritePrev, lineToPrev ? -1 : 0, aShowAsIcon, aIconOrientation, aValueAxisIndex, aColor);
    }
     
    public SwingRenderingInfo(double aValue, boolean aNormalBar, boolean aOverwritePrev, int lineToPrev, boolean aShowAsIcon, int aIconOrientation, int aValueAxisIndex, Color aColor) {
        value               = aValue;
        normalBar           = aNormalBar;
        overwritePrev       = aOverwritePrev;
        drawLineToPrev      = lineToPrev;
        showAsIcon          = aShowAsIcon;
        visible             = true;
        iconOrientation     = aIconOrientation;
        valueAxisIndex      = aValueAxisIndex;
        color               = aColor;
    }

    /** 
     * Create a new SwingRenderingInfo with the option to use an icon.
     * 
     * @param aValue            The tooltip value.
     * @param lineToPrev        Draw a line to previous Bar.
     * @param aShowAsIcon       true: drwaw an icon instead of the normal bar is shown.
     * @param aIconOrientation  either SwingRenderer#LEFT or SwingRenderer#LEFT
     */
    public SwingRenderingInfo(double aValue, boolean lineToPrev, boolean aShowAsIcon, int aIconOrientation) {
        this(aValue, lineToPrev, aShowAsIcon, aIconOrientation, 0);
    }
    
    /**
	 * Create a new SwingRenderingInfo with the option to use an icon.
	 * 
	 * @param aValue
	 *        The tooltip value.
	 * @param lineToPrev
	 *        Draw a line to previous Bar.
	 * @param aShowAsIcon
	 *        true: drwaw an icon instead of the normal bar is shown.
	 * @param aIconOrientation
	 *        either SwingRenderer#LEFT or SwingRenderer#LEFT
	 * @param aAxisIndex
	 *        The value axis index.
	 */
    public SwingRenderingInfo(double aValue, boolean lineToPrev, boolean aShowAsIcon, int aIconOrientation, int aAxisIndex) {
        this(aValue, lineToPrev, aShowAsIcon, aIconOrientation, aAxisIndex, null);
    }

    public SwingRenderingInfo(double aValue, boolean lineToPrev, boolean aShowAsIcon, int aIconOrientation, int aAxisIndex, Object anInformation) {
        value           = aValue;
        normalBar       = true;
        drawLineToPrev  = lineToPrev ? -1 : 0;
        showAsIcon      = aShowAsIcon;
        visible         = true;
        iconOrientation = aIconOrientation;
        color           = Color.LIGHT_GRAY;
        valueAxisIndex  = aAxisIndex;
        information     = anInformation;
    }
    
    /** 
     * Create a new SwingRenderingInfo with tooltip and color.
     * 
     * (Will always drawLineToPrev)
     * 
     * @param aValue            The tooltip value.
     * @param aColor            Color of the rendering object. 
     */
    public SwingRenderingInfo(double aValue, Color aColor) {
        this(aValue, aColor, 0);
    }
    
    /** 
     * Create a new SwingRenderingInfo with tooltip and color.
     * 
     * (Will always drawLineToPrev)
     * 
     * @param aValue            The tooltip value.
     * @param aColor            Color of the rendering object. 
     */
    public SwingRenderingInfo(double aValue, Color aColor, int anAxisIndex) {
        this(aValue, aColor, anAxisIndex, null);
    }

    public SwingRenderingInfo(double aValue, Color aColor, int anAxisIndex, Object anInformation) {
        this(aValue, true, aColor, anAxisIndex, anInformation);
    }
    
    /** 
     * Create a new SwingRenderingInfo with tooltip and color.
     * 
     * (Will always drawLineToPrev)
     * 
     * @param aValue            The tooltip value.
     * @param lineToPrev        Draw a line to previous Bar.
     * @param aColor            Color of the rendering object. 
     */
     public SwingRenderingInfo(double aValue,  boolean lineToPrev, Color aColor) {
        this(aValue, lineToPrev, aColor, 0);
    }

     /** 
      * Create a new SwingRenderingInfo with tooltip and color.
      * 
      * (Will always drawLineToPrev)
      * 
      * @param aValue            The tooltip value.
      * @param lineToPrev        Draw a line to previous Bar.
      * @param aColor            Color of the rendering object. 
      */
     public SwingRenderingInfo(double aValue,  boolean lineToPrev, Color aColor, int anAxisIndex) {
         this(aValue, lineToPrev, aColor, anAxisIndex, null);
     }

     public SwingRenderingInfo(double aValue,  boolean lineToPrev, Color aColor, int anAxisIndex, Object anInformation) {
         value           = aValue;
         drawLineToPrev  = lineToPrev ? -1 : 0;
         visible         = true;
         color           = aColor;
         valueAxisIndex  = anAxisIndex;
         information     = anInformation;
     }

    /** 
     * Create a new SwingRenderingInfo with tooltip and default color.
     * 
     * (Will always drawLineToPrev)
     * 
     * @param aValue            The tooltip value.
     */
     public SwingRenderingInfo(double aValue) {
        value           = aValue;
        drawLineToPrev  = -1;
        color           = Color.LIGHT_GRAY;
    }

    /**
     * This method returns the color.
     * 
     * @return Returns the color.
     */
    public Color getColor() {
        return this.color;
    }
    
    /**
     * This method sets the color.
     *
     * @param aColor The color to set.
     */
    public void setColor(Color aColor) {
        this.color = aColor;
    }
    
    /**
     * This method returns the drawLineToPrev.
     * 
     * @return Returns the drawLineToPrev.
     */
    public boolean isDrawLineToPrev() {
        return this.drawLineToPrev < 0;
    }

    /**
     * This method returns the number of the column to draw a line to (relative from this column).
     *
     * @return    The number of columns to draw the line to.
     */
    public int getDrawLineToPrev() {
        return this.drawLineToPrev;
    }    

    /**
     * This method sets the drawLineToPrev.
     *
     * @param aDrawLineToPrev The drawLineToPrev to set.
     */
    public void setDrawLineToPrev(boolean aDrawLineToPrev) {
        this.drawLineToPrev = aDrawLineToPrev ? -1 : 0;
    }
    
    /**
     * This method returns the normalBar.
     * 
     * @return Returns the normalBar.
     */
    public boolean isNormalBar() {
        return this.normalBar;
    }
    
    /**
     * This method sets the normalBar.
     *
     * @param aNormalBar The normalBar to set.
     */
    public void setNormalBar(boolean aNormalBar) {
        this.normalBar = aNormalBar;
    }
    
    /**
     * This method returns the showAsIcon.
     * 
     * @return Returns the showAsIcon.
     */
    public boolean isShowAsIcon() {
        return this.showAsIcon;
    }

    /**
     * This method sets the showAsIcon.
     *
     * @param aShowAsIcon The showAsIcon to set.
     */
    public void setShowAsIcon(boolean aShowAsIcon) {
        this.showAsIcon = aShowAsIcon;
    }

    /**
     * This method returns the iconOrientation.
     * 
     * @return Returns the iconOrientation.
     */
    public int getIconOrientation() {
        return this.iconOrientation;
    }

    /**
     * This method sets the iconOrientation.
     *
     * @param aIconOrientation The iconOrientation to set.
     */
    public void setIconOrientation(int aIconOrientation) {
        this.iconOrientation = aIconOrientation;
    }
    
    /**
     * This method returns the valueAxisIndex.
     * 
     * @return Returns the valueAxisIndex.
     */
    public int getValueAxisIndex() {
        return this.valueAxisIndex;
    }

    /**
     * This method sets the valueAxisIndex.
     *
     * @param aValueAxisIndex The valueAxisIndex to set.
     */
    public void setValueAxisIndex(int aValueAxisIndex) {
        this.valueAxisIndex = aValueAxisIndex;
    }
    
    /**
     * This method returns the value.
     * 
     * @return Returns the value.
     */
    public double getValue() {
        return this.value;
    }
    
    /**
     * This method sets the value.
     *
     * @param aValue The value to set.
     */
    public void setValue(double aValue) {
        this.value = aValue;
    }

    /**
     * This method returns the overwritePrev.
     * 
     * @return Returns the overwritePrev.
     */
    public boolean isOverwritePrev() {
        return this.overwritePrev;
    }
    
    /**
     * This method sets the overwritePrev.
     *
     * @param aOverwritePrev The overwritePrev to set.
     */
    public void setOverwritePrev(boolean aOverwritePrev) {
        this.overwritePrev = aOverwritePrev;
    }
    
    /**
     * This method returns the visible.
     * 
     * @return Returns the visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * This method sets the visible.
     *
     * @param aVisible The visible to set.
     */
    public void setVisible(boolean aVisible) {
        this.visible = aVisible;
    }
    
    /** See {@link #information}. */
    public Object getInformation() {
        return this.information;
    }

    /** See {@link #information}. */
    public void setInformation(Object aInformation) {
        this.information = aInformation;
    }
    
}

