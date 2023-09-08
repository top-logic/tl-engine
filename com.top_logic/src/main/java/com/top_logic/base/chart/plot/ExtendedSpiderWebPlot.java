/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;

/**
 * Extension of the SpiderWebPlot.
 * 
 * This Extension allows to create a SpiderWebPlot that doesen't display the legend
 * for the first n (given in the constructor) series in the data set. This allows using
 * these series as a kind of background-image.
 * 
 * Series that should be displayed with an outline only needs to be given in as set of
 * integer in the constructor (because of a bug in the SpiderWebPlot rendering).
 * 
 * @author     <a href="mailto:cca@top-logic.com">Christian Cantorino</a>
 */
public class ExtendedSpiderWebPlot extends SpiderWebPlot {

	private int backgroundPlots;
    private int ignoreLegend;
    private Set<Integer> outlineSeries;

    /**
     * The given number of series in the given data set will be ignored when writing a legend.
     * Series in the Set outlineSeries will be displayed as outline only.
     * 
     * @param    aDataset         The data set to be displayed, must not be <code>null</code>.
     * @param    ignorePlots      Number of plots not be written.
     * @param    outlineSeries    Set of flags, if ...
     */
    public ExtendedSpiderWebPlot(DefaultCategoryDataset aDataset, int ignorePlots, Set<Integer> outlineSeries) {
        super(aDataset);

        this.backgroundPlots = ignorePlots;
        this.ignoreLegend    = ignorePlots;
        this.outlineSeries   = outlineSeries;
    }

    @Override
	protected void drawRadarPoly(Graphics2D aGraph2D, Rectangle2D anArea, Point2D aCentre, PlotRenderingInfo anInfo, int aSeries, int aCatCount, double headH, double headW) {
        Polygon          thePolygon  = new Polygon();
        EntityCollection theEntities = null;

        if (anInfo != null) {
            theEntities = anInfo.getOwner().getEntityCollection();
        }

        CategoryToolTipGenerator theTooltipGenerator = this.getToolTipGenerator();
        CategoryURLGenerator     theURLGenerator     = this.getURLGenerator();
        CategoryDataset          theDataSet          = this.getDataset();

        if (theTooltipGenerator == null) {
        	theTooltipGenerator = new CategoryToolTipGenerator() {

				@Override
				public String generateToolTip(CategoryDataset aDataset, int aRow, int aColumn) {
					Comparable<?> theKey = aDataset.getColumnKey(aColumn);

					return (theKey instanceof String) ? (String) theKey : null;
				}
			};
        }

        double theFactor     = this.getDirection().getFactor();
        double theStartAngle = this.getStartAngle();
        double theMaxValue   = this.getMaxValue();
        Paint  thePaint      = this.getSeriesPaint(aSeries);
        Paint  theOutline    = this.getSeriesOutlinePaint(aSeries);
        Stroke theStroke     = this.getSeriesOutlineStroke(aSeries);

        // Plot the data...
        for (int theCat = 0; theCat < aCatCount; theCat++) {
            Number theDataValue = this.getPlotValue(aSeries, theCat);

            if (theDataValue != null) {
                double theValue = theDataValue.doubleValue();

                if (theValue >= 0) { // draw the polygon series...
                    // Finds our starting angle from the center for this axis
					double theAngle = theStartAngle + (theFactor * theCat * 360 / aCatCount);

                    // The following angle calc will ensure there isn't a top
                    // vertical axis - this may be useful if you don't want any
                    // given criteria to 'appear' move important than the
                    // others..
                    // + (getDirection().getFactor()
                    // * (cat + 0.5) * 360 / catCount);

                    // find the point at the appropriate distance end point
                    // along the axis/angle identified above and add it to the
                    // polygon

					Point2D thePoint = this.getWebPoint(anArea, theAngle, theValue / theMaxValue);

					thePolygon.addPoint((int) thePoint.getX(), (int) thePoint.getY());

                    // put an ellipse at the point being plotted..

					aGraph2D.setPaint(thePaint);
					aGraph2D.setStroke(theStroke);
					aGraph2D.setPaint(theOutline);

					drawHeadPoint(aSeries, aGraph2D, thePoint, headH, headW);

                    if (theEntities != null) {
                        String theURL     = null;
                        String theTooltip = theTooltipGenerator.generateToolTip(theDataSet, aSeries, theCat);

						if (theURLGenerator != null) {
                            theURL = theURLGenerator.generateURL(theDataSet, aSeries, theCat);
                        }

                        Shape theArea = new Rectangle(
                                                   (int) (thePoint.getX() - headW),
                                                   (int) (thePoint.getY() - headH),
                                                   (int) (headW * 2),
                                                   (int) (headH * 2));

                        theEntities.add(new CategoryItemEntity(theArea,
                                                               theTooltip,
                                                               theURL,
                                                               theDataSet,
                                                               aSeries,
//                                                                           theDataSet.getColumnKey(theCat),
                                                               theCat));
                    }

                }
            }
        }

        // Plot the polygon
        aGraph2D.setPaint(thePaint);
        aGraph2D.setStroke(theStroke);
        aGraph2D.draw(thePolygon);

        // Lastly, fill the web polygon if this is required

        // hack because SpiderWebCharts acts stupid...
        // if we don't want a series to be filled but we want on outline. 
        // for some reason the renderer doesen't show an outline even if it is filled transparency
        // that's why only series not contained in the list will be filled
		if (this.isWebFilled() && !this.outlineSeries.contains(Integer.valueOf(aSeries))) {
            aGraph2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            aGraph2D.fill(thePolygon);
            aGraph2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.getForegroundAlpha()));
        }
    }

	/**
	 * Draws the head point (on the axis) for the kind of series.
	 * 
	 * @param series
	 *        the series currently painted
	 * @param graphics
	 *        the graphics device.
	 * @param point
	 *        the point locate coordinates of the head point
	 * @param headH
	 *        the data point height
	 * @param headW
	 *        the data point width
	 * 
	 * @see #drawRadarPoly(Graphics2D, Rectangle2D, Point2D, PlotRenderingInfo, int, int, double,
	 *      double)
	 */
	protected void drawHeadPoint(int series, Graphics2D graphics, Point2D point, double headH, double headW) {
		double x = point.getX() - headW / 2;
		double y = point.getY() - headH / 2;
		Ellipse2D headPoint = new Ellipse2D.Double(x, y, headW, headH);

		graphics.fill(headPoint);
		graphics.draw(headPoint);
	}

	/**
	 * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
	 * 
	 * @param aGraph2D
	 *        the graphics device.
	 * @param anArea
	 *        the area within which the plot should be drawn.
	 * @param anAnchor
	 *        the anchor point (<code>null</code> permitted).
	 * @param parentState
	 *        the state from the parent plot, if there is one.
	 * @param anInfo
	 *        collects info about the drawing.
	 */
    @Override
	public void draw(Graphics2D aGraph2D, Rectangle2D anArea, Point2D anAnchor, PlotState parentState, PlotRenderingInfo anInfo) {
        this.ignoreLegend = this.backgroundPlots;

        this.getInsets().trim(anArea);

        if (anInfo != null) {
            anInfo.setPlotArea(anArea);
            anInfo.setDataArea(anArea);
        }

        this.drawBackground(aGraph2D, anArea);

        Shape theSavedClip = aGraph2D.getClip();

        aGraph2D.clip(anArea);

        Composite theComposite = aGraph2D.getComposite();

        aGraph2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.getForegroundAlpha()));

        this.drawDataset(aGraph2D, anArea, anInfo, this.getDataset());

        aGraph2D.setClip(theSavedClip);
        aGraph2D.setComposite(theComposite);
        this.drawOutline(aGraph2D, anArea);
    }

    @Override
	public LegendItemCollection getLegendItems() {
        LegendItemCollection theResult  = new LegendItemCollection();
        TableOrder           theOrder   = this.getDataExtractOrder();
        CategoryDataset      theDataset = this.getDataset();
        List<?>              theKeys    = null;

		if (theOrder == TableOrder.BY_ROW) {
            theKeys = theDataset.getRowKeys();
        }
        else if (theOrder == TableOrder.BY_COLUMN) {
            theKeys = theDataset.getColumnKeys();
        }

        if (theKeys != null) {
            int   theSeries = 0;
            Shape theShape  = this.getLegendItemShape();

            for (Iterator<?> theIt = theKeys.iterator(); theIt.hasNext(); ) {
                String     theLabel   = theIt.next().toString();
                String     theDesc    = theLabel;
                Paint      thePaint   = this.outlineSeries.contains(Integer.valueOf(theSeries)) ? new Color(1f, 1f, 1f, 0f) : this.getSeriesPaint(theSeries);
                Paint      theOutline = this.getSeriesOutlinePaint(theSeries);
                Stroke     theStroke  = this.getSeriesOutlineStroke(theSeries);
                LegendItem theItem    = new LegendItem(theLabel, theDesc, null, null, theShape, thePaint, theStroke, theOutline);

                if (this.ignoreLegend < 1) {
                    theResult.add(theItem);
                }

                this.ignoreLegend--;

                theSeries++;
            }
        }

        return theResult;
    }

    /** 
     * Draw the data to the spider chart.
     * 
     * @param    aGraph2D    The graphic object to write to, must not be <code>null</code>.
     * @param    anArea      The allowed drawing area, must not be <code>null</code>.
     * @param    anInfo      Some information about the rendering, must not be <code>null</code>.
     * @param    aSet        The values to be draw to the chart, must not be <code>null</code>.
     * @see      #drawRadarPoly(Graphics2D, Rectangle2D, Point2D, PlotRenderingInfo, int, int, double, double)
     */
    protected void drawDataset(Graphics2D aGraph2D, Rectangle2D anArea, PlotRenderingInfo anInfo, CategoryDataset aSet) {
		if (!DatasetUtils.isEmptyOrNull(aSet)) {
            int theSeriesCount = 0, theCatCount = 0;

            if (getDataExtractOrder() == TableOrder.BY_ROW) {
                theSeriesCount = aSet.getRowCount();
                theCatCount    = aSet.getColumnCount();
            }
            else {
                theSeriesCount = aSet.getColumnCount();
                theCatCount    = aSet.getRowCount();
            }

            // ensure we have a maximum value to use on the axes
            if (getMaxValue() == DEFAULT_MAX_VALUE) {
            	calculateMaxValue(theSeriesCount, theCatCount);
            }

            // Next, setup the plot area 
            // adjust the plot area by the interior spacing value

            double theInteriorGap = this.getInteriorGap();
			double theGapHori     = anArea.getWidth() * theInteriorGap;
            double theGapVert     = anArea.getHeight() * theInteriorGap;

            double X = anArea.getX() + theGapHori / 2;
            double Y = anArea.getY() + theGapVert / 2;
            double W = anArea.getWidth() - theGapHori;
            double H = anArea.getHeight() - theGapVert;

            double headW = anArea.getWidth() * this.headPercent;
            double headH = anArea.getHeight() * this.headPercent;

            // make the chart area a square
            double min = Math.min(W, H) / 2;
            X = (X + X + W) / 2 - min;
            Y = (Y + Y + H) / 2 - min;
            W = 2 * min;
            H = 2 * min;

            Point2D     theCentre    = new Point2D.Double(X + W / 2, Y + H / 2);
            Rectangle2D theRadarArea = new Rectangle2D.Double(X, Y, W, H);
            
            // Now actually plot each of the series polygons..
            for (int theSeries = 0; theSeries < theSeriesCount; theSeries++) {
                this.drawRadarPoly(aGraph2D, theRadarArea, theCentre, anInfo, theSeries, theCatCount, headH, headW);
            }

            CategoryItemLabelGenerator theLabelGenerator = this.getLabelGenerator();
            boolean                    isSpiderLabel     = (theLabelGenerator instanceof SpiderChartLabelGenerator);

            if (isSpiderLabel) {
                ((SpiderChartLabelGenerator) theLabelGenerator).prepare(anArea, theRadarArea, aGraph2D.getFontRenderContext(), this.getLabelFont());
            }

            // draw the axis and category label
            double theFactor     = this.getDirection().getFactor();
            double theStartAngle = this.getStartAngle();
            Paint  theAxisPaint  = this.getAxisLinePaint();
            Stroke theAxisStroke = this.getAxisLineStroke();

            for (int theCat = 0; theCat < theCatCount; theCat++) {
				double  theAngle    = theStartAngle + (theFactor * theCat * 360 / theCatCount);
                Point2D theEndPoint = this.getWebPoint(theRadarArea, theAngle, 1); // 1 = end of axis
                Line2D  theLine     = new Line2D.Double(theCentre, theEndPoint);

				aGraph2D.setPaint(theAxisPaint);
				aGraph2D.setStroke(theAxisStroke);
                aGraph2D.draw(theLine);

                this.drawLabel(aGraph2D, theRadarArea, 0.0, theCat, theAngle, 360.0 / theCatCount);
            }

            if (isSpiderLabel) {
                ((SpiderChartLabelGenerator) theLabelGenerator).reset();
            }
        }
        else { 
            this.drawNoDataMessage(aGraph2D, anArea);
        }
    }

    /**
     * Loop through each of the series to get the maximum value on each category axis.
     *
     * @param aSeriesCount  The number of series.
     * @param aCatCount     The number of categories.
     */
    private void calculateMaxValue(int aSeriesCount, int aCatCount) {
        double theValue  = 0;
        Number theNumber = null;

        for (int theSeries = 0; theSeries < aSeriesCount; theSeries++) {
            for (int theCat = 0; theCat < aCatCount; theCat++) {
                theNumber = this.getPlotValue(theSeries, theCat);

                if (theNumber != null) {
                    theValue = theNumber.doubleValue();

                    if (theValue > getMaxValue()) { 
                        this.setMaxValue(theValue);
                    }   
                }
            }
        }
    }

    /**
     * Label generator which will only create labels of a defined maximum width. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class SpiderChartLabelGenerator extends StandardCategoryItemLabelGenerator {

        // Attributes

        private FontRenderContext context;
        private Font font;
        private double maxWidth;
//		private double maxWidth;

        // Constructors

        /** 
         * Creates a {@link SpiderChartLabelGenerator}.
         */
        public SpiderChartLabelGenerator() {
        }

        // Overridden methods from StandardCategoryItemLabelGenerator

        @Override
		public String generateColumnLabel(CategoryDataset aDataset, int aColumn) {
            return this.trim(super.generateColumnLabel(aDataset, aColumn));
        }

        @Override
		public String generateRowLabel(CategoryDataset aDataset, int aRow) {
            return this.trim(super.generateRowLabel(aDataset, aRow));
        }

        // Public methods

        /** 
         * Prepare the label generator with the graphical context.
         * 
         * @param    aContext    The context, must not be <code>null</code>.
         * @param    aFont       The used font, must not be <code>null</code>.
         */
        public void prepare(Rectangle2D anArea, Rectangle2D aRadar, FontRenderContext aContext, Font aFont) {
            this.maxWidth = ((anArea.getWidth() - aRadar.getWidth()) / 2) - 4;
			this.context  = aContext;
            this.font     = aFont;
        }

        /** 
         * Reset the generator after usage.
         */
        public void reset() {
            this.context = null;
            this.font    = null;
        }

        // Protected methods

        /** 
         * Short the given label, if it is larger than the {@link #maxWidth}.
         * 
         * @param    aLabel    The label to be checked, must not be <code>null</code>.
         * @return   The label, which will have a painted maximum length of {@link #maxWidth}.
         */
        protected String trim(String aLabel) {
        	String      theLabel  = aLabel;
        	Rectangle2D theBounds = this.font.getStringBounds(aLabel, this.context);
        	int         theCount  = 0;

        	theLabel  = aLabel;

        	while (theBounds.getWidth() >= this.maxWidth) {
        		int theLength = theLabel.length() - 1;

        		theLabel  = theLabel.substring(0, theLength);
        		theBounds = this.font.getStringBounds(theLabel + "...", this.context);
        		theCount++;
        	}

        	if (theCount > 0) {
        		theLabel += "...";
        	}

        	return theLabel;
        }
    }
}
