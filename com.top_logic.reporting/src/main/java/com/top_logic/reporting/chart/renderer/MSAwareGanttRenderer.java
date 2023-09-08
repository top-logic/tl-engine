/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.util.Date;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.TimePeriod;

import com.top_logic.util.Utils;

/**
 * Extend the GanttRenderer to draw Subintervals as Milestones.
 *
 * <code>&gt;------&lt;&gt;------&lt;&gt;-------&lt;</code>
 */
public class MSAwareGanttRenderer extends GanttRenderer {

    /** 
     * Create a new MSAwareGanttRenderer.
     */
    public MSAwareGanttRenderer() {
        super();
    }
    
    /** Color of line connecting the Milestones */
    Paint  msLineColor   = Color.black;

    /** Stroke for line connecting the Milestones */
    Stroke msLineStroke  = new BasicStroke();

    /** How are the Milestone Triangles filled */
    Paint  msTriangleFill = Color.red;
    
    /** Label for MilestoneRendering "{0} at {1}" */
    String  msLabel      = "{0} : {1} - {2}";

    /** DateFormat for Milestone-formatting */
    DateFormat  msFormat;
    
    /**
     * Overridden to draw Subintervals as Milestones.
     * 
     * This will ignore the percentComplete value for now.
     */
    @Override
	protected void drawTasks(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, GanttCategoryDataset gcd, int row, int column) {

        TaskSeriesCollection dataset = (TaskSeriesCollection) gcd;
        
        assert PlotOrientation.HORIZONTAL == plot.getOrientation() :
            "VERTICAL GanttChart is not implemented";

        int count = dataset.getSubIntervalCount(row, column);
        // Draw the normal way then paint over with Milestones.
        drawTask(g2, state, dataArea, plot, domainAxis, rangeAxis, 
                    dataset, row, column);
        
        if (count == 0) {
            return; // Nothing to paint here
        }

        RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
        Task          task              = dataset.getSeries(row).get(column);

        for (int subinterval = 0; subinterval < count; subinterval++) {
            // Draw a starting Triangle, than a line and an final Triangle
            // Two adjacent Triangle then will form the diamond shape.
            
            // Map start/ end to plot coordinates,
            Number start = dataset.getStartValue(row, column, subinterval);
            Number end   = dataset.getEndValue  (row, column, subinterval);
            if (start == null || end == null) {
                return;
            }
            int translatedStart = (int) rangeAxis.valueToJava2D(
                    start.doubleValue(), dataArea, rangeAxisLocation);
            int translatedEnd   = (int) rangeAxis.valueToJava2D(
                    end.doubleValue(), dataArea, rangeAxisLocation);
    
            int lineLength = translatedEnd - translatedStart;
            if (lineLength < 0) { // Milestones not in Order or out of Project Range 
                continue;         // Skip rendering this nonsense
            }
            
            
            int rectStart = (int) calculateBarW0(plot, plot.getOrientation(), 
                    dataArea, domainAxis, state, row, column);
            int rectBreadth  = (int) state.getBarWidth();
            int rectCenter   = rectStart + (rectBreadth >> 1);
            int rectBreadth4 = rectBreadth >> 2;
            int startX       = translatedStart + rectBreadth4;
            int endX         = translatedEnd   - rectBreadth4;
            
            int sharedY[] =  new int[] { rectCenter + rectBreadth4 , rectCenter , rectCenter - rectBreadth4};
    
            // Draws starting Triangle
            Polygon leftTriangle = new Polygon(  
                    new int[] { translatedStart , startX , translatedStart },
                    sharedY, 3);
            
            g2.setPaint(msTriangleFill);
            g2.fill(leftTriangle);
            if (isDrawBarOutline() 
                    && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
                g2.setStroke(getItemStroke(row, column));
                g2.setPaint(getItemOutlinePaint(row, column));
                g2.draw(leftTriangle);
            }
    
            // Draw connecting line
            g2.setPaint(msLineColor);
            g2.setStroke(msLineStroke); 
            g2.drawLine(startX, sharedY[1], endX, sharedY[1]);
            
            // Draws final Triangle
            Polygon rightTriangle = new Polygon(  
                    new int[] { translatedEnd , endX , translatedEnd },
                    sharedY, 3);
            
            g2.setPaint(msTriangleFill);
            g2.fill(rightTriangle);
            if (isDrawBarOutline() 
                    && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
                g2.setStroke(getItemStroke(row, column));
                g2.setPaint(getItemOutlinePaint(row, column));
                g2.draw(rightTriangle);
            }
            
            
            // collect entity and tool tip information for both Triangles
            // KHA Renderer for Gantt is broken in this release of JFreechart
            // So I drop the concept completely and do it on my own.
            if (state.getInfo() != null && subinterval > 0) {
                EntityCollection entities = state.getEntityCollection();
                if (entities != null) {
                    Task   msTask = task.getSubtask(subinterval - 1);
                    
					String tip = getTooltip(msTask);
                    
                    
                    Rectangle bounds = leftTriangle.getBounds();
                    // extend bounds to the left to cover previous Triangle, too
                    bounds.add(translatedStart - rectBreadth4 , sharedY[1]);
                    
                    String url = null;
                    CategoryItemEntity entity = new CategoryItemEntity(
						bounds, tip, url, dataset, dataset.getRowKey(row),
						dataset.getColumnKey(column));
                    entities.add(entity);
                }
            }
            
        }
    }

	private String getTooltip(Task msTask) {
		String msName = msTask.getDescription();
		TimePeriod msDuration = msTask.getDuration();
		Date msStart = msDuration.getStart();
		Date msEnd = msDuration.getEnd();

		return Utils.format(msLabel, msName, msFormat.format(msStart), msFormat.format(msEnd));
	}

    /**
     * Draws a single task, in case start and end are the same this renders a MS-Triangle.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the data plot area.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the data.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     */
    @Override
	protected void drawTask(Graphics2D g2,
                            CategoryItemRendererState state,
                            Rectangle2D dataArea,
                            CategoryPlot plot,
                            CategoryAxis domainAxis,
                            ValueAxis rangeAxis,
                            GanttCategoryDataset dataset,
                            int row,
                            int column) {

        Number startValue = dataset.getStartValue(row, column);
        Number endValue   = dataset.getEndValue  (row, column);
        
        if (startValue == null || endValue == null) {
            return;
        }
        
        if (!startValue.equals(endValue)) {
            super.drawTask(g2, state, dataArea, plot, domainAxis, 
                    rangeAxis, dataset, row, column);
        } else { 
            this.drawMSTask(g2, state, dataArea, plot, domainAxis, 
                    rangeAxis, dataset, row, column, startValue);
        }



    }

    /** 
     * Draw a Milestone Task as Diamond Shape.
     */
    private void drawMSTask(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, GanttCategoryDataset dataset, int row, int column,
            Number startValue) {
        
        // This code will work horizontally only as asserted in code above.

        RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
        
        int xValue = (int) rangeAxis.valueToJava2D(startValue.doubleValue(), dataArea, rangeAxisLocation);

        int rectStart = (int) calculateBarW0(plot, plot.getOrientation(), 
                dataArea, domainAxis, state, row, column);
        int rectBreadth  = (int) state.getBarWidth();
        int rectCenter   = rectStart + (rectBreadth >> 1);
        int rectBreadth4 = rectBreadth >> 2;
        int startX       = xValue - rectBreadth4;
        int endX         = xValue + rectBreadth4 + 1;
        int startY      =  rectCenter + rectBreadth4;
        int endY         = rectCenter - rectBreadth4;
        
        Polygon diamodShape = new Polygon(  
                new int[] { startX     , xValue , endX       , xValue },
                new int[] { rectCenter , startY , rectCenter , endY   },
                4);
        
        g2.setPaint(msTriangleFill);
        g2.fill(diamodShape);
        if (isDrawBarOutline() 
                && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
            g2.setStroke(getItemStroke(row, column));
            g2.setPaint(getItemOutlinePaint(row, column));
            g2.draw(diamodShape);
        }
        
        // collect entity and tool tip information for the Milestone
        if (state.getInfo() != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                Task  msTask = ((TaskSeriesCollection) dataset) .getSeries(row).get(column);

				String tip = getTooltip(msTask);
                
                Rectangle            theBounds = diamodShape.getBounds();
                String               theURL    = null;
                CategoryURLGenerator theURLGen = this.getItemURLGenerator(row, column);

                if (theURLGen != null) {
                    theURL = theURLGen.generateURL(dataset, row, column);
                }

                entities.add(new CategoryItemEntity(theBounds, tip, theURL, dataset, dataset.getRowKey(row), dataset.getColumnKey(column)));
            }
        }
    }


    /** @see #msLineColor */
    public Paint getMsLineColor() {
        return (this.msLineColor);
    }


    /** @see #msLineColor */
    public void setMsLineColor(Paint aMsLineColor) {
        this.msLineColor = aMsLineColor;
    }


    /** @see #msLineStroke */
    public Stroke getMsLineStroke() {
        return (this.msLineStroke);
    }


    /** @see #msLineStroke */
    public void setMsLineStroke(Stroke aMsLineStroke) {
        this.msLineStroke = aMsLineStroke;
    }


    /** @see #msTriangleFill */
    public Paint getMsTriangleFill() {
        return (this.msTriangleFill);
    }

    /** @see #msTriangleFill */
    public void setMsTriangleFill(Paint aMsDiamondFill) {
        this.msTriangleFill = aMsDiamondFill;
    }


    public String getMsLabel() {
        return (this.msLabel);
    }


    public void setMsLabel(String aMsLabel) {
        this.msLabel = aMsLabel;
    }


    public DateFormat getMsFormat() {
        return (this.msFormat);
    }


    public void setMsFormat(DateFormat aMsFormat) {
        this.msFormat = aMsFormat;
    }
    
}