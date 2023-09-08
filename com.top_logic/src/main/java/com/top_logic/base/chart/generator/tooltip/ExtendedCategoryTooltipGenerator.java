/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.tooltip;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;

/**
 * The AbstractExtendedCategoryTooltipGenerator works together with the {@link ExtendedCategoryDataset}.
 * The ExtendedCategoryDataset stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getTooltipFor(Object)}. This object is that from the ExtendedCategoryDataset.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedCategoryTooltipGenerator implements CategoryToolTipGenerator {

    /** 
     * @see org.jfree.chart.labels.CategoryToolTipGenerator#generateToolTip(org.jfree.data.category.CategoryDataset, int, int)
     */
    @Override
	public String generateToolTip(CategoryDataset aDataset, int aRow, int aColumn) {
        String theTooltip = "";
        
        if (aDataset instanceof ExtendedCategoryDataset) {
            ExtendedCategoryDataset theDataset = (ExtendedCategoryDataset) aDataset;
            
            theTooltip = getTooltipFor(theDataset.getObject(aRow, aColumn));
        } 
        
        return theTooltip;
    }

    /** 
     * This method returns the tooltip for the given object.
     * 
     * @param anObject A object.
     */
    public abstract String getTooltipFor(Object anObject);
    
}

