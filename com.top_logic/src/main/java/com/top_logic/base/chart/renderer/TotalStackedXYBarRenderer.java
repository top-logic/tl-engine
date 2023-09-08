/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
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

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * The TotalStackedXYBarRenderer write the totals over the stacked bars.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TotalStackedXYBarRenderer extends StackedXYBarRenderer {

    /** Indicates whether the positive totals are shown. */
    private boolean showPositiveTotal = true;
    /** Indicates whether the negative totals are shown. */
    private boolean showNegativeTotal = true;
    
    /** The formatter for the totals. */
    private NumberFormat totalFormatter;
    /** The font for the labels. */
    private Font totalLabelFont = new Font("SansSerif", Font.PLAIN, 10);
    
    /**
	 * Creates a {@link TotalStackedXYBarRenderer} without a percentage range axis.
	 */
    public TotalStackedXYBarRenderer() {
        this(false);
    }

    /**
	 * Creates a {@link TotalStackedXYBarRenderer} with or without a percentage range axis.
	 * 
	 * @param aRenderAsPercentages
	 *        A boolean.
	 */
    public TotalStackedXYBarRenderer(boolean aRenderAsPercentages) {
        super();
        this.setRenderAsPercentages(aRenderAsPercentages);
        this.totalFormatter = NumberFormat.getInstance();
    }
    
    @Override
	public void drawItem(Graphics2D aG2, XYItemRendererState aState, Rectangle2D aDataArea, PlotRenderingInfo aInfo, XYPlot aPlot, ValueAxis aDomainAxis, ValueAxis aRangeAxis, XYDataset aDataset, int aSeries, int aItem, CrosshairState aCrosshairState, int aPass) {
        draw(aG2, aState, aDataArea, aInfo, aPlot, aDomainAxis, aRangeAxis, aDataset, aSeries, aItem, aPass);
        super.drawItem(aG2, aState, aDataArea, aInfo, aPlot, aDomainAxis, aRangeAxis, aDataset, aSeries, aItem, aCrosshairState, aPass);
    }

    private void draw(Graphics2D aG2, XYItemRendererState aState, Rectangle2D aDataArea, PlotRenderingInfo aInfo, XYPlot aPlot, ValueAxis aDomainAxis, ValueAxis aRangeAxis, XYDataset aDataset, int aSeries, int aItem, int aPass) {
        if (!(aDataset instanceof IntervalXYDataset 
                && aDataset instanceof TableXYDataset)) {
            String message = "dataset (type " + aDataset.getClass().getName() 
                + ") has wrong type:";
            boolean and = false;
            if (!IntervalXYDataset.class.isAssignableFrom(aDataset.getClass())) {
                message += " it is no IntervalXYDataset";
                and = true;
            }
            if (!TableXYDataset.class.isAssignableFrom(aDataset.getClass())) {
                if (and) {
                    message += " and";
                }
                message += " it is no TableXYDataset";
            }

            throw new IllegalArgumentException(message);
        }

        IntervalXYDataset intervalDataset = (IntervalXYDataset) aDataset;
        double value = intervalDataset.getYValue(aSeries, aItem);
        if (Double.isNaN(value)) {
            return;
        }
        
        // if we are rendering the values as percentages, we need to calculate
        // the total for the current item.  Unfortunately here we end up 
        // repeating the calculation more times than is strictly necessary -
        // hopefully I'll come back to this and find a way to add the 
        // total(s) to the renderer state.  The other problem is we implicitly
        // assume the dataset has no negative values...perhaps that can be
        // fixed too.
		double total = DatasetUtils.calculateStackTotal((TableXYDataset) aDataset, aItem);
        if (getRenderAsPercentages()) {
            value = value / total;
        }
        
        double positiveBase = 0.0;
        double negativeBase = 0.0;
        
        for (int i = 0; i < aSeries; i++) {
            double v = aDataset.getYValue(i, aItem);
            if (!Double.isNaN(v)) {
                if (getRenderAsPercentages()) {
                    v = v / total;
                }
                if (v > 0) {
                    positiveBase = positiveBase + v;
                }
                else {
                    negativeBase = negativeBase + v;
                }
            }
        }

        double translatedBase;
        double translatedValue;
        RectangleEdge edgeR = aPlot.getRangeAxisEdge();
        if (value > 0.0) {
            translatedBase = aRangeAxis.valueToJava2D(positiveBase, aDataArea, 
                    edgeR);
            translatedValue = aRangeAxis.valueToJava2D(positiveBase + value, 
                    aDataArea, edgeR);
        }
        else {
            translatedBase = aRangeAxis.valueToJava2D(negativeBase, aDataArea, 
                    edgeR);
            translatedValue = aRangeAxis.valueToJava2D(negativeBase + value, 
                    aDataArea, edgeR);
        }

        RectangleEdge edgeD = aPlot.getDomainAxisEdge();
        double startX = intervalDataset.getStartXValue(aSeries, aItem);
        if (Double.isNaN(startX)) {
            return;
        }
        double translatedStartX = aDomainAxis.valueToJava2D(startX, aDataArea, 
                edgeD);

        double endX = intervalDataset.getEndXValue(aSeries, aItem);
        if (Double.isNaN(endX)) {
            return;
        }
        double translatedEndX = aDomainAxis.valueToJava2D(endX, aDataArea, edgeD);

        double translatedWidth = Math.max(1, Math.abs(translatedEndX 
                - translatedStartX));
        double translatedHeight = Math.abs(translatedValue - translatedBase);
        if (getMargin() > 0.0) {
            double cut = translatedWidth * getMargin();
            translatedWidth = translatedWidth - cut;
            translatedStartX = translatedStartX + cut / 2;
        }

        Rectangle2D bar = null;
        PlotOrientation orientation = aPlot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(Math.min(translatedBase, 
                    translatedValue), translatedEndX, translatedHeight,
                    translatedWidth);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            bar = new Rectangle2D.Double(translatedStartX,
                    Math.min(translatedBase, translatedValue),
                    translatedWidth, translatedHeight);
        }

        if (aPass == 0) {
            Paint itemPaint = getItemPaint(aSeries, aItem);
            if (getGradientPaintTransformer() 
                    != null && itemPaint instanceof GradientPaint) {
                GradientPaint gp = (GradientPaint) itemPaint;
                itemPaint = getGradientPaintTransformer().transform(gp, bar);
            }
            aG2.setPaint(itemPaint);
            aG2.fill(bar);
            if (isDrawBarOutline() 
                    && Math.abs(translatedEndX - translatedStartX) > 3) {
                aG2.setStroke(getItemStroke(aSeries, aItem));
                aG2.setPaint(getItemOutlinePaint(aSeries, aItem));
                aG2.draw(bar);
            }

            // add an entity for the item...
            if (aInfo != null) {
                EntityCollection entities = aInfo.getOwner().getEntityCollection();
                if (entities != null) {
                    addEntity(entities, bar, aDataset, aSeries, aItem, 
                            bar.getCenterX(), bar.getCenterY());
                }
            }
        }
        else if (aPass == 1) {
            // handle item label drawing, now that we know all the bars have
            // been drawn...
            if (isItemLabelVisible(aSeries, aItem)) {
                XYItemLabelGenerator generator = getItemLabelGenerator(aSeries, 
                        aItem);
                drawItemLabel(aG2, aDataset, aSeries, aItem, aPlot, generator, bar, 
                        value < 0.0);
            }
        }
        
        if (value > 0.0) {
            if (this.showPositiveTotal) {
                if (isLastPositiveItem(aDataset, aSeries, aItem)) {
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
                if (isLastNegativeItem(aDataset, aSeries, aItem)) {
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
    
    private boolean isLastPositiveItem(XYDataset aDataset, int aSeries, int aItem) {
        boolean theResult = true;
        Number theDataValue = aDataset.getY(aSeries, aItem);
        if (theDataValue == null) { 
            return false; 
        }

        for (int r = aSeries + 1; r < aDataset.getSeriesCount(); r++) {
            theDataValue = aDataset.getY(r, aItem);
            if (theDataValue != null) {
                theResult = theResult && (theDataValue.doubleValue() <= 0.0);
            }
        }
        return theResult;
    }

    private boolean isLastNegativeItem(XYDataset aDataset, int aSeries, int aItem) {
        boolean theResult = true;
        Number theDataValue = aDataset.getY(aSeries, aItem);
        if (theDataValue == null) { 
            return false; 
        }

        for (int r = aSeries + 1; r < aDataset.getSeriesCount(); r++) {
            theDataValue = aDataset.getY(r, aItem);
            if (theDataValue != null) {
                theResult = theResult && (theDataValue.doubleValue() >= 0.0);
            }
        }
        return theResult;
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
    
    public NumberFormat getTotalFormatter() {
        return this.totalFormatter;
    }
    
    public void setTotalFormatter(NumberFormat aTotalFormatter) {
        this.totalFormatter = aTotalFormatter;
    }
    
    public Font getTotalLabelFont() {
        return this.totalLabelFont;
    }
    
    public void setTotalLabelFont(Font aTotalLabelFont) {
        this.totalLabelFont = aTotalLabelFont;
    }
    
}

