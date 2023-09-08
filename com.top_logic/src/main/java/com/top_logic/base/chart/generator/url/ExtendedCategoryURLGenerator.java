/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.url;

import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;

/**
 * The AbstractExtendedCategoryURLGenerator works together with the {@link ExtendedCategoryDataset}.
 * The ExtendedCategoryDataset stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getURLFor(Object)}. This object is that from the ExtendedCategoryDataset.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedCategoryURLGenerator implements
        CategoryURLGenerator {

    /** 
     * @see org.jfree.chart.urls.CategoryURLGenerator#generateURL(org.jfree.data.category.CategoryDataset, int, int)
     */
    @Override
	public String generateURL(CategoryDataset aDataset, int aRow, int aCategory) {
        String theUrl = "";
        
        if (aDataset instanceof ExtendedCategoryDataset) {
            ExtendedCategoryDataset theDataset = (ExtendedCategoryDataset) aDataset;
            
            theUrl = getURLFor(theDataset.getObject(aRow, aCategory));
        }
        return theUrl;
    }

    /** 
     * This method returns the url for the given object.
     * 
     * 
     * @param anObject A object.
     */
    public abstract String getURLFor(Object anObject);
    
}

