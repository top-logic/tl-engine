/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.renderer;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.ui.GradientPaintTransformer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.DataUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

/**
 * The BarLineRenderer is a BarRenderer which draws only the top line of the bars.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class BarLineRenderer extends StackedBarRenderer {

    /** The lineStrength of the bar */
    protected double lineStrength;



    public BarLineRenderer() {
        super(false);
        this.lineStrength = 1.0;
    }

    public BarLineRenderer(double lineStrength) {
        super(false);
        this.lineStrength = lineStrength;
    }



    @Override
	public Range findRangeBounds(CategoryDataset dataset) {
        if (getRenderAsPercentages()) {
            return new Range(0.0, 1.0);
        }
        else {
			Range result = DatasetUtils.findRangeBounds(dataset);
            if (result != null) {
                if (getIncludeBaseInRange()) {
                    result = Range.expandToInclude(result, getBase());
                }
            }
            return result;
        }
    }



    @Override
	public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass) {

        // nothing is drawn for null values...
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null) {
            return;
        }

        double value = dataValue.doubleValue();
        double total = 0.0;  // only needed if calculating percentages
        if (getRenderAsPercentages()) {
			total = DataUtils.calculateColumnTotal(dataset, column);
            value = value / total;
        }

        PlotOrientation orientation = plot.getOrientation();
        double barW0 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                dataArea, plot.getDomainAxisEdge())
                - state.getBarWidth() / 2.0;

        double positiveBase = getBase();
        double negativeBase = positiveBase;

        for (int i = 0; i < row; i++) {
            Number v = dataset.getValue(i, column);
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

// START CHANGES BOS:
// ------------------

        double translatedBase;
        double translatedValue;
        RectangleEdge location = plot.getRangeAxisEdge();
        if (value >= 0.0) {
            translatedBase = rangeAxis.valueToJava2D(getBase(), dataArea, location); // here
            translatedValue = rangeAxis.valueToJava2D(getBase() + value, dataArea, location); // here
        }
        else {
            translatedBase = rangeAxis.valueToJava2D(getBase(), dataArea, location); // here
            translatedValue = rangeAxis.valueToJava2D(getBase() + value, dataArea, location); // here
        }
        double barL0 = Math.min(translatedBase, translatedValue);
        barL0 -= lineStrength/2; // here
        double barLength = lineStrength; // here

        Rectangle2D bar = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(barL0, barW0, barLength, state.getBarWidth());
        }
        else {
            bar = new Rectangle2D.Double(barW0, barL0, state.getBarWidth(), barLength);
        }
        if (pass == 0) {
            Paint itemPaint = getItemPaint(row, column);
            GradientPaintTransformer t = getGradientPaintTransformer();
            if (t != null && itemPaint instanceof GradientPaint) {
                itemPaint = t.transform((GradientPaint) itemPaint, bar);
            }
            g2.setPaint(itemPaint);
            g2.fill(bar);
            // here: removed bar border
//            if (isDrawBarOutline()
//                    && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
//                g2.setStroke(getItemOutlineStroke(row, column));
//                g2.setPaint(getItemOutlinePaint(row, column));
//                g2.draw(bar);
//            }

// END CHANGES BOS:
// ----------------

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, bar);
            }
        }
        else if (pass == 1) {
            CategoryItemLabelGenerator generator = getItemLabelGenerator(row,
                    column);
            if (generator != null && isItemLabelVisible(row, column)) {
                drawItemLabel(g2, dataset, row, column, plot, generator, bar,
                        (value < 0.0));
            }
        }
    }

}
