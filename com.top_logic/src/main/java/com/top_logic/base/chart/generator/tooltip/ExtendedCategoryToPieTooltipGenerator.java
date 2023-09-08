/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.tooltip;

import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.data.general.PieDataset;

import com.top_logic.base.chart.dataset.ExtendedCategoryToPieDataset;

/**
 * The ExtendedCategoryToPieTooltipGenerator works together with the {@link ExtendedCategoryToPieDataset}.
 * The ExtendedCategoryToPieDataset stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getTooltipFor(Object)}. This object is that from the ExtendedCategoryToPieDataset.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedCategoryToPieTooltipGenerator implements PieToolTipGenerator {

    @Override
	public String generateToolTip(PieDataset aDataset, Comparable aKey) {
        String theTooltip = "";
        
        if (aDataset instanceof ExtendedCategoryToPieDataset) {
            ExtendedCategoryToPieDataset theDataset = (ExtendedCategoryToPieDataset) aDataset;
            
            theTooltip = getTooltipFor(theDataset.getObject(aDataset.getIndex(aKey)));
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

