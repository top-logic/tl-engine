/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.util;

import java.text.NumberFormat;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;

/**
 * The SwingToolTipGenerator generates the tooltips for the 
 * {@link com.top_logic.reporting.chart.renderer.SwingRenderer}. 
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class SwingToolTipGenerator implements CategoryToolTipGenerator {

    /** The format is used to format the number of the {@link SwingRenderingInfo}. */
    private NumberFormat format;

    /** 
     * Creates a {@link SwingToolTipGenerator}. 
     */
    public SwingToolTipGenerator() {
        this.format = null;
    }
    
    /**
     * Creates a {@link SwingToolTipGenerator}.
     * 
     * @param aFormat
     *        See {@link #format}.
     */
    public SwingToolTipGenerator(NumberFormat aFormat) {
        this.format = aFormat;
    }

    /**
     * This method returns the formatted tooltip (if a format is set) from
     * the {@link SwingRenderingInfo}.
     * 
     * @see org.jfree.chart.labels.CategoryToolTipGenerator#generateToolTip(org.jfree.data.category.CategoryDataset,int, int)
     */
    @Override
	public String generateToolTip(CategoryDataset aDataset, int aRow, int aColumn) {
        SwingRenderingInfo info = ((SwingDataset)aDataset).getRenderingInfo(aRow, aColumn);
       
        if (info == null) { return ""; }
        
        if (this.format == null) {
            return String.valueOf(info.getValue());
        } else {
            return this.format.format(info.getValue());
        }
    }

}

