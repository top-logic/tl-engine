/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.axis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.reporting.chart.dataset.TreeTask;
import com.top_logic.reporting.common.tree.TreeAxis;

/**
 * Handle a CategoryAxis using {@link TreeTask}s.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TreeTaskAxis extends CategoryAxis {
    
    /** Indent every subtask by this may pixels */
    protected double indent = 15.0;
    
    /** Offset from label to line  */
    protected double labelOffset = 4.0;

    /** 
     * Create a new TreeCategoryAxis with a label.
     */
    public TreeTaskAxis(String aLabel) {
        super(aLabel);
    }
    
    /**
     * @see #indent
     */
    public void setIndent(double aIndent) {
        this.indent = aIndent;
    }

    /**
     * @see #labelOffset
     */
    public void setLabelOffset(double aLabelOffset) {
        this.labelOffset = aLabelOffset;
    }

    /**
     * Must override this a there is not hook based on category.
     */
    @Override
	public List<CategoryTick> refreshTicks(Graphics2D aGraphics2, AxisState aState, Rectangle2D aDataArea, RectangleEdge anEdge) {
        List<CategoryTick> theTicks = new ArrayList<>();
        
        // sanity check for data area...
        if (aDataArea.getHeight() <= 0.0 || aDataArea.getWidth() < 0.0) {
            return theTicks;
        }

        CategoryPlot thePlot       = (CategoryPlot) getPlot();
        List         theCategories = thePlot.getCategoriesForAxis(this);
        double       theMax        = 0.0;

        if (theCategories != null) {
            CategoryLabelPosition thePosition = this.getCategoryLabelPositions().getLabelPosition(anEdge);
            float                 theRatio    = this.getMaximumCategoryLabelWidthRatio();

            if (theRatio <= 0.0) {
                theRatio = thePosition.getWidthRatio();   
            }
                  
            float theFloat = 0.0f;

            if (thePosition.getWidthType() == CategoryLabelWidthType.CATEGORY) {
                theFloat = (float) calculateCategorySize(theCategories.size(), aDataArea, anEdge);  
            }
            else {
                if (RectangleEdge.isLeftOrRight(anEdge)) {
                    theFloat = (float) aDataArea.getWidth();
                }
                else {
                    theFloat = (float) aDataArea.getHeight();
                }
            }

            int theIndex = 0;

            for (Iterator theIt = theCategories.iterator(); theIt.hasNext(); ) {
                TreeTask  theTask     = (TreeTask) theIt.next();
                double    theCalcSize = labelOffset + theTask.getDepth() * indent;
                TextBlock theLabel    = this.createLabel(theTask, theFloat * theRatio, anEdge, aGraphics2);

                if (anEdge == RectangleEdge.TOP || anEdge == RectangleEdge.BOTTOM) {
                    theCalcSize += calculateTextBlockHeight(theLabel, thePosition,aGraphics2);
                }
                else if (anEdge == RectangleEdge.LEFT || anEdge == RectangleEdge.RIGHT) {
                    theCalcSize += calculateTextBlockWidth(theLabel, thePosition, aGraphics2);
                }

                theMax = Math.max(theMax,theCalcSize);

                theTicks.add(new CategoryTick(theTask, theLabel, thePosition.getLabelAnchor(), thePosition.getRotationAnchor(), thePosition.getAngle()));
                theIndex++;
            }
        }

        aState.setMax(theMax);
        
        return theTicks;
    }

    @Override
    protected TextBlock createLabel(Comparable aCategory, float aWidth, RectangleEdge anEdge, Graphics2D aGraphics2D) {
		return TextUtils.createTextBlock(((TreeTask) aCategory).getDescription(),
                                             this.getTickLabelFont(aCategory), this.getTickLabelPaint(aCategory), aWidth,
                                             this.getMaximumCategoryLabelLines(), new G2TextMeasurer(aGraphics2D));
    }

    @Override
	protected AxisState drawCategoryLabels(Graphics2D aGraphic2, Rectangle2D aPlotArea, Rectangle2D aDataArea, RectangleEdge anEdge, AxisState aState, PlotRenderingInfo aPlotState) {
        if (aState == null) {
            throw new IllegalArgumentException("Null 'state' argument.");
        }

        assert anEdge == RectangleEdge.LEFT : "Works only for " + RectangleEdge.LEFT;

        if (this.isTickLabelsVisible()) {       
            List<CategoryTick> theTicks      = this.refreshTicks(aGraphic2, aState, aPlotArea, anEdge);       
            List               theCategories = ((CategoryPlot) this.getPlot()).getCategoriesForAxis(this);
            int                theSize       = theTicks.size();
            int                theIndex      = 0;

            aState.setTicks(theTicks);     

            for (CategoryTick theTick : theTicks) {
                TreeTask   theTask     = (TreeTask) theCategories.get(theIndex);
                Comparable theCategory = theTick.getCategory();

                aGraphic2.setFont(this.getTickLabelFont(theCategory));
                aGraphic2.setPaint(this.getTickLabelPaint(theCategory));

                double theDepth  = theTask.getDepth() * this.indent;
                double theX1     = aState.getCursor() - this.getCategoryLabelPositionOffset();
                double theX0     = theX1 - aState.getMax();
                double theY0     = this.getCategoryStart(theIndex, theSize, aDataArea, anEdge);
                double theY1     = this.getCategoryEnd(theIndex, theSize, aDataArea, anEdge);

                Rectangle2D           theArea        = new Rectangle2D.Double(theX0, theY0, (theX1 - theX0), (theY1 - theY0));
				Point2D thePoint = RectangleAnchor.LEFT.getAnchorPoint(theArea);
                CategoryLabelPosition thePosition    = this.getCategoryLabelPositions().getLabelPosition(anEdge);
                TextBlockAnchor       theLabelAnchor = thePosition.getLabelAnchor();
                double                theAngle       = thePosition.getAngle();
                double                theX           = thePoint.getX();
                double                theY           = thePoint.getY();

                // Draw the icon before the label.
                Image       theIcon    = ThemeFactory.getTheme().getImage(theTask.getType());
                Rectangle2D theIconBox = TreeAxis.drawImage(aGraphic2, (int) theDepth, aPlotArea, (int) theY, theIcon);

                // Now draw the label.
                TextBlock theBlock   = theTick.getLabel();
                double    theXAnchor = theIconBox.getMaxX() + this.labelOffset;
                double    theYAnchor = theIconBox.getY();

                theBlock.draw(aGraphic2, (float) theXAnchor, (float) theYAnchor, TextBlockAnchor.TOP_LEFT, 
                                         (float) theX,       (float) theY,       theAngle);

                // Draw the line to our parent.
                int theParentIndex = theTask.getParentIndex();

                if (theParentIndex >= 0 && theParentIndex < theCategories.size()) { // parentIndex may be out of range ?
                    TreeTask theParentCategory = (TreeTask) theCategories.get(theParentIndex);
                    double   theSubIndent      = this.indent / 2;

                    theXAnchor -= this.labelOffset; // Do not hit Label with line.
                    theYAnchor += theSubIndent;

                    if (theDepth > 0) {
                        double theParentX = (theParentCategory.getDepth() + 1) * this.indent;
                        double theParentY = (this.getCategoryStart(theParentIndex, theSize, aDataArea, anEdge) + this.getCategoryEnd(theParentIndex, theSize, aDataArea, anEdge)) / 2.0;
                        Line2D theHorLine = new Line2D.Double(theParentX, theYAnchor,                theIconBox.getX(), theYAnchor);
                        Line2D theVerLine = new Line2D.Double(theParentX, theParentY + theSubIndent, theParentX,        theIconBox.getY() + theSubIndent);
    
                        aGraphic2.setPaint(Color.LIGHT_GRAY);
                        aGraphic2.draw(theHorLine);
                        aGraphic2.draw(theVerLine);
                    }
                }

                Shape theBounds = theBlock.calculateBounds(aGraphic2,      (float) theX, (float) theY, 
                                                           theLabelAnchor, (float) theX, (float) theY, theAngle);

                if (aPlotState != null && aPlotState.getOwner() != null) {
                    EntityCollection theEntities = aPlotState.getOwner().getEntityCollection();

                    if (theEntities != null) {
                        String theTooltip = this.getCategoryLabelToolTip(theCategory);

                        theEntities.add(new CategoryLabelEntity(theCategory, theBounds, theTooltip, null));
                    }
                }

                theIndex++;
            }

            aState.cursorLeft(aState.getMax() + this.getCategoryLabelPositionOffset());
        }

        return aState;
    }
}