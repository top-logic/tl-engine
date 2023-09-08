/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.dataset.SwingDatasetUtilities;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;
import com.top_logic.reporting.chart.plot.SwingPlot;

/**
 * The SwingRenderer draws swing charts with image maps (urls and tooltips).
 * In the com.top_logic_reporting.chart.demo.swing are two examples how a swing 
 * chart looks like. The SwingMain class contains the code for the both examples. 
 * The SwingRenderer needs a 
 * {@link com.top_logic.reporting.chart.dataset.SwingDataset} which contains 
 * {@link com.top_logic.reporting.chart.info.swing.SwingRenderingInfo} to draw a 
 * swing chart.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingRenderer extends BarRenderer {

    /** Constant for the icon orientation right(>). */
    public static final int RIGHT = 0;
    /** Constant for the icon orientation left (<). */
    public static final int LEFT  = 1;
    
    /** The color for the lines between the bars. */
    private Color   lineColor;
    /** The bar width. */
    private double  barWidth;
    /** The gradient indicates how strong the fill color tone is.  */
    private int     gradientColorValue;
    /**
     * The cache contains {@link CacheEntry}s. The keys are unique strings (see
     * {@link SwingDatasetUtilities#getKey(int, int)}).
     */
    private Map<String,CacheEntry> cache;
    /** Indicate whether the axis label is added to the tooltip information. */
    private boolean toolTipWithAxes;
    
    /** 
     * Creates a {@link SwingRenderer}.
     */
    public SwingRenderer() {
        this.gradientColorValue = 50;
        this.cache              = new HashMap<>();
        this.lineColor          = Color.BLACK;
        this.toolTipWithAxes    = true;
        setMaximumBarWidth(75);
    }
    
    /** 
     * Overriden to draw the special swing chart items.
     * 
     * @param aGraphic       A graphics device.
     * @param aRendererState A state information for one chart.
     * @param aDataArea      A data plot area.
     * @param aPlot          A plot.
     * @param aCategoryAxis  A domain axis.
     * @param aValueAxis     A range axis.
     * @param aDataset       A {@link SwingDataset}.
     * @param aRow           A row.
     * @param aColumn        A column.
     * @param aPass          A pass.
     * 
     * @see org.jfree.chart.renderer.category.CategoryItemRenderer#drawItem(java.awt.Graphics2D, org.jfree.chart.renderer.category.CategoryItemRendererState, java.awt.geom.Rectangle2D, org.jfree.chart.plot.CategoryPlot, org.jfree.chart.axis.CategoryAxis, org.jfree.chart.axis.ValueAxis, org.jfree.data.category.CategoryDataset, int, int, int)
     */
    @Override
	public void drawItem(Graphics2D aGraphic, CategoryItemRendererState aRendererState, Rectangle2D aDataArea, CategoryPlot aPlot, CategoryAxis aCategoryAxis, ValueAxis aValueAxis, CategoryDataset aDataset, int aRow, int aColumn, int aPass) {
        /*
         * Get the value from the dataset and the corresponding info object.
         */
        double             value = getValue(aRow, aColumn, aDataset);
        SwingRenderingInfo info  = ((SwingDataset)aDataset).getRenderingInfo(aRow, aColumn);
        
        /* If the info object is null do nothing and return. */
		if (info == null) {
			return;
		}
        
        /*
         * Note, we have to get the correct axis and bar width. The index of the
         * axis is in the info object.
         */
        ValueAxis axis = aPlot.getRangeAxis(info.getValueAxisIndex());
		if (info.isVisible()) {
			this.barWidth = aRendererState.getBarWidth();
		} else {
			this.barWidth = 0;
		}

        /*
         * To draw a bar we need the x, y, width and height of the bar. The
         * x-coordinate and the bar width are for a normal or special bars the
         * same. The y-coordinate and the height are different between normal
         * and special bars.
         */
        double x = getX(aDataArea, aPlot, aCategoryAxis, aColumn);
        double y = 0;
        double w = this.barWidth;
        double h = 0;
        
        if (info.isNormalBar()) {
            y = getYNormalBar(aDataArea, aPlot, axis, value);
            h = getHeight(aDataArea, aPlot, axis, value);
        } else {
            CacheEntry prev = null;
            if (aRow == 0) {
                int theDiff = info.getDrawLineToPrev();

                if (theDiff == 0) {
                    theDiff = -1;
                }

                prev = getPrevCacheEntry(aColumn + theDiff, 0, false);
            } else {
                prev = getPrevCacheEntry(aColumn, aRow, true);
            }
            
			if (prev != null) {
				double prevValue = prev.value;
				h = getHeight(aDataArea, aPlot, axis, Math.abs(value));
				y = getYSpecialBar(value, h, prev, info, prevValue);
			}
        }
        
        /*
         * Create the bar with the calculated values and store the value, bar
         * and info object as cache entry into the cache.
         */
        Rectangle2D bar   = new Rectangle2D.Double(x, y, w, h);
        Shape       shape = bar; // Is needed later for the tooltip information
        CacheEntry  entry = new CacheEntry(value, info, bar);
        String      key   = SwingDatasetUtilities.getKey(aRow, aColumn);
        this.cache.put(key, entry);
        
		/* Do not draw an invible bar. */
		if (!info.isVisible()) {
			return;
		}

        /*
         * Bars can be drawn as icon. The info object contains the information.
         */
        boolean showAsIcon = info.isShowAsIcon();
        if(!showAsIcon) {
            drawBar(info.getColor(), aGraphic, aDataset, aRow, aColumn, bar);
        }
        
        /*
         * Draw a line between the current item and the previous item. A line is
         * drawn if in the info object the flag was set, the previous item is
         * NOT null and the item is not shown as icon. If the item is shown as
         * icon the drawIcon-method have to draw the line. Here we do not know
         * which icon it is and from where to where we have to draw the line.
         */
        int        theDiff = info.getDrawLineToPrev();
        CacheEntry prev    = (theDiff == 0) ? null : this.getPrevCacheEntry(aColumn + theDiff, 0, false);

        if (info.isDrawLineToPrev() && !showAsIcon && prev != null) {
            double prevValue = prev.value;
            
            double x1Line = prev.bar.getX() + w;
            double x2Line = x;
            double y1Line = 0;
            if (prevValue >= 0) {
                y1Line = prev.bar.getY();
            } else {
                y1Line = prev.bar.getY() + prev.bar.getHeight();
            }

            double y2Line = y1Line;
            
            if (theDiff < -1) {
                double theSize = 5.0d;
                double thePos  = x1Line;

                aGraphic.setPaint(Color.GRAY);

                do {
                    Line2D theLine = new Line2D.Double(thePos, y1Line, thePos + theSize, y2Line);
                    
                    aGraphic.draw(theLine);
                    thePos += 2 * theSize;
                } while (thePos < x2Line);
            }
            else {
                Line2D line = new Line2D.Double(x1Line, y1Line, x2Line, y2Line);

                aGraphic.setPaint(this.getLineColor());
                aGraphic.draw(line);
            }
        }
        
        /*
         * Draw the current item as icon and store the shape into the shape. The
         * shape is needed for the tooltip information.
         */
        if (showAsIcon) {
            shape = drawIcon(aGraphic, aColumn, entry, x, y, w, h, info.getDrawLineToPrev());
        }
        
        /*
         * Collect the tooltips and urls for the image map.
         */
        if (aRendererState.getInfo() != null && shape != null) {
            ChartRenderingInfo chartRenderingInfo = aRendererState.getInfo().getOwner();
            if(chartRenderingInfo != null){
            	EntityCollection theEntities = chartRenderingInfo.getEntityCollection();
            	if (theEntities != null) {
            		String                   theTooltip   = null;
            		CategoryToolTipGenerator theGenerator = this.getToolTipGenerator(aRow, aColumn);
            		
            		if (theGenerator != null) {
            			theTooltip = theGenerator.generateToolTip(aDataset, aRow, aColumn);
            		}
            		
            		String               theURL          = null;
            		CategoryURLGenerator theURLGenerator = this.getItemURLGenerator(aRow, aColumn);
            		if (theURLGenerator != null) {
            			theURL = theURLGenerator.generateURL(aDataset, aRow, aColumn);
            		}
            		
            		theEntities.add(new CategoryItemEntity(shape, theTooltip, theURL, aDataset, aRow, aDataset.getColumnKey(aColumn)));
            	}
            }
        }

		CategoryItemLabelGenerator labelGenerator = this.getItemLabelGenerator(aRow, aColumn);
		if (labelGenerator != null && this.isItemLabelVisible(aRow, aColumn)) {
			boolean isNegative = value < 0.0;
			this.drawItemLabel(aGraphic, aDataset, aRow, aColumn, this.getPlot(), labelGenerator, bar, isNegative);
		}
    }

    /**
	 * Overridden to calculate the correct bar with for the swing items.
	 * 
	 * @see org.jfree.chart.renderer.category.BarRenderer#calculateBarWidth(org.jfree.chart.plot.CategoryPlot,
	 *      java.awt.geom.Rectangle2D, int,
	 *      org.jfree.chart.renderer.category.CategoryItemRendererState)
	 */
    @Override
	protected void calculateBarWidth(CategoryPlot aPlot, 
                                     Rectangle2D aDataArea, 
                                     int aRendererIndex,
                                     CategoryItemRendererState aState) {
                                         
        CategoryAxis    domainAxis = getDomainAxis(aPlot, aRendererIndex);
        CategoryDataset dataset    = aPlot.getDataset(aRendererIndex);
        if (dataset != null) {
            int    columns        = dataset.getColumnCount();
            double space          = aDataArea.getWidth();
            double maxWidth       = getMaximumBarWidth();
            double categoryMargin = 0.0;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            double used = space * (1 - domainAxis.getLowerMargin() - 
                                       domainAxis.getUpperMargin() - categoryMargin);
            if (columns > 0) {
                aState.setBarWidth(Math.min(used / columns, maxWidth));
            }
            else {
                aState.setBarWidth(Math.min(used, maxWidth));
            }
        }
    }
    
    /**
     * This method draws items as icons. This implementation draws triangles
     * instead of bars. This method can be overwritten to draw another icon, but
     * please note that this method have to draw the line to the previous item.
     * The given boolean flag indicates if a line must be drawn.
     * 
     * @param  aGraphic The graphics device.
     * @param  aColumn  The current column.
     * @param  aEntry   The current cache entry.
     * @param  x        The x-coordinate.
     * @param  y        The y-coordinate.
     * @param  w        The item width.
     * @param  h        The item height.
     * @param  aPrevReference Indicates if a line to the previous item must be drawn.
     * @return Returns the shape of the icon or <code>null</code>. The shape is needed 
     *         for the image map.
     */
    private Shape drawIcon(Graphics2D aGraphic, int aColumn, CacheEntry aEntry, double x, double y, double w, double h, int aPrevReference) {
        if (aEntry.value < 0) {
            y += h;
        }
        
        GeneralPath triangle = new GeneralPath();
        int         xShift   = Math.min((int)(this.barWidth / 2), 30);
        int         yShift   = xShift;
        if (aEntry.info.getIconOrientation() == LEFT) {
            triangle.moveTo((float)x + xShift, (float)y - yShift);
            triangle.lineTo((float)x + xShift, (float)y + yShift);
            triangle.lineTo((float)x, (float)y);
            
            if (aPrevReference < 0) {
                CacheEntry prev = getPrevCacheEntry(aColumn + aPrevReference, 0, false);
                if (prev == null) {return null;}
                Line2D line = new Line2D.Double(x, y, prev.bar.getX() + prev.bar.getWidth(), y);
                aGraphic.setPaint(getLineColor());
                aGraphic.draw(line);
            } 
        } else {
            triangle.moveTo((float)(x + w), (float)y);
            triangle.lineTo((float)(x + w - xShift), (float)y - yShift);
            triangle.lineTo((float)(x + w - xShift), (float)y + yShift);
        }
        triangle.closePath();

        aGraphic.setPaint(aEntry.info.getColor());
        aGraphic.fill(triangle);
        aGraphic.draw(triangle);
        
        return triangle;
    }

    /** 
     * This method returns the y-coordinate for a normal bar.
     * 
     * @param  aDataArea  A data plot area.
     * @param  aPlot      A plot.
     * @param  aValueAxis A value axis.
     * @param  value      A value.
     * @return Returns the y-coordinate for a normal bar.
     */
    private double getYNormalBar(Rectangle2D aDataArea, CategoryPlot aPlot, 
                                 ValueAxis aValueAxis, double value) {
        double[] barL0L1 = new double[] {0.0, 0.0};
        if (value >= 0) {
            barL0L1[0] = value;
            barL0L1[1] = 0.0;
        } else {
            barL0L1[0] = 0.0;
            barL0L1[1] = value;
        }
        
        RectangleEdge theLocation = aPlot.getRangeAxisEdge();
        double        transL0     = aValueAxis.valueToJava2D(barL0L1[0], aDataArea, theLocation);
        double        transL1     = aValueAxis.valueToJava2D(barL0L1[1], aDataArea, theLocation);
        return Math.min(transL0, transL1);
    }
    
    /**
     * This method returns the y-coordinate for a special bar.
     * 
     * @param  aValue     A value.
     * @param  aheight    A height.
     * @param  aPrev      A previous cache entry.
     * @param  aPrevValue A previous value.
     * @return Returns the y-coordinate for a special bar.
     */
    private double getYSpecialBar(double aValue, double aheight, CacheEntry aPrev, 
                                  SwingRenderingInfo aInfo, double aPrevValue) {
        if (aInfo.isOverwritePrev()) {return aPrev.bar.getY();}

        double y;
        if (aPrevValue >= 0) {
            if (aValue >= 0) {
                y = aPrev.bar.getY() - aheight;
            } else {
                y = aPrev.bar.getY();
            }
        } else {
            if (aValue >= 0) {
                double diff = aPrev.bar.getHeight() - aheight;
                y = aPrev.bar.getY() + diff;
            } else {
                y = aPrev.bar.getY() + aPrev.bar.getHeight();
            }
        }
        return y;
    }
    
    /** 
     * This method returns the item height.
     * 
     * @param  aDataArea  A data plot area.
     * @param  aPlot      A plot.
     * @param  aValueAxis A value axis.
     * @param  value      A value.
     * @return Returns the item height.
     */
    private double getHeight(Rectangle2D aDataArea, CategoryPlot aPlot, 
                             ValueAxis aValueAxis, double value) {
        /*
         * Normaly you can use double[] barL0L1 = calculateBarL0L1(value); But
         * the calculation considers the visible area and cut off the height if
         * it is not completely visible. But this works only with normal bars
         * and not with our special bars. Here we ignore the visible area and
         * all works fine.
         */
        double[] barL0L1 = new double[] {0.0, 0.0};
        if (value >= 0) {
            barL0L1[0] = value;
            barL0L1[1] = 0.0;
        } else {
            barL0L1[0] = 0.0;
            barL0L1[1] = value;
        }
        
        /*
         * Calculate the coordinates, width and length of the bar.
         */
        RectangleEdge theLocation = aPlot.getRangeAxisEdge();
        double transL0 = aValueAxis.valueToJava2D(barL0L1[0], aDataArea, theLocation);
        double transL1 = aValueAxis.valueToJava2D(barL0L1[1], aDataArea, theLocation);
        return Math.max(Math.abs(transL1 - transL0), getMinimumBarLength());
    }
    
    /** 
     * This method returns for the given column and row the previous cache entry.
     * 
     * @param  aColumn   A column.
     * @param  aRow      A row.
     * @param  useStacks Indicates whether stacked bars are to be considered.
     * @return Returns for the given column and row the previous cache entry.
     */
    private CacheEntry getPrevCacheEntry(int aColumn, int aRow, boolean useStacks) {
        CacheEntry result = null;
        
        for (int i = aColumn; i >= 0; i--) {
            int    row  = 0;
            Object prev = this.cache.get(SwingDatasetUtilities.getKey(row, i));
            if (useStacks) {
                Object afterPrev  = this.cache.get(SwingDatasetUtilities.getKey(++row, i));
                while (afterPrev != null && row < aRow) {
                    prev      = afterPrev;
                    afterPrev = this.cache.get(SwingDatasetUtilities.getKey(++row, i));
                }
            }
            if (prev != null) {
                return (CacheEntry)prev;
            }
        }
        return result;
    }

    /**
     * This method draws the given bar with outline and the given color with
     * gradient paint.
     */
    private void drawBar(Color aColor, Graphics2D aGraphic, CategoryDataset aDataset,
                         int aRow, int aColumn, Rectangle2D aBar) {

		/* Clip the BAR !!!
		 * 
		 * This avoid an ugly out of memory problem the problem occurs, when the bar is much bigger
		 * that the graphic. The clipping should be done by java, but java seems to have a problem:
		 * It seems that first the bar is drawn (in memory) and then the clipping is done. This
		 * explains the OutOfMeomory problem.
		 * 
		 * This explains the observation, that sometime PMC needs a lot of memory on daytime without
		 * any reason to be seen in the application logs or the database connectivity cf: QC3271-
		 * Notfallausleitung, Trac #13836, TTS 11033 */
		Shape clip = aGraphic.getClip();
		Rectangle clipBounds = clip.getBounds();

		double clipMinY = clipBounds.getMinY();
		double clipMaxY = clipBounds.getMaxY();

		double barMinY = aBar.getMinY();
		double barMaxY = aBar.getMaxY();

		if (barMinY < clipMinY) {
			barMinY = clipMinY;
		}
		if (barMaxY > clipMaxY) {
			barMaxY = clipMaxY;
		}

		aBar.setRect(aBar.getX(), barMinY, aBar.getWidth(), barMaxY - barMinY);

        Color itemColor1 = aColor;
        int   red        = itemColor1.getRed()  - this.gradientColorValue > 0 ? 
                                      itemColor1.getRed() -   this.gradientColorValue : 0;
        int   green      = itemColor1.getGreen()- this.gradientColorValue > 0 ? 
                                      itemColor1.getGreen() - this.gradientColorValue : 0;
        int   blue       = itemColor1.getBlue() - this.gradientColorValue > 0 ? 
                                      itemColor1.getBlue()  - this.gradientColorValue : 0;
        Color itemColor2 = new Color(red, green, blue);

        GradientPaint gradient = new GradientPaint((float)aBar.getX(), (float)aBar.getY(), 
                                                  itemColor1, 
                                                  (float)(aBar.getX() + aBar.getWidth()), 
                                                  (float)(aBar.getY() + aBar.getHeight()), 
                                                  itemColor2);
        aGraphic.setPaint(gradient);
        aGraphic.fill(aBar);
        
        if(isDrawBarOutline()) {
            Stroke stroke = getItemOutlineStroke(aRow, aColumn);
            Paint  paint  = getItemOutlinePaint (aRow, aColumn);
            if(stroke != null && paint != null) {
                aGraphic.setPaint (paint);
                aGraphic.setStroke(stroke);
            }
        }
        
        aGraphic.draw(aBar);
    }
    
    /**
     * This method returns the x-coordinate for an item.
     * 
     * @param  aDataArea     A data plot area.
     * @param  aPlot         A plot.
     * @param  aCategoryAxis A category axis.
     * @param  aColumn       A column.
     * @return Returns the x-coordinate for an item.
     */
    private double getX(Rectangle2D aDataArea, CategoryPlot aPlot, CategoryAxis aCategoryAxis, int aColumn) {
		int actualVisibleCols = getNumberOfVisibleColumnsUpToColumn(aPlot, aColumn);
		int totalVisibleCols = getVisibleColumnCount(aPlot);

		if (actualVisibleCols == totalVisibleCols) {
			// we are at the end of the chart, this column will not be displayed and no more columns
			// will be displayed
			// the value does not matter
			return 0.0;
		}

		return aCategoryAxis.getCategoryMiddle(actualVisibleCols, totalVisibleCols, aDataArea,
			aPlot.getDomainAxisEdge())
			- (this.barWidth / 2);
    }
    
    /**
	 * the number of visible columns with a number less than aColumn
	 */
	private int getNumberOfVisibleColumnsUpToColumn(CategoryPlot aPlot, int aColumn) {
		if (aPlot instanceof SwingPlot) {
			return ((SwingPlot) aPlot).getNumberOfVisibleColumnsUpToColumn(aColumn);
		}
		return aColumn;
	}

	/**
	 * the number of visible columns
	 */
	private int getVisibleColumnCount(CategoryPlot aPlot) {
		if (aPlot instanceof SwingPlot) {
			return ((SwingPlot) aPlot).getVisibleColumnCount();
		}
		return getColumnCount();
	}
    
    /**
     * This method returns the value at the given row and column from the given
     * dataset.
     * 
     * @param  aRow     A row.
     * @param  aColumn  A column.
     * @param  aDataset A {@link CategoryDataset}.
     * @return Returns the value at the given row and column from the given
     *         dataset.
     */
    private double getValue(int aRow, int aColumn, CategoryDataset aDataset) {
        Number number = aDataset.getValue(aRow, aColumn);
        if (number == null) {return 0.0;}
        
        return ((Double)number).doubleValue();
    }
    
    /**
     * This method returns the lineColor.
     * 
     * @return Returns the lineColor.
     */
    public Color getLineColor() {
        return this.lineColor;
    }

    /**
     * This method sets the lineColor.
     *
     * @param aLineColor The lineColor to set.
     */
    public void setLineColor(Color aLineColor) {
        this.lineColor = aLineColor;
    }
    
    /**
     * This method returns the toolTipWithAxes.
     * 
     * @return Returns the toolTipWithAxes.
     */
    public boolean isToolTipWithAxes() {
        return this.toolTipWithAxes;
    }

    /**
     * This method sets the toolTipWithAxes.
     *
     * @param aToolTipWithAxes The toolTipWithAxes to set.
     */
    public void setToolTipWithAxes(boolean aToolTipWithAxes) {
        this.toolTipWithAxes = aToolTipWithAxes;
    }
    
    /**
     * The CacheEntry contains for one item all useful information (value, the
     * rendering info and the shape).
     */
    private class CacheEntry {
        
        /** The value.          */
        public double             value;
        /** The rendering info. */
        public SwingRenderingInfo info;
        /** The bar.            */
        public Rectangle2D        bar;
        
        /** 
         * Creates a {@link CacheEntry} with the
         * given parameters.
         * 
         * @param aValue A value.
         * @param anInfo A rendering info.
         * @param aBar   A bar.
         */
        public CacheEntry(double aValue, SwingRenderingInfo anInfo, Rectangle2D aBar) {
            this.value = aValue;
            this.info  = anInfo;
            this.bar   = aBar;
        }
    }
    
}

