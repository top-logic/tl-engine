/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.jfc;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItemSource;
import org.jfree.chart.block.Arrangement;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.title.LegendTitle;

/**
 * The LegendWithDimension stores during the writing process the
 * width and height of the legend.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public class LegendWithDimension extends LegendTitle {

    /** 
     * An optional container for wrapping the legend items (allows for adding
     * a title or other text to the legend). 
     */
    private BlockContainer wrapper;

    /** Creates a {@link LegendWithDimension}. */
    public LegendWithDimension(LegendItemSource aSource) {
        super(aSource);
    }

    /** Creates a {@link LegendWithDimension}. */
    public LegendWithDimension(LegendItemSource aSource, Arrangement aLayout) {
        super(aSource, aLayout, aLayout);
    }

    /**
     * Draws the block within the specified area.
     * 
     * @param g2  the graphics device.
     * @param area  the area.
     * @param params  ignored (<code>null</code> permitted).
     * 
     * @return An {@link org.jfree.chart.block.EntityBlockResult} or 
     *         <code>null</code>.
     */
    @Override
	public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        Rectangle2D target = (Rectangle2D) area.clone();
        target = trimMargin(target);
        if (getBackgroundPaint() != null) {
            g2.setPaint(getBackgroundPaint());
            g2.fill(target);
        }
        BlockFrame border = getFrame();
        border.draw(g2, target);
        border.getInsets().trim(target);
        BlockContainer container = getWrapper();
        if (container == null) {
            container = getItemContainer(); 
        }
        target = trimPadding(target);
        setWidth(target.getWidth());
        setHeight(target.getHeight());
        
        return container.draw(g2, target, params);   
    }
    
    @Override
	public void setWrapper(BlockContainer aWrapper) {
        super.setWrapper(aWrapper);
        
        this.wrapper = aWrapper;
    }
    
    @Override
	public BlockContainer getWrapper() {
        return this.wrapper;
    }
    
}

