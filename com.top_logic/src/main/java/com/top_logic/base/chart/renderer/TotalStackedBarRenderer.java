/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.DataUtils;
import org.jfree.data.category.CategoryDataset;

/**
 * This class represents a total stacked bar renderer which can render the sum of the stacks and
 * with a percentage range axis.
 * 
 * @author Thomas Dickhut
 */
public class TotalStackedBarRenderer extends StackedBarRenderer {

    /** Indicates whether the positive totals are shown. */
    private boolean showPositiveTotal = true;
    /** Indicates whether the negative totals are shown. */
    private boolean showNegativeTotal = true;
    
    /** The formatter for the totals. */
    private NumberFormat totalFormatter;
    /** The font for the labels. */
    private Font totalLabelFont = new Font("SansSerif", Font.PLAIN, 10);
    
    /**
	 * Creates a {@link TotalStackedBarRenderer} without a percentage range axis.
	 */
    public TotalStackedBarRenderer() {
        this(false);
    }

    /**
	 * Creates a {@link TotalStackedBarRenderer} with or without a percentage range axis.
	 * 
	 * @param aRenderAsPercentages
	 *        A boolean.
	 */
    public TotalStackedBarRenderer(boolean aRenderAsPercentages) {
        super(aRenderAsPercentages);
        this.totalFormatter = NumberFormat.getInstance();
    }
    
    /**
     * Draws a stacked bar for a specific item.
     * 
     * @param aG2         The graphics device.
     * @param aState      The renderer state.
     * @param aDataArea   The plot area.
     * @param aPlot       The plot.
     * @param aDomainAxis The domain (category) axis.
     * @param aRangeAxis  The range (value) axis.
     * @param aDataset    The data.
     * @param aRow        The row index (zero-based).
     * @param aColumn     The column index (zero-based).
     * @param aPass       The pass index.
     */
    @Override
	public void drawItem(Graphics2D   aG2, 
            CategoryItemRendererState aState, 
            Rectangle2D               aDataArea, 
            CategoryPlot              aPlot, 
            CategoryAxis              aDomainAxis, 
            ValueAxis                 aRangeAxis, 
            CategoryDataset           aDataset, 
            int                       aRow, 
            int                       aColumn, 
            int                       aPass) {
        draw(aG2, aState, aDataArea, aPlot, aDomainAxis, aRangeAxis, aDataset, aRow, aColumn, aPass);
        super.drawItem(aG2, aState, aDataArea, aPlot, aDomainAxis, aRangeAxis, aDataset, aRow, aColumn, aPass);
    }

    /**
     * Draws a stacked bar for a specific item.
     * 
     * @param aG2         The graphics device.
     * @param aState      The renderer state.
     * @param aDataArea   The plot area.
     * @param aPlot       The plot.
     * @param aDomainAxis The domain (category) axis.
     * @param aRangeAxis  The range (value) axis.
     * @param aDataset    The data.
     * @param aRow        The row index (zero-based).
     * @param aColumn     The column index (zero-based).
     */
    public void draw(Graphics2D aG2,
            CategoryItemRendererState aState,
            Rectangle2D aDataArea,
            CategoryPlot aPlot,
            CategoryAxis aDomainAxis,
            ValueAxis aRangeAxis,
            CategoryDataset aDataset,
            int aRow,
            int aColumn, 
            int aPass) {
        
        // nothing is drawn for null values...
        Number dataValue = aDataset.getValue(aRow, aColumn);
        if (dataValue == null) {
            return;
        }
        
        double value = dataValue.doubleValue();
		double total = DataUtils.calculateColumnTotal(aDataset, aColumn);
        if (getRenderAsPercentages()) {
            value = value / total;
        }
        
        PlotOrientation orientation = aPlot.getOrientation();
        double barW0 = aDomainAxis.getCategoryMiddle(aColumn, getColumnCount(), aDataArea, aPlot.getDomainAxisEdge()) 
                       - aState.getBarWidth() / 2.0;

        double positiveBase = getBase();
        double negativeBase = positiveBase;

        for (int i = 0; i < aRow; i++) {
            Number v = aDataset.getValue(i, aColumn);
            if (v != null) {
                double d = v.doubleValue();
                if (getRenderAsPercentages()) {
                    d = d / total;
                }
                if (d > 0) {
                    positiveBase = positiveBase + d;
                }
                else {
                    negativeBase = negativeBase + d;
                }
            }
        }

        double translatedBase;
        double translatedValue;
        RectangleEdge location = aPlot.getRangeAxisEdge();
        if (value >= 0.0) {
            translatedBase = aRangeAxis.valueToJava2D(positiveBase, aDataArea, 
                    location);
            translatedValue = aRangeAxis.valueToJava2D(positiveBase + value, 
                    aDataArea, location);
        }
        else {
            translatedBase = aRangeAxis.valueToJava2D(negativeBase, aDataArea, 
                    location);
            translatedValue = aRangeAxis.valueToJava2D(negativeBase + value, 
                    aDataArea, location);
        }
        double barL0 = Math.min(translatedBase, translatedValue);
        double barLength = Math.max(Math.abs(translatedValue - translatedBase),
                getMinimumBarLength());

        Rectangle2D bar = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(barL0, barW0, barLength, 
                    aState.getBarWidth());
        }
        else {
            bar = new Rectangle2D.Double(barW0, barL0, aState.getBarWidth(), 
                    barLength);
        }
        if (aPass == 0) {
            Paint itemPaint = getItemPaint(aRow, aColumn);
            GradientPaintTransformer t = getGradientPaintTransformer();
            if (t != null && itemPaint instanceof GradientPaint) {
                itemPaint = t.transform((GradientPaint) itemPaint, bar);
            }
            aG2.setPaint(itemPaint);
            aG2.fill(bar);
            if (isDrawBarOutline() 
                    && aState.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
                aG2.setStroke(getItemOutlineStroke(aRow, aColumn));
                aG2.setPaint(getItemOutlinePaint(aRow, aColumn));
                aG2.draw(bar);
            }

            // add an item entity, if this information is being collected
            EntityCollection entities = aState.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, aDataset, aRow, aColumn, bar);
            }
        }
        else if (aPass == 1) {
            CategoryItemLabelGenerator generator = getItemLabelGenerator(aRow, 
                    aColumn);
            if (generator != null && isItemLabelVisible(aRow, aColumn)) {
                drawItemLabel(aG2, aDataset, aRow, aColumn, aPlot, generator, bar, 
                        (value < 0.0));
            }
        }        
        
        if (value > 0.0) {
            if (this.showPositiveTotal) {
                if (isLastPositiveItem(aDataset, aRow, aColumn)) {
                    aG2.setPaint(Color.black);
                    aG2.setFont(this.totalLabelFont);
					TextUtils.drawRotatedString(
                        this.totalFormatter.format(total), aG2,
                        (float) bar.getCenterX(),
                        (float) (bar.getMinY() - 3.0), 
                        TextAnchor.BOTTOM_CENTER, 
                        0.0, 
                        TextAnchor.BOTTOM_CENTER
                    );              
                }
            }
        } 
        else {
            if (this.showNegativeTotal) {
                if (isLastNegativeItem(aDataset, aRow, aColumn)) {
                    aG2.setPaint(Color.black);
                    aG2.setFont(this.totalLabelFont);
					TextUtils.drawRotatedString(
                        String.valueOf(total), aG2,
                        (float) bar.getCenterX(),
                        (float) (bar.getMaxY() + 3.0), 
                        TextAnchor.TOP_CENTER, 
                        0.0, 
                        TextAnchor.TOP_CENTER
                    );              
                }
            }            
        }
    }

    /**
     * Returns <code>true</code> if the specified item is the last positive
     * value for that category.
     * 
     * @param  aDataset The dataset.
     * @param  aRow The row (series).
     * @param  aColumn The column (category).
     * @return Returns <code>true</code> if the specified item is the last
     *         positive value for that category, <code>false</code> otherwise.
     */
    private boolean isLastPositiveItem(CategoryDataset aDataset, int aRow, int aColumn) {
        boolean theResult = true;
        Number theDataValue = aDataset.getValue(aRow, aColumn);
        if (theDataValue == null) { 
            return false; 
        }
        for (int r = aRow + 1; r < aDataset.getRowCount(); r++) {
            theDataValue = aDataset.getValue(r, aColumn);
            if (theDataValue != null) {
                theResult = theResult && (theDataValue.doubleValue() <= 0.0);
            }
        }
        return theResult;
    }

    /**
     * Returns <code>true</code> if the specified item is the last negative
     * value for that category.
     * 
     * @param  aDataset The dataset.
     * @param  aRow     The row (series).
     * @param  aColumn  The column (category).
     * @return Returns <code>true</code> if the specified item is the last
     *         negative value for that category, <code>false</code> otherwise.
     */
    private boolean isLastNegativeItem(CategoryDataset aDataset, int aRow, int aColumn) {
        boolean theResult = true;
        Number theDataValue = aDataset.getValue(aRow, aColumn);
        if (theDataValue == null) { 
            return false; 
        }
        for (int r = aRow + 1; r < aDataset.getRowCount(); r++) {
            theDataValue = aDataset.getValue(r, aColumn);
            if (theDataValue != null) {
                theResult = theResult && (theDataValue.doubleValue() >= 0.0);
            }
        }
        return theResult;
    }

    /**
     * Returns the totalFormatter.
     */
    public NumberFormat getTotalFormatter() {
        return this.totalFormatter;
    }
    
    /**
     * Returns the totalLabelFont.
     */
    public Font getTotalLabelFont() {
        return this.totalLabelFont;
    }

    /**
     * @param anTotalFormatter The totalFormatter to set.
     */
    public void setTotalFormatter(NumberFormat anTotalFormatter) {
        this.totalFormatter = anTotalFormatter;
    }

    /**
     * @param anTotalLabelFont The totalLabelFont to set.
     */
    public void setTotalLabelFont(Font anTotalLabelFont) {
        this.totalLabelFont = anTotalLabelFont;
    }

    public boolean isShowNegativeTotal() {
        return this.showNegativeTotal;
    }

    public void setShowNegativeTotal(boolean aShowNegativeTotal) {
        this.showNegativeTotal = aShowNegativeTotal;
    }
    
    public boolean isShowPositiveTotal() {
        return this.showPositiveTotal;
    }

    public void setShowPositiveTotal(boolean aShowPositiveTotal) {
        this.showPositiveTotal = aShowPositiveTotal;
    }
    
}
