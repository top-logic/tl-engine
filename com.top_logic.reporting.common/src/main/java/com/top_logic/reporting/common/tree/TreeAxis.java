/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.tree;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import com.top_logic.basic.Logger;

/**
 * Renders a tree as symbolic y-axis instead of normal labels or values.
 * 
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TreeAxis extends SymbolAxis {

    /** The rendering information for the tree. */
    private TreeInfo[]       infos;
    /** The icons which be displayed in front of the labels. */
    private Image[]          icons;
    /** The tree depth. */
    private int              treeDepth;
    /** The distance between two tree icons on the x-axis. */
    private int              treeIconsDistance;
    /** The distance between the icon and the label on the x-axis. */
    private int              iconLabelDistance;
    /** Image map entities for the image map. */
    private EntityCollection entities;
    
    /** 
     * Creates a {@link TreeAxis} with the
     * given parameters.
     * 
     * @param someTreeInfos A array of {@link TreeInfo}s.
     * @param someIcons     A array of icons which be displayed before the labels.
     */
    public TreeAxis(TreeInfo[] someTreeInfos, Image[] someIcons) {
        super("", getTreeNodeLabels(someTreeInfos));
        
        this.infos             = someTreeInfos;
        this.icons             = someIcons;
        this.treeDepth         = getTreeDepth(someTreeInfos);
        this.treeIconsDistance = 15;
        this.iconLabelDistance =  5;
        this.entities          = new StandardEntityCollection();
    }

    @Override
	protected double findMaximumTickLabelWidth(List someTicks, Graphics2D aG2,
                                               Rectangle2D aDataArea, boolean aVertical) {
        double theMax  = 0.0;
        int    theSize = someTicks.size();

        if (theSize == infos.length) {
            RectangleInsets theInsets  = this.getTickLabelInsets();
            Font            theFont    = this.getTickLabelFont();
            FontMetrics     theMetrics = aG2.getFontMetrics(theFont);
        
            for (int i = 0; i < theSize; i++) {
                Tick        theTick   = (Tick) someTicks.get(i);
				Rectangle2D theBounds = TextUtils.getTextBounds(theTick.getText(),
                                                                    aG2, theMetrics);
                double indention = (infos[i].getDepth() * treeIconsDistance) + 
                                   treeIconsDistance;
                double currWidth = theBounds.getWidth() + theInsets.getLeft() + 
                                   theInsets.getRight() + indention;

                theMax = Math.max(theMax, currWidth);
            }
        }
        return theMax;
    }
    
    @Override
	protected AxisState drawTickMarksAndLabels(Graphics2D aG2, double aCursor,
                                               Rectangle2D aPlotArea,
                                               Rectangle2D aDataArea, 
                                               RectangleEdge aEdge) {
        AxisState theState = new AxisState(aCursor);

        if (calculateVisibleTickCount() > ValueAxis.MAXIMUM_TICK_COUNT) {
            Logger.warn("Tree axis couldn't drawn because to many elements ('" + calculateVisibleTickCount() + "') for the tree the maximum tick count is ('" + ValueAxis.MAXIMUM_TICK_COUNT + "') ", this);
        }
        
        List theTicks = this.refreshTicks(aG2, theState, aDataArea, aEdge);
        int  theSize  = theTicks.size();

        if (theSize == infos.length) {
            theState.setTicks(theTicks);
            aG2.setFont(getTickLabelFont());

            for (int i = 0; i < theSize; i++) {
                ValueTick theTick = (ValueTick)theTicks.get(i);

                if (isTickLabelsVisible()) {
                    aG2.setPaint(getTickLabelPaint());
                    float[]     anchorPoint = calculateAnchorPoint(theTick, aCursor, aDataArea, 
                                                                   aEdge);
                
                    /* 
                     * Draw the icon before the label.
                     */
                    TreeInfo    currentInfo = infos[i];
                    Image       icon        = icons[currentInfo.getIconIndex()];
                    int         indentation = currentInfo.getDepth() * treeIconsDistance;
                    Rectangle2D iconBounds  = TreeAxis.drawImage(aG2, indentation, aPlotArea, (int) anchorPoint[1], icon);
                    int         iconX       = (int) iconBounds.getMinX();
                    int         iconH       = (int) iconBounds.getHeight();
                
                    /*
                     * Draw the label after the icon.
                     * 
                     * Note the method calculateRotatedStringBounds calculates the x and y
                     * coordinate to display the label on a normal y-axis not for the 
                     * tree axis. We have to adjust the coordinates.
                     */
                    String theTickLabel = currentInfo.getLabel();
					Shape labelShape = TextUtils.calculateRotatedStringBounds(
                                                                    theTickLabel,
                                                                    aG2,
                                                                    anchorPoint[0],
                                                                    anchorPoint[1],
                                                                    theTick.getTextAnchor(),
                                                                    theTick.getAngle(),
                                                                    theTick.getRotationAnchor());
                
                    if (labelShape == null) { continue; }
                
                    Rectangle2D labelBounds = labelShape.getBounds2D();
                
                    int labelX  = (int)(iconBounds.getX() + iconBounds.getWidth()
                                  + iconLabelDistance     + labelBounds.getWidth());
                    int anchorY = (int)anchorPoint[1];
					TextUtils.drawRotatedString(theTickLabel, aG2, labelX, anchorY,
                                                    theTick.getTextAnchor(), theTick.getAngle(),
                                                    theTick.getRotationAnchor());
                    labelX -= labelBounds.getWidth();
                    labelBounds.setRect(labelX, anchorY - (labelBounds.getHeight() / 2),
                                        labelBounds.getWidth(), labelBounds.getHeight());
                
                    /* 
                     * Draw the lines to parent.
                     */
                    int parentIndex = currentInfo.getParent();
                    if (parentIndex != i) {
                        double xHLine = aPlotArea.getMinX() + (iconH / 2)
                                        + (infos[parentIndex].getDepth() * treeIconsDistance);
                        Line2D hLine = new Line2D.Double(xHLine, anchorY, iconX, anchorY);
                        aG2.setPaint(Color.LIGHT_GRAY);
                        aG2.draw(hLine);
                        
                        ValueTick parentTick = (ValueTick) theTicks.get(parentIndex);
                        float[] anchorPoint2 = calculateAnchorPoint(parentTick, aCursor,
                                                                    aDataArea, aEdge);
                        Line2D  vLine        = new Line2D.Double(xHLine, anchorY, xHLine,
                                                                 anchorPoint2[1]);
                        aG2.setPaint(Color.LIGHT_GRAY);
                        aG2.draw(vLine);
                    }
                
                    /*
                     * Add the image map entries for the icon and label.
                     */
                    entities.add(new ChartEntity(labelBounds, currentInfo.getLabelTooltip(),
                                                 currentInfo.getLabelUrl()));
                    entities.add(new ChartEntity(iconBounds, currentInfo.getIconTooltip(),
                                                 currentInfo.getIconUrl()));
                }
            }
        }
        return theState;
    }
    
    public static Rectangle2D drawImage(Graphics2D aGraph, int anIndent, Rectangle2D aPlotArea, int anYCoord, Image anIcon) {
        int         theW      = anIcon.getWidth(null);
        int         theH      = anIcon.getHeight(null);
        int         theX      = (int)(aPlotArea.getMinX() + anIndent);
        int         theY      = (anYCoord - (theH / 2));
        Rectangle2D theResult = new Rectangle2D.Double(theX, theY, theW, theH);

        if (aGraph.drawImage(anIcon, theX, theY, theW, theH, null)) { 
            return theResult;
        }
        else {
            return null;
        }
    }
    /** 
     * This method returns the tree depth for the given tree infos.
     * 
     * @param  aSomeTreeInfos A array of {@link TreeInfo}s.
     * @return Returns the tree depth for the given tree infos.
     */
    private static int getTreeDepth(TreeInfo[] aSomeTreeInfos) {
        int max = 0;
        for (int i = 0; i < aSomeTreeInfos.length; i++) {
            max = Math.max(max, aSomeTreeInfos[i].getDepth());
        }
        return max;
    }

    /** 
     * This method returns the labels of all given tree nodes in a string array.
     * 
     * @param  aSomeTreeInfos A array of {@link TreeInfo}s.
     * @return Returns the labels of all given tree nodes in a string array.
     */
    private static String[] getTreeNodeLabels(TreeInfo[] aSomeTreeInfos) {
        String[] labels = new String[aSomeTreeInfos.length];
        for (int i = 0; i < aSomeTreeInfos.length; i++) {
           labels[i] = aSomeTreeInfos[i].getLabel(); 
        }
        return labels;
    }

    /**
     * This method returns the icons.
     * 
     * @return Returns the icons.
     */
    public Image[] getIcons() {
        return this.icons;
    }

    /**
     * This method sets the icons.
     *
     * @param aIcons The icons to set.
     */
    public void setIcons(Image[] aIcons) {
        this.icons = aIcons;
    }
    
    /**
     * This method returns the iconLabelDistance.
     * 
     * @return Returns the iconLabelDistance.
     */
    public int getIconLabelDistance() {
        return this.iconLabelDistance;
    }

    /**
     * This method sets the iconLabelDistance.
     *
     * @param aIconLabelDistance The iconLabelDistance to set.
     */
    public void setIconLabelDistance(int aIconLabelDistance) {
        this.iconLabelDistance = aIconLabelDistance;
    }

    /**
     * This method returns the treeDepth.
     * 
     * @return Returns the treeDepth.
     */
    public int getTreeDepth() {
        return this.treeDepth;
    }
    
    /**
     * This method sets the treeDepth.
     *
     * @param aTreeDepth The treeDepth to set.
     */
    public void setTreeDepth(int aTreeDepth) {
        this.treeDepth = aTreeDepth;
    }
    
    /**
     * This method returns the treeIconsDistance.
     * 
     * @return Returns the treeIconsDistance.
     */
    public int getTreeIconsDistance() {
        return this.treeIconsDistance;
    }
    
    /**
     * This method sets the treeIconsDistance.
     *
     * @param aTreeIconsDistance The treeIconsDistance to set.
     */
    public void setTreeIconsDistance(int aTreeIconsDistance) {
        this.treeIconsDistance = aTreeIconsDistance;
    }

    /**
     * This method returns the entities.
     * 
     * @return Returns the entities.
     */
    public EntityCollection getEntities() {
        return this.entities;
    }
    
    /**
     * This method sets the entities.
     *
     * @param aEntities The entities to set.
     */
    public void setEntities(EntityCollection aEntities) {
        this.entities = aEntities;
    }
    
    /**
     * This method returns the infos.
     * 
     * @return Returns the infos.
     */
    public TreeInfo[] getInfos() {
        return this.infos;
    }
    
    /**
     * This method sets the infos.
     *
     * @param aInfos The infos to set.
     */
    public void setInfos(TreeInfo[] aInfos) {
        this.infos = aInfos;
    }
}

