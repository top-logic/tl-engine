/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.generator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.TickLabelEntity;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextLine;
import org.jfree.chart.ui.RectangleEdge;


/**
 * The MaxLabelLengthCategoryAxis only overwritten to handle the unique
 * category names. See comment of {@link #replaceUniqueLabels(List)}!
 * 
 * @see MaxLabelLengthProvider
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class MaxLabelLengthCategoryAxis extends CategoryAxis {

	
	/**
	 * Only overwritten to handle the unique category names. See comment of
	 * {@link #replaceUniqueLabels(List)}!
	 */
	@Override
    protected AxisState drawCategoryLabels(Graphics2D g2,
            Rectangle2D plotArea,
            Rectangle2D dataArea,
            RectangleEdge edge,
            AxisState state,
            PlotRenderingInfo plotState) {
        if (state == null) {
            throw new IllegalArgumentException("Null 'state' argument.");
        }

        if (isTickLabelsVisible()) {
            List ticks = refreshTicks(g2, state, dataArea, edge);
            ticks = replaceUniqueLabels(ticks);
            state.setTicks(ticks);        
          
            int categoryIndex = 0;
            Iterator iterator = ticks.iterator();
            while (iterator.hasNext()) {
                
                CategoryTick tick = (CategoryTick) iterator.next();
                g2.setFont(getTickLabelFont(tick.getCategory()));
                g2.setPaint(getTickLabelPaint(tick.getCategory()));

                CategoryLabelPosition position 
                    = getCategoryLabelPositions().getLabelPosition(edge);
                double x0 = 0.0;
                double x1 = 0.0;
                double y0 = 0.0;
                double y1 = 0.0;
                if (edge == RectangleEdge.TOP) {
                    x0 = getCategoryStart(categoryIndex, ticks.size(), 
                            dataArea, edge);
                    x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, 
                            edge);
                    y1 = state.getCursor() - getCategoryLabelPositionOffset();
                    y0 = y1 - state.getMax();
                }
                else if (edge == RectangleEdge.BOTTOM) {
                    x0 = getCategoryStart(categoryIndex, ticks.size(), 
                            dataArea, edge);
                    x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, 
                            edge); 
                    y0 = state.getCursor() + getCategoryLabelPositionOffset();
                    y1 = y0 + state.getMax();
                }
                else if (edge == RectangleEdge.LEFT) {
                    y0 = getCategoryStart(categoryIndex, ticks.size(), 
                            dataArea, edge);
                    y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, 
                            edge);
                    x1 = state.getCursor() - getCategoryLabelPositionOffset();
                    x0 = x1 - state.getMax();
                }
                else if (edge == RectangleEdge.RIGHT) {
                    y0 = getCategoryStart(categoryIndex, ticks.size(), 
                            dataArea, edge);
                    y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, 
                            edge);
                    x0 = state.getCursor() + getCategoryLabelPositionOffset();
                    x1 = x0 - state.getMax();
                }
                Rectangle2D area = new Rectangle2D.Double(x0, y0, (x1 - x0), 
                        (y1 - y0));
				Point2D anchorPoint = position.getCategoryAnchor().getAnchorPoint(area);
                TextBlock block = tick.getLabel();
                block.draw(g2, (float) anchorPoint.getX(), 
                        (float) anchorPoint.getY(), position.getLabelAnchor(), 
                        (float) anchorPoint.getX(), (float) anchorPoint.getY(), 
                        position.getAngle());
                Shape bounds = block.calculateBounds(g2, 
                        (float) anchorPoint.getX(), (float) anchorPoint.getY(), 
                        position.getLabelAnchor(), (float) anchorPoint.getX(), 
                        (float) anchorPoint.getY(), position.getAngle());
                if (plotState != null && plotState.getOwner() != null) {
                    EntityCollection entities 
                        = plotState.getOwner().getEntityCollection();
                    if (entities != null) {
                        String tooltip = getCategoryLabelToolTip(
                                tick.getCategory());
                        entities.add(new TickLabelEntity(bounds, tooltip, 
                                null));
                    }
                }
                categoryIndex++;
            }

            if (edge.equals(RectangleEdge.TOP)) {
                double h = state.getMax();
                state.cursorUp(h);
            }
            else if (edge.equals(RectangleEdge.BOTTOM)) {
                double h = state.getMax();
                state.cursorDown(h);
            }
            else if (edge == RectangleEdge.LEFT) {
                double w = state.getMax();
                state.cursorLeft(w);
            }
            else if (edge == RectangleEdge.RIGHT) {
                double w = state.getMax();
                state.cursorRight(w);
            }
        }
        return state;
    }

    /** 
     * This method replaces the unique cutted labels (e.g. home...town~3) through
     * the normal label (e.g. home...town). The unique labels are necessary for the category 
     * names. The category names must be unique! 
     * 
     * @param aTicks The ticks of the domain axis.
     */
    private List replaceUniqueLabels(List aTicks) {
        ArrayList newTicks = new ArrayList(aTicks.size());
        for (Iterator ticksIter = aTicks.iterator(); ticksIter.hasNext();) {
            CategoryTick tick = (CategoryTick) ticksIter.next();
            
            // Get the label text and if the label contains the MaxLabelLengthProvider 
            // separator remove it!
            TextBlock label = tick.getLabel();
            TextLine lastLine = label.getLastLine();
            String text = lastLine.getFirstTextFragment().getText();
            int endIndex = text.lastIndexOf(MaxLabelLengthProvider.SEP);
            
            if (endIndex != -1) {
                text = text.substring(0, endIndex);
            }
            TextBlock textBlock = new TextBlock();
            textBlock.addLine(new TextLine(text, getTickLabelFont(tick.getCategory()), getTickLabelPaint(tick.getCategory())));
            textBlock.setLineAlignment(label.getLineAlignment());
            newTicks.add(new CategoryTick(tick.getCategory(), textBlock, tick.getLabelAnchor(), tick.getRotationAnchor(), tick.getAngle()));
        }
        
        return newTicks;
    }
    
}

